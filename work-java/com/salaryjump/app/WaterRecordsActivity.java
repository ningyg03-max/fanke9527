package com.salaryjump.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.salaryjump.app.WaterReminderScheduler;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/* loaded from: classes.dex */
public class WaterRecordsActivity extends Activity {
    private Button clearButton;
    private float density;
    private LinearLayout listContainer;
    private TextView summaryView;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        MainActivity.applyTheme(this);
        this.density = getResources().getDisplayMetrics().density;
        configureWindow();
        buildInterface();
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        refreshRecords();
    }

    private void configureWindow() {
        MainActivity.applyEdgeToEdge(getWindow());
    }

    private void buildInterface() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20.0f), dp(20.0f), dp(20.0f), dp(28.0f));
        scrollView.addView(linearLayout, new FrameLayout.LayoutParams(-1, -2));
        setContentView(UiKit.aurora(this, scrollView));
        MotionKit.enterChildren(linearLayout, this.density);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        linearLayout.addView(linearLayout2, matchWrap());
        Button button = new Button(this);
        button.setText("返回");
        button.setTextSize(13.0f);
        int i = MainActivity.COLOR_TEXT;
        button.setTextColor(i);
        button.setAllCaps(false);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setPadding(dp(14.0f), 0, dp(14.0f), 0);
        int i2 = MainActivity.COLOR_BORDER;
        button.setBackground(MainActivity.softPill(dp(21.0f), dp(1.5f)));
        button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.WaterRecordsActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                WaterRecordsActivity.this.finish();
            }
        });
        linearLayout2.addView(button, new LinearLayout.LayoutParams(-2, dp(42.0f)));
        TextView textView = new TextView(this);
        textView.setText("喝水记录");
        textView.setTextSize(20.0f);
        textView.setTextColor(i);
        textView.setTypeface(Typeface.create("sans", 1));
        textView.setGravity(17);
        linearLayout2.addView(textView, new LinearLayout.LayoutParams(0, dp(42.0f), 1.0f));
        Button button2 = new Button(this);
        this.clearButton = button2;
        button2.setText("清空全部");
        this.clearButton.setTextSize(13.0f);
        this.clearButton.setTextColor(MainActivity.COLOR_DANGER);
        this.clearButton.setAllCaps(false);
        this.clearButton.setMinWidth(0);
        this.clearButton.setMinHeight(0);
        this.clearButton.setPadding(dp(14.0f), 0, dp(14.0f), 0);
        this.clearButton.setBackground(MainActivity.softPill(dp(21.0f), dp(1.5f)));
        this.clearButton.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.WaterRecordsActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                WaterRecordsActivity.this.confirmClearAll();
            }
        });
        linearLayout2.addView(this.clearButton, new LinearLayout.LayoutParams(-2, dp(42.0f)));
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(1);
        linearLayout3.setPadding(dp(18.0f), dp(18.0f), dp(18.0f), dp(18.0f));
        linearLayout3.setBackground(MainActivity.softCard(dp(20.0f)));
        linearLayout3.setElevation(dp(3.0f));
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(18.0f);
        linearLayout.addView(linearLayout3, matchWrap);
        TextView textView2 = new TextView(this);
        this.summaryView = textView2;
        textView2.setTextSize(18.0f);
        this.summaryView.setTextColor(MainActivity.COLOR_ACCENT);
        this.summaryView.setTypeface(Typeface.create("sans", 1));
        linearLayout3.addView(this.summaryView);
        TextView textView3 = new TextView(this);
        textView3.setText("点记录的时间可修改；每条记录可单独删除，总喝水量会自动重新计算。");
        textView3.setTextSize(13.0f);
        textView3.setTextColor(MainActivity.COLOR_MUTED);
        textView3.setPadding(0, dp(8.0f), 0, 0);
        linearLayout3.addView(textView3);
        LinearLayout linearLayout4 = new LinearLayout(this);
        this.listContainer = linearLayout4;
        linearLayout4.setOrientation(1);
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(16.0f);
        linearLayout.addView(this.listContainer, matchWrap2);
    }

    public void refreshRecords() {
        WaterReminderSettings load = WaterReminderSettings.load(this);
        List<WaterReminderScheduler.WaterRecord> records = WaterReminderScheduler.getRecords(this);
        Collections.sort(records, new Comparator<WaterReminderScheduler.WaterRecord>() { // from class: com.salaryjump.app.WaterRecordsActivity.3
            @Override // java.util.Comparator
            public int compare(WaterReminderScheduler.WaterRecord waterRecord, WaterReminderScheduler.WaterRecord waterRecord2) {
                if (waterRecord.timestamp < waterRecord2.timestamp) {
                    return 1;
                }
                if (waterRecord.timestamp == waterRecord2.timestamp) {
                    return 0;
                }
                return -1;
            }
        });
        this.summaryView.setText("今日已喝 " + WaterReminderScheduler.getConsumed(this) + " / " + load.targetMl + " ml");
        int i = 0;
        this.clearButton.setVisibility(records.isEmpty() ? 8 : 0);
        this.listContainer.removeAllViews();
        boolean isEmpty = records.isEmpty();
        int i2 = MainActivity.COLOR_SURFACE;
        int i3 = 17;
        if (isEmpty) {
            TextView textView = new TextView(this);
            textView.setText("今天还没有喝水记录");
            textView.setTextSize(15.0f);
            textView.setTextColor(MainActivity.COLOR_MUTED);
            textView.setGravity(17);
            textView.setPadding(0, dp(44.0f), 0, dp(44.0f));
            textView.setBackground(MainActivity.softCard(dp(18.0f)));
            this.listContainer.addView(textView, matchWrap());
            return;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        for (final WaterReminderScheduler.WaterRecord waterRecord : records) {
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(i);
            linearLayout.setGravity(16);
            linearLayout.setPadding(dp(14.0f), dp(12.0f), dp(10.0f), dp(12.0f));
            int i4 = MainActivity.COLOR_BORDER;
            linearLayout.setBackground(MainActivity.softCard(dp(16.0f)));
            TextView textView2 = new TextView(this);
            textView2.setText(simpleDateFormat.format(Long.valueOf(waterRecord.timestamp)));
            textView2.setTextSize(11.0f);
            textView2.setTextColor(MainActivity.COLOR_BLUE);
            textView2.setTypeface(Typeface.create("monospace", 1));
            textView2.setSingleLine(true);
            textView2.setGravity(i3);
            textView2.setPadding(dp(9.0f), i, dp(9.0f), i);
            textView2.setBackground(MainActivity.softPill(dp(16.0f), dp(1.0f)));
            textView2.setClickable(true);
            textView2.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.WaterRecordsActivity.4
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    WaterRecordsActivity.this.showTimeEditDialog(waterRecord);
                }
            });
            linearLayout.addView(textView2, new LinearLayout.LayoutParams(-2, dp(34.0f)));
            TextView textView3 = new TextView(this);
            int recordKcal = DrinkTypes.recordKcal(this, waterRecord.type, waterRecord.amountMl);
            textView3.setText(DrinkTypes.name(this, waterRecord.type) + " " + waterRecord.amountMl + " ml" + (recordKcal > 0 ? " · " + recordKcal + " kcal" : " · 0 kcal"));
            textView3.setTextSize(12.0f);
            textView3.setTextColor(MainActivity.COLOR_ACCENT);
            textView3.setGravity(16);
            textView3.setSingleLine(true);
            textView3.setEllipsize(TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(44.0f), 1.0f);
            layoutParams.leftMargin = dp(8.0f);
            linearLayout.addView(textView3, layoutParams);
            Button button = new Button(this);
            button.setText("删除");
            button.setTextSize(13.0f);
            button.setTextColor(MainActivity.COLOR_DANGER);
            button.setAllCaps(false);
            button.setMinWidth(0);
            button.setMinHeight(0);
            button.setPadding(dp(9.0f), 0, dp(9.0f), 0);
            button.setBackground(MainActivity.softPill(dp(19.0f), dp(1.5f)));
            button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.WaterRecordsActivity.5
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    if (WaterReminderScheduler.deleteRecord(WaterRecordsActivity.this, waterRecord.timestamp)) {
                        SalaryWidgetProvider.refreshAll(WaterRecordsActivity.this);
                        WaterReminderScheduler.scheduleNext(WaterRecordsActivity.this);
                        WaterRecordsActivity.this.refreshRecords();
                    }
                }
            });
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, dp(40.0f));
            layoutParams2.leftMargin = dp(8.0f);
            linearLayout.addView(button, layoutParams2);
            LinearLayout.LayoutParams matchWrap = matchWrap();
            matchWrap.bottomMargin = dp(10.0f);
            this.listContainer.addView(linearLayout, matchWrap);
            int i5 = MainActivity.COLOR_SURFACE;
            i = 0;
            i3 = 17;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showTimeEditDialog(final WaterReminderScheduler.WaterRecord waterRecord) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(waterRecord.timestamp);
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() { // from class: com.salaryjump.app.WaterRecordsActivity.6
            @Override // android.app.TimePickerDialog.OnTimeSetListener
            public void onTimeSet(TimePicker timePicker, int i, int i2) {
                Calendar calendar2 = Calendar.getInstance();
                calendar2.setTimeInMillis(waterRecord.timestamp);
                calendar2.set(11, i);
                calendar2.set(12, i2);
                if (WaterReminderScheduler.updateRecordTime(WaterRecordsActivity.this, waterRecord.timestamp, calendar2.getTimeInMillis())) {
                    Toast.makeText(WaterRecordsActivity.this, "时间已修改", 0).show();
                    WaterRecordsActivity.this.refreshRecords();
                }
            }
        }, calendar.get(11), calendar.get(12), true).show();
    }

    public void confirmClearAll() {
        new AlertDialog.Builder(this).setTitle("清空喝水记录").setMessage("确定删除今天的全部喝水记录吗？").setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("清空", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.WaterRecordsActivity.7
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                WaterReminderScheduler.resetConsumed(WaterRecordsActivity.this);
                SalaryWidgetProvider.refreshAll(WaterRecordsActivity.this);
                WaterReminderScheduler.scheduleNext(WaterRecordsActivity.this);
                Toast.makeText(WaterRecordsActivity.this, "今天的喝水记录已清空", 0).show();
                WaterRecordsActivity.this.refreshRecords();
            }
        }).show();
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

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private int dp(float f) {
        return Math.round(f * this.density);
    }
}
