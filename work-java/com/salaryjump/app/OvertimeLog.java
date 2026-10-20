package com.salaryjump.app;

import android.content.Context;
import android.content.SharedPreferences;

/* loaded from: classes.dex */
public final class OvertimeLog {
    private static final String KEY_DAY = "ot_day";
    private static final String KEY_END = "ot_end";
    private static final String KEY_START = "ot_start";

    private OvertimeLog() {
    }

    public static class Punch {
        public int day;
        public long end;
        public long start;

        public Punch(int i, long j, long j2) {
            this.day = i;
            this.start = j;
            this.end = j2;
        }

        public boolean running() {
            return this.start > 0 && this.end <= 0;
        }

        public boolean exists() {
            return this.start > 0;
        }
    }

    static Punch load(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        return new Punch(sharedPreferences.getInt(KEY_DAY, 0), sharedPreferences.getLong(KEY_START, 0L), sharedPreferences.getLong(KEY_END, 0L));
    }

    static void start(Context context, int i, long j) {
        context.getSharedPreferences("salary_jump", 0).edit().putInt(KEY_DAY, i).putLong(KEY_START, j).putLong(KEY_END, 0L).apply();
    }

    static void stop(Context context, long j) {
        context.getSharedPreferences("salary_jump", 0).edit().putLong(KEY_END, j).apply();
    }

    static void clear(Context context) {
        context.getSharedPreferences("salary_jump", 0).edit().remove(KEY_DAY).remove(KEY_START).remove(KEY_END).apply();
    }

    static boolean hasPunch(Context context, int i) {
        return load(context).day == i && load(context).exists();
    }

    static boolean isRunning(Context context, int i) {
        Punch load = load(context);
        return load.day == i && load.running();
    }

    public static double seconds(int i, long j, long j2, int i2, long j3) {
        if (i != i2 || j <= 0) {
            return 0.0d;
        }
        if (j2 <= 0) {
            j2 = j3;
        }
        if (j2 <= j) {
            return 0.0d;
        }
        return (j2 - j) / 1000.0d;
    }

    static double secondsOfDay(Context context, int i, long j) {
        Punch load = load(context);
        return seconds(load.day, load.start, load.end, i, j);
    }

    public static String format(double d) {
        int i = (int) d;
        int i2 = i / 3600;
        int i3 = (i % 3600) / 60;
        int i4 = i % 60;
        if (i2 > 0) {
            return String.format("%d:%02d:%02d", Integer.valueOf(i2), Integer.valueOf(i3), Integer.valueOf(i4));
        }
        return String.format("%d:%02d", Integer.valueOf(i3), Integer.valueOf(i4));
    }
}
