package com.salaryjump.app;

/* loaded from: classes.dex */
public final class OvertimeCalc {
    private OvertimeCalc() {
    }

    public static double seconds(double d, int i, int i2) {
        double d2 = (i * 60.0d) + (i2 * 60.0d);
        if (d > d2) {
            return d - d2;
        }
        return 0.0d;
    }

    public static double earned(double d, double d2, double d3, int i) {
        if (d <= 0.0d || d2 <= 0.0d || d3 <= 0.0d || i <= 0) {
            return 0.0d;
        }
        return d3 * (d / d2) * (i / 10.0d);
    }

    public static boolean isOvertime(double d, int i, int i2) {
        return seconds(d, i, i2) > 0.0d;
    }
}
