package com.salaryjump.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.provider.Settings;

/* loaded from: classes.dex */
public class WaterReminderReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 4201;
    private static final int REQUEST_CONTENT = 4210;
    private static final int REQUEST_DRINK = 4211;
    private static final int REQUEST_SNOOZE = 4212;

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        if ("com.salaryjump.app.action.WATER_DRINK".equals(action)) {
            drinkAndReschedule(context);
            return;
        }
        if ("com.salaryjump.app.action.WATER_SNOOZE".equals(action)) {
            cancelNotification(context);
            WaterReminderScheduler.scheduleSnooze(context);
        } else if ("com.salaryjump.app.action.WATER_REMIND".equals(action)) {
            if (WaterReminderScheduler.isAllowedNow(context)) {
                showNotification(context);
            }
            WaterReminderScheduler.scheduleNext(context);
        } else if ("android.intent.action.BOOT_COMPLETED".equals(action) || "android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action)) {
            WaterReminderScheduler.scheduleNext(context);
        }
    }

    public static void drinkAndReschedule(Context context) {
        drinkAndReschedule(context, 0);
    }

    public static void drinkAndReschedule(Context context, int i) {
        WaterReminderScheduler.addDrink(context, i);
        cancelNotification(context);
        SalaryWidgetProvider.refreshAll(context);
        WaterReminderScheduler.scheduleNext(context);
    }

    public static void cancelNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    private void showNotification(Context context) {
        WaterReminderSettings load = WaterReminderSettings.load(context);
        int consumed = WaterReminderScheduler.getConsumed(context);
        createChannel(context, load.reminderMode);
        Notification.Builder priority = new Notification.Builder(context, channelId(load.reminderMode)).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("喝水时间到了").setContentText("今日已喝 " + consumed + " / " + load.targetMl + " ml，补充 " + load.cupMl + " ml" + MainActivity.earnSuffix(context)).setContentIntent(contentIntent(context)).setAutoCancel(true).setOnlyAlertOnce(false).setShowWhen(true).setCategory("reminder").setPriority(0);
        priority.addAction(0, "已喝 " + load.cupMl + " ml", actionIntent(context, "com.salaryjump.app.action.WATER_DRINK", REQUEST_DRINK));
        priority.addAction(0, "10 分钟后", actionIntent(context, "com.salaryjump.app.action.WATER_SNOOZE", REQUEST_SNOOZE));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, priority.build());
        }
    }

    private void createChannel(Context context, int i) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        if (notificationManager == null) {
            return;
        }
        AudioAttributes build = new AudioAttributes.Builder().setUsage(5).build();
        NotificationChannel notificationChannel = new NotificationChannel(channelId(i), "喝水提醒", 3);
        notificationChannel.setDescription("工作时间按设定间隔提醒喝水");
        notificationChannel.setShowBadge(false);
        boolean z = i == 0 || i == 2;
        boolean z2 = i == 1 || i == 2;
        if (z) {
            notificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, build);
        } else {
            notificationChannel.setSound(null, null);
        }
        notificationChannel.enableVibration(z2);
        if (z2) {
            notificationChannel.setVibrationPattern(new long[]{0, 420, 220, 420});
        }
        notificationManager.createNotificationChannel(notificationChannel);
    }

    private static String channelId(int i) {
        if (i == 0) {
            return "water_reminder_sound";
        }
        if (i == 1) {
            return "water_reminder_vibrate";
        }
        return "water_reminder_both";
    }

    private static PendingIntent contentIntent(Context context) {
        Intent intent = new Intent(context, (Class<?>) MainActivity.class);
        intent.setFlags(335544320);
        return PendingIntent.getActivity(context, REQUEST_CONTENT, intent, 201326592);
    }

    private static PendingIntent actionIntent(Context context, String str, int i) {
        Intent intent = new Intent(context, (Class<?>) WaterReminderReceiver.class);
        intent.setAction(str);
        return PendingIntent.getBroadcast(context, i, intent, 201326592);
    }
}
