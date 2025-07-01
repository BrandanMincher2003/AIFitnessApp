package com.example.finalyearproject.Workout;

import java.io.Serializable;

public class WorkoutExercise implements Serializable {
    private String name;
    private String targetMuscles;
    private String description;
    private String difficulty;
    private String equipment;
    private String type;
    private String sets;
    private String reps;
    private String documentId; // Firestore document ID

    // Required no-argument constructor for Firestore
    public WorkoutExercise() {}

    // Constructor without documentId
    public WorkoutExercise(String name, String targetMuscles, String description, String difficulty,
                           String equipment, String type, String sets, String reps) {
        this.name = name;
        this.targetMuscles = targetMuscles;
        this.description = description;
        this.difficulty = difficulty;
        this.equipment = equipment;
        this.type = type;
        this.sets = sets;
        this.reps = reps;
    }

    // Constructor with documentId
    public WorkoutExercise(String name, String targetMuscles, String description, String difficulty,
                           String equipment, String type, String sets, String reps, String documentId) {
        this(name, targetMuscles, description, difficulty, equipment, type, sets, reps);
        this.documentId = documentId;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getTargetMuscles() {
        return targetMuscles;
    }

    public String getDescription() {
        return description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getEquipment() {
        return equipment;
    }

    public String getType() {
        return type;
    }

    public String getSets() {
        return sets;
    }

    public String getReps() {
        return reps;
    }

    public String getDocumentId() {
        return documentId;
    }

    // Setters
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setSets(String sets) {
        this.sets = sets;
    }

    public void setReps(String reps) {
        this.reps = reps;
    }
}
