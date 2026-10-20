package com.salaryjump.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;
import com.salaryjump.app.MainActivity;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;

/* loaded from: classes.dex */
public class SalaryWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_REFRESH = "com.salaryjump.app.action.REFRESH_WIDGET";
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.00");
    private static final long REFRESH_IDLE_MS = 60000;
    private static final long REFRESH_WORKING_MS = 15000;
    private static final int REQUEST_REFRESH = 4101;

    static String cumulativeText(Context context) {
        // 与主卡一致：本月累计（本月已归档 + 今天未归档时的实时已赚）
        return "本月 ¥ " + MONEY.format(MainActivity.monthlyTotal(context))
                + " · 已结算 " + MainActivity.monthlySettledDays(context) + " 天";
    }

    private static boolean isWorking(int i) {
        return i == 2 || i == 3 || i == 4 || i == 6;
    }

    @Override // android.appwidget.AppWidgetProvider
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] iArr) {
        refreshAll(context);
        startLiveUpdates(context);
    }

    @Override // android.appwidget.AppWidgetProvider, android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent == null || intent.getAction() == null) {
            return;
        }
        String action = intent.getAction();
        if (ACTION_REFRESH.equals(action) || "android.intent.action.BOOT_COMPLETED".equals(action) || "android.intent.action.DATE_CHANGED".equals(action) || "android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action)) {
            refreshAll(context);
            startLiveUpdates(context);
        }
    }

    @Override // android.appwidget.AppWidgetProvider
    public void onEnabled(Context context) {
        super.onEnabled(context);
        scheduleNext(context);
        startLiveUpdates(context);
    }

    @Override // android.appwidget.AppWidgetProvider
    public void onDisabled(Context context) {
        super.onDisabled(context);
        WidgetUpdateService.stop(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (alarmManager != null) {
            alarmManager.cancel(refreshIntent(context));
        }
    }

    public static void refreshAll(Context context) {
        updateAll(context, true);
        startLiveUpdates(context);
    }

    static void updateAll(Context context, boolean z) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, (Class<?>) SalaryWidgetProvider.class));
        if (appWidgetIds == null || appWidgetIds.length == 0) {
            return;
        }
        MainActivity.SalarySettings load = MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0));
        Calendar calendar = Calendar.getInstance();
        MainActivity.SalarySnapshot calculate = MainActivity.SalaryCalculator.calculate(context, load, calendar);
        for (int i : appWidgetIds) {
            appWidgetManager.updateAppWidget(i, buildViews(context, load, calculate, calendar));
        }
        if (z) {
            scheduleNext(context);
        }
    }

    static void startLiveUpdates(Context context) {
        if (hasWidgets(context)) {
            WidgetUpdateService.start(context);
        }
    }

    static boolean hasWidgets(Context context) {
        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, (Class<?>) SalaryWidgetProvider.class));
        return appWidgetIds != null && appWidgetIds.length > 0;
    }

    static long recommendedInterval(Context context) {
        MainActivity.SalarySnapshot calculate = MainActivity.SalaryCalculator.calculate(context, MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0)), Calendar.getInstance());
        return (calculate.state == 2 || calculate.state == 4 || calculate.state == 6) ? 5000L : 30000L;
    }

    private static RemoteViews buildViews(Context context, MainActivity.SalarySettings salarySettings, MainActivity.SalarySnapshot salarySnapshot, Calendar calendar) {
        Context context2;
        RemoteViews remoteViews;
        int i;
        RemoteViews remoteViews2 = new RemoteViews(context.getPackageName(), R.layout.widget_salary);
        int color = context.getResources().getColor(R.color.widget_accent);
        remoteViews2.setTextViewText(R.id.widget_clock, String.format(Locale.getDefault(), "%02d:%02d:%02d", Integer.valueOf(calendar.get(11)), Integer.valueOf(calendar.get(12)), Integer.valueOf(calendar.get(13))));
        remoteViews2.setTextViewText(R.id.widget_amount, "¥ " + MONEY.format(salarySnapshot.earned));
        remoteViews2.setProgressBar(R.id.widget_progress, 1000, (int) Math.round(salarySnapshot.progress * 1000.0d), false);
        WaterReminderSettings load = WaterReminderSettings.load(context);
        int consumed = WaterReminderScheduler.getConsumed(context);
        remoteViews2.setTextViewText(R.id.widget_water_text, "今日喝水 " + consumed + " / " + load.targetMl + " ml");
        remoteViews2.setProgressBar(R.id.widget_water_progress, Math.max(1, load.targetMl), Math.min(consumed, load.targetMl), false);
        remoteViews2.setTextViewText(R.id.widget_cumulative, cumulativeText(context));
        remoteViews2.setTextViewText(R.id.widget_next_water, WaterReminderScheduler.nextReminderText(context));
        remoteViews2.setTextViewText(R.id.widget_drink, "+" + load.cupMl + " ml");
        remoteViews2.setOnClickPendingIntent(R.id.widget_drink, waterDrinkIntent(context));
        remoteViews2.setTextColor(R.id.widget_amount, color);
        remoteViews2.setViewVisibility(R.id.widget_timer, 0);
        try {
            context2 = context;
            try {
                remoteViews2.setImageViewBitmap(R.id.widget_pet, WorkerPetView.renderBitmap(context, WorkerPetView.poseForState(context, salarySnapshot.state), 120, WorkerPet.phaseFrom(calendar), PetState.fatigueLevel(context), false));
            } catch (Throwable th) {
            }
        } catch (Throwable th2) {
            context2 = context;
        }
        if (salarySnapshot.state == 0) {
            remoteViews2.setTextViewText(R.id.widget_status, WidgetText.restText(WorkdayHelper.restNote(context2, salarySettings, calendar), salarySnapshot.extraEarned > 0.0d ? MONEY.format(salarySnapshot.extraEarned) : null));
            remoteViews = remoteViews2;
            remoteViews.setChronometer(R.id.widget_timer, SystemClock.elapsedRealtime(), "", false);
            remoteViews.setViewVisibility(R.id.widget_timer, 8);
            i = -9340278;
        } else {
            remoteViews = remoteViews2;
            if (salarySnapshot.state == 7) {
                remoteViews.setTextViewText(R.id.widget_status, WidgetText.holidayWorkText(WorkdayHelper.restNote(context2, salarySettings, calendar), salarySnapshot.holidayComp, salarySnapshot.holidayRateX10));
                remoteViews.setChronometer(R.id.widget_timer, SystemClock.elapsedRealtime(), "", false);
                remoteViews.setViewVisibility(R.id.widget_timer, 8);
                i = -2525434;
            } else if (salarySnapshot.state == 8) {
                remoteViews.setTextViewText(R.id.widget_status, WidgetText.leaveText(salarySnapshot.leaveName));
                remoteViews.setChronometer(R.id.widget_timer, SystemClock.elapsedRealtime(), "", false);
                remoteViews.setViewVisibility(R.id.widget_timer, 8);
                i = -7643914;
            } else {
                if (salarySnapshot.state == 1) {
                    remoteViews.setTextViewText(R.id.widget_status, "还没上班");
                    startCountdown(remoteViews, millisUntil(calendar, salarySettings.workStartMinutes), "距开工 %s");
                } else if (salarySnapshot.state == 2) {
                    remoteViews.setTextViewText(R.id.widget_status, "工作中");
                    startCountdown(remoteViews, millisUntil(calendar, salarySettings.breakStartMinutes), "距午休 %s");
                } else if (salarySnapshot.state == 3) {
                    remoteViews.setTextViewText(R.id.widget_status, "午休中");
                    startCountdown(remoteViews, millisUntil(calendar, salarySettings.breakEndMinutes), "距下午工作 %s");
                } else if (salarySnapshot.state == 4) {
                    remoteViews.setTextViewText(R.id.widget_status, "工作中");
                    startCountdown(remoteViews, millisUntil(calendar, salarySettings.workEndMinutes), "距下班 %s");
                } else if (salarySnapshot.state == 6) {
                    remoteViews.setTextViewText(R.id.widget_status, "加班中 · 已 " + OvertimeLog.format(salarySnapshot.overtimeSeconds));
                    remoteViews.setChronometer(R.id.widget_timer, SystemClock.elapsedRealtime(), "", false);
                    remoteViews.setViewVisibility(R.id.widget_timer, 8);
                } else {
                    remoteViews.setTextViewText(R.id.widget_status, "今日已下班");
                    remoteViews.setChronometer(R.id.widget_timer, SystemClock.elapsedRealtime(), "", false);
                    remoteViews.setViewVisibility(R.id.widget_timer, 8);
                }
                i = -14326805;
            }
        }
        remoteViews.setTextColor(R.id.widget_status, i);
        remoteViews.setOnClickPendingIntent(R.id.widget_root, openAppIntent(context2));
        return remoteViews;
    }

    private static void startCountdown(RemoteViews remoteViews, long j, String str) {
        remoteViews.setChronometer(R.id.widget_timer, SystemClock.elapsedRealtime() + Math.max(0L, j), str, true);
        remoteViews.setChronometerCountDown(R.id.widget_timer, true);
    }

    private static long millisUntil(Calendar calendar, int i) {
        return Math.max(0L, (i * REFRESH_IDLE_MS) - ((((calendar.get(11) * 3600000) + (calendar.get(12) * REFRESH_IDLE_MS)) + (calendar.get(13) * 1000)) + calendar.get(14)));
    }

    private static PendingIntent openAppIntent(Context context) {
        Intent intent = new Intent(context, (Class<?>) MainActivity.class);
        intent.setFlags(335544320);
        return PendingIntent.getActivity(context, 4100, intent, 201326592);
    }

    private static PendingIntent waterDrinkIntent(Context context) {
        Intent intent = new Intent(context, (Class<?>) WaterReminderReceiver.class);
        intent.setAction("com.salaryjump.app.action.WATER_DRINK");
        return PendingIntent.getBroadcast(context, 4213, intent, 201326592);
    }

    private static PendingIntent refreshIntent(Context context) {
        Intent intent = new Intent(context, (Class<?>) SalaryWidgetProvider.class);
        intent.setAction(ACTION_REFRESH);
        return PendingIntent.getBroadcast(context, REQUEST_REFRESH, intent, 201326592);
    }

    private static void scheduleNext(Context context) {
        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, (Class<?>) SalaryWidgetProvider.class));
        if (appWidgetIds == null || appWidgetIds.length == 0) {
            return;
        }
        long j = isWorking(MainActivity.SalaryCalculator.calculate(context, MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0)), Calendar.getInstance()).state) ? REFRESH_WORKING_MS : REFRESH_IDLE_MS;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (alarmManager != null) {
            alarmManager.set(1, System.currentTimeMillis() + j, refreshIntent(context));
        }
    }
}
