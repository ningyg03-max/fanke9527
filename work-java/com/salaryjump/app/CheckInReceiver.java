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
import java.util.Calendar;

/* loaded from: classes.dex */
public class CheckInReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "checkin_reminder";
    private static final int NOTIFICATION_ID = 54001;

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            return;
        }
        String action = intent == null ? null : intent.getAction();
        if ("android.intent.action.BOOT_COMPLETED".equals(action) || "android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action)) {
            CheckInReminder.schedule(context);
        } else {
            if (!"com.salaryjump.app.action.CHECKIN_REMIND".equals(action)) {
                return;
            }
            int currentKind = CheckInReminder.currentKind(context);
            if (currentKind != 0) {
                notify(context, currentKind);
            }
            CheckInReminder.schedule(context);
        }
    }

    private void notify(Context context, int i) {
        String str;
        String str2;
        createChannel(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        if (notificationManager == null) {
            return;
        }
        if (i == 1) {
            str2 = "今天加班了吗？";
            str = "有加班或请假的话，记得在 App 里记一下";
        } else if (StatutoryHolidays.isStatutory(Calendar.getInstance())) {
            str = "法定节假日 · 出勤记得标记，按 " + HolidayWork.rateText(WorkRules.statutoryRateX10(context)) + "计薪";
            str2 = "今天出勤了吗？";
        } else if (!WorkRules.useCompFor(context, false)) {
            str = "如果出勤了，记得标记「出勤」，按 " + HolidayWork.rateText(WorkRules.weekendRateX10(context)) + "计薪";
            str2 = "今天出勤了吗？";
        } else {
            str = "如果出勤了，记得标记「出勤」，记 1 天调休";
            str2 = "今天出勤了吗？";
        }
        Notification.Builder category = new Notification.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(str2).setContentText(str).setStyle(new Notification.BigTextStyle().bigText(str)).setContentIntent(contentIntent(context)).setAutoCancel(true).setShowWhen(true).setCategory("reminder");
        try {
            category.setColor(-15681151);
        } catch (Throwable th) {
        }
        notificationManager.notify(NOTIFICATION_ID, category.build());
    }

    static void clear(Context context) {
        NotificationManager notificationManager;
        if (context != null && (notificationManager = (NotificationManager) context.getSystemService("notification")) != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    private static PendingIntent contentIntent(Context context) {
        Intent intent = new Intent(context, (Class<?>) MainActivity.class);
        intent.putExtra("from_checkin", true);
        return PendingIntent.getActivity(context, NOTIFICATION_ID, intent, pendingFlag());
    }

    private static int pendingFlag() {
        return 201326592;
    }

    private void createChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        if (notificationManager == null) {
            return;
        }
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "打卡提醒", 3);
        notificationChannel.setDescription("该记的还没记时提醒（工作日加班 / 休息日出勤）");
        notificationChannel.setShowBadge(true);
        try {
            notificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, new AudioAttributes.Builder().setUsage(5).build());
        } catch (Throwable th) {
        }
        notificationManager.createNotificationChannel(notificationChannel);
    }
}
