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
import java.text.DecimalFormat;
import java.util.Calendar;

/* loaded from: classes.dex */
public class PaydayReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "payday_reminder";
    private static final int NOTIFICATION_ID = 53501;
    private static final int NOTIFICATION_ID_EVE = 53502;

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            return;
        }
        String action = intent == null ? null : intent.getAction();
        if ("android.intent.action.BOOT_COMPLETED".equals(action) || "android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action)) {
            PaydayReminder.schedule(context);
            return;
        }
        boolean equals = "com.salaryjump.app.action.PAYDAY_EVE".equals(action);
        if (equals || "com.salaryjump.app.action.PAYDAY".equals(action)) {
            notify(context, equals);
            PaydayReminder.schedule(context);
        }
    }

    private void notify(Context context, boolean z) {
        String str;
        int day = PaydayReminder.day(context);
        createChannel(context, z);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        if (notificationManager == null) {
            return;
        }
        if (z) {
            str = "明天是发薪日（" + day + " 号）";
        } else {
            str = "今天是发薪日 💰";
        }
        String buildSummary = z ? "提前看看上个月的战报吧" : buildSummary(context);
        Notification.Builder category = new Notification.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(str).setContentText(buildSummary).setStyle(new Notification.BigTextStyle().bigText(buildSummary)).setContentIntent(contentIntent(context)).setAutoCancel(true).setShowWhen(true).setCategory("reminder");
        if (!z) {
            category.addAction(0, "查看战报", reportIntent(context));
        }
        try {
            category.setColor(-15681151);
        } catch (Throwable th) {
        }
        notificationManager.notify(z ? NOTIFICATION_ID_EVE : NOTIFICATION_ID, category.build());
    }

    private static String buildSummary(Context context) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(2, -1);
            int i = calendar.get(1);
            int i2 = calendar.get(2) + 1;
            long monthTotalCents = SalaryHistory.monthTotalCents(context, i, i2);
            if (monthTotalCents <= 0) {
                return "上月还没有收入记录";
            }
            return i2 + " 月累计已赚 ¥" + new DecimalFormat("#,##0.00").format(monthTotalCents / 100.0d);
        } catch (Throwable th) {
            return "点击查看月度战报";
        }
    }

    private static PendingIntent contentIntent(Context context) {
        Intent intent = new Intent(context, (Class<?>) MonthlyReportActivity.class);
        intent.putExtra("from_payday", true);
        return PendingIntent.getActivity(context, NOTIFICATION_ID, intent, pendingFlag());
    }

    private static PendingIntent reportIntent(Context context) {
        return PendingIntent.getActivity(context, NOTIFICATION_ID_EVE, new Intent(context, (Class<?>) MonthlyReportActivity.class), pendingFlag());
    }

    private static int pendingFlag() {
        return 201326592;
    }

    private void createChannel(Context context, boolean z) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        if (notificationManager == null) {
            return;
        }
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "发薪日提醒", 3);
        notificationChannel.setDescription("发薪日当天与提前一天的提醒");
        notificationChannel.setShowBadge(true);
        try {
            notificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, new AudioAttributes.Builder().setUsage(5).build());
        } catch (Throwable th) {
        }
        notificationManager.createNotificationChannel(notificationChannel);
    }
}
