package com.salaryjump.app;

import android.content.Context;
import android.content.SharedPreferences;

/* loaded from: classes.dex */
public class WaterReminderSettings {
    public static final int MODE_BOTH = 2;
    public static final int MODE_SOUND = 0;
    public static final int MODE_VIBRATE = 1;
    public boolean enabled = true;
    public int intervalMinutes = 90;
    public int targetMl = 2000;
    public int cupMl = 250;
    public int reminderMode = 2;
    public int windowMode = 0;
    public int winStartMinutes = 480;
    public int winEndMinutes = 1200;
    public boolean skipBreak = true;
    public boolean weekend = false;

    /** 三个提醒模式，顺序与设置界面的单选按钮一致 */
    static final int[] MODES = {MODE_SOUND, MODE_VIBRATE, MODE_BOTH};

    /**
     * 把设置界面单选按钮的下标映射成提醒模式（纯函数，便于回归测试）。
     * 下标越界（没选中）时回落到 MODE_BOTH。
     */
    public static int modeFor(int i) {
        return (i >= 0 && i < MODES.length) ? MODES[i] : MODE_BOTH;
    }

    public WaterReminderSettings copy() {
        WaterReminderSettings waterReminderSettings = new WaterReminderSettings();
        waterReminderSettings.enabled = this.enabled;
        waterReminderSettings.intervalMinutes = this.intervalMinutes;
        waterReminderSettings.targetMl = this.targetMl;
        waterReminderSettings.cupMl = this.cupMl;
        waterReminderSettings.reminderMode = this.reminderMode;
        waterReminderSettings.windowMode = this.windowMode;
        waterReminderSettings.winStartMinutes = this.winStartMinutes;
        waterReminderSettings.winEndMinutes = this.winEndMinutes;
        waterReminderSettings.skipBreak = this.skipBreak;
        waterReminderSettings.weekend = this.weekend;
        return waterReminderSettings;
    }

    public static WaterReminderSettings load(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        WaterReminderSettings waterReminderSettings = new WaterReminderSettings();
        waterReminderSettings.enabled = sharedPreferences.getBoolean("water_enabled", waterReminderSettings.enabled);
        waterReminderSettings.intervalMinutes = sharedPreferences.getInt("water_interval", waterReminderSettings.intervalMinutes);
        waterReminderSettings.targetMl = sharedPreferences.getInt("water_target", waterReminderSettings.targetMl);
        waterReminderSettings.cupMl = sharedPreferences.getInt("water_cup", waterReminderSettings.cupMl);
        waterReminderSettings.reminderMode = sharedPreferences.getInt("water_mode", waterReminderSettings.reminderMode);
        waterReminderSettings.windowMode = sharedPreferences.getInt("water_window_mode", waterReminderSettings.windowMode);
        waterReminderSettings.winStartMinutes = sharedPreferences.getInt("water_win_start", waterReminderSettings.winStartMinutes);
        waterReminderSettings.winEndMinutes = sharedPreferences.getInt("water_win_end", waterReminderSettings.winEndMinutes);
        waterReminderSettings.skipBreak = sharedPreferences.getBoolean("water_skip_break", waterReminderSettings.skipBreak);
        waterReminderSettings.weekend = sharedPreferences.getBoolean("water_weekend", waterReminderSettings.weekend);
        return waterReminderSettings;
    }

    public void save(Context context) {
        context.getSharedPreferences("salary_jump", 0).edit().putBoolean("water_enabled", this.enabled).putInt("water_interval", this.intervalMinutes).putInt("water_target", this.targetMl).putInt("water_cup", this.cupMl).putInt("water_mode", this.reminderMode).putInt("water_window_mode", this.windowMode).putInt("water_win_start", this.winStartMinutes).putInt("water_win_end", this.winEndMinutes).putBoolean("water_skip_break", this.skipBreak).putBoolean("water_weekend", this.weekend).apply();
    }
}
