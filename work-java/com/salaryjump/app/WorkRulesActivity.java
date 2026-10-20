package com.salaryjump.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/* loaded from: classes.dex */
public class WorkRulesActivity extends Activity {
    private float density;
    private LinearLayout root;
    private LinearLayout statutoryContent;
    private LinearLayout weekdayContent;
    private LinearLayout weekendContent;

    private interface OnChoice {
        void onChoice(int i);
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        MainActivity.applyTheme(this);
        this.density = getResources().getDisplayMetrics().density;
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        LinearLayout linearLayout = new LinearLayout(this);
        this.root = linearLayout;
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20.0f), dp(20.0f), dp(20.0f), dp(28.0f));
        scrollView.addView(linearLayout, new FrameLayout.LayoutParams(-1, -2));
        setContentView(UiKit.aurora(this, scrollView));
        MotionKit.enterChildren(this.root, this.density);
        buildHeader();
        buildWeekdaySection();
        buildWeekendSection();
        buildStatutorySection();
        buildBalanceSection();
        buildFooter();
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
        button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.WorkRulesActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                WorkRulesActivity.this.finish();
            }
        });
        linearLayout.addView(button, new LinearLayout.LayoutParams(-2, dp(42.0f)));
        TextView textView = new TextView(this);
        textView.setText("加班与出勤规则");
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
        TextView text = text("按你公司的规定设置，改完立即生效。之后在主页打卡、工资日历、累计都会自动按这里的规则执行。", 12.0f, MainActivity.COLOR_MUTED, 0);
        text.setLineSpacing(dp(2.0f), 1.05f);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(12.0f);
        this.root.addView(text, matchWrap);
    }

    private void buildWeekdaySection() {
        this.root.addView(sectionHead("⏱", "工作日加班", "下班后继续工作 · 按时长计薪"), sectionParams());
        LinearLayout card = card();
        this.weekdayContent = new LinearLayout(this);
        this.weekdayContent.setOrientation(1);
        card.addView(this.weekdayContent, matchWrap());
        this.root.addView(card, cardParams());
        fillWeekday();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fillWeekday() {
        this.weekdayContent.removeAllViews();
        this.weekdayContent.addView(rowLabel("加班倍率"), wrapWrap());
        this.weekdayContent.addView(choiceRow(new String[]{"1.5 倍", "2 倍", "3 倍"}, new int[]{15, 20, 30}, WorkRules.overtimeRateX10(this), new OnChoice() { // from class: com.salaryjump.app.WorkRulesActivity.2
            @Override // com.salaryjump.app.WorkRulesActivity.OnChoice
            public void onChoice(int i) {
                WorkRules.setOvertimeRateX10(WorkRulesActivity.this, i);
                WorkRulesActivity.this.fillWeekday();
            }
        }), choiceParams());
        this.weekdayContent.addView(rowLabel("起算时间"), rowLabelParams());
        this.weekdayContent.addView(choiceRow(new String[]{"下班即算", "15 分钟", "30 分钟", "1 小时"}, new int[]{0, 15, 30, 60}, WorkRules.overtimeGraceMinutes(this), new OnChoice() { // from class: com.salaryjump.app.WorkRulesActivity.3
            @Override // com.salaryjump.app.WorkRulesActivity.OnChoice
            public void onChoice(int i) {
                WorkRules.setOvertimeGraceMinutes(WorkRulesActivity.this, i);
                WorkRulesActivity.this.fillWeekday();
            }
        }), choiceParams());
        this.weekdayContent.addView(body("加班费从「下班时间 + 起算时间」开始计算，与你何时点「开始」无关。"), bodyParams());
    }

    private void buildWeekendSection() {
        this.root.addView(sectionHead("🏖", "周末出勤", "休息日去上班的记账方式"), sectionParams());
        LinearLayout card = card();
        this.weekendContent = new LinearLayout(this);
        this.weekendContent.setOrientation(1);
        card.addView(this.weekendContent, matchWrap());
        this.root.addView(card, cardParams());
        fillWeekend();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fillWeekend() {
        this.weekendContent.removeAllViews();
        this.weekendContent.addView(rowLabel("处理方式"), wrapWrap());
        this.weekendContent.addView(choiceRow(new String[]{"调休", "计薪"}, new int[]{1, 0}, WorkRules.weekendMode(this), new OnChoice() { // from class: com.salaryjump.app.WorkRulesActivity.4
            @Override // com.salaryjump.app.WorkRulesActivity.OnChoice
            public void onChoice(int i) {
                WorkRules.setWeekendMode(WorkRulesActivity.this, i);
                WorkRulesActivity.this.fillWeekend();
            }
        }), choiceParams());
        this.weekendContent.addView(body("调休：不直接计薪，记 1 天调休；休调休假时抵消。\n计薪：按选择的倍率直接计入工资。"), bodyParams());
        if (!WorkRules.isCompMode(this)) {
            this.weekendContent.addView(rowLabel("计薪倍率"), rowLabelParams());
            this.weekendContent.addView(choiceRow(new String[]{"1.5 倍", "2 倍", "3 倍"}, new int[]{15, 20, 30}, WorkRules.weekendRateX10(this), new OnChoice() { // from class: com.salaryjump.app.WorkRulesActivity.5
                @Override // com.salaryjump.app.WorkRulesActivity.OnChoice
                public void onChoice(int i) {
                    WorkRules.setWeekendRateX10(WorkRulesActivity.this, i);
                    WorkRulesActivity.this.fillWeekend();
                }
            }), choiceParams());
        }
    }

    private void buildStatutorySection() {
        this.root.addView(sectionHead("🎏", "法定节假日出勤", "春节 / 国庆等法定假日上班"), sectionParams());
        LinearLayout card = card();
        this.statutoryContent = new LinearLayout(this);
        this.statutoryContent.setOrientation(1);
        card.addView(this.statutoryContent, matchWrap());
        this.root.addView(card, cardParams());
        fillStatutory();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fillStatutory() {
        this.statutoryContent.removeAllViews();
        this.statutoryContent.addView(rowLabel("出勤倍率"), wrapWrap());
        this.statutoryContent.addView(choiceRow(new String[]{"2 倍", "3 倍"}, new int[]{20, 30}, WorkRules.statutoryRateX10(this), new OnChoice() { // from class: com.salaryjump.app.WorkRulesActivity.6
            @Override // com.salaryjump.app.WorkRulesActivity.OnChoice
            public void onChoice(int i) {
                WorkRules.setStatutoryRateX10(WorkRulesActivity.this, i);
                WorkRulesActivity.this.fillStatutory();
            }
        }), choiceParams());
        this.statutoryContent.addView(body("法定假日上班按这里设置的倍率直接计薪（不受调休模式影响）。"), bodyParams());
    }

    private void buildBalanceSection() {
        this.root.addView(sectionHead("📅", "调休账户", "自动结算 + 期初余额可手动设置"), sectionParams());
        LinearLayout card = card();
        TextView text = text(WorkRules.formatDays(WorkRules.compBalance(this)) + " 天", 34.0f, MainActivity.COLOR_ACCENT, 1);
        text.setIncludeFontPadding(false);
        card.addView(text, wrapWrap());
        card.addView(body("余额 = 期初 + 已获调休 - 已休调休"), bodyParams());
        card.addView(body("期初 " + WorkRules.formatDays(LeaveAccounts.compInitial(this)) + " 天（手动设置）\n已获 " + WorkRules.formatDays(WorkRules.compEarned(this)) + " 天（周末出勤记调休）\n已休 " + WorkRules.formatDays(WorkRules.compUsed(this)) + " 天（请假类型为「调休」）"), bodyParams());
        TextView text2 = text("维护假期余额（年假 / 育儿假 / 长期贡献奖假等） ›", 12.5f, MainActivity.COLOR_ACCENT, 0);
        text2.setPadding(0, dp(12.0f), 0, 0);
        text2.setClickable(true);
        text2.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.WorkRulesActivity.7
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                WorkRulesActivity.this.startActivity(new Intent(WorkRulesActivity.this, (Class<?>) LeaveAccountsActivity.class));
            }
        });
        card.addView(text2, wrapWrap());
        this.root.addView(card, cardParams());
    }

    private void buildFooter() {
        TextView text = text("说明：修改规则只影响之后的记录，历史记录保留当时的计算方式。", 11.5f, MainActivity.COLOR_LABEL, 0);
        text.setLineSpacing(dp(2.0f), 1.1f);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(18.0f);
        this.root.addView(text, matchWrap);
    }

    private LinearLayout choiceRow(String[] strArr, int[] iArr, int i, final OnChoice onChoice) {
        Drawable softPill;
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        for (int i2 = 0; i2 < strArr.length; i2++) {
            final int i3 = iArr[i2];
            int i4 = i3 == i ? 1 : 0;
            TextView text = text(strArr[i2], 12.5f, i4 != 0 ? MainActivity.COLOR_ON_ACCENT : MainActivity.COLOR_TEXT, i4);
            text.setGravity(17);
            text.setPadding(dp(2.0f), dp(9.0f), dp(2.0f), dp(9.0f));
            if (i4 != 0) {
                softPill = UiKit.solid(MainActivity.COLOR_ACCENT, dp(19.0f));
            } else {
                softPill = MainActivity.softPill(dp(19.0f), dp(1.5f));
            }
            text.setBackground(softPill);
            text.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.WorkRulesActivity.8
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    onChoice.onChoice(i3);
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(38.0f), 1.0f);
            if (i2 > 0) {
                layoutParams.leftMargin = dp(7.0f);
            }
            linearLayout.addView(text, layoutParams);
        }
        return linearLayout;
    }

    private LinearLayout sectionHead(String str, String str2, String str3) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(18.0f), dp(16.0f), dp(18.0f), dp(16.0f));
        linearLayout.setBackground(UiKit.hero(MainActivity.HERO_COLORS, dp(20.0f)));
        linearLayout.setElevation(dp(5.0f));
        linearLayout.addView(text(str + " " + str2, 17.0f, -1, 1), wrapWrap());
        TextView text = text(str3, 11.5f, -939524097, 0);
        LinearLayout.LayoutParams wrapWrap = wrapWrap();
        wrapWrap.topMargin = dp(5.0f);
        linearLayout.addView(text, wrapWrap);
        return linearLayout;
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

    private TextView rowLabel(String str) {
        TextView text = text(str, 11.5f, MainActivity.COLOR_LABEL, 1);
        text.setLetterSpacing(0.12f);
        return text;
    }

    private TextView body(String str) {
        TextView text = text(str, 12.5f, MainActivity.COLOR_MUTED, 0);
        text.setLineSpacing(dp(3.0f), 1.08f);
        return text;
    }

    private LinearLayout.LayoutParams sectionParams() {
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(20.0f);
        return matchWrap;
    }

    private LinearLayout.LayoutParams cardParams() {
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(12.0f);
        return matchWrap;
    }

    private LinearLayout.LayoutParams choiceParams() {
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(10.0f);
        return matchWrap;
    }

    private LinearLayout.LayoutParams rowLabelParams() {
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(16.0f);
        return matchWrap;
    }

    private LinearLayout.LayoutParams bodyParams() {
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(9.0f);
        return matchWrap;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private LinearLayout.LayoutParams wrapWrap() {
        return new LinearLayout.LayoutParams(-2, -2);
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

    private int dp(float f) {
        return Math.round(f * this.density);
    }
}
