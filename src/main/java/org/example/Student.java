package org.example;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.util.Map;

@PlanningEntity
public class Student {
    private String id;
    private Map<String, Double> preferredMentors;
    private Map<String, Integer> grades;

    @PlanningVariable(valueRangeProviderRefs = "mentorRange")
    private Mentor mentor;

    public Student() {
    }

    public Student(String id, Map<String, Double> preferredMentors, Map<String, Integer> grades) {
        this.id = id;
        this.preferredMentors = preferredMentors;
        this.grades = grades;
        convertRanks();
    }

    private void convertRanks() {
        int n = preferredMentors.size();
        preferredMentors.forEach((k,v) -> preferredMentors.put(k, (n + 1 - preferredMentors.get(k)) / n));
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Double> getPreferredMentors() {
        return preferredMentors;
    }

    public void setPreferredMentors(Map<String, Double> preferredMentors) {
        this.preferredMentors = preferredMentors;
    }

    public Map<String, Integer> getGrades() {
        return grades;
    }

    public void setGrades(Map<String, Integer> grades) {
        this.grades = grades;
    }

    public Mentor getMentor() {
        return mentor;
    }

    public void setMentor(Mentor mentor) {
        this.mentor = mentor;
    }



    public double getMentorRank(Mentor mentor) {
        if (mentor == null)
            return 0;
        return this.getPreferredMentors().getOrDefault(mentor.getId(), 0.0);
    }

    public double getStudentRank() {
        if (this.mentor == null) return 0;
        return this.mentor.getStudentRank(this);
    }

    public double getMentorRank() {
        return this.getMentorRank(this.mentor);
    }

    public double getCompatibility() {
        if (this.mentor == null) {
            return 0;
        }
        return this.mentor.calculateCompatibility(this);
    }

    public boolean preffersMore(Mentor m) {
        return this.getMentorRank(m) > this.getMentorRank();
    }

    public boolean isMoreCompatible(Mentor m) {
        return m.calculateCompatibility(this) > this.getCompatibility();
    }

    @Override
    public String toString() {
        return "Student{" +
                "id='" + id + '\'' +
                ", preferredMentors=" + preferredMentors +
                ", grades=" + grades +
                ", mentor=" + mentor +
                '}';
    }
}
