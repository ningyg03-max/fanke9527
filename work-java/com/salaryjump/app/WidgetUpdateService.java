package com.salaryjump.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;

/* loaded from: classes.dex */
public class WidgetUpdateService extends Service {
    private static final String CHANNEL_ID = "salary_jump_widget";
    private static final int NOTIFICATION_ID = 4102;
    private Handler handler;
    private final Runnable ticker = new Runnable() { // from class: com.salaryjump.app.WidgetUpdateService.1
        @Override // java.lang.Runnable
        public void run() {
            boolean hasWidgets = SalaryWidgetProvider.hasWidgets(WidgetUpdateService.this);
            WidgetUpdateService widgetUpdateService = WidgetUpdateService.this;
            if (!hasWidgets) {
                widgetUpdateService.stopSelf();
                return;
            }
            PowerManager powerManager = (PowerManager) widgetUpdateService.getSystemService("power");
            if (powerManager == null || powerManager.isInteractive()) {
                try {
                    SalaryWidgetProvider.updateAll(WidgetUpdateService.this, false);
                } finally {
                    if (WidgetUpdateService.this.handler != null) {
                        WidgetUpdateService.this.handler.postDelayed(this, SalaryWidgetProvider.recommendedInterval(WidgetUpdateService.this));
                    }
                }
            }
        }
    };

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void start(Context context) {
        try {
            context.startForegroundService(new Intent(context, (Class<?>) WidgetUpdateService.class));
        } catch (Throwable th) {
        }
    }

    public static void stop(Context context) {
        try {
            context.stopService(new Intent(context, (Class<?>) WidgetUpdateService.class));
        } catch (Throwable th) {
        }
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        this.handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        try {
            startInForeground();
            this.handler.removeCallbacks(this.ticker);
            this.handler.post(this.ticker);
            return 1;
        } catch (Throwable th) {
            stopSelf();
            return 2;
        }
    }

    @Override // android.app.Service
    public void onDestroy() {
        Handler handler = this.handler;
        if (handler != null) {
            handler.removeCallbacks(this.ticker);
        }
        super.onDestroy();
    }

    private void startInForeground() {
        Notification buildNotification = buildNotification();
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, buildNotification, 1073741824);
        } else {
            startForeground(NOTIFICATION_ID, buildNotification);
        }
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, (Class<?>) MainActivity.class);
        intent.setFlags(335544320);
        PendingIntent activity = PendingIntent.getActivity(this, 4103, intent, 201326592);
        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("SalaryJump").setContentText("桌面金额实时更新中").setContentIntent(activity).setOngoing(true).setOnlyAlertOnce(true).setShowWhen(false).setPriority(-2);
        if (Build.VERSION.SDK_INT >= 31) {
            builder.setForegroundServiceBehavior(1);
        }
        return builder.build();
    }

    private void createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
        if (notificationManager == null) {
            return;
        }
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "SalaryJump 桌面更新", 1);
        notificationChannel.setDescription("保持 SalaryJump 桌面金额实时更新");
        notificationChannel.setShowBadge(false);
        notificationChannel.enableVibration(false);
        notificationChannel.setSound(null, null);
        notificationManager.createNotificationChannel(notificationChannel);
    }
}
