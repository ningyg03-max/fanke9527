package com.salaryjump.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import com.salaryjump.app.MainActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/* loaded from: classes.dex */
public final class WaterReminderScheduler {
    static final String ACTION_DRINK = "com.salaryjump.app.action.WATER_DRINK";
    static final String ACTION_REMIND = "com.salaryjump.app.action.WATER_REMIND";
    static final String ACTION_SNOOZE = "com.salaryjump.app.action.WATER_SNOOZE";
    private static final String KEY_RECORDS = "water_records";
    static final int REQUEST_REMIND = 4200;
    private static final long SNOOZE_MINUTES = 10;

    private WaterReminderScheduler() {
    }

    static int getConsumed(Context context) {
        Iterator<WaterRecord> it = getRecords(context).iterator();
        int i = 0;
        while (it.hasNext()) {
            i += it.next().amountMl;
        }
        return i;
    }

    static List<WaterRecord> getRecords(Context context) {
        int i;
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        ensureCurrentDay(sharedPreferences);
        List<WaterRecord> parseRecords = parseRecords(sharedPreferences.getString(KEY_RECORDS, ""));
        if (parseRecords.isEmpty() && (i = sharedPreferences.getInt("water_consumed", 0)) > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(11, 0);
            calendar.set(12, 0);
            calendar.set(13, 0);
            calendar.set(14, 0);
            parseRecords.add(new WaterRecord(calendar.getTimeInMillis(), i));
            saveRecords(sharedPreferences, parseRecords);
        }
        return parseRecords;
    }

    static void addDrink(Context context) {
        addDrink(context, 0);
    }

    static void addDrink(Context context, int i) {
        addDrinkAt(context, i, System.currentTimeMillis());
    }

    static void addDrinkAt(Context context, int i, long j) {
        List<WaterRecord> records = getRecords(context);
        records.add(new WaterRecord(j, DrinkTypes.ml(context, i), i));
        saveRecords(context.getSharedPreferences("salary_jump", 0), records);
    }

    static boolean deleteRecord(Context context, long j) {
        List<WaterRecord> records = getRecords(context);
        boolean z = true;
        int size = records.size() - 1;
        while (true) {
            if (size < 0) {
                z = false;
                break;
            }
            if (records.get(size).timestamp == j) {
                records.remove(size);
                break;
            }
            size--;
        }
        if (z) {
            saveRecords(context.getSharedPreferences("salary_jump", 0), records);
        }
        return z;
    }

    static boolean updateRecordTime(Context context, long j, long j2) {
        List<WaterRecord> records = getRecords(context);
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).timestamp == j) {
                WaterRecord waterRecord = records.get(i);
                records.set(i, new WaterRecord(j2, waterRecord.amountMl, waterRecord.type));
                saveRecords(context.getSharedPreferences("salary_jump", 0), records);
                return true;
            }
        }
        return false;
    }

    static void resetConsumed(Context context) {
        context.getSharedPreferences("salary_jump", 0).edit().putString("water_day", dayKey(Calendar.getInstance())).putString(KEY_RECORDS, "").putInt("water_consumed", 0).apply();
    }

    private static void ensureCurrentDay(SharedPreferences sharedPreferences) {
        String dayKey = dayKey(Calendar.getInstance());
        String string = sharedPreferences.getString("water_day", "");
        if (dayKey.equals(string)) {
            return;
        }
        if (string != null && string.length() > 0) {
            List<WaterRecord> parseRecords = parseRecords(sharedPreferences.getString(KEY_RECORDS, ""));
            if (!parseRecords.isEmpty()) {
                WaterHistory.archive(sharedPreferences, string, parseRecords);
            }
        }
        sharedPreferences.edit().putString("water_day", dayKey).putString(KEY_RECORDS, "").putInt("water_consumed", 0).apply();
    }

    private static List<WaterRecord> parseRecords(String str) {
        ArrayList arrayList = new ArrayList();
        if (str != null && str.length() != 0) {
            for (String str2 : str.split(";")) {
                String[] split = str2.split(",");
                if (split.length >= 2) {
                    try {
                        arrayList.add(new WaterRecord(Long.parseLong(split[0]), Integer.parseInt(split[1]), split.length >= 3 ? Integer.parseInt(split[2]) : 0));
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        return arrayList;
    }

    private static void saveRecords(SharedPreferences sharedPreferences, List<WaterRecord> list) {
        Collections.sort(list, new Comparator<WaterRecord>() { // from class: com.salaryjump.app.WaterReminderScheduler.1
            @Override // java.util.Comparator
            public int compare(WaterRecord waterRecord, WaterRecord waterRecord2) {
                if (waterRecord.timestamp < waterRecord2.timestamp) {
                    return -1;
                }
                return waterRecord.timestamp == waterRecord2.timestamp ? 0 : 1;
            }
        });
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (WaterRecord waterRecord : list) {
            i += waterRecord.amountMl;
            sb.append(waterRecord.timestamp);
            sb.append(',');
            sb.append(waterRecord.amountMl);
            sb.append(',');
            sb.append(waterRecord.type);
            sb.append(';');
        }
        sharedPreferences.edit().putString("water_day", dayKey(Calendar.getInstance())).putString(KEY_RECORDS, sb.toString()).putInt("water_consumed", i).apply();
    }

    static void scheduleNext(Context context) {
        if (!WaterReminderSettings.load(context).enabled) {
            cancel(context);
            return;
        }
        long nextReminderMillis = nextReminderMillis(context);
        if (nextReminderMillis <= 0) {
            cancel(context);
        } else {
            scheduleAt(context, nextReminderMillis);
        }
    }

    static void scheduleSnooze(Context context) {
        scheduleAt(context, System.currentTimeMillis() + 600000);
    }

    static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (alarmManager != null) {
            alarmManager.cancel(reminderIntent(context));
        }
    }

    static long nextReminderMillis(Context context) {
        WaterReminderSettings load = WaterReminderSettings.load(context);
        if (!load.enabled || load.intervalMinutes <= 0) {
            return -1L;
        }
        if (getConsumed(context) >= load.targetMl) {
            return firstReminderOnNextWorkday(context, load, Calendar.getInstance());
        }
        return nextReminderAfter(context, load, Calendar.getInstance());
    }

    static String nextReminderText(Context context) {
        WaterReminderSettings load = WaterReminderSettings.load(context);
        if (!load.enabled) {
            return "喝水提醒已关闭";
        }
        if (getConsumed(context) >= load.targetMl) {
            return "今日喝水目标已完成";
        }
        long nextReminderMillis = nextReminderMillis(context);
        if (nextReminderMillis <= 0) {
            return "暂无提醒";
        }
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(nextReminderMillis);
        if (sameDay(calendar, calendar2)) {
            return "下次提醒 " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar2.getTime());
        }
        return "下次提醒 " + new SimpleDateFormat("M月d日 HH:mm", Locale.getDefault()).format(calendar2.getTime());
    }

    static boolean isAllowedNow(Context context) {
        WaterReminderSettings load = WaterReminderSettings.load(context);
        if (!load.enabled || getConsumed(context) >= load.targetMl) {
            return false;
        }
        MainActivity.SalarySettings load2 = MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0));
        Calendar calendar = Calendar.getInstance();
        int i = (calendar.get(11) * 60) + calendar.get(12);
        if (load.windowMode == 1) {
            if (i < load.winStartMinutes || i >= load.winEndMinutes) {
                return false;
            }
            if (!load.skipBreak || i < load2.breakStartMinutes || i >= load2.breakEndMinutes) {
                return load.weekend || isDrinkingDay(context, load2, calendar);
            }
            return false;
        }
        if (!isDrinkingDay(context, load2, calendar) || i < load2.workStartMinutes || i >= load2.workEndMinutes) {
            return false;
        }
        return i < load2.breakStartMinutes || i >= load2.breakEndMinutes;
    }

    static boolean isDrinkingDay(Context context, MainActivity.SalarySettings salarySettings, Calendar calendar) {
        if (WorkdayHelper.isWorkday(context, salarySettings, calendar)) {
            return true;
        }
        return HolidayWork.has(context, WorkdayHelper.stamp(calendar));
    }

    /* JADX WARN: Code restructure failed: missing block: B:42:0x00b1, code lost:
    
        continue;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    static long nextReminderAfter(Context context, WaterReminderSettings waterReminderSettings, Calendar calendar) {
        MainActivity.SalarySettings load = MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0));
        boolean custom = waterReminderSettings.windowMode == 1;
        int startMin = custom ? waterReminderSettings.winStartMinutes : load.workStartMinutes;
        int endMin = custom ? waterReminderSettings.winEndMinutes : load.workEndMinutes;
        boolean skipBreak = !custom || waterReminderSettings.skipBreak;
        boolean everyDay = custom && waterReminderSettings.weekend;

        Calendar day = (Calendar) calendar.clone();
        day.set(11, 0);
        day.set(12, 0);
        day.set(13, 0);
        day.set(14, 0);

        for (int d = 0; d < 14; d++) {
            if (everyDay || isDrinkingDay(context, load, day)) {
                Calendar slot = (Calendar) day.clone();
                slot.set(11, startMin / 60);
                slot.set(12, startMin % 60);
                slot.set(13, 0);
                slot.set(14, 0);
                for (int n = 0; n < 48; n++) {
                    slot.add(12, waterReminderSettings.intervalMinutes);
                    int now = (slot.get(11) * 60) + slot.get(12);
                    if (now >= endMin) {
                        break;
                    }
                    boolean inBreak = skipBreak && now >= load.breakStartMinutes && now < load.breakEndMinutes;
                    boolean tooSoon = slot.getTimeInMillis() <= calendar.getTimeInMillis() + 500;
                    if (inBreak || tooSoon) {
                        continue;
                    }
                    return slot.getTimeInMillis();
                }
            }
            day.add(5, 1);
        }
        return -1L;
    }

    private static long firstReminderOnNextWorkday(Context context, WaterReminderSettings waterReminderSettings, Calendar calendar) {
        int i = 0;
        MainActivity.SalarySettings load = MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0));
        boolean z = waterReminderSettings.windowMode == 1;
        int i2 = z ? waterReminderSettings.winStartMinutes : load.workStartMinutes;
        int i3 = z ? waterReminderSettings.winEndMinutes : load.workEndMinutes;
        boolean z2 = !z || waterReminderSettings.skipBreak;
        boolean z3 = z && waterReminderSettings.weekend;
        Calendar calendar2 = (Calendar) calendar.clone();
        calendar2.add(5, 1);
        calendar2.set(11, 0);
        calendar2.set(12, 0);
        calendar2.set(13, 0);
        calendar2.set(14, 0);
        int i4 = 0;
        while (i4 < 14) {
            if (z3 || WorkdayHelper.isWorkday(context, load, calendar2)) {
                Calendar calendar3 = (Calendar) calendar2.clone();
                calendar3.set(11, i2 / 60);
                calendar3.set(12, i2 % 60);
                calendar3.set(13, i);
                calendar3.set(14, i);
                calendar3.add(12, waterReminderSettings.intervalMinutes);
                int i5 = (calendar3.get(11) * 60) + calendar3.get(12);
                if (i5 < i3 && (!z2 || i5 < load.breakStartMinutes || i5 >= load.breakEndMinutes)) {
                    return calendar3.getTimeInMillis();
                }
            }
            calendar2.add(5, 1);
            i4++;
            i = 0;
        }
        return -1L;
    }

    private static void scheduleAt(Context context, long j) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (alarmManager == null) {
            return;
        }
        PendingIntent reminderIntent = reminderIntent(context);
        if (Build.VERSION.SDK_INT >= 31 && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(0, j, reminderIntent);
        } else {
            alarmManager.setAndAllowWhileIdle(0, j, reminderIntent);
        }
    }

    private static PendingIntent reminderIntent(Context context) {
        Intent intent = new Intent(context, (Class<?>) WaterReminderReceiver.class);
        intent.setAction(ACTION_REMIND);
        return PendingIntent.getBroadcast(context, REQUEST_REMIND, intent, 201326592);
    }

    static class WaterRecord {
        final int amountMl;
        final long timestamp;
        final int type;

        WaterRecord(long j, int i) {
            this(j, i, 0);
        }

        WaterRecord(long j, int i, int i2) {
            this.timestamp = j;
            this.amountMl = i;
            this.type = i2;
        }
    }

    private static String dayKey(Calendar calendar) {
        return String.format(Locale.US, "%04d-%02d-%02d", Integer.valueOf(calendar.get(1)), Integer.valueOf(calendar.get(2) + 1), Integer.valueOf(calendar.get(5)));
    }

    private static boolean sameDay(Calendar calendar, Calendar calendar2) {
        return calendar.get(1) == calendar2.get(1) && calendar.get(6) == calendar2.get(6);
    }
}
