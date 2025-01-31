package org.example.Loaders;

import org.example.Mentor;

import java.io.*;
import java.util.*;

public class MentorLoader {
    public static List<Mentor> loadMentors(String folderPath) {
        List<Mentor> mentors = new ArrayList<>();
        File folder = new File(folderPath);

        try {
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                BufferedReader reader = new BufferedReader(new FileReader(file));

                String id = reader.readLine().split(": ")[1];
                int capacity = Integer.parseInt(reader.readLine().split(": ")[1]);

                String line;
                Map<String, Double> preferredStudents = new HashMap<>();
                while (!(line = reader.readLine()).isBlank()) {
                    String[] student_rang = line.split(":");
                    preferredStudents.put(student_rang[0], Double.parseDouble(student_rang[1]));
                }

                String[] popis = reader.readLine().split(", ");
                Set<String> predmeti = new HashSet<>(Arrays.asList(popis));

                mentors.add(new Mentor(id, capacity, preferredStudents, predmeti));
                reader.close();
            }
        } catch (IOException e) {
            System.err.println("Greška pri čitanju mentora: " + e.getMessage());
        }

        return mentors;
    }
}
