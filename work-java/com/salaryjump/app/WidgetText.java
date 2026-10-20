package com.salaryjump.app;

/* loaded from: classes.dex */
final class WidgetText {
    private WidgetText() {
    }

    static String restText(String str, String str2) {
        if (str == null || str.length() == 0) {
            str = "休息日";
        }
        return (str2 == null || str2.length() <= 0) ? "今天是" + str : "今天是" + str + " · 外快 ¥" + str2;
    }

    static String holidayWorkText(String str, boolean z, int i) {
        if (str == null || str.length() == 0) {
            str = "休息日";
        }
        if (z) {
            return str + "出勤 · 调休+1";
        }
        return str + "出勤 · " + HolidayWork.rateText(i);
    }

    static String leaveText(String str) {
        if (str == null || str.length() == 0) {
            return "今天请假";
        }
        return "请假 · " + str;
    }
}
