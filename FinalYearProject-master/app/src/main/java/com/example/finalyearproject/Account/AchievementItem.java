package com.example.finalyearproject.Account;

public class AchievementItem {
    private String Name;
    private String Description;
    private String Image;
    private boolean hasAchieved; // ✅ Add this field

    public AchievementItem() {
        // Needed for Firestore deserialization
    }

    public AchievementItem(String name, String description, String image) {
        this.Name = name;
        this.Description = description;
        this.Image = image;
    }

    public String getName() {
        return Name;
    }

    public String getDescription() {
        return Description;
    }

    public String getImage() {
        return Image;
    }

    // ✅ Getter and Setter for hasAchieved
    public boolean isHasAchieved() {
        return hasAchieved;
    }

    public void setAchieved(boolean hasAchieved) {
        this.hasAchieved = hasAchieved;
    }
}
