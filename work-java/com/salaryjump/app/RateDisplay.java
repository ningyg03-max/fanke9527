package com.salaryjump.app;

/**
 * 主卡「费率」栏的显示规则。
 *
 * <h3>为什么要这个</h3>
 * 主卡的「每小时 / 每分钟 / 每秒」是**费率**（日薪 ÷ 工作时长），
 * 原来只在改设置时更新 —— 所以<b>下班后、周末、请假时照样显示 ¥35.20</b>，
 * 好像在说「你还在赚」。
 *
 * <p>这里按当前状态决定：<b>赚的时候显示数字，不赚的时候灰掉</b>。
 *
 * <h3>休息日出勤的三种情况（必须分清）</h3>
 * <table>
 *   <tr><th>场景</th><th>rateX10</th><th>费率栏</th></tr>
 *   <tr><td>法定假日出勤</td><td>30</td><td>¥105.60 · <b>3 倍 结算</b></td></tr>
 *   <tr><td>周末计薪出勤</td><td>20</td><td>¥70.40 · <b>2 倍 结算</b></td></tr>
 *   <tr><td><b>调休出勤</b></td><td><b>0</b></td><td><b>— · 调休 · 不计薪</b>（灰掉）</td></tr>
 * </table>
 *
 * 调休出勤是<b>无薪</b>的（只是攒调休天数），所以倍率是 0 ——
 * <b>绝不能把它当成「倍率未知」而回落到加班倍率</b>，那会显示成「1.5 倍」并放大时薪，
 * 与当天实收 ¥0 自相矛盾。
 */
final class RateDisplay {
    /** 费率正常显示（正在赚） */
    static final int SHOW = 0;
    /** 灰掉（不显示数字） */
    static final int DIM = 1;

    private RateDisplay() {
    }

    /**
     * 该不该显示费率数字。
     *
     * @param state       当前状态
     * @param holidayComp 是否「调休出勤」（无薪）
     */
    static int modeFor(int state, boolean holidayComp) {
        switch (state) {
            case MainActivity.STATE_MORNING:        // 上午
            case MainActivity.STATE_AFTERNOON:      // 下午
            case MainActivity.STATE_OVERTIME:       // 加班中
                return SHOW;
            case MainActivity.STATE_HOLIDAY_WORK:   // 休息日出勤
                // 调休无薪 → 灰掉；法定/计薪有倍率 → 显示
                return holidayComp ? DIM : SHOW;
            default:
                // 还没上班 / 午休 / 下班后 / 休息日 / 请假 → 灰掉
                return DIM;
        }
    }

    /** 兼容旧调用（不知道是否调休时，按非调休处理）。 */
    static int modeFor(int state) {
        return modeFor(state, false);
    }

    /** 灰掉时用的占位文本。 */
    static String dimText() {
        return "—";
    }

    /**
     * 费率栏的标签文字。
     *
     * @param state       当前状态
     * @param rateX10     出勤倍率 ×10（加班 15、计薪周末 20、法定 30）
     * @param holidayComp 是否调休出勤
     */
    static String labelFor(int state, int rateX10, boolean holidayComp) {
        if (state == MainActivity.STATE_OVERTIME) {
            return "加班 " + formatRate(rateX10);
        }
        if (state == MainActivity.STATE_HOLIDAY_WORK) {
            if (holidayComp) {
                return "调休 · 不计薪";
            }
            return formatRate(rateX10) + " 结算";
        }
        return "每小时";
    }

    /** 兼容旧调用。 */
    static String labelFor(int state, int rateX10) {
        return labelFor(state, rateX10, false);
    }

    /** 倍率 ×10 → 「1.5 倍」。 */
    static String formatRate(int rateX10) {
        if (rateX10 <= 0) {
            return "1 倍";
        }
        double value = rateX10 / 10.0d;
        if (value == Math.floor(value)) {
            return ((int) value) + " 倍";
        }
        return value + " 倍";
    }

    /**
     * 当前状态对应的实际时薪（元/小时）。
     *
     * <p>加班和出勤按倍率放大 —— 否则「加班中 1.5 倍」显示的还是平时的费率，
     * 跟状态胶囊里的「加班费 ¥x」自相矛盾。
     *
     * <p>调休出勤<b>不放大</b>（它不产生收入），调用方应据此灰掉。
     *
     * @param baseHourly  基础时薪（元/小时）
     * @param state       当前状态
     * @param rateX10     倍率 ×10
     * @param holidayComp 是否调休出勤
     */
    static double hourlyFor(double baseHourly, int state, int rateX10, boolean holidayComp) {
        if (holidayComp && state == MainActivity.STATE_HOLIDAY_WORK) {
            return baseHourly;                       // 调休不放大（反正会灰掉）
        }
        if ((state == MainActivity.STATE_OVERTIME || state == MainActivity.STATE_HOLIDAY_WORK)
                && rateX10 > 0) {
            return baseHourly * rateX10 / 10.0d;
        }
        return baseHourly;
    }

    /** 兼容旧调用。 */
    static double hourlyFor(double baseHourly, int state, int rateX10) {
        return hourlyFor(baseHourly, state, rateX10, false);
    }
}
