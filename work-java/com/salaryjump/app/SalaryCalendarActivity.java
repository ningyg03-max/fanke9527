package com.salaryjump.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.salaryjump.app.LeaveRecord;
import com.salaryjump.app.MainActivity;
import com.salaryjump.app.OvertimeLog;
import com.salaryjump.app.SalaryHistory;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class SalaryCalendarActivity extends Activity {
    private static final String[] WEEK_NAMES = {"日", "一", "二", "三", "四", "五", "六"};
    private float density;
    private LinearLayout listContainer;
    /** 详情卡（点某天后显示当天拆分） */
    private LinearLayout detailContainer;
    /** 当前选中的日期戳（0 = 未选，默认选今天） */
    private int selectedStamp;
    /** 当前高亮格子的日期戳 + 视图（点选时只换底色，不重建网格） */
    private int selectedCellStamp;
    private LinearLayout selectedCellView;
    /** 当前月里「今天」的日期戳 */
    private int todayStamp;
    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
    private int month;
    private TextView monthTitleView;
    private TextView monthTotalView;
    private TextView summaryView;
    private int year;

    static /* synthetic */ int access$112(SalaryCalendarActivity salaryCalendarActivity, int i) {
        int i2 = salaryCalendarActivity.month + i;
        salaryCalendarActivity.month = i2;
        return i2;
    }

    static /* synthetic */ int access$208(SalaryCalendarActivity salaryCalendarActivity) {
        int i = salaryCalendarActivity.year;
        salaryCalendarActivity.year = i + 1;
        return i;
    }

    static /* synthetic */ int access$210(SalaryCalendarActivity salaryCalendarActivity) {
        int i = salaryCalendarActivity.year;
        salaryCalendarActivity.year = i - 1;
        return i;
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        MainActivity.applyTheme(this);
        this.density = getResources().getDisplayMetrics().density;
        Calendar calendar = Calendar.getInstance();
        this.year = calendar.get(1);
        this.month = calendar.get(2) + 1;
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
        linearLayout.setPadding(dp(20.0f), dp(18.0f), dp(20.0f), dp(26.0f));
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
        button.setTextColor(MainActivity.COLOR_TEXT);
        button.setAllCaps(false);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setPadding(dp(12.0f), 0, dp(12.0f), 0);
        button.setBackground(MainActivity.softPill(dp(21.0f), dp(1.5f)));
        button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.SalaryCalendarActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                SalaryCalendarActivity.this.finish();
            }
        });
        linearLayout2.addView(button, new LinearLayout.LayoutParams(-2, dp(42.0f)));
        TextView textView = new TextView(this);
        textView.setText("工资日历");
        textView.setTextSize(20.0f);
        textView.setTextColor(MainActivity.COLOR_TEXT);
        textView.setTypeface(Typeface.create("sans", 1));
        textView.setGravity(17);
        linearLayout2.addView(textView, new LinearLayout.LayoutParams(0, dp(42.0f), 1.0f));
        Button button2 = new Button(this);
        button2.setText("补记");
        button2.setTextSize(13.0f);
        button2.setTextColor(MainActivity.COLOR_ACCENT);
        button2.setAllCaps(false);
        button2.setMinWidth(0);
        button2.setMinHeight(0);
        button2.setPadding(dp(12.0f), 0, dp(12.0f), 0);
        button2.setBackground(MainActivity.softPill(dp(21.0f), dp(1.5f)));
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.SalaryCalendarActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                SalaryCalendarActivity.this.showBackfillDialog();
            }
        });
        linearLayout2.addView(button2, new LinearLayout.LayoutParams(-2, dp(42.0f)));
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(0);
        linearLayout3.setGravity(16);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(14.0f);
        linearLayout.addView(linearLayout3, matchWrap);
        linearLayout3.addView(monthNav("‹", -1), new LinearLayout.LayoutParams(dp(44.0f), dp(40.0f)));
        this.monthTitleView = new TextView(this);
        this.monthTitleView.setTextSize(17.0f);
        this.monthTitleView.setTextColor(MainActivity.COLOR_TEXT);
        this.monthTitleView.setGravity(17);
        this.monthTitleView.setTypeface(Typeface.create("sans", 1));
        linearLayout3.addView(this.monthTitleView, new LinearLayout.LayoutParams(0, dp(40.0f), 1.0f));
        linearLayout3.addView(monthNav("›", 1), new LinearLayout.LayoutParams(dp(44.0f), dp(40.0f)));
        LinearLayout linearLayout4 = new LinearLayout(this);
        linearLayout4.setOrientation(1);
        linearLayout4.setPadding(dp(20.0f), dp(18.0f), dp(20.0f), dp(18.0f));
        linearLayout4.setBackground(MainActivity.softCard(dp(20.0f)));
        linearLayout4.setElevation(dp(3.0f));
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(12.0f);
        linearLayout.addView(linearLayout4, matchWrap2);
        LinearLayout linearLayout5 = new LinearLayout(this);
        linearLayout5.setOrientation(0);
        linearLayout5.setGravity(16);
        linearLayout4.addView(linearLayout5, matchWrap());
        View view = new View(this);
        view.setBackground(UiKit.circle(MainActivity.COLOR_ACCENT_BRIGHT));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(7.0f), dp(7.0f));
        layoutParams.rightMargin = dp(7.0f);
        linearLayout5.addView(view, layoutParams);
        TextView textView2 = new TextView(this);
        textView2.setText("本月合计");
        textView2.setTextSize(11.5f);
        textView2.setTextColor(MainActivity.COLOR_LABEL);
        textView2.setLetterSpacing(0.12f);
        textView2.setTypeface(Typeface.create("sans", 1));
        linearLayout5.addView(textView2, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView textView3 = new TextView(this);
        textView3.setText("战报 ›");
        textView3.setTextSize(12.5f);
        textView3.setTextColor(MainActivity.COLOR_BLUE);
        textView3.setGravity(21);
        textView3.setPadding(dp(10.0f), 0, dp(2.0f), 0);
        textView3.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.SalaryCalendarActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                try {
                    SalaryCalendarActivity.this.startActivity(new Intent(SalaryCalendarActivity.this, (Class<?>) MonthlyReportActivity.class));
                } catch (Throwable th) {
                }
            }
        });
        linearLayout5.addView(textView3, new LinearLayout.LayoutParams(-2, -2));
        this.monthTotalView = new TextView(this);
        this.monthTotalView.setTextSize(30.0f);
        this.monthTotalView.setTextColor(MainActivity.COLOR_ACCENT);
        this.monthTotalView.setTypeface(Typeface.create("sans", 1));
        this.monthTotalView.setIncludeFontPadding(false);
        LinearLayout.LayoutParams matchWrap3 = matchWrap();
        matchWrap3.topMargin = dp(10.0f);
        linearLayout4.addView(this.monthTotalView, matchWrap3);
        this.summaryView = new TextView(this);
        this.summaryView.setTextSize(12.5f);
        this.summaryView.setTextColor(MainActivity.COLOR_MUTED);
        LinearLayout.LayoutParams matchWrap4 = matchWrap();
        matchWrap4.topMargin = dp(8.0f);
        linearLayout4.addView(this.summaryView, matchWrap4);
        this.listContainer = new LinearLayout(this);
        this.listContainer.setOrientation(1);
        LinearLayout.LayoutParams matchWrap5 = matchWrap();
        matchWrap5.topMargin = dp(14.0f);
        linearLayout.addView(this.listContainer, matchWrap5);

        this.detailContainer = new LinearLayout(this);
        this.detailContainer.setOrientation(1);
        LinearLayout.LayoutParams matchWrap6 = matchWrap();
        matchWrap6.topMargin = dp(12.0f);
        linearLayout.addView(this.detailContainer, matchWrap6);
    }

    private TextView monthNav(String str, final int i) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(20.0f);
        textView.setTextColor(MainActivity.COLOR_BLUE);
        textView.setGravity(17);
        textView.setBackground(MainActivity.softPill(dp(20.0f), dp(1.5f)));
        textView.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.SalaryCalendarActivity.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                SalaryCalendarActivity.access$112(SalaryCalendarActivity.this, i);
                int i2 = SalaryCalendarActivity.this.month;
                SalaryCalendarActivity salaryCalendarActivity = SalaryCalendarActivity.this;
                if (i2 > 12) {
                    salaryCalendarActivity.month = 1;
                    SalaryCalendarActivity.access$208(SalaryCalendarActivity.this);
                } else if (salaryCalendarActivity.month < 1) {
                    SalaryCalendarActivity.this.month = 12;
                    SalaryCalendarActivity.access$210(SalaryCalendarActivity.this);
                }
                SalaryCalendarActivity.this.render();
            }
        });
        return textView;
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        if (PrivacyLock.isEnabled(this) && !MainActivity.isAppUnlocked()) {
            finish();
        } else {
            render();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void render() {
        this.monthTitleView.setText(this.year + " 年 " + this.month + " 月");
        double monthTotal = SalaryHistory.monthTotalCents(this, this.year, this.month) / 100.0d;
        this.monthTotalView.setText("¥ " + this.moneyFormat.format(monthTotal));

        int todayStamp = SalaryHistory.stamp(Calendar.getInstance());
        this.todayStamp = todayStamp;
        this.selectedCellView = null;
        this.selectedCellStamp = 0;
        List<SalaryHistory.Day> byMonth = SalaryHistory.byMonth(this, this.year, this.month);

        // 统计：计薪天数 / 日均
        boolean isCurrentMonth = SalaryHistory.yearOf(todayStamp) == this.year
                && SalaryHistory.monthOf(todayStamp) == this.month;
        int statDays = MonthBackfill.statDays(this, this.year, this.month,
                byMonth.size() + (isCurrentMonth ? 0 : 1));
        String avg = statDays > 0 ? this.moneyFormat.format(monthTotal / statDays) : "0.00";
        this.summaryView.setText(statDays + " 个工作日 · 日均 " + avg + " 元"
                + (isCurrentMonth ? " · 今天进行中" : ""));

        // 该月每天一行记录（用于快速查表）
        java.util.HashMap<Integer, Long> centsByDay = new java.util.HashMap<>();
        for (int i = 0; i < byMonth.size(); i++) {
            centsByDay.put(Integer.valueOf(byMonth.get(i).stamp), Long.valueOf(byMonth.get(i).cents));
        }

        // 默认选中今天（若在本月），否则选中该月最后一天
        if (this.selectedStamp == 0
                || SalaryHistory.yearOf(this.selectedStamp) != this.year
                || SalaryHistory.monthOf(this.selectedStamp) != this.month) {
            boolean todayInMonth = SalaryHistory.yearOf(todayStamp) == this.year
                    && SalaryHistory.monthOf(todayStamp) == this.month;
            int lastDay = daysInMonth(this.year, this.month);
            this.selectedStamp = todayInMonth ? todayStamp
                    : (this.year * 10000) + (this.month * 100) + lastDay;
        }

        this.listContainer.removeAllViews();
        buildCalendarGrid(todayStamp);
        buildDetailCard(todayStamp);
    }

    private static int daysInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /** 日历网格（共用组件 CalendarGrid）。 */
    private void buildCalendarGrid(final int todayStamp) {
        LinearLayout card = CalendarGrid.build(this, this.year, this.month, todayStamp,
                this.selectedStamp, new CalendarGrid.DayListener() {
                    @Override // com.salaryjump.app.CalendarGrid.DayListener
                    public void onDay(int stamp, LinearLayout cell) {
                        SalaryCalendarActivity salaryCalendarActivity = SalaryCalendarActivity.this;
                        // 只换两格底色 + 重建详情卡，不重建整个网格
                        salaryCalendarActivity.unselectCurrentCell();
                        salaryCalendarActivity.selectedStamp = stamp;
                        salaryCalendarActivity.selectedCellView = cell;
                        salaryCalendarActivity.selectedCellStamp = stamp;
                        CalendarGrid.applySelectedBackground(salaryCalendarActivity, cell);
                        salaryCalendarActivity.buildDetailCard(todayStamp);
                    }
                });
        this.listContainer.addView(card, matchWrap());
    }

    /** 把上一个高亮格子的底色还原成常规样式。 */
    private void unselectCurrentCell() {
        if (this.selectedCellView == null || this.selectedCellStamp == 0) {
            return;
        }
        DayBreakdown bd = DayBreakdown.of(this, this.selectedCellStamp);
        CalendarGrid.applyBackground(this, this.selectedCellView, bd,
                this.selectedCellStamp == this.todayStamp, false);
        this.selectedCellView = null;
        this.selectedCellStamp = 0;
    }

    private boolean isFuture(int stamp, int todayStamp) {
        return stamp > todayStamp;
    }

    private boolean isRestDay(int stamp) {
        try {
            MainActivity.SalarySettings settings =
                    MainActivity.SalarySettings.load(getSharedPreferences("salary_jump", 0));
            Calendar calendar = Calendar.getInstance();
            calendar.set(SalaryHistory.yearOf(stamp), SalaryHistory.monthOf(stamp) - 1,
                    SalaryHistory.dayOf(stamp));
            return !WorkdayHelper.isWorkday(this, settings, calendar);
        } catch (Throwable unused) {
            return false;
        }
    }

    /** 详情卡：选中那天的完整拆分（日薪 / 加班费 / 外快 / 补录）+ 操作入口。 */
    private void buildDetailCard(int todayStamp) {
        this.detailContainer.removeAllViews();
        int stamp = this.selectedStamp;
        if (stamp == 0) {
            return;
        }
        DayBreakdown bd = DayBreakdown.of(this, stamp);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(1);
        card.setPadding(dp(16.0f), dp(14.0f), dp(16.0f), dp(14.0f));
        card.setBackground(MainActivity.softCard(dp(18.0f)));
        this.detailContainer.addView(card, matchWrap());

        // 标题行：日期 · 星期 · 类型
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(0);
        titleRow.setGravity(16);
        card.addView(titleRow, matchWrap());
        Calendar calendar = Calendar.getInstance();
        calendar.set(SalaryHistory.yearOf(stamp), SalaryHistory.monthOf(stamp) - 1,
                SalaryHistory.dayOf(stamp));
        TextView title = new TextView(this);
        title.setText(SalaryHistory.monthOf(stamp) + " 月 " + SalaryHistory.dayOf(stamp) + " 日 · 周"
                + WEEK_NAMES[calendar.get(7) - 1]);
        title.setTextSize(13.5f);
        title.setTextColor(MainActivity.COLOR_TEXT);
        title.setTypeface(Typeface.create("sans", 1));
        titleRow.addView(title, new LinearLayout.LayoutParams(0, -2, 1.0f));

        TextView opButton = new TextView(this);
        opButton.setText("操作 ›");
        opButton.setTextSize(12.5f);
        opButton.setTextColor(MainActivity.COLOR_BLUE);
        opButton.setPadding(dp(10.0f), dp(4.0f), dp(2.0f), dp(4.0f));
        opButton.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.SalaryCalendarActivity.12
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                SalaryCalendarActivity.this.showMarkDialog(SalaryCalendarActivity.this.selectedStamp,
                        !SalaryCalendarActivity.this.isRestDay(SalaryCalendarActivity.this.selectedStamp));
            }
        });
        titleRow.addView(opButton, wrapWrap());

        // 类型说明
        TextView typeView = new TextView(this);
        typeView.setText(bd.dayTypeText());
        typeView.setTextSize(11.5f);
        typeView.setTextColor(MainActivity.COLOR_MUTED);
        LinearLayout.LayoutParams typeParams = wrapWrap();
        typeParams.topMargin = dp(3.0f);
        card.addView(typeView, typeParams);

        if (bd.totalCents == 0) {
            TextView empty = new TextView(this);
            empty.setText(bd.hasRecord ? "这天没有入账（" + bd.dayTypeText() + "）" : "这天没有入账记录");
            empty.setTextSize(12.5f);
            empty.setTextColor(MainActivity.COLOR_LABEL);
            LinearLayout.LayoutParams emptyParams = wrapWrap();
            emptyParams.topMargin = dp(10.0f);
            card.addView(empty, emptyParams);
            return;
        }

        addDetailRow(card, "日薪", bd.baseCents, MainActivity.COLOR_TEXT);
        if (bd.hasOvertime()) {
            addDetailRow(card, "加班费 · " + OvertimeLog.format(bd.overtimeSeconds),
                    bd.overtimeCents, MainActivity.COLOR_AMBER);
        }
        if (bd.extraCents > 0) {
            String remark = ExtraIncome.remarkOf(this, stamp);
            String label = (remark == null || remark.length() == 0) ? "外快" : "外快 · " + remark;
            addDetailRow(card, label, bd.extraCents, MainActivity.COLOR_EXTRA);
        }
        if (bd.backfillCents > 0) {
            addDetailRow(card, "补录收入", bd.backfillCents, MainActivity.COLOR_BLUE);
        }

        // 合计
        android.view.View divider = new android.view.View(this);
        divider.setBackgroundColor(MainActivity.COLOR_BORDER);
        LinearLayout.LayoutParams dividerParams = matchWrap();
        dividerParams.topMargin = dp(10.0f);
        card.addView(divider, new LinearLayout.LayoutParams(-1, dp(1.0f)));

        LinearLayout totalRow = new LinearLayout(this);
        totalRow.setOrientation(0);
        totalRow.setGravity(16);
        LinearLayout.LayoutParams totalParams = matchWrap();
        totalParams.topMargin = dp(9.0f);
        card.addView(totalRow, totalParams);
        TextView totalLabel = new TextView(this);
        totalLabel.setText("当天到手");
        totalLabel.setTextSize(12.5f);
        totalLabel.setTextColor(MainActivity.COLOR_MUTED);
        totalRow.addView(totalLabel, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView totalValue = new TextView(this);
        totalValue.setText("¥ " + this.moneyFormat.format(bd.totalCents / 100.0d));
        totalValue.setTextSize(18.0f);
        totalValue.setTextColor(MainActivity.COLOR_ACCENT);
        totalValue.setTypeface(Typeface.create("sans", 1));
        totalValue.setIncludeFontPadding(false);
        totalRow.addView(totalValue, wrapWrap());
    }

    private void addDetailRow(LinearLayout parent, String label, long cents, int valueColor) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(0);
        row.setGravity(16);
        LinearLayout.LayoutParams rowParams = matchWrap();
        rowParams.topMargin = dp(8.0f);
        parent.addView(row, rowParams);

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(12.5f);
        labelView.setTextColor(MainActivity.COLOR_MUTED);
        row.addView(labelView, new LinearLayout.LayoutParams(0, -2, 1.0f));

        TextView valueView = new TextView(this);
        valueView.setText("¥ " + this.moneyFormat.format(cents / 100.0d));
        valueView.setTextSize(13.5f);
        valueView.setTextColor(valueColor);
        valueView.setTypeface(Typeface.create("sans", 1));
        row.addView(valueView, wrapWrap());
    }

    private LinearLayout.LayoutParams wrapWrap() {
        return new LinearLayout.LayoutParams(-2, -2);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    public void showBackfillDialog() {
        new AlertDialog.Builder(this).setTitle("补录").setItems(new String[]{"某一天（出勤 / 请假 / 外快）", "整月收入（直接录入金额）"}, new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.SalaryCalendarActivity.6
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                SalaryCalendarActivity salaryCalendarActivity = SalaryCalendarActivity.this;
                if (i == 0) {
                    salaryCalendarActivity.showDayBackfillDialog();
                } else {
                    salaryCalendarActivity.showMonthBackfillDialog();
                }
            }
        }).setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showMonthBackfillDialog() {
        final int i = this.year;
        final int i2 = this.month;
        int lastDayStamp = MonthBackfill.lastDayStamp(i, i2);
        if (lastDayStamp > SalaryHistory.stamp(Calendar.getInstance())) {
            Toast.makeText(this, "该月还没结束，暂不支持补录\n可翻到更早的月份操作", 1).show();
            return;
        }
        int earliestAllowedStamp = MonthBackfill.earliestAllowedStamp();
        if (lastDayStamp < earliestAllowedStamp) {
            Toast.makeText(this, "该月超出保留期，最早可补录到 " + (earliestAllowedStamp / 10000) + " 年 " + ((earliestAllowedStamp / 100) % 100) + " 月", 1).show();
            return;
        }
        long monthCents = MonthBackfill.monthCents(this, i, i2);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20.0f), dp(8.0f), dp(20.0f), dp(4.0f));
        TextView textView = new TextView(this);
        textView.setText("金额（元）");
        textView.setTextSize(12.5f);
        textView.setTextColor(MainActivity.COLOR_MUTED);
        linearLayout.addView(textView, matchWrap());
        final EditText editText = new EditText(this);
        editText.setInputType(8194);
        editText.setTextSize(18.0f);
        editText.setTextColor(MainActivity.COLOR_TEXT);
        editText.setSingleLine(true);
        editText.setPadding(dp(12.0f), dp(10.0f), dp(12.0f), dp(10.0f));
        editText.setBackground(UiKit.solid(MainActivity.COLOR_BACKGROUND, dp(9.0f), MainActivity.COLOR_BORDER, dp(1.0f)));
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(6.0f);
        linearLayout.addView(editText, matchWrap);
        TextView textView2 = new TextView(this);
        textView2.setText("备注（可选）");
        textView2.setTextSize(12.5f);
        textView2.setTextColor(MainActivity.COLOR_MUTED);
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(12.0f);
        linearLayout.addView(textView2, matchWrap2);
        final EditText editText2 = new EditText(this);
        editText2.setTextSize(14.0f);
        editText2.setTextColor(MainActivity.COLOR_TEXT);
        editText2.setSingleLine(true);
        editText2.setPadding(dp(12.0f), dp(10.0f), dp(12.0f), dp(10.0f));
        editText2.setBackground(UiKit.solid(MainActivity.COLOR_BACKGROUND, dp(9.0f), MainActivity.COLOR_BORDER, dp(1.0f)));
        LinearLayout.LayoutParams matchWrap3 = matchWrap();
        matchWrap3.topMargin = dp(6.0f);
        linearLayout.addView(editText2, matchWrap3);
        TextView textView3 = new TextView(this);
        String str = "将记入 " + i2 + " 月 " + (lastDayStamp % 100) + " 日，显示为「补录收入」；已归档月份的金额会自动并入累计";
        if (monthCents > 0) {
            str = str + "\n该月已补录 ¥" + this.moneyFormat.format(monthCents / 100.0d) + "，本次为叠加";
        }
        textView3.setText(str);
        textView3.setTextSize(11.0f);
        textView3.setTextColor(MainActivity.COLOR_LABEL);
        textView3.setLineSpacing(dp(2.0f), 1.1f);
        LinearLayout.LayoutParams matchWrap4 = matchWrap();
        matchWrap4.topMargin = dp(10.0f);
        linearLayout.addView(textView3, matchWrap4);
        new AlertDialog.Builder(this).setTitle("补录 " + i + " 年 " + i2 + " 月 收入").setView(linearLayout).setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("保存", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.SalaryCalendarActivity.7
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i3) {
                double d;
                try {
                    d = Double.parseDouble(editText.getText().toString().trim().replace(",", ""));
                } catch (Throwable th) {
                    d = 0.0d;
                }
                if (d <= 0.0d || d > 1000000.0d) {
                    Toast.makeText(SalaryCalendarActivity.this, "请输入正确的金额（0.01 ~ 1000000）", 1).show();
                    return;
                }
                boolean add = MonthBackfill.add(SalaryCalendarActivity.this, i, i2, Math.round(100.0d * d), editText2.getText().toString());
                SalaryCalendarActivity salaryCalendarActivity = SalaryCalendarActivity.this;
                if (add) {
                    Toast.makeText(salaryCalendarActivity, "已补录 ¥" + SalaryCalendarActivity.this.moneyFormat.format(d), 0).show();
                    SalaryWidgetProvider.refreshAll(SalaryCalendarActivity.this);
                    SalaryCalendarActivity.this.render();
                    return;
                }
                Toast.makeText(salaryCalendarActivity, "保存失败：该月超出保留期", 1).show();
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showDayBackfillDialog() {
        final DatePicker datePicker = new DatePicker(this);
        datePicker.setMaxDate(Calendar.getInstance().getTimeInMillis());
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20.0f), dp(10.0f), dp(20.0f), dp(4.0f));
        linearLayout.addView(datePicker, matchWrap());
        new AlertDialog.Builder(this).setTitle("补记哪一天？").setView(linearLayout).setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("确定", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.SalaryCalendarActivity.8
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                int year = (datePicker.getYear() * 10000) + ((datePicker.getMonth() + 1) * 100) + datePicker.getDayOfMonth();
                Calendar cal = Calendar.getInstance();
                cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                SalaryCalendarActivity.this.showMarkDialog(year, !WorkdayHelper.isWorkday(SalaryCalendarActivity.this, MainActivity.SalarySettings.load(SalaryCalendarActivity.this.getSharedPreferences("salary_jump", 0)), cal));
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showMarkDialog(final int i, boolean z) {
        boolean has = HolidayWork.has(this, i);
        boolean has2 = LeaveRecord.has(this, i);
        ArrayList arrayList = new ArrayList();
        if (z) {
            arrayList.add(has ? "取消出勤标记" : "标记为出勤");
        } else {
            arrayList.add(has2 ? "修改请假记录" : "标记请假");
            if (has2) {
                arrayList.add("取消请假");
            }
        }
        arrayList.add(ExtraIncome.has(this, i) ? "修改外快" : "记外快");
        OvertimeLog.Punch load = OvertimeLog.load(this);
        if (load.day == i && load.exists()) {
            arrayList.add("删除加班记录");
        }
        if (MonthBackfill.centsOn(this, i) > 0) {
            arrayList.add("删除补录");
        }
        arrayList.add("删除这天的记录");
        final String[] strArr = (String[]) arrayList.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle(SalaryHistory.monthOf(i) + " 月 " + SalaryHistory.dayOf(i) + " 日").setItems(strArr, new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.SalaryCalendarActivity.9
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                long j;
                String str = strArr[i2];
                if ("删除加班记录".equals(str)) {
                    try {
                        double secondsOfDay = OvertimeLog.secondsOfDay(SalaryCalendarActivity.this, i, System.currentTimeMillis());
                        MainActivity.SalarySettings load2 = MainActivity.SalarySettings.load(SalaryCalendarActivity.this.getSharedPreferences("salary_jump", 0));
                        j = Math.round(OvertimeCalc.earned(load2.dailySalary, MainActivity.SalaryCalculator.totalWorkSeconds(load2), secondsOfDay, load2.overtimeRateX10) * 100.0d);
                    } catch (Throwable th) {
                        j = 0;
                    }
                    OvertimeLog.clear(SalaryCalendarActivity.this);
                    ArchiveSync.onOvertimeRemoved(SalaryCalendarActivity.this, i, j);
                    Toast.makeText(SalaryCalendarActivity.this, "已删除加班记录", 0).show();
                    SalaryWidgetProvider.refreshAll(SalaryCalendarActivity.this);
                    SalaryCalendarActivity.this.render();
                    return;
                }
                if ("记外快".equals(str) || "修改外快".equals(str)) {
                    ExtraIncomeDialog.show(SalaryCalendarActivity.this, i, new Runnable() { // from class: com.salaryjump.app.SalaryCalendarActivity.9.1
                        @Override // java.lang.Runnable
                        public void run() {
                            SalaryCalendarActivity.this.render();
                        }
                    });
                    return;
                }
                if ("标记请假".equals(str) || "修改请假记录".equals(str)) {
                    SalaryCalendarActivity.this.showLeaveTypePicker(i);
                    return;
                }
                if ("取消请假".equals(str)) {
                    LeaveRecord.remove(SalaryCalendarActivity.this, i);
                    Toast.makeText(SalaryCalendarActivity.this, "已取消请假", 0).show();
                } else if ("取消出勤标记".equals(str)) {
                    HolidayWork.remove(SalaryCalendarActivity.this, i);
                    Toast.makeText(SalaryCalendarActivity.this, "已取消出勤标记", 0).show();
                } else if ("标记为出勤".equals(str)) {
                    boolean isStatutory = StatutoryHolidays.isStatutory(i);
                    boolean useCompFor = WorkRules.useCompFor(SalaryCalendarActivity.this, isStatutory);
                    SalaryCalendarActivity salaryCalendarActivity = SalaryCalendarActivity.this;
                    if (useCompFor) {
                        HolidayWork.put(salaryCalendarActivity, i, 0, true);
                        Toast.makeText(SalaryCalendarActivity.this, "已标记出勤 · 调休 +1 天", 0).show();
                    } else {
                        HolidayWork.put(salaryCalendarActivity, i, WorkRules.restDayRateX10(SalaryCalendarActivity.this, isStatutory), false);
                        Toast.makeText(SalaryCalendarActivity.this, "已标记出勤", 0).show();
                    }
                } else if ("删除补录".equals(str)) {
                    MonthBackfill.removeAll(SalaryCalendarActivity.this, SalaryHistory.yearOf(i), SalaryHistory.monthOf(i));
                    Toast.makeText(SalaryCalendarActivity.this, "已删除该月补录", 0).show();
                } else if ("删除这天的记录".equals(str)) {
                    SalaryHistory.remove(SalaryCalendarActivity.this, i);
                    Toast.makeText(SalaryCalendarActivity.this, "已删除", 0).show();
                }
                SalaryWidgetProvider.refreshAll(SalaryCalendarActivity.this);
                WaterReminderScheduler.scheduleNext(SalaryCalendarActivity.this);
                SalaryCalendarActivity.this.render();
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showLeaveTypePicker(final int i) {
        String[] strArr = new String[LeaveRecord.TYPE_NAMES.length];
        for (int i2 = 0; i2 < LeaveRecord.TYPE_NAMES.length; i2++) {
            strArr[i2] = LeaveRecord.TYPE_NAMES[i2] + " · " + LeaveRecord.payText(LeaveRecord.TYPE_PAY_PERCENT[i2]);
            String remText = LeaveAccounts.remText(this, LeaveRecord.TYPE_NAMES[i2]);
            if (remText != null) {
                strArr[i2] = strArr[i2] + " · " + remText;
            }
        }
        new AlertDialog.Builder(this).setTitle(SalaryHistory.monthOf(i) + " 月 " + SalaryHistory.dayOf(i) + " 日 · 请假类型").setItems(strArr, new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.SalaryCalendarActivity.10
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i3) {
                SalaryCalendarActivity.this.showLeaveDurationPicker(i, LeaveRecord.TYPE_NAMES[i3], LeaveRecord.TYPE_PAY_PERCENT[i3]);
            }
        }).setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showLeaveDurationPicker(final int i, final String str, final int i2) {
        AlertDialog.Builder items = new AlertDialog.Builder(this).setTitle(str + " · " + LeaveRecord.payText(i2)).setItems(new String[]{"整天", "半天"}, new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.SalaryCalendarActivity.11
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i3) {
                LeaveRecord.put(SalaryCalendarActivity.this, i, str, i2, i3 == 1);
                Toast.makeText(SalaryCalendarActivity.this, "已记为" + LeaveRecord.describe(str, i2, i3 == 1) + " · " + SalaryCalendarActivity.this.moneyFormat.format(LeaveRecord.earned(MainActivity.SalarySettings.load(SalaryCalendarActivity.this.getSharedPreferences("salary_jump", 0)).dailySalary, i2, i3 == 1)) + " 元", 0).show();
                SalaryWidgetProvider.refreshAll(SalaryCalendarActivity.this);
                SalaryCalendarActivity.this.render();
            }
        });
        items.setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
    }

    private int dp(float f) {
        return Math.round(f * this.density);
    }
}
