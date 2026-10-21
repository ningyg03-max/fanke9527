package com.salaryjump.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;

/**
 * 每日工资归档（把「昨天」的工资落成一行记录）。
 *
 * <h3>为什么独立成类</h3>
 * 这个逻辑原来藏在 {@code MainActivity.cumRollover()} 里，而它<b>只被
 * {@code cumulativeTotal()} / {@code cumulativeDays()} 顺带调用</b>。
 * v3.87 主卡改用 {@code monthlyTotal()} 之后，那两个方法没人调了 →
 * <b>归档再也没跑过，昨天的工资一直不入账</b>。
 *
 * <p>抽出来之后：① 归档可以被显式调用，不再依赖某个显示函数 ② 可独立测试。
 */
final class DailyArchive {
    private DailyArchive() {
    }

    private static int stamp(Calendar calendar) {
        return (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
    }

    /**
     * 把「上次归档日 + 1 天」到「昨天」之间的每一天，按当时的规则算好并落库。
     *
     * <p>同一天重复调用不会重复加钱（`cum_last_day` 只会前进）。
     */
    static void rollover(Context context) {
        if (context == null) {
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences("salary_jump", 0);
        MainActivity.SalarySettings settings = MainActivity.SalarySettings.load(prefs);

        // 归档到「昨天」为止 —— 今天的钱还在实时算
        Calendar cursor = Calendar.getInstance();
        cursor.add(5, -1);
        int yesterdayStamp = stamp(cursor);

        int lastDay = prefs.getInt("cum_last_day", 0);
        if (lastDay == 0) {
            // 首次使用：只设基准，不补历史
            prefs.edit().putInt("cum_last_day", yesterdayStamp).apply();
            return;
        }
        if (lastDay >= yesterdayStamp) {
            return;
        }

        double total = Double.longBitsToDouble(prefs.getLong("cum_total_bits", 0L));
        int days = prefs.getInt("cum_days", 0);

        Calendar day = Calendar.getInstance();
        day.set(lastDay / 10000, ((lastDay / 100) % 100) - 1, lastDay % 100);

        int processed = 0;
        while (true) {
            day.add(5, 1);
            int dayStamp = stamp(day);
            if (dayStamp > yesterdayStamp) {
                break;
            }
            int stampValue = (day.get(1) * 10000) + ((day.get(2) + 1) * 100) + day.get(5);

            int rateX10 = HolidayWork.rateX10Of(context, stampValue);
            long backfill = MonthBackfill.centsOn(context, stampValue);
            long extra = ExtraIncome.centsOf(context, stampValue);

            if (rateX10 > 0) {
                // 休息日出勤（法定 / 计薪周末），按倍率
                double earned = HolidayWork.earned(settings.dailySalary, rateX10);
                days++;
                SalaryHistory.put(context, stampValue, Math.round(earned * 100.0d) + extra + backfill);
                total = total + ((extra + backfill) / 100.0d) + earned;
            } else if (HolidayWork.isComp(context, stampValue)) {
                // 调休出勤：无薪，只有外快 / 补录
                total = (extra > 0L || backfill > 0L) ? total + ((extra + backfill) / 100.0d) : total;
                SalaryHistory.put(context, stampValue, extra + backfill);
                processed++;
            } else if (WorkdayHelper.isWorkday(context, settings, day)) {
                LeaveRecord.Entry entry = LeaveRecord.get(context, stampValue);
                if (entry != null) {
                    // 请假：按计薪比例
                    double earned = LeaveRecord.earned(settings.dailySalary, entry.payPercent, entry.half);
                    days++;
                    SalaryHistory.put(context, stampValue, Math.round(100.0d * earned) + extra + backfill);
                    total = total + earned + ((extra + backfill) / 100.0d);
                    processed++;
                } else {
                    // 正常工作日：日薪 + 加班费
                    double overtime = 0.0d;
                    if (settings.overtimeEnabled && settings.overtimeRateX10 > 0) {
                        overtime = OvertimeCalc.earned(settings.dailySalary,
                                MainActivity.SalaryCalculator.totalWorkSeconds(settings),
                                OvertimeLog.secondsOfDay(context, dayStamp, System.currentTimeMillis()),
                                settings.overtimeRateX10);
                    }
                    double earned = settings.dailySalary + overtime;
                    days++;
                    SalaryHistory.put(context, dayStamp, Math.round(100.0d * earned) + extra + backfill);
                    total = total + earned + ((extra + backfill) / 100.0d);
                }
            } else {
                // 休息日：只有外快 / 补录才建行
                if (extra > 0L || backfill > 0L) {
                    long sum = extra + backfill;
                    total = total + (sum / 100.0d);
                    SalaryHistory.put(context, stampValue, sum);
                }
                processed++;
            }

            if (processed > 4000) {
                break;
            }
            lastDay = stampValue;
        }

        prefs.edit()
                .putInt("cum_last_day", lastDay)
                .putLong("cum_total_bits", Double.doubleToRawLongBits(total))
                .putInt("cum_days", days)
                .apply();
    }
}
