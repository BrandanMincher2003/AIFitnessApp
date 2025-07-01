package com.example.finalyearproject.Community;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommunityWorkoutItem {

    private String title;
    private List<String> targetMuscles;
    private String imageUrl;
    private String description;
    private List<String> equipment;
    private String type;
    private Timestamp timestamp;

    private String creatorName;
    private String creatorIconUrl;
    private String id;

    private List<Map<String, Object>> exercises;

    // Required no-args constructor for Firestore
    public CommunityWorkoutItem() {}

    // Full constructor (without exercises)
    public CommunityWorkoutItem(String title, List<String> targetMuscles, String imageUrl, String description,
                                List<String> equipment, String type, Timestamp timestamp,
                                String creatorName, String creatorIconUrl, String id) {
        this.title = title;
        this.targetMuscles = targetMuscles;
        this.imageUrl = imageUrl;
        this.description = description;
        this.equipment = equipment;
        this.type = type;
        this.timestamp = timestamp;
        this.creatorName = creatorName;
        this.creatorIconUrl = creatorIconUrl;
        this.id = id;
        this.exercises = new ArrayList<>();
    }

    // Extended constructor (with exercises)
    public CommunityWorkoutItem(String title, List<String> targetMuscles, String imageUrl, String description,
                                List<String> equipment, String type, Timestamp timestamp,
                                String creatorName, String creatorIconUrl, String id,
                                List<Map<String, Object>> exercises) {
        this.title = title;
        this.targetMuscles = targetMuscles;
        this.imageUrl = imageUrl;
        this.description = description;
        this.equipment = equipment;
        this.type = type;
        this.timestamp = timestamp;
        this.creatorName = creatorName;
        this.creatorIconUrl = creatorIconUrl;
        this.id = id;
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    // Getters
    public String getTitle() { return title; }

    public List<String> getTargetMuscles() { return targetMuscles; }

    public String getImageUrl() { return imageUrl; }

    public String getDescription() { return description; }

    public List<String> getEquipment() { return equipment; }

    public String getType() { return type; }

    public Timestamp getTimestamp() { return timestamp; }

    public String getCreatorName() { return creatorName; }

    public String getCreatorIconUrl() { return creatorIconUrl; }

    public String getId() { return id; }

    public List<Map<String, Object>> getExercises() { return exercises; }

    public void setExercises(List<Map<String, Object>> exercises) {
        this.exercises = exercises != null ? exercises : new ArrayList<>();
    }

    // Display helpers
    public String getTargetMusclesText() {
        return (targetMuscles != null && !targetMuscles.isEmpty())
                ? String.join(", ", targetMuscles)
                : "No muscles listed";
    }

    public String getEquipmentText() {
        return (equipment != null && !equipment.isEmpty())
                ? String.join(", ", equipment)
                : "No equipment listed";
    }
}
