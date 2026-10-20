package com.salaryjump.app;

import android.content.Context;

/**
 * 年度统计里需要跨天聚合的指标。
 *
 * <h3>加班次数为什么不能数打卡记录</h3>
 * {@code OvertimeLog} 只存**最后一次**打卡（`ot_day` / `ot_start` / `ot_end` 三个 key），
 * 换一天再打卡就把上一条覆盖了 —— 历史加班记录根本没留下来。
 *
 * <p>所以加班要从**每日金额**反推：
 * <pre>
 *   加班费 = 当天入账 − ArchiveSync.expectedCents(当天)
 *   加班费 &gt; 0  ⟺  那天有加班
 * </pre>
 * 这个等式在 v3.68 做日历拆分时已验证过（10/14: 1789.37 − 1007.78 = 781.59），
 * 并且带「只有本该上班的日子才可能加班」的守卫（见 {@link DayBreakdown}）。
 */
final class YearlyStats {
    private YearlyStats() {
    }

    /**
     * 统计某一年的加班。
     *
     * @return 长度为 2 的数组：{@code [0] = 加班次数，[1] = 加班费总额（分）}
     */
    static long[] overtimeSummary(Context context, int year) {
        long count = 0L;
        long total = 0L;
        if (context == null) {
            return new long[]{0L, 0L};
        }
        for (SalaryHistory.Day day : SalaryHistory.load(context)) {
            if (SalaryHistory.yearOf(day.stamp) != year) {
                continue;
            }
            DayBreakdown breakdown = DayBreakdown.of(context, day.stamp);
            if (breakdown.hasOvertime()) {
                count++;
                total += breakdown.overtimeCents;
            }
        }
        return new long[]{count, total};
    }
}
