package com.salaryjump.app;

import android.content.Context;
import android.content.SharedPreferences;
import com.salaryjump.app.HolidayWork;
import com.salaryjump.app.LeaveRecord;
import com.salaryjump.app.MainActivity;
import java.util.Calendar;

/* loaded from: classes.dex */
final class ArchiveSync {
    private ArchiveSync() {
    }

    static boolean isArchived(Context context, int i) {
        int i2;
        return context != null && (i2 = context.getSharedPreferences("salary_jump", 0).getInt("cum_last_day", 0)) > 0 && i <= i2;
    }

    static void adjust(Context context, int i, long j) {
        if (context == null || j == 0 || !isArchived(context, i)) {
            return;
        }
        SalaryHistory.adjust(context, i, j);
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        sharedPreferences.edit().putLong("cum_total_bits", Double.doubleToRawLongBits(Math.max(0.0d, Double.longBitsToDouble(sharedPreferences.getLong("cum_total_bits", 0L)) + (j / 100.0d)))).apply();
    }

    static double contribution(Context context, int i) {
        if (context == null) {
            return 0.0d;
        }
        int rateX10Of = HolidayWork.rateX10Of(context, i);
        if (rateX10Of > 0) {
            return HolidayWork.earned(dailySalary(context), rateX10Of);
        }
        if (HolidayWork.isComp(context, i)) {
            return 0.0d;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(i / 10000, ((i / 100) % 100) - 1, i % 100);
        if (!WorkdayHelper.isWorkday(context, MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0)), calendar)) {
            return 0.0d;
        }
        LeaveRecord.Entry entry = LeaveRecord.get(context, i);
        if (entry != null) {
            return LeaveRecord.earned(dailySalary(context), entry.payPercent, entry.half);
        }
        return dailySalary(context);
    }

    static double dailySalary(Context context) {
        return MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0)).dailySalary;
    }

    static void onOvertimeRemoved(Context context, int i, long j) {
        if (context == null) {
            return;
        }
        if (isArchived(context, i)) {
            adjust(context, i, -j);
        } else {
            refreshPrewritten(context, i);
        }
    }

    static long expectedCents(Context context, int i) {
        if (context == null) {
            return 0L;
        }
        long centsOf = ExtraIncome.centsOf(context, i) + MonthBackfill.centsOn(context, i);
        int rateX10Of = HolidayWork.rateX10Of(context, i);
        if (rateX10Of > 0) {
            return Math.round(HolidayWork.earned(dailySalary(context), rateX10Of) * 100.0d) + centsOf;
        }
        if (HolidayWork.isComp(context, i)) {
            return centsOf;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(i / 10000, ((i / 100) % 100) - 1, i % 100);
        if (!WorkdayHelper.isWorkday(context, MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0)), calendar)) {
            return centsOf;
        }
        LeaveRecord.Entry entry = LeaveRecord.get(context, i);
        return entry != null ? Math.round(LeaveRecord.earned(dailySalary(context), entry.payPercent, entry.half) * 100.0d) + centsOf : Math.round(dailySalary(context) * 100.0d) + centsOf;
    }

    static void refreshPrewritten(Context context, int i) {
        if (context == null || isArchived(context, i) || !SalaryHistory.has(context, i)) {
            return;
        }
        long expectedCents = expectedCents(context, i) + overtimeCents(context, i);
        if (SalaryHistory.centsOf(context, i) == expectedCents) {
            return;
        }
        if (expectedCents <= 0) {
            SalaryHistory.remove(context, i);
        } else {
            SalaryHistory.put(context, i, expectedCents);
        }
    }

    static long overtimeCents(Context context, int i) {
        MainActivity.SalarySettings load = MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0));
        if (!load.overtimeEnabled || load.overtimeRateX10 <= 0) {
            return 0L;
        }
        return Math.round(OvertimeCalc.earned(load.dailySalary, MainActivity.SalaryCalculator.totalWorkSeconds(load), OvertimeLog.secondsOfDay(context, i, System.currentTimeMillis()), load.overtimeRateX10) * 100.0d);
    }

    static void syncEventRow(Context context, int i) {
        if (context == null || !isArchived(context, i)) {
            return;
        }
        if (HolidayWork.has(context, i) || LeaveRecord.has(context, i)) {
            if (!SalaryHistory.has(context, i)) {
                backfillRow(context, i);
            }
        } else if (SalaryHistory.has(context, i) && SalaryHistory.centsOf(context, i) == 0 && ExtraIncome.centsOf(context, i) == 0) {
            SalaryHistory.remove(context, i);
        }
    }

    static void backfillRow(Context context, int i) {
        long max = Math.max(0L, expectedCents(context, i) + overtimeCents(context, i));
        SalaryHistory.put(context, i, max);
        boolean countsAsSettledDay = countsAsSettledDay(context, i);
        if (max == 0 && !countsAsSettledDay) {
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        sharedPreferences.edit().putLong("cum_total_bits", Double.doubleToRawLongBits(Math.max(0.0d, Double.longBitsToDouble(sharedPreferences.getLong("cum_total_bits", 0L)) + (max / 100.0d)))).putInt("cum_days", sharedPreferences.getInt("cum_days", 0) + (countsAsSettledDay ? 1 : 0)).apply();
    }

    static boolean countsAsSettledDay(Context context, int i) {
        if (HolidayWork.rateX10Of(context, i) > 0) {
            return true;
        }
        if (HolidayWork.isComp(context, i)) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(i / 10000, ((i / 100) % 100) - 1, i % 100);
        return WorkdayHelper.isWorkday(context, MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0)), calendar);
    }

    static void healEventRowsOnce(Context context) {
        if (context == null) {
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        if (!sharedPreferences.getBoolean("event_row_heal_v1", false)) {
            sharedPreferences.edit().putBoolean("event_row_heal_v1", true).apply();
            int i = sharedPreferences.getInt("cum_last_day", 0);
            if (i <= 0) {
                return;
            }
            for (HolidayWork.Entry entry : HolidayWork.load(context)) {
                if (entry.stamp <= i && !SalaryHistory.has(context, entry.stamp)) {
                    SalaryHistory.put(context, entry.stamp, Math.max(0L, expectedCents(context, entry.stamp)));
                }
            }
        }
    }

    static void recalcArchivedRow(Context context, int i) {
        if (context == null || !isArchived(context, i) || !SalaryHistory.has(context, i)) {
            return;
        }
        long expectedCents = expectedCents(context, i) + overtimeCents(context, i);
        long centsOf = expectedCents - SalaryHistory.centsOf(context, i);
        if (centsOf == 0) {
            return;
        }
        SalaryHistory.put(context, i, expectedCents);
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        sharedPreferences.edit().putLong("cum_total_bits", Double.doubleToRawLongBits(Math.max(0.0d, Double.longBitsToDouble(sharedPreferences.getLong("cum_total_bits", 0L)) + (centsOf / 100.0d)))).apply();
    }
}
