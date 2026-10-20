package com.salaryjump.app;

import android.content.Context;
import android.content.SharedPreferences;
import com.salaryjump.app.HolidayWork;
import com.salaryjump.app.LeaveRecord;
import java.util.Iterator;
import java.util.Locale;

/* loaded from: classes.dex */
public final class WorkRules {
    public static final int WEEKEND_COMP = 1;
    public static final int WEEKEND_PAY = 0;

    private WorkRules() {
    }

    static int overtimeRateX10(Context context) {
        return prefs(context).getInt("overtime_rate", 15);
    }

    static void setOvertimeRateX10(Context context, int i) {
        prefs(context).edit().putInt("overtime_rate", i).apply();
    }

    static int overtimeGraceMinutes(Context context) {
        return prefs(context).getInt("overtime_grace", 30);
    }

    static void setOvertimeGraceMinutes(Context context, int i) {
        prefs(context).edit().putInt("overtime_grace", i).apply();
    }

    static int weekendMode(Context context) {
        return prefs(context).getInt("rule_weekend_mode", 1);
    }

    static void setWeekendMode(Context context, int i) {
        prefs(context).edit().putInt("rule_weekend_mode", i != 1 ? 0 : 1).apply();
    }

    static int weekendRateX10(Context context) {
        return prefs(context).getInt("rule_weekend_x10", 20);
    }

    static void setWeekendRateX10(Context context, int i) {
        prefs(context).edit().putInt("rule_weekend_x10", i).apply();
    }

    static boolean isCompMode(Context context) {
        return weekendMode(context) == 1;
    }

    static int statutoryRateX10(Context context) {
        return prefs(context).getInt("rule_statutory_x10", 30);
    }

    static void setStatutoryRateX10(Context context, int i) {
        prefs(context).edit().putInt("rule_statutory_x10", i).apply();
    }

    static int restDayRateX10(Context context, boolean z) {
        if (z) {
            return statutoryRateX10(context);
        }
        if (isCompMode(context)) {
            return 0;
        }
        return weekendRateX10(context);
    }

    static boolean useCompFor(Context context, boolean z) {
        return !z && isCompMode(context);
    }

    static double compEarned(Context context) {
        Iterator<HolidayWork.Entry> it = HolidayWork.load(context).iterator();
        int i = 0;
        while (it.hasNext()) {
            if (it.next().comp) {
                i++;
            }
        }
        return i;
    }

    static double compUsed(Context context) {
        double d = 0.0d;
        for (LeaveRecord.Entry entry : LeaveRecord.load(context)) {
            if ("调休".equals(entry.name)) {
                d += entry.half ? 0.5d : 1.0d;
            }
        }
        return d;
    }

    static double compBalance(Context context) {
        return LeaveAccounts.compRemaining(context);
    }

    public static String formatDays(double d) {
        if (d == Math.floor(d) && !Double.isInfinite(d)) {
            return String.valueOf((int) d);
        }
        return String.format(Locale.US, "%.1f", Double.valueOf(d));
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("salary_jump", 0);
    }
}
