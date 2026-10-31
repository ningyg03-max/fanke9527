package com.salaryjump.app;

import java.util.Calendar;
import java.util.HashMap;

/**
 * 统计范围 —— 只算「会维护数据」的日子。
 *
 * <p>用户明确说<b>周末不维护</b>。如果把周末的空缺算进统计，
 * 达标率和平均值都会被无端拉低，趋势也会出现假的下跌 ——
 * 那不是「喝得少了」，是「那天根本没记」。
 *
 * <p>所以统计只取周一至周五。这个判断集中在这里，
 * 图表、汇总、分析卡都用同一套口径，避免各处各算一套。
 */
final class WaterScope {
    private WaterScope() {
    }

    static int stamp(Calendar calendar) {
        return (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
    }

    /** 这一天要不要计入统计（周六周日不算） */
    static boolean isTracked(int dayStamp) {
        if (dayStamp <= 0) {
            return true;
        }
        Calendar day = Calendar.getInstance();
        day.set(dayStamp / 10000, ((dayStamp / 100) % 100) - 1, dayStamp % 100, 12, 0, 0);
        day.set(14, 0);
        int dow = day.get(7);
        return dow != Calendar.SATURDAY && dow != Calendar.SUNDAY;
    }

    /** 一段天数里有几天需要统计（比如近 7 天一般是 5 天） */
    static int trackedDays(int days, Calendar today) {
        int n = Math.max(1, days);
        if (today == null) {
            return n;
        }
        Calendar cursor = (Calendar) today.clone();
        cursor.add(5, -(n - 1));
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (isTracked(stamp(cursor))) {
                count++;
            }
            cursor.add(5, 1);
        }
        return count;
    }

    /**
     * 把稀疏表展开成「只含工作日」的定长数组。
     *
     * <p>给分析卡用 —— 它内部把 0 当作「没记录」来计算波动和趋势，
     * 如果混进周末的 0，结论就会失真。直接不给它周末那几天最省事。
     */
    static int[] trackedSeries(HashMap<Integer, Integer> totals, int days, Calendar today) {
        int n = Math.max(1, days);
        if (today == null) {
            // 没有「今天」就无法判断哪天是周末，只能原样返回
            return WaterDailyChart.series(totals, n, null);
        }
        int[] all = WaterDailyChart.series(totals, n, today);
        int count = 0;
        Calendar cursor = (Calendar) today.clone();
        cursor.add(5, -(n - 1));
        boolean[] keep = new boolean[n];
        for (int i = 0; i < n; i++) {
            keep[i] = isTracked(stamp(cursor));
            if (keep[i]) {
                count++;
            }
            cursor.add(5, 1);
        }
        int[] out = new int[count];
        int k = 0;
        for (int i = 0; i < n; i++) {
            if (keep[i]) {
                out[k++] = all[i];
            }
        }
        return out;
    }

    /** 每个柱子要不要计入统计（图表用它把周末画成「不计」的样子） */
    static boolean[] mask(int days, Calendar today) {
        int n = Math.max(1, days);
        boolean[] out = new boolean[n];
        if (today == null) {
            for (int i = 0; i < n; i++) {
                out[i] = true;
            }
            return out;
        }
        Calendar cursor = (Calendar) today.clone();
        cursor.add(5, -(n - 1));
        for (int i = 0; i < n; i++) {
            out[i] = isTracked(stamp(cursor));
            cursor.add(5, 1);
        }
        return out;
    }
}
