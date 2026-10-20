package com.salaryjump.app;

import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;
import com.salaryjump.app.MainActivity;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

/* loaded from: classes.dex */
public final class WorkdayHelper {
    private static final String BUILTIN = "20250101:1:元旦;20250126:0:春节;20250128:1:春节;20250129:1:春节;20250130:1:春节;20250131:1:春节;20250201:1:春节;20250202:1:春节;20250203:1:春节;20250204:1:春节;20250208:0:春节;20250404:1:清明节;20250405:1:清明节;20250406:1:清明节;20250427:0:劳动节;20250501:1:劳动节;20250502:1:劳动节;20250503:1:劳动节;20250504:1:劳动节;20250505:1:劳动节;20250531:1:端午节;20250601:1:端午节;20250602:1:端午节;20250928:0:国庆节、中秋节;20251001:1:国庆节、中秋节;20251002:1:国庆节、中秋节;20251003:1:国庆节、中秋节;20251004:1:国庆节、中秋节;20251005:1:国庆节、中秋节;20251006:1:国庆节、中秋节;20251007:1:国庆节、中秋节;20251008:1:国庆节、中秋节;20251011:0:国庆节、中秋节;20260101:1:元旦;20260102:1:元旦;20260103:1:元旦;20260104:0:元旦;20260214:0:春节;20260215:1:春节;20260216:1:春节;20260217:1:春节;20260218:1:春节;20260219:1:春节;20260220:1:春节;20260221:1:春节;20260222:1:春节;20260223:1:春节;20260228:0:春节;20260404:1:清明节;20260405:1:清明节;20260406:1:清明节;20260501:1:劳动节;20260502:1:劳动节;20260503:1:劳动节;20260504:1:劳动节;20260505:1:劳动节;20260509:0:劳动节;20260619:1:端午节;20260620:1:端午节;20260621:1:端午节;20260920:0:国庆节;20260925:1:中秋节;20260926:1:中秋节;20260927:1:中秋节;20261001:1:国庆节;20261002:1:国庆节;20261003:1:国庆节;20261004:1:国庆节;20261005:1:国庆节;20261006:1:国庆节;20261007:1:国庆节;20261010:0:国庆节";
    private static final long CAL_TTL_MS = 900000;
    private static HashMap<Integer, String> builtinRest;
    private static HashMap<Integer, String> builtinWork;
    private static long calCacheAt;
    private static int calCacheDay;
    private static HashMap<Integer, String> calRest;
    private static HashMap<Integer, String> calWork;
    private static String onlineData;

    private WorkdayHelper() {
    }

    static int stamp(Calendar calendar) {
        return (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
    }

    private static void ensureBuiltin() {
        if (builtinRest != null) {
            return;
        }
        HashMap<Integer, String> hashMap = new HashMap<>();
        HashMap<Integer, String> hashMap2 = new HashMap<>();
        parseInto(hashMap, hashMap2, BUILTIN);
        if (onlineData != null) {
            parseInto(hashMap, hashMap2, onlineData);
        }
        builtinRest = hashMap;
        builtinWork = hashMap2;
    }

    static void applyOnlineData(Context context) {
        if (context == null) {
            return;
        }
        try {
            onlineData = context.getSharedPreferences("salary_jump", 0).getString("holiday_data", null);
        } catch (Throwable th) {
            onlineData = null;
        }
        builtinRest = null;
        builtinWork = null;
    }

    private static void parseInto(HashMap<Integer, String> hashMap, HashMap<Integer, String> hashMap2, String str) {
        for (String str2 : str.split(";")) {
            String[] split = str2.split(":");
            if (split.length == 3) {
                try {
                    int parseInt = Integer.parseInt(split[0]);
                    if ("1".equals(split[1])) {
                        hashMap.put(Integer.valueOf(parseInt), split[2]);
                        hashMap2.remove(Integer.valueOf(parseInt));
                    } else {
                        hashMap2.put(Integer.valueOf(parseInt), split[2]);
                        hashMap.remove(Integer.valueOf(parseInt));
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
    }

    static void invalidate() {
        calCacheAt = 0L;
        calCacheDay = 0;
        builtinRest = null;
        builtinWork = null;
    }

    private static void ensureCalendar(Context context) {
        int stamp = stamp(Calendar.getInstance());
        long currentTimeMillis = System.currentTimeMillis();
        if (calWork != null && calCacheDay == stamp && currentTimeMillis - calCacheAt < CAL_TTL_MS) {
            return;
        }
        HashMap<Integer, String> hashMap = new HashMap<>();
        HashMap<Integer, String> hashMap2 = new HashMap<>();
        if (context != null) {
            try {
                if (context.checkSelfPermission("android.permission.READ_CALENDAR") == 0) {
                    readCalendar(context, hashMap, hashMap2);
                }
            } catch (Throwable th) {
            }
        }
        calWork = hashMap;
        calRest = hashMap2;
        calCacheAt = currentTimeMillis;
        calCacheDay = stamp;
    }

    private static void readCalendar(Context context, HashMap<Integer, String> hashMap, HashMap<Integer, String> hashMap2) {
        HashMap<Integer, String> hashMap3;
        Calendar calendar = Calendar.getInstance();
        calendar.add(6, -400);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.add(6, 400);
        Cursor query = context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, new String[]{"dtstart", "dtend", "title"}, "deleted = 0 AND allDay = 1 AND dtstart >= ? AND dtstart <= ?", new String[]{String.valueOf(calendar.getTimeInMillis()), String.valueOf(calendar2.getTimeInMillis())}, null);
        if (query == null) {
            return;
        }
        try {
            int columnIndex = query.getColumnIndex("dtstart");
            int columnIndex2 = query.getColumnIndex("dtend");
            int columnIndex3 = query.getColumnIndex("title");
            while (query.moveToNext()) {
                long j = query.getLong(columnIndex);
                long j2 = query.isNull(columnIndex2) ? 0L : query.getLong(columnIndex2);
                String string = query.getString(columnIndex3);
                if (string != null) {
                    String trim = string.trim();
                    if (trim.contains("班")) {
                        hashMap3 = hashMap;
                    } else if (trim.contains("休") || trim.contains("假")) {
                        hashMap3 = hashMap2;
                    } else {
                        continue;
                    }
                    String cleanName = cleanName(trim);
                    if (j2 <= j) {
                        j2 = 1 + j;
                    }
                    Calendar calendar3 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    calendar3.setTimeInMillis(j);
                    Calendar calendar4 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    calendar4.setTimeInMillis(j2);
                    for (int i = 0; calendar3.before(calendar4) && i < 400; i++) {
                        hashMap3.put(Integer.valueOf(stamp(calendar3)), cleanName);
                        calendar3.add(5, 1);
                    }
                }
            }
        } finally {
            query.close();
        }
    }

    private static String cleanName(String str) {
        String trim = str.replace("（休）", "").replace("(休)", "").replace("（班）", "").replace("(班)", "").replace("（放假）", "").replace("(放假)", "").trim();
        return trim.length() == 0 ? str : trim;
    }

    static boolean isWorkday(Context context, MainActivity.SalarySettings salarySettings, Calendar calendar) {
        int stamp = stamp(calendar);
        if (salarySettings.useCalendar) {
            ensureCalendar(context);
            if (calWork.containsKey(Integer.valueOf(stamp))) {
                return true;
            }
            if (calRest.containsKey(Integer.valueOf(stamp))) {
                return false;
            }
        }
        if (salarySettings.workMode == 0) {
            return true;
        }
        boolean z = (calendar.get(7) == 7 || calendar.get(7) == 1) ? false : true;
        if (salarySettings.workMode == 1) {
            return z;
        }
        ensureBuiltin();
        if (builtinWork.containsKey(Integer.valueOf(stamp))) {
            return true;
        }
        if (builtinRest.containsKey(Integer.valueOf(stamp))) {
            return false;
        }
        return z;
    }

    /**
     * 这天是不是「补班日」—— 被安排上班的周末调休日。
     *
     * <p>补班日长着周末的脸却要上班，但**没有额外工资**（1 倍），
     * 是全年最容易看糊涂的几天，日历上要标出来。
     *
     * @return 补班原因（如「国庆节」）；不是补班日返回 null
     */
    static String makeupNote(Context context, MainActivity.SalarySettings salarySettings, Calendar calendar) {
        if (calendar == null) {
            return null;
        }
        int stamp = stamp(calendar);
        int dow = ((Calendar) calendar.clone()).get(7);
        if (dow != 7 && dow != 1) {
            return null;                       // 补班只会落在周末
        }
        if (salarySettings != null && salarySettings.useCalendar) {
            ensureCalendar(context);
            String str = calWork.get(Integer.valueOf(stamp));
            if (str != null) {
                return str.length() == 0 ? "调休上班" : str;
            }
        }
        if (salarySettings != null && salarySettings.workMode != 2) {
            return null;                       // 非智能模式不认补班
        }
        ensureBuiltin();
        String str2 = builtinWork.get(Integer.valueOf(stamp));
        if (str2 != null) {
            return str2.length() == 0 ? "调休上班" : str2;
        }
        return null;
    }

    static int workdaysInMonth(Context context, MainActivity.SalarySettings salarySettings, int i, int i2) {
        int i3 = 0;
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(i, i2 - 1, 1);
            int actualMaximum = calendar.getActualMaximum(5);
            for (int i4 = 1; i4 <= actualMaximum; i4++) {
                calendar.set(5, i4);
                if (isWorkday(context, salarySettings, calendar)) {
                    i3++;
                }
            }
        } catch (Throwable th) {
        }
        return i3;
    }

    static String restNote(Context context, MainActivity.SalarySettings salarySettings, Calendar calendar) {
        if (isWorkday(context, salarySettings, calendar)) {
            return null;
        }
        int stamp = stamp(calendar);
        if (salarySettings.useCalendar) {
            ensureCalendar(context);
            String str = calRest.get(Integer.valueOf(stamp));
            if (str != null) {
                return str;
            }
        }
        if (salarySettings.workMode != 2) {
            return "周末";
        }
        ensureBuiltin();
        String str2 = builtinRest.get(Integer.valueOf(stamp));
        return str2 != null ? str2 : "周末";
    }
}
