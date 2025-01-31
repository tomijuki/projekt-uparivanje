package org.example.Iteracija_1;
import org.example.Main;
import org.example.Mentor;
import org.example.Student;
import org.optaplanner.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import org.optaplanner.core.api.score.stream.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.stream.Collectors;

public class PairingConstraintProvider1 implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                unrankedConstraint(constraintFactory),
                capacityConstraint(constraintFactory),
                stability1(constraintFactory),
                stability2(constraintFactory),
                objectiveFunction(constraintFactory)
        };
    }

    private Constraint unrankedConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Student.class)
                .filter(student -> student.getMentorRank() == 0 && !student.getMentor().getId().equals("NULL"))
                .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_HARD, student -> BigDecimal.valueOf(2 * Main.numOfMentors))
                .asConstraint("Paired with unranked mentor");
    }

    private Constraint capacityConstraint(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Student.class)
                .filter(student -> !student.getMentor().getId().equals("NULL"))
                .groupBy(
                        Student::getMentor,
                        ConstraintCollectors.toList(Student::getStudentRank)
                )
                .filter((mentor, ranks) -> ranks.size() >= mentor.getCapacity())
                .groupBy(
                        (mentor, ranks) -> mentor,
                        (mentor, ranks) -> ranks.size() - mentor.getCapacity(),
                        (mentor, ranks) ->
                                ranks.stream()
                                        .sorted(Comparator.reverseOrder())
                                        .collect(Collectors.toList())
                                        .subList(0, mentor.getCapacity())
                                        .stream().mapToDouble(n -> n).average().getAsDouble()
                )
                .groupBy(
                        ConstraintCollectors.sum((mentor, count, avgRank) -> count),
                        ConstraintCollectors.sumBigDecimal((mentor, count, avgRank) -> BigDecimal.valueOf(avgRank))
                )
                .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_HARD, (count, avgAvgRank) ->
                        count == 0
                                ? BigDecimal.ZERO
                                : BigDecimal.valueOf(
                                        (1 / (Main.numOfMentors + 1)) *
                                        (count + (1 - avgAvgRank.doubleValue() / Main.numOfMentors))
                                )
                )
                .asConstraint("Capacity");
    }

    private Constraint stability1(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Student.class)
                .filter(student -> !student.getMentor().getId().equals("NULL"))
                .groupBy(
                        Student::getMentor,
                        ConstraintCollectors.toList(Student::getStudentRank)
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
                .filter((mentor, worstRank) -> worstRank < 1.0)
                .join(constraintFactory.forEach(Student.class).filter(student -> student.getMentorRank() < 1.0),
                        Joiners.filtering((mentor, worstRank, student) ->
                                student.preffersMore(mentor) && mentor.getStudentRank(student) > worstRank
                        )
                )
                .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_HARD, (mentor, worstRank, student) ->
                        BigDecimal.valueOf(student.getMentorRank(mentor))
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
                .join(constraintFactory.forEach(Student.class).filter(student -> student.getMentorRank() < 1.0),
                        Joiners.filtering((mentor, student) ->
                                student.preffersMore(mentor)
                        )
                )
                .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_HARD, (mentor, student) ->
                        BigDecimal.valueOf(student.getMentorRank(mentor))
                )
                .asConstraint("Stability2");
    }

    private Constraint objectiveFunction(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Student.class)
                .groupBy(
                        ConstraintCollectors.count(),
                        ConstraintCollectors.averageBigDecimal(s -> BigDecimal.valueOf(s.getMentorRank())),
                        ConstraintCollectors.averageBigDecimal(s -> BigDecimal.valueOf(s.getStudentRank()))
                ).rewardBigDecimal(HardSoftBigDecimalScore.ONE_SOFT, (count, avgStudentSatisfaction, avgMentorSatisfaction) -> {
                    if (count == 0)
                        return BigDecimal.ZERO;
                    return BigDecimal.valueOf(Math.sqrt(
                            (
                            Math.pow(count / Main.numOfStudents, 2) +
                            Math.pow(avgStudentSatisfaction.doubleValue(), 2) +
                            Math.pow(1 - Math.abs(avgMentorSatisfaction.doubleValue() - avgStudentSatisfaction.doubleValue()), 2)
                            ) / 3
                    ) * 100);
                }).asConstraint("Objective function");
    }
}
