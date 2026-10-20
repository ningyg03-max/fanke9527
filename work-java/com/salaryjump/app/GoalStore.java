package com.salaryjump.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 存钱罐目标（支持多个）。
 *
 * <h3>存储格式</h3>
 * 沿用工程惯例的分隔符字符串（与 {@code salary_history} 一致）：
 * <pre>
 *   goal_list = 名称|目标分|开始日|达标日|确认日|完成金额分   （多条用 ; 分隔）
 *   MacBook Pro|2356000|20260901|20261014|20261015|2390000
 * </pre>
 * · <b>确认日 = 0</b> → 这条是「当前进行中的目标」<br>
 * · <b>确认日 &gt; 0</b> → 已完成，进历史
 *
 * <h3>为什么要多个目标</h3>
 * 原来只有 4 个扁平 key（{@code goal_name} 等），新目标会直接覆盖旧目标。
 * 改成列表后可以保留历史，并能显示「用时多久、超出了多少」。
 *
 * <h3>进度为什么不会重叠</h3>
 * 进度 = 累加 {@code 日期 >= 开始日} 的收入。新目标的开始日取
 * <b>上一个目标完成日的次日</b>，所以两段区间首尾相接、不重叠。
 * 已完成的目标把实际金额<b>冻结</b>存下来（{@code frozenCents}），
 * 不再重算 —— 否则以后补录历史数据会让旧目标的数字跳变。
 */
final class GoalStore {
    /** 目标名称最大长度 */
    static final int MAX_NAME = 12;
    /** 一次最多保留多少个已完成目标 */
    static final int KEEP_DONE = 20;

    private static final String KEY_LIST = "goal_list";
    private static final String KEY_MIGRATED = "goal_list_migrated";
    /** 旧格式的 key —— 迁移后保留不删，作为回退保险 */
    private static final String OLD_NAME = "goal_name";
    private static final String OLD_AMOUNT = "goal_amount_bits";
    private static final String OLD_START = "goal_start_stamp";
    private static final String OLD_CELEBRATED = "goal_celebrated";

    static final class Goal {
        String name = "";
        /** 目标金额（分） */
        long targetCents;
        int startStamp;
        /** 第一次达成 100% 的日期戳；尚未达成为 0 */
        int reachedStamp;
        /** 用户确认完成的日期戳；0 表示进行中 */
        int doneStamp;
        /** 确认完成时冻结的「实际攒到」（分） */
        long frozenCents;

        boolean isDone() {
            return doneStamp > 0;
        }

        boolean isReached() {
            return reachedStamp > 0;
        }
    }

    private GoalStore() {
    }

    /* ─────────── 名称清洗 ─────────── */

    /** 清掉分隔符（和 ExtraIncome.cleanRemark 同一套做法），避免破坏存储格式。 */
    static String cleanName(String str) {
        if (str == null) {
            return "";
        }
        String trim = str.trim();
        if (trim.length() == 0) {
            return "";
        }
        String flat = trim.replace('|', ' ')
                .replace(';', ' ')
                .replace(':', ' ')
                .replace('\n', ' ')
                .replace('\r', ' ')
                .replaceAll("\\s+", " ")
                .trim();
        return flat.length() > MAX_NAME ? flat.substring(0, MAX_NAME) : flat;
    }

    /* ─────────── 解析 / 序列化 ─────────── */

    static List<Goal> parse(String raw) {
        List<Goal> out = new ArrayList<>();
        if (raw == null || raw.length() == 0) {
            return out;
        }
        for (String row : raw.split(";")) {
            String[] parts = row.split("\\|");
            if (parts.length < 6) {
                continue;
            }
            try {
                Goal goal = new Goal();
                goal.name = parts[0];
                goal.targetCents = Long.parseLong(parts[1].trim());
                goal.startStamp = Integer.parseInt(parts[2].trim());
                goal.reachedStamp = Integer.parseInt(parts[3].trim());
                goal.doneStamp = Integer.parseInt(parts[4].trim());
                goal.frozenCents = Long.parseLong(parts[5].trim());
                if (goal.targetCents > 0 && goal.name.length() > 0) {
                    out.add(goal);
                }
            } catch (NumberFormatException e) {
                // 坏行跳过，不影响其它目标
            }
        }
        return out;
    }

    static String serialize(List<Goal> goals) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < goals.size(); i++) {
            Goal goal = goals.get(i);
            if (goal == null || goal.targetCents <= 0 || goal.name.length() == 0) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(goal.name).append('|')
                    .append(goal.targetCents).append('|')
                    .append(goal.startStamp).append('|')
                    .append(goal.reachedStamp).append('|')
                    .append(goal.doneStamp).append('|')
                    .append(goal.frozenCents);
        }
        return sb.toString();
    }

    /* ─────────── 读写（含迁移） ─────────── */

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("salary_jump", 0);
    }

    /**
     * 读全部目标；顺带执行一次性迁移（旧的单目标 → 列表）。
     * 迁移幂等：跑过一次就置标记，不会重复执行。
     */
    static List<Goal> load(Context context) {
        if (context == null) {
            return new ArrayList<>();
        }
        SharedPreferences sharedPreferences = prefs(context);
        migrateIfNeeded(context, sharedPreferences);
        return parse(sharedPreferences.getString(KEY_LIST, ""));
    }

    /**
     * 把旧的 4 个扁平 key 迁移成列表格式。
     *
     * <p>只在「没迁移过 且 旧数据存在」时执行；旧的 key <b>保留不删</b>，
     * 万一新格式出问题还能回退。
     */
    static void migrateIfNeeded(Context context, SharedPreferences sharedPreferences) {
        if (sharedPreferences.getBoolean(KEY_MIGRATED, false)) {
            return;
        }
        String oldName = sharedPreferences.getString(OLD_NAME, "");
        double amount = Double.longBitsToDouble(sharedPreferences.getLong(OLD_AMOUNT, 0L));
        if (oldName == null || oldName.trim().length() == 0 || amount <= 0.0d) {
            // 没有旧目标，只需打标记
            sharedPreferences.edit().putBoolean(KEY_MIGRATED, true).apply();
            return;
        }
        Goal goal = new Goal();
        goal.name = cleanName(oldName);
        goal.targetCents = Math.round(amount * 100.0d);
        goal.startStamp = sharedPreferences.getInt(OLD_START, 0);
        if (goal.startStamp <= 0) {
            goal.startStamp = todayStamp();
        }
        goal.reachedStamp = 0;
        goal.doneStamp = 0;
        goal.frozenCents = 0L;
        // 旧的「已庆祝里程碑」不再需要（多目标下按目标单独记），但保留 key 不删

        List<Goal> list = new ArrayList<>();
        list.add(goal);
        sharedPreferences.edit()
                .putString(KEY_LIST, serialize(list))
                .putBoolean(KEY_MIGRATED, true)
                .apply();
    }

    static void save(Context context, List<Goal> goals) {
        if (context == null) {
            return;
        }
        trimDone(goals);
        prefs(context).edit()
                .putString(KEY_LIST, serialize(goals))
                .putBoolean(KEY_MIGRATED, true)
                .apply();
    }

    /** 已完成的目标只保留最近 {@link #KEEP_DONE} 个；未完成的始终保留。 */
    private static void trimDone(List<Goal> goals) {
        List<Goal> done = new ArrayList<>();
        for (int i = 0; i < goals.size(); i++) {
            if (goals.get(i).isDone()) {
                done.add(goals.get(i));
            }
        }
        while (done.size() > KEEP_DONE) {
            goals.remove(done.remove(0));
        }
    }

    /* ─────────── 常用查询 ─────────── */

    /** 当前进行中的目标；没有则返回 null。 */
    static Goal current(Context context) {
        for (Goal goal : load(context)) {
            if (!goal.isDone()) {
                return goal;
            }
        }
        return null;
    }

    /** 已完成的全部目标，最近的排前面。 */
    static List<Goal> completed(Context context) {
        List<Goal> out = new ArrayList<>();
        for (Goal goal : load(context)) {
            if (goal.isDone()) {
                out.add(goal);
            }
        }
        // 已完成的按确认日倒序（最近的在前）
        for (int i = 0; i < out.size(); i++) {
            for (int j = i + 1; j < out.size(); j++) {
                if (out.get(j).doneStamp > out.get(i).doneStamp) {
                    Goal tmp = out.get(i);
                    out.set(i, out.get(j));
                    out.set(j, tmp);
                }
            }
        }
        return out;
    }

    /**
     * 当前目标的进度（元）。
     *
     * · 已完成的目标 → 直接返回冻结金额（不再重算，避免历史跳变）<br>
     * · 进行中的目标 → 累加「开始日之后」的收入，<b>排除补录</b>（补录属于历史月份）
     */
    static double progressYuan(Context context, Goal goal) {
        if (goal == null) {
            return 0.0d;
        }
        if (goal.isDone()) {
            return goal.frozenCents / 100.0d;
        }
        return MonthBackfill.goalProgress(context, goal.startStamp);
    }

    /**
     * 累加 [fromStamp, toStamp] 区间内的收入（元），排除补录。
     *
     * <p>冻结已完成目标时用这个 —— 必须限定到「完成日」为止，
     * 否则会把完成日之后赚的钱也算进去，和下一个目标重复计算。
     */
    static double progressInRange(Context context, int fromStamp, int toStamp) {
        if (context == null || fromStamp <= 0) {
            return 0.0d;
        }
        double sum = 0.0d;
        for (SalaryHistory.Day day : SalaryHistory.load(context)) {
            if (day.stamp < fromStamp) {
                continue;
            }
            if (toStamp > 0 && day.stamp > toStamp) {
                continue;
            }
            long centsOn = day.cents - MonthBackfill.centsOn(context, day.stamp);
            if (centsOn > 0L) {
                sum += centsOn / 100.0d;
            }
        }
        return sum;
    }

    /**
     * 达标检测：进度首次 ≥ 100% 时记下达标日。
     *
     * @return 这次调用是否「刚刚达标」（用于触发庆祝）
     */
    static boolean markReachedIfNeeded(Context context, Goal goal) {
        if (context == null || goal == null || goal.isDone() || goal.isReached()) {
            return false;
        }
        double target = goal.targetCents / 100.0d;
        if (target <= 0.0d) {
            return false;
        }
        if (progressYuan(context, goal) < target) {
            return false;
        }
        // 必须改「从存储读出来的那一份」再存回去 ——
        // 直接改传进来的 goal 对象没用，它不在列表里
        List<Goal> goals = load(context);
        for (int i = 0; i < goals.size(); i++) {
            Goal item = goals.get(i);
            if (sameGoal(item, goal)) {
                item.reachedStamp = todayStamp();
                break;
            }
        }
        save(context, goals);
        return true;
    }

    /**
     * 用户点「确认完成」。
     *
     * @param doneStamp 确认日（0 或负数 → 用今天）
     */
    static void complete(Context context, Goal goal, int doneStamp) {
        if (context == null || goal == null) {
            return;
        }
        List<Goal> goals = load(context);
        for (int i = 0; i < goals.size(); i++) {
            Goal item = goals.get(i);
            if (sameGoal(item, goal)) {
                int day = doneStamp > 0 ? doneStamp : todayStamp();
                item.doneStamp = day;
                if (item.reachedStamp <= 0 || item.reachedStamp > day) {
                    item.reachedStamp = day;           // 没记过 / 记晚了，都按确认日算
                }
                // 只冻结「开始日 → 完成日」这段的钱。
                // 用 progressYuan（累加到当下）会把完成日之后赚的也算进来，
                // 导致它和下一个目标的区间重叠、总收入被算两遍。
                item.frozenCents = Math.round(
                        progressInRange(context, item.startStamp, day) * 100.0d);
                save(context, goals);
                return;
            }
        }
    }

    /**
     * 开一个新目标。
     *
     * <p>开始日：若上一个目标已完成，取「完成日次日」；否则用今天。
     * 这样两段统计区间首尾相接、<b>不重叠</b>。
     */
    static void startNew(Context context, String name, long targetCents, int startStamp) {
        if (context == null) {
            return;
        }
        List<Goal> goals = load(context);
        Goal goal = new Goal();
        goal.name = cleanName(name);
        goal.targetCents = Math.max(0L, targetCents);
        goal.startStamp = startStamp > 0 ? startStamp : suggestStartStamp(context);
        goal.reachedStamp = 0;
        goal.doneStamp = 0;
        goal.frozenCents = 0L;
        if (goal.name.length() == 0 || goal.targetCents <= 0L) {
            return;
        }
        goals.add(goal);
        save(context, goals);
    }

    /** 两个 Goal 是否指向同一条记录（列表里没有唯一 id，按内容比）。 */
    static boolean sameGoal(Goal a, Goal b) {
        if (a == null || b == null) {
            return false;
        }
        return a.targetCents == b.targetCents
                && a.startStamp == b.startStamp
                && a.name.equals(b.name);
    }

    /** 建议的新目标开始日：上一个已完成目标的次日；没有就用今天。 */
    static int suggestStartStamp(Context context) {
        List<Goal> done = completed(context);
        if (!done.isEmpty()) {
            return nextDay(done.get(0).doneStamp);
        }
        for (Goal goal : load(context)) {
            if (!goal.isDone() && goal.startStamp > 0) {
                return goal.startStamp;            // 已有进行中的目标，不该再开
            }
        }
        return todayStamp();
    }

    /** 清空全部目标（设置里「清除目标」用）。 */
    static void clear(Context context) {
        if (context == null) {
            return;
        }
        prefs(context).edit().putString(KEY_LIST, "").putBoolean(KEY_MIGRATED, true).apply();
    }

    /** 把旧的「已庆祝」标记复位（换目标后里程碑要重新庆祝）。 */
    static void resetCelebrate(Context context) {
        if (context == null) {
            return;
        }
        prefs(context).edit().putInt(OLD_CELEBRATED, 0).putBoolean(KEY_MIGRATED, true).apply();
    }

    /* ─────────── 日期工具 ─────────── */

    static int todayStamp() {
        return stamp(Calendar.getInstance());
    }

    static int stamp(Calendar calendar) {
        return (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
    }

    static int nextDay(int stamp) {
        if (stamp <= 0) {
            return todayStamp();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(stamp / 10000, ((stamp / 100) % 100) - 1, stamp % 100);
        calendar.add(5, 1);
        return stamp(calendar);
    }

    /** 两个日期戳之间隔几天（用于算「用时」）。 */
    static int daysBetween(int fromStamp, int toStamp) {
        if (fromStamp <= 0 || toStamp <= 0) {
            return 0;
        }
        Calendar from = Calendar.getInstance();
        from.set(fromStamp / 10000, ((fromStamp / 100) % 100) - 1, fromStamp % 100, 0, 0, 0);
        from.set(14, 0);
        Calendar to = Calendar.getInstance();
        to.set(toStamp / 10000, ((toStamp / 100) % 100) - 1, toStamp % 100, 0, 0, 0);
        to.set(14, 0);
        long diff = (to.getTimeInMillis() - from.getTimeInMillis()) / 86400000L;
        return (int) Math.max(0L, diff);
    }

    /** 日期戳 → 中文短日期，如「2026 年 9 月 1 日」。 */
    static String formatStamp(int stamp) {
        if (stamp <= 0) {
            return "—";
        }
        return (stamp / 10000) + " 年 " + ((stamp / 100) % 100) + " 月 " + (stamp % 100) + " 日";
    }

    /** 日期戳 →「9 月 1 日」。 */
    static String formatStampShort(int stamp) {
        if (stamp <= 0) {
            return "—";
        }
        return ((stamp / 100) % 100) + " 月 " + (stamp % 100) + " 日";
    }
}
