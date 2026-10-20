package com.salaryjump.app;

import android.content.Context;
import android.content.SharedPreferences;
import com.salaryjump.app.WaterReminderScheduler;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public final class DrinkTypes {
    public static final int BUILTIN = 8;
    private static final String KEY_CUSTOM = "custom_drinks";
    private static final String KEY_HIDDEN = "hidden_drinks";
    private static final String KEY_KCAL = "drink_kcal_";
    private static final String KEY_NAME = "drink_name_";
    private static final String KEY_NEXT = "drink_next_id";
    private static final String KEY_PRESETS = "drink_presets";
    private static final String KEY_PRESETS_VER = "drink_presets_ver";
    private static final String KEY_REMOVED = "removed_drinks";
    private static final String KEY_SUGAR = "drink_sugar_";
    public static final int MAX = 32;
    private static final int PRESETS_VERSION = 2;
    /** 单杯容量合法区间（ml） */
    public static final int ML_MIN = 10;
    public static final int ML_MAX = 2000;
    /** 每 100ml 热量合法区间（kcal） */
    public static final int KCAL_MIN = 0;
    public static final int KCAL_MAX = 900;
    /** 每日喝水总量合法区间（ml） */
    public static final int TARGET_MIN = 100;
    public static final int TARGET_MAX = 10000;

    public static final String[] NAMES = {"白开水", "茶", "咖啡", "奶茶", "可乐/汽水", "果汁", "牛奶", "其他"};
    public static final String[] UNITS = {"杯", "杯", "杯", "杯", "罐", "杯", "盒", "次"};
    private static final int[] DEFAULTS = {250, 250, 300, 500, 330, 250, 250, 250};
    private static final int[] DEFAULTS_KCAL = {0, 0, 2, 55, 43, 45, 65, 0};
    private static final boolean[] DEFAULTS_SUGAR = {false, false, false, true, true, true, false, false};
    static final Object[][] SEED_PRESETS = {new Object[]{"白开水", 0, false}, new Object[]{"无糖茶", 0, false}, new Object[]{"黑咖啡 / 美式", 2, false}, new Object[]{"拿铁 / 奶咖", 55, true}, new Object[]{"奶茶（常规）", 55, true}, new Object[]{"可乐（含糖）", 43, true}, new Object[]{"可乐（无糖）", 0, false}, new Object[]{"冰红茶", 39, true}, new Object[]{"果汁（100%）", 45, true}, new Object[]{"全脂牛奶", 65, false}, new Object[]{"脱脂牛奶", 35, false}, new Object[]{"豆浆（无糖）", 31, false}, new Object[]{"乳酸菌饮料", 60, true}, new Object[]{"运动饮料", 27, true}, new Object[]{"能量饮料", 46, true}, new Object[]{"椰子水", 20, true}, new Object[]{"苏打水（无糖）", 0, false}, new Object[]{"啤酒（普通）", 43, false}, new Object[]{"啤酒（淡爽）", 35, false}, new Object[]{"精酿啤酒", 55, false}, new Object[]{"无醇啤酒", 18, false}};
    private static final Object[][] PRESETS_V2_ADD = {new Object[]{"啤酒（普通）", 43, false}, new Object[]{"啤酒（淡爽）", 35, false}, new Object[]{"精酿啤酒", 55, false}, new Object[]{"无醇啤酒", 18, false}};

    private DrinkTypes() {
    }

    public static List<Integer> frequentIds(Context context) {
        String[] strArr;
        int i;
        int i2;
        ArrayList arrayList = new ArrayList();
        if (context != null) {
            try {
                SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
                Calendar calendar = Calendar.getInstance();
                calendar.add(5, -29);
                int stamp = WorkdayHelper.stamp(calendar);
                final HashMap hashMap = new HashMap();
                final HashMap hashMap2 = new HashMap();
                String string = sharedPreferences.getString("water_records", "");
                if (string != null && string.length() > 0) {
                    String[] split = string.split(";");
                    int length = split.length;
                    int i3 = 0;
                    while (i3 < length) {
                        String[] split2 = split[i3].split(",");
                        if (split2.length < 3) {
                            strArr = split;
                            i = length;
                            i2 = i3;
                        } else {
                            try {
                                i = length;
                                strArr = split;
                                i2 = i3;
                                try {
                                    addFreq(hashMap, hashMap2, Integer.valueOf(Integer.parseInt(split2[2].trim())), 1, Long.parseLong(split2[0].trim()));
                                } catch (NumberFormatException e) {
                                }
                            } catch (NumberFormatException e2) {
                                strArr = split;
                                i = length;
                                i2 = i3;
                            }
                        }
                        i3 = i2 + 1;
                        split = strArr;
                        length = i;
                    }
                }
                String string2 = sharedPreferences.getString("water_history", "");
                if (string2 != null && string2.length() > 0) {
                    for (String str : string2.split(";")) {
                        String[] split3 = str.split(":");
                        if (split3.length >= 4) {
                            try {
                                int parseInt = Integer.parseInt(split3[0].trim());
                                if (parseInt >= stamp) {
                                    addFreq(hashMap, hashMap2, Integer.valueOf(Integer.parseInt(split3[1].trim())), Math.max(1, Integer.parseInt(split3[3].trim())), stampToMs(parseInt));
                                }
                            } catch (NumberFormatException e3) {
                            }
                        }
                    }
                }
                ArrayList arrayList2 = new ArrayList();
                int count = count(context);
                for (Object numObj : hashMap.keySet()) {
                    Integer num = (Integer) numObj;
                    int i4 = 0;
                    while (true) {
                        if (i4 >= count) {
                            break;
                        }
                        if (idAt(context, i4) != num.intValue()) {
                            i4++;
                        } else {
                            arrayList2.add(num);
                            break;
                        }
                    }
                }
                Collections.sort(arrayList2, new Comparator<Integer>() { // from class: com.salaryjump.app.DrinkTypes.1
                    @Override // java.util.Comparator
                    public int compare(Integer num2, Integer num3) {
                        int intValue = ((Integer) hashMap.get(num2)).intValue();
                        int intValue2 = ((Integer) hashMap.get(num3)).intValue();
                        if (intValue != intValue2) {
                            return intValue2 - intValue;
                        }
                        long longValue = ((Long) hashMap2.get(num2)).longValue();
                        long longValue2 = ((Long) hashMap2.get(num3)).longValue();
                        if (longValue != longValue2) {
                            return longValue2 > longValue ? 1 : -1;
                        }
                        return 0;
                    }
                });
                for (int i5 = 0; i5 < arrayList2.size() && arrayList.size() < 2; i5++) {
                    arrayList.add((Integer) arrayList2.get(i5));
                }
            } catch (Throwable th) {
            }
            return arrayList;
        }
        return arrayList;
    }

    private static void addFreq(HashMap<Integer, Integer> hashMap, HashMap<Integer, Long> hashMap2, Integer num, int i, long j) {
        Integer num2 = hashMap.get(num);
        hashMap.put(num, Integer.valueOf((num2 == null ? 0 : num2.intValue()) + i));
        Long l = hashMap2.get(num);
        if (l == null || j > l.longValue()) {
            hashMap2.put(num, Long.valueOf(j));
        }
    }

    private static long stampToMs(int i) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(i / 10000, ((i / 100) % 100) - 1, i % 100, 12, 0, 0);
            calendar.set(14, 0);
            return calendar.getTimeInMillis();
        } catch (Throwable th) {
            return 0L;
        }
    }

    /* ─────────── 输入范围（设置界面共用） ─────────── */

    /** 单杯容量：10 – 2000 ml */
    public static boolean mlInRange(int i) {
        return i >= ML_MIN && i <= ML_MAX;
    }

    /** 每 100ml 热量：0 – 900 kcal */
    public static boolean kcalInRange(int i) {
        return i >= KCAL_MIN && i <= KCAL_MAX;
    }

    /** 每日喝水总量：100 – 10000 ml */
    public static boolean targetInRange(int i) {
        return i >= TARGET_MIN && i <= TARGET_MAX;
    }

    public static int clamp(int i) {
        if (i < 0) {
            return 0;
        }
        if (i >= 32) {
            return 31;
        }
        return i;
    }

    private static List<Integer> visibleIds(Context context) {
        ArrayList arrayList = new ArrayList();
        List<Integer> removedIds = removedIds(context);
        for (int i = 0; i < 8; i++) {
            if (!removedIds.contains(Integer.valueOf(i))) {
                arrayList.add(Integer.valueOf(i));
            }
        }
        Iterator<String[]> it = customs(context).iterator();
        while (it.hasNext()) {
            try {
                arrayList.add(Integer.valueOf(Integer.parseInt(it.next()[0])));
            } catch (NumberFormatException e) {
            }
        }
        return arrayList;
    }

    public static int count(Context context) {
        return visibleIds(context).size();
    }

    public static int idAt(Context context, int i) {
        List<Integer> visibleIds = visibleIds(context);
        if (i < 0 || i >= visibleIds.size()) {
            return 0;
        }
        return visibleIds.get(i).intValue();
    }

    private static List<Integer> removedIds(Context context) {
        ArrayList arrayList = new ArrayList();
        if (context == null) {
            return arrayList;
        }
        int i = 0;
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        String string = sharedPreferences.getString(KEY_REMOVED, null);
        if (string == null) {
            String string2 = sharedPreferences.getString(KEY_HIDDEN, "");
            StringBuilder sb = new StringBuilder();
            if (string2 != null && string2.length() > 0) {
                String[] split = string2.split(",");
                int length = split.length;
                while (i < length) {
                    try {
                        int parseInt = Integer.parseInt(split[i].trim());
                        if (parseInt > 0 && parseInt < 8 && !hasRecords(context, parseInt)) {
                            arrayList.add(Integer.valueOf(parseInt));
                            if (sb.length() > 0) {
                                sb.append(',');
                            }
                            sb.append(parseInt);
                        }
                    } catch (NumberFormatException e) {
                    }
                    i++;
                }
            }
            sharedPreferences.edit().putString(KEY_REMOVED, sb.toString()).remove(KEY_HIDDEN).apply();
            return arrayList;
        }
        if (string.length() == 0) {
            return arrayList;
        }
        String[] split2 = string.split(",");
        int length2 = split2.length;
        while (i < length2) {
            try {
                int parseInt2 = Integer.parseInt(split2[i].trim());
                if (parseInt2 > 0 && parseInt2 < 8) {
                    arrayList.add(Integer.valueOf(parseInt2));
                }
            } catch (NumberFormatException e2) {
            }
            i++;
        }
        return arrayList;
    }

    private static void saveRemovedIds(Context context, List<Integer> list) {
        StringBuilder sb = new StringBuilder();
        for (Integer num : list) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(num.intValue());
        }
        context.getSharedPreferences("salary_jump", 0).edit().putString(KEY_REMOVED, sb.toString()).apply();
    }

    public static int removedCount(Context context) {
        return removedIds(context).size();
    }

    public static boolean isRemoved(Context context, int i) {
        return i > 0 && i < 8 && removedIds(context).contains(Integer.valueOf(i));
    }

    public static boolean hasRecords(Context context, int i) {
        if (context == null) {
            return false;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        String string = sharedPreferences.getString("water_records", "");
        if (string != null && string.length() > 0) {
            for (String str : string.split(";")) {
                String[] split = str.split(",");
                if (split.length >= 2) {
                    try {
                        if ((split.length >= 3 ? Integer.parseInt(split[2]) : 0) == i) {
                            return true;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        String string2 = sharedPreferences.getString("water_history", "");
        if (string2 != null && string2.length() > 0) {
            for (String str2 : string2.split(";")) {
                String[] split2 = str2.split(":");
                if (split2.length >= 2) {
                    try {
                        if (Integer.parseInt(split2[1]) == i) {
                            return true;
                        }
                    } catch (NumberFormatException e2) {
                    }
                }
            }
        }
        return false;
    }

    public static String removeCheck(Context context, int i) {
        if (context == null) {
            return "无法删除";
        }
        if (i == 0) {
            return "白开水是主按钮的默认饮品，不能删除。";
        }
        if (!isKnown(context, i)) {
            return "该饮品已不存在。";
        }
        if (hasRecords(context, i)) {
            return "「" + name(context, i) + "」已经有喝水记录，不能删除。\n\n喝过的饮品若被删除，历史记录和统计就对不上号了。";
        }
        return null;
    }

    public static String remove(Context context, int i) {
        String removeCheck = removeCheck(context, i);
        if (removeCheck != null) {
            return removeCheck;
        }
        if (i >= 8) {
            removeCustom(context, i);
            return null;
        }
        List<Integer> removedIds = removedIds(context);
        if (!removedIds.contains(Integer.valueOf(i))) {
            removedIds.add(Integer.valueOf(i));
            saveRemovedIds(context, removedIds);
        }
        return null;
    }

    public static String name(Context context, int i) {
        String string;
        if (context != null && (string = context.getSharedPreferences("salary_jump", 0).getString(KEY_NAME + i, null)) != null && string.length() > 0) {
            return string;
        }
        return baseName(context, i);
    }

    public static String baseName(Context context, int i) {
        if (i >= 0 && i < 8) {
            return NAMES[i];
        }
        String[] find = find(context, i);
        return find == null ? "已删除饮品" : find[1];
    }

    public static boolean isRenamed(Context context, int i) {
        String string;
        return (context == null || (string = context.getSharedPreferences("salary_jump", 0).getString(new StringBuilder().append(KEY_NAME).append(i).toString(), null)) == null || string.length() <= 0) ? false : true;
    }

    public static String nameCheck(String str) {
        String trim = str == null ? "" : str.trim();
        if (trim.length() == 0) {
            return "名称不能为空";
        }
        if (trim.length() > 8) {
            return "名称请控制在 8 个字以内";
        }
        if (trim.indexOf(44) >= 0 || trim.indexOf(59) >= 0 || trim.indexOf(58) >= 0) {
            return "名称不能包含 , ; : 符号";
        }
        return null;
    }

    public static void setName(Context context, int i, String str) {
        if (context == null) {
            return;
        }
        String trim = str == null ? "" : str.trim();
        if (trim.length() == 0) {
            trim = baseName(context, i);
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        if (trim.equals(baseName(context, i))) {
            sharedPreferences.edit().remove(KEY_NAME + i).apply();
        } else {
            sharedPreferences.edit().putString(KEY_NAME + i, trim).apply();
        }
    }

    public static String name(int i) {
        return (i < 0 || i >= 8) ? "饮品" : NAMES[i];
    }

    public static String unit(Context context, int i) {
        String string;
        if (context != null && (string = context.getSharedPreferences("salary_jump", 0).getString("drink_unit_" + i, "")) != null && string.length() > 0) {
            return string;
        }
        if (i >= 0 && i < 8) {
            return UNITS[i];
        }
        String[] find = find(context, i);
        return (find == null || find[2].length() == 0) ? "次" : find[2];
    }

    public static void setUnit(Context context, int i, String str) {
        if (context == null || i < 0) {
            return;
        }
        String trim = str == null ? "" : str.trim();
        if (trim.length() == 0) {
            return;
        }
        if (trim.length() > 2) {
            trim = trim.substring(0, 2);
        }
        String trim2 = trim.replace(',', ' ').replace(';', ' ').replace(':', ' ').trim();
        if (trim2.length() == 0) {
            return;
        }
        context.getSharedPreferences("salary_jump", 0).edit().putString("drink_unit_" + i, trim2).apply();
    }

    public static boolean isUnitCustom(Context context, int i) {
        return context != null && context.getSharedPreferences("salary_jump", 0).contains(new StringBuilder().append("drink_unit_").append(i).toString());
    }

    public static int ml(Context context, int i) {
        int i2 = 250;
        if (context == null) {
            return 250;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        if (i == 0) {
            return WaterReminderSettings.load(context).cupMl;
        }
        if (i > 0 && i < 8) {
            i2 = DEFAULTS[i];
        }
        return sharedPreferences.getInt("drink_ml_" + i, i2);
    }

    public static void setMl(Context context, int i, int i2) {
        if (i == 0 || context == null) {
            return;
        }
        context.getSharedPreferences("salary_jump", 0).edit().putInt("drink_ml_" + i, i2).apply();
    }

    public static int kcal(Context context, int i) {
        if (context == null) {
            return 0;
        }
        return context.getSharedPreferences("salary_jump", 0).getInt(KEY_KCAL + i, (i <= 0 || i >= 8) ? 0 : DEFAULTS_KCAL[i]);
    }

    public static void setKcal(Context context, int i, int i2) {
        if (context == null) {
            return;
        }
        if (i2 < 0) {
            i2 = 0;
        }
        if (i2 > 900) {
            i2 = 900;
        }
        context.getSharedPreferences("salary_jump", 0).edit().putInt(KEY_KCAL + i, i2).apply();
    }

    public static boolean isKcalCustom(Context context, int i) {
        if (context == null) {
            return false;
        }
        return context.getSharedPreferences("salary_jump", 0).contains(KEY_KCAL + i);
    }

    public static boolean isSugary(Context context, int i) {
        int i2;
        if (context == null) {
            return false;
        }
        if (i <= 0 || i >= 8 || !DEFAULTS_SUGAR[i]) {
            i2 = 0;
        } else {
            i2 = 1;
        }
        if (context.getSharedPreferences("salary_jump", 0).getInt(KEY_SUGAR + i, i2) != 1) {
            return false;
        }
        return true;
    }

    public static void setSugary(Context context, int i, boolean z) {
        if (context == null) {
            return;
        }
        context.getSharedPreferences("salary_jump", 0).edit().putInt(KEY_SUGAR + i, z ? 1 : 0).apply();
    }

    public static int recordKcal(Context context, int i, int i2) {
        return Math.round((i2 / 100.0f) * kcal(context, i));
    }

    public static int todayKcal(Context context) {
        int i = 0;
        for (WaterReminderScheduler.WaterRecord waterRecord : WaterReminderScheduler.getRecords(context)) {
            i += recordKcal(context, waterRecord.type, waterRecord.amountMl);
        }
        return i;
    }

    public static int todaySugaryCups(Context context) {
        Iterator<WaterReminderScheduler.WaterRecord> it = WaterReminderScheduler.getRecords(context).iterator();
        int i = 0;
        while (it.hasNext()) {
            if (isSugary(context, it.next().type)) {
                i++;
            }
        }
        return i;
    }

    public static class Preset {
        public int kcal;
        public String name;
        public boolean sugar;

        public Preset(String str, int i, boolean z) {
            this.name = str;
            this.kcal = i;
            this.sugar = z;
        }
    }

    public static List<Preset> presets(Context context) {
        ArrayList arrayList = new ArrayList();
        if (context == null) {
            return arrayList;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        String string = sharedPreferences.getString(KEY_PRESETS, null);
        if (string == null) {
            for (Object[] objArr : SEED_PRESETS) {
                arrayList.add(new Preset((String) objArr[0], ((Integer) objArr[1]).intValue(), ((Boolean) objArr[2]).booleanValue()));
            }
            savePresets(context, arrayList);
            sharedPreferences.edit().putInt(KEY_PRESETS_VER, 2).apply();
            return arrayList;
        }
        if (string.length() == 0) {
            return arrayList;
        }
        for (String str : string.split(";")) {
            String[] split = str.split(",");
            if (split.length >= 3 && split[0].length() > 0) {
                try {
                    arrayList.add(new Preset(split[0], Integer.parseInt(split[1]), "1".equals(split[2])));
                } catch (NumberFormatException e) {
                }
            }
        }
        mergeNewPresets(sharedPreferences, arrayList);
        return arrayList;
    }

    private static void mergeNewPresets(SharedPreferences sharedPreferences, List<Preset> list) {
        boolean z;
        int i = sharedPreferences.getInt(KEY_PRESETS_VER, 1);
        if (i >= 2) {
            return;
        }
        ArrayList<Preset> arrayList = new ArrayList();
        if (i < 2) {
            for (Object[] objArr : PRESETS_V2_ADD) {
                arrayList.add(new Preset((String) objArr[0], ((Integer) objArr[1]).intValue(), ((Boolean) objArr[2]).booleanValue()));
            }
        }
        boolean z2 = false;
        for (Preset preset : arrayList) {
            Iterator<Preset> it = list.iterator();
            while (true) {
                if (!it.hasNext()) {
                    z = false;
                    break;
                } else if (it.next().name.equals(preset.name)) {
                    z = true;
                    break;
                }
            }
            if (!z) {
                list.add(preset);
                z2 = true;
            }
        }
        if (z2) {
            savePresets(sharedPreferences, list);
        }
        sharedPreferences.edit().putInt(KEY_PRESETS_VER, 2).apply();
    }

    private static void savePresets(Context context, List<Preset> list) {
        savePresets(context.getSharedPreferences("salary_jump", 0), list);
    }

    private static void savePresets(SharedPreferences sharedPreferences, List<Preset> list) {
        StringBuilder sb = new StringBuilder();
        for (Preset preset : list) {
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(preset.name).append(',').append(preset.kcal).append(',').append(preset.sugar ? 1 : 0);
        }
        sharedPreferences.edit().putString(KEY_PRESETS, sb.toString()).apply();
    }

    private static int presetIndexOf(Context context, String str, int i) {
        List<Preset> presets = presets(context);
        for (int i2 = 0; i2 < presets.size(); i2++) {
            if (i2 != i && presets.get(i2).name.equals(str)) {
                return i2;
            }
        }
        return -1;
    }

    public static String presetAdd(Context context, String str, int i, boolean z) {
        if (context == null) {
            return "无法保存";
        }
        String nameCheck = nameCheck(str);
        if (nameCheck != null) {
            return nameCheck;
        }
        if (i < 0 || i > 900) {
            return "热量请填 0 到 900 之间的整数";
        }
        String trim = str.trim();
        if (presetIndexOf(context, trim, -1) >= 0) {
            return "参考表里已有「" + trim + "」";
        }
        List<Preset> presets = presets(context);
        if (presets.size() >= 60) {
            return "参考表最多 60 条";
        }
        presets.add(new Preset(trim, i, z));
        savePresets(context, presets);
        return null;
    }

    public static String presetUpdate(Context context, int i, String str, int i2, boolean z) {
        if (context == null) {
            return "无法保存";
        }
        List<Preset> presets = presets(context);
        if (i < 0 || i >= presets.size()) {
            return "条目已不存在";
        }
        String nameCheck = nameCheck(str);
        if (nameCheck != null) {
            return nameCheck;
        }
        if (i2 < 0 || i2 > 900) {
            return "热量请填 0 到 900 之间的整数";
        }
        String trim = str.trim();
        String str2 = presets.get(i).name;
        if (presetIndexOf(context, trim, i) >= 0) {
            return "参考表里已有「" + trim + "」";
        }
        boolean equals = trim.equals(str2);
        presets.set(i, new Preset(trim, i2, z));
        savePresets(context, presets);
        if (!equals) {
            renameDrinksFrom(context, str2, trim);
            return null;
        }
        return null;
    }

    private static void renameDrinksFrom(Context context, String str, String str2) {
        int count = count(context);
        for (int i = 0; i < count; i++) {
            int idAt = idAt(context, i);
            if (name(context, idAt).equals(str)) {
                setName(context, idAt, str2);
                setKcal(context, idAt, kcalOfPreset(context, str2));
            }
        }
    }

    private static int kcalOfPreset(Context context, String str) {
        for (Preset preset : presets(context)) {
            if (preset.name.equals(str)) {
                return preset.kcal;
            }
        }
        return 0;
    }

    public static boolean presetInUse(Context context, String str) {
        if (context == null) {
            return false;
        }
        int count = count(context);
        for (int i = 0; i < count; i++) {
            if (name(context, idAt(context, i)).equals(str)) {
                return true;
            }
        }
        return false;
    }

    public static String presetRemoveCheck(Context context, int i) {
        List<Preset> presets = presets(context);
        if (i < 0 || i >= presets.size()) {
            return "条目已不存在";
        }
        String str = presets.get(i).name;
        if (presetInUse(context, str)) {
            return "「" + str + "」已经是一个在用的饮品，不能从参考表里删除。\n\n如需移除，请先在选饮品面板里长按删除这个饮品。";
        }
        return null;
    }

    public static void presetRemove(Context context, int i) {
        List<Preset> presets = presets(context);
        if (i < 0 || i >= presets.size()) {
            return;
        }
        presets.remove(i);
        savePresets(context, presets);
    }

    public static List<Preset> availablePresets(Context context) {
        ArrayList arrayList = new ArrayList();
        for (Preset preset : presets(context)) {
            if (!presetInUse(context, preset.name)) {
                arrayList.add(preset);
            }
        }
        return arrayList;
    }

    public static boolean isKnown(Context context, int i) {
        return (i >= 0 && i < 8) || find(context, i) != null;
    }

    public static boolean isCustom(Context context, int i) {
        return i >= 8 && find(context, i) != null;
    }

    public static String addCustom(Context context, String str, String str2, int i) {
        if (context == null) {
            return "无法保存";
        }
        String trim = str == null ? "" : str.trim();
        if (trim.length() == 0) {
            return "请输入饮品名称";
        }
        if (trim.length() > 8) {
            return "名称请控制在 8 个字以内";
        }
        if (trim.indexOf(44) >= 0 || trim.indexOf(59) >= 0 || trim.indexOf(58) >= 0) {
            return "名称不能包含 , ; : 符号";
        }
        if (findByName(context, trim) == null) {
            String trim2 = str2 != null ? str2.trim() : "";
            if (trim2.length() > 2) {
                trim2 = trim2.substring(0, 2);
            }
            if (trim2.length() == 0) {
                trim2 = "次";
            }
            if (i < 10 || i > 2000) {
                return "容量请设置在 10 到 2000 ml 之间";
            }
            SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
            int i2 = sharedPreferences.getInt(KEY_NEXT, 8);
            if (i2 < 8 || i2 >= 32) {
                return "自定义饮品已达上限（24 个）";
            }
            List<String[]> customs = customs(context);
            StringBuilder sb = new StringBuilder();
            for (String[] strArr : customs) {
                if (sb.length() > 0) {
                    sb.append(';');
                }
                sb.append(strArr[0]).append(',').append(strArr[1]).append(',').append(strArr[2]);
            }
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(i2).append(',').append(trim).append(',').append(trim2);
            sharedPreferences.edit().putString(KEY_CUSTOM, sb.toString()).putInt(KEY_NEXT, i2 + 1).putInt("drink_ml_" + i2, i).apply();
            return null;
        }
        return "已有同名饮品";
    }

    public static void removeCustom(Context context, int i) {
        if (context == null || i < 8) {
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        List<String[]> customs = customs(context);
        StringBuilder sb = new StringBuilder();
        for (String[] strArr : customs) {
            if (Integer.parseInt(strArr[0]) != i) {
                if (sb.length() > 0) {
                    sb.append(';');
                }
                sb.append(strArr[0]).append(',').append(strArr[1]).append(',').append(strArr[2]);
            }
        }
        sharedPreferences.edit().putString(KEY_CUSTOM, sb.toString()).remove("drink_ml_" + i).apply();
    }

    private static String[] findByName(Context context, String str) {
        for (String[] strArr : customs(context)) {
            if (strArr[1].equals(str)) {
                return strArr;
            }
        }
        return null;
    }

    private static String[] find(Context context, int i) {
        if (context == null || i < 8) {
            return null;
        }
        for (String[] strArr : customs(context)) {
            if (Integer.parseInt(strArr[0]) == i) {
                return strArr;
            }
        }
        return null;
    }

    private static List<String[]> customs(Context context) {
        ArrayList arrayList = new ArrayList();
        if (context == null) {
            return arrayList;
        }
        String string = context.getSharedPreferences("salary_jump", 0).getString(KEY_CUSTOM, "");
        if (string == null || string.length() == 0) {
            return arrayList;
        }
        for (String str : string.split(";")) {
            String[] split = str.split(",");
            if (split.length == 3 && split[0].length() > 0 && split[1].length() > 0) {
                arrayList.add(new String[]{split[0], split[1], split[2]});
            }
        }
        return arrayList;
    }
}
