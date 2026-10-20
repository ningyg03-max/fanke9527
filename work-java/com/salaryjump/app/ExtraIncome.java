package com.salaryjump.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public final class ExtraIncome {
    private static final int KEEP_DAYS = 400;
    private static final String KEY = "extra_income";
    public static final int REMARK_MAX = 12;

    private ExtraIncome() {
    }

    public static class Entry {
        public long cents;
        public String remark;
        public int stamp;

        Entry(int i, long j, String str) {
            this.stamp = i;
            this.cents = j;
            this.remark = str;
        }
    }

    static List<Entry> load(Context context) {
        ArrayList arrayList = new ArrayList();
        if (context == null) {
            return arrayList;
        }
        String string = prefs(context).getString(KEY, "");
        if (string == null || string.length() == 0) {
            return arrayList;
        }
        for (String str : string.split(";")) {
            String[] split = str.split(",");
            if (split.length >= 2) {
                try {
                    arrayList.add(new Entry(Integer.parseInt(split[0]), Long.parseLong(split[1]), split.length >= 3 ? split[2] : ""));
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
                sb.append(entry2.stamp).append(',').append(entry2.cents).append(',').append(entry2.remark);
            }
        }
        prefs(context).edit().putString(KEY, sb.toString()).apply();
    }

    static void put(Context context, int i, long j, String str) {
        boolean z;
        if (context == null) {
            return;
        }
        List<Entry> load = load(context);
        Iterator<Entry> it = load.iterator();
        while (true) {
            if (!it.hasNext()) {
                z = false;
                break;
            }
            Entry next = it.next();
            if (next.stamp == i) {
                next.cents = j;
                next.remark = cleanRemark(str);
                z = true;
                break;
            }
        }
        if (!z) {
            load.add(new Entry(i, j, cleanRemark(str)));
        }
        save(context, load);
    }

    static void remove(Context context, int i) {
        if (context == null) {
            return;
        }
        List<Entry> load = load(context);
        for (int size = load.size() - 1; size >= 0; size--) {
            if (load.get(size).stamp == i) {
                load.remove(size);
            }
        }
        save(context, load);
    }

    static Entry get(Context context, int i) {
        for (Entry entry : load(context)) {
            if (entry.stamp == i) {
                return entry;
            }
        }
        return null;
    }

    static boolean has(Context context, int i) {
        return get(context, i) != null;
    }

    static long centsOf(Context context, int i) {
        Entry entry = get(context, i);
        if (entry == null) {
            return 0L;
        }
        return entry.cents;
    }

    static double dayTotal(Context context, int i) {
        return centsOf(context, i) / 100.0d;
    }

    static String remarkOf(Context context, int i) {
        Entry entry = get(context, i);
        return (entry == null || entry.remark == null) ? "" : entry.remark;
    }

    static void apply(Context context, int i, long j, String str) {
        if (context == null) {
            return;
        }
        long centsOf = centsOf(context, i);
        long max = Math.max(0L, j);
        if (max <= 0) {
            remove(context, i);
        } else {
            put(context, i, max, str);
        }
        long j2 = max - centsOf;
        if (j2 == 0) {
            return;
        }
        ArchiveSync.adjust(context, i, j2);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("salary_jump", 0);
    }

    public static String cleanRemark(String str) {
        if (str == null) {
            return "";
        }
        String trim = str.trim();
        if (trim.length() == 0) {
            return "";
        }
        String trim2 = trim.replace(',', ' ').replace(';', ' ').replace(':', ' ').replace('\n', ' ').replaceAll("\\s+", " ").trim();
        if (trim2.length() > 12) {
            return trim2.substring(0, 12);
        }
        return trim2;
    }
}
