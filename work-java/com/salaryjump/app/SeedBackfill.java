package com.salaryjump.app;

import android.content.Context;
import android.content.SharedPreferences;
import com.salaryjump.app.MainActivity;
import com.salaryjump.app.SalaryHistory;
import java.util.Calendar;
import java.util.Iterator;

/* loaded from: classes.dex */
final class SeedBackfill {
    private static final String FLAG = "seed_backfill_2026_10_v1";
    private static final String FLAG_V2 = "seed_backfill_2026_10_v2";
    static final double MONTHLY_SALARY = 18140.0d;

    private SeedBackfill() {
    }

    static void applyOnce(Context context) {
        if (context == null) {
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        if (!sharedPreferences.getBoolean(FLAG, false)) {
            alignMonthlySalary(context);
            HolidayWork.put(context, 20261006, 0, true);
            LeaveRecord.put(context, 20261008, "育儿假", 100, false);
            LeaveRecord.put(context, 20261009, "育儿假", 100, false);
            ArchiveSync.recalcArchivedRow(context, 20261008);
            ArchiveSync.recalcArchivedRow(context, 20261009);
            sharedPreferences.edit().putBoolean(FLAG, true).apply();
        }
        if (!sharedPreferences.getBoolean(FLAG_V2, false)) {
            applyFix(context);
            sharedPreferences.edit().putBoolean(FLAG_V2, true).apply();
        }
    }

    private static void applyFix(Context context) {
        HolidayWork.put(context, 20261006, 0, true);
        LeaveRecord.put(context, 20261008, "育儿假", 100, false);
        LeaveRecord.put(context, 20261009, "育儿假", 100, false);
        ArchiveSync.syncEventRow(context, 20261006);
        ArchiveSync.syncEventRow(context, 20261008);
        ArchiveSync.syncEventRow(context, 20261009);
        Iterator<SalaryHistory.Day> it = SalaryHistory.byMonth(context, 2026, 10).iterator();
        while (it.hasNext()) {
            ArchiveSync.recalcArchivedRow(context, it.next().stamp);
        }
        reconcileCumulative(context);
    }

    private static void reconcileCumulative(Context context) {
        int i = 0;
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        int i2 = sharedPreferences.getInt("cum_last_day", 0);
        long j = 0;
        for (SalaryHistory.Day day : SalaryHistory.load(context)) {
            if (day.stamp <= i2) {
                j += day.cents;
                if (ArchiveSync.countsAsSettledDay(context, day.stamp)) {
                    i++;
                }
            }
        }
        sharedPreferences.edit().putLong("cum_total_bits", Double.doubleToRawLongBits(j / 100.0d)).putInt("cum_days", i).apply();
    }

    static void alignMonthlySalary(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        MainActivity.SalarySettings load = MainActivity.SalarySettings.load(sharedPreferences);
        Calendar calendar = Calendar.getInstance();
        int workdaysInMonth = WorkdayHelper.workdaysInMonth(context, load, calendar.get(1), calendar.get(2) + 1);
        if (workdaysInMonth <= 0) {
            return;
        }
        double d = MONTHLY_SALARY / workdaysInMonth;
        if (Math.abs(load.dailySalary - d) < 0.005d) {
            return;
        }
        sharedPreferences.edit().putLong("daily_salary_bits", Double.doubleToRawLongBits(d)).apply();
    }
}
