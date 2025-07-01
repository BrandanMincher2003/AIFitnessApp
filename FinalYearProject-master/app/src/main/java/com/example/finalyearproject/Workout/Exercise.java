package com.example.finalyearproject.Workout;

import java.io.Serializable;

public class Exercise implements Serializable {
    private String name;
    private String targetMuscles;
    private String description;
    private String difficulty;
    private String equipment;
    private String type;

    // Required no-argument constructor for Firestore
    public Exercise() {}

    // Full constructor
    public Exercise(String name, String targetMuscles, String description, String difficulty, String equipment, String type) {
        this.name = name;
        this.targetMuscles = targetMuscles;
        this.description = description;
        this.difficulty = difficulty;
        this.equipment = equipment;
        this.type = type;
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
}
