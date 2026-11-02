package com.salaryjump.app;

/**
 * 上班时间计算 —— 纯函数。
 *
 * <p>抽出来的原因：半天假那半天要不要算钱，取决于「现在上到第几小时」，
 * 而这套时间数学原来只在 {@code calculateBase} 里内联写了一遍。
 * 半天假的分支需要同样的算法，复制一份迟早会不一致，所以统一放这里。
 */
final class WorkClock {
    private WorkClock() {
    }

    /**
     * 到此刻为止「已工作」的秒数（已扣掉午休，不超过总工时）。
     *
     * <p>注意：午休期间返回的是午休开始前的秒数，不会继续累加。
     *
     * @param secondsOfDay    当天已过的秒数（时×3600 + 分×60 + 秒）
     * @param startMinutes    上班时间（分钟，8:00 → 480）
     * @param endMinutes      下班时间（分钟）
     * @param breakStartMins  午休开始（分钟）
     * @param breakEndMins    午休结束（分钟）
     * @param totalSeconds    全天的工作总秒数（不含午休）
     */
    static double elapsedSeconds(int secondsOfDay, int startMinutes, int endMinutes,
                                 int breakStartMins, int breakEndMins, double totalSeconds) {
        double now = secondsOfDay;
        double start = startMinutes * 60.0d;
        double end = endMinutes * 60.0d;
        double breakStart = breakStartMins * 60.0d;
        double breakEnd = breakEndMins * 60.0d;
        if (now < start) {
            return 0.0d;
        }
        if (now < breakStart) {
            return now - start;
        }
        if (now < breakEnd) {
            return breakStart - start;
        }
        if (now < end) {
            return (now - breakEnd) + (breakStart - start);
        }
        return Math.max(0.0d, totalSeconds);
    }

    /** 已工作比例（0~1） */
    static double elapsedRatio(int secondsOfDay, int startMinutes, int endMinutes,
                               int breakStartMins, int breakEndMins, double totalSeconds) {
        if (totalSeconds <= 0.0d) {
            return 0.0d;
        }
        double elapsed = elapsedSeconds(secondsOfDay, startMinutes, endMinutes,
                breakStartMins, breakEndMins, totalSeconds);
        return Math.max(0.0d, Math.min(1.0d, elapsed / totalSeconds));
    }

    /**
     * 现在是不是「正在上班的时段」（午休不算）。
     *
     * <p>用于半天假：请假那半天不该显示费率，上班那半天该显示。
     */
    static boolean inWorkWindow(int secondsOfDay, int startMinutes, int endMinutes,
                                int breakStartMins, int breakEndMins) {
        double now = secondsOfDay;
        double start = startMinutes * 60.0d;
        double end = endMinutes * 60.0d;
        double breakStart = breakStartMins * 60.0d;
        double breakEnd = breakEndMins * 60.0d;
        if (now < start || now >= end) {
            return false;
        }
        return !(now >= breakStart && now < breakEnd);
    }

    /** 从 Calendar 取「当天已过的秒数」 */
    static int secondsOfDay(java.util.Calendar calendar) {
        if (calendar == null) {
            return 0;
        }
        return (calendar.get(11) * 3600) + (calendar.get(12) * 60) + calendar.get(13);
    }
}
