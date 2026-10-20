package com.salaryjump.app;

import android.content.Context;
import java.util.Calendar;

/**
 * 某一天的金额拆分（日薪 / 加班费 / 外快 / 补录）。
 *
 * 抽成纯函数的原因：日历格子要在一格里塞下「日薪多少、加班费多少」，
 * 而这两个数在存储里是混在一起的一个总额（{@code SalaryHistory.centsOf}）。
 * 拆分规则必须准确，放这里才能被回归测试覆盖。
 *
 * 拆分依据：
 * <pre>
 *   expected = ArchiveSync.expectedCents(那天)   // 不含加班应得的金额
 *   expected = 外快 + 补录 + 日薪
 *   → 日薪     = expected − 外快 − 补录
 *   → 加班费   = 当日入账 − expected
 * </pre>
 */
final class DayBreakdown {
    /** 当天入账总额（分） */
    final long totalCents;
    /** 日薪部分（分）—— 含请假折算、法定倍率；调休出勤为 0 */
    final long baseCents;
    /** 加班费（分） */
    final long overtimeCents;
    final long extraCents;
    final long backfillCents;
    final int stamp;

    /** 当天在存储里有没有一行记录 */
    final boolean hasRecord;
    /** 当天是不是工作日（按内置节假日 + 调休判定） */
    final boolean isWorkday;
    /** 调休出勤（无薪，攒调休天数） */
    final boolean isComp;
    /** 法定/休息日出勤倍率 ×10；>0 表示出勤 */
    final int holidayRateX10;
    final String leaveName;
    final int leavePayPercent;
    final boolean leaveHalf;
    /** 加班时长（秒） */
    final double overtimeSeconds;
    /** 补班日标记：非 null 表示这天是「被安排上班的周末」（没有额外工资），值为补班原因 */
    final String makeupNote;

    private DayBreakdown(int stamp, long totalCents, long baseCents, long overtimeCents,
                         long extraCents, long backfillCents, boolean hasRecord, boolean isWorkday,
                         boolean isComp, int holidayRateX10, String leaveName,
                         int leavePayPercent, boolean leaveHalf, double overtimeSeconds,
                         String makeupNote) {
        this.stamp = stamp;
        this.totalCents = totalCents;
        this.baseCents = baseCents;
        this.overtimeCents = overtimeCents;
        this.extraCents = extraCents;
        this.backfillCents = backfillCents;
        this.hasRecord = hasRecord;
        this.isWorkday = isWorkday;
        this.isComp = isComp;
        this.holidayRateX10 = holidayRateX10;
        this.leaveName = leaveName;
        this.leavePayPercent = leavePayPercent;
        this.leaveHalf = leaveHalf;
        this.overtimeSeconds = overtimeSeconds;
        this.makeupNote = makeupNote;
    }

    static DayBreakdown of(Context context, int stamp) {
        boolean hasRecord = SalaryHistory.has(context, stamp);
        long total = hasRecord ? SalaryHistory.centsOf(context, stamp) : 0L;
        long expected = ArchiveSync.expectedCents(context, stamp);
        long extra = ExtraIncome.centsOf(context, stamp);
        long backfill = MonthBackfill.centsOn(context, stamp);

        // 工作日 / 调休出勤 / 出勤倍率 —— 金额拆分要用，所以先算
        boolean isWorkday = false;
        try {
            MainActivity.SalarySettings settings =
                    MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0));
            Calendar calendar = Calendar.getInstance();
            calendar.set(SalaryHistory.yearOf(stamp), SalaryHistory.monthOf(stamp) - 1,
                    SalaryHistory.dayOf(stamp));
            isWorkday = WorkdayHelper.isWorkday(context, settings, calendar);
        } catch (Throwable unused) {
        }
        boolean isComp = HolidayWork.isComp(context, stamp);
        int holidayRateX10 = HolidayWork.rateX10Of(context, stamp);
        String makeupNote = null;
        try {
            Calendar makeupCalendar = Calendar.getInstance();
            makeupCalendar.set(SalaryHistory.yearOf(stamp), SalaryHistory.monthOf(stamp) - 1,
                    SalaryHistory.dayOf(stamp));
            makeupNote = WorkdayHelper.makeupNote(context,
                    MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0)),
                    makeupCalendar);
        } catch (Throwable unused) {
        }

        long base;
        long overtime;
        if (!hasRecord) {
            // 没有记录的日子不拆金额。
            // 注意：expectedCents 对「工作日」永远会算出日薪 —— 那是「本该有」而不是
            // 「实际有」。直接拿来用会让空白的工作日显示成「日薪 300 / 加班 −300」。
            base = 0L;
            overtime = 0L;
            extra = 0L;
            backfill = 0L;
        } else {
            // 加班费 = 超出「应得」的部分；日薪 = 总额扣掉其余三项。
            // 这样定义能保证：日薪 + 加班 + 外快 + 补录 ≡ 当天入账（恒等式）。
            overtime = total - expected;
            if (overtime < 0L) {
                overtime = 0L;
            }
            // 守卫：只有「本该上班的日子」才可能产生加班。
            // 休息日的 expectedCents 会返回 0（因为那天没有应得工资），
            // 若不加这道判断，休息日上任何一笔非外快/补录的金额都会被误判成加班。
            if (!isWorkday && !isComp && holidayRateX10 <= 0) {
                overtime = 0L;
            }
            base = total - overtime - extra - backfill;
        }

        LeaveRecord.Entry entry = LeaveRecord.get(context, stamp);
        double otSeconds = 0.0d;
        try {
            otSeconds = OvertimeLog.secondsOfDay(context, stamp, System.currentTimeMillis());
        } catch (Throwable unused) {
        }

        return new DayBreakdown(stamp, total, base, overtime, extra, backfill,
                hasRecord, isWorkday, isComp, holidayRateX10,
                entry == null ? null : entry.name,
                entry == null ? 100 : entry.payPercent,
                entry != null && entry.half, otSeconds, makeupNote);
    }

    /** 那天有没有加班费 */
    boolean hasOvertime() {
        return overtimeCents > 0;
    }

    /** 那天有没有任何入账 */
    boolean hasMoney() {
        return totalCents > 0;
    }

    /**
     * 格子里的紧凑金额：`1007.78` → `"1008"`；`12345` → `"1.2万"`；0 → `""`。
     *
     * 格子只有约 44dp 宽，必须紧凑。
     */
    static String formatCompact(long cents) {
        long yuan = Math.round(cents / 100.0d);
        if (yuan <= 0) {
            return "";
        }
        if (yuan < 10000) {
            return String.valueOf(yuan);
        }
        long wan10 = Math.round(yuan / 1000.0d);   // 万元 × 10
        long whole = wan10 / 10;
        long frac = wan10 % 10;
        return frac == 0 ? whole + "万" : whole + "." + frac + "万";
    }

    /**
     * 格子里的外快文本：`外 200`。
     *
     * 带「外」字前缀是刻意的 —— 只靠颜色区分（蓝色）容易让人看成一笔来源不明的钱。
     * 为 0 时返回 null（该行不显示）。
     */
    static String extraCellText(long cents) {
        String compact = formatCompact(cents);
        return compact.length() == 0 ? null : "外 " + compact;
    }

    /**
     * 格子里的状态短标签（没有金额可显示时用）。
     * 返回 null 表示「什么都不显示」。
     */
    String cellTag() {
        if (isComp) {
            return "调休";
        }
        if (leaveName != null) {
            return "假";
        }
        if (holidayRateX10 > 0) {
            return "出勤";
        }
        if (!isWorkday && hasRecord) {
            return "休";
        }
        return null;
    }

    /** 详情里的类型描述，例如「工作日」「出勤 · 2 倍」「育儿假 · 全薪 · 半天」。 */
    String dayTypeText() {
        if (leaveName != null) {
            return LeaveRecord.describe(leaveName, leavePayPercent, leaveHalf);
        }
        if (holidayRateX10 > 0) {
            return "出勤 · " + HolidayWork.rateText(holidayRateX10);
        }
        if (isComp) {
            return "出勤 · 调休";
        }
        if (isWorkday) {
            return "工作日";
        }
        return "休息日";
    }
}
