package com.salaryjump.app;

import android.content.Context;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 打工小人的语录池。
 *
 * <h3>两类语录</h3>
 * <ul>
 *   <li>{@link #auto(Context)} —— 小人下方自动显示的那行（按当前状态挑）</li>
 *   <li>{@link #poke(Context)} —— 点小人时蹦出来的（连戳彩蛋在 {@link WorkerPet} 里）</li>
 * </ul>
 *
 * <h3>为什么抽成独立类</h3>
 * 原来内联在 {@code PetState} 里，想加一句就要改那个 279 行的方法。
 * 独立出来之后：① 文案集中一处 ② 可以单独回归测试（池子非空、状态能命中）。
 *
 * <h3>风格</h3>
 * 自嘲但不丧 —— 不卖惨，也不打鸡血。
 */
final class PetQuotes {
    private PetQuotes() {
    }

    /* ══════════════ 自动语录 ══════════════ */

    static List<String> auto(Context context) {
        List<String> out = new ArrayList<>();
        if (context == null) {
            return out;
        }
        try {
            Calendar calendar = Calendar.getInstance();
            int dow = calendar.get(7);          // 1=周日 … 7=周六
            int hour = calendar.get(11);
            int today = SalaryHistory.stamp(calendar);

            int daysToPayday = PetState.daysToPayday(context);
            int streak = PetState.workStreak(context);
            MainActivity.SalarySettings settings =
                    MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0));

            boolean onLeave = LeaveRecord.has(context, today);
            boolean hasOvertime = false;
            try {
                OvertimeLog.Punch punch = OvertimeLog.load(context);
                hasOvertime = punch.day == today && punch.exists();
            } catch (Throwable unused) {
            }

            /* ── 发薪日优先 ── */
            if (daysToPayday == 0) {
                out.add("今天发工资！💰");
                out.add("发薪日到啦，晚上吃点好的");
                out.add("一个月就等这一天");
                out.add("到账了，先别查余额，让我高兴一会");
            } else if (daysToPayday == 1) {
                out.add("明天就发工资了，今晚睡得着");
                out.add("明天到账，今天先忍住不乱花");
            }

            /* ── 请假 ── */
            if (onLeave) {
                out.add("今天不上班，嘿嘿");
                out.add("请假在家，理直气壮");
                out.add("睡到自然醒，这就是人生");
                out.add("手机一响就心跳，还好今天不用去");
            }

            /* ── 疲劳（按天数分档，带动态数字）── */
            if (streak >= 10) {
                out.add("连着上了 " + streak + " 天，魂快散了…");
                out.add("我已经不认识床以外的东西了");
                out.add("上到第 " + streak + " 天，我开始怀疑日历");
            } else if (streak >= 6) {
                out.add("连着 " + streak + " 天了，有点扛不住");
                out.add("好累…但还得干");
                out.add("咖啡已经不起作用了");
            } else if (streak >= 3) {
                out.add("连着干了 " + streak + " 天，还行");
                out.add("有点累，但还能撑");
            }

            /* ── 加班 ── */
            if (hasOvertime) {
                out.add("今天又加班了…");
                out.add("加班中，勿扰（钱到位了）");
                out.add("这个点的钱，赚得格外清楚");
                out.add("灯还亮着，人还在这儿");
            }

            /* ── 外快 ── */
            try {
                long extra = ExtraIncome.centsOf(context, today);
                if (extra > 0L) {
                    out.add("接了笔私活，钱包鼓了点");
                    out.add("外快到账，比工资来得快");
                }
            } catch (Throwable unused) {
            }

            /* ── 出勤 / 调休 / 补班 ── */
            try {
                if (HolidayWork.has(context, today)) {
                    if (HolidayWork.isComp(context, today)) {
                        out.add("今天是来换休的，不是来赚钱的");
                    } else {
                        out.add("别人在晒周末，我在晒工位");
                        out.add("周末上班，代价是换来的（有补贴）");
                    }
                }
                String makeup = WorkdayHelper.makeupNote(context, settings, calendar);
                if (makeup != null && !onLeave) {
                    out.add("明明是周末，却要打卡");
                    out.add("调休上班，一分不多拿");
                }
            } catch (Throwable unused) {
            }

            /* ── 存钱罐 ── */
            try {
                GoalStore.Goal goal = GoalStore.current(context);
                if (goal != null && goal.targetCents > 0L) {
                    double ratio = GoalStore.progressYuan(context, goal)
                            / (goal.targetCents / 100.0d);
                    if (ratio >= 1.0d) {
                        out.add("目标达成了！🎉");
                        out.add("攒够了，可以下手了");
                    } else if (ratio >= 0.5d) {
                        out.add("存钱罐过半了，有点小骄傲");
                        out.add("一半了，比想象中快");
                    } else if (ratio >= 0.25d) {
                        out.add("攒到 1/4 了，慢慢来");
                        out.add("起步了，别急");
                    }
                }
            } catch (Throwable unused) {
            }

            /* ── 星期氛围 ── */
            if (dow == 2) {
                out.add("周一…不想说话");
                out.add("又是周一，缓一缓");
                out.add("周一的存在就是为了衬托周五");
            } else if (dow == 6) {
                out.add("周五啦，撑住就是周末");
                out.add("再熬几个小时就自由了");
            } else if (dow == 7) {
                out.add("周六！自由的空气");
                out.add("今天谁也别找我");
            } else if (dow == 1 && hour >= 17) {
                out.add("想到明天要上班…");
                out.add("周末怎么这么快");
                out.add("快乐的时间总是按分钟算的");
            }

            /* ── 时段 ── */
            if (hour < 9) {
                out.add("早…让我先醒醒");
                out.add("眼睛睁开了，脑子还没到");
            } else if (hour >= 22) {
                out.add("这么晚了，早点休息");
            }

            /* ── 今天还没记录 / 这个月空白 ── */
            try {
                if (!SalaryHistory.has(context, today)) {
                    out.add("今天还没开始记，钱在等你");
                    out.add("打过卡才算今天开始");
                } else if (SalaryHistory.byMonth(context, calendar.get(1), calendar.get(2) + 1).size() <= 1) {
                    out.add("这个月还是空白，去「工资日历」补上");
                }
            } catch (Throwable unused) {
            }
        } catch (Throwable unused) {
        }

        if (out.isEmpty()) {
            out.add("打工人的一天");
            out.add("平平无奇，但钱在涨");
        }
        return out;
    }

    /* ══════════════ 点击语录 ══════════════ */

    static List<String> poke(Context context) {
        List<String> out = new ArrayList<>();
        if (context == null) {
            out.add("嘿嘿");
            return out;
        }
        try {
            int daysToPayday = PetState.daysToPayday(context);
            int streak = PetState.workStreak(context);
            Calendar calendar = Calendar.getInstance();
            int today = SalaryHistory.stamp(calendar);
            int hour = calendar.get(11);

            if (daysToPayday == 0) {
                out.add("别戳了，我在数钱");
                out.add("今天身价不一样，别乱碰");
            }
            if (streak >= 6) {
                out.add("别戳…让我躺一会");
                out.add("累…但还能再战");
                out.add("一指头戳在我最后一口气上");
            }
            // 非工作时间
            try {
                MainActivity.SalarySettings settings =
                        MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0));
                int nowMinutes = (hour * 60) + calendar.get(12);
                boolean offDuty = nowMinutes < settings.workStartMinutes
                        || nowMinutes >= settings.workEndMinutes;
                if (offDuty || !WorkdayHelper.isWorkday(context, settings, calendar)) {
                    out.add("下班了，别找我");
                    out.add("现在戳我，不计工时");
                }
            } catch (Throwable unused) {
            }
            // 存钱罐有目标
            try {
                if (GoalStore.current(context) != null) {
                    out.add("离那个目标又近了一点");
                }
            } catch (Throwable unused) {
            }

            out.add("摸鱼被抓！");
            out.add("加油！加油！");
            out.add("嘿嘿");
            out.add("戳我干嘛，快去赚钱");
            out.add("摸一下，钱会变多吗");
            out.add("手闲了？去干活");
        } catch (Throwable unused) {
            out.add("嘿嘿");
        }
        return out;
    }

    /**
     * 从池子里挑一条，尽量不跟上一条重复。
     *
     * @param last 上一条（可为 null）
     */
    static String pick(List<String> pool, String last) {
        if (pool == null || pool.isEmpty()) {
            return "";
        }
        String chosen = pool.get((int) (Math.random() * pool.size()));
        if (pool.size() > 1 && chosen.equals(last)) {
            chosen = pool.get((int) (Math.random() * pool.size()));
        }
        return chosen;
    }
}
