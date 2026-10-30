package com.salaryjump.app;

import java.util.Calendar;
import java.util.HashMap;

/**
 * 「每日总量」柱状图的纯计算逻辑。
 *
 * <p>抽出来是为了能单测 —— 这些规则（哪天有柱、柱多高、哪格标日期）
 * 一旦写错，界面上只会表现为「看起来怪怪的」，很难靠眼睛发现。
 */
final class WaterDailyChart {
    /** 30 天太窄，只每 5 天标一个日期 */
    static final int LABEL_EVERY = 5;

    private WaterDailyChart() {
    }

    static int stamp(Calendar calendar) {
        return (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
    }

    /**
     * 把「日期 → 当天总量」的稀疏表展开成按时间正序的定长数组。
     * 没记录的日子放 0（不是跳过），这样柱子和日期永远对得上。
     */
    static int[] series(HashMap<Integer, Integer> totals, int days, Calendar today) {
        int n = Math.max(1, days);
        int[] out = new int[n];
        if (totals == null || today == null) {
            return out;
        }
        Calendar cursor = (Calendar) today.clone();
        cursor.add(5, -(n - 1));
        for (int i = 0; i < n; i++) {
            Integer value = totals.get(Integer.valueOf(stamp(cursor)));
            out[i] = value == null ? 0 : value.intValue();
            cursor.add(5, 1);
        }
        return out;
    }

    /** 纵轴上限：最矮要能盖住目标线，最高要盖住最高的那根柱 */
    static int axisMax(int[] data, int targetMl) {
        int max = Math.max(1, targetMl);
        if (data != null) {
            for (int value : data) {
                if (value > max) {
                    max = value;
                }
            }
        }
        return Math.round(max * 1.08f) + 1;
    }

    /**
     * 柱高（像素）。
     *
     * <p>0 值不返回 0 —— 留一个很矮的墩子，表示「这天没记录」，
     * 和「这格压根没画」是两件事。
     */
    static int barPx(int value, int axisMax, int plotPx, int zeroPx) {
        if (value <= 0) {
            return zeroPx;
        }
        int height = Math.round(plotPx * (value / (float) Math.max(1, axisMax)));
        return Math.max(zeroPx * 3, height);
    }

    /** 这个格子要不要标日期（7 天全标，30 天每 5 天标一个） */
    static boolean showLabel(int index, int days) {
        if (days <= 7) {
            return true;
        }
        return index % LABEL_EVERY == 0 || index == days - 1;
    }

    /** 平均每天（按范围天数算，没喝的日子算 0） */
    static int average(int[] data) {
        if (data == null || data.length == 0) {
            return 0;
        }
        int sum = 0;
        for (int value : data) {
            sum += value;
        }
        return Math.round(sum / (float) data.length);
    }

    static int maxValue(int[] data) {
        int max = 0;
        if (data != null) {
            for (int value : data) {
                if (value > max) {
                    max = value;
                }
            }
        }
        return max;
    }

    /**
     * 横轴文字。
     *
     * @param shortForm true = 只写「日」（30 天格子太窄，写 "10/15" 放不下）
     */
    static String[] labels(Calendar today, int days, boolean shortForm) {
        int n = Math.max(1, days);
        String[] out = new String[n];
        Calendar cursor = (Calendar) today.clone();
        cursor.add(5, -(n - 1));
        for (int i = 0; i < n; i++) {
            out[i] = shortForm
                    ? String.valueOf(cursor.get(5))
                    : (cursor.get(2) + 1) + "/" + cursor.get(5);
            cursor.add(5, 1);
        }
        return out;
    }
}
