package org.example;

import java.util.Map;
import java.util.Set;

public class Mentor {
    private String id;
    private int capacity;
    private Map<String, Double> preferredStudents;
    private Set<String> predmeti;

    // Konstruktor koji prima tri argumenta
    public Mentor(String id, int capacity, Map<String, Double> preferredStudents, Set<String> predmeti) {
        this.id = id;
        this.capacity = capacity;
        this.preferredStudents = preferredStudents;
        this.predmeti = predmeti;
        convertRanks();
    }

    private void convertRanks() {
        int n = preferredStudents.size();
        preferredStudents.forEach((k,v) -> preferredStudents.put(k, (n + 1 - preferredStudents.get(k)) / n));
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Map<String, Double> getPreferredStudents() {
        return preferredStudents;
    }

    public void setPreferredStudents(Map<String, Double> preferredStudents) {
        this.preferredStudents = preferredStudents;
    }

    public Set<String> getPredmeti() {
        return predmeti;
    }

    public void setPredmeti(Set<String> predmeti) {
        this.predmeti = predmeti;
    }



    public double getStudentRank(Student student) {
        if (student == null)
            return 0;
        return this.getPreferredStudents().getOrDefault(student.getId(), 0.0);
    }

    public double calculateCompatibility(Student s) {
        if (this.getId().equals("NULL"))
            return 0;

        double score = 0;
        for (String p : this.predmeti) {
            score += Math.pow(s.getGrades().getOrDefault(p, 0), 2);
        }

        return Math.sqrt(score * this.predmeti.size()) + 0.1;
    }

    @Override
    public String toString() {
        return "Mentor{" +
                "id='" + id + '\'' +
                ", capacity=" + capacity +
                ", preferredStudents=" + preferredStudents +
                ", predmeti=" + predmeti +
                '}';
    }
}
