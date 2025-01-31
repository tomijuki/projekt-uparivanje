package org.example;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import org.example.Iteracija_2.PairingSolution2;
import org.example.Loaders.MentorLoader;
import org.example.Loaders.StudentLoader;
import org.example.Iteracija_1.PairingSolution1;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

public class Main {
    public static double numOfStudents;
    public static double numOfMentors;

    public static void main(String[] args) {
        String studentFolder = "src/main/resources/students";
        String mentorFolder = "src/main/resources/mentors";
        Set<Student> finalSolution = new TreeSet<>(
                Comparator.comparingInt((Student s) -> s.getId().length())
                        .thenComparing(Student::getId)
        );
        //--------------------------------------------------------------------------------------------------
        //1. Iteracija
        List<Student> students1 = StudentLoader.loadStudents(studentFolder);
        List<Mentor> mentors1 = MentorLoader.loadMentors(mentorFolder).stream().filter(m -> m.getCapacity() > 0).collect(Collectors.toList());
        numOfStudents = students1.size();
        numOfMentors = mentors1.size();

        mentors1.add(new Mentor("NULL", Integer.MAX_VALUE, new HashMap<>(), new HashSet<>()));

        PairingSolution1 solution1 = new PairingSolution1();
        solution1.setStudents(students1);
        solution1.setMentors(mentors1);

        SolverFactory<PairingSolution1> solverFactory1 = SolverFactory.createFromXmlResource("org/example/solverConfig1.xml");
        Solver<PairingSolution1> solver1 = solverFactory1.buildSolver();
        long startTime1 = System.currentTimeMillis();
        solver1.addEventListener(event -> {
            PairingSolution1 currentSolution = event.getNewBestSolution();
            System.out.printf("%7.3f: %s\n" , (System.currentTimeMillis() - startTime1) / 1000.0, currentSolution.getScore());
        });
        System.out.println("1. Iteracija");
        PairingSolution1 solvedSolution1 = solver1.solve(solution1);
        //--------------------------------------------------------------------------------------------------
        //2. Iteracija
        List<Student> students2 = solvedSolution1.getStudents().stream()
                .filter(student -> {
                    if (student.getMentor() == null || student.getMentor().getId().equals("NULL")) {
                        return true;
                    } else {
                        finalSolution.add(student);
                        student.getMentor().setCapacity(student.getMentor().getCapacity() - 1);
                        return false;
                    }
                })
                .collect(Collectors.toList());
        List<Mentor> mentors2 = solvedSolution1.getMentors().stream()
                .filter(mentor -> mentor.getCapacity() != 0)
                .collect(Collectors.toList());
        numOfStudents = students2.size();
        numOfMentors = mentors2.size();

        PairingSolution2 solution2 = new PairingSolution2();
        solution2.setStudents(students2);
        solution2.setMentors(mentors2);

        SolverFactory<PairingSolution2> solverFactory2 = SolverFactory.createFromXmlResource("org/example/solverConfig2.xml");
        Solver<PairingSolution2> solver2 = solverFactory2.buildSolver();
        long startTime2 = System.currentTimeMillis();
        solver2.addEventListener(event -> {
            PairingSolution2 currentSolution = event.getNewBestSolution();
            System.out.printf("%7.3f: %s\n" , (System.currentTimeMillis() - startTime2) / 1000.0, currentSolution.getScore());
        });
        System.out.println("2. Iteracija:");
        PairingSolution2 solvedSolution2 = solver2.solve(solution2);
        //--------------------------------------------------------------------------------------------------
        //Ispis rjesenja
        finalSolution.addAll(solvedSolution2.getStudents());

        try (BufferedWriter writer = Files.newBufferedWriter(Path.of("./result.txt"))) {
            for (Student s : finalSolution) {
                writer.write(String.format("%4s -> %4s\n", s.getId(), s.getMentor().getId()));
            }
        } catch (IOException ignored) {}
    }

}
