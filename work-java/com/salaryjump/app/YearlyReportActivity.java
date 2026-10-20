package com.salaryjump.app;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.salaryjump.app.ExtraIncome;
import com.salaryjump.app.HolidayWork;
import com.salaryjump.app.LeaveRecord;
import com.salaryjump.app.SalaryHistory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

/* loaded from: classes.dex */
public class YearlyReportActivity extends Activity {
    private LinearLayout contentBox;
    private float density;
    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
    private LinearLayout root;
    private int year;

    static /* synthetic */ int access$012(YearlyReportActivity yearlyReportActivity, int i) {
        int i2 = yearlyReportActivity.year + i;
        yearlyReportActivity.year = i2;
        return i2;
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        MainActivity.applyTheme(this);
        this.density = getResources().getDisplayMetrics().density;
        this.year = Calendar.getInstance().get(1);
        configureWindow();
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        LinearLayout linearLayout = new LinearLayout(this);
        this.root = linearLayout;
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20.0f), dp(18.0f), dp(20.0f), dp(26.0f));
        scrollView.addView(linearLayout, new FrameLayout.LayoutParams(-1, -2));
        setContentView(UiKit.aurora(this, scrollView));
        MotionKit.enterChildren(linearLayout, this.density);
        buildHeader();
        buildYearNav();
        this.contentBox = new LinearLayout(this);
        this.contentBox.setOrientation(1);
        linearLayout.addView(this.contentBox, matchWrap());
        render();
    }

    private void configureWindow() {
        MainActivity.applyEdgeToEdge(getWindow());
    }

    private void buildHeader() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        this.root.addView(linearLayout, matchWrap());
        Button button = new Button(this);
        button.setText("返回");
        button.setTextSize(13.0f);
        button.setTextColor(MainActivity.COLOR_TEXT);
        button.setAllCaps(false);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setPadding(dp(12.0f), 0, dp(12.0f), 0);
        button.setBackground(MainActivity.softPill(dp(21.0f), dp(1.5f)));
        button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.YearlyReportActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                YearlyReportActivity.this.finish();
            }
        });
        linearLayout.addView(button, new LinearLayout.LayoutParams(-2, dp(42.0f)));
        TextView textView = new TextView(this);
        textView.setText("年度总览");
        textView.setTextSize(20.0f);
        textView.setTextColor(MainActivity.COLOR_TEXT);
        textView.setTypeface(Typeface.create("sans", 1));
        textView.setGravity(17);
        linearLayout.addView(textView, new LinearLayout.LayoutParams(0, dp(42.0f), 1.0f));
        Button button2 = new Button(this);
        button2.setText("返回");
        button2.setTextSize(13.0f);
        button2.setAllCaps(false);
        button2.setMinWidth(0);
        button2.setMinHeight(0);
        button2.setPadding(dp(12.0f), 0, dp(12.0f), 0);
        button2.setVisibility(4);
        linearLayout.addView(button2, new LinearLayout.LayoutParams(-2, dp(42.0f)));
    }

    private void buildYearNav() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(14.0f);
        this.root.addView(linearLayout, matchWrap);
        linearLayout.addView(yearNav("‹", -1), new LinearLayout.LayoutParams(dp(44.0f), dp(40.0f)));
        TextView textView = new TextView(this);
        textView.setText(String.valueOf(this.year));
        textView.setVisibility(4);
        linearLayout.addView(textView, new LinearLayout.LayoutParams(0, dp(40.0f), 1.0f));
        linearLayout.addView(yearNav("›", 1), new LinearLayout.LayoutParams(dp(44.0f), dp(40.0f)));
    }

    private TextView yearNav(String str, final int i) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(20.0f);
        textView.setTextColor(MainActivity.COLOR_BLUE);
        textView.setGravity(17);
        textView.setBackground(MainActivity.softPill(dp(20.0f), dp(1.5f)));
        textView.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.YearlyReportActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                YearlyReportActivity.access$012(YearlyReportActivity.this, i);
                YearlyReportActivity.this.render();
            }
        });
        return textView;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void render() {
        int i;
        int i2;
        boolean z;
        int i3;
        boolean z2;
        double d;
        int i4;
        int dp;
        int[] iArr;
        this.contentBox.removeAllViews();
        int i5 = this.year;
        long[] jArr = new long[12];
        ArrayList arrayList = new ArrayList();
        int i6 = 0;
        int i7 = 0;
        long j = 0;
        for (int i8 = 1; i8 <= 12; i8++) {
            int i9 = 0;
            for (SalaryHistory.Day day : SalaryHistory.byMonth(this, i5, i8)) {
                arrayList.add(day);
                int i10 = i8 - 1;
                jArr[i10] = jArr[i10] + day.cents;
                j += day.cents;
                i9++;
            }
            i7 += MonthBackfill.statDays(this, i5, i8, i9);
        }
        long j2 = 0;
        for (int i11 = 1; i11 <= 12; i11++) {
            j2 += SalaryHistory.monthTotalCents(this, i5 - 1, i11);
        }
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20.0f), dp(20.0f), dp(20.0f), dp(20.0f));
        linearLayout.setBackground(UiKit.hero(MainActivity.HERO_COLORS, dp(24.0f)));
        linearLayout.setElevation(dp(6.0f));
        this.contentBox.addView(linearLayout, matchWrap());
        TextView text = text("全年总收入", 12.0f, -939524097, 0);
        text.setLetterSpacing(0.12f);
        linearLayout.addView(text, wrapWrap());
        double d2 = j / 100.0d;
        TextView text2 = text("¥ " + this.moneyFormat.format(d2), 36.0f, -1, 1);
        text2.setIncludeFontPadding(false);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        long j3 = j;
        matchWrap.topMargin = dp(10.0f);
        linearLayout.addView(text2, matchWrap);
        if (j2 > 0) {
            i = i7;
            double d3 = j3 - j2;
            double d4 = d3 / 100.0d;
            String str = d4 >= 0.0d ? "+" : "−";
            TextView text3 = text("较去年 " + str + this.moneyFormat.format(Math.abs(d4)) + " 元（" + str + String.format(Locale.US, "%.1f", Double.valueOf(Math.abs(d3 / j2) * 100.0d)) + "%）", 12.0f, -939524097, 0);
            LinearLayout.LayoutParams matchWrap2 = matchWrap();
            matchWrap2.topMargin = dp(8.0f);
            linearLayout.addView(text3, matchWrap2);
        } else {
            i = i7;
            TextView text4 = text("暂无去年数据对比", 12.0f, -1929379841, 0);
            LinearLayout.LayoutParams matchWrap3 = matchWrap();
            matchWrap3.topMargin = dp(8.0f);
            linearLayout.addView(text4, matchWrap3);
        }
        LinearLayout card = card();
        LinearLayout.LayoutParams matchWrap4 = matchWrap();
        matchWrap4.topMargin = dp(14.0f);
        this.contentBox.addView(card, matchWrap4);
        card.addView(text(i5 + " 年 · 每月收入", 12.5f, MainActivity.COLOR_TEXT, 1), wrapWrap());
        long j4 = 0;
        for (int i12 = 0; i12 < 12; i12++) {
            j4 = Math.max(j4, jArr[i12]);
        }
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        int i13 = 80;
        linearLayout2.setGravity(80);
        matchWrap().topMargin = dp(12.0f);
        card.addView(linearLayout2, new LinearLayout.LayoutParams(-1, dp(96.0f)));
        int i14 = 0;
        while (i14 < 12) {
            LinearLayout linearLayout3 = new LinearLayout(this);
            linearLayout3.setOrientation(1);
            linearLayout3.setGravity(i13);
            View view = new View(this);
            if (j4 > 0) {
                i4 = i6;
                d = d2;
                dp = Math.max(dp(4.0f), (int) Math.round(dp(90.0f) * (jArr[i14] / j4)));
            } else {
                d = d2;
                i4 = i6;
                dp = dp(4.0f);
            }
            GradientDrawable.Orientation orientation = GradientDrawable.Orientation.TOP_BOTTOM;
            if (jArr[i14] > 0) {
                iArr = new int[2];
                iArr[i4] = -13773633;
                iArr[1] = -15815028;
            } else {
                iArr = new int[2];
                iArr[i4] = MainActivity.COLOR_TRACK;
                iArr[1] = MainActivity.COLOR_TRACK;
            }
            GradientDrawable gradientDrawable = new GradientDrawable(orientation, iArr);
            gradientDrawable.setCornerRadius(dp(4.0f));
            view.setBackground(gradientDrawable);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp);
            layoutParams.leftMargin = dp(1.5f);
            layoutParams.rightMargin = dp(1.5f);
            linearLayout3.addView(view, layoutParams);
            i6 = i4;
            linearLayout2.addView(linearLayout3, new LinearLayout.LayoutParams(i6, -1, 1.0f));
            i14++;
            d2 = d;
            i13 = 80;
        }
        double d5 = d2;
        LinearLayout linearLayout4 = new LinearLayout(this);
        linearLayout4.setOrientation(i6);
        LinearLayout.LayoutParams matchWrap5 = matchWrap();
        matchWrap5.topMargin = dp(5.0f);
        card.addView(linearLayout4, matchWrap5);
        for (int i15 = 1; i15 <= 12; i15++) {
            TextView text5 = text(String.valueOf(i15), 9.0f, MainActivity.COLOR_LABEL, 0);
            text5.setGravity(17);
            linearLayout4.addView(text5, new LinearLayout.LayoutParams(0, -2, 1.0f));
        }
        long j5 = 0;
        for (ExtraIncome.Entry entry : ExtraIncome.load(this)) {
            if (SalaryHistory.yearOf(entry.stamp) == i5) {
                j5 += entry.cents;
            }
        }
        // 加班：OvertimeLog 只存最后一次打卡，历史记录留不下来，
        // 所以从每日金额反推（当天入账 − 应得 > 0 即那天有加班）
        long[] overtime = YearlyStats.overtimeSummary(this, i5);
        double countLeaveDays = countLeaveDays(i5);
        Iterator<HolidayWork.Entry> it = HolidayWork.load(this).iterator();
        int i16 = 0;
        while (it.hasNext()) {
            if (SalaryHistory.yearOf(it.next().stamp) == i5) {
                i16++;
            }
        }
        int i17 = 0;
        for (int i18 = 1; i18 < 12; i18++) {
            if (jArr[i18] > jArr[i17]) {
                i17 = i18;
            }
        }
        double d6 = i > 0 ? d5 / i : 0.0d;
        LinearLayout card2 = card();
        LinearLayout.LayoutParams matchWrap6 = matchWrap();
        matchWrap6.topMargin = dp(14.0f);
        this.contentBox.addView(card2, matchWrap6);
        card2.addView(text("全年统计", 12.5f, MainActivity.COLOR_TEXT, 1), wrapWrap());
        LinearLayout statRow = statRow();
        card2.addView(statRow, statRowParams());
        // 8 项 → 2 行 × 4 列
        addStat(statRow, String.valueOf(i), "计薪天数");
        addStat(statRow, countLeaveDays > 0.0d ? trimNum(countLeaveDays) : "0", "请假天数");
        addStat(statRow, String.valueOf(i16), "休息日出勤");
        addStat(statRow, overtime[0] > 0 ? String.valueOf(overtime[0]) : "0", "加班次数");
        LinearLayout statRow2 = statRow();
        card2.addView(statRow2, statRowParams());
        addStat(statRow2, overtime[1] > 0 ? "¥" + this.moneyFormat.format(overtime[1] / 100.0d) : "¥0", "加班费总额");
        addStat(statRow2, j5 > 0 ? "¥" + this.moneyFormat.format(j5 / 100.0d) : "¥0", "外快总额");
        addStat(statRow2, jArr[i17] > 0 ? (i17 + 1) + " 月" : "—", "最佳月份");
        addStat(statRow2, d6 > 0.0d ? "¥" + this.moneyFormat.format(d6) : "¥0", "日均");
        LinearLayout card3 = card();
        LinearLayout.LayoutParams matchWrap7 = matchWrap();
        matchWrap7.topMargin = dp(14.0f);
        this.contentBox.addView(card3, matchWrap7);
        card3.addView(text("假期使用（" + i5 + " 年）", 12.5f, MainActivity.COLOR_TEXT, 1), wrapWrap());
        boolean z3 = i5 == Calendar.getInstance().get(1);
        double usedInYear = LeaveAccounts.usedInYear(this, "年假", i5);
        if (z3) {
            i2 = 0;
            z = LeaveAccounts.isSet(this, 0);
        } else {
            i2 = 0;
            z = false;
        }
        addLeaveRow(card3, "🏖 年假", usedInYear, z, z3 ? LeaveAccounts.remaining(this, i2) : 0.0d, 0);
        double usedInYear2 = LeaveAccounts.usedInYear(this, "育儿假", i5);
        if (z3) {
            i3 = 1;
            z2 = LeaveAccounts.isSet(this, 1);
        } else {
            i3 = 1;
            z2 = false;
        }
        addLeaveRow(card3, "👶 育儿假", usedInYear2, z2, z3 ? LeaveAccounts.remaining(this, i3) : 0.0d, 1);
        double usedInYear3 = LeaveAccounts.usedInYear(this, "调休", i5);
        LinearLayout linearLayout5 = new LinearLayout(this);
        linearLayout5.setOrientation(0);
        linearLayout5.setGravity(16);
        LinearLayout.LayoutParams matchWrap8 = matchWrap();
        matchWrap8.topMargin = dp(10.0f);
        card3.addView(linearLayout5, matchWrap8);
        linearLayout5.addView(text("📅 调休", 13.0f, MainActivity.COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, -2, 1.0f));
        String str2 = "已休 " + trimNum(usedInYear3) + " 天";
        if (z3 && LeaveAccounts.compSet(this)) {
            str2 = str2 + " · 剩 " + WorkRules.formatDays(LeaveAccounts.compRemaining(this)) + " 天";
        }
        linearLayout5.addView(text(str2, 12.5f, MainActivity.COLOR_MUTED, 0), wrapWrap());
    }

    private double countLeaveDays(int i) {
        double d = 0.0d;
        for (LeaveRecord.Entry entry : LeaveRecord.load(this)) {
            if (SalaryHistory.yearOf(entry.stamp) == i) {
                d += entry.half ? 0.5d : 1.0d;
            }
        }
        return d;
    }

    private void addLeaveRow(LinearLayout linearLayout, String str, double d, boolean z, double d2, int i) {
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(10.0f);
        linearLayout.addView(linearLayout2, matchWrap);
        linearLayout2.addView(text(str, 13.0f, MainActivity.COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, -2, 1.0f));
        String str2 = "已用 " + trimNum(d) + " 天";
        if (z) {
            str2 = str2 + " · 剩 " + WorkRules.formatDays(d2) + " 天";
        }
        linearLayout2.addView(text(str2, 12.5f, MainActivity.COLOR_MUTED, 0), wrapWrap());
    }

    private String trimNum(double d) {
        return WorkRules.formatDays(d);
    }

    private LinearLayout statRow() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        return linearLayout;
    }

    private LinearLayout.LayoutParams statRowParams() {
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(12.0f);
        return matchWrap;
    }

    private void addStat(LinearLayout linearLayout, String str, String str2) {
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        linearLayout.addView(linearLayout2, new LinearLayout.LayoutParams(0, -2, 1.0f));
        // 4 项一行 → 字号收小，长数值（¥3,456.78）也要放得下
        linearLayout2.addView(text(str, 12.5f, MainActivity.COLOR_TEXT, 1), wrapWrap());
        LinearLayout.LayoutParams wrapWrap = wrapWrap();
        wrapWrap.topMargin = dp(3.0f);
        linearLayout2.addView(text(str2, 9.5f, MainActivity.COLOR_LABEL, 0), wrapWrap);
    }

    private LinearLayout card() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(18.0f), dp(16.0f), dp(18.0f), dp(16.0f));
        boolean z = (getResources().getConfiguration().uiMode & 48) == 32;
        linearLayout.setBackground(UiKit.glowCard(MainActivity.COLOR_SURFACE, MainActivity.COLOR_ACCENT_BRIGHT, z ? 43 : 46, z ? 28 : 43, dp(22.0f)));
        linearLayout.setElevation(dp(3.0f));
        return linearLayout;
    }

    private TextView text(String str, float f, int i, int i2) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(f);
        textView.setTextColor(i);
        if (i2 != 0) {
            textView.setTypeface(Typeface.create("sans", 1));
        }
        return textView;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private LinearLayout.LayoutParams wrapWrap() {
        return new LinearLayout.LayoutParams(-2, -2);
    }

    private int dp(float f) {
        return Math.round(f * this.density);
    }
}
