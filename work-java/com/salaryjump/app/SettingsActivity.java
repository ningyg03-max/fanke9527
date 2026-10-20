package com.salaryjump.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.salaryjump.app.MainActivity;

/* loaded from: classes.dex */
public class SettingsActivity extends Activity {
    static final int SECTION_SALARY = 1;
    static final int SECTION_TOOLS = 3;
    static final int SECTION_WATER = 2;
    private float density;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        MainActivity.applyTheme(this);
        this.density = getResources().getDisplayMetrics().density;
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20.0f), dp(20.0f), dp(20.0f), dp(28.0f));
        scrollView.addView(linearLayout, new FrameLayout.LayoutParams(-1, -2));
        setContentView(UiKit.aurora(this, scrollView));
        MotionKit.enterChildren(linearLayout, this.density);
        buildHeader(linearLayout);
        buildCards(linearLayout);
        buildFooter(linearLayout);
    }

    private void buildHeader(LinearLayout linearLayout) {
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
        button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.SettingsActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                SettingsActivity.this.finish();
            }
        });
        linearLayout2.addView(button, new LinearLayout.LayoutParams(-2, dp(42.0f)));
        TextView textView = new TextView(this);
        textView.setText("设置");
        textView.setTextSize(20.0f);
        textView.setTextColor(MainActivity.COLOR_TEXT);
        textView.setTypeface(Typeface.create("sans", 1));
        textView.setGravity(17);
        linearLayout2.addView(textView, new LinearLayout.LayoutParams(0, dp(42.0f), 1.0f));
        Button button2 = new Button(this);
        button2.setText("返回");
        button2.setTextSize(13.0f);
        button2.setAllCaps(false);
        button2.setMinWidth(0);
        button2.setMinHeight(0);
        button2.setPadding(dp(12.0f), 0, dp(12.0f), 0);
        button2.setVisibility(4);
        linearLayout2.addView(button2, new LinearLayout.LayoutParams(-2, dp(42.0f)));
        TextView textView2 = new TextView(this);
        textView2.setText("设置按功能分为三组，点进去修改后「保存」即生效。");
        textView2.setTextSize(12.0f);
        textView2.setTextColor(MainActivity.COLOR_MUTED);
        textView2.setLineSpacing(dp(2.0f), 1.05f);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(12.0f);
        linearLayout.addView(textView2, matchWrap);
    }

    private void buildCards(LinearLayout linearLayout) {
        linearLayout.addView(card("💼", "薪跳设置", "工资 · 月薪 · 时间 · 加班 · 发薪日", new int[]{MainActivity.COLOR_ACCENT_BRIGHT}, 1), cardParams(14.0f));
        linearLayout.addView(card("💧", "喝水设置", "提醒开关 · 间隔 · 饮品 · 热量 · 参考表", new int[]{MainActivity.COLOR_BLUE_BRIGHT}, 2), cardParams(12.0f));
        linearLayout.addView(card("🔧", "工具与安全", "节假日 · 备份 · 诊断 · 隐私锁 · 战报 · 说明", new int[]{MainActivity.COLOR_ACCENT_BRIGHT}, SECTION_TOOLS), cardParams(12.0f));
    }

    private LinearLayout card(String str, String str2, String str3, int[] iArr, final int i) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        linearLayout.setPadding(dp(18.0f), dp(16.0f), dp(16.0f), dp(16.0f));
        boolean z = (getResources().getConfiguration().uiMode & 48) == 32;
        linearLayout.setBackground(UiKit.glowCard(MainActivity.COLOR_SURFACE, iArr[0], z ? 43 : 46, z ? 28 : 43, dp(22.0f)));
        linearLayout.setElevation(dp(4.0f));
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(24.0f);
        linearLayout.addView(textView, new LinearLayout.LayoutParams(-2, -2));
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
        layoutParams.leftMargin = dp(14.0f);
        linearLayout.addView(linearLayout2, layoutParams);
        TextView textView2 = new TextView(this);
        textView2.setText(str2);
        textView2.setTextSize(16.0f);
        textView2.setTextColor(MainActivity.COLOR_TEXT);
        textView2.setTypeface(Typeface.create("sans", 1));
        linearLayout2.addView(textView2, matchWrap());
        TextView textView3 = new TextView(this);
        textView3.setText(str3);
        textView3.setTextSize(11.5f);
        textView3.setTextColor(MainActivity.COLOR_MUTED);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(4.0f);
        linearLayout2.addView(textView3, matchWrap);
        TextView textView4 = new TextView(this);
        textView4.setText("›");
        textView4.setTextSize(22.0f);
        textView4.setTextColor(MainActivity.COLOR_LABEL);
        linearLayout.addView(textView4, new LinearLayout.LayoutParams(-2, -2));
        linearLayout.setClickable(true);
        linearLayout.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.SettingsActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                SettingsActivity.this.openSection(i);
            }
        });
        return linearLayout;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void openSection(int i) {
        try {
            MainActivity.SettingsDialog settingsDialog = new MainActivity.SettingsDialog(this, MainActivity.SalarySettings.load(getSharedPreferences("salary_jump", 0)), new MainActivity.SettingsDialog.Listener() { // from class: com.salaryjump.app.SettingsActivity.3
                @Override // com.salaryjump.app.MainActivity.SettingsDialog.Listener
                public void onSaved(MainActivity.SalarySettings salarySettings) {
                    SettingsActivity.this.onSettingsSaved(salarySettings);
                }
            }, i);
            settingsDialog.show();
            Window window = settingsDialog.getWindow();
            if (window != null) {
                window.setLayout(-1, -1);
                try {
                    window.setBackgroundDrawable(new ColorDrawable(0));
                    window.clearFlags(2);
                    window.setGravity(48);
                } catch (Throwable th) {
                }
            }
        } catch (Throwable th2) {
            Toast.makeText(this, "打开失败：" + th2.getMessage(), 0).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onSettingsSaved(MainActivity.SalarySettings salarySettings) {
        try {
            salarySettings.save(getSharedPreferences("salary_jump", 0));
            SalaryWidgetProvider.refreshAll(this);
            WaterReminderScheduler.scheduleNext(this);
            WorkdayHelper.invalidate();
        } catch (Throwable th) {
        }
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService("alarm");
            if (Build.VERSION.SDK_INT >= 31 && WaterReminderSettings.load(this).enabled && alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent("android.settings.REQUEST_SCHEDULE_EXACT_ALARM");
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } catch (Throwable th2) {
        }
        Toast.makeText(this, "设置已保存", 0).show();
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i2 != -1 || intent == null || intent.getData() == null) {
            return;
        }
        if (i == 4302) {
            BackupTool.export(this, intent.getData());
        } else if (i == 4303) {
            BackupTool.restore(this, intent.getData());
        } else if (i == MainActivity.REQ_BACKUP_TREE) {
            onBackupTreePicked(intent.getData());
        }
    }

    /** 用户选好自动备份的文件夹：接管授权，然后重开设置页让用户看到已生效。 */
    private void onBackupTreePicked(Uri uri) {
        String label = BackupScheduler.adoptTree(this, uri);
        if (label == null) {
            Toast.makeText(this, "无法使用该文件夹，请换一个试试", 1).show();
            return;
        }
        Toast.makeText(this, "自动备份已开启\n目录：" + label, 1).show();
        // 自动备份的开关都在「工具与安全」分区里，重开一次让用户看到结果
        openSection(MainActivity.STATE_LUNCH);
    }

    private void buildFooter(LinearLayout linearLayout) {
        TextView textView = new TextView(this);
        textView.setText("SalaryJump " + MainActivity.versionName(this) + "\n修改规则只影响之后的记录，历史记录保留当时的计算方式。");
        textView.setTextSize(11.5f);
        textView.setTextColor(MainActivity.COLOR_LABEL);
        textView.setLineSpacing(dp(2.0f), 1.1f);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(20.0f);
        linearLayout.addView(textView, matchWrap);
    }

    private LinearLayout.LayoutParams cardParams(float f) {
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(f);
        return matchWrap;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private int dp(float f) {
        return Math.round(f * this.density);
    }
}
