package com.salaryjump.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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
        float f = 18.0f;
        float f2 = 14.0f;
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
        int i14 = 1;
        for (int i15 = 0; i15 < 32; i15++) {
            if (iArr[i15] > i14) {
                i14 = iArr[i15];
            }
        }
        Iterator it2 = arrayList.iterator();
        while (it2.hasNext()) {
            int intValue = ((Integer) it2.next()).intValue();
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(1);
            linearLayout.setPadding(dp(f), dp(f2), dp(f), dp(f2));
            linearLayout.setBackground(MainActivity.softCard(dp(16.0f)));
            LinearLayout.LayoutParams matchWrap = matchWrap();
            matchWrap.topMargin = dp(10.0f);
            this.listContainer.addView(linearLayout, matchWrap);
            LinearLayout linearLayout2 = new LinearLayout(this);
            linearLayout2.setOrientation(0);
            linearLayout2.setGravity(16);
            linearLayout.addView(linearLayout2, matchWrap());
            TextView textView2 = new TextView(this);
            textView2.setText(DrinkTypes.name(this, intValue));
            textView2.setTextSize(15.0f);
            textView2.setTextColor(MainActivity.COLOR_TEXT);
            textView2.setTypeface(Typeface.create("sans", 1));
            linearLayout2.addView(textView2, new LinearLayout.LayoutParams(0, dp(26.0f), 1.0f));
            TextView textView3 = new TextView(this);
            textView3.setText(iArr2[intValue] + " " + DrinkTypes.unit(this, intValue) + " · " + iArr[intValue] + " ml");
            textView3.setTextSize(13.0f);
            textView3.setTextColor(MainActivity.COLOR_MUTED);
            textView3.setGravity(5);
            linearLayout2.addView(textView3, new LinearLayout.LayoutParams(-2, dp(26.0f)));
            SheenBar sheenBar = new SheenBar(this);
            sheenBar.setProgress(iArr[intValue] / Math.max(1, i14));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(8.0f));
            layoutParams.topMargin = dp(10.0f);
            linearLayout.addView(sheenBar, layoutParams);
            f = 18.0f;
            f2 = 14.0f;
        }
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
