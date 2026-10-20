package com.salaryjump.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 自动备份的闹钟接收器。
 *
 * <p>由 {@link BackupScheduler#schedule} 排定，到点触发一次备份。
 */
public class BackupReceiver extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            return;
        }
        try {
            String action = intent == null ? null : intent.getAction();
            if ("android.intent.action.BOOT_COMPLETED".equals(action)
                    || "android.intent.action.TIME_SET".equals(action)
                    || "android.intent.action.TIMEZONE_CHANGED".equals(action)) {
                // 开机 / 改时间后闹钟会被清掉，重新排一次
                BackupScheduler.schedule(context);
                return;
            }
            BackupScheduler.runBackup(context, true);
        } catch (Throwable unused) {
            // 备份失败不能影响系统（接收器里崩溃会被系统记录）
        }
    }
}
