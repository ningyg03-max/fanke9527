package com.salaryjump.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;

/**
 * 测试桩 MainActivity —— 仅含纯逻辑内部类（SalarySettings / SalarySnapshot / SalaryCalculator），
 * 从真实 3.61 MainActivity 自动抽取。
 */
public class MainActivity extends Context {
    /** 本月已结算天数（与真实实现一致）。 */
    public static int monthlySettledDays(Context context) {
        if (context == null) {
            return 0;
        }
        try {
            java.util.Calendar now = java.util.Calendar.getInstance();
            int count = 0;
            for (SalaryHistory.Day day : SalaryHistory.byMonth(context, now.get(1), now.get(2) + 1)) {
                if (ArchiveSync.countsAsSettledDay(context, day.stamp)) {
                    count++;
                }
            }
            return count;
        } catch (Throwable unused) {
            return 0;
        }
    }

    /** 本月累计（与真实实现一致）：本月已归档 + 今天未归档时的实时已赚。 */
    static double monthlyTotal(Context context) {
        if (context == null) {
            return 0.0d;
        }
        try {
            java.util.Calendar now = java.util.Calendar.getInstance();
            int year = now.get(1);
            int month = now.get(2) + 1;
            int todayStamp = SalaryHistory.stamp(now);
            android.content.SharedPreferences sp =
                    context.getSharedPreferences("salary_jump", 0);
            SalarySettings settings = SalarySettings.load(sp);

            long archived = SalaryHistory.monthTotalCents(context, year, month);
            long todayCents = SalaryHistory.centsOf(context, todayStamp);
            double live = todayCents > 0L ? 0.0d
                    : SalaryCalculator.calculate(context, settings, now).earned;
            return (archived / 100.0d) + live;
        } catch (Throwable unused) {
            return 0.0d;
        }
    }

    /** 倒计时格式（与真实实现一致）：不足 1 小时「分:秒」，否则「时:分:秒」。 */
    static String formatDuration(int seconds) {
        if (seconds < 0) {
            seconds = 0;
        }
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        if (h > 0) {
            return String.format(java.util.Locale.US, "%d:%02d:%02d",
                    Integer.valueOf(h), Integer.valueOf(m), Integer.valueOf(s));
        }
        return String.format(java.util.Locale.US, "%02d:%02d",
                Integer.valueOf(m), Integer.valueOf(s));
    }

    /**
     * 与真实 MainActivity 同实现（薄封装）：
     * 从偏好读「进度起算日」再交给 MonthBackfill.goalProgress。
     * 放在桩里是为了锁住 key 名与语义 —— 存钱罐的「累计已攒」必须走这条，
     * 不能用 SalaryHistory.totalCents()（那是全部历史总额）。
     */
    static double goalProgress(Context context) {
        return MonthBackfill.goalProgress(context,
                context.getSharedPreferences("salary_jump", 0).getInt("goal_start_stamp", 0));
    }

    static final int STATE_REST_DAY = 0;
    static final int STATE_BEFORE_WORK = 1;
    static final int STATE_MORNING = 2;
    static final int STATE_LUNCH = 3;
    static final int STATE_AFTERNOON = 4;
    static final int STATE_AFTER_WORK = 5;
    static final int STATE_OVERTIME = 6;
    static final int STATE_HOLIDAY_WORK = 7;
    static final int STATE_LEAVE = 8;

    static class SalarySettings {
        double dailySalary = 300.0d;
        int workStartMinutes = 480;
        int workEndMinutes = 1020;
        int breakStartMinutes = 690;
        int breakEndMinutes = 750;
        int workMode = 2;
        boolean useCalendar = false;
        boolean overtimeEnabled = false;
        int overtimeRateX10 = 15;
        int overtimeGraceMinutes = 30;

        SalarySettings() {
        }

        SalarySettings copy() {
            SalarySettings salarySettings = new SalarySettings();
            salarySettings.dailySalary = this.dailySalary;
            salarySettings.workStartMinutes = this.workStartMinutes;
            salarySettings.workEndMinutes = this.workEndMinutes;
            salarySettings.breakStartMinutes = this.breakStartMinutes;
            salarySettings.breakEndMinutes = this.breakEndMinutes;
            salarySettings.workMode = this.workMode;
            salarySettings.useCalendar = this.useCalendar;
            salarySettings.overtimeEnabled = this.overtimeEnabled;
            salarySettings.overtimeRateX10 = this.overtimeRateX10;
            salarySettings.overtimeGraceMinutes = this.overtimeGraceMinutes;
            return salarySettings;
        }

        static SalarySettings load(SharedPreferences sharedPreferences) {
            SalarySettings salarySettings = new SalarySettings();
            salarySettings.dailySalary = Double.longBitsToDouble(sharedPreferences.getLong("daily_salary_bits", Double.doubleToRawLongBits(salarySettings.dailySalary)));
            salarySettings.workStartMinutes = sharedPreferences.getInt("work_start", salarySettings.workStartMinutes);
            salarySettings.workEndMinutes = sharedPreferences.getInt("work_end", salarySettings.workEndMinutes);
            salarySettings.breakStartMinutes = sharedPreferences.getInt("break_start", salarySettings.breakStartMinutes);
            salarySettings.breakEndMinutes = sharedPreferences.getInt("break_end", salarySettings.breakEndMinutes);
            salarySettings.workMode = sharedPreferences.getInt("work_mode", salarySettings.workMode);
            salarySettings.useCalendar = sharedPreferences.getBoolean("use_calendar", salarySettings.useCalendar);
            salarySettings.overtimeEnabled = sharedPreferences.getBoolean("overtime_enabled", salarySettings.overtimeEnabled);
            salarySettings.overtimeRateX10 = sharedPreferences.getInt("overtime_rate", salarySettings.overtimeRateX10);
            salarySettings.overtimeGraceMinutes = sharedPreferences.getInt("overtime_grace", salarySettings.overtimeGraceMinutes);
            return salarySettings;
        }

        void save(SharedPreferences sharedPreferences) {
            sharedPreferences.edit().putLong("daily_salary_bits", Double.doubleToRawLongBits(this.dailySalary)).putInt("work_start", this.workStartMinutes).putInt("work_end", this.workEndMinutes).putInt("break_start", this.breakStartMinutes).putInt("break_end", this.breakEndMinutes).putInt("work_mode", this.workMode).putBoolean("use_calendar", this.useCalendar).putBoolean("overtime_enabled", this.overtimeEnabled).putInt("overtime_rate", this.overtimeRateX10).putInt("overtime_grace", this.overtimeGraceMinutes).apply();
        }
    }

    static class SalarySnapshot {
        double earned;
        double extraEarned;
        boolean holidayComp;
        int holidayRateX10;
        boolean leaveHalf;
        String leaveName;
        int leavePayPercent;
        double overtimeEarned;
        double overtimeSeconds;
        double progress;
        int state;

        SalarySnapshot() {
        }
    }

    static class SalaryCalculator {
        SalaryCalculator() {
        }

        static double totalWorkSeconds(SalarySettings salarySettings) {
            return Math.max(0.0d, ((salarySettings.workEndMinutes - salarySettings.workStartMinutes) * 60.0d) - ((salarySettings.breakEndMinutes - salarySettings.breakStartMinutes) * 60.0d));
        }

        static SalarySnapshot calculate(Context context, SalarySettings salarySettings, Calendar calendar) {
            SalarySnapshot calculateBase = calculateBase(context, salarySettings, calendar);
            try {
                double dayTotal = ExtraIncome.dayTotal(context, (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5));
                if (dayTotal > 0.0d) {
                    calculateBase.extraEarned = dayTotal;
                    calculateBase.earned += dayTotal;
                }
            } catch (Throwable th) {
            }
            return calculateBase;
        }

        private static SalarySnapshot calculateBase(Context context, SalarySettings salarySettings, Calendar calendar) {
            double d;
            double d2;
            SalarySnapshot salarySnapshot = new SalarySnapshot();
            double d3 = totalWorkSeconds(salarySettings);
            if (d3 <= 0.0d) {
                salarySnapshot.state = 5;
                return salarySnapshot;
            }
            if (!WorkdayHelper.isWorkday(context, salarySettings, calendar)) {
                int i = (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
                int rateX10Of = HolidayWork.rateX10Of(context, i);
                if (rateX10Of > 0) {
                    salarySnapshot.state = MainActivity.STATE_HOLIDAY_WORK;
                    salarySnapshot.holidayRateX10 = rateX10Of;
                    salarySnapshot.progress = 1.0d;
                    salarySnapshot.earned = HolidayWork.earned(salarySettings.dailySalary, rateX10Of);
                    return salarySnapshot;
                }
                if (HolidayWork.isComp(context, i)) {
                    salarySnapshot.state = MainActivity.STATE_HOLIDAY_WORK;
                    salarySnapshot.holidayComp = true;
                    salarySnapshot.progress = 1.0d;
                    salarySnapshot.earned = 0.0d;
                    return salarySnapshot;
                }
                salarySnapshot.state = 0;
                return salarySnapshot;
            }
            LeaveRecord.Entry entry = LeaveRecord.get(context, (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5));
            if (entry != null) {
                salarySnapshot.state = 8;
                salarySnapshot.leavePayPercent = entry.payPercent;
                salarySnapshot.leaveHalf = entry.half;
                salarySnapshot.leaveName = entry.name;
                salarySnapshot.progress = LeaveRecord.ratio(entry.payPercent, entry.half);
                salarySnapshot.earned = LeaveRecord.earned(salarySettings.dailySalary, entry.payPercent, entry.half);
                return salarySnapshot;
            }
            double d4 = (calendar.get(11) * 3600.0d) + (calendar.get(12) * 60.0d) + calendar.get(13) + (calendar.get(14) / 1000.0d);
            double d5 = salarySettings.workStartMinutes * 60.0d;
            double d6 = salarySettings.workEndMinutes * 60.0d;
            double d7 = salarySettings.breakStartMinutes * 60.0d;
            double d8 = 60.0d * salarySettings.breakEndMinutes;
            if (d4 < d5) {
                salarySnapshot.state = 1;
                d2 = 0.0d;
                d = d3;
            } else if (d4 < d7) {
                salarySnapshot.state = 2;
                d2 = d4 - d5;
                d = d3;
            } else if (d4 < d8) {
                salarySnapshot.state = MainActivity.STATE_LUNCH;
                d2 = d7 - d5;
                d = d3;
            } else if (d4 < d6) {
                salarySnapshot.state = 4;
                d2 = (d4 - d8) + (d7 - d5);
                d = d3;
            } else {
                salarySnapshot.state = 5;
                if (salarySettings.overtimeEnabled && salarySettings.overtimeRateX10 > 0) {
                    double secondsOfDay = OvertimeLog.secondsOfDay(context, (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5), calendar.getTimeInMillis());
                    if (secondsOfDay <= 0.0d) {
                        d = d3;
                    } else {
                        salarySnapshot.state = MainActivity.STATE_OVERTIME;
                        salarySnapshot.overtimeSeconds = secondsOfDay;
                        d = d3;
                        salarySnapshot.overtimeEarned = OvertimeCalc.earned(salarySettings.dailySalary, d, secondsOfDay, salarySettings.overtimeRateX10);
                    }
                } else {
                    d = d3;
                }
                d2 = d;
            }
            salarySnapshot.progress = Math.max(0.0d, Math.min(d, d2)) / d;
            salarySnapshot.earned = (salarySettings.dailySalary * salarySnapshot.progress) + salarySnapshot.overtimeEarned;
            return salarySnapshot;
        }
    }
}
