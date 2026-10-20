package com.salaryjump.app;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 自动备份的纯逻辑：文件名、周期判断、保留份数。
 *
 * <p>抽出来的原因：这些规则算错了会丢备份，而它们跟 Android 系统无关，
 * 放在这里可以完整回归测试。系统交互（选目录、闹钟）另在 {@link BackupScheduler}。
 */
final class BackupPlan {
    /** 文件名前缀 */
    static final String PREFIX = "SalaryJump-";
    static final String SUFFIX = ".json";

    /* ─────────── 周期选项 ─────────── */
    static final int EVERY_DAY = 0;
    static final int EVERY_WEEK = 1;
    static final int EVERY_MONTH = 2;
    static final int[] PERIODS = {EVERY_DAY, EVERY_WEEK, EVERY_MONTH};

    /* ─────────── 保留份数选项 ─────────── */
    static final int KEEP_7 = 0;
    static final int KEEP_30 = 1;
    static final int[] KEEP_OPTIONS = {7, 30};

    private BackupPlan() {
    }

    static String periodName(int period) {
        if (period == EVERY_WEEK) {
            return "每周";
        }
        if (period == EVERY_MONTH) {
            return "每月";
        }
        return "每天";
    }

    static int keepCount(int option) {
        return (option >= 0 && option < KEEP_OPTIONS.length) ? KEEP_OPTIONS[option] : 7;
    }

    static String keepName(int option) {
        return "最近 " + keepCount(option) + " 份";
    }

    /* ─────────── 文件名 ─────────── */

    /** 日期戳 → 备份文件名，如 {@code SalaryJump-20261015.json}。 */
    static String fileNameFor(int stamp) {
        if (stamp <= 0) {
            return PREFIX + "unknown" + SUFFIX;
        }
        return PREFIX + stamp + SUFFIX;
    }

    /** 从文件名解析出日期戳；不是本 App 的备份文件返回 0。 */
    static int stampOf(String fileName) {
        if (fileName == null || !fileName.startsWith(PREFIX) || !fileName.endsWith(SUFFIX)) {
            return 0;
        }
        String body = fileName.substring(PREFIX.length(), fileName.length() - SUFFIX.length());
        try {
            int stamp = Integer.parseInt(body.trim());
            return stamp > 0 ? stamp : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 该删掉哪些备份文件（保留最近的 N 份）。
     *
     * @param names 目录里所有文件名
     * @return 要删除的文件名（按日期从旧到新）
     */
    static List<String> filesToPrune(List<String> names, int keep) {
        List<String> ours = new ArrayList<>();
        if (names != null) {
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                if (stampOf(name) > 0 && !ours.contains(name)) {
                    ours.add(name);
                }
            }
        }
        // 按日期升序（旧的在前）
        for (int i = 0; i < ours.size(); i++) {
            for (int j = i + 1; j < ours.size(); j++) {
                if (stampOf(ours.get(j)) < stampOf(ours.get(i))) {
                    String tmp = ours.get(i);
                    ours.set(i, ours.get(j));
                    ours.set(j, tmp);
                }
            }
        }
        List<String> out = new ArrayList<>();
        int excess = ours.size() - Math.max(1, keep);
        for (int i = 0; i < excess; i++) {
            out.add(ours.get(i));
        }
        return out;
    }

    /* ─────────── 该不该备份 ─────────── */

    /** 两个日期戳隔了几天（按自然日算，忽略时分秒）。 */
    static int daysBetween(int fromStamp, int toStamp) {
        return GoalStore.daysBetween(fromStamp, toStamp);
    }

    /**
     * 距下次该备份的时间（毫秒）。0 表示已经该备了。
     *
     * @param lastBackupAtMs 上次备份时间戳（0 → 从没备过，立刻该备）
     * @param period         周期（EVERY_DAY / WEEK / MONTH）
     */
    static long msUntilDue(long lastBackupAtMs, long nowMs, int period) {
        if (lastBackupAtMs <= 0L) {
            return 0L;
        }
        long interval = intervalMs(period);
        long due = lastBackupAtMs + interval;
        return Math.max(0L, due - nowMs);
    }

    /** 周期对应的间隔毫秒数。 */
    static long intervalMs(int period) {
        if (period == EVERY_WEEK) {
            return 7L * 24L * 3600L * 1000L;
        }
        if (period == EVERY_MONTH) {
            return 30L * 24L * 3600L * 1000L;
        }
        return 24L * 3600L * 1000L;
    }

    /**
     * 打开 App 时的补跑判断：距上次备份是否已经超过周期。
     *
     * <p>定时闹钟可能被 ROM 杀掉，所以每次打开 App 都要检查一次。
     */
    static boolean shouldCatchUp(long lastBackupAtMs, long nowMs, int period) {
        return msUntilDue(lastBackupAtMs, nowMs, period) <= 0L;
    }

    /** 计算今天指定时间点的时间戳（毫秒）。 */
    static long todayAt(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(11, hour);
        calendar.set(12, minute);
        calendar.set(13, 0);
        calendar.set(14, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 下一个备份时刻：今天 hour:minute；若已过，则顺延一个周期。
     */
    static long nextTriggerAt(long nowMs, int hour, int minute, int period) {
        long today = todayAt(hour, minute);
        if (today > nowMs) {
            return today;
        }
        return today + intervalMs(period);
    }

    /** 把时间戳格式化成「今天 22:00」「10 月 15 日 22:00」这类可读文案。 */
    static String describeAt(int hour, int minute) {
        return String.format("%02d:%02d", Integer.valueOf(hour), Integer.valueOf(minute));
    }
}
