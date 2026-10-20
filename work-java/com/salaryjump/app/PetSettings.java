package com.salaryjump.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 打工小人的显示设置（显示与否 / 尺寸档位 / 是否画工位场景）。
 *
 * 三项都存进既有的 salary_jump 偏好里，和其它设置同一处，备份/恢复自动带上。
 */
public final class PetSettings {
    /** 尺寸档位 */
    static final int SIZE_SMALL = 0;
    static final int SIZE_MEDIUM = 1;
    static final int SIZE_LARGE = 2;

    /** 三档尺寸的宽度 dp：小 / 中 / 大 */
    static final int[] SIZES_DP = {88, 104, 120};

    /** 高度 = 宽度 × 此比例（沿用原始 88×92 的观感） */
    private static final float HEIGHT_RATIO = 92.0f / 88.0f;

    private PetSettings() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("salary_jump", 0);
    }

    /* ─────────── 显示小人 ─────────── */

    static boolean visible(Context context) {
        return prefs(context).getBoolean("pet_visible", true);
    }

    static void setVisible(Context context, boolean z) {
        prefs(context).edit().putBoolean("pet_visible", z).apply();
    }

    /* ─────────── 尺寸档位 ─────────── */

    static int size(Context context) {
        int i = prefs(context).getInt("pet_size", SIZE_MEDIUM);
        return (i < SIZE_SMALL || i > SIZE_LARGE) ? SIZE_MEDIUM : i;
    }

    static void setSize(Context context, int i) {
        if (i < SIZE_SMALL) {
            i = SIZE_SMALL;
        } else if (i > SIZE_LARGE) {
            i = SIZE_LARGE;
        }
        prefs(context).edit().putInt("pet_size", i).apply();
    }

    /** 当前档位的宽度（dp） */
    static int widthDp(Context context) {
        return SIZES_DP[size(context)];
    }

    /** 当前档位的高度（dp） */
    static int heightDp(Context context) {
        return Math.round(SIZES_DP[size(context)] * HEIGHT_RATIO);
    }

    /** 档位名称（用于设置界面） */
    static String sizeName(int i) {
        if (i == SIZE_SMALL) {
            return "小";
        }
        if (i == SIZE_LARGE) {
            return "大";
        }
        return "中";
    }

    /* ─────────── 工位场景 ─────────── */

    static boolean scene(Context context) {
        return prefs(context).getBoolean("pet_scene", true);
    }

    static void setScene(Context context, boolean z) {
        prefs(context).edit().putBoolean("pet_scene", z).apply();
    }
}
