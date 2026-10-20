package com.salaryjump.app;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Calendar;

/**
 * 日历网格（7 列 × N 周），每格显示「日期 + 日薪 / 加班费 / 外快」。
 *
 * <p>被两个页面共用：
 * <ul>
 *   <li>{@code SalaryCalendarActivity} —— 可点选，点某天在下方看详情</li>
 *   <li>{@code MonthlyReportActivity} —— 只读，作为「每日明细」的日历视图</li>
 * </ul>
 *
 * <p>抽出来的原因：两个页面展示同一批数据，格式必须一致；
 * 复制一份迟早会走样（v3.64 就吃过「同一份逻辑两处不一致」的亏）。
 */
final class CalendarGrid {
    private static final String[] WEEK_NAMES = {"日", "一", "二", "三", "四", "五", "六"};

    /** 点某一天的回调。cell 用于调用方自行切换高亮底色。 */
    interface DayListener {
        void onDay(int stamp, LinearLayout cell);
    }

    private CalendarGrid() {
    }

    static int daysInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private static int dp(Activity activity, float value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }

    /**
     * 构建整个网格卡片。
     *
     * @param selectedStamp 需要高亮的日期戳；0 表示不高亮
     * @param listener      点某天回调；null 表示只读
     */
    static LinearLayout build(Activity activity, int year, int month,
                              int todayStamp, int selectedStamp, DayListener listener) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(1);
        card.setPadding(dp(activity, 10.0f), dp(activity, 12.0f),
                dp(activity, 10.0f), dp(activity, 12.0f));
        card.setBackground(MainActivity.softCard(dp(activity, 18.0f)));

        // 星期表头
        LinearLayout head = new LinearLayout(activity);
        head.setOrientation(0);
        card.addView(head, matchWrap());
        for (int i = 0; i < 7; i++) {
            TextView textView = new TextView(activity);
            textView.setText(WEEK_NAMES[i]);
            textView.setTextSize(10.0f);
            textView.setTextColor(MainActivity.COLOR_LABEL);
            textView.setGravity(17);
            head.addView(textView, new LinearLayout.LayoutParams(0, dp(activity, 18.0f), 1.0f));
        }

        Calendar first = Calendar.getInstance();
        first.set(year, month - 1, 1);
        int leading = first.get(7) - 1;                 // 1 号前空几格（周日 = 0）
        int total = daysInMonth(year, month);
        int rows = (int) Math.ceil((leading + total) / 7.0d);

        int day = 1;
        for (int r = 0; r < rows; r++) {
            LinearLayout row = new LinearLayout(activity);
            row.setOrientation(0);
            LinearLayout.LayoutParams rowParams = matchWrap();
            rowParams.topMargin = dp(activity, 4.0f);
            card.addView(row, rowParams);

            for (int c = 0; c < 7; c++) {
                LinearLayout.LayoutParams cellParams =
                        new LinearLayout.LayoutParams(0, dp(activity, 62.0f), 1.0f);
                cellParams.leftMargin = c == 0 ? 0 : dp(activity, 2.0f);
                LinearLayout cell = new LinearLayout(activity);
                cell.setOrientation(1);
                cell.setGravity(17);
                cell.setPadding(dp(activity, 1.0f), dp(activity, 4.0f),
                        dp(activity, 1.0f), dp(activity, 4.0f));
                row.addView(cell, cellParams);

                if (r == 0 && c < leading || day > total) {
                    continue;
                }
                final int stamp = (year * 10000) + (month * 100) + day;
                fillCell(activity, cell, stamp, day, todayStamp, selectedStamp, listener);
                day++;
            }
        }
        return card;
    }

    private static void fillCell(final Activity activity, final LinearLayout cell, final int stamp,
                                 int dayOfMonth, int todayStamp, int selectedStamp,
                                 final DayListener listener) {
        DayBreakdown bd = DayBreakdown.of(activity, stamp);
        boolean isToday = stamp == todayStamp;
        boolean isSelected = selectedStamp != 0 && stamp == selectedStamp;
        applyBackground(activity, cell, bd, isToday, isSelected);

        TextView dayView = new TextView(activity);
        dayView.setText(String.valueOf(dayOfMonth));
        dayView.setTextSize(11.0f);
        dayView.setGravity(17);
        boolean dim = (!bd.isWorkday && !bd.hasRecord) || stamp > todayStamp;
        dayView.setTextColor(isSelected || isToday ? MainActivity.COLOR_ACCENT
                : (dim ? MainActivity.COLOR_LABEL : MainActivity.COLOR_TEXT));
        dayView.setTypeface(Typeface.create("sans", isSelected || isToday ? 1 : 0));
        cell.addView(dayView, matchWrap());

        // 补班标记：补班日是周末却要上班，且没有额外工资 —— 标出来免得看糊涂
        if (bd.makeupNote != null) {
            addLine(activity, cell, "补班", MainActivity.COLOR_LABEL, 9.0f);
        }

        if (bd.hasOvertime()) {
            addLine(activity, cell, DayBreakdown.formatCompact(bd.baseCents), MainActivity.COLOR_ACCENT, 10.0f);
            addLine(activity, cell, "+" + DayBreakdown.formatCompact(bd.overtimeCents), MainActivity.COLOR_AMBER, 9.5f);
            if (bd.extraCents > 0) {
                addLine(activity, cell, DayBreakdown.extraCellText(bd.extraCents), MainActivity.COLOR_EXTRA, 9.5f);
            }
        } else if (bd.extraCents > 0) {
            if (bd.baseCents > 0) {
                addLine(activity, cell, DayBreakdown.formatCompact(bd.baseCents), MainActivity.COLOR_ACCENT, 10.0f);
            }
            addLine(activity, cell, DayBreakdown.extraCellText(bd.extraCents), MainActivity.COLOR_EXTRA, 9.5f);
        } else if (bd.hasMoney()) {
            addLine(activity, cell, DayBreakdown.formatCompact(bd.totalCents), MainActivity.COLOR_ACCENT, 10.0f);
        } else {
            String tag = bd.cellTag();
            if (tag != null) {
                addLine(activity, cell, tag, bd.isComp ? MainActivity.COLOR_AMBER : MainActivity.COLOR_LABEL, 9.0f);
            }
        }

        if (listener != null) {
            cell.setClickable(true);
            cell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onDay(stamp, cell);
                }
            });
        }
    }

    private static void addLine(Activity activity, LinearLayout cell, String text, int color, float size) {
        if (text == null || text.length() == 0) {
            return;
        }
        TextView textView = new TextView(activity);
        textView.setText(text);
        textView.setTextSize(size);
        textView.setGravity(17);
        textView.setTextColor(color);
        textView.setTypeface(Typeface.create("sans", 1));
        textView.setIncludeFontPadding(false);
        cell.addView(textView, matchWrap());
    }

    /** 格子的常规底色（选中 / 今天 / 有记录 / 休息日）。 */
    static void applyBackground(Activity activity, LinearLayout cell, DayBreakdown bd,
                                boolean isToday, boolean isSelected) {
        if (isSelected) {
            cell.setBackground(MainActivity.softPill(dp(activity, 10.0f), dp(activity, 1.5f)));
            return;
        }
        boolean rest = !bd.isWorkday && !bd.hasRecord;
        cell.setBackground(isToday || !rest ? MainActivity.softCard(dp(activity, 10.0f)) : null);
    }

    /** 选中态底色（点选时用）。 */
    static void applySelectedBackground(Activity activity, LinearLayout cell) {
        cell.setBackground(MainActivity.softPill(dp(activity, 10.0f), dp(activity, 1.5f)));
    }

    private static LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }
}
