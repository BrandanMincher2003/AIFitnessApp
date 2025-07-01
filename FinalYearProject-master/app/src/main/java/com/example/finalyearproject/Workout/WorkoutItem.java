package com.example.finalyearproject.Workout;

public class WorkoutItem {
    private String id;
    private String title;
    private String description;
    private String imageUrl; // Updated from imageResId to imageUrl

    public WorkoutItem() {}

    public WorkoutItem(String id, String title, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public String getImageUrl() { return imageUrl; }

    public void setDescription(String description) {
        this.description = description;
    }
}
