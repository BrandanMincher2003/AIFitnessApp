package com.example.finalyearproject.Notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TimelineDailyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        TimelineNotificationScheduler.scheduleNotificationForLatestTimeline(context);
    }
}
