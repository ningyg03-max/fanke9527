package com.salaryjump.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/* loaded from: classes.dex */
public class LeaveAccountsActivity extends Activity {
    private static final String[] ICONS = {"🏖", "👶", "🏆", "💊", "📋"};
    private LinearLayout cardsBox;
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
        this.cardsBox = new LinearLayout(this);
        this.cardsBox.setOrientation(1);
        this.root.addView(this.cardsBox, matchWrap());
        fillCards();
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
        button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.LeaveAccountsActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                LeaveAccountsActivity.this.finish();
            }
        });
        linearLayout.addView(button, new LinearLayout.LayoutParams(-2, dp(42.0f)));
        TextView textView = new TextView(this);
        textView.setText("假期账户");
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
        TextView text = text("维护你的假期余额。标记请假时按类型自动扣减，随时可以手动校正。", 12.0f, MainActivity.COLOR_MUTED, 0);
        text.setLineSpacing(dp(2.0f), 1.05f);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(12.0f);
        this.root.addView(text, matchWrap);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fillCards() {
        this.cardsBox.removeAllViews();
        for (int i = 0; i < LeaveAccounts.YEAR_NAMES.length; i++) {
            addYearCard(i);
        }
        addCompCard();
    }

    private void addYearCard(final int i) {
        boolean isSet = LeaveAccounts.isSet(this, i);
        double quota = LeaveAccounts.quota(this, i);
        double adjust = LeaveAccounts.adjust(this, i);
        double usedThisYear = LeaveAccounts.usedThisYear(this, i);
        double d = quota + adjust;
        double d2 = d - usedThisYear;
        LinearLayout card = card();
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        linearLayout.addView(text(ICONS[i], 19.0f, MainActivity.COLOR_TEXT, 0), wrapWrap());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
        layoutParams.leftMargin = dp(9.0f);
        linearLayout.addView(text(LeaveAccounts.YEAR_NAMES[i], 15.0f, MainActivity.COLOR_TEXT, 1), layoutParams);
        TextView text = text(isSet ? "编辑" : "设置", 12.5f, MainActivity.COLOR_ACCENT, 0);
        text.setPadding(dp(13.0f), dp(6.0f), dp(13.0f), dp(6.0f));
        text.setBackground(MainActivity.softPill(dp(16.0f), dp(1.0f)));
        text.setClickable(true);
        text.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.LeaveAccountsActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                LeaveAccountsActivity.this.showYearEditDialog(i);
            }
        });
        linearLayout.addView(text, wrapWrap());
        card.addView(linearLayout, matchWrap());
        if (isSet) {
            LinearLayout linearLayout2 = new LinearLayout(this);
            linearLayout2.setOrientation(0);
            linearLayout2.setGravity(16);
            TextView text2 = text(WorkRules.formatDays(d2), 31.0f, d2 > 0.0d ? MainActivity.COLOR_ACCENT : MainActivity.COLOR_AMBER, 1);
            text2.setIncludeFontPadding(false);
            linearLayout2.addView(text2, wrapWrap());
            linearLayout2.addView(text(" 天", 13.0f, MainActivity.COLOR_MUTED, 1), wrapWrap());
            LinearLayout.LayoutParams wrapWrap = wrapWrap();
            wrapWrap.leftMargin = dp(5.0f);
            linearLayout2.addView(text(d2 > 0.0d ? "剩余" : "已用完", 11.5f, MainActivity.COLOR_MUTED, 0), wrapWrap);
            LinearLayout.LayoutParams matchWrap = matchWrap();
            matchWrap.topMargin = dp(11.0f);
            card.addView(linearLayout2, matchWrap);
            StringBuilder append = new StringBuilder("今年总额 ").append(WorkRules.formatDays(quota));
            if (adjust != 0.0d) {
                append.append(" · 调整 ").append(adjust > 0.0d ? "+" : "").append(WorkRules.formatDays(adjust));
            }
            append.append(" · 已用 ").append(WorkRules.formatDays(usedThisYear));
            LinearLayout.LayoutParams matchWrap2 = matchWrap();
            matchWrap2.topMargin = dp(8.0f);
            card.addView(text(append.toString(), 11.5f, MainActivity.COLOR_MUTED, 0), matchWrap2);
            if (d > 0.0d) {
                SheenBar sheenBar = new SheenBar(this);
                sheenBar.setProgress((float) Math.max(0.0d, Math.min(1.0d, d2 / d)));
                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, dp(7.0f));
                layoutParams2.topMargin = dp(11.0f);
                card.addView(sheenBar, layoutParams2);
            }
        } else {
            View text3 = text("未设置", 18.0f, MainActivity.COLOR_LABEL, 0);
            LinearLayout.LayoutParams matchWrap3 = matchWrap();
            matchWrap3.topMargin = dp(11.0f);
            card.addView(text3, matchWrap3);
            LinearLayout.LayoutParams matchWrap4 = matchWrap();
            matchWrap4.topMargin = dp(6.0f);
            card.addView(text("点「设置」录入年度额度，之后请假自动扣减", 11.5f, MainActivity.COLOR_LABEL, 0), matchWrap4);
        }
        LinearLayout.LayoutParams matchWrap5 = matchWrap();
        matchWrap5.topMargin = dp(12.0f);
        this.cardsBox.addView(card, matchWrap5);
    }

    private void addCompCard() {
        double compInitial = LeaveAccounts.compInitial(this);
        double compEarned = WorkRules.compEarned(this);
        double compUsed = WorkRules.compUsed(this);
        double d = (compInitial + compEarned) - compUsed;
        LinearLayout card = card();
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        linearLayout.addView(text("📅", 19.0f, MainActivity.COLOR_TEXT, 0), wrapWrap());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
        layoutParams.leftMargin = dp(9.0f);
        linearLayout.addView(text("调休", 15.0f, MainActivity.COLOR_TEXT, 1), layoutParams);
        TextView text = text("编辑", 12.5f, MainActivity.COLOR_ACCENT, 0);
        text.setPadding(dp(13.0f), dp(6.0f), dp(13.0f), dp(6.0f));
        text.setBackground(MainActivity.softPill(dp(16.0f), dp(1.0f)));
        text.setClickable(true);
        text.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.LeaveAccountsActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                LeaveAccountsActivity.this.showCompEditDialog();
            }
        });
        linearLayout.addView(text, wrapWrap());
        card.addView(linearLayout, matchWrap());
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        TextView text2 = text(WorkRules.formatDays(d), 31.0f, d > 0.0d ? MainActivity.COLOR_ACCENT : MainActivity.COLOR_AMBER, 1);
        text2.setIncludeFontPadding(false);
        linearLayout2.addView(text2, wrapWrap());
        linearLayout2.addView(text(" 天", 13.0f, MainActivity.COLOR_MUTED, 1), wrapWrap());
        LinearLayout.LayoutParams wrapWrap = wrapWrap();
        wrapWrap.leftMargin = dp(5.0f);
        linearLayout2.addView(text(d > 0.0d ? "剩余" : "已用完", 11.5f, MainActivity.COLOR_MUTED, 0), wrapWrap);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(11.0f);
        card.addView(linearLayout2, matchWrap);
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(8.0f);
        card.addView(text("期初 " + WorkRules.formatDays(compInitial) + " ＋ 已获 " + WorkRules.formatDays(compEarned) + "（周末出勤）－ 已休 " + WorkRules.formatDays(compUsed) + "（请假「调休」）", 11.5f, MainActivity.COLOR_MUTED, 0), matchWrap2);
        LinearLayout.LayoutParams matchWrap3 = matchWrap();
        matchWrap3.topMargin = dp(12.0f);
        this.cardsBox.addView(card, matchWrap3);
    }

    private void buildFooter() {
        TextView text = text("说明：余额仅用于展示与提醒，不影响请假薪酬计算。\n「已用」按当前年份实时统计，跨年后自动按新年度重新计算；总额如需调整请手动修改。", 11.5f, MainActivity.COLOR_LABEL, 0);
        text.setLineSpacing(dp(3.0f), 1.1f);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(18.0f);
        this.root.addView(text, matchWrap);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showYearEditDialog(final int i) {
        String str = LeaveAccounts.YEAR_NAMES[i];
        final double usedThisYear = LeaveAccounts.usedThisYear(this, i);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(22.0f), dp(8.0f), dp(22.0f), dp(4.0f));
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        linearLayout2.addView(text("今年总额", 14.0f, MainActivity.COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, -2, 1.0f));
        final EditText styledNumberInput = styledNumberInput();
        styledNumberInput.setText(WorkRules.formatDays(LeaveAccounts.quota(this, i)));
        linearLayout2.addView(styledNumberInput, new LinearLayout.LayoutParams(dp(86.0f), dp(42.0f)));
        linearLayout2.addView(text(" 天", 12.5f, MainActivity.COLOR_MUTED, 0), wrapWrap());
        linearLayout.addView(linearLayout2, matchWrap());
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(10.0f);
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(0);
        linearLayout3.setGravity(16);
        linearLayout3.addView(text("手动调整", 14.0f, MainActivity.COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, -2, 1.0f));
        final EditText styledNumberInput2 = styledNumberInput();
        styledNumberInput2.setText(WorkRules.formatDays(LeaveAccounts.adjust(this, i)));
        linearLayout3.addView(styledNumberInput2, new LinearLayout.LayoutParams(dp(86.0f), dp(42.0f)));
        linearLayout3.addView(text(" 天", 12.5f, MainActivity.COLOR_MUTED, 0), wrapWrap());
        linearLayout.addView(linearLayout3, matchWrap);
        TextView text = text("例：开始用本 App 前已休过 3 天 → 填 -3；去年结转 +2 天 → 填 +2。", 11.0f, MainActivity.COLOR_LABEL, 0);
        text.setLineSpacing(dp(2.0f), 1.1f);
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(8.0f);
        linearLayout.addView(text, matchWrap2);
        View view = new View(this);
        view.setBackgroundColor(MainActivity.COLOR_BORDER);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(1.0f));
        layoutParams.topMargin = dp(12.0f);
        linearLayout.addView(view, layoutParams);
        final TextView text2 = text("", 12.5f, MainActivity.COLOR_MUTED, 0);
        final TextView text3 = text("", 13.5f, MainActivity.COLOR_ACCENT, 1);
        LinearLayout.LayoutParams matchWrap3 = matchWrap();
        matchWrap3.topMargin = dp(10.0f);
        linearLayout.addView(text2, matchWrap3);
        LinearLayout.LayoutParams matchWrap4 = matchWrap();
        matchWrap4.topMargin = dp(7.0f);
        linearLayout.addView(text3, matchWrap4);
        final Runnable runnable = new Runnable() { // from class: com.salaryjump.app.LeaveAccountsActivity.4
            @Override // java.lang.Runnable
            public void run() {
                double parseNum = LeaveAccountsActivity.this.parseNum(styledNumberInput.getText().toString());
                double parseNum2 = LeaveAccountsActivity.this.parseNum(styledNumberInput2.getText().toString());
                text2.setText("已用（自动统计今年）  " + WorkRules.formatDays(usedThisYear) + " 天");
                text3.setText("剩余  " + WorkRules.formatDays((parseNum + parseNum2) - usedThisYear) + " 天");
            }
        };
        TextWatcher textWatcher = new TextWatcher() { // from class: com.salaryjump.app.LeaveAccountsActivity.5
            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence charSequence, int i2, int i3, int i4) {
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence charSequence, int i2, int i3, int i4) {
            }

            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable editable) {
                runnable.run();
            }
        };
        styledNumberInput.addTextChangedListener(textWatcher);
        styledNumberInput2.addTextChangedListener(textWatcher);
        runnable.run();
        new AlertDialog.Builder(this).setTitle("编辑「" + str + "」").setView(linearLayout).setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("保存", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.LeaveAccountsActivity.6
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                LeaveAccounts.setBudget(LeaveAccountsActivity.this, i, LeaveAccountsActivity.this.clampDays(LeaveAccountsActivity.this.parseNum(styledNumberInput.getText().toString())), LeaveAccountsActivity.this.clampDays(LeaveAccountsActivity.this.parseNum(styledNumberInput2.getText().toString())));
                LeaveAccountsActivity.this.fillCards();
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showCompEditDialog() {
        final double compEarned = WorkRules.compEarned(this);
        final double compUsed = WorkRules.compUsed(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(22.0f), dp(8.0f), dp(22.0f), dp(4.0f));
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        linearLayout2.addView(text("期初余额", 14.0f, MainActivity.COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, -2, 1.0f));
        final EditText styledNumberInput = styledNumberInput();
        styledNumberInput.setText(WorkRules.formatDays(LeaveAccounts.compInitial(this)));
        linearLayout2.addView(styledNumberInput, new LinearLayout.LayoutParams(dp(86.0f), dp(42.0f)));
        linearLayout2.addView(text(" 天", 12.5f, MainActivity.COLOR_MUTED, 0), wrapWrap());
        linearLayout.addView(linearLayout2, matchWrap());
        TextView text = text("开始使用本 App 之前，手上已经攒的调休天数。", 11.0f, MainActivity.COLOR_LABEL, 0);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(8.0f);
        linearLayout.addView(text, matchWrap);
        View view = new View(this);
        view.setBackgroundColor(MainActivity.COLOR_BORDER);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(1.0f));
        layoutParams.topMargin = dp(12.0f);
        linearLayout.addView(view, layoutParams);
        final TextView text2 = text("", 12.5f, MainActivity.COLOR_MUTED, 0);
        final TextView text3 = text("", 13.5f, MainActivity.COLOR_ACCENT, 1);
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(10.0f);
        linearLayout.addView(text2, matchWrap2);
        LinearLayout.LayoutParams matchWrap3 = matchWrap();
        matchWrap3.topMargin = dp(7.0f);
        linearLayout.addView(text3, matchWrap3);
        final Runnable runnable = new Runnable() { // from class: com.salaryjump.app.LeaveAccountsActivity.7
            @Override // java.lang.Runnable
            public void run() {
                double parseNum = LeaveAccountsActivity.this.parseNum(styledNumberInput.getText().toString());
                text2.setText("已获（自动）  " + WorkRules.formatDays(compEarned) + " 天    ·    已休（自动）  " + WorkRules.formatDays(compUsed) + " 天");
                text3.setText("剩余  " + WorkRules.formatDays((parseNum + compEarned) - compUsed) + " 天");
            }
        };
        styledNumberInput.addTextChangedListener(new TextWatcher() { // from class: com.salaryjump.app.LeaveAccountsActivity.8
            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable editable) {
                runnable.run();
            }
        });
        runnable.run();
        new AlertDialog.Builder(this).setTitle("编辑「调休」").setView(linearLayout).setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("保存", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.LeaveAccountsActivity.9
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                LeaveAccounts.setCompInitial(LeaveAccountsActivity.this, LeaveAccountsActivity.this.clampDays(LeaveAccountsActivity.this.parseNum(styledNumberInput.getText().toString())));
                LeaveAccountsActivity.this.fillCards();
            }
        }).show();
    }

    private EditText styledNumberInput() {
        EditText editText = new EditText(this);
        editText.setTextSize(14.5f);
        editText.setTextColor(MainActivity.COLOR_TEXT);
        editText.setGravity(17);
        editText.setSingleLine(true);
        editText.setInputType(12290);
        editText.setPadding(dp(8.0f), 0, dp(8.0f), 0);
        editText.setBackground(rounded(MainActivity.COLOR_BACKGROUND, 8.0f, MainActivity.COLOR_BORDER, 1.0f));
        return editText;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public double parseNum(String str) {
        if (str == null) {
            return 0.0d;
        }
        try {
            return Double.parseDouble(str.trim());
        } catch (Throwable th) {
            return 0.0d;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public double clampDays(double d) {
        if (d < -366.0d) {
            return -366.0d;
        }
        if (d > 366.0d) {
            return 366.0d;
        }
        return d;
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

    private GradientDrawable rounded(int i, float f, int i2, float f2) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(dp(f));
        if (f2 > 0.0f) {
            gradientDrawable.setStroke(dp(f2), i2);
        }
        return gradientDrawable;
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
