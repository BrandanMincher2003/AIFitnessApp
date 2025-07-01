package com.example.finalyearproject.Notifications;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;

public class TimelineNotificationScheduler {

    public static void scheduleNotificationForLatestTimeline(Context context) {
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("timeline")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        if (timestamp != null) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(timestamp.toDate().getTime());
                            calendar.add(Calendar.HOUR_OF_DAY, -2); // 2 hours before

                            if (calendar.getTimeInMillis() > System.currentTimeMillis()) {
                                scheduleNotification(context, calendar);
                            }
                        }
                    }
                });
    }

    @SuppressLint("ScheduleExactAlarm")
    private static void scheduleNotification(Context context, Calendar calendar) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("title", "Don't forget to workout!");
        intent.putExtra("message", "make sure you stay on track to achieve your goals!");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 201, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Log.d("TimelineNotification", "Notification set for: " + calendar.getTime());
        }
    }
}
