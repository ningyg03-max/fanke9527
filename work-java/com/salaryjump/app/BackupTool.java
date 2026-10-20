package com.salaryjump.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class BackupTool {
    static final int REQ_EXPORT = 4302;
    static final int REQ_IMPORT = 4303;

    private BackupTool() {
    }

    static void export(Context context, Uri uri) {
        try {
            OutputStream openOutputStream = context.getContentResolver().openOutputStream(uri);
            if (openOutputStream == null) {
                Toast.makeText(context, "无法写入该位置", 1).show();
                return;
            }
            String json = buildJson(context);
            openOutputStream.write(json.getBytes("UTF-8"));
            openOutputStream.close();
            Toast.makeText(context, "已导出 "
                    + context.getSharedPreferences("salary_jump", 0).getAll().size() + " 项数据", 0).show();
        } catch (Throwable th) {
            Toast.makeText(context, "导出失败：" + th.getMessage(), 1).show();
        }
    }

    static void restore(final Context context, Uri uri) {
        try {
            InputStream openInputStream = context.getContentResolver().openInputStream(uri);
            if (openInputStream == null) {
                Toast.makeText(context, "无法读取该文件", 1).show();
                return;
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bArr = new byte[4096];
            while (true) {
                int read = openInputStream.read(bArr);
                if (read <= 0) {
                    break;
                } else {
                    byteArrayOutputStream.write(bArr, 0, read);
                }
            }
            openInputStream.close();
            final JSONObject optJSONObject = new JSONObject(new String(byteArrayOutputStream.toByteArray(), "UTF-8")).optJSONObject("data");
            if (optJSONObject == null) {
                Toast.makeText(context, "文件格式不对，不是 SalaryJump 的备份", 1).show();
                return;
            }
            final int length = optJSONObject.length();
            new AlertDialog.Builder(context).setTitle("恢复数据").setMessage("将从备份恢复 " + length + " 项数据，覆盖当前设置与记录。\n\n此操作不可撤销，确定继续？").setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("恢复", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.BackupTool.1
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    BackupTool.applyData(context, optJSONObject, length);
                }
            }).show();
        } catch (Throwable th) {
            Toast.makeText(context, "读取失败：" + th.getMessage(), 1).show();
        }
    }

    /** 把全部偏好序列化成备份 JSON（手动导出与自动备份共用同一份格式）。 */
    static String buildJson(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        StringBuilder sb = new StringBuilder();
        sb.append("{\"app\":\"SalaryJump\",\"version\":2,\"savedAt\":")
                .append(System.currentTimeMillis()).append(",\"data\":{");
        boolean first = true;
        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (!first) {
                sb.append(',');
            }
            sb.append('"').append(escapeJson(entry.getKey())).append("\":");
            if (value instanceof String) {
                sb.append('"').append(escapeJson((String) value)).append('"');
            } else {
                sb.append(value.toString());
            }
            first = false;
        }
        sb.append("}}");
        return sb.toString();
    }

    /**
     * 把备份写到指定 Uri（自动备份用）。
     *
     * @return 是否成功
     */
    static boolean writeJsonTo(Context context, Uri uri) {
        OutputStream out = null;
        try {
            out = context.getContentResolver().openOutputStream(uri, "wt");
            if (out == null) {
                return false;
            }
            out.write(buildJson(context).getBytes("UTF-8"));
            out.flush();
            return true;
        } catch (Throwable unused) {
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Throwable unused) {
                }
            }
        }
    }

    static void applyData(Context context, JSONObject jSONObject, int i) {
        SharedPreferences.Editor edit = context.getSharedPreferences("salary_jump", 0).edit();
        Iterator<String> keys = jSONObject.keys();
        while (keys.hasNext()) {
            String next = keys.next();
            Object opt = jSONObject.opt(next);
            if (opt instanceof String) {
                edit.putString(next, (String) opt);
            } else if (opt instanceof Boolean) {
                edit.putBoolean(next, ((Boolean) opt).booleanValue());
            } else if (opt instanceof Integer) {
                edit.putInt(next, ((Integer) opt).intValue());
            } else if (opt instanceof Long) {
                edit.putLong(next, ((Long) opt).longValue());
            } else if (opt instanceof Number) {
                edit.putLong(next, ((Number) opt).longValue());
            }
        }
        edit.apply();
        WorkdayHelper.invalidate();
        WaterReminderScheduler.scheduleNext(context);
        SalaryWidgetProvider.refreshAll(context);
        Toast.makeText(context, "已恢复 " + i + " 项数据", 0).show();
    }

    private static String escapeJson(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (charAt == '\"') {
                sb.append("\\\"");
            } else if (charAt == '\\') {
                sb.append("\\\\");
            } else if (charAt == '\n') {
                sb.append("\\n");
            } else if (charAt == '\r') {
                sb.append("\\r");
            } else if (charAt == '\t') {
                sb.append("\\t");
            } else if (charAt < ' ') {
                sb.append(String.format("\\u%04x", Integer.valueOf(charAt)));
            } else {
                sb.append(charAt);
            }
        }
        return sb.toString();
    }
}
