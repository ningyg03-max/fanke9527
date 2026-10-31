package com.salaryjump.app;

import com.salaryjump.app.WaterReminderScheduler;
import java.util.Calendar;
import java.util.List;

/**
 * 归档每天的小时分布。
 *
 * <p>为什么单独存：{@code water_history} 的格式是「日期:类型:总量:杯数」，
 * <b>没有时间</b>。到了第二天清零，{@code water_records} 里的时间戳就没了 ——
 * 于是「几点喝、空档多长」这类分析永远做不了。
 *
 * <p>这里在日切归档的那一刻把小时桶算好存下来。
 * 只有在<b>日切时</b>才写，所以同一天不会重复追加。
 */
final class WaterHourLog {
    static final String KEY = "water_hours";
    private static final int KEEP_DAYS = 365;
    /** 一天 24 个小时桶 */
    static final int HOURS = 24;

    private WaterHourLog() {
    }

    /** 把一天的记录折叠成 24 个桶（每桶 = 该小时喝的总 ml） */
    static int[] buckets(List<WaterReminderScheduler.WaterRecord> records) {
        int[] out = new int[HOURS];
        if (records == null) {
            return out;
        }
        for (WaterReminderScheduler.WaterRecord record : records) {
            if (record == null || record.amountMl <= 0) {
                continue;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(record.timestamp);
            int hour = calendar.get(11);
            if (hour >= 0 && hour < HOURS) {
                out[hour] = out[hour] + record.amountMl;
            }
        }
        return out;
    }

    /** 只在至少有一个桶非 0 时才写，避免存一堆全 0 的日子 */
    static void archive(android.content.SharedPreferences prefs, int dayStamp, int[] buckets) {
        if (prefs == null || dayStamp == 0 || buckets == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int hour = 0; hour < HOURS && hour < buckets.length; hour++) {
            if (buckets[hour] > 0) {
                if (sb.length() > 0) {
                    sb.append('|');
                }
                sb.append(hour).append('~').append(buckets[hour]);
            }
        }
        if (sb.length() == 0) {
            return;
        }
        // 同一天已经有记录就替换，不要重复追加
        StringBuilder out = new StringBuilder();
        String existing = prefs.getString(KEY, "");
        if (existing != null && existing.length() > 0) {
            for (String item : existing.split(";")) {
                if (item.length() > 0 && !item.startsWith(dayStamp + ":")) {
                    if (out.length() > 0) {
                        out.append(';');
                    }
                    out.append(item);
                }
            }
        }
        if (out.length() > 0) {
            out.append(';');
        }
        out.append(dayStamp).append(':').append(sb);
        prefs.edit().putString(KEY, trim(out.toString())).apply();
    }

    private static String trim(String raw) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(6, -KEEP_DAYS);
        int threshold = (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
        StringBuilder sb = new StringBuilder();
        for (String item : raw.split(";")) {
            String[] split = item.split(":");
            if (split.length != 2) {
                continue;
            }
            try {
                if (Integer.parseInt(split[0]) < threshold) {
                    continue;
                }
            } catch (NumberFormatException e) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(item);
        }
        return sb.toString();
    }

    /**
     * 读取某个时间段的小时桶（跨多天累加）。
     *
     * @param from 起始日期戳（含）
     * @param to   结束日期戳（含）
     */
    static int[] range(android.content.SharedPreferences prefs, int from, int to) {
        int[] out = new int[HOURS];
        if (prefs == null) {
            return out;
        }
        String raw = prefs.getString(KEY, "");
        if (raw == null || raw.length() == 0) {
            return out;
        }
        for (String item : raw.split(";")) {
            String[] split = item.split(":");
            if (split.length != 2) {
                continue;
            }
            int day;
            try {
                day = Integer.parseInt(split[0]);
            } catch (NumberFormatException e) {
                continue;
            }
            if (day < from || day > to) {
                continue;
            }
            // 每个小时桶单独 try —— 一个坏桶不能把这一天的好数据一起丢掉
            for (String pair : split[1].split("\\|")) {
                String[] kv = pair.split("~");
                if (kv.length != 2) {
                    continue;
                }
                try {
                    int hour = Integer.parseInt(kv[0]);
                    int ml = Integer.parseInt(kv[1]);
                    if (hour >= 0 && hour < HOURS && ml > 0) {
                        out[hour] = out[hour] + ml;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return out;
    }

    /**
     * 逐天返回小时桶（每天一个 24 长度数组）。
     *
     * <p><b>不要用 {@link #range} 算空档</b> —— 累加会把「某天某点喝过」填进别的天的空档里，
     * 算出来的是「这段时间里有没有在这个钟点喝过」，不是「典型的一天断多久」。
     */
    static int[][] days(android.content.SharedPreferences prefs, int from, int to) {
        return days(prefs, from, to, false);
    }

    /**
     * @param trackedOnly true = 只要需要维护的日子（跳过周末），
     *                    否则周末的全天空档会被当成「一天没喝」
     */
    static int[][] days(android.content.SharedPreferences prefs, int from, int to, boolean trackedOnly) {
        if (prefs == null) {
            return new int[0][];
        }
        String raw = prefs.getString(KEY, "");
        if (raw == null || raw.length() == 0) {
            return new int[0][];
        }
        java.util.ArrayList<int[]> list = new java.util.ArrayList<>();
        for (String item : raw.split(";")) {
            String[] split = item.split(":");
            if (split.length != 2) {
                continue;
            }
            int day;
            try {
                day = Integer.parseInt(split[0]);
            } catch (NumberFormatException e) {
                continue;
            }
            if (day < from || day > to) {
                continue;
            }
            if (trackedOnly && !WaterScope.isTracked(day)) {
                continue;
            }
            int[] buckets = new int[HOURS];
            for (String pair : split[1].split("\\|")) {
                String[] kv = pair.split("~");
                if (kv.length != 2) {
                    continue;
                }
                try {
                    int hour = Integer.parseInt(kv[0]);
                    int ml = Integer.parseInt(kv[1]);
                    if (hour >= 0 && hour < HOURS && ml > 0) {
                        buckets[hour] = buckets[hour] + ml;
                    }
                } catch (NumberFormatException e) {
                }
            }
            list.add(buckets);
        }
        return list.toArray(new int[list.size()][]);
    }

    /** 某段时间内有几天的时间数据 */
    static int dayCountInRange(android.content.SharedPreferences prefs, int from, int to) {
        if (prefs == null) {
            return 0;
        }
        String raw = prefs.getString(KEY, "");
        if (raw == null || raw.length() == 0) {
            return 0;
        }
        int count = 0;
        for (String item : raw.split(";")) {
            String[] split = item.split(":");
            if (split.length != 2) {
                continue;
            }
            try {
                int day = Integer.parseInt(split[0]);
                if (day >= from && day <= to) {
                    count++;
                }
            } catch (NumberFormatException e) {
            }
        }
        return count;
    }

    /** 有几天的数据（用来判断「时间分布」这类分析可不可信） */
    static int dayCount(android.content.SharedPreferences prefs) {
        if (prefs == null) {
            return 0;
        }
        String raw = prefs.getString(KEY, "");
        if (raw == null || raw.length() == 0) {
            return 0;
        }
        int count = 0;
        for (String item : raw.split(";")) {
            if (item.split(":").length == 2) {
                count++;
            }
        }
        return count;
    }
}
