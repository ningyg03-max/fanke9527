package com.salaryjump.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

/* loaded from: classes.dex */
public final class HolidayUpdater {
    private static final int CONNECT_MS = 6000;
    private static final long DEADLINE_MS = 25000;
    static final String KEY_DATA = "holiday_data";
    static final String KEY_STAGE = "holiday_update_stage";
    static final String KEY_STATUTORY = "statutory_data";
    static final String KEY_UPDATED_AT = "holiday_updated_at";
    private static final int READ_MS = 8000;
    private static final int TIMEOUT_MS = 12000;
    private static String lastNetError;
    private static final String[] MIRRORS = {"https://cdn.jsdelivr.net/gh/NateScarlet/holiday-cn@master/%d.json", "https://fastly.jsdelivr.net/gh/NateScarlet/holiday-cn@master/%d.json", "https://gcore.jsdelivr.net/gh/NateScarlet/holiday-cn@master/%d.json", "https://raw.githubusercontent.com/NateScarlet/holiday-cn/master/%d.json"};
    private static final HashMap<Integer, Integer> MID_AUTUMN = new HashMap<>();

    public interface Callback {
        void onResult(boolean z, String str);
    }

    static {
        MID_AUTUMN.put(2024, 20240917);
        MID_AUTUMN.put(2025, 20251006);
        MID_AUTUMN.put(2026, 20260925);
        MID_AUTUMN.put(2027, 20270915);
        MID_AUTUMN.put(2028, 20281003);
        MID_AUTUMN.put(2029, 20290922);
        MID_AUTUMN.put(2030, 20300912);
        MID_AUTUMN.put(2031, 20311001);
        MID_AUTUMN.put(2032, 20320919);
        MID_AUTUMN.put(2033, 20330908);
    }

    private HolidayUpdater() {
    }

    static void markStage(Context context, String str) {
        if (context == null) {
            return;
        }
        try {
            context.getSharedPreferences("salary_jump", 0).edit().putString(KEY_STAGE, System.currentTimeMillis() + "|" + str).commit();
        } catch (Throwable th) {
        }
    }

    static String lastStageText(Context context) {
        int indexOf;
        if (context == null) {
            return null;
        }
        try {
            String string = context.getSharedPreferences("salary_jump", 0).getString(KEY_STAGE, null);
            if (string == null || string.length() == 0 || (indexOf = string.indexOf(124)) <= 0) {
                return null;
            }
            return new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(new Date(Long.parseLong(string.substring(0, indexOf)))) + "  " + string.substring(indexOf + 1);
        } catch (Throwable th) {
            return null;
        }
    }

    static String currentStage(Context context) {
        return rawStage(context);
    }

    static boolean lastStageSucceeded(Context context) {
        String rawStage = rawStage(context);
        return rawStage != null && rawStage.contains("成功");
    }

    private static String rawStage(Context context) {
        if (context == null) {
            return null;
        }
        try {
            String string = context.getSharedPreferences("salary_jump", 0).getString(KEY_STAGE, null);
            if (string == null) {
                return null;
            }
            int indexOf = string.indexOf(124);
            return indexOf >= 0 ? string.substring(indexOf + 1) : string;
        } catch (Throwable th) {
            return null;
        }
    }

    static void check(final Context context, final Callback callback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() { // from class: com.salaryjump.app.HolidayUpdater.1
            @Override // java.lang.Runnable
            public void run() {
                String sbTmp;
                boolean zTmp;
                HolidayUpdater.markStage(context, "后台线程已启动");
                try {
                    sbTmp = HolidayUpdater.doUpdate(context);
                    HolidayUpdater.markStage(context, "成功：" + sbTmp);
                    zTmp = true;
                } catch (Throwable th) {
                    String message = th.getMessage();
                    StringBuilder append = new StringBuilder().append("更新失败：");
                    if (message == null) {
                        message = th.getClass().getSimpleName();
                    }
                    sbTmp = append.append(message).toString();
                    HolidayUpdater.markStage(context, sbTmp);
                    zTmp = false;
                }
                final String sb = sbTmp;
                final boolean z = zTmp;
                handler.post(new Runnable() { // from class: com.salaryjump.app.HolidayUpdater.1.1
                    @Override // java.lang.Runnable
                    public void run() {
                        callback.onResult(z, sb);
                    }
                });
            }
        }).start();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String doUpdate(Context context) throws Exception {
        long j;
        String str;
        int i;
        int i2;
        int i3 = Calendar.getInstance().get(1);
        StringBuilder sb = new StringBuilder();
        ArrayList<int[]> arrayList = new ArrayList();
        HolidayNames.clear();
        long currentTimeMillis = System.currentTimeMillis();
        String str2 = null;
        lastNetError = null;
        ArrayList arrayList2 = new ArrayList();
        markStage(context, "开始拉取数据");
        int i4 = i3;
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        while (i4 <= i3 + 1) {
            String str3 = str2;
            int i8 = 0;
            while (true) {
                if (i8 >= MIRRORS.length) {
                    j = currentTimeMillis;
                    str = str3;
                    break;
                }
                long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
                if (currentTimeMillis2 <= DEADLINE_MS) {
                    int i9 = i8 + 1;
                    j = currentTimeMillis;
                    markStage(context, "请求 " + i4 + " 年（镜像 " + i9 + "/" + MIRRORS.length + "，已 " + (currentTimeMillis2 / 1000) + " 秒）");
                    str3 = fetch(String.format(Locale.US, MIRRORS[i8], Integer.valueOf(i4)));
                    if (str3 == null) {
                        i8 = i9;
                        currentTimeMillis = j;
                    } else {
                        str = str3;
                        break;
                    }
                } else {
                    throw new Exception("超时（已用 " + (currentTimeMillis2 / 1000) + " 秒），请检查网络后重试");
                }
            }
            if (str != null) {
                markStage(context, "已收到 " + i4 + " 年数据（" + str.length() + " 字节）");
                JSONArray optJSONArray = new JSONObject(str).optJSONArray("days");
                if (optJSONArray == null) {
                    i = i3;
                } else if (optJSONArray.length() == 0) {
                    i = i3;
                } else {
                    ArrayList arrayList3 = new ArrayList();
                    int i10 = 0;
                    while (i10 < optJSONArray.length()) {
                        JSONObject optJSONObject = optJSONArray.optJSONObject(i10);
                        if (optJSONObject != null) {
                            String optString = optJSONObject.optString("date");
                            String optString2 = optJSONObject.optString("name");
                            int optBoolean = optJSONObject.optBoolean("isOffDay") ? 1 : 0;
                            int parseStamp = parseStamp(optString);
                            if (parseStamp > 0) {
                                if (sb.length() > 0) {
                                    sb.append(';');
                                }
                                i2 = i3;
                                sb.append(parseStamp).append(':').append(optBoolean).append(':').append(clean(optString2));
                                HolidayNames.put(parseStamp, clean(optString2));
                                arrayList3.add(new int[]{parseStamp, optBoolean});
                                if (optBoolean != 0) {
                                    i6++;
                                } else {
                                    i7++;
                                }
                                i10++;
                                i3 = i2;
                            }
                        }
                        i2 = i3;
                        i10++;
                        i3 = i2;
                    }
                    i = i3;
                    arrayList.addAll(deriveStatutory(i4, arrayList3));
                    i5++;
                }
            } else {
                arrayList2.add(i4 + " 年：" + (lastNetError == null ? "全部镜像无响应" : lastNetError));
                i = i3;
            }
            i4++;
            i3 = i;
            currentTimeMillis = j;
            str2 = null;
        }
        if (i5 == 0) {
            markStage(context, "全部失败：" + (lastNetError == null ? "无响应" : lastNetError));
            StringBuilder sb2 = new StringBuilder("无法获取节假日数据。\n");
            if (lastNetError != null) {
                sb2.append("错误：").append(lastNetError).append("\n\n");
            }
            sb2.append("建议：\n· 打开设置页的「网络诊断」查看具体卡点\n· 检查系统是否限制了本应用的联网权限");
            throw new Exception(sb2.toString());
        }
        markStage(context, "解析完成，正在保存");
        SharedPreferences.Editor edit = context.getSharedPreferences("salary_jump", 0).edit();
        edit.putString(KEY_DATA, sb.toString());
        StringBuilder sb3 = new StringBuilder();
        for (int[] iArr : arrayList) {
            if (sb3.length() > 0) {
                sb3.append(';');
            }
            sb3.append(iArr[0]);
        }
        edit.putString(KEY_STATUTORY, sb3.toString());
        edit.putLong(KEY_UPDATED_AT, System.currentTimeMillis());
        edit.apply();
        return "已更新 " + i5 + " 年数据：" + i6 + " 天放假、" + i7 + " 天补班、法定 " + arrayList.size() + " 天";
    }

    private static String fetch(String str) {
        HttpURLConnection httpURLConnection;
        try {
            httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
            try {
                httpURLConnection.setConnectTimeout(CONNECT_MS);
                httpURLConnection.setReadTimeout(READ_MS);
                httpURLConnection.setRequestProperty("User-Agent", "SalaryJump/1.0");
                if (httpURLConnection.getResponseCode() != 200) {
                    return null;
                }
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    sb.append(readLine);
                }
                bufferedReader.close();
                String sb2 = sb.toString();
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                return sb2;
            } catch (Throwable th) {
                try {
                    lastNetError = th.getClass().getSimpleName() + (th.getMessage() == null ? "" : ": " + th.getMessage());
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                    return null;
                } finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                }
            }
        } catch (Throwable th2) {
            httpURLConnection = null;
            return null;
        }
    }

    private static int parseStamp(String str) {
        if (str == null || str.length() < 10) {
            return 0;
        }
        try {
            return (Integer.parseInt(str.substring(0, 4)) * 10000) + (Integer.parseInt(str.substring(5, 7)) * 100) + Integer.parseInt(str.substring(8, 10));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String clean(String str) {
        return str == null ? "" : str.replace(',', ' ').replace(';', ' ').replace(':', ' ');
    }

    private static List<int[]> deriveStatutory(int i, List<int[]> list) {
        boolean z;
        ArrayList arrayList = new ArrayList();
        int i2 = i * 10000;
        arrayList.add(new int[]{i2 + 101});
        arrayList.add(new int[]{i2 + 501});
        arrayList.add(new int[]{i2 + 502});
        arrayList.add(new int[]{i2 + 1001});
        arrayList.add(new int[]{i2 + 1002});
        arrayList.add(new int[]{i2 + 1003});
        String[] strArr = {"清明", "端午"};
        int i3 = 0;
        for (int i4 = 0; i4 < 2; i4++) {
            String str = strArr[i4];
            Iterator<int[]> it = list.iterator();
            while (true) {
                if (it.hasNext()) {
                    int[] next = it.next();
                    if (next[1] == 1 && HolidayNames.matches(next[0], str)) {
                        arrayList.add(new int[]{next[0]});
                        break;
                    }
                }
            }
        }
        int intValue = MID_AUTUMN.getOrDefault(Integer.valueOf(i), 0).intValue();
        Iterator<int[]> it2 = list.iterator();
        while (true) {
            if (!it2.hasNext()) {
                z = false;
                break;
            }
            int[] next2 = it2.next();
            if (next2[1] == 1 && HolidayNames.matches(next2[0], "中秋")) {
                z = true;
                break;
            }
        }
        if (z) {
            if (intValue > 0) {
                arrayList.add(new int[]{intValue});
            } else {
                Iterator<int[]> it3 = list.iterator();
                while (true) {
                    if (!it3.hasNext()) {
                        break;
                    }
                    int[] next3 = it3.next();
                    if (next3[1] == 1 && HolidayNames.matches(next3[0], "中秋")) {
                        arrayList.add(new int[]{next3[0]});
                        break;
                    }
                }
            }
        }
        ArrayList arrayList2 = new ArrayList();
        for (int[] iArr : list) {
            if (iArr[1] == 1 && HolidayNames.matches(iArr[0], "春节")) {
                arrayList2.add(Integer.valueOf(iArr[0]));
            }
        }
        sortInts(arrayList2);
        int i5 = 0;
        while (i5 < arrayList2.size() && isWeekend(((Integer) arrayList2.get(i5)).intValue())) {
            i5++;
        }
        while (i5 < arrayList2.size() && i3 < 4) {
            arrayList.add(new int[]{((Integer) arrayList2.get(i5)).intValue()});
            i3++;
            i5++;
        }
        return arrayList;
    }

    private static boolean isWeekend(int i) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(i / 10000, ((i / 100) % 100) - 1, i % 100);
        int i2 = calendar.get(7);
        return i2 == 1 || i2 == 7;
    }

    private static void sortInts(List<Integer> list) {
        int i = 0;
        while (i < list.size()) {
            int i2 = i + 1;
            for (int i3 = i2; i3 < list.size(); i3++) {
                if (list.get(i3).intValue() < list.get(i).intValue()) {
                    Integer num = list.get(i);
                    list.set(i, list.get(i3));
                    list.set(i3, num);
                }
            }
            i = i2;
        }
    }

    static final class HolidayNames {
        private static final HashMap<Integer, String> NAMES = new HashMap<>();

        HolidayNames() {
        }

        static void put(int i, String str) {
            NAMES.put(Integer.valueOf(i), str);
        }

        static boolean matches(int i, String str) {
            String str2 = NAMES.get(Integer.valueOf(i));
            return str2 != null && str2.contains(str);
        }

        static void clear() {
            NAMES.clear();
        }
    }
}
