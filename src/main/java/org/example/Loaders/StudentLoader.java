package org.example.Loaders;

import org.example.Student;

import java.io.*;
import java.util.*;

public class StudentLoader {
    public static List<Student> loadStudents(String folderPath) {
        List<Student> students = new ArrayList<>();
        File folder = new File(folderPath);

        try {
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                BufferedReader reader = new BufferedReader(new FileReader(file));

                String id = reader.readLine().split(": ")[1];

                String line; double rank = 0;
                Map<String, Double> preferredMentors = new HashMap<>();
                while (!(line = reader.readLine()).isBlank()) {
                    preferredMentors.put(line, ++rank);
                }

                Map<String, Integer> grades = new HashMap<>();
                while ((line = reader.readLine()) != null) {
                    String[] predmet_ocjena = line.split(":");
                    grades.put(predmet_ocjena[0], Integer.parseInt(predmet_ocjena[1]));
                }

                students.add(new Student(id, preferredMentors, grades));
                reader.close();
            }
        } catch (IOException e) {
            System.err.println("Greška pri čitanju studenata: " + e.getMessage());
        }

        return students;
    }
}
