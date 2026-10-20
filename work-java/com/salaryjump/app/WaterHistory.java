package com.salaryjump.app;

import android.content.SharedPreferences;
import com.salaryjump.app.WaterReminderScheduler;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/* loaded from: classes.dex */
public final class WaterHistory {
    private static final int KEEP_DAYS = 365;
    private static final String KEY = "water_history";

    private WaterHistory() {
    }

    static int dayStamp(String str) {
        if (str == null) {
            return 0;
        }
        try {
            return Integer.parseInt(str.replace("-", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static void archive(SharedPreferences sharedPreferences, String str, List<WaterReminderScheduler.WaterRecord> list) {
        int dayStamp;
        if (str == null || str.length() == 0 || list == null || list.isEmpty() || (dayStamp = dayStamp(str)) == 0) {
            return;
        }
        int[] iArr = new int[32];
        int[] iArr2 = new int[32];
        for (WaterReminderScheduler.WaterRecord waterRecord : list) {
            int clamp = DrinkTypes.clamp(waterRecord.type);
            iArr[clamp] = iArr[clamp] + waterRecord.amountMl;
            iArr2[clamp] = iArr2[clamp] + 1;
        }
        StringBuilder sb = new StringBuilder(sharedPreferences.getString(KEY, ""));
        for (int i = 0; i < 32; i++) {
            if (iArr2[i] > 0) {
                if (sb.length() > 0) {
                    sb.append(';');
                }
                sb.append(dayStamp).append(':').append(i).append(':').append(iArr[i]).append(':').append(iArr2[i]);
            }
        }
        sharedPreferences.edit().putString(KEY, trim(sb.toString())).apply();
    }

    private static String trim(String str) {
        boolean z;
        Calendar calendar = Calendar.getInstance();
        calendar.add(6, -365);
        int i = (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
        StringBuilder sb = new StringBuilder();
        for (String str2 : str.split(";")) {
            String[] split = str2.split(":");
            if (split.length == 4) {
                try {
                    z = Integer.parseInt(split[0]) >= i;
                } catch (NumberFormatException e) {
                    z = false;
                }
                if (z) {
                    if (sb.length() > 0) {
                        sb.append(';');
                    }
                    sb.append(str2);
                }
            }
        }
        return sb.toString();
    }

    static HashMap<Integer, Integer> query(SharedPreferences sharedPreferences, int i, int i2, int[] iArr, int[] iArr2) {
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        String string = sharedPreferences.getString(KEY, "");
        if (string == null || string.length() == 0) {
            return hashMap;
        }
        for (String str : string.split(";")) {
            String[] split = str.split(":");
            if (split.length == 4) {
                try {
                    int parseInt = Integer.parseInt(split[0]);
                    if (parseInt >= i && parseInt <= i2) {
                        int clamp = DrinkTypes.clamp(Integer.parseInt(split[1]));
                        int parseInt2 = Integer.parseInt(split[2]);
                        int parseInt3 = Integer.parseInt(split[3]);
                        iArr[clamp] = iArr[clamp] + parseInt2;
                        iArr2[clamp] = iArr2[clamp] + parseInt3;
                        Integer num = hashMap.get(Integer.valueOf(parseInt));
                        hashMap.put(Integer.valueOf(parseInt), Integer.valueOf((num == null ? 0 : num.intValue()) + parseInt2));
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return hashMap;
    }
}
