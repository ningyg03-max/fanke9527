package com.salaryjump.app;

import android.content.Context;
import java.lang.Thread;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/* loaded from: classes.dex */
public final class CrashLogger {
    private static final String KEY_AT = "last_crash_at";
    private static final String KEY_TEXT = "last_crash_text";
    private static final int MAX_LEN = 6000;
    private static boolean installed;

    private CrashLogger() {
    }

    static void install(Context context) {
        if (installed || context == null) {
            return;
        }
        installed = true;
        final Context applicationContext = context.getApplicationContext();
        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() { // from class: com.salaryjump.app.CrashLogger.1
            @Override // java.lang.Thread.UncaughtExceptionHandler
            public void uncaughtException(Thread thread, Throwable th) {
                try {
                    CrashLogger.save(applicationContext, thread, th);
                } catch (Throwable th2) {
                }
                if (defaultUncaughtExceptionHandler != null) {
                    defaultUncaughtExceptionHandler.uncaughtException(thread, th);
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void save(Context context, Thread thread, Throwable th) {
        StringBuilder sb = new StringBuilder();
        sb.append("时间: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date())).append('\n');
        sb.append("线程: ").append(thread == null ? "?" : thread.getName()).append('\n');
        sb.append("异常: ").append(th.getClass().getName()).append('\n');
        sb.append("信息: ").append(th.getMessage()).append("\n\n");
        appendTrace(sb, th, 0);
        String sb2 = sb.toString();
        if (sb2.length() > MAX_LEN) {
            sb2 = sb2.substring(0, MAX_LEN) + "\n…（已截断）";
        }
        context.getSharedPreferences("salary_jump", 0).edit().putLong(KEY_AT, System.currentTimeMillis()).putString(KEY_TEXT, sb2).commit();
    }

    private static void appendTrace(StringBuilder sb, Throwable th, int i) {
        if (th == null || i > 4) {
            return;
        }
        for (StackTraceElement stackTraceElement : th.getStackTrace()) {
            sb.append("    at ").append(stackTraceElement.toString()).append('\n');
        }
        Throwable cause = th.getCause();
        if (cause != null) {
            sb.append("  Caused by: ").append(cause.getClass().getName()).append(": ").append(cause.getMessage()).append('\n');
            appendTrace(sb, cause, i + 1);
        }
    }

    static String lastCrashText(Context context) {
        if (context == null) {
            return null;
        }
        try {
            String string = context.getSharedPreferences("salary_jump", 0).getString(KEY_TEXT, null);
            if (string == null) {
                return null;
            }
            if (string.length() == 0) {
                return null;
            }
            return string;
        } catch (Throwable th) {
            return null;
        }
    }

    static long lastCrashAt(Context context) {
        if (context == null) {
            return 0L;
        }
        try {
            return context.getSharedPreferences("salary_jump", 0).getLong(KEY_AT, 0L);
        } catch (Throwable th) {
            return 0L;
        }
    }

    static String summary(Context context) {
        String lastCrashText = lastCrashText(context);
        if (lastCrashText == null) {
            return null;
        }
        String str = "";
        String str2 = str;
        for (String str3 : lastCrashText.split("\n")) {
            if (str3.startsWith("异常: ")) {
                str = str3.substring(4).trim();
            } else if (str3.startsWith("信息: ")) {
                str2 = str3.substring(4).trim();
            }
        }
        int lastIndexOf = str.lastIndexOf(46);
        if (lastIndexOf >= 0) {
            str = str.substring(lastIndexOf + 1);
        }
        String str4 = str + (str2.length() > 0 ? ": " + str2 : "");
        if (str4.length() > 60) {
            return str4.substring(0, 60) + "…";
        }
        return str4;
    }

    static void clear(Context context) {
        if (context == null) {
            return;
        }
        context.getSharedPreferences("salary_jump", 0).edit().remove(KEY_AT).remove(KEY_TEXT).commit();
    }
}
