package com.example.finalyearproject.Account;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.finalyearproject.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.*;

import java.util.*;

public class AchievementTracker {

    private final FirebaseFirestore db;
    private final String uid;
    private final View rootView;

    public AchievementTracker(FirebaseFirestore db, String uid, View rootView) {
        this.db = db;
        this.uid = uid;
        this.rootView = rootView;
    }

    public void checkOnTrackAchievement() {
        CollectionReference achievementsRef = db.collection("users")
                .document(uid).collection("achievements");

        achievementsRef.whereEqualTo("Name", "On Track")
                .get().addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        Boolean achieved = doc.getBoolean("hasAchieved");
                        if (achieved != null && !achieved) {
                            checkTrackingProgressAndUnlock(doc.getReference(), "On Track", "You've added your first progress goal.");
                        }
                    }
                });
    }

    public void checkSquadSyncAchievement() {
        CollectionReference achievementsRef = db.collection("users")
                .document(uid).collection("achievements");

        achievementsRef.whereEqualTo("Name", "Squad Sync")
                .get().addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        Boolean achieved = doc.getBoolean("hasAchieved");
                        if (achieved != null && !achieved) {
                            Map<String, Object> update = new HashMap<>();
                            update.put("hasAchieved", true);
                            update.put("achievedAt", FieldValue.serverTimestamp());

                            doc.getReference().update(update)
                                    .addOnSuccessListener(aVoid ->
                                            showAchievementPopup("Squad Sync", "Save a community workout made by another user"));
                        }
                    }
                });
    }

    public void checkGoalGetterAchievement() {
        CollectionReference achievementsRef = db.collection("users")
                .document(uid).collection("achievements");

        achievementsRef.whereEqualTo("Name", "Goal Getter")
                .get().addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        Boolean achieved = doc.getBoolean("hasAchieved");
                        if (achieved != null && !achieved) {
                            evaluateGoalGetter(doc.getReference(), "Goal Getter", "Achieve your progress goal");
                        }
                    }
                });
    }

    private void evaluateGoalGetter(DocumentReference achievementRef, String title, String description) {
        CollectionReference progressRef = db.collection("users").document(uid).collection("progress");

        progressRef.get().addOnSuccessListener(progressDocs -> {
            for (DocumentSnapshot progressDoc : progressDocs.getDocuments()) {
                Double startingValue = progressDoc.getDouble("startingValue");
                Double targetValue = progressDoc.getDouble("targetValue");

                if (startingValue == null || targetValue == null) continue;

                CollectionReference trackingRef = progressDoc.getReference().collection("tracking");
                trackingRef.get().addOnSuccessListener(trackingDocs -> {
                    for (DocumentSnapshot trackingDoc : trackingDocs.getDocuments()) {
                        Double currentValue = trackingDoc.getDouble("value");
                        if (currentValue == null) continue;

                        boolean goalMet = (startingValue > targetValue && currentValue <= targetValue)
                                || (startingValue < targetValue && currentValue >= targetValue);

                        if (goalMet) {
                            Map<String, Object> update = new HashMap<>();
                            update.put("hasAchieved", true);
                            update.put("achievedAt", FieldValue.serverTimestamp());

                            achievementRef.update(update)
                                    .addOnSuccessListener(aVoid -> showAchievementPopup(title, description));
                            return;
                        }
                    }
                });
            }
        });
    }


    public void checkBlueprintMasterAchievement() {
        CollectionReference achievementsRef = db.collection("users")
                .document(uid).collection("achievements");

        achievementsRef.whereEqualTo("Name", "Blueprint Master")
                .get().addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        Boolean achieved = doc.getBoolean("hasAchieved");
                        if (achieved != null && !achieved) {
                            Map<String, Object> update = new HashMap<>();
                            update.put("hasAchieved", true);
                            update.put("achievedAt", FieldValue.serverTimestamp());

                            doc.getReference().update(update)
                                    .addOnSuccessListener(aVoid ->
                                            showAchievementPopup("Blueprint Master", "Create your first custom workout plan"));
                        }
                    }
                });
    }


    public void checkHabitHackerAchievement() {
        CollectionReference achievementsRef = db.collection("users")
                .document(uid).collection("achievements");

        achievementsRef.whereEqualTo("Name", "Habit Hacker")
                .get().addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        Boolean achieved = doc.getBoolean("hasAchieved");
                        if (achieved != null && !achieved) {
                            checkTimelineFor3Workouts(doc.getReference(), "Habit Hacker", "Complete 3 workouts within a single week");
                        }
                    }
                });
    }

    public void checkFirstRepAchievement() {
        CollectionReference achievementsRef = db.collection("users")
                .document(uid).collection("achievements");

        achievementsRef.whereEqualTo("Name", "First Rep")
                .get().addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        Boolean achieved = doc.getBoolean("hasAchieved");
                        if (achieved != null && !achieved) {
                            checkTimelineForFirstWorkout(doc.getReference(), "First Rep", "Complete your first workout");
                        }
                    }
                });
    }

    public void checkWeekendWarriorAchievement() {
        CollectionReference achievementsRef = db.collection("users")
                .document(uid).collection("achievements");

        achievementsRef.whereEqualTo("Name", "Weekend Warrior")
                .get().addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        Boolean achieved = doc.getBoolean("hasAchieved");
                        if (achieved != null && !achieved) {
                            checkTimelineForWeekendWorkout(doc.getReference(), "Weekend Warrior", "Complete workouts on Saturday or Sunday");
                        }
                    }
                });
    }

    private void checkTimelineForWeekendWorkout(DocumentReference achievementRef, String title, String description) {
        CollectionReference timelineRef = db.collection("users").document(uid).collection("timeline");

        timelineRef.get().addOnSuccessListener(querySnapshots -> {
            for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                Timestamp ts = doc.getTimestamp("timestamp");
                if (ts != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(ts.toDate());
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                    // Check if it's Saturday (7) or Sunday (1)
                    if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                        Map<String, Object> update = new HashMap<>();
                        update.put("hasAchieved", true);
                        update.put("achievedAt", FieldValue.serverTimestamp());

                        achievementRef.update(update)
                                .addOnSuccessListener(aVoid -> showAchievementPopup(title, description));
                        return;
                    }
                }
            }
        });
    }

    private void checkTimelineForFirstWorkout(DocumentReference achievementRef, String title, String description) {
        CollectionReference timelineRef = db.collection("users").document(uid).collection("timeline");

        timelineRef.get().addOnSuccessListener(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                Map<String, Object> update = new HashMap<>();
                update.put("hasAchieved", true);
                update.put("achievedAt", FieldValue.serverTimestamp());

                achievementRef.update(update)
                        .addOnSuccessListener(aVoid -> showAchievementPopup(title, description));
            }
        });
    }

    private void checkTimelineFor3Workouts(DocumentReference achievementRef, String title, String description) {
        CollectionReference timelineRef = db.collection("users").document(uid).collection("timeline");

        timelineRef.get().addOnSuccessListener(querySnapshots -> {
            List<Date> dates = new ArrayList<>();
            for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                Timestamp ts = doc.getTimestamp("timestamp");
                if (ts != null) dates.add(ts.toDate());
            }

            dates.sort(Date::compareTo);

            for (int i = 0; i < dates.size(); i++) {
                int count = 1;
                for (int j = i + 1; j < dates.size(); j++) {
                    long diff = dates.get(j).getTime() - dates.get(i).getTime();
                    if (diff <= 6 * 24 * 60 * 60 * 1000L) {
                        count++;
                    } else break;
                }
                if (count >= 3) {
                    Map<String, Object> update = new HashMap<>();
                    update.put("hasAchieved", true);
                    update.put("achievedAt", FieldValue.serverTimestamp());

                    achievementRef.update(update)
                            .addOnSuccessListener(aVoid -> showAchievementPopup(title, description));
                    return;
                }
            }
        });
    }

    private void checkTrackingProgressAndUnlock(DocumentReference achievementRef, String title, String description) {
        CollectionReference progressRef = db.collection("users").document(uid).collection("progress");

        progressRef.get().addOnSuccessListener(progressSnapshots -> {
            for (DocumentSnapshot progressDoc : progressSnapshots.getDocuments()) {
                CollectionReference trackingRef = progressDoc.getReference().collection("tracking");

                trackingRef.get().addOnSuccessListener(trackingSnapshots -> {
                    if (trackingSnapshots.size() > 1) {
                        Map<String, Object> update = new HashMap<>();
                        update.put("hasAchieved", true);
                        update.put("achievedAt", FieldValue.serverTimestamp());

                        achievementRef.update(update)
                                .addOnSuccessListener(aVoid -> showAchievementPopup(title, description));
                    }
                });
            }
        });
    }

    private void showAchievementPopup(String title, String description) {
        View popupView = View.inflate(rootView.getContext(), R.layout.popup_achievement_unlocked, null);

        TextView unlockedText = popupView.findViewById(R.id.achievementUnlockedTitle);
        TextView titleText = popupView.findViewById(R.id.achievementTitle);
        TextView descText = popupView.findViewById(R.id.achievementDesc);
        ImageView icon = popupView.findViewById(R.id.achievementIcon);
        ImageView closeBtn = popupView.findViewById(R.id.closeButton);

        unlockedText.setText("Achievement Unlocked!");
        titleText.setText(title);
        descText.setText(description);
        icon.setImageResource(R.drawable.ic_achievement);

        View container = popupView.findViewById(R.id.achievementPopupContainer);
        container.startAnimation(android.view.animation.AnimationUtils.loadAnimation(rootView.getContext(), R.anim.slide_up));

        AlertDialog dialog = new AlertDialog.Builder(rootView.getContext())
                .setView(popupView)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        closeBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
