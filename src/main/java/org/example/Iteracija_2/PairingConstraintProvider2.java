package org.example.Iteracija_2;
import org.example.Main;
import org.example.Mentor;
import org.example.Student;
import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import org.optaplanner.core.api.score.stream.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.stream.Collectors;

public class PairingConstraintProvider2 implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                capacityConstraint(constraintFactory),
                stability1(constraintFactory),
                stability2(constraintFactory),
                averageCompatibility(constraintFactory)
        };
    }

    private Constraint capacityConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Student.class)
                .filter(student -> !student.getMentor().getId().equals("NULL"))
                .groupBy(
                        Student::getMentor,
                        ConstraintCollectors.toList(Student::getCompatibility)
                )
                .filter((mentor, comp) -> comp.size() >= mentor.getCapacity())
                .groupBy(
                        (mentor, comp) -> mentor,
                        (mentor, comp) -> comp.size() - mentor.getCapacity(),
                        (mentor, comp) ->
                                comp.stream()
                                        .sorted(Comparator.reverseOrder())
                                        .collect(Collectors.toList())
                                        .subList(0, mentor.getCapacity())
                                        .stream().mapToDouble(n -> n).average().getAsDouble()
                )
                .groupBy(
                        ConstraintCollectors.sum((mentor, count, avgComp) -> count),
                        ConstraintCollectors.sumBigDecimal((mentor, count, avgComp) -> BigDecimal.valueOf(avgComp))
                )
                .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_HARD, (count, avgAvgComp) ->
                        count == 0
                                ? BigDecimal.ZERO
                                : BigDecimal.valueOf(
                                (1 / (Main.numOfMentors + 1)) *
                                        (count + (1 / (1 + avgAvgComp.doubleValue() / Main.numOfMentors)))
                        )
                )
                .asConstraint("Capacity");
    }

    private Constraint stability1(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Student.class)
                .filter(student -> !student.getMentor().getId().equals("NULL"))
                .groupBy(
                        Student::getMentor,
                        ConstraintCollectors.toList(Student::getCompatibility)
                )
                .groupBy(
                        (mentor, ranks) -> mentor,
                        (mentor, ranks) ->
                                mentor.getCapacity() > ranks.size()
                                        ? 0.0
                                        : ranks.stream()
                                            .sorted(Comparator.reverseOrder())
                                            .collect(Collectors.toList())
                                            .get(mentor.getCapacity() - 1)
                )
                .join(constraintFactory.forEach(Student.class),
                        Joiners.filtering((mentor, worstRank, student) ->
                                student.isMoreCompatible(mentor) && mentor.calculateCompatibility(student) > worstRank
                        )
                )
                .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_HARD, (mentor, worstRank, student) ->
                        BigDecimal.valueOf(mentor.calculateCompatibility(student))
                )
                .asConstraint("Stability1");
    }

    private Constraint stability2(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Mentor.class)
                .filter(mentor -> !mentor.getId().equals("NULL"))
                .ifNotExists(
                        Student.class,
                        Joiners.equal(mentor -> mentor, Student::getMentor)
                )
                .join(constraintFactory.forEach(Student.class),
                        Joiners.filtering((mentor, student) ->
                                student.isMoreCompatible(mentor)
                        )
                )
                .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_HARD, (mentor, student) ->
                        BigDecimal.valueOf(mentor.calculateCompatibility(student))
                )
                .asConstraint("Stability2");
    }

    private Constraint averageCompatibility(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Student.class)
                .filter(student -> student.getCompatibility() != 0)
                .groupBy(
                        ConstraintCollectors.averageBigDecimal(student -> BigDecimal.valueOf(student.getCompatibility()))
                ).rewardBigDecimal(HardSoftBigDecimalScore.ONE_SOFT, averageCompatibility -> averageCompatibility)
                .asConstraint("Average compatibility");
    }

}
