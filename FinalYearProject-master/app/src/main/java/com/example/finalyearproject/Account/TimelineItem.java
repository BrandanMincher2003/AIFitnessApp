package com.example.finalyearproject.Account;

public class TimelineItem {
    private String workoutName;
    private String formattedDate;

    public TimelineItem() {
        // Required empty constructor for Firestore (if needed)
    }

    public TimelineItem(String workoutName, String formattedDate) {
        this.workoutName = workoutName;
        this.formattedDate = formattedDate;
    }

    public String getWorkoutName() {
        return workoutName;
    }

    public void setWorkoutName(String workoutName) {
        this.workoutName = workoutName;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }
}
