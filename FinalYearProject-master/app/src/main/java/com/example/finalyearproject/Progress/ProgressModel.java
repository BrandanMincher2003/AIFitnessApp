package com.example.finalyearproject.Progress;

import java.util.List;

/**
 * Model class representing a single progress goal, such as a weight goal.
 */
public class ProgressModel {
    private String name;
    private double startingValue;
    private double targetValue;
    private String unit;
    private String docId;
    private String graphUrl; // ✅ URL of the uploaded progress chart image

    // Optional: store cached tracking data points
    private List<TrackingEntry> trackingEntries;

    // Required empty constructor for Firestore
    public ProgressModel() {}

    /**
     * Constructor for use in ProgressFragment and adapter.
     */
    public ProgressModel(String name, double startingValue, double targetValue, String unit, String docId, String graphUrl) {
        this.name = name;
        this.startingValue = startingValue;
        this.targetValue = targetValue;
        this.unit = unit;
        this.docId = docId;
        this.graphUrl = graphUrl;
    }

    // Getters
    public String getName() {
        return name;
    }

    public double getStartingValue() {
        return startingValue;
    }

    public double getTargetValue() {
        return targetValue;
    }

    public String getUnit() {
        return unit;
    }

    public String getDocId() {
        return docId;
    }

    public String getGraphUrl() {
        return graphUrl;
    }

    // Optional tracking data
    public List<TrackingEntry> getTrackingEntries() {
        return trackingEntries;
    }

    public void setTrackingEntries(List<TrackingEntry> trackingEntries) {
        this.trackingEntries = trackingEntries;
    }

    /**
     * Inner class to represent a single tracking data point.
     */
    public static class TrackingEntry {
        private long timestamp;
        private double value;

        public TrackingEntry() {}

        public TrackingEntry(long timestamp, double value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public double getValue() {
            return value;
        }
    }
}
