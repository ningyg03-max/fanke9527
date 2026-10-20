package com.salaryjump.app;

import android.content.Context;
import com.salaryjump.app.MainActivity;
import com.salaryjump.app.SalaryHistory;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/* loaded from: classes.dex */
final class MonthBackfill {
    private static final int KEEP_RECORDS = 240;
    private static final String KEY = "month_backfill";
    static final long MAX_CENTS = 100000000;

    private MonthBackfill() {
    }

    static class Entry {
        long cents;
        String remark;
        int yearMonth;

        Entry() {
        }
    }

    static List<Entry> load(Context context) {
        ArrayList arrayList = new ArrayList();
        if (context == null) {
            return arrayList;
        }
        String string = context.getSharedPreferences("salary_jump", 0).getString(KEY, "");
        if (string == null || string.length() == 0) {
            return arrayList;
        }
        for (String str : string.split(";")) {
            String[] split = str.split(",");
            if (split.length >= 2) {
                try {
                    Entry entry = new Entry();
                    entry.yearMonth = Integer.parseInt(split[0].trim());
                    entry.cents = Long.parseLong(split[1].trim());
                    entry.remark = split.length >= 3 ? split[2] : "";
                    if (entry.cents > 0) {
                        arrayList.add(entry);
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return arrayList;
    }

    private static void save(Context context, List<Entry> list) {
        StringBuilder sb = new StringBuilder();
        for (Entry entry : list) {
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(entry.yearMonth).append(',').append(entry.cents).append(',').append(entry.remark == null ? "" : entry.remark);
        }
        context.getSharedPreferences("salary_jump", 0).edit().putString(KEY, sb.toString()).apply();
    }

    static long monthCents(Context context, int i, int i2) {
        int i3 = (i * 100) + i2;
        long j = 0;
        for (Entry entry : load(context)) {
            if (entry.yearMonth == i3) {
                j += entry.cents;
            }
        }
        return j;
    }

    static boolean hasMonth(Context context, int i, int i2) {
        return monthCents(context, i, i2) > 0;
    }

    static double goalProgress(Context context, int i) {
        double d = 0.0d;
        if (context == null || i <= 0) {
            return 0.0d;
        }
        for (SalaryHistory.Day day : SalaryHistory.load(context)) {
            if (day.stamp >= i) {
                long centsOn = day.cents - centsOn(context, day.stamp);
                if (centsOn > 0) {
                    d += centsOn / 100.0d;
                }
            }
        }
        return d;
    }

    static int statDays(Context context, int i, int i2, int i3) {
        if (context == null || monthCents(context, i, i2) <= 0) {
            return i3;
        }
        try {
            int workdaysInMonth = WorkdayHelper.workdaysInMonth(context, MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0)), i, i2);
            if (workdaysInMonth <= 0) {
                return i3;
            }
            return Math.max(workdaysInMonth, i3);
        } catch (Throwable th) {
            return i3;
        }
    }

    static int lastDayStamp(int i, int i2) {
        return (i * 10000) + (i2 * 100) + PaydayReminder.daysInMonth(i, i2);
    }

    static long centsOn(Context context, int i) {
        if (i <= 0) {
            return 0L;
        }
        int i2 = i % 100;
        int i3 = (i / 100) % 100;
        int i4 = i / 10000;
        if (i3 < 1 || i3 > 12 || i2 != PaydayReminder.daysInMonth(i4, i3)) {
            return 0L;
        }
        return monthCents(context, i4, i3);
    }

    static boolean add(Context context, int i, int i2, long j, String str) {
        if (context == null || j <= 0 || i2 < 1 || i2 > 12 || lastDayStamp(i, i2) < earliestAllowedStamp()) {
            return false;
        }
        List<Entry> load = load(context);
        Entry entry = new Entry();
        entry.yearMonth = (i * 100) + i2;
        entry.cents = Math.min(MAX_CENTS, j);
        entry.remark = ExtraIncome.cleanRemark(str);
        load.add(entry);
        while (load.size() > KEEP_RECORDS) {
            load.remove(0);
        }
        save(context, load);
        sync(context, i, i2, entry.cents);
        return true;
    }

    static int earliestAllowedStamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(5, -1490);
        return (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
    }

    static void removeAll(Context context, int i, int i2) {
        if (context == null) {
            return;
        }
        long monthCents = monthCents(context, i, i2);
        if (monthCents <= 0) {
            return;
        }
        int i3 = (i * 100) + i2;
        List<Entry> load = load(context);
        for (int size = load.size() - 1; size >= 0; size--) {
            if (load.get(size).yearMonth == i3) {
                load.remove(size);
            }
        }
        save(context, load);
        sync(context, i, i2, -monthCents);
        int lastDayStamp = lastDayStamp(i, i2);
        if (SalaryHistory.has(context, lastDayStamp) && SalaryHistory.centsOf(context, lastDayStamp) == 0) {
            SalaryHistory.remove(context, lastDayStamp);
        }
    }

    private static void sync(Context context, int i, int i2, long j) {
        int lastDayStamp = lastDayStamp(i, i2);
        if (ArchiveSync.isArchived(context, lastDayStamp)) {
            ArchiveSync.adjust(context, lastDayStamp, j);
        } else {
            SalaryHistory.adjust(context, lastDayStamp, j);
        }
    }
}
