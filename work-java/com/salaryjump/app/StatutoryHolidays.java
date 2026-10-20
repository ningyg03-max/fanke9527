package com.salaryjump.app;

import android.content.Context;
import java.util.Calendar;
import java.util.HashMap;

/* loaded from: classes.dex */
public final class StatutoryHolidays {
    private static final String DATA = "20250101;20250128;20250129;20250130;20250131;20250404;20250501;20250502;20250531;20251006;20251001;20251002;20251003;20260101;20260216;20260217;20260218;20260219;20260404;20260501;20260502;20260619;20260925;20261001;20261002;20261003";
    private static HashMap<Integer, Boolean> online;
    private static HashMap<Integer, Boolean> table;

    private StatutoryHolidays() {
    }

    static void applyOnlineData(Context context) {
        if (context == null) {
            return;
        }
        HashMap<Integer, Boolean> hashMap = new HashMap<>();
        try {
            String string = context.getSharedPreferences("salary_jump", 0).getString("statutory_data", null);
            if (string != null && string.length() > 0) {
                for (String str : string.split(";")) {
                    try {
                        hashMap.put(Integer.valueOf(Integer.parseInt(str.trim())), Boolean.TRUE);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        } catch (Throwable th) {
        }
        if (hashMap.isEmpty()) {
            hashMap = null;
        }
        online = hashMap;
    }

    private static void ensure() {
        if (table != null) {
            return;
        }
        HashMap<Integer, Boolean> hashMap = new HashMap<>();
        for (String str : DATA.split(";")) {
            try {
                hashMap.put(Integer.valueOf(Integer.parseInt(str.trim())), Boolean.TRUE);
            } catch (NumberFormatException e) {
            }
        }
        table = hashMap;
    }

    static boolean isStatutory(Calendar calendar) {
        return isStatutory(WorkdayHelper.stamp(calendar));
    }

    static boolean isStatutory(int i) {
        ensure();
        if (table.containsKey(Integer.valueOf(i))) {
            return true;
        }
        return online != null && online.containsKey(Integer.valueOf(i));
    }

    static int count() {
        ensure();
        return table.size();
    }
}
