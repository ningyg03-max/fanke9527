package com.salaryjump.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.salaryjump.app.WaterReminderScheduler;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

/* loaded from: classes.dex */
public class WaterStatsActivity extends Activity {
    private float density;
    private LinearLayout listContainer;
    private Button[] rangeButtons;
    private TextView rangeNoteView;
    private TextView summaryView;
    private final DecimalFormat amountFormat = new DecimalFormat("#,##0");
    /** 未达标的柱子用浅绿，避免整张图全是同一种绿 */
    private static final int COLOR_BAR_LOW = android.graphics.Color.rgb(143, 208, 192);
    private int rangeDays = 7;

    private int dp(float f) {
        return Math.round(f * this.density);
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        MainActivity.applyTheme(this);
        this.density = getResources().getDisplayMetrics().density;
        configureWindow();
        buildInterface();
    }

    private void configureWindow() {
        MainActivity.applyEdgeToEdge(getWindow());
    }

    private void buildInterface() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setBackground(null);
        linearLayout.setPadding(dp(20.0f), dp(20.0f), dp(20.0f), dp(24.0f));
        scrollView.addView(linearLayout, new FrameLayout.LayoutParams(-1, -2));
        setContentView(UiKit.aurora(this, scrollView));
        MotionKit.enterChildren(linearLayout, this.density);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        linearLayout.addView(linearLayout2, matchWrap());
        Button button = new Button(this);
        button.setText("返回");
        button.setTextSize(14.0f);
        button.setTextColor(MainActivity.COLOR_TEXT);
        button.setAllCaps(false);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setPadding(dp(16.0f), 0, dp(16.0f), 0);
        button.setBackground(MainActivity.softPill(dp(21.0f), dp(1.5f)));
        button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.WaterStatsActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                WaterStatsActivity.this.finish();
            }
        });
        linearLayout2.addView(button, new LinearLayout.LayoutParams(-2, dp(42.0f)));
        TextView textView = new TextView(this);
        textView.setText("喝水统计");
        textView.setTextSize(20.0f);
        textView.setTextColor(MainActivity.COLOR_TEXT);
        textView.setTypeface(Typeface.create("sans", 1));
        textView.setGravity(17);
        linearLayout2.addView(textView, new LinearLayout.LayoutParams(0, dp(42.0f), 1.0f));
        Button button2 = new Button(this);
        button2.setText("返回");
        button2.setTextSize(14.0f);
        button2.setAllCaps(false);
        button2.setMinWidth(0);
        button2.setMinHeight(0);
        button2.setPadding(dp(16.0f), 0, dp(16.0f), 0);
        button2.setVisibility(4);
        linearLayout2.addView(button2, new LinearLayout.LayoutParams(-2, dp(42.0f)));
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(0);
        linearLayout3.setGravity(16);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(18.0f);
        linearLayout.addView(linearLayout3, matchWrap);
        this.rangeButtons = new Button[3];
        String[] strArr = {"今日", "近7天", "近30天"};
        int[] iArr = {1, 7, 30};
        for (int i = 0; i < 3; i++) {
            Button button3 = new Button(this);
            button3.setText(strArr[i]);
            button3.setTextSize(13.0f);
            button3.setAllCaps(false);
            button3.setMinWidth(0);
            button3.setMinHeight(0);
            final int i2 = iArr[i];
            button3.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.WaterStatsActivity.2
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    WaterStatsActivity.this.rangeDays = i2;
                    WaterStatsActivity.this.render();
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(40.0f), 1.0f);
            if (i > 0) {
                layoutParams.leftMargin = dp(8.0f);
            }
            linearLayout3.addView(button3, layoutParams);
            this.rangeButtons[i] = button3;
        }
        LinearLayout linearLayout4 = new LinearLayout(this);
        linearLayout4.setOrientation(1);
        linearLayout4.setPadding(dp(22.0f), dp(20.0f), dp(22.0f), dp(20.0f));
        linearLayout4.setBackground(MainActivity.softCard(dp(20.0f)));
        linearLayout4.setElevation(dp(3.0f));
        linearLayout4.setElevation(dp(2.0f));
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(16.0f);
        linearLayout.addView(linearLayout4, matchWrap2);
        this.summaryView = new TextView(this);
        this.summaryView.setTextSize(26.0f);
        this.summaryView.setTextColor(MainActivity.COLOR_BLUE);
        this.summaryView.setTypeface(Typeface.create("sans", 1));
        linearLayout4.addView(this.summaryView, matchWrap());
        this.rangeNoteView = new TextView(this);
        this.rangeNoteView.setTextSize(13.0f);
        this.rangeNoteView.setTextColor(MainActivity.COLOR_MUTED);
        LinearLayout.LayoutParams matchWrap3 = matchWrap();
        matchWrap3.topMargin = dp(6.0f);
        linearLayout4.addView(this.rangeNoteView, matchWrap3);
        this.listContainer = new LinearLayout(this);
        this.listContainer.setOrientation(1);
        LinearLayout.LayoutParams matchWrap4 = matchWrap();
        matchWrap4.topMargin = dp(16.0f);
        linearLayout.addView(this.listContainer, matchWrap4);
        render();
    }

    public void render() {
        int i = 0;
        while (true) {
            boolean z = true;
            if (i >= 3) {
                break;
            }
            if ((i != 0 || this.rangeDays != 1) && ((i != 1 || this.rangeDays != 7) && (i != 2 || this.rangeDays != 30))) {
                z = false;
            }
            this.rangeButtons[i].setTextColor(z ? MainActivity.COLOR_ON_BLUE : MainActivity.COLOR_TEXT);
            this.rangeButtons[i].setBackground(z ? rounded(MainActivity.COLOR_BLUE_BRIGHT, 19, 0, 0) : MainActivity.softPill(dp(19.0f), dp(1.5f)));
            i++;
        }
        SharedPreferences sharedPreferences = getSharedPreferences("salary_jump", 0);
        final int[] iArr = new int[32];
        int[] iArr2 = new int[32];
        int i2 = 0;
        for (WaterReminderScheduler.WaterRecord waterRecord : WaterReminderScheduler.getRecords(this)) {
            int clamp = DrinkTypes.clamp(waterRecord.type);
            iArr[clamp] = iArr[clamp] + waterRecord.amountMl;
            iArr2[clamp] = iArr2[clamp] + 1;
            i2 += waterRecord.amountMl;
        }
        Calendar calendar = Calendar.getInstance();
        int i3 = (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
        HashMap hashMap = new HashMap();
        hashMap.put(Integer.valueOf(i3), Integer.valueOf(i2));
        if (this.rangeDays > 1) {
            Calendar calendar2 = Calendar.getInstance();
            calendar2.add(5, -(this.rangeDays - 1));
            int i4 = (calendar2.get(1) * 10000) + ((calendar2.get(2) + 1) * 100) + calendar2.get(5);
            Calendar calendar3 = Calendar.getInstance();
            calendar3.add(5, -1);
            hashMap.putAll(WaterHistory.query(sharedPreferences, i4, (calendar3.get(1) * 10000) + ((calendar3.get(2) + 1) * 100) + calendar3.get(5), iArr, iArr2));
        }
        int i5 = WaterReminderSettings.load(this).targetMl;
        Iterator it = hashMap.values().iterator();
        int i6 = 0;
        while (it.hasNext()) {
            if (((Integer) it.next()).intValue() >= i5) {
                i6++;
            }
        }
        int i7 = 0;
        for (int i8 = 0; i8 < 32; i8++) {
            i7 += iArr[i8];
        }
        this.summaryView.setText(this.amountFormat.format(i7) + " ml");
        int i9 = 0;
        int i10 = 0;
        int i11 = 0;
        for (int i12 = 0; i12 < 32; i12++) {
            i9 += Math.round((iArr[i12] / 100.0f) * DrinkTypes.kcal(this, i12));
            i10 += iArr2[i12];
            if (DrinkTypes.isSugary(this, i12)) {
                i11 += iArr2[i12];
            }
        }
        this.rangeNoteView.setText("目标 " + i5 + " ml · 达标 " + i6 + " 天" + (this.rangeDays > 1 ? " / 共 " + this.rangeDays + " 天" : "") + "\n约 " + i9 + " kcal" + (i10 > 0 ? " · 含糖 " + i11 + " / " + i10 + " 杯" : ""));
        this.listContainer.removeAllViews();
        ArrayList arrayList = new ArrayList();
        for (int i13 = 0; i13 < 32; i13++) {
            if (iArr2[i13] > 0) {
                arrayList.add(Integer.valueOf(i13));
            }
        }
        if (arrayList.isEmpty()) {
            TextView textView = new TextView(this);
            textView.setText("暂无记录，去主界面记一笔吧");
            textView.setTextSize(14.0f);
            textView.setTextColor(MainActivity.COLOR_MUTED);
            textView.setGravity(17);
            textView.setPadding(0, dp(36.0f), 0, dp(36.0f));
            textView.setBackground(MainActivity.softCard(dp(18.0f)));
            this.listContainer.addView(textView, matchWrap());
            return;
        }
        Collections.sort(arrayList, new Comparator<Integer>() { // from class: com.salaryjump.app.WaterStatsActivity.3
            @Override // java.util.Comparator
            public int compare(Integer num, Integer num2) {
                return iArr[num2.intValue()] - iArr[num.intValue()];
            }
        });

        // 「今日」只有一根柱子，没有信息量，所以不画柱状图；
        // 分析卡也一样 —— 一天的数据看不出任何规律
        if (this.rangeDays > 1) {
            int[] dailySeries = WaterDailyChart.series(hashMap, this.rangeDays, Calendar.getInstance());

            Calendar from = Calendar.getInstance();
            from.add(5, -(this.rangeDays - 1));
            int fromStamp = (from.get(1) * 10000) + ((from.get(2) + 1) * 100) + from.get(5);
            Calendar now = Calendar.getInstance();
            int toStamp = (now.get(1) * 10000) + ((now.get(2) + 1) * 100) + now.get(5);

            DailyAnalysis.Result analysis = DailyAnalysis.analyze(
                    dailySeries, i5,
                    WaterHourLog.days(sharedPreferences, fromStamp, toStamp),
                    i10, i11, i7);
            LinearLayout.LayoutParams analysisLp = matchWrap();
            analysisLp.topMargin = dp(10.0f);
            this.listContainer.addView(buildAnalysisCard(analysis), analysisLp);

            LinearLayout.LayoutParams chartLp = matchWrap();
            chartLp.topMargin = dp(10.0f);
            this.listContainer.addView(buildDailyChart(dailySeries, this.rangeDays, i5), chartLp);
        }

        // ── 饮料两列 ──
        LinearLayout.LayoutParams drinksLp = matchWrap();
        drinksLp.topMargin = dp(10.0f);
        this.listContainer.addView(buildDrinksGrid(arrayList, iArr, iArr2), drinksLp);
    }

    /**
     * 分析卡。
     *
     * <p>刻意<b>不叫「AI 分析」</b> —— 这是统计规则算出来的，
     * 标成 AI 会让人误以为它能考虑体重、运动量这些它根本没有的数据。
     */
    private LinearLayout buildAnalysisCard(DailyAnalysis.Result result) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(1);
        card.setPadding(dp(18.0f), dp(15.0f), dp(18.0f), dp(15.0f));
        card.setBackground(MainActivity.softCard(dp(20.0f)));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(0);
        head.setGravity(16);
        card.addView(head, matchWrap());

        TextView title = new TextView(this);
        title.setText("分析");
        title.setTextSize(12.0f);
        title.setTextColor(MainActivity.COLOR_LABEL);
        title.setTypeface(Typeface.create("sans", 1));
        head.addView(title, new LinearLayout.LayoutParams(0, -2, 1.0f));

        // 这行小字很重要：让人知道依据是什么，而不是「AI 说的」
        TextView tag = new TextView(this);
        tag.setText("按记录统计");
        tag.setTextSize(9.5f);
        tag.setTextColor(MainActivity.COLOR_LABEL);
        tag.setPadding(dp(7.0f), dp(2.0f), dp(7.0f), dp(2.0f));
        tag.setBackground(MainActivity.softPill(dp(9.0f), 0));
        head.addView(tag, new LinearLayout.LayoutParams(-2, -2));

        if (result.hint != null) {
            TextView hint = new TextView(this);
            hint.setText(result.hint);
            hint.setTextSize(12.5f);
            hint.setTextColor(MainActivity.COLOR_MUTED);
            hint.setLineSpacing(dp(2.0f), 1.0f);
            LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(-1, -2);
            hintLp.topMargin = dp(8.0f);
            card.addView(hint, hintLp);
            return card;
        }

        TextView headline = new TextView(this);
        headline.setText(result.headline);
        headline.setTextSize(16.0f);
        headline.setTextColor(MainActivity.COLOR_TEXT);
        headline.setTypeface(Typeface.create("sans", 1));
        headline.setLineSpacing(dp(2.0f), 1.05f);
        LinearLayout.LayoutParams headlineLp = new LinearLayout.LayoutParams(-1, -2);
        headlineLp.topMargin = dp(7.0f);
        card.addView(headline, headlineLp);

        for (int i = 0; i < result.findings.size(); i++) {
            DailyAnalysis.Finding finding = result.findings.get(i);
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(0);
            LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(-1, -2);
            lineLp.topMargin = dp(8.0f);
            card.addView(line, lineLp);

            View dot = new View(this);
            dot.setBackground(rounded(MainActivity.COLOR_ACCENT, dp(3.0f), 0, 0));
            LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(5.0f), dp(5.0f));
            dotLp.topMargin = dp(6.0f);
            dotLp.rightMargin = dp(8.0f);
            line.addView(dot, dotLp);

            TextView detail = new TextView(this);
            // 第一条已经是大标题，不再重复标题；后面几条带上标题更清楚
            detail.setText(i == 0 ? finding.detail : (finding.title + " · " + finding.detail));
            detail.setTextSize(11.5f);
            detail.setTextColor(MainActivity.COLOR_MUTED);
            detail.setLineSpacing(dp(2.0f), 1.05f);
            line.addView(detail, new LinearLayout.LayoutParams(0, -2, 1.0f));
        }

        // 时间分布的可信度提示 —— 让用户知道「空档分析」还没攒够数据
        int hourDays = WaterHourLog.dayCount(this.getSharedPreferences("salary_jump", 0));
        if (hourDays < 3) {
            TextView foot = new TextView(this);
            foot.setText("· 时间分布类分析（几点喝、空档多长）要攒够 3 天才有");
            foot.setTextSize(10.0f);
            foot.setTextColor(MainActivity.COLOR_LABEL);
            foot.setLineSpacing(dp(1.5f), 1.0f);
            LinearLayout.LayoutParams footLp = new LinearLayout.LayoutParams(-1, -2);
            footLp.topMargin = dp(9.0f);
            card.addView(foot, footLp);
        }
        return card;
    }

    /**
     * 每日总量柱状图。
     *
     * <p>7 天：格子够宽，柱顶写数字；30 天：格子只有 10dp，数字放不下，
     * 改成右上角显示「平均 / 最高」，日期每 5 天标一个。
     */
    private LinearLayout buildDailyChart(int[] data, int days, int targetMl) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(1);
        card.setPadding(dp(14.0f), dp(16.0f), dp(14.0f), dp(10.0f));
        card.setBackground(MainActivity.softCard(dp(20.0f)));
        card.setElevation(dp(2.0f));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(0);
        head.setGravity(16);
        card.addView(head, matchWrap());

        TextView title = new TextView(this);
        title.setText("每日总量");
        title.setTextSize(13.5f);
        title.setTextColor(MainActivity.COLOR_TEXT);
        title.setTypeface(Typeface.create("sans", 1));
        head.addView(title, new LinearLayout.LayoutParams(0, -2, 1.0f));

        TextView hint = new TextView(this);
        hint.setTextSize(11.0f);
        hint.setTextColor(MainActivity.COLOR_LABEL);
        hint.setGravity(5);
        hint.setSingleLine(true);
        if (days <= 7) {
            hint.setText("目标 " + targetMl + " ml");
        } else {
            hint.setText("平均 " + this.amountFormat.format(WaterDailyChart.average(data))
                    + " · 最高 " + this.amountFormat.format(WaterDailyChart.maxValue(data)));
        }
        head.addView(hint, new LinearLayout.LayoutParams(-2, -2));

        int plotH = dp(96.0f);
        int labelBand = dp(15.0f);
        int usable = plotH - labelBand;
        int axisMax = WaterDailyChart.axisMax(data, targetMl);

        int availW = getResources().getDisplayMetrics().widthPixels - dp(40.0f) - dp(28.0f);
        int colW = Math.max(1, availW / days);
        int barW = Math.max(dp(3.0f), Math.round(colW * 0.62f));
        if (days <= 7) {
            barW = Math.min(barW, dp(26.0f));
        }

        FrameLayout plot = new FrameLayout(this);
        LinearLayout.LayoutParams plotLp = new LinearLayout.LayoutParams(-1, plotH);
        plotLp.topMargin = dp(13.0f);
        card.addView(plot, plotLp);

        // 目标虚线
        if (targetMl > 0 && targetMl <= axisMax) {
            View goalLine = new View(this);
            goalLine.setBackground(new DashedLine(MainActivity.COLOR_SOFT_BORDER, dp(1.2f), dp(4.0f), dp(3.0f)));
            FrameLayout.LayoutParams goalLp = new FrameLayout.LayoutParams(-1, dp(2.0f));
            goalLp.topMargin = Math.max(0, plotH - Math.round(usable * (targetMl / (float) axisMax)) - dp(1.0f));
            plot.addView(goalLine, goalLp);
        }

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(0);
        plot.addView(row, new FrameLayout.LayoutParams(-1, -1));

        int zeroPx = dp(2.0f);
        Calendar now = Calendar.getInstance();
        for (int i = 0; i < days; i++) {
            LinearLayout col = new LinearLayout(this);
            col.setOrientation(1);
            // BOTTOM | CENTER_HORIZONTAL：整列贴底，柱子从同一条基线长出来
            col.setGravity(0x51);
            row.addView(col, new LinearLayout.LayoutParams(0, -1, 1.0f));

            if (days <= 7 && data[i] > 0) {
                TextView value = new TextView(this);
                value.setText(this.amountFormat.format(data[i]));
                value.setTextSize(8.5f);
                value.setTextColor(MainActivity.COLOR_MUTED);
                value.setSingleLine(true);
                value.setIncludeFontPadding(false);
                value.setGravity(17);
                LinearLayout.LayoutParams valueLp = new LinearLayout.LayoutParams(-2, -2);
                valueLp.bottomMargin = dp(2.0f);
                col.addView(value, valueLp);
            }

            View bar = new View(this);
            bar.setBackground(rounded(data[i] <= 0 ? MainActivity.COLOR_TRACK
                    : (data[i] >= targetMl ? MainActivity.COLOR_ACCENT : COLOR_BAR_LOW),
                    dp(2.5f), 0, 0));
            col.addView(bar, new LinearLayout.LayoutParams(barW,
                    WaterDailyChart.barPx(data[i], axisMax, usable, zeroPx)));
        }

        // 横轴：绝对定位，不然 30 天格子只有 10dp，日期会被裁掉
        FrameLayout axis = new FrameLayout(this);
        LinearLayout.LayoutParams axisLp = new LinearLayout.LayoutParams(-1, dp(16.0f));
        axisLp.topMargin = dp(3.0f);
        card.addView(axis, axisLp);

        int labelW = days <= 7 ? dp(40.0f) : dp(24.0f);
        String[] labels = WaterDailyChart.labels(now, days, days > 7);
        for (int i = 0; i < days; i++) {
            if (!WaterDailyChart.showLabel(i, days)) {
                continue;
            }
            TextView label = new TextView(this);
            label.setText(labels[i]);
            label.setTextSize(days <= 7 ? 8.5f : 9.0f);
            label.setTextColor(MainActivity.COLOR_LABEL);
            label.setSingleLine(true);
            label.setIncludeFontPadding(false);
            label.setGravity(17);
            FrameLayout.LayoutParams labelLp = new FrameLayout.LayoutParams(labelW, -2);
            labelLp.leftMargin = Math.round((i * colW) + (colW / 2.0f) - (labelW / 2.0f));
            axis.addView(label, labelLp);
        }
        return card;
    }

    /**
     * 饮料卡片，两列。
     *
     * <p>原来每种饮料一整行（8 行），现在两列 4 行 —— 竖向省一半。
     */
    private LinearLayout buildDrinksGrid(ArrayList types, int[] mlArr, int[] cntArr) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(1);

        int gap = dp(9.0f);
        int availW = getResources().getDisplayMetrics().widthPixels - dp(40.0f);
        int cardW = (availW - gap) / 2;
        int innerW = Math.max(1, cardW - dp(26.0f));

        int maxMl = 1;
        for (int i = 0; i < types.size(); i++) {
            maxMl = Math.max(maxMl, mlArr[((Integer) types.get(i)).intValue()]);
        }

        for (int i = 0; i < types.size(); i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(0);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1, -2);
            if (i > 0) {
                rowLp.topMargin = gap;
            }
            wrap.addView(row, rowLp);

            row.addView(drinkCard(((Integer) types.get(i)).intValue(), mlArr, cntArr, maxMl, innerW),
                    new LinearLayout.LayoutParams(0, -2, 1.0f));

            if (i + 1 < types.size()) {
                LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(0, -2, 1.0f);
                rightLp.leftMargin = gap;
                row.addView(drinkCard(((Integer) types.get(i + 1)).intValue(), mlArr, cntArr, maxMl, innerW),
                        rightLp);
            } else {
                // 奇数个时补空位，否则最后一张卡会撑满整行
                row.addView(new View(this), new LinearLayout.LayoutParams(0, -2, 1.0f));
            }
        }
        return wrap;
    }

    private LinearLayout drinkCard(int type, int[] mlArr, int[] cntArr, int maxMl, int innerW) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(1);
        card.setPadding(dp(13.0f), dp(12.0f), dp(13.0f), dp(11.0f));
        card.setBackground(MainActivity.softCard(dp(16.0f)));

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(0);
        top.setGravity(16);
        card.addView(top, matchWrap());

        TextView name = new TextView(this);
        name.setText(DrinkTypes.name(this, type));
        name.setTextSize(13.0f);
        name.setTextColor(MainActivity.COLOR_TEXT);
        name.setTypeface(Typeface.create("sans", 1));
        name.setSingleLine(true);
        name.setEllipsize(TextUtils.TruncateAt.END);
        top.addView(name, new LinearLayout.LayoutParams(0, -2, 1.0f));

        TextView count = new TextView(this);
        count.setText(cntArr[type] + " " + DrinkTypes.unit(this, type));
        count.setTextSize(10.0f);
        count.setTextColor(MainActivity.COLOR_LABEL);
        count.setSingleLine(true);
        top.addView(count, new LinearLayout.LayoutParams(-2, -2));

        String number = this.amountFormat.format(mlArr[type]);
        SpannableString amount = new SpannableString(number + " ml");
        amount.setSpan(new RelativeSizeSpan(0.6f), number.length(), amount.length(), 33);
        amount.setSpan(new ForegroundColorSpan(MainActivity.COLOR_LABEL), number.length(), amount.length(), 33);
        TextView amountView = new TextView(this);
        amountView.setText(amount);
        amountView.setTextSize(16.5f);
        amountView.setTextColor(MainActivity.COLOR_BLUE);
        amountView.setTypeface(Typeface.create("sans", 1));
        amountView.setSingleLine(true);
        LinearLayout.LayoutParams amountLp = new LinearLayout.LayoutParams(-2, -2);
        amountLp.topMargin = dp(2.0f);
        card.addView(amountView, amountLp);

        int trackH = dp(5.0f);
        FrameLayout track = new FrameLayout(this);
        track.setBackground(rounded(MainActivity.COLOR_TRACK, trackH / 2, 0, 0));
        LinearLayout.LayoutParams trackLp = new LinearLayout.LayoutParams(-1, trackH);
        trackLp.topMargin = dp(9.0f);
        card.addView(track, trackLp);

        // 宽度直接算出来，不用等布局回调（OnLayoutChangeListener 之前踩过坑）
        float frac = Math.min(1.0f, mlArr[type] / (float) maxMl);
        View fill = new View(this);
        fill.setBackground(rounded(MainActivity.COLOR_ACCENT, trackH / 2, 0, 0));
        track.addView(fill, new FrameLayout.LayoutParams(
                Math.max(trackH, Math.round(innerW * frac)), trackH));
        return card;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private GradientDrawable rounded(int i, int i2, int i3, int i4) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(dp(i2));
        if (i4 > 0) {
            gradientDrawable.setStroke(dp(i4), i3);
        }
        return gradientDrawable;
    }
}
