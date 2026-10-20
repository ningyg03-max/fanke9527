package com.salaryjump.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.salaryjump.app.LeaveRecord;
import com.salaryjump.app.MainActivity;
import com.salaryjump.app.SalaryHistory;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/* loaded from: classes.dex */
public class MonthlyReportActivity extends Activity {
    private LinearLayout contentBox;
    private float density;
    private int month;
    private TextView monthTitleView;
    private LinearLayout root;
    private int year;
    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
    private final DecimalFormat intFormat = new DecimalFormat("#,##0");
    private double dailySalary = 300.0d;

    static /* synthetic */ int access$012(MonthlyReportActivity monthlyReportActivity, int i) {
        int i2 = monthlyReportActivity.month + i;
        monthlyReportActivity.month = i2;
        return i2;
    }

    static /* synthetic */ int access$108(MonthlyReportActivity monthlyReportActivity) {
        int i = monthlyReportActivity.year;
        monthlyReportActivity.year = i + 1;
        return i;
    }

    static /* synthetic */ int access$110(MonthlyReportActivity monthlyReportActivity) {
        int i = monthlyReportActivity.year;
        monthlyReportActivity.year = i - 1;
        return i;
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        MainActivity.applyTheme(this);
        this.density = getResources().getDisplayMetrics().density;
        Calendar calendar = Calendar.getInstance();
        calendar.add(2, -1);
        this.year = calendar.get(1);
        this.month = calendar.get(2) + 1;
        this.dailySalary = MainActivity.SalarySettings.load(getSharedPreferences("salary_jump", 0)).dailySalary;
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20.0f), dp(20.0f), dp(20.0f), dp(28.0f));
        scrollView.addView(linearLayout, new FrameLayout.LayoutParams(-1, -2));
        setContentView(UiKit.aurora(this, scrollView));
        MotionKit.enterChildren(this.root, this.density);
        this.root = linearLayout;
        buildHeader();
        this.contentBox = new LinearLayout(this);
        this.contentBox.setOrientation(1);
        linearLayout.addView(this.contentBox, matchWrap());
        render();
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
        button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MonthlyReportActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MonthlyReportActivity.this.finish();
            }
        });
        linearLayout.addView(button, new LinearLayout.LayoutParams(-2, dp(42.0f)));
        TextView textView = new TextView(this);
        textView.setText("月度战报");
        textView.setTextSize(20.0f);
        textView.setTextColor(MainActivity.COLOR_TEXT);
        textView.setTypeface(Typeface.create("sans", 1));
        textView.setGravity(17);
        linearLayout.addView(textView, new LinearLayout.LayoutParams(0, dp(42.0f), 1.0f));
        Button button2 = new Button(this);
        button2.setText("年度");
        button2.setTextSize(13.0f);
        button2.setTextColor(MainActivity.COLOR_ACCENT);
        button2.setAllCaps(false);
        button2.setMinWidth(0);
        button2.setMinHeight(0);
        button2.setPadding(dp(12.0f), 0, dp(12.0f), 0);
        button2.setBackground(MainActivity.softPill(dp(21.0f), dp(1.5f)));
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MonthlyReportActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                try {
                    MonthlyReportActivity.this.startActivity(new Intent(MonthlyReportActivity.this, (Class<?>) YearlyReportActivity.class));
                } catch (Throwable th) {
                }
            }
        });
        linearLayout.addView(button2, new LinearLayout.LayoutParams(-2, dp(42.0f)));
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(14.0f);
        this.root.addView(linearLayout2, matchWrap);
        linearLayout2.addView(monthNav("‹", -1), new LinearLayout.LayoutParams(dp(44.0f), dp(40.0f)));
        this.monthTitleView = new TextView(this);
        this.monthTitleView.setTextSize(17.0f);
        this.monthTitleView.setTextColor(MainActivity.COLOR_TEXT);
        this.monthTitleView.setGravity(17);
        this.monthTitleView.setTypeface(Typeface.create("sans", 1));
        linearLayout2.addView(this.monthTitleView, new LinearLayout.LayoutParams(0, dp(40.0f), 1.0f));
        linearLayout2.addView(monthNav("›", 1), new LinearLayout.LayoutParams(dp(44.0f), dp(40.0f)));
    }

    private TextView monthNav(String str, final int i) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(22.0f);
        textView.setTextColor(MainActivity.COLOR_BLUE);
        textView.setGravity(17);
        textView.setBackground(MainActivity.softPill(dp(20.0f), dp(1.5f)));
        textView.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MonthlyReportActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MonthlyReportActivity.access$012(MonthlyReportActivity.this, i);
                int i2 = MonthlyReportActivity.this.month;
                MonthlyReportActivity monthlyReportActivity = MonthlyReportActivity.this;
                if (i2 < 1) {
                    monthlyReportActivity.month = 12;
                    MonthlyReportActivity.access$110(MonthlyReportActivity.this);
                } else if (monthlyReportActivity.month > 12) {
                    MonthlyReportActivity.this.month = 1;
                    MonthlyReportActivity.access$108(MonthlyReportActivity.this);
                }
                MonthlyReportActivity.this.render();
            }
        });
        return textView;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void render() {
        String str;
        String str2;
        int i;
        this.monthTitleView.setText(this.year + " 年 " + this.month + " 月");
        this.contentBox.removeAllViews();
        List<SalaryHistory.Day> byMonth = SalaryHistory.byMonth(this, this.year, this.month);
        double monthTotalCents = SalaryHistory.monthTotalCents(this, this.year, this.month) / 100.0d;
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20.0f), dp(20.0f), dp(20.0f), dp(20.0f));
        linearLayout.setBackground(UiKit.hero(MainActivity.HERO_COLORS, dp(24.0f)));
        linearLayout.setElevation(dp(6.0f));
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(14.0f);
        this.contentBox.addView(linearLayout, matchWrap);
        TextView textView = new TextView(this);
        textView.setText("本月累计已赚");
        textView.setTextSize(11.5f);
        textView.setTextColor(-939524097);
        textView.setLetterSpacing(0.12f);
        textView.setTypeface(Typeface.create("sans", 1));
        linearLayout.addView(textView, wrapWrap());
        TextView textView2 = new TextView(this);
        textView2.setText("¥ " + this.moneyFormat.format(monthTotalCents));
        textView2.setTextSize(36.0f);
        textView2.setTextColor(-1);
        textView2.setTypeface(Typeface.create("sans", 1));
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(8.0f);
        linearLayout.addView(textView2, matchWrap2);
        int i2 = 0;
        long j = 0;
        for (SalaryHistory.Day day : byMonth) {
            i2++;
            if (day.cents > j) {
                j = day.cents;
            }
        }
        int statDays = MonthBackfill.statDays(this, this.year, this.month, i2);
        int countLeaveDays = countLeaveDays();
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        LinearLayout.LayoutParams matchWrap3 = matchWrap();
        matchWrap3.topMargin = dp(16.0f);
        linearLayout.addView(linearLayout2, matchWrap3);
        addStat(linearLayout2, String.valueOf(statDays), "计薪天数");
        if (monthTotalCents <= 0.0d || statDays <= 0) {
            str = "¥ ";
            str2 = "¥0";
        } else {
            str = "¥ ";
            str2 = "¥" + this.moneyFormat.format(monthTotalCents / statDays);
        }
        addStat(linearLayout2, str2, "日均");
        addStat(linearLayout2, countLeaveDays > 0 ? String.valueOf(countLeaveDays) : "0", "请假天数");
        LinearLayout card = card();
        LinearLayout.LayoutParams matchWrap4 = matchWrap();
        matchWrap4.topMargin = dp(14.0f);
        this.contentBox.addView(card, matchWrap4);
        addRowLabel(card, "最高的一天");
        TextView textView3 = new TextView(this);
        if (j > 0) {
            Iterator<SalaryHistory.Day> it = byMonth.iterator();
            while (true) {
                if (!it.hasNext()) {
                    i = 0;
                    break;
                }
                SalaryHistory.Day next = it.next();
                if (next.cents == j) {
                    i = next.stamp;
                    break;
                }
            }
            textView3.setText(str + this.moneyFormat.format(j / 100.0d) + "   ·   " + SalaryHistory.monthOf(i) + " 月 " + SalaryHistory.dayOf(i) + " 日");
        } else {
            textView3.setText("本月暂无记录");
        }
        textView3.setTextSize(20.0f);
        textView3.setTextColor(MainActivity.COLOR_ACCENT);
        textView3.setTypeface(Typeface.create("sans", 1));
        LinearLayout.LayoutParams matchWrap5 = matchWrap();
        matchWrap5.topMargin = dp(8.0f);
        card.addView(textView3, matchWrap5);
        buildGoalSection();
        if (byMonth.isEmpty()) {
            TextView textView4 = new TextView(this);
            textView4.setText("这个月还没有记录");
            textView4.setTextSize(15.0f);
            textView4.setTextColor(MainActivity.COLOR_MUTED);
            textView4.setGravity(17);
            textView4.setPadding(0, dp(44.0f), 0, dp(44.0f));
            textView4.setBackground(MainActivity.softCard(dp(18.0f)));
            LinearLayout.LayoutParams matchWrap6 = matchWrap();
            matchWrap6.topMargin = dp(14.0f);
            this.contentBox.addView(textView4, matchWrap6);
            return;
        }
        // 每日明细：日历网格（和「工资日历」页同一套组件，格式一致）
        LinearLayout card2 = card();
        LinearLayout.LayoutParams matchWrap7 = matchWrap();
        matchWrap7.topMargin = dp(14.0f);
        this.contentBox.addView(card2, matchWrap7);
        addRowLabel(card2, "每日明细");
        TextView hint = new TextView(this);
        hint.setText("日薪 / 加班费 / 外快 —— 想看某天详情去「工资日历」");
        hint.setTextSize(11.0f);
        hint.setTextColor(MainActivity.COLOR_LABEL);
        LinearLayout.LayoutParams hintParams = matchWrap();
        hintParams.topMargin = dp(2.0f);
        hintParams.bottomMargin = dp(8.0f);
        card2.addView(hint, hintParams);
        card2.addView(CalendarGrid.build(this, this.year, this.month, 0, 0, null), matchWrap());
    }

    private void buildGoalSection() {
        GoalStore.Goal goal = GoalStore.current(this);
        if (goal == null) {
            // 没有进行中的目标时，至少把已完成的历史展示出来
            buildCompletedGoals();
            return;
        }
        String string = goal.name;
        double longBitsToDouble = goal.targetCents / 100.0d;
        LinearLayout card = card();
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(14.0f);
        this.contentBox.addView(card, matchWrap);
        addRowLabel(card, "存钱罐");
        TextView textView = new TextView(this);
        textView.setText("🎯 " + string + "  " + this.moneyFormat.format(longBitsToDouble));
        textView.setTextSize(13.5f);
        textView.setTextColor(MainActivity.COLOR_TEXT);
        textView.setTypeface(Typeface.create("sans", 1));
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(8.0f);
        card.addView(textView, matchWrap2);

        // 开始日期 + 状态
        TextView goalSub = new TextView(this);
        goalSub.setText("开始日期 " + GoalStore.formatStamp(goal.startStamp)
                + "　·　" + (goal.isReached() ? "已达成" : "进行中")
                + "　·　已用 " + GoalStore.daysBetween(goal.startStamp, GoalStore.todayStamp()) + " 天");
        goalSub.setTextSize(11.0f);
        goalSub.setTextColor(MainActivity.COLOR_LABEL);
        LinearLayout.LayoutParams goalSubParams = matchWrap();
        goalSubParams.topMargin = dp(3.0f);
        card.addView(goalSub, goalSubParams);
        // 必须和主界面用同一个口径：只算「进度起算日」之后的收入。
        // （原来这里用 SalaryHistory.totalCents()，是全部历史总额，无视起算日
        //   —— 导致战报上的「累计已攒」等于用 App 以来的总收入。）
        double d = MainActivity.goalProgress(this);
        SheenBar sheenBar = new SheenBar(this);
        sheenBar.setProgress((float) Math.min(1.0d, d / longBitsToDouble));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(7.0f));
        layoutParams.topMargin = dp(10.0f);
        card.addView(sheenBar, layoutParams);
        TextView textView2 = new TextView(this);
        textView2.setText("累计已攒 " + this.moneyFormat.format(d) + " · 还差 " + this.moneyFormat.format(Math.max(0.0d, longBitsToDouble - d)));
        textView2.setTextSize(12.0f);
        textView2.setTextColor(MainActivity.COLOR_MUTED);
        LinearLayout.LayoutParams matchWrap3 = matchWrap();
        matchWrap3.topMargin = dp(9.0f);
        card.addView(textView2, matchWrap3);
        buildCompletedGoals();
    }

    /** 已完成的目标（历史）。每张卡：名称 · 起止日期 · 用时 · 目标 vs 实际。 */
    private void buildCompletedGoals() {
        java.util.List<GoalStore.Goal> done = GoalStore.completed(this);
        if (done.isEmpty()) {
            return;
        }
        LinearLayout card = card();
        LinearLayout.LayoutParams cardParams = matchWrap();
        cardParams.topMargin = dp(14.0f);
        this.contentBox.addView(card, cardParams);
        addRowLabel(card, "已完成的目标");

        for (int i = 0; i < done.size(); i++) {
            GoalStore.Goal goal = done.get(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(0);
            row.setGravity(16);
            LinearLayout.LayoutParams rowParams = matchWrap();
            rowParams.topMargin = dp(10.0f);
            card.addView(row, rowParams);

            LinearLayout left = new LinearLayout(this);
            left.setOrientation(1);
            row.addView(left, new LinearLayout.LayoutParams(0, -2, 1.0f));

            TextView nameView = new TextView(this);
            nameView.setText("🏆 " + goal.name);
            nameView.setTextSize(13.0f);
            nameView.setTextColor(MainActivity.COLOR_TEXT);
            nameView.setTypeface(Typeface.create("sans", 1));
            left.addView(nameView, matchWrap());

            TextView dateView = new TextView(this);
            int used = GoalStore.daysBetween(goal.startStamp, goal.doneStamp);
            dateView.setText(GoalStore.formatStampShort(goal.startStamp) + " → "
                    + GoalStore.formatStampShort(goal.doneStamp)
                    + "　用时 " + used + " 天");
            dateView.setTextSize(10.5f);
            dateView.setTextColor(MainActivity.COLOR_LABEL);
            LinearLayout.LayoutParams dateParams = matchWrap();
            dateParams.topMargin = dp(2.0f);
            left.addView(dateView, dateParams);

            LinearLayout right = new LinearLayout(this);
            right.setOrientation(1);
            right.setGravity(21);
            row.addView(right, wrapWrap());
            TextView amountView = new TextView(this);
            amountView.setText("¥" + this.moneyFormat.format(goal.frozenCents / 100.0d));
            amountView.setTextSize(13.5f);
            amountView.setTextColor(MainActivity.COLOR_ACCENT);
            amountView.setTypeface(Typeface.create("sans", 1));
            right.addView(amountView, wrapWrap());
            long over = goal.frozenCents - goal.targetCents;
            TextView overView = new TextView(this);
            overView.setText(over >= 0 ? "超出 ¥" + this.moneyFormat.format(over / 100.0d)
                    : "差 ¥" + this.moneyFormat.format(-over / 100.0d));
            overView.setTextSize(10.5f);
            overView.setTextColor(over >= 0 ? MainActivity.COLOR_ACCENT : MainActivity.COLOR_MUTED);
            LinearLayout.LayoutParams overParams = wrapWrap();
            overParams.topMargin = dp(2.0f);
            right.addView(overView, overParams);
        }
    }

    private int countLeaveDays() {
        int i = 0;
        for (LeaveRecord.Entry entry : LeaveRecord.load(this)) {
            if (SalaryHistory.yearOf(entry.stamp) == this.year && SalaryHistory.monthOf(entry.stamp) == this.month) {
                i++;
            }
        }
        return i;
    }

    private void addStat(LinearLayout linearLayout, String str, String str2) {
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        linearLayout2.setGravity(1);
        linearLayout.addView(linearLayout2, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(15.0f);
        textView.setTextColor(-1);
        textView.setGravity(17);
        textView.setTypeface(Typeface.create("sans", 1));
        linearLayout2.addView(textView, matchWrap());
        TextView textView2 = new TextView(this);
        textView2.setText(str2);
        textView2.setTextSize(10.5f);
        textView2.setTextColor(-1191182337);
        textView2.setGravity(17);
        LinearLayout.LayoutParams wrapWrap = wrapWrap();
        wrapWrap.topMargin = dp(3.0f);
        linearLayout2.addView(textView2, wrapWrap);
    }

    private void addRowLabel(LinearLayout linearLayout, String str) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(11.5f);
        textView.setTextColor(MainActivity.COLOR_LABEL);
        textView.setLetterSpacing(0.12f);
        textView.setTypeface(Typeface.create("sans", 1));
        linearLayout.addView(textView, matchWrap());
    }

    private LinearLayout card() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20.0f), dp(20.0f), dp(20.0f), dp(20.0f));
        boolean z = (getResources().getConfiguration().uiMode & 48) == 32;
        linearLayout.setBackground(UiKit.glowCard(MainActivity.COLOR_SURFACE, MainActivity.COLOR_ACCENT_BRIGHT, z ? 43 : 46, z ? 28 : 43, dp(24.0f)));
        linearLayout.setElevation(dp(4.0f));
        return linearLayout;
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        if (PrivacyLock.isEnabled(this) && !MainActivity.isAppUnlocked()) {
            finish();
        }
    }

    int dp(float f) {
        return Math.round(f * this.density);
    }

    LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    LinearLayout.LayoutParams wrapWrap() {
        return new LinearLayout.LayoutParams(-2, -2);
    }
}
