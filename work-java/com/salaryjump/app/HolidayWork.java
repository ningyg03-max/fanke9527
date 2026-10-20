package com.salaryjump.app;

import android.content.Context;
import com.salaryjump.app.MainActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/* loaded from: classes.dex */
public final class HolidayWork {
    private static final int KEEP_DAYS = 400;
    private static final String KEY = "holiday_work";

    private HolidayWork() {
    }

    public static class Entry {
        public boolean comp;
        public int rateX10;
        public int stamp;

        Entry(int i, int i2) {
            this(i, i2, false);
        }

        Entry(int i, int i2, boolean z) {
            this.stamp = i;
            this.rateX10 = i2;
            this.comp = z;
        }
    }

    static List<Entry> load(Context context) {
        ArrayList arrayList = new ArrayList();
        String string = context.getSharedPreferences("salary_jump", 0).getString(KEY, "");
        if (string == null || string.length() == 0) {
            return arrayList;
        }
        for (String str : string.split(";")) {
            String[] split = str.split(",");
            if (split.length >= 2) {
                try {
                    arrayList.add(new Entry(Integer.parseInt(split[0]), Integer.parseInt(split[1]), split.length >= 3 && "1".equals(split[2])));
                } catch (NumberFormatException e) {
                }
            }
        }
        return arrayList;
    }

    private static void save(Context context, List<Entry> list) {
        int i = 0;
        while (i < list.size()) {
            int i2 = i + 1;
            for (int i3 = i2; i3 < list.size(); i3++) {
                if (list.get(i3).stamp < list.get(i).stamp) {
                    Entry entry = list.get(i);
                    list.set(i, list.get(i3));
                    list.set(i3, entry);
                }
            }
            i = i2;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(6, -400);
        int stamp = WorkdayHelper.stamp(calendar);
        StringBuilder sb = new StringBuilder();
        for (Entry entry2 : list) {
            if (entry2.stamp >= stamp) {
                if (sb.length() > 0) {
                    sb.append(';');
                }
                sb.append(entry2.stamp).append(',').append(entry2.rateX10).append(',').append(entry2.comp ? 1 : 0);
            }
        }
        context.getSharedPreferences("salary_jump", 0).edit().putString(KEY, sb.toString()).apply();
    }

    static void put(Context context, int i, int i2) {
        put(context, i, i2, false);
    }

    static void put(Context context, int i, int i2, boolean z) {
        boolean z2;
        double contribution = ArchiveSync.contribution(context, i);
        List<Entry> load = load(context);
        Iterator<Entry> it = load.iterator();
        while (true) {
            if (!it.hasNext()) {
                z2 = false;
                break;
            }
            Entry next = it.next();
            if (next.stamp == i) {
                next.rateX10 = i2;
                next.comp = z;
                z2 = true;
                break;
            }
        }
        if (!z2) {
            load.add(new Entry(i, i2, z));
        }
        save(context, load);
        ArchiveSync.adjust(context, i, Math.round((ArchiveSync.contribution(context, i) - contribution) * 100.0d));
        ArchiveSync.syncEventRow(context, i);
    }

    static void remove(Context context, int i) {
        double contribution = ArchiveSync.contribution(context, i);
        List<Entry> load = load(context);
        for (int size = load.size() - 1; size >= 0; size--) {
            if (load.get(size).stamp == i) {
                load.remove(size);
            }
        }
        save(context, load);
        ArchiveSync.adjust(context, i, Math.round((ArchiveSync.contribution(context, i) - contribution) * 100.0d));
        ArchiveSync.syncEventRow(context, i);
    }

    static boolean has(Context context, int i) {
        Iterator<Entry> it = load(context).iterator();
        while (it.hasNext()) {
            if (it.next().stamp == i) {
                return true;
            }
        }
        return false;
    }

    static int rateX10Of(Context context, int i) {
        for (Entry entry : load(context)) {
            if (entry.stamp == i) {
                return entry.rateX10;
            }
        }
        return 0;
    }

    static boolean isComp(Context context, int i) {
        for (Entry entry : load(context)) {
            if (entry.stamp == i) {
                return entry.comp;
            }
        }
        return false;
    }

    static int defaultRateX10(Context context, Calendar calendar) {
        return WorkRules.restDayRateX10(context, StatutoryHolidays.isStatutory(calendar));
    }

    static double earned(double d, int i) {
        if (d <= 0.0d || i <= 0) {
            return 0.0d;
        }
        return d * (i / 10.0d);
    }

    public static String rateText(int i) {
        if (i % 10 == 0) {
            return (i / 10) + " 倍";
        }
        return String.format(Locale.US, "%.1f 倍", Float.valueOf(i / 10.0f));
    }

    static boolean isRestDay(Context context, MainActivity.SalarySettings salarySettings, Calendar calendar) {
        return !WorkdayHelper.isWorkday(context, salarySettings, calendar);
    }
}
