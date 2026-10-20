package com.salaryjump.app;

import android.content.Context;
import android.content.SharedPreferences;
import com.salaryjump.app.MainActivity;
import com.salaryjump.app.OvertimeLog;
import com.salaryjump.app.SalaryHistory;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/* loaded from: classes.dex */
final class PetState {
    static final int DESK_GRACE_MINUTES = 120;
    private static final String KEY_LAST_QUOTE = "pet_last_quote";
    /** 上一条点击语录（避免连续重复） */
    private static final String KEY_POKE_QUOTE = "pet_last_poke_quote";

    private PetState() {
    }

    static int fatigueLevel(Context context) {
        int workStreak = workStreak(context);
        if (workStreak >= 10) {
            return 3;
        }
        if (workStreak >= 6) {
            return 2;
        }
        if (workStreak >= 3) {
            return 1;
        }
        return 0;
    }

    static int workStreak(Context context) {
        boolean z;
        if (context == null) {
            return 0;
        }
        try {
            MainActivity.SalarySettings load = MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0));
            List<SalaryHistory.Day> load2 = SalaryHistory.load(context);
            if (load2.isEmpty()) {
                return 0;
            }
            int i = 0;
            for (SalaryHistory.Day day : load2) {
                if (MonthBackfill.centsOn(context, day.stamp) <= 0) {
                    if (i == 0 || day.stamp < i) {
                        i = day.stamp;
                    }
                }
            }
            if (i == 0) {
                return 0;
            }
            Calendar calendar = Calendar.getInstance();
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            for (int i2 = 0; i2 < 60; i2++) {
                boolean z2 = true;
                int i3 = (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
                if (i3 < i) {
                    break;
                }
                boolean isWorkday = WorkdayHelper.isWorkday(context, load, calendar);
                boolean has = LeaveRecord.has(context, i3);
                if (!isWorkday || has) {
                    z = false;
                } else {
                    z = true;
                }
                arrayList.add(Boolean.valueOf(z));
                if (isWorkday && !has) {
                    try {
                        if (OvertimeLog.secondsOfDay(context, i3, System.currentTimeMillis()) <= 0.0d) {
                            z2 = false;
                        }
                    } catch (Throwable th) {
                    }
                    arrayList2.add(Boolean.valueOf(z2));
                    calendar.add(5, -1);
                }
                z2 = false;
                arrayList2.add(Boolean.valueOf(z2));
                calendar.add(5, -1);
            }
            int size = arrayList.size();
            boolean[] zArr = new boolean[size];
            boolean[] zArr2 = new boolean[arrayList2.size()];
            for (int i4 = 0; i4 < size; i4++) {
                zArr[i4] = ((Boolean) arrayList.get(i4)).booleanValue();
                zArr2[i4] = ((Boolean) arrayList2.get(i4)).booleanValue();
            }
            return scoreSequence(zArr, zArr2);
        } catch (Throwable th2) {
            return 0;
        }
    }

    static int scoreSequence(boolean[] zArr, boolean[] zArr2) {
        int i = 0;
        for (int length = zArr.length - 1; length >= 0; length--) {
            if (zArr[length]) {
                i++;
                if (length < zArr2.length && zArr2[length]) {
                    i++;
                }
            } else {
                i = Math.max(0, i - 2);
            }
        }
        return i;
    }

    static boolean isAtDesk(Context context, Calendar calendar) {
        boolean z;
        if (context == null || calendar == null) {
            return false;
        }
        try {
            MainActivity.SalarySettings load = MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0));
            int i = (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
            try {
                OvertimeLog.Punch load2 = OvertimeLog.load(context);
                if (load2.day == i) {
                    if (load2.running()) {
                        return true;
                    }
                }
            } catch (Throwable th) {
            }
            int i2 = (calendar.get(11) * 60) + calendar.get(12);
            if (i2 < load.workStartMinutes) {
                return false;
            }
            if ((!WorkdayHelper.isWorkday(context, load, calendar) || LeaveRecord.has(context, i)) && !HolidayWork.has(context, i)) {
                z = false;
            } else {
                z = true;
            }
            if (!z) {
                return false;
            }
            if (i2 > load.workEndMinutes + DESK_GRACE_MINUTES) {
                return false;
            }
            return true;
        } catch (Throwable th2) {
            return false;
        }
    }

    static int daysToPayday(Context context) {
        if (context == null || !PaydayReminder.isEnabled(context)) {
            return -1;
        }
        try {
            Calendar calendar = Calendar.getInstance();
            int i = 1;
            int i2 = calendar.get(1);
            int i3 = calendar.get(2) + 1;
            int i4 = calendar.get(5);
            int effectiveDay = PaydayReminder.effectiveDay(i2, i3, PaydayReminder.day(context));
            if (i4 <= effectiveDay) {
                return effectiveDay - i4;
            }
            if (i3 != 12) {
                i = i3 + 1;
            }
            return (PaydayReminder.daysInMonth(i2, i3) - i4) + PaydayReminder.effectiveDay(i3 == 12 ? i2 + 1 : i2, i, PaydayReminder.day(context));
        } catch (Throwable th) {
            return -1;
        }
    }

    static String quote(Context context) {
        if (context == null) {
            return "";
        }
        String last = "";
        try {
            last = context.getSharedPreferences("salary_jump", 0).getString(KEY_LAST_QUOTE, "");
        } catch (Throwable unused) {
        }
        String chosen = PetQuotes.pick(PetQuotes.auto(context), last);
        try {
            context.getSharedPreferences("salary_jump", 0).edit()
                    .putString(KEY_LAST_QUOTE, chosen).apply();
        } catch (Throwable unused) {
        }
        return chosen;
    }

    static String pokeText(Context context) {
        // 语录池见 PetQuotes；顺带记下上一条，避免连续重复
        String last = "";
        try {
            last = context == null ? "" : context.getSharedPreferences("salary_jump", 0)
                    .getString(KEY_POKE_QUOTE, "");
        } catch (Throwable unused) {
        }
        String chosen = PetQuotes.pick(PetQuotes.poke(context), last);
        try {
            if (context != null) {
                context.getSharedPreferences("salary_jump", 0).edit()
                        .putString(KEY_POKE_QUOTE, chosen).apply();
            }
        } catch (Throwable unused) {
        }
        return chosen;
    }

}
