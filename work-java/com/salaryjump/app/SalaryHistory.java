package com.salaryjump.app;

import android.content.Context;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public final class SalaryHistory {
    static final int KEEP_DAYS = 1500;
    private static final String KEY = "salary_history";

    private SalaryHistory() {
    }

    public static class Day {
        public long cents;
        public int stamp;

        Day(int i, long j) {
            this.stamp = i;
            this.cents = j;
        }
    }

    static void put(Context context, int i, long j) {
        DataCache.invalidate();
        boolean z;
        List<Day> load = load(context);
        Iterator<Day> it = load.iterator();
        while (true) {
            if (!it.hasNext()) {
                z = false;
                break;
            }
            Day next = it.next();
            if (next.stamp == i) {
                next.cents = j;
                z = true;
                break;
            }
        }
        if (!z) {
            load.add(new Day(i, j));
        }
        save(context, load);
    }

    static void adjust(Context context, int i, long j) {
        boolean z;
        if (j == 0) {
            return;
        }
        List<Day> load = load(context);
        Iterator<Day> it = load.iterator();
        while (true) {
            if (!it.hasNext()) {
                z = false;
                break;
            }
            Day next = it.next();
            if (next.stamp == i) {
                next.cents = Math.max(0L, next.cents + j);
                z = true;
                break;
            }
        }
        if (!z) {
            load.add(new Day(i, Math.max(0L, j)));
        }
        save(context, load);
    }

    static void remove(Context context, int i) {
        DataCache.invalidate();
        List<Day> load = load(context);
        for (int size = load.size() - 1; size >= 0; size--) {
            if (load.get(size).stamp == i) {
                load.remove(size);
            }
        }
        save(context, load);
    }

    static boolean has(Context context, int i) {
        Iterator<Day> it = load(context).iterator();
        while (it.hasNext()) {
            if (it.next().stamp == i) {
                return true;
            }
        }
        return false;
    }

    static long centsOf(Context context, int i) {
        for (Day day : load(context)) {
            if (day.stamp == i) {
                return day.cents;
            }
        }
        return -1L;
    }

    static List<Day> load(Context context) {
        // 命中缓存直接返回（外面套新 ArrayList，避免调用方改到缓存本体）
        List<Day> cached = DataCache.salaryHistory(context);
        if (cached != null) {
            return new ArrayList(cached);
        }
        ArrayList arrayList = new ArrayList();
        String string = DataCache.prefs(context).getString(KEY, "");
        if (string == null || string.length() == 0) {
            return arrayList;
        }
        for (String str : string.split(";")) {
            String[] split = str.split(":");
            if (split.length >= 2) {
                try {
                    arrayList.add(new Day(Integer.parseInt(split[0]), Long.parseLong(split[1])));
                } catch (NumberFormatException e) {
                }
            }
        }
        DataCache.putSalaryHistory(arrayList);
        return new ArrayList(arrayList);
    }

    private static void save(Context context, List<Day> list) {
        DataCache.invalidate();
        int i = 0;
        while (i < list.size()) {
            int i2 = i + 1;
            for (int i3 = i2; i3 < list.size(); i3++) {
                if (list.get(i3).stamp < list.get(i).stamp) {
                    Day day = list.get(i);
                    list.set(i, list.get(i3));
                    list.set(i3, day);
                }
            }
            i = i2;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(6, -1500);
        int stamp = stamp(calendar);
        StringBuilder sb = new StringBuilder();
        for (Day day2 : list) {
            if (day2.stamp >= stamp) {
                if (sb.length() > 0) {
                    sb.append(';');
                }
                sb.append(day2.stamp).append(':').append(day2.cents);
            }
        }
        context.getSharedPreferences("salary_jump", 0).edit().putString(KEY, sb.toString()).apply();
    }

    static List<Day> byMonth(Context context, int i, int i2) {
        ArrayList arrayList = new ArrayList();
        for (Day day : load(context)) {
            if (day.stamp / 10000 == i && (day.stamp / 100) % 100 == i2) {
                arrayList.add(day);
            }
        }
        ArrayList arrayList2 = new ArrayList();
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            arrayList2.add((Day) arrayList.get(size));
        }
        return arrayList2;
    }

    static long monthTotalCents(Context context, int i, int i2) {
        Iterator<Day> it = byMonth(context, i, i2).iterator();
        long j = 0;
        while (it.hasNext()) {
            j += it.next().cents;
        }
        return j;
    }

    static List<Integer> months(Context context) {
        ArrayList arrayList = new ArrayList();
        Iterator<Day> it = load(context).iterator();
        while (it.hasNext()) {
            int i = it.next().stamp / 100;
            if (!arrayList.contains(Integer.valueOf(i))) {
                arrayList.add(Integer.valueOf(i));
            }
        }
        int i2 = 0;
        while (i2 < arrayList.size()) {
            int i3 = i2 + 1;
            for (int i4 = i3; i4 < arrayList.size(); i4++) {
                if (((Integer) arrayList.get(i4)).intValue() > ((Integer) arrayList.get(i2)).intValue()) {
                    Integer num = (Integer) arrayList.get(i2);
                    arrayList.set(i2, (Integer) arrayList.get(i4));
                    arrayList.set(i4, num);
                }
            }
            i2 = i3;
        }
        if (arrayList.size() > 24) {
            return arrayList.subList(0, 24);
        }
        return arrayList;
    }

    static long totalCents(Context context) {
        Iterator<Day> it = load(context).iterator();
        long j = 0;
        while (it.hasNext()) {
            j += it.next().cents;
        }
        return j;
    }

    static int stamp(Calendar calendar) {
        return (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
    }

    static int yearOf(int i) {
        return i / 10000;
    }

    static int monthOf(int i) {
        return (i / 100) % 100;
    }

    static int dayOf(int i) {
        return i % 100;
    }
}
