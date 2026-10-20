package com.salaryjump.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.salaryjump.app.MainActivity;
import java.util.Calendar;

/* loaded from: classes.dex */
public final class CheckInReminder {
    public static final int DEFAULT_HOUR = 20;
    public static final int DEFAULT_MINUTE = 0;
    private static final String KEY_ENABLED = "checkin_enabled";
    private static final String KEY_HOUR = "checkin_hour";
    private static final String KEY_MINUTE = "checkin_minute";
    private static final int REQ_REMIND = 53411;

    private CheckInReminder() {
    }

    static boolean isEnabled(Context context) {
        if (context == null) {
            return false;
        }
        return context.getSharedPreferences("salary_jump", 0).getBoolean(KEY_ENABLED, false);
    }

    static int hour(Context context) {
        if (context == null) {
            return 20;
        }
        return clampHour(context.getSharedPreferences("salary_jump", 0).getInt(KEY_HOUR, 20));
    }

    static int minute(Context context) {
        if (context == null) {
            return 0;
        }
        return clampMinute(context.getSharedPreferences("salary_jump", 0).getInt(KEY_MINUTE, 0));
    }

    static void set(Context context, boolean z, int i, int i2) {
        if (context == null) {
            return;
        }
        context.getSharedPreferences("salary_jump", 0).edit().putBoolean(KEY_ENABLED, z).putInt(KEY_HOUR, clampHour(i)).putInt(KEY_MINUTE, clampMinute(i2)).apply();
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
        arm(alarmManager, REQ_REMIND, nextTrigger(Calendar.getInstance(), hour(context), minute(context)), reminderIntent(context));
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
        alarmManager.cancel(reminderIntent(context));
    }

    private static PendingIntent reminderIntent(Context context) {
        Intent intent = new Intent(context, (Class<?>) CheckInReceiver.class);
        intent.setAction("com.salaryjump.app.action.CHECKIN_REMIND");
        return PendingIntent.getBroadcast(context, REQ_REMIND, intent, pendingFlag());
    }

    private static int pendingFlag() {
        return 201326592;
    }

    public static long nextTrigger(Calendar calendar, int i, int i2) {
        Calendar calendar2 = (Calendar) calendar.clone();
        calendar2.set(11, clampHour(i));
        calendar2.set(12, clampMinute(i2));
        calendar2.set(13, 0);
        calendar2.set(14, 0);
        if (calendar2.getTimeInMillis() <= calendar.getTimeInMillis()) {
            calendar2.add(5, 1);
        }
        return calendar2.getTimeInMillis();
    }

    public static int clampHour(int i) {
        if (i < 0) {
            return 0;
        }
        if (i > 23) {
            return 23;
        }
        return i;
    }

    public static int clampMinute(int i) {
        if (i < 0) {
            return 0;
        }
        if (i > 59) {
            return 59;
        }
        return i;
    }

    public static int reminderKind(boolean z, boolean z2, boolean z3, boolean z4, boolean z5) {
        if (z) {
            if (!z2 || z3 || z4) {
                return 0;
            }
            return 1;
        }
        if (z5) {
            return 0;
        }
        return 2;
    }

    static int currentKind(Context context) {
        Calendar calendar = Calendar.getInstance();
        int stamp = WorkdayHelper.stamp(calendar);
        MainActivity.SalarySettings load = MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0));
        return reminderKind(WorkdayHelper.isWorkday(context, load, calendar), load.overtimeEnabled, OvertimeLog.hasPunch(context, stamp), LeaveRecord.has(context, stamp), HolidayWork.has(context, stamp));
    }
}
