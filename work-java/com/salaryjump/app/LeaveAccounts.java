package com.salaryjump.app;

import android.content.Context;
import android.content.SharedPreferences;
import com.salaryjump.app.LeaveRecord;
import java.util.Calendar;

/* loaded from: classes.dex */
public final class LeaveAccounts {
    private static final String KEY_COMP_INITIAL = "la_comp_initial";
    private static final String SUFFIX_ADJUST = "_adjust";
    private static final String SUFFIX_QUOTA = "_quota";
    static final String[] YEAR_KEYS = {"annual", "parental", "contrib", "sick", "personal"};
    public static final String[] YEAR_NAMES = {"年假", "育儿假", "长期贡献奖假", "病假", "事假"};

    private LeaveAccounts() {
    }

    static boolean isSet(Context context, int i) {
        return prefs(context).contains("la_" + YEAR_KEYS[i] + SUFFIX_QUOTA);
    }

    static double quota(Context context, int i) {
        return getDouble(prefs(context), "la_" + YEAR_KEYS[i] + SUFFIX_QUOTA);
    }

    static double adjust(Context context, int i) {
        return getDouble(prefs(context), "la_" + YEAR_KEYS[i] + SUFFIX_ADJUST);
    }

    static void setBudget(Context context, int i, double d, double d2) {
        prefs(context).edit().putLong("la_" + YEAR_KEYS[i] + SUFFIX_QUOTA, Double.doubleToRawLongBits(d)).putLong("la_" + YEAR_KEYS[i] + SUFFIX_ADJUST, Double.doubleToRawLongBits(d2)).apply();
    }

    static double usedThisYear(Context context, int i) {
        return usedThisYear(context, YEAR_NAMES[i]);
    }

    static double usedThisYear(Context context, String str) {
        return usedInYear(context, str, Calendar.getInstance().get(1));
    }

    static double usedInYear(Context context, String str, int i) {
        double d = 0.0d;
        for (LeaveRecord.Entry entry : LeaveRecord.load(context)) {
            if (str.equals(entry.name) && SalaryHistory.yearOf(entry.stamp) == i) {
                d += entry.half ? 0.5d : 1.0d;
            }
        }
        return d;
    }

    static double remaining(Context context, int i) {
        return (quota(context, i) + adjust(context, i)) - usedThisYear(context, i);
    }

    static boolean compSet(Context context) {
        return prefs(context).contains(KEY_COMP_INITIAL);
    }

    static double compInitial(Context context) {
        return getDouble(prefs(context), KEY_COMP_INITIAL);
    }

    static void setCompInitial(Context context, double d) {
        prefs(context).edit().putLong(KEY_COMP_INITIAL, Double.doubleToRawLongBits(d)).apply();
    }

    static double compRemaining(Context context) {
        return (compInitial(context) + WorkRules.compEarned(context)) - WorkRules.compUsed(context);
    }

    static String remText(Context context, String str) {
        if ("调休".equals(str)) {
            if (compSet(context)) {
                return "剩 " + WorkRules.formatDays(compRemaining(context)) + " 天";
            }
            return null;
        }
        for (int i = 0; i < YEAR_NAMES.length; i++) {
            if (YEAR_NAMES[i].equals(str)) {
                if (isSet(context, i)) {
                    return "剩 " + WorkRules.formatDays(remaining(context, i)) + " 天";
                }
                return null;
            }
        }
        return null;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("salary_jump", 0);
    }

    private static double getDouble(SharedPreferences sharedPreferences, String str) {
        return Double.longBitsToDouble(sharedPreferences.getLong(str, 0L));
    }
}
