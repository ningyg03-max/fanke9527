package com.salaryjump.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.Calendar;

/* loaded from: classes.dex */
public final class PaydayReminder {
    public static final int FIRE_HOUR = 10;
    private static final String KEY_DAY = "payday_day";
    private static final String KEY_ENABLED = "payday_enabled";
    private static final int REQ_REMIND = 53401;
    private static final int REQ_REMIND_EVE = 53402;

    private PaydayReminder() {
    }

    static boolean isEnabled(Context context) {
        if (context == null) {
            return false;
        }
        return context.getSharedPreferences("salary_jump", 0).getBoolean(KEY_ENABLED, false);
    }

    static int day(Context context) {
        if (context == null) {
            return 15;
        }
        return clampDay(context.getSharedPreferences("salary_jump", 0).getInt(KEY_DAY, 15));
    }

    static void set(Context context, boolean z, int i) {
        if (context == null) {
            return;
        }
        context.getSharedPreferences("salary_jump", 0).edit().putBoolean(KEY_ENABLED, z).putInt(KEY_DAY, clampDay(i)).apply();
        if (z) {
            schedule(context);
        } else {
            cancel(context);
        }
    }

    static void schedule(Context context) {
        if (context == null || !isEnabled(context)) {
            cancel(context);
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (alarmManager == null) {
            return;
        }
        int day = day(context);
        long nextTrigger = nextTrigger(Calendar.getInstance(), day, 0);
        long nextTrigger2 = nextTrigger(Calendar.getInstance(), day, -1);
        arm(alarmManager, REQ_REMIND, nextTrigger, reminderIntent(context, REQ_REMIND, false));
        arm(alarmManager, REQ_REMIND_EVE, nextTrigger2, reminderIntent(context, REQ_REMIND_EVE, true));
    }

    private static void arm(AlarmManager alarmManager, int i, long j, PendingIntent pendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= 31 && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(0, j, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(0, j, pendingIntent);
            }
        } catch (Throwable th) {
            try {
                alarmManager.set(0, j, pendingIntent);
            } catch (Throwable th2) {
            }
        }
    }

    static void cancel(Context context) {
        AlarmManager alarmManager;
        if (context == null || (alarmManager = (AlarmManager) context.getSystemService("alarm")) == null) {
            return;
        }
        alarmManager.cancel(reminderIntent(context, REQ_REMIND, false));
        alarmManager.cancel(reminderIntent(context, REQ_REMIND_EVE, true));
    }

    private static PendingIntent reminderIntent(Context context, int i, boolean z) {
        Intent intent = new Intent(context, (Class<?>) PaydayReceiver.class);
        intent.setAction(z ? "com.salaryjump.app.action.PAYDAY_EVE" : "com.salaryjump.app.action.PAYDAY");
        return PendingIntent.getBroadcast(context, i, intent, pendingFlag());
    }

    private static int pendingFlag() {
        return 201326592;
    }

    public static int daysInMonth(int i, int i2) {
        switch (i2) {
            case 1:
                return 31;
            case WaterReminderSettings.MODE_BOTH /* 2 */:
                return ((i % 4 != 0 || i % 100 == 0) && i % 400 != 0) ? 28 : 29;
            case 3:
                return 31;
            case PrivacyLock.PIN_LENGTH /* 4 */:
                return 30;
            case PrivacyLock.MAX_FAILS /* 5 */:
                return 31;
            case 6:
                return 30;
            case 7:
                return 31;
            case DrinkTypes.BUILTIN /* 8 */:
                return 31;
            case 9:
                return 30;
            case FIRE_HOUR /* 10 */:
                return 31;
            case 11:
                return 30;
            default:
                return 31;
        }
    }

    public static int effectiveDay(int i, int i2, int i3) {
        int daysInMonth = daysInMonth(i, i2);
        if (i3 > daysInMonth) {
            return daysInMonth;
        }
        if (i3 < 1) {
            return 1;
        }
        return i3;
    }

    public static int clampDay(int i) {
        if (i < 1) {
            return 1;
        }
        if (i > 31) {
            return 31;
        }
        return i;
    }

    public static long nextTrigger(Calendar calendar, int i, int i2) {
        Calendar calendar2 = (Calendar) calendar.clone();
        int i3 = calendar2.get(1);
        int i4 = calendar2.get(2);
        int effectiveDay = effectiveDay(i3, i4 + 1, i);
        calendar2.set(1, i3);
        calendar2.set(2, i4);
        calendar2.set(5, effectiveDay);
        calendar2.set(11, 10);
        calendar2.set(12, 0);
        calendar2.set(13, 0);
        calendar2.set(14, 0);
        calendar2.add(5, i2);
        if (calendar2.getTimeInMillis() <= calendar.getTimeInMillis()) {
            calendar2.add(2, 1);
            calendar2.set(5, effectiveDay(calendar2.get(1), calendar2.get(2) + 1, i));
            calendar2.set(11, 10);
            calendar2.set(12, 0);
            calendar2.set(13, 0);
            calendar2.set(14, 0);
            calendar2.add(5, i2);
        }
        return calendar2.getTimeInMillis();
    }

    public static String periodText(int i, int i2) {
        return i2 + " 月工资";
    }
}
