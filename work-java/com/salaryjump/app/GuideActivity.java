package com.salaryjump.app;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/* loaded from: classes.dex */
public class GuideActivity extends Activity {
    private float density;
    private LinearLayout root;

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
        buildQuickRef();
        buildOvertimeSection();
        buildLeaveSection();
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
        button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.GuideActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                GuideActivity.this.finish();
            }
        });
        linearLayout.addView(button, new LinearLayout.LayoutParams(-2, dp(42.0f)));
        TextView textView = new TextView(this);
        textView.setText("加班与请假");
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
        TextView text = text("先看「快速对照」确认你的情况用哪个功能，再看后面的详细规则。", 12.0f, MainActivity.COLOR_MUTED, 0);
        text.setLineSpacing(dp(2.0f), 1.05f);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(12.0f);
        this.root.addView(text, matchWrap);
    }

    private void buildQuickRef() {
        String formatDays;
        String sb;
        this.root.addView(sectionHead("🎯", "快速对照", "今天的情况 → 用哪个功能"), sectionParams());
        LinearLayout card = card();
        card.addView(cardTitle("今天该记什么？"), wrapWrap());
        if (WorkRules.isCompMode(this)) {
            sb = "记调休 +1 天";
        } else {
            StringBuilder append = new StringBuilder().append("日薪 × ");
            if (WorkRules.weekendRateX10(this) / 10.0f == Math.floor(WorkRules.weekendRateX10(this) / 10.0f)) {
                formatDays = String.valueOf(WorkRules.weekendRateX10(this) / 10);
            } else {
                formatDays = WorkRules.formatDays(WorkRules.weekendRateX10(this) / 10.0d);
            }
            sb = append.append(formatDays).toString();
        }
        String str = "日薪 × " + (WorkRules.statutoryRateX10(this) / 10);
        card.addView(refRow("正常上班", "不用操作", "日薪全额"), refParams());
        card.addView(refRow("工作日下班后继续干", "开始 → 结束", "+ " + WorkRules.formatDays(WorkRules.overtimeRateX10(this) / 10.0d) + " 倍"), refParams());
        card.addView(refRow("周末来上班", "出勤打卡", sb), refParams());
        card.addView(refRow("法定假日来上班", "出勤打卡", str), refParams());
        card.addView(refRow("请假", "标记", "日薪 × 比例"), refParams());
        card.addView(highlight("以上按你「加班与出勤规则」里的设置自动执行，不需要手动选择。"), formulaParams());
        card.addView(body("出勤打卡怎么操作：主卡出现「今天出勤了？[打卡]」，点一下即完成，当天自动结算；记错点「取消」；补记往期在工资日历长按那一天。"), bodyParams());
        card.addView(body("提示：规则可在「设置 → 加班与出勤规则」里修改（立即生效）；法定假日识别依赖节假日数据，如不准可在设置里点「更新节假日」。"), bodyParams());
        this.root.addView(card, cardParams());
    }

    private void buildOvertimeSection() {
        this.root.addView(sectionHead("⏱", "加班费", "下班后继续工作 · 按时长自动计算"), sectionParams());
        LinearLayout card = card();
        card.addView(cardTitle("它是怎么工作的"), wrapWrap());
        card.addView(body("先在下班时间过后，到主卡「今天加班了？」这一行点「开始」；\n结束工作后点「结束」，这段时长会按加班倍率折算成钱。\n加班收入实时显示，结束时自动记入工资日历。"), bodyParams());
        this.root.addView(card, cardParams());
        LinearLayout card2 = card();
        card2.addView(cardTitle("计算方式"), wrapWrap());
        card2.addView(formula("加班费 = 加班时长 × 每分钟工资 × 加班倍率"), formulaParams());
        card2.addView(body("其中：每分钟工资 = 日薪 ÷ 每日工作分钟数（自动扣除午休）。\n所以加班费和你设置里的「每天工资」是同一套比例，不需要单独配置。"), bodyParams());
        card2.addView(exampleTitle("示例（日薪 ¥300 · 每天工作 8 小时）"), exampleTitleParams());
        card2.addView(exampleRow("加班 2 小时", "1.5 倍", "¥112.50"), exampleRowParams());
        card2.addView(exampleRow("加班 1 小时", "2 倍", "¥75.00"), exampleRowParams());
        this.root.addView(card2, cardParams());
        LinearLayout card3 = card();
        card3.addView(cardTitle("计时起点（重要）"), wrapWrap());
        card3.addView(highlight("起点固定为「下班时间 + 宽限」，与你什么时候点击无关。"), formulaParams());
        card3.addView(body("例：17:00 下班、宽限 30 分钟 ——\n即使你 20:00 才想起来点「开始」，也仍然从 17:30 起算，不会被扣掉。"), bodyParams());
        card3.addView(body("宽限可在设置里选择：下班即算 / 15 分钟 / 30 分钟 / 1 小时。"), bodyParams());
        this.root.addView(card3, cardParams());
        LinearLayout card4 = card();
        card4.addView(cardTitle("加班倍率"), wrapWrap());
        card4.addView(body("1.5 倍 / 2 倍 / 3 倍三档可选，按你公司的规定在设置里切换即可。\n倍率只作用于「工作日下班后继续干」的这段时间。"), bodyParams());
        card4.addView(highlight("休息日或法定假日来上班？不用加班功能 —— 请见下方「出勤打卡」。"), formulaParams());
        this.root.addView(card4, cardParams());
        LinearLayout card5 = card();
        card5.addView(cardTitle("操作流程"), wrapWrap());
        card5.addView(stepRow("1", "下班时间过后，主卡出现「今天加班了？」，点「开始」"), stepParams());
        card5.addView(stepRow("2", "计时中显示「⏱ 加班中 0:32:15」，可随时查看"), stepParams());
        card5.addView(stepRow("3", "点「结束」结算，显示「今日加班 2:30:00 · ¥112.50 元」"), stepParams());
        card5.addView(stepRow("4", "想重记：点「重来」清除今天的加班记录，重新开始"), stepParams());
        card5.addView(body("提示：标记请假后，加班行会自动隐藏（请假当天不可能在加班）。"), bodyParams());
        this.root.addView(card5, cardParams());
    }

    private void buildLeaveSection() {
        this.root.addView(sectionHead("🏖", "请假", "标记当天 · 按类型比例调整收入"), sectionParams());
        LinearLayout card = card();
        card.addView(cardTitle("为什么需要标记"), wrapWrap());
        card.addView(body("默认每个工作日都按「上满班」算钱。\n如果当天请假却不标记，这一天的收入就会被算多 —— 标记后金额自动按比例调整。"), bodyParams());
        this.root.addView(card, cardParams());
        LinearLayout card2 = card();
        card2.addView(cardTitle("五种类型与收入比例"), wrapWrap());
        card2.addView(typeRow("年假 / 育儿假 / 长期贡献奖假 / 调休 / 婚假", "100%（全薪）", true), typeParams());
        card2.addView(typeRow("病假", "60%", true), typeParams());
        card2.addView(typeRow("事假", "0%（无薪）", false), typeParams());
        card2.addView(body("比例在标记时自动带入，不需要手填。"), bodyParams());
        this.root.addView(card2, cardParams());
        LinearLayout card3 = card();
        card3.addView(cardTitle("整天 / 半天"), wrapWrap());
        card3.addView(formula("半天收入 = 整天收入 × 50%"), formulaParams());
        card3.addView(body("例：病假整天是 60% 日薪，病假半天就是 60% × 50% = 30% 日薪。"), bodyParams());
        this.root.addView(card3, cardParams());
        LinearLayout card4 = card();
        card4.addView(cardTitle("两个标记入口"), wrapWrap());
        card4.addView(stepRow("1", "主卡「今天请假了？[标记]」 —— 两步：选类型 → 选整天 / 半天"), stepParams());
        card4.addView(stepRow("2", "忘记标记也没关系：下班后文案自动变成「今天请假了吗？[补记]」，随时补"), stepParams());
        card4.addView(stepRow("3", "更早的日期：工资日历里长按某一天 →「标记请假」即可补录"), stepParams());
        card4.addView(body("已标记的日期在日历里用紫色显示；长按还能「修改请假记录」或「取消请假」。"), bodyParams());
        this.root.addView(card4, cardParams());
        LinearLayout card5 = card();
        card5.addView(cardTitle("计算示例（日薪 ¥300）"), wrapWrap());
        card5.addView(exampleRow("年假 · 整天", "100%", "¥300.00"), exampleRowParams());
        card5.addView(exampleRow("病假 · 整天", "60%", "¥180.00"), exampleRowParams());
        card5.addView(exampleRow("病假 · 半天", "30%", "¥90.00"), exampleRowParams());
        card5.addView(exampleRow("事假 · 整天", "0%", "¥0.00"), exampleRowParams());
        this.root.addView(card5, cardParams());
    }

    private void buildFooter() {
        TextView text = text("加班费与请假金额都会记入工资日历；\n修改「每天工资」后，已有的历史记录不会自动重算。", 11.5f, MainActivity.COLOR_LABEL, 0);
        text.setLineSpacing(dp(2.0f), 1.1f);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(18.0f);
        this.root.addView(text, matchWrap);
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

    private TextView cardTitle(String str) {
        return text(str, 14.0f, MainActivity.COLOR_TEXT, 1);
    }

    private TextView body(String str) {
        TextView text = text(str, 12.5f, MainActivity.COLOR_MUTED, 0);
        text.setLineSpacing(dp(3.0f), 1.08f);
        return text;
    }

    private TextView formula(String str) {
        TextView text = text(str, 12.5f, MainActivity.COLOR_ACCENT, 1);
        text.setPadding(dp(12.0f), dp(10.0f), dp(12.0f), dp(10.0f));
        text.setBackground(UiKit.solid(MainActivity.COLOR_CHIP_GREEN_BG, dp(10.0f)));
        return text;
    }

    private TextView highlight(String str) {
        return formula(str);
    }

    private TextView exampleTitle(String str) {
        return text(str, 11.5f, MainActivity.COLOR_LABEL, 1);
    }

    private LinearLayout exampleRow(String str, String str2, String str3) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        linearLayout.addView(text(str, 12.5f, MainActivity.COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView text = text(str2, 12.5f, MainActivity.COLOR_MUTED, 0);
        text.setGravity(21);
        linearLayout.addView(text, new LinearLayout.LayoutParams(-2, -2));
        if (str3 != null && str3.length() > 0) {
            TextView text2 = text(str3, 13.0f, MainActivity.COLOR_ACCENT, 1);
            text2.setGravity(21);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(90.0f), -2);
            layoutParams.leftMargin = dp(10.0f);
            linearLayout.addView(text2, layoutParams);
        }
        return linearLayout;
    }

    private LinearLayout stepRow(String str, String str2) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        TextView text = text(str, 11.5f, MainActivity.COLOR_ACCENT, 1);
        text.setGravity(17);
        text.setBackground(UiKit.solid(MainActivity.COLOR_CHIP_GREEN_BG, dp(10.0f)));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(20.0f), dp(20.0f));
        layoutParams.topMargin = dp(1.0f);
        linearLayout.addView(text, layoutParams);
        TextView text2 = text(str2, 12.5f, MainActivity.COLOR_TEXT, 0);
        text2.setLineSpacing(dp(2.0f), 1.05f);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, -2, 1.0f);
        layoutParams2.leftMargin = dp(10.0f);
        linearLayout.addView(text2, layoutParams2);
        return linearLayout;
    }

    private LinearLayout refRow(String str, String str2, String str3) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        linearLayout.addView(text(str, 12.5f, MainActivity.COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView text = text(str2, 11.5f, MainActivity.COLOR_MUTED, 0);
        text.setGravity(21);
        linearLayout.addView(text, new LinearLayout.LayoutParams(-2, -2));
        TextView text2 = text(str3, 12.5f, MainActivity.COLOR_ACCENT, 1);
        text2.setGravity(21);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(78.0f), -2);
        layoutParams.leftMargin = dp(10.0f);
        linearLayout.addView(text2, layoutParams);
        return linearLayout;
    }

    private LinearLayout typeRow(String str, String str2, boolean z) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        linearLayout.addView(text(str, 12.5f, MainActivity.COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView text = text(str2, 12.5f, z ? MainActivity.COLOR_ACCENT : MainActivity.COLOR_MUTED, 1);
        text.setGravity(21);
        linearLayout.addView(text, new LinearLayout.LayoutParams(-2, -2));
        return linearLayout;
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

    private LinearLayout.LayoutParams bodyParams() {
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(9.0f);
        return matchWrap;
    }

    private LinearLayout.LayoutParams formulaParams() {
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(10.0f);
        return matchWrap;
    }

    private LinearLayout.LayoutParams exampleTitleParams() {
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(14.0f);
        matchWrap.bottomMargin = dp(2.0f);
        return matchWrap;
    }

    private LinearLayout.LayoutParams exampleRowParams() {
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(8.0f);
        return matchWrap;
    }

    private LinearLayout.LayoutParams stepParams() {
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(9.0f);
        return matchWrap;
    }

    private LinearLayout.LayoutParams refParams() {
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(9.0f);
        return matchWrap;
    }

    private LinearLayout.LayoutParams typeParams() {
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
