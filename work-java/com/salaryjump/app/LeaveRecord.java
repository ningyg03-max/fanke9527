package com.salaryjump.app;

import android.content.Context;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public final class LeaveRecord {
    private static final int KEEP_DAYS = 400;
    private static final String KEY = "leave_records";
    public static final String[] TYPE_NAMES = {"年假", "育儿假", "长期贡献奖假", "调休", "婚假", "病假", "事假"};
    public static final int[] TYPE_PAY_PERCENT = {100, 100, 100, 100, 100, 60, 0};

    private LeaveRecord() {
    }

    public static class Entry {
        public boolean half;
        public String name;
        public int payPercent;
        public int stamp;

        Entry(int i, int i2, boolean z, String str) {
            this.stamp = i;
            this.payPercent = i2;
            this.half = z;
            this.name = str;
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
                    int parseInt = Integer.parseInt(split[0]);
                    boolean z = true;
                    int parseInt2 = Integer.parseInt(split[1]);
                    if (split.length < 3 || !"1".equals(split[2])) {
                        z = false;
                    }
                    arrayList.add(new Entry(parseInt, clampPercent(parseInt2), z, (split.length < 4 || split[3].length() <= 0) ? "请假" : split[3]));
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
                sb.append(entry2.stamp).append(',').append(entry2.payPercent).append(',').append(entry2.half ? 1 : 0).append(',').append(cleanName(entry2.name));
            }
        }
        context.getSharedPreferences("salary_jump", 0).edit().putString(KEY, sb.toString()).apply();
    }

    static void put(Context context, int i, String str, int i2, boolean z) {
        boolean z2;
        if (context == null) {
            return;
        }
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
                next.name = cleanName(str);
                next.payPercent = clampPercent(i2);
                next.half = z;
                z2 = true;
                break;
            }
        }
        if (!z2) {
            load.add(new Entry(i, clampPercent(i2), z, cleanName(str)));
        }
        save(context, load);
        ArchiveSync.adjust(context, i, Math.round((ArchiveSync.contribution(context, i) - contribution) * 100.0d));
        ArchiveSync.syncEventRow(context, i);
    }

    static void remove(Context context, int i) {
        if (context == null) {
            return;
        }
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
        return get(context, i) != null;
    }

    static Entry get(Context context, int i) {
        for (Entry entry : load(context)) {
            if (entry.stamp == i) {
                return entry;
            }
        }
        return null;
    }

    public static double ratio(int i, boolean z) {
        double clampPercent = clampPercent(i) / 100.0d;
        if (z) {
            clampPercent *= 0.5d;
        }
        if (clampPercent < 0.0d) {
            return 0.0d;
        }
        if (clampPercent > 1.0d) {
            return 1.0d;
        }
        return clampPercent;
    }

    public static double earned(double d, int i, boolean z) {
        if (d <= 0.0d) {
            return 0.0d;
        }
        return d * ratio(i, z);
    }

    public static String payText(int i) {
        int clampPercent = clampPercent(i);
        if (clampPercent >= 100) {
            return "全薪";
        }
        if (clampPercent <= 0) {
            return "无薪";
        }
        return clampPercent + "%";
    }

    public static String describe(String str, int i, boolean z) {
        StringBuilder sb = new StringBuilder();
        if (str == null || str.length() == 0) {
            str = "请假";
        }
        sb.append(str);
        sb.append(" · ").append(payText(i));
        if (z) {
            sb.append(" · 半天");
        }
        return sb.toString();
    }

    public static int presetIndex(String str) {
        if (str == null) {
            return -1;
        }
        for (int i = 0; i < TYPE_NAMES.length; i++) {
            if (TYPE_NAMES[i].equals(str)) {
                return i;
            }
        }
        return -1;
    }

    private static int clampPercent(int i) {
        if (i < 0) {
            return 0;
        }
        if (i > 100) {
            return 100;
        }
        return i;
    }

    private static String cleanName(String str) {
        if (str == null) {
            return "请假";
        }
        String trim = str.trim();
        if (trim.length() == 0) {
            return "请假";
        }
        if (trim.length() > 8) {
            trim = trim.substring(0, 8);
        }
        return trim.replace(',', ' ').replace(';', ' ').replace(':', ' ');
    }
}
