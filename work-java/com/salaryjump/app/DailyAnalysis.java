package com.salaryjump.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 喝水「分析」—— 规则引擎，不接大模型。
 *
 * <h3>为什么不用 AI</h3>
 * 这类分析本质是<b>统计描述</b>：达标率、趋势斜率、空档长度、成分占比，
 * 全都能用确定公式算出来。大模型拿到同样的数字只会换个说法重述，
 * 不会多出一个字节的信息量；而缺少体重 / 活动量 / 气温时，
 * 它反而会编出「你可能轻度脱水」这类听着专业、实则没有依据的话。
 *
 * <p>「AI 感」不靠模型，靠<b>文案写得像人话</b>：
 * 不说「平均 2961ml，达标率 53%」，而说「你不缺总量，缺的是每天都喝到」。
 *
 * <h3>设计</h3>
 * 先算事实 → 再选模板 → 最后填数字。全部纯函数，可单测。
 */
final class DailyAnalysis {
    /** 一天里活动的主要时段（用来算空档） */
    static final int DAY_START_HOUR = 8;
    static final int DAY_END_HOUR = 20;

    private DailyAnalysis() {
    }

    /** 一条发现 */
    static final class Finding {
        final String title;
        final String detail;
        final int severity;

        Finding(String title, String detail, int severity) {
            this.title = title;
            this.detail = detail;
            this.severity = severity;
        }
    }

    static final class Result {
        /** 一句话结论（大号显示）；数据不足时为 null */
        final String headline;
        /** 具体依据，最多 3 条 */
        final List<Finding> findings;
        /** 数据不足时的提示（大号显示这条，findings 为空） */
        final String hint;

        Result(String headline, List<Finding> findings, String hint) {
            this.headline = headline;
            this.findings = findings;
            this.hint = hint;
        }

        boolean isEmpty() {
            return findings.isEmpty() && hint == null;
        }
    }

    /**
     * @param daily      每日总量，按时间正序，0 = 那天没记录
     * @param targetMl   目标
     * @param hourDays   逐天的小时桶（每天一个 24 长度数组）。少于 3 天不做空档分析。
     *                   注意传<b>每天的</b>，不要传累加值 —— 累加会把某天某点喝过填进别的天的空档
     * @param totalCups  总杯数
     * @param sugaryCups 含糖杯数
     * @param totalMl    总毫升
     */
    static Result analyze(int[] daily, int targetMl, int[][] hourDays,
                          int totalCups, int sugaryCups, int totalMl) {
        int days = daily == null ? 0 : daily.length;
        if (days < 3) {
            return new Result(null, new ArrayList<Finding>(),
                    days == 0 ? "还没有记录，先记几天再看分析" : "记录只有 " + days + " 天，攒够 7 天再看更有意义");
        }

        List<Finding> list = new ArrayList<>();
        int recorded = 0;
        int goalDays = 0;
        int sum = 0;
        int maxV = 0;
        int minV = Integer.MAX_VALUE;
        for (int value : daily) {
            sum += value;
            if (value > 0) {
                recorded++;
                minV = Math.min(minV, value);
            }
            if (value >= targetMl) {
                goalDays++;
            }
            maxV = Math.max(maxV, value);
        }
        if (recorded == 0) {
            return new Result(null, new ArrayList<Finding>(), "这段时间没有记录");
        }
        if (minV == Integer.MAX_VALUE) {
            minV = 0;
        }

        double avg = sum / (double) days;
        int avgRecorded = Math.round(sum / (float) recorded);
        int goalRate = Math.round(100.0f * goalDays / days);
        // 波动只看「有记录的日子」—— 把没记录的日子当 0 会让波动虚高，
        // 和「有记录的日子平均」混在同一句话里就自相矛盾了
        double sigma = stddevOfRecorded(daily);
        int missing = days - recorded;

        // ── 规则 1：记录不连续 ──
        if (missing > 0 && missing / (double) days > 0.2) {
            list.add(new Finding(
                    missing + " 天没有记录",
                    "分不清是没喝还是忘了记，分析会把它们当成 0",
                    Math.min(60, 30 + missing * 4)));
        }

        // ── 规则 2：忽高忽低（最有价值的判断）──
        if (sigma > 500 && goalRate < 60) {
            list.add(new Finding(
                    "你不缺总量，缺的是每天都喝到",
                    "有记录的日子平均 " + avgRecorded + " ml（目标 " + targetMl + "）。"
                            + days + " 天里只有 " + goalDays + " 天达标（" + goalRate + "%），"
                            + "上下波动 " + Math.round(sigma) + " ml：最好的一天 " + maxV
                            + "，最差 " + minV,
                    95));
        } else if (goalRate >= 80) {
            list.add(new Finding(
                    "喝得很稳，继续保持",
                    days + " 天里 " + goalDays + " 天达标（" + goalRate + "%），"
                            + "每天平均 " + Math.round(avg) + " ml",
                    10));
        } else if (avg < targetMl * 0.8) {
            list.add(new Finding(
                    "整体偏少，差得有点多",
                    "平均每天 " + Math.round(avg) + " ml，比目标少 "
                            + Math.round(targetMl - avg) + " ml",
                    90));
        }

        // ── 规则 3：趋势往下走 ──
        int half = days / 2;
        if (half >= 2) {
            int firstAvg = avgRecordedOf(daily, 0, half);
            int secondAvg = avgRecordedOf(daily, half, days);
            // 任一半有记录的天数不足 2 天 → 不判断趋势，样本太小会说瞎话
            if (firstAvg > 0 && secondAvg > 0 && secondAvg < firstAvg * 0.85) {
                int drop = Math.round(100.0f * (firstAvg - secondAvg) / firstAvg);
                list.add(new Finding(
                        "最近在往下走",
                        "前 " + half + " 天平均 " + firstAvg + " ml，后 " + (days - half)
                                + " 天 " + secondAvg + " ml，降了 " + drop
                                + "%（只按有记录的日子算）",
                        85));
            }
        }

        // ── 规则 4：白天空档太长（需要时间数据）──
        if (hourDays != null && hourDays.length >= 3) {
            // 逐天算再取最差的一天 —— 这才是「典型的一天会断多久」
            int worst = 0;
            for (int[] day : hourDays) {
                worst = Math.max(worst, longestDryStreak(day));
            }
            if (worst >= 4) {
                list.add(new Finding(
                        "白天有 " + worst + " 小时的空档",
                        "按 " + DAY_START_HOUR + ":00–" + DAY_END_HOUR + ":00 算，"
                                + "最长有一天连续 " + worst + " 小时没喝东西。"
                                + "一口气补不如分几次",
                        80));
            }
        }

        // ── 规则 5：大杯低频 ──
        if (totalCups > 0) {
            int perCup = Math.round(totalMl / (float) totalCups);
            if (perCup >= 400) {
                list.add(new Finding(
                        "一次喝得多、次数偏少",
                        "平均每次 " + perCup + " ml，一天约 "
                                + Math.round(totalCups / (float) recorded) + " 次。"
                                + "拆成 250 ml 喝吸收更均匀",
                        70));
            }
        }

        // ── 规则 6：含糖偏高 ──
        if (totalCups > 0 && sugaryCups / (double) totalCups > 0.3) {
            list.add(new Finding(
                    "含糖饮料占比偏高",
                    sugaryCups + " / " + totalCups + " 杯含糖（"
                            + Math.round(100.0f * sugaryCups / totalCups) + "%）",
                    75));
        }

        Collections.sort(list, new Comparator<Finding>() {
            @Override
            public int compare(Finding a, Finding b) {
                return b.severity - a.severity;
            }
        });
        while (list.size() > 3) {
            list.remove(list.size() - 1);
        }

        return new Result(list.isEmpty() ? "数据太少，还看不出规律" : list.get(0).title,
                list, null);
    }

    /** 只按「有记录的日子」算平均；记录不足 2 天返回 -1（表示无从判断） */
    static int avgRecordedOf(int[] data, int from, int to) {
        if (data == null || from < 0 || to > data.length || from >= to) {
            return -1;
        }
        int sum = 0;
        int count = 0;
        for (int i = from; i < to; i++) {
            if (data[i] > 0) {
                sum += data[i];
                count++;
            }
        }
        return count < 2 ? -1 : Math.round(sum / (float) count);
    }

    /** 只按「有记录的日子」算标准差 */
    static double stddevOfRecorded(int[] data) {
        if (data == null) {
            return 0.0d;
        }
        int sum = 0;
        int count = 0;
        for (int value : data) {
            if (value > 0) {
                sum += value;
                count++;
            }
        }
        if (count < 2) {
            return 0.0d;
        }
        double avg = sum / (double) count;
        double acc = 0.0d;
        for (int value : data) {
            if (value > 0) {
                double diff = value - avg;
                acc += diff * diff;
            }
        }
        return Math.sqrt(acc / count);
    }

    /** 一段区间的平均值（含 0 值） */
    static int avgOf(int[] data, int from, int to) {
        if (data == null || from < 0 || to > data.length || from >= to) {
            return 0;
        }
        int sum = 0;
        for (int i = from; i < to; i++) {
            sum += data[i];
        }
        return Math.round(sum / (float) (to - from));
    }

    static double stddev(int[] data, double avg) {
        if (data == null || data.length == 0) {
            return 0.0d;
        }
        double acc = 0.0d;
        for (int value : data) {
            double diff = value - avg;
            acc += diff * diff;
        }
        return Math.sqrt(acc / data.length);
    }

    /**
     * 白昼时段里最长的连续「干涸」小时数。
     *
     * <p>只看 {@link #DAY_START_HOUR}–{@link #DAY_END_HOUR}，
     * 半夜不喝是正常的，不该算成空档。
     */
    static int longestDryStreak(int[] hours) {
        int best = 0;
        int current = 0;
        for (int hour = DAY_START_HOUR; hour <= DAY_END_HOUR && hour < hours.length; hour++) {
            if (hours[hour] <= 0) {
                current++;
                best = Math.max(best, current);
            } else {
                current = 0;
            }
        }
        return best;
    }
}
