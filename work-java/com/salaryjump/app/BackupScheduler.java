package com.salaryjump.app;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.Settings;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动备份的调度与执行。
 *
 * <h3>为什么用 AlarmManager 而不是 WorkManager</h3>
 * 本工程没有 Gradle，加依赖麻烦；而 {@code AlarmManager} 的套路
 * ({@link PaydayReminder} / {@link WaterReminderScheduler}) 已经在用，
 * 权限与开机重启接收器都是现成的。
 *
 * <h3>「指定路径」是怎么实现的</h3>
 * Android 10+ 的分区存储不允许直接写任意路径，
 * 所以走 <b>SAF 目录授权</b>：用户选一次文件夹，
 * 用 {@code takePersistableUriPermission} 永久记住，之后自动往里面写。
 *
 * <h3>ROM 会杀后台怎么办</h3>
 * 定时闹钟可能不触发（国产 ROM / 三星的后台限制）。
 * 因此额外做「打开 App 时补跑」({@link #catchUpIfDue})。
 */
final class BackupScheduler {
    static final String KEY_ENABLED = "backup_auto_enabled";
    static final String KEY_TREE_URI = "backup_tree_uri";
    static final String KEY_TREE_LABEL = "backup_tree_label";
    static final String KEY_PERIOD = "backup_period";
    static final String KEY_KEEP = "backup_keep";
    static final String KEY_HOUR = "backup_hour";
    static final String KEY_MINUTE = "backup_minute";
    static final String KEY_LAST_AT = "backup_last_at";
    static final String KEY_LAST_STATE = "backup_last_state";

    private static final int REQ_ALARM = 4401;
    private static final String CHANNEL_ID = "backup_result";
    private static final int NOTIFY_ID = 4402;

    private BackupScheduler() {
    }

    /* ─────────── 设置读写 ─────────── */

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("salary_jump", 0);
    }

    static boolean isEnabled(Context context) {
        return context != null && prefs(context).getBoolean(KEY_ENABLED, false);
    }

    static String treeUri(Context context) {
        return context == null ? "" : prefs(context).getString(KEY_TREE_URI, "");
    }

    static String treeLabel(Context context) {
        return context == null ? "" : prefs(context).getString(KEY_TREE_LABEL, "");
    }

    static int period(Context context) {
        return context == null ? BackupPlan.EVERY_DAY : prefs(context).getInt(KEY_PERIOD, BackupPlan.EVERY_DAY);
    }

    static int keepOption(Context context) {
        return context == null ? BackupPlan.KEEP_7 : prefs(context).getInt(KEY_KEEP, BackupPlan.KEEP_7);
    }

    static int hour(Context context) {
        return context == null ? 22 : prefs(context).getInt(KEY_HOUR, 22);
    }

    static int minute(Context context) {
        return context == null ? 0 : prefs(context).getInt(KEY_MINUTE, 0);
    }

    static long lastBackupAt(Context context) {
        return context == null ? 0L : prefs(context).getLong(KEY_LAST_AT, 0L);
    }

    static String lastState(Context context) {
        return context == null ? "" : prefs(context).getString(KEY_LAST_STATE, "");
    }

    /** 用户选好目录后的完整流程（持久化授权 + 保存 + 开启）。
     *
     * @return 成功返回可展示的目录名；失败返回 null
     */
    static String adoptTree(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        try {
            // 关键：把授权长期记住，之后自动备份才不用再弹选择框
            context.getContentResolver().takePersistableUriPermission(uri, 3);
        } catch (Throwable unused) {
            // 某些系统/目录不支持持久化 —— 仍然记住，只是重启后可能要重选
        }
        try {
            String label = uri.getLastPathSegment();
            if (label == null || label.length() == 0) {
                label = uri.toString();
            }
            int colon = label.lastIndexOf(':');
            if (colon >= 0 && colon + 1 < label.length()) {
                label = label.substring(colon + 1);
            }
            setTree(context, uri, label);
            setEnabled(context, true);
            return label;
        } catch (Throwable unused) {
            return null;
        }
    }

    /** 目录选好后调用：记住授权并重排闹钟。 */
    static void setTree(Context context, Uri uri, String label) {
        if (context == null || uri == null) {
            return;
        }
        prefs(context).edit()
                .putString(KEY_TREE_URI, uri.toString())
                .putString(KEY_TREE_LABEL, label == null ? "" : label)
                .apply();
        schedule(context);
    }

    static void setOption(Context context, String key, int value) {
        if (context == null) {
            return;
        }
        prefs(context).edit().putInt(key, value).apply();
        schedule(context);
    }

    static void setEnabled(Context context, boolean enabled) {
        if (context == null) {
            return;
        }
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
        if (enabled) {
            schedule(context);
        } else {
            cancel(context);
        }
    }

    /* ─────────── 闹钟 ─────────── */

    static void schedule(Context context) {
        if (context == null || !isEnabled(context)) {
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (alarmManager == null) {
            return;
        }
        long triggerAt = BackupPlan.nextTriggerAt(System.currentTimeMillis(),
                hour(context), minute(context), period(context));
        PendingIntent pendingIntent = alarmIntent(context);
        try {
            if (Build.VERSION.SDK_INT >= 31 && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(0, triggerAt, pendingIntent);
            } else {
                alarmManager.setAndAllowWhileIdle(0, triggerAt, pendingIntent);
            }
        } catch (Throwable th) {
            try {
                alarmManager.set(0, triggerAt, pendingIntent);
            } catch (Throwable unused) {
            }
        }
    }

    static void cancel(Context context) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
            if (alarmManager != null) {
                alarmManager.cancel(alarmIntent(context));
            }
        } catch (Throwable unused) {
        }
    }

    private static PendingIntent alarmIntent(Context context) {
        Intent intent = new Intent(context, BackupReceiver.class);
        intent.setAction("com.salaryjump.app.BACKUP_ALARM");
        int flags = Build.VERSION.SDK_INT >= 23 ? 201326592 : 134217728;  // FLAG_IMMUTABLE|UPDATE_CURRENT
        return PendingIntent.getBroadcast(context, REQ_ALARM, intent, flags);
    }

    /* ─────────── 补跑（定时被 ROM 杀掉时的兜底） ─────────── */

    /**
     * 打开 App 时调用：距上次备份超过周期就立刻补一次。
     *
     * @return 是否触发了补跑
     */
    static boolean catchUpIfDue(Context context) {
        if (context == null || !isEnabled(context) || treeUri(context).length() == 0) {
            return false;
        }
        long last = lastBackupAt(context);
        if (!BackupPlan.shouldCatchUp(last, System.currentTimeMillis(), period(context))) {
            return false;
        }
        runBackup(context, true);
        return true;
    }

    /* ─────────── 执行备份 ─────────── */

    /**
     * 执行一次备份。
     *
     * @param notifyResult 是否发通知告知结果（定时备份发，手动备份也发）
     */
    static void runBackup(Context context, boolean notifyResult) {
        if (context == null) {
            return;
        }
        String tree = treeUri(context);
        if (tree.length() == 0) {
            record(context, "还没选备份文件夹", notifyResult);
            return;
        }
        try {
            Uri treeUri = Uri.parse(tree);
            String fileName = BackupPlan.fileNameFor(GoalStore.todayStamp());
            Uri target = createFileUnder(context, treeUri, fileName);
            if (target == null) {
                record(context, "无法写入备份文件夹（可能需要重新选择）", notifyResult);
                return;
            }
            if (!BackupTool.writeJsonTo(context, target)) {
                record(context, "导出失败", notifyResult);
                return;
            }
            prune(context, treeUri);
            long now = System.currentTimeMillis();
            prefs(context).edit()
                    .putLong(KEY_LAST_AT, now)
                    .putString(KEY_LAST_STATE, "成功 · " + fileName)
                    .apply();
            if (notifyResult) {
                notify(context, "备份完成", fileName);
            }
        } catch (Throwable th) {
            record(context, "备份失败：" + th.getClass().getSimpleName(), notifyResult);
        } finally {
            // 无论成功失败都重排下一次，避免一次失败后彻底不跑了
            schedule(context);
        }
    }

    private static void record(Context context, String state, boolean notifyResult) {
        prefs(context).edit()
                .putString(KEY_LAST_STATE, state)
                .putLong(KEY_LAST_AT, System.currentTimeMillis())
                .apply();
        if (notifyResult) {
            notify(context, "自动备份失败", state);
        }
    }

    /** 在授权的目录下建文件（同名则覆盖）。 */
    private static Uri createFileUnder(Context context, Uri treeUri, String fileName) {
        try {
            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri,
                    DocumentsContract.getTreeDocumentId(treeUri));
            Uri created = DocumentsContract.createDocument(
                    context.getContentResolver(), docUri, "application/json", fileName);
            if (created != null) {
                return created;
            }
        } catch (Throwable unused) {
        }
        // 回退：直接拼一个子文档路径
        try {
            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri,
                    DocumentsContract.getTreeDocumentId(treeUri) + "/" + fileName);
            OutputStream out = context.getContentResolver().openOutputStream(docUri, "wt");
            if (out != null) {
                out.close();
                return docUri;
            }
        } catch (Throwable unused) {
        }
        return null;
    }

    /** 按「保留份数」删掉最旧的备份。 */
    private static void prune(Context context, Uri treeUri) {
        try {
            int keep = BackupPlan.keepCount(keepOption(context));
            Uri children = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri,
                    DocumentsContract.getTreeDocumentId(treeUri));
            List<String> names = new ArrayList<>();
            List<String> ids = new ArrayList<>();
            Cursor cursor = context.getContentResolver().query(children,
                    new String[]{"document_id", "display_name"}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    names.add(cursor.getString(1));
                    ids.add(cursor.getString(0));
                }
                cursor.close();
            }
            List<String> doomed = BackupPlan.filesToPrune(names, keep);
            for (int i = 0; i < doomed.size(); i++) {
                int index = names.indexOf(doomed.get(i));
                if (index < 0) {
                    continue;
                }
                Uri docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, ids.get(index));
                DocumentsContract.deleteDocument(context.getContentResolver(), docUri);
            }
        } catch (Throwable unused) {
        }
    }

    /* ─────────── 通知 ─────────── */

    private static void notify(Context context, String title, String body) {
        try {
            NotificationManager manager = (NotificationManager) context.getSystemService("notification");
            if (manager == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        "备份结果", 2);
                channel.setDescription("自动备份成功或失败时通知");
                manager.createNotificationChannel(channel);
            }
            Notification.Builder builder = Build.VERSION.SDK_INT >= 26
                    ? new Notification.Builder(context, CHANNEL_ID)
                    : new Notification.Builder(context);
            builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(true);
            PendingIntent contentIntent = PendingIntent.getActivity(context, NOTIFY_ID + 1,
                    new Intent(context, MainActivity.class),
                    Build.VERSION.SDK_INT >= 23 ? 201326592 : 134217728);
            builder.setContentIntent(contentIntent);
            manager.notify(NOTIFY_ID, builder.build());
        } catch (Throwable unused) {
        }
    }

    /** 系统是否允许精确闹钟（设置界面用来提示）。 */
    static boolean canScheduleExact(Context context) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
            return alarmManager != null
                    && (Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms());
        } catch (Throwable unused) {
            return false;
        }
    }

    /** 拉起系统的精确闹钟授权页。 */
    static Intent exactAlarmSettingsIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        return intent;
    }
}
