package com.salaryjump.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.salaryjump.app.ExtraIncome;
import java.util.Calendar;
import java.util.Locale;

/* loaded from: classes.dex */
public final class ExtraIncomeDialog {
    private ExtraIncomeDialog() {
    }

    public static void show(final Activity activity, final int i, final Runnable runnable) {
        if (activity == null) {
            return;
        }
        float f = activity.getResources().getDisplayMetrics().density;
        ExtraIncome.Entry entry = ExtraIncome.get(activity, i);
        boolean z = i == todayStamp();
        String str = SalaryHistory.monthOf(i) + " 月 " + SalaryHistory.dayOf(i) + " 日";
        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(f, 22.0f), dp(f, 8.0f), dp(f, 22.0f), dp(f, 2.0f));
        linearLayout.addView(label(activity, "金额（元）", f), matchWrap());
        boolean z2 = z;
        final EditText editText = new EditText(activity);
        editText.setInputType(8194);
        editText.setTextSize(20.0f);
        editText.setTextColor(MainActivity.COLOR_TEXT);
        editText.setGravity(8388611);
        editText.setSingleLine(true);
        editText.setPadding(dp(f, 14.0f), dp(f, 12.0f), dp(f, 14.0f), dp(f, 12.0f));
        editText.setBackground(UiKit.solid(MainActivity.COLOR_BACKGROUND, dp(f, 9.0f), MainActivity.COLOR_BORDER, dp(f, 1.0f)));
        if (entry != null) {
            editText.setText(money(entry.cents));
            editText.setSelection(editText.getText().length());
        }
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(f, 7.0f);
        linearLayout.addView(editText, matchWrap);
        LinearLayout linearLayout2 = new LinearLayout(activity);
        linearLayout2.setOrientation(0);
        int[] iArr = {50, 100, 200, 500};
        for (int i2 = 0; i2 < 4; i2++) {
            final int i3 = iArr[i2];
            Button button = new Button(activity);
            button.setText("+" + i3);
            button.setTextSize(12.5f);
            button.setAllCaps(false);
            button.setMinWidth(0);
            button.setMinHeight(0);
            button.setPadding(0, 0, 0, 0);
            button.setTextColor(MainActivity.COLOR_TEXT);
            button.setBackground(MainActivity.softPill(dp(f, 18.0f), dp(f, 1.5f)));
            button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.ExtraIncomeDialog.1
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    editText.setText(ExtraIncomeDialog.money(ExtraIncomeDialog.parseCents(editText.getText().toString()) + (i3 * 100)));
                    editText.setSelection(editText.getText().length());
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(f, 36.0f), 1.0f);
            if (i2 > 0) {
                layoutParams.leftMargin = dp(f, 7.0f);
            }
            linearLayout2.addView(button, layoutParams);
        }
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(f, 9.0f);
        linearLayout.addView(linearLayout2, matchWrap2);
        linearLayout.addView(label(activity, "备注（可选，最多 12 字）", f), labelParams(f));
        final EditText editText2 = new EditText(activity);
        editText2.setInputType(1);
        editText2.setHint("如：帮朋友修图");
        editText2.setTextSize(14.0f);
        editText2.setTextColor(MainActivity.COLOR_TEXT);
        editText2.setSingleLine(true);
        editText2.setPadding(dp(f, 14.0f), dp(f, 11.0f), dp(f, 14.0f), dp(f, 11.0f));
        editText2.setBackground(UiKit.solid(MainActivity.COLOR_BACKGROUND, dp(f, 9.0f), MainActivity.COLOR_BORDER, dp(f, 1.0f)));
        if (entry != null && entry.remark != null) {
            editText2.setText(entry.remark);
        }
        LinearLayout.LayoutParams matchWrap3 = matchWrap();
        matchWrap3.topMargin = dp(f, 7.0f);
        linearLayout.addView(editText2, matchWrap3);
        AlertDialog.Builder title = new AlertDialog.Builder(activity).setTitle(entry == null ? "记一笔外快" : "修改外快");
        StringBuilder append = new StringBuilder().append(z2 ? "今天（" : "").append(str);
        AlertDialog.Builder positiveButton = title.setMessage(append.append(z2 ? "）" : "").append(" 额外赚到的钱").toString()).setView(linearLayout).setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("保存", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.ExtraIncomeDialog.2
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i4) {
                long parseCents = ExtraIncomeDialog.parseCents(editText.getText().toString());
                Activity activity2 = activity;
                if (parseCents <= 0) {
                    Toast.makeText(activity2, "请输入金额", 0).show();
                    return;
                }
                ExtraIncome.apply(activity2, i, parseCents, editText2.getText().toString());
                SalaryWidgetProvider.refreshAll(activity);
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        if (entry != null) {
            positiveButton.setNeutralButton("删除", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.ExtraIncomeDialog.3
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i4) {
                    ExtraIncome.apply(activity, i, 0L, "");
                    SalaryWidgetProvider.refreshAll(activity);
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            });
        }
        positiveButton.show();
    }

    private static int todayStamp() {
        Calendar calendar = Calendar.getInstance();
        return (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
    }

    static long parseCents(String str) {
        if (str == null) {
            return 0L;
        }
        String replace = str.trim().replace(",", "").replace("，", "");
        if (replace.length() == 0) {
            return 0L;
        }
        try {
            return Math.round(Double.parseDouble(replace) * 100.0d);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    static String money(long j) {
        return String.format(Locale.US, "%.2f", Double.valueOf(j / 100.0d));
    }

    private static TextView label(Activity activity, String str, float f) {
        TextView textView = new TextView(activity);
        textView.setText(str);
        textView.setTextSize(12.5f);
        textView.setTextColor(MainActivity.COLOR_MUTED);
        return textView;
    }

    private static LinearLayout.LayoutParams labelParams(float f) {
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(f, 16.0f);
        return matchWrap;
    }

    private static LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private static int dp(float f, float f2) {
        return Math.round(f2 * f);
    }
}
