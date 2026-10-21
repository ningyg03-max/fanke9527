package com.salaryjump.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.salaryjump.app.DrinkTypes;
import com.salaryjump.app.HolidayUpdater;
import com.salaryjump.app.LeaveRecord;
import com.salaryjump.app.OvertimeLog;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONObject;

/* loaded from: classes.dex */
public class MainActivity extends Activity {
    static final int HERO_LINE = 956301311;
    private static final int MATCH = -1;
    private static final int REQ_BACKUP_EXPORT = 4302;
    private static final int REQ_BACKUP_IMPORT = 4303;
    /** 选自动备份的文件夹（SettingsActivity 也要用，所以是包级可见） */
    static final int REQ_BACKUP_TREE = 4304;
    static final int STATE_AFTERNOON = 4;
    static final int STATE_AFTER_WORK = 5;
    static final int STATE_BEFORE_WORK = 1;
    static final int STATE_HOLIDAY_WORK = 7;
    static final int STATE_LEAVE = 8;
    static final int STATE_LUNCH = 3;
    static final int STATE_MORNING = 2;
    static final int STATE_OVERTIME = 6;
    static final int STATE_REST_DAY = 0;
    private TextView amountView;
    private CancellationSignal bioCancel;
    private TextView[] caps;
    private LinearLayout capsRow;
    private TextView cumulativeDaysView;
    private TextView cumulativeSubView;
    private TextView cumulativeView;
    private float density;
    private AlertDialog drinkDialog;
    private TextView extraActionView;
    private LinearLayout extraRow;
    private TextView extraStatusView;
    private CancellationSignal fpCancel;
    private TextView goalDateView;
    /** 开始日期 + 状态（如「开始日期 2026 年 9 月 1 日 · 进行中」） */
    private TextView goalSubView;
    /** 「确认完成」按钮（仅在已达标时显示） */
    private Button goalConfirmButton;
    /** 当前展示的目标（点确认时要用） */
    private GoalStore.Goal currentGoal;
    private MilestoneBar goalMilestoneBar;
    private TextView goalMilestoneTag;
    private TextView goalPercentView;
    private TextView goalRemainView;
    private TextView goalSavedView;
    private TextView goalView;
    private Handler handler;
    private TextView holidayActionView;
    private LinearLayout holidayRow;
    private TextView hourlyView;
    /** 费率栏的标签（动态显示「每小时」/「加班 1.5 倍」/「调休 · 不计薪」）*/
    private TextView hourlyLabelView;
    /** addStat 的副产物：刚创建的标签控件 */
    private TextView lastStatLabel;
    long lastNextWaterAt;
    CharSequence lastNextWaterText;
    private long lastWaterUiUpdate;
    private TextView leaveActionView;
    private LinearLayout leaveRow;
    private TextView leaveStatusView;
    private LinearLayout lockDots;
    private TextView lockHintView;
    private View lockOverlay;
    private TextView minuteView;
    private TextView overtimeActionView;
    private LinearLayout overtimeRow;
    private TextView overtimeStatusView;
    private TextView petMoodView;
    /** 小人 + 心情文案所在的竖列（隐藏小人时整列收起） */
    private LinearLayout petColumn;
    /** 疲劳提示行（≥2 级才显示） */
    private TextView petFatigueView;
    /** 金额 + 小人这一行（宽度由两者共享） */
    private LinearLayout heroAmountRow;
    /** 上次给金额设的字号（px），避免每秒重复 requestLayout */
    private float amountTextSizePx;
    private SharedPreferences preferences;
    private SheenBar progressBar;
    private LinearLayout root;
    /** 工作节奏（智能·08:00-17:00·午休…），现在和「今日已赚」同一行 */
    private TextView scheduleView;
    private TextView secondView;
    private SalarySettings settings;
    private TextView settingsDialogStatusView;
    private TextView statusView;
    private TextView targetView;
    private Runnable ticker;
    private TextView waterAmountView;
    private Button waterDrinkButton;
    private TextView waterNextView;
    private SheenBar waterProgressBar;
    private TextView waterTargetView;
    private WorkerPetView workerPetView;
    private static boolean appUnlocked = false;
    private static int visibleActivities = 0;
    private static boolean lifecycleHooked = false;
    static int COLOR_BACKGROUND = Color.rgb(239, 247, 246);
    static int COLOR_SURFACE = Color.rgb(255, 255, 255);
    static int COLOR_TEXT = Color.rgb(22, 50, 59);
    static int COLOR_MUTED = Color.rgb(97, 128, 140);
    static int COLOR_LABEL = Color.rgb(138, 166, 176);
    static int COLOR_BORDER = Color.rgb(227, 239, 236);
    static int COLOR_TRACK = Color.rgb(230, 242, 238);
    static int COLOR_ACCENT = Color.rgb(11, 158, 125);
    static int COLOR_ACCENT_BRIGHT = Color.rgb(16, 185, 129);
    static int COLOR_BLUE = Color.rgb(15, 118, 110);
    static int COLOR_BLUE_BRIGHT = Color.rgb(20, 184, 166);
    static int COLOR_AMBER = Color.rgb(180, 83, 9);
    static int COLOR_ON_ACCENT = Color.rgb(255, 255, 255);
    static int COLOR_ON_BLUE = Color.rgb(255, 255, 255);
    static int COLOR_DANGER = Color.rgb(190, 18, 60);
    static int COLOR_SOFT_BG = Color.rgb(247, 252, 250);
    static int COLOR_SOFT_BORDER = Color.rgb(211, 233, 227);
    static int COLOR_SOFT_TEXT = Color.rgb(46, 90, 85);
    /** 外快专用色：真蓝（原来的 COLOR_BLUE 是青绿，深色下和日薪分不出来） */
    static int COLOR_EXTRA = Color.rgb(37, 99, 235);
    static int COLOR_CHIP_BG = Color.rgb(255, 247, 234);
    static int COLOR_HERO_SHADOW = Color.rgb(16, 185, 129);
    static int[] HERO_COLORS = {Color.rgb(14, 164, 122), Color.rgb(16, 185, 129), Color.rgb(34, 198, 180), Color.rgb(45, 212, 191)};
    static int[] BG_COLORS = {Color.rgb(236, 251, 244), Color.rgb(238, 246, 254), Color.rgb(241, 246, 255)};
    static int[] LOGO_COLORS = {Color.rgb(16, 185, 129), Color.rgb(20, 184, 166)};
    static int[] PILL_COLORS = {Color.rgb(16, 185, 129), Color.rgb(13, 148, 136)};
    static int[] PILL_BLUE_COLORS = {Color.rgb(13, 148, 136), Color.rgb(20, 184, 166)};
    static int COLOR_CHIP_GREEN_BG = Color.rgb(236, 253, 245);
    static int COLOR_BLUE_SHADOW = Color.rgb(20, 184, 166);
    static int HERO_TEXT = Color.rgb(255, 255, 255);
    static int HERO_SUB = -939524097;
    static int HERO_SUB2 = -1191182337;
    static int HERO_TRACK = 1124073471;
    static int HERO_CHIP = 872415231;
    private static ColorStateList CSL_ACCENT = ColorStateList.valueOf(COLOR_ACCENT);
    private static ColorStateList CSL_ACCENT_BRIGHT = ColorStateList.valueOf(COLOR_ACCENT_BRIGHT);
    private static ColorStateList CSL_MUTED = ColorStateList.valueOf(COLOR_MUTED);
    private static ColorStateList CSL_BLUE = ColorStateList.valueOf(COLOR_BLUE);
    private static ColorStateList CSL_BLUE_BRIGHT = ColorStateList.valueOf(COLOR_BLUE_BRIGHT);
    private static ColorStateList CSL_AMBER = ColorStateList.valueOf(COLOR_AMBER);
    static boolean NIGHT = true;
    private String lastCapLayout = "";
    private String lockInput = "";
    private int lockFails = 0;
    private String lastBiometricError = "";
    private boolean bioFallbackOnly = false;
    private boolean autoPrompted = false;
    private boolean amountIntro = true;
    private boolean cumIntro = true;
    private boolean cumHidden = true;
    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
    private final DecimalFormat rateFormat = new DecimalFormat("#,##0.00");
    private final DecimalFormat secondFormat = new DecimalFormat("#,##0.000");

    static /* synthetic */ int access$308() {
        int i = visibleActivities;
        visibleActivities = i + 1;
        return i;
    }

    static /* synthetic */ int access$310() {
        int i = visibleActivities;
        visibleActivities = i + MATCH;
        return i;
    }

    void setHolidayStatus(final String str, final boolean z) {
        final TextView textView = this.settingsDialogStatusView;
        if (textView == null) {
            return;
        }
        try {
            runOnUiThread(new Runnable() { // from class: com.salaryjump.app.MainActivity.1
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        textView.setText(str);
                        textView.setTextColor(z ? MainActivity.COLOR_ACCENT : MainActivity.COLOR_DANGER);
                    } catch (Throwable th) {
                    }
                }
            });
        } catch (Throwable th) {
        }
    }

    static String versionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Throwable th) {
            return "";
        }
    }

    static String lastHolidayUpdateText(Context context) {
        try {
            String lastStageText = HolidayUpdater.lastStageText(context);
            long j = context.getSharedPreferences("salary_jump", 0).getLong("holiday_updated_at", 0L);
            if (lastStageText != null) {
                if (!HolidayUpdater.lastStageSucceeded(context)) {
                    return "⚠️ 上次尝试停在：" + lastStageText;
                }
                if (j > 0) {
                    return "上次更新：" + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(j));
                }
            }
            if (j > 0) {
                return "上次更新：" + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(j));
            }
            return "尚未更新过节假日数据（内置 2025-2026）";
        } catch (Throwable th) {
            return "";
        }
    }

    static boolean lastHolidayUpdateIncomplete(Context context) {
        try {
            if (HolidayUpdater.lastStageText(context) == null) {
                return false;
            }
            return !HolidayUpdater.lastStageSucceeded(context);
        } catch (Throwable th) {
            return false;
        }
    }

    static String earnSuffix(Context context) {
        try {
            SalarySnapshot calculate = SalaryCalculator.calculate(context, SalarySettings.load(context.getSharedPreferences("salary_jump", 0)), Calendar.getInstance());
            if (calculate.earned <= 0.0d) {
                return "";
            }
            return " · 今日已赚 ¥" + new DecimalFormat("#,##0.00").format(calculate.earned);
        } catch (Throwable th) {
            return "";
        }
    }

    static void applyTheme(Context context) {
        boolean z = (context.getResources().getConfiguration().uiMode & 48) == 32;
        NIGHT = z;
        if (z) {
            HERO_TEXT = Color.rgb(255, 255, 255);
            HERO_SUB = -939524097;
            HERO_SUB2 = -1191182337;
            HERO_TRACK = 1124073471;
            HERO_CHIP = 872415231;
            COLOR_BACKGROUND = Color.rgb(13, 28, 32);
            COLOR_SURFACE = Color.rgb(16, 33, 36);
            COLOR_TEXT = Color.rgb(232, 244, 242);
            COLOR_MUTED = Color.rgb(159, 184, 190);
            COLOR_LABEL = Color.rgb(126, 154, 163);
            COLOR_BORDER = Color.rgb(31, 58, 56);
            COLOR_TRACK = Color.rgb(30, 51, 54);
            COLOR_ACCENT = Color.rgb(52, 211, 153);
            COLOR_ACCENT_BRIGHT = Color.rgb(52, 211, 153);
            COLOR_BLUE = Color.rgb(45, 212, 191);
            COLOR_BLUE_BRIGHT = Color.rgb(45, 212, 191);
            COLOR_AMBER = Color.rgb(252, 211, 77);
            COLOR_ON_ACCENT = Color.rgb(4, 33, 26);
            COLOR_ON_BLUE = Color.rgb(5, 46, 63);
            COLOR_DANGER = Color.rgb(251, 113, 133);
            COLOR_SOFT_BG = Color.rgb(18, 36, 42);
            COLOR_SOFT_BORDER = Color.rgb(31, 62, 61);
            COLOR_SOFT_TEXT = Color.rgb(191, 227, 220);
            COLOR_EXTRA = Color.rgb(96, 165, 250);
            COLOR_CHIP_BG = Color.rgb(42, 36, 21);
            COLOR_CHIP_GREEN_BG = Color.rgb(18, 46, 38);
            COLOR_BLUE_SHADOW = Color.rgb(0, 0, 0);
            PILL_BLUE_COLORS = new int[]{Color.rgb(45, 212, 191), Color.rgb(20, 184, 166)};
            COLOR_HERO_SHADOW = Color.rgb(0, 0, 0);
            HERO_COLORS = new int[]{Color.rgb(STATE_OVERTIME, 95, 70), Color.rgb(11, 138, 106), Color.rgb(14, 156, 140), Color.rgb(20, 184, 166)};
            BG_COLORS = new int[]{Color.rgb(STATE_HOLIDAY_WORK, 17, 15), Color.rgb(8, 21, 24), Color.rgb(STATE_HOLIDAY_WORK, 16, 25)};
            LOGO_COLORS = new int[]{Color.rgb(16, 185, 129), Color.rgb(45, 212, 191)};
            PILL_COLORS = new int[]{Color.rgb(52, 211, 153), Color.rgb(45, 212, 191)};
        } else {
            HERO_TEXT = Color.rgb(10, 59, 49);
            HERO_SUB = -1391838415;
            HERO_SUB2 = -1945486543;
            HERO_TRACK = 386546481;
            HERO_CHIP = 252328753;
            COLOR_BACKGROUND = Color.rgb(239, 247, 246);
            COLOR_SURFACE = Color.rgb(255, 255, 255);
            COLOR_TEXT = Color.rgb(22, 50, 59);
            COLOR_MUTED = Color.rgb(97, 128, 140);
            COLOR_LABEL = Color.rgb(138, 166, 176);
            COLOR_BORDER = Color.rgb(227, 239, 236);
            COLOR_TRACK = Color.rgb(230, 242, 238);
            COLOR_ACCENT = Color.rgb(11, 158, 125);
            COLOR_ACCENT_BRIGHT = Color.rgb(16, 185, 129);
            COLOR_BLUE = Color.rgb(15, 118, 110);
            COLOR_BLUE_BRIGHT = Color.rgb(20, 184, 166);
            COLOR_AMBER = Color.rgb(180, 83, 9);
            COLOR_EXTRA = Color.rgb(37, 99, 235);
            COLOR_ON_ACCENT = Color.rgb(255, 255, 255);
            COLOR_ON_BLUE = Color.rgb(255, 255, 255);
            COLOR_DANGER = Color.rgb(190, 18, 60);
            COLOR_SOFT_BG = Color.rgb(247, 252, 250);
            COLOR_SOFT_BORDER = Color.rgb(211, 233, 227);
            COLOR_SOFT_TEXT = Color.rgb(46, 90, 85);
            COLOR_CHIP_BG = Color.rgb(255, 247, 234);
            COLOR_CHIP_GREEN_BG = Color.rgb(236, 253, 245);
            COLOR_BLUE_SHADOW = Color.rgb(20, 184, 166);
            PILL_BLUE_COLORS = new int[]{Color.rgb(13, 148, 136), Color.rgb(20, 184, 166)};
            COLOR_HERO_SHADOW = Color.rgb(16, 185, 129);
            HERO_COLORS = new int[]{Color.rgb(14, 164, 122), Color.rgb(16, 185, 129), Color.rgb(34, 198, 180), Color.rgb(45, 212, 191)};
            BG_COLORS = new int[]{Color.rgb(234, 247, 242), Color.rgb(232, 241, 247), Color.rgb(237, 239, 249)};
            LOGO_COLORS = new int[]{Color.rgb(16, 185, 129), Color.rgb(20, 184, 166)};
            PILL_COLORS = new int[]{Color.rgb(16, 185, 129), Color.rgb(13, 148, 136)};
        }
        CSL_ACCENT = ColorStateList.valueOf(COLOR_ACCENT);
        CSL_ACCENT_BRIGHT = ColorStateList.valueOf(COLOR_ACCENT_BRIGHT);
        CSL_MUTED = ColorStateList.valueOf(COLOR_MUTED);
        CSL_BLUE = ColorStateList.valueOf(COLOR_BLUE);
        CSL_BLUE_BRIGHT = ColorStateList.valueOf(COLOR_BLUE_BRIGHT);
        CSL_AMBER = ColorStateList.valueOf(COLOR_AMBER);
        if (context instanceof Activity) {
            applyEdgeToEdge(((Activity) context).getWindow());
        }
    }

    static Drawable bgGradient() {
        return UiKit.vertical(BG_COLORS);
    }

    private TextView label(String str) {
        TextView text = text(str, 11.5f, COLOR_LABEL, 1);
        text.setLetterSpacing(0.12f);
        return text;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void buildCumulativeCard() {
        LinearLayout card = card();
        MotionKit.enter(card, this.density, STATE_LUNCH);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(14.0f);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        card.addView(linearLayout, matchWrap());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(14.0f), dp(14.0f));
        layoutParams.rightMargin = dp(4.0f);
        View dot = dot(COLOR_ACCENT_BRIGHT);
        Drawable breathingDot = MotionKit.breathingDot(dp(6.0f), COLOR_ACCENT_BRIGHT);
        dot.setBackground(breathingDot);
        if (breathingDot instanceof Animatable) {
            ((Animatable) breathingDot).start();
        }
        linearLayout.addView(dot, layoutParams);
        linearLayout.addView(label("累计收入（本月）"), new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView text = text("明细 ›", 12.5f, COLOR_ACCENT, 0);
        text.setGravity(21);
        text.setPadding(dp(10.0f), 0, dp(2.0f), 0);
        text.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, (Class<?>) SalaryCalendarActivity.class));
            }
        });
        linearLayout.addView(text, new LinearLayout.LayoutParams(-2, -2));
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(10.0f);
        card.addView(linearLayout2, matchWrap2);
        this.cumulativeView = text("", 30.0f, COLOR_ACCENT, 1);
        this.cumulativeView.setIncludeFontPadding(false);
        this.cumulativeView.setClickable(true);
        this.cumulativeView.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.toggleCumulativeMask();
            }
        });
        linearLayout2.addView(this.cumulativeView, new LinearLayout.LayoutParams(-2, -2));
        this.cumulativeDaysView = text("", 11.5f, COLOR_ACCENT, 1);
        this.cumulativeDaysView.setBackground(UiKit.solid(COLOR_CHIP_GREEN_BG, dp(12.0f)));
        this.cumulativeDaysView.setPadding(dp(10.0f), dp(5.0f), dp(10.0f), dp(5.0f));
        LinearLayout.LayoutParams wrapWrap = wrapWrap();
        wrapWrap.leftMargin = dp(9.0f);
        linearLayout2.addView(this.cumulativeDaysView, wrapWrap);
        this.cumulativeSubView = text("", 12.5f, COLOR_MUTED, 0);
        LinearLayout.LayoutParams matchWrap3 = matchWrap();
        matchWrap3.topMargin = dp(8.0f);
        card.addView(this.cumulativeSubView, matchWrap3);
        buildGoalSection(card);
        this.root.addView(card, matchWrap);
    }

    private void buildGoalSection(LinearLayout linearLayout) {
        View view = new View(this);
        view.setBackgroundColor(COLOR_BORDER);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH, dp(1.0f));
        layoutParams.topMargin = dp(15.0f);
        linearLayout.addView(view, layoutParams);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(13.0f);
        linearLayout.addView(linearLayout2, matchWrap);
        this.goalView = text("", 13.5f, COLOR_TEXT, 1);
        linearLayout2.addView(this.goalView, new LinearLayout.LayoutParams(0, dp(28.0f), 1.0f));
        this.goalPercentView = text("", 21.0f, COLOR_ACCENT, 1);
        this.goalPercentView.setGravity(21);
        this.goalPercentView.setIncludeFontPadding(false);
        linearLayout2.addView(this.goalPercentView, new LinearLayout.LayoutParams(-2, dp(28.0f)));
        TextView text = text("设置", 12.5f, COLOR_ACCENT, 0);
        text.setGravity(21);
        text.setPadding(dp(10.0f), 0, dp(2.0f), 0);
        text.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                MainActivity.this.showGoalDialog();
            }
        });
        linearLayout2.addView(text, new LinearLayout.LayoutParams(-2, dp(28.0f)));
        this.goalSubView = text("", 10.5f, COLOR_LABEL, 0);
        LinearLayout.LayoutParams goalSubParams = matchWrap();
        goalSubParams.topMargin = dp(2.0f);
        linearLayout.addView(this.goalSubView, goalSubParams);
        this.goalMilestoneBar = new MilestoneBar(this, this.density);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(MATCH, -2);
        layoutParams2.topMargin = dp(8.0f);
        linearLayout.addView(this.goalMilestoneBar, layoutParams2);
        this.goalMilestoneTag = text("", 11.5f, COLOR_AMBER, 1);
        this.goalMilestoneTag.setPadding(dp(11.0f), dp(6.0f), dp(11.0f), dp(6.0f));
        this.goalMilestoneTag.setBackground(UiKit.solid(COLOR_CHIP_BG, dp(10.0f)));
        LinearLayout.LayoutParams wrapWrap = wrapWrap();
        wrapWrap.topMargin = dp(8.0f);
        linearLayout.addView(this.goalMilestoneTag, wrapWrap);
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(0);
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(12.0f);
        linearLayout.addView(linearLayout3, matchWrap2);
        this.goalConfirmButton = new Button(this);
        this.goalConfirmButton.setText("✅ 已买到，确认完成");
        this.goalConfirmButton.setTextSize(13.0f);
        this.goalConfirmButton.setAllCaps(false);
        this.goalConfirmButton.setTextColor(COLOR_ON_ACCENT);
        this.goalConfirmButton.setBackground(UiKit.hero(HERO_COLORS, dp(12.0f)));
        this.goalConfirmButton.setVisibility(8);
        this.goalConfirmButton.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.42
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                MainActivity.this.confirmGoalDone();
            }
        });
        LinearLayout.LayoutParams confirmParams = matchWrap();
        confirmParams.topMargin = dp(10.0f);
        linearLayout.addView(this.goalConfirmButton, new LinearLayout.LayoutParams(MATCH, dp(44.0f)));
        this.goalSavedView = addGoalCol(linearLayout3, "已攒", 0);
        this.goalRemainView = addGoalCol(linearLayout3, "还差", 1);
        this.goalDateView = addGoalCol(linearLayout3, "预计完成", 2);
    }

    private TextView addGoalCol(LinearLayout linearLayout, String str, int i) {
        int i2;
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        linearLayout.addView(linearLayout2, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView text = text(str, 10.5f, COLOR_LABEL, 0);
        int i3 = 5;
        if (i == 0) {
            i2 = STATE_LUNCH;
        } else if (i == 1) {
            i2 = 1;
        } else {
            i2 = 5;
        }
        text.setGravity(i2);
        linearLayout2.addView(text, matchWrap());
        TextView text2 = text("", 14.0f, COLOR_TEXT, 1);
        if (i == 0) {
            i3 = STATE_LUNCH;
        } else if (i == 1) {
            i3 = 1;
        }
        text2.setGravity(i3);
        text2.setSingleLine(true);
        text2.setAutoSizeTextTypeUniformWithConfiguration(10, 14, 1, 2);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(4.0f);
        linearLayout2.addView(text2, matchWrap);
        return text2;
    }

    static class MilestoneBar extends View {
        private final float density;
        private final Paint dotPaint;
        private final Paint fillPaint;
        private final Paint labelPaint;
        private int nextIndex;
        private float progress;
        private final Paint trackPaint;

        MilestoneBar(Context context, float f) {
            super(context);
            this.trackPaint = new Paint(1);
            this.fillPaint = new Paint(1);
            this.dotPaint = new Paint(1);
            this.labelPaint = new Paint(1);
            this.progress = 0.0f;
            this.nextIndex = MainActivity.MATCH;
            this.density = f;
            this.labelPaint.setColor(MainActivity.COLOR_LABEL);
            this.labelPaint.setTextSize(dpf(9.5f));
            this.labelPaint.setTextAlign(Paint.Align.CENTER);
            this.fillPaint.setColor(MainActivity.COLOR_ACCENT_BRIGHT);
            this.trackPaint.setColor(MainActivity.COLOR_TRACK);
        }

        private float dpf(float f) {
            return f * this.density;
        }

        void setData(float f, int i) {
            this.progress = Math.max(0.0f, Math.min(1.0f, f));
            this.nextIndex = i;
            invalidate();
        }

        @Override // android.view.View
        protected void onMeasure(int i, int i2) {
            setMeasuredDimension(View.MeasureSpec.getSize(i), Math.round(dpf(40.0f)));
        }

        @Override // android.view.View
        protected void onDraw(Canvas canvas) {
            float dpf = dpf(16.0f);
            float width = getWidth() - dpf;
            float max = Math.max(1.0f, width - dpf);
            float dpf2 = dpf(13.0f);
            float dpf3 = dpf(1.5f);
            float dpf4 = dpf(6.0f);
            float f = dpf2 - dpf3;
            float f2 = dpf2 + dpf3;
            canvas.drawRoundRect(dpf, f, width, f2, dpf3, dpf3, this.trackPaint);
            float f3 = dpf + (this.progress * max);
            if (f3 > dpf) {
                canvas.drawRoundRect(dpf, f, f3, f2, dpf3, dpf3, this.fillPaint);
            }
            int i = 0;
            while (i <= 4) {
                float f4 = i;
                float f5 = ((max * f4) / 4.0f) + dpf;
                boolean z = this.progress >= (f4 / 4.0f) - 0.001f;
                boolean z2 = !z && i == this.nextIndex;
                Paint paint = this.dotPaint;
                if (z) {
                    paint.setStyle(Paint.Style.FILL);
                    this.dotPaint.setColor(MainActivity.COLOR_ACCENT_BRIGHT);
                    canvas.drawCircle(f5, dpf2, dpf4, this.dotPaint);
                } else {
                    paint.setStyle(Paint.Style.FILL);
                    this.dotPaint.setColor(MainActivity.COLOR_SURFACE);
                    canvas.drawCircle(f5, dpf2, dpf4, this.dotPaint);
                    this.dotPaint.setStyle(Paint.Style.STROKE);
                    this.dotPaint.setStrokeWidth(dpf(z2 ? 2.5f : 2.0f));
                    this.dotPaint.setColor(z2 ? MainActivity.COLOR_AMBER : MainActivity.COLOR_BORDER);
                    canvas.drawCircle(f5, dpf2, dpf4, this.dotPaint);
                }
                canvas.drawText((i * 25) + "%", f5, dpf(34.0f), this.labelPaint);
                i++;
            }
        }
    }

    public void showGoalDialog() {
        SharedPreferences sharedPreferences = getSharedPreferences("salary_jump", 0);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(24.0f), dp(10.0f), dp(24.0f), dp(4.0f));
        final EditText editText = new EditText(this);
        editText.setHint("目标名称（如 MacBook Pro）");
        GoalStore.Goal editing = GoalStore.current(this);
        editText.setText(editing == null ? "" : editing.name);
        editText.setTextSize(15.0f);
        editText.setTextColor(COLOR_TEXT);
        editText.setSingleLine(true);
        editText.setPadding(dp(12.0f), dp(10.0f), dp(12.0f), dp(10.0f));
        editText.setBackground(rounded(COLOR_BACKGROUND, 9, COLOR_BORDER, 1));
        linearLayout.addView(editText, matchWrap());
        final EditText editText2 = new EditText(this);
        editText2.setHint("目标金额（元）");
        editText2.setInputType(8194);
        double longBitsToDouble = editing == null ? 0.0d : editing.targetCents / 100.0d;
        if (longBitsToDouble > 0.0d) {
            editText2.setText(String.valueOf((long) longBitsToDouble));
        }
        editText2.setTextSize(15.0f);
        editText2.setTextColor(COLOR_TEXT);
        editText2.setSingleLine(true);
        editText2.setPadding(dp(12.0f), dp(10.0f), dp(12.0f), dp(10.0f));
        editText2.setBackground(rounded(COLOR_BACKGROUND, 9, COLOR_BORDER, 1));
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(12.0f);
        linearLayout.addView(editText2, matchWrap);
        // 新建目标时，起算日默认接在上一个完成日之后（避免两段进度重叠）
        int i = editing != null ? editing.startStamp : GoalStore.suggestStartStamp(this);
        if (i <= 0) {
            i = GoalStore.todayStamp();
        }
        final int[] iArr = {i};
        final boolean editingExisting = editing != null;
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(12.0f);
        linearLayout.addView(linearLayout2, matchWrap2);
        linearLayout2.addView(text("进度起算", 13.5f, COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, -2, 1.0f));
        final Button button = new Button(this);
        button.setTextSize(13.5f);
        button.setTextColor(COLOR_TEXT);
        button.setAllCaps(false);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setPadding(dp(12.0f), 0, dp(12.0f), 0);
        button.setBackground(rounded(COLOR_BACKGROUND, 9, COLOR_BORDER, 1));
        button.setText((i / 10000) + " 年 " + ((i / 100) % 100) + " 月 " + (i % 100) + " 日");
        button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.5
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Calendar calendar2 = Calendar.getInstance();
                int i2 = iArr[0];
                calendar2.set(i2 / 10000, ((i2 / 100) % 100) - 1, i2 % 100);
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() { // from class: com.salaryjump.app.MainActivity.5.1
                    @Override // android.app.DatePickerDialog.OnDateSetListener
                    public void onDateSet(DatePicker datePicker, int i3, int i4, int i5) {
                        int i6 = i4 + 1;
                        iArr[0] = (i3 * 10000) + (i6 * 100) + i5;
                        button.setText(i3 + " 年 " + i6 + " 月 " + i5 + " 日");
                    }
                }, calendar2.get(1), calendar2.get(2), calendar2.get(5));
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });
        linearLayout2.addView(button, wrapWrap());
        TextView text = text(editingExisting
                ? "只统计起算日之后的收入；补录的历史月份不计入进度"
                : "新目标的起算日默认接在上一个目标完成之后，两段进度不会重复计算", 10.5f, COLOR_LABEL, 0);
        text.setLineSpacing(dp(2.0f), 1.1f);
        LinearLayout.LayoutParams matchWrap3 = matchWrap();
        matchWrap3.topMargin = dp(6.0f);
        linearLayout.addView(text, matchWrap3);
        AlertDialog.Builder view = new AlertDialog.Builder(this)
                .setTitle(editingExisting ? "编辑当前目标" : "设一个新目标")
                .setView(linearLayout);
        view.setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("保存", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.6
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                double d;
                try {
                    d = Double.parseDouble(editText2.getText().toString().trim().replace(",", ""));
                } catch (NumberFormatException e) {
                    d = 0.0d;
                }
                MainActivity main = MainActivity.this;
                String name = GoalStore.cleanName(editText.getText().toString());
                if (d <= 0.0d) {
                    // 金额为 0 → 清除当前目标（保留已完成的历史）
                    GoalStore.Goal cur = GoalStore.current(main);
                    if (cur != null) {
                        java.util.List<GoalStore.Goal> list = GoalStore.load(main);
                        for (int k = list.size() - 1; k >= 0; k--) {
                            if (GoalStore.sameGoal(list.get(k), cur)) {
                                list.remove(k);
                            }
                        }
                        GoalStore.save(main, list);
                    }
                    Toast.makeText(main, "目标已清除", 0).show();
                } else if (name.length() == 0) {
                    Toast.makeText(main, "请填写目标名称", 0).show();
                } else if (editingExisting) {
                    // 编辑当前目标：改名称/金额；开始日允许改（用户明确选了才改）
                    java.util.List<GoalStore.Goal> list = GoalStore.load(main);
                    for (int k = 0; k < list.size(); k++) {
                        GoalStore.Goal item = list.get(k);
                        if (!item.isDone()) {
                            item.name = name;
                            item.targetCents = Math.round(d * 100.0d);
                            item.startStamp = iArr[0];
                        }
                    }
                    GoalStore.save(main, list);
                    Toast.makeText(main, "目标已保存", 0).show();
                } else {
                    // 新建目标
                    GoalStore.startNew(main, name, Math.round(d * 100.0d), iArr[0]);
                    GoalStore.resetCelebrate(main);
                    Toast.makeText(main, "新目标已设好", 0).show();
                }
                main.rebaselineGoalCelebrate(d);
                main.updateGoalView();
                main.updateCumulativeCard();
                SalaryWidgetProvider.refreshAll(main);
            }
        }).show();
    }

    private void checkGoalCelebrate(double d) {
        int i;
        if (d >= 1.0d) {
            i = 100;
        } else if (d >= 0.75d) {
            i = 75;
        } else if (d >= 0.5d) {
            i = 50;
        } else if (d < 0.25d) {
            i = 0;
        } else {
            i = 25;
        }
        if (i == 0) {
            return;
        }
        try {
            if (i > this.preferences.getInt("goal_celebrated", 0)) {
                this.preferences.edit().putInt("goal_celebrated", i).apply();
                if (this.workerPetView != null) {
                    this.workerPetView.celebrate();
                }
                Toast.makeText(this, i >= 100 ? "🎉 存钱目标达成！" : "🎉 存钱罐已达 " + i + "%！", 1).show();
            }
        } catch (Throwable th) {
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void rebaselineGoalCelebrate(double d) {
        int i = 0;
        if (d > 0.0d) {
            try {
                double goalProgress = goalProgress(this) / d;
                if (goalProgress >= 1.0d) {
                    i = 100;
                } else if (goalProgress >= 0.75d) {
                    i = 75;
                } else if (goalProgress >= 0.5d) {
                    i = 50;
                } else if (goalProgress >= 0.25d) {
                    i = 25;
                }
                this.preferences.edit().putInt("goal_celebrated", i).apply();
            } catch (Throwable th) {
                return;
            }
        }
        this.preferences.edit().putInt("goal_celebrated", i).apply();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateGoalView() {
        if (this.goalView == null) {
            return;
        }
        GoalStore.Goal goal = GoalStore.current(this);
        this.currentGoal = goal;
        if (goal == null) {
            this.goalView.setText("设置一个存钱目标");
            this.goalView.setTextColor(COLOR_MUTED);
            this.goalSubView.setText("");
            this.goalPercentView.setText("");
            this.goalMilestoneBar.setData(0.0f, MATCH);
            this.goalMilestoneTag.setVisibility(8);
            this.goalConfirmButton.setVisibility(8);
            this.goalSavedView.setText("");
            this.goalRemainView.setText("");
            this.goalDateView.setText("");
            return;
        }

        double target = goal.targetCents / 100.0d;
        double progress = GoalStore.progressYuan(this, goal);
        double ratio = target > 0.0d ? progress / target : 0.0d;
        double dailySalary = this.settings.dailySalary;

        // 达标检测（第一次攒够时记下达标日）
        GoalStore.markReachedIfNeeded(this, goal);
        goal = GoalStore.current(this);
        this.currentGoal = goal;
        boolean reached = goal != null && goal.isReached();

        this.goalView.setText("🎯 " + goal.name);
        this.goalView.setTextColor(COLOR_TEXT);

        // 开始日期 + 状态
        String status = reached ? "已达成" : "进行中";
        this.goalSubView.setText("开始日期 " + GoalStore.formatStamp(goal.startStamp) + "　·　" + status);

        int percent = (int) Math.max(0, Math.round(100.0d * ratio));
        if (percent > 999) {
            percent = 999;
        }
        this.goalPercentView.setText(percent + "%");
        this.goalPercentView.setTextColor(COLOR_ACCENT);

        int milestone = 1;
        while (milestone < 4 && ratio >= (milestone + 1) / 4.0d - 1.0E-4d) {
            milestone++;
        }
        this.goalMilestoneBar.setData((float) ratio, milestone);

        if (ratio >= 1.0d) {
            // 达标：显示达标日，并给出「确认完成」按钮
            this.goalMilestoneTag.setVisibility(0);
            this.goalMilestoneTag.setText(this.cumHidden
                    ? "🎉 已攒够！"
                    : "🎉 " + GoalStore.formatStampShort(goal.reachedStamp) + "已攒够（¥"
                            + this.moneyFormat.format(progress) + "）");
            this.goalMilestoneTag.setTextColor(COLOR_ACCENT);
            this.goalMilestoneTag.setBackground(UiKit.solid(COLOR_CHIP_GREEN_BG, dp(10.0f)));
        } else {
            double nextTarget = (milestone + 1) <= 4 ? (milestone + 1) * target / 4.0d
                    : target;
            int workdays = dailySalary > 0.0d
                    ? (int) Math.ceil(Math.max(0.0d, nextTarget - progress) / dailySalary) : 0;
            StringBuilder sb = new StringBuilder();
            sb.append("下个里程碑 ").append((milestone + 1) * 25).append("%");
            if (!this.cumHidden && nextTarget > 0.0d) {
                sb.append("（¥").append(this.moneyFormat.format(nextTarget)).append("）");
            }
            if (workdays > 0) {
                sb.append(" · 还需 ").append(workdays).append(" 个工作日");
            }
            this.goalMilestoneTag.setVisibility(0);
            this.goalMilestoneTag.setText(sb.toString());
            this.goalMilestoneTag.setTextColor(COLOR_AMBER);
            this.goalMilestoneTag.setBackground(UiKit.solid(COLOR_CHIP_BG, dp(10.0f)));
        }

        if (this.goalConfirmButton != null) {
            this.goalConfirmButton.setVisibility(ratio >= 1.0d ? 0 : 8);
        }

        double remain = Math.max(0.0d, target - progress);
        this.goalSavedView.setText(this.cumHidden ? "¥••••••" : "¥" + this.moneyFormat.format(progress));
        this.goalRemainView.setText(this.cumHidden ? "¥••••••" : "¥" + this.moneyFormat.format(remain));
        if (ratio >= 1.0d) {
            int usedDays = GoalStore.daysBetween(goal.startStamp, goal.reachedStamp);
            this.goalDateView.setText(usedDays > 0 ? usedDays + " 天" : "已完成");
        } else if (dailySalary > 0.0d) {
            this.goalDateView.setText(estimateFinishDate(this, (int) Math.ceil(remain / dailySalary)));
        } else {
            this.goalDateView.setText("—");
        }
    }

    /** 用户点「确认完成」：弹确认框（默认今天），确认后结清当前目标。 */
    void confirmGoalDone() {
        final GoalStore.Goal goal = this.currentGoal != null
                ? this.currentGoal : GoalStore.current(this);
        if (goal == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("确认完成")
                .setMessage("确认「" + goal.name + "」已经完成？\n\n"
                        + "确认后这条会记入历史，并可以从下一个目标继续攒。")
                .setNegativeButton("还没", (DialogInterface.OnClickListener) null)
                .setPositiveButton("确认完成", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.43
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i) {
                        GoalStore.complete(MainActivity.this, goal, GoalStore.todayStamp());
                        // 换目标了，里程碑庆祝要重新开始
                        GoalStore.resetCelebrate(MainActivity.this);
                        MainActivity.this.updateGoalView();
                        MainActivity.this.updateCumulativeCard();
                        SalaryWidgetProvider.refreshAll(MainActivity.this);
                        Toast.makeText(MainActivity.this, "🎉 目标完成！可以设下一个了", 1).show();
                    }
                })
                .show();
    }

    static String estimateFinishDate(Context context, int i) {
        if (i <= 0) {
            return "今天";
        }
        try {
            SalarySettings load = SalarySettings.load(context.getSharedPreferences("salary_jump", 0));
            Calendar calendar = Calendar.getInstance();
            int i2 = 0;
            for (int i3 = 0; i3 < 2000 && i2 < i; i3++) {
                calendar.add(5, 1);
                if (WorkdayHelper.isWorkday(context, load, calendar)) {
                    i2++;
                }
            }
            return (calendar.get(2) + 1) + "月" + calendar.get(5) + "日";
        } catch (Throwable th) {
            return "—";
        }
    }

    /** 归档昨天及之前的工资 —— 委托给可测的 {@link DailyArchive}。 */
    private static void cumRollover(Context context) {
        DailyArchive.rollover(context);
    }

    private static int cumStamp(Calendar calendar) {
        return (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
    }

    /**
     * 本月已结算天数 —— 和 {@link #monthlyTotal} 配套，避免「本月金额 + 全部天数」的矛盾。
     *
     * <p>遍历本月已归档的每一天，用 {@link ArchiveSync#countsAsSettledDay} 判定
     * （它会把「调休出勤」排除掉，那些天不算计薪日）。
     */
    public static int monthlySettledDays(Context context) {
        if (context == null) {
            return 0;
        }
        try {
            Calendar now = Calendar.getInstance();
            int count = 0;
            for (SalaryHistory.Day day : SalaryHistory.byMonth(context, now.get(1), now.get(2) + 1)) {
                if (ArchiveSync.countsAsSettledDay(context, day.stamp)) {
                    count++;
                }
            }
            return count;
        } catch (Throwable unused) {
            return 0;
        }
    }

    public static int cumulativeDays(Context context) {
        cumRollover(context);
        return context.getSharedPreferences("salary_jump", 0).getInt("cum_days", 0);
    }

    /** 当前目标的已攒金额（元）—— 战报等页面统一走这个入口。 */
    static double goalProgress(Context context) {
        GoalStore.Goal goal = GoalStore.current(context);
        return GoalStore.progressYuan(context, goal);
    }

    /**
     * 本月累计收入（元）= 本月已归档的各天 + 今天还没归档的实时部分。
     *
     * <p>与 {@link #cumulativeTotal}（从开始用到现在的总额）不同：
     * 这个只算「本月 1 号到今天」。已归档的日期直接查 {@code SalaryHistory}；
     * 今天若还没归档，用 {@code SalaryCalculator.calculate().earned} 补上。
     */
    static double monthlyTotal(Context context) {
        if (context == null) {
            return 0.0d;
        }
        try {
            Calendar now = Calendar.getInstance();
            int year = now.get(1);
            int month = now.get(2) + 1;
            int todayStamp = SalaryHistory.stamp(now);
            SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
            SalarySettings settings = SalarySettings.load(sharedPreferences);

            long archived = SalaryHistory.monthTotalCents(context, year, month);

            // 今天若还没归档（没有当天记录），补上实时已赚
            long todayCents = SalaryHistory.centsOf(context, todayStamp);
            double live = todayCents > 0L ? 0.0d
                    : SalaryCalculator.calculate(context, settings, now).earned;

            return (archived / 100.0d) + live;
        } catch (Throwable unused) {
            return 0.0d;
        }
    }

    static double cumulativeTotal(Context context) {
        cumRollover(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences("salary_jump", 0);
        return Double.longBitsToDouble(sharedPreferences.getLong("cum_total_bits", 0L)) + SalaryCalculator.calculate(context, SalarySettings.load(sharedPreferences), Calendar.getInstance()).earned;
    }

    private void promptExactAlarmOnceOnLaunch() {
        SharedPreferences sharedPreferences = this.preferences;
        if (sharedPreferences.getBoolean("exact_alarm_prompted", false)) {
            return;
        }
        sharedPreferences.edit().putBoolean("exact_alarm_prompted", true).apply();
        requestExactAlarmPermissionIfNeeded();
    }

    void updateCumulativeCard() {
        if (this.cumulativeView != null) {
            if (this.cumHidden) {
                this.cumulativeView.setTag(null);
                this.cumulativeView.setText("¥ ••••••••");
                this.cumulativeSubView.setText("按工作日累计 · 点击金额查看");
            } else {
                double cumulativeTotal = monthlyTotal(this);
                if (this.cumIntro) {
                    this.cumIntro = false;
                    MotionKit.countUp(this.cumulativeView, "¥ ", cumulativeTotal, this.moneyFormat);
                } else {
                    this.cumulativeView.setText("¥ " + this.moneyFormat.format(cumulativeTotal));
                }
                this.cumulativeSubView.setText("按工作日累计");
            }
            // 标题是「累计收入（本月）」，天数也要跟着是本月的
            this.cumulativeDaysView.setText("已结算 " + monthlySettledDays(this) + " 天");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void toggleCumulativeMask() {
        this.cumHidden = !this.cumHidden;
        if (!this.cumHidden) {
            this.cumIntro = true;
        }
        updateCumulativeCard();
        updateGoalView();
    }

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        try {
            CrashLogger.install(this);
            WorkdayHelper.invalidate();
            WorkdayHelper.applyOnlineData(this);
            StatutoryHolidays.applyOnlineData(this);
            applyTheme(this);
            this.density = getResources().getDisplayMetrics().density;
            SharedPreferences sharedPreferences = getSharedPreferences("salary_jump", 0);
            this.preferences = sharedPreferences;
            this.settings = SalarySettings.load(sharedPreferences);
            configureWindow();
            buildInterface();
            startTicker();
            updateRateLabels();
            SalaryWidgetProvider.refreshAll(this);
            WaterReminderScheduler.scheduleNext(this);
            requestNotificationPermission();
            promptExactAlarmOnceOnLaunch();
            setupPrivacyLock();
        } catch (Throwable th) {
            showStartupError(th);
        }
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        if (PrivacyLock.isEnabled(this) && !appUnlocked) {
            showLockOverlay();
        } else if (this.lockOverlay != null) {
            this.lockOverlay.setVisibility(8);
        }
    }

    @Override // android.app.Activity
    protected void onResume() {
        super.onResume();
        try {
            this.settings = SalarySettings.load(this.preferences);
            applyPetSettings();
            // 打开 App 时补跑自动备份（定时闹钟可能被 ROM 杀掉）
            BackupScheduler.catchUpIfDue(this);
            // ★ 归档昨天的工资。
            //   原来它藏在 cumulativeTotal() / cumulativeDays() 里靠「界面调它」触发，
            //   但 v3.87 起主卡改用 monthlyTotal()，那两个方法没人调了 →
            //   归档从此再也没跑过（用户 10/20 的工资没入账就是这么来的）。
            //   归档是「每天该做一次」的事，必须显式调用，不能依附于某个显示函数。
            cumRollover(this);
            Calendar calendar = Calendar.getInstance();
            ArchiveSync.refreshPrewritten(this, (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5));
            SeedBackfill.applyOnce(this);
            ArchiveSync.healEventRowsOnce(this);
            CheckInReceiver.clear(this);
            try {
                if (PaydayReminder.isEnabled(this)) {
                    Calendar calendar2 = Calendar.getInstance();
                    int i = (calendar2.get(1) * 10000) + ((calendar2.get(2) + 1) * 100) + calendar2.get(5);
                    if (calendar2.get(5) == PaydayReminder.effectiveDay(calendar2.get(1), calendar2.get(2) + 1, PaydayReminder.day(this)) && this.preferences.getInt("payday_celebrated_day", 0) != i) {
                        this.preferences.edit().putInt("payday_celebrated_day", i).apply();
                        if (this.workerPetView != null) {
                            this.workerPetView.celebrate();
                        }
                        Toast.makeText(this, "💰 今天是发薪日！", 1).show();
                    }
                }
            } catch (Throwable th) {
            }
            updateRateLabels();
            refreshDisplay();
            requestCalendarPermissionIfNeeded();
        } catch (Throwable th2) {
        }
        try {
            if (PrivacyLock.isEnabled(this) && !appUnlocked && (this.lockOverlay == null || this.lockOverlay.getVisibility() != 0)) {
                showLockOverlay();
            }
        } catch (Throwable th3) {
        }
        if (!this.autoPrompted && this.lockOverlay != null && this.lockOverlay.getVisibility() == 0 && PrivacyLock.isEnabled(this) && !appUnlocked && this.lockFails < 5 && fingerprintAvailable()) {
            this.autoPrompted = true;
            requestFingerprint();
        }
    }

    @Override // android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            if (PrivacyLock.isEnabled(this) && !appUnlocked) {
                showLockOverlay();
            }
        } catch (Throwable th) {
        }
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        if (this.lockOverlay != null && this.lockOverlay.getVisibility() == 0) {
            return;
        }
        super.onBackPressed();
    }

    private void setupPrivacyLock() {
        if (!lifecycleHooked) {
            lifecycleHooked = true;
            registerLockLifecycle();
        }
        this.lockOverlay = buildLockOverlay();
        this.lockOverlay.setVisibility(8);
        addContentView(this.lockOverlay, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    private void registerLockLifecycle() {
        try {
            getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() { // from class: com.salaryjump.app.MainActivity.7
                @Override // android.app.Application.ActivityLifecycleCallbacks
                public void onActivityCreated(Activity activity, Bundle bundle) {
                }

                @Override // android.app.Application.ActivityLifecycleCallbacks
                public void onActivityStarted(Activity activity) {
                    MainActivity.access$308();
                }

                @Override // android.app.Application.ActivityLifecycleCallbacks
                public void onActivityResumed(Activity activity) {
                }

                @Override // android.app.Application.ActivityLifecycleCallbacks
                public void onActivityPaused(Activity activity) {
                }

                @Override // android.app.Application.ActivityLifecycleCallbacks
                public void onActivityStopped(Activity activity) {
                    MainActivity.access$310();
                    if (MainActivity.visibleActivities <= 0 && !activity.isChangingConfigurations() && PrivacyLock.isEnabled(activity)) {
                        boolean unused = MainActivity.appUnlocked = false;
                    }
                }

                @Override // android.app.Application.ActivityLifecycleCallbacks
                public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                }

                @Override // android.app.Application.ActivityLifecycleCallbacks
                public void onActivityDestroyed(Activity activity) {
                }
            });
        } catch (Throwable th) {
        }
    }

    private View buildLockOverlay() {
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(COLOR_BACKGROUND);
        frameLayout.setClickable(true);
        frameLayout.setFocusable(true);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setGravity(1);
        linearLayout.setPadding(dp(34.0f), 0, dp(34.0f), 0);
        frameLayout.addView(linearLayout, new FrameLayout.LayoutParams(MATCH, -2, 17));
        TextView textView = new TextView(this);
        textView.setText("🔒 SalaryJump");
        textView.setTextSize(20.0f);
        textView.setTextColor(COLOR_TEXT);
        textView.setGravity(17);
        textView.setTypeface(Typeface.create("sans", 1));
        linearLayout.addView(textView, matchWrap());
        this.lockDots = new LinearLayout(this);
        this.lockDots.setOrientation(0);
        this.lockDots.setGravity(17);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(22.0f);
        linearLayout.addView(this.lockDots, matchWrap);
        renderLockDots();
        this.lockHintView = new TextView(this);
        this.lockHintView.setText("输入 4 位 PIN 解锁");
        this.lockHintView.setTextSize(12.5f);
        this.lockHintView.setTextColor(COLOR_MUTED);
        this.lockHintView.setGravity(17);
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(16.0f);
        linearLayout.addView(this.lockHintView, matchWrap2);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        linearLayout2.setGravity(1);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH, -2);
        layoutParams.topMargin = dp(26.0f);
        layoutParams.leftMargin = dp(18.0f);
        layoutParams.rightMargin = dp(18.0f);
        linearLayout.addView(linearLayout2, layoutParams);
        String[][] strArr = {new String[]{"1", "2", "3"}, new String[]{"4", "5", "6"}, new String[]{"7", "8", "9"}, new String[]{"bio", "0", "del"}};
        for (int i = 0; i < 4; i++) {
            LinearLayout linearLayout3 = new LinearLayout(this);
            linearLayout3.setOrientation(0);
            linearLayout3.setGravity(17);
            LinearLayout.LayoutParams matchWrap3 = matchWrap();
            if (i > 0) {
                matchWrap3.topMargin = dp(10.0f);
            }
            linearLayout2.addView(linearLayout3, matchWrap3);
            for (int i2 = 0; i2 < strArr[i].length; i2++) {
                linearLayout3.addView(buildLockKey(strArr[i][i2]), new LinearLayout.LayoutParams(0, dp(58.0f), 1.0f));
            }
        }
        return frameLayout;
    }

    private View buildLockKey(final String str) {
        TextView textView = new TextView(this);
        textView.setGravity(17);
        textView.setTextSize(("bio".equals(str) || "del".equals(str)) ? 15.0f : 22.0f);
        textView.setTextColor(COLOR_TEXT);
        textView.setTypeface(Typeface.create("sans", 1));
        if ("bio".equals(str)) {
            textView.setText("指纹");
            textView.setTextColor(COLOR_ACCENT);
        } else if ("del".equals(str)) {
            textView.setText("⌫");
        } else {
            textView.setText(str);
        }
        textView.setBackground(UiKit.solid(COLOR_SURFACE, dp(18.0f)));
        ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(MATCH, MATCH);
        marginLayoutParams.setMargins(dp(6.0f), 0, dp(6.0f), 0);
        textView.setLayoutParams(marginLayoutParams);
        textView.setClickable(true);
        textView.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.8
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.onLockKey(str);
            }
        });
        return textView;
    }

    private boolean fingerprintAvailable() {
        return PrivacyLock.isBiometricAllowed(this) && PrivacyLock.biometricAvailable(this);
    }

    private void renderLockDots() {
        if (this.lockDots == null) {
            return;
        }
        this.lockDots.removeAllViews();
        int i = 0;
        while (i < 4) {
            View view = new View(this);
            view.setBackground(UiKit.circle(i < this.lockInput.length() ? COLOR_ACCENT_BRIGHT : COLOR_BORDER));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(13.0f), dp(13.0f));
            layoutParams.leftMargin = dp(9.0f);
            layoutParams.rightMargin = dp(9.0f);
            this.lockDots.addView(view, layoutParams);
            i++;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onLockKey(String str) {
        String str2;
        if ("bio".equals(str)) {
            requestFingerprint();
            return;
        }
        boolean equals = "del".equals(str);
        String str3 = this.lockInput;
        if (equals) {
            if (str3.length() > 0) {
                this.lockInput = this.lockInput.substring(0, this.lockInput.length() - 1);
                renderLockDots();
                return;
            }
            return;
        }
        if (str3.length() >= 4) {
            return;
        }
        this.lockInput += str;
        renderLockDots();
        if (this.lockInput.length() == 4) {
            if (PrivacyLock.verify(this, this.lockInput)) {
                appUnlocked = true;
                this.lockInput = "";
                this.lockFails = 0;
                this.lockHintView.setText("输入 4 位 PIN 解锁");
                renderLockDots();
                this.lockOverlay.setVisibility(8);
                return;
            }
            this.lockFails++;
            this.lockInput = "";
            renderLockDots();
            TextView textView = this.lockHintView;
            if (this.lockFails >= 5) {
                str2 = "输入错误次数过多，请使用 PIN";
            } else {
                str2 = "PIN 不正确，请重试";
            }
            textView.setText(str2);
        }
    }

    private void showLockOverlay() {
        if (this.lockOverlay == null) {
            try {
                this.lockOverlay = buildLockOverlay();
                addContentView(this.lockOverlay, new FrameLayout.LayoutParams(MATCH, MATCH));
            } catch (Throwable th) {
                return;
            }
        }
        boolean z = this.lockOverlay.getVisibility() != 0;
        this.lockOverlay.setVisibility(0);
        if (z) {
            this.lockInput = "";
            renderLockDots();
            this.autoPrompted = false;
        }
    }

    private void requestFingerprint() {
        if (this.lockFails >= 5) {
            setLockHint("输入错误次数过多，请用 PIN");
            return;
        }
        if (!PrivacyLock.isBiometricAllowed(this)) {
            setLockHint("指纹解锁已关闭，请用 PIN");
            return;
        }
        if (!PrivacyLock.biometricAvailable(this)) {
            setLockHint("未检测到已录入的指纹，请用 PIN");
            return;
        }
        if (Build.VERSION.SDK_INT >= 28 && !this.bioFallbackOnly) {
            try {
                startBiometricPrompt();
                return;
            } catch (Throwable th) {
                this.lastBiometricError = "BiometricPrompt: " + th;
                this.bioFallbackOnly = true;
            }
        }
        try {
            if (startLegacyFingerprint()) {
                return;
            }
        } catch (Throwable th2) {
            this.lastBiometricError = "FingerprintManager: " + th2;
        }
        if (this.lastBiometricError.length() > 0) {
            setLockHint("指纹不可用（" + this.lastBiometricError + "），请用 PIN");
        } else {
            setLockHint("指纹不可用，请用 PIN");
        }
    }

    private void startBiometricPrompt() {
        this.bioCancel = new CancellationSignal();
        new BiometricPrompt.Builder(this).setTitle("解锁 SalaryJump").setSubtitle("请验证指纹").setNegativeButton("使用 PIN", getMainExecutor(), new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.9
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.setLockHint("输入 4 位 PIN 解锁");
            }
        }).build().authenticate(this.bioCancel, getMainExecutor(), new BiometricPrompt.AuthenticationCallback() { // from class: com.salaryjump.app.MainActivity.10
            @Override // android.hardware.biometrics.BiometricPrompt.AuthenticationCallback
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult authenticationResult) {
                MainActivity.this.unlockByBiometric();
            }

            @Override // android.hardware.biometrics.BiometricPrompt.AuthenticationCallback
            public void onAuthenticationError(int i, CharSequence charSequence) {
                if (i == 5 || i == 10 || i == 13) {
                    MainActivity.this.setLockHint("输入 4 位 PIN 解锁");
                    return;
                }
                MainActivity.this.lastBiometricError = "错误 " + i + "：" + ((Object) charSequence);
                MainActivity.this.bioFallbackOnly = true;
                MainActivity.this.setLockHint("指纹异常：" + ((Object) charSequence) + "（请用 PIN）");
            }

            @Override // android.hardware.biometrics.BiometricPrompt.AuthenticationCallback
            public void onAuthenticationFailed() {
                MainActivity.this.setLockHint("指纹不匹配，请再试一次");
            }
        });
    }

    private boolean startLegacyFingerprint() {
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService("fingerprint");
        if (fingerprintManager == null || !fingerprintManager.isHardwareDetected() || !fingerprintManager.hasEnrolledFingerprints()) {
            return false;
        }
        this.fpCancel = new CancellationSignal();
        fingerprintManager.authenticate(null, this.fpCancel, 0, new FingerprintManager.AuthenticationCallback() { // from class: com.salaryjump.app.MainActivity.11
            @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult authenticationResult) {
                MainActivity.this.unlockByBiometric();
            }

            @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
            public void onAuthenticationError(int i, CharSequence charSequence) {
                MainActivity.this.lastBiometricError = "错误 " + i + "：" + ((Object) charSequence);
                MainActivity.this.setLockHint("指纹异常：" + ((Object) charSequence) + "（请用 PIN）");
            }

            @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
            public void onAuthenticationFailed() {
                MainActivity.this.setLockHint("指纹不匹配，请再试一次");
            }

            @Override // android.hardware.fingerprint.FingerprintManager.AuthenticationCallback
            public void onAuthenticationHelp(int i, CharSequence charSequence) {
                MainActivity.this.setLockHint(String.valueOf(charSequence));
            }
        }, null);
        setLockHint("请将手指放在指纹传感器上");
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void unlockByBiometric() {
        stopFingerprint();
        appUnlocked = true;
        this.lockInput = "";
        this.lockFails = 0;
        renderLockDots();
        setLockHint("输入 4 位 PIN 解锁");
        if (this.lockOverlay != null) {
            this.lockOverlay.setVisibility(8);
        }
    }

    private void stopFingerprint() {
        if (this.fpCancel != null) {
            try {
                this.fpCancel.cancel();
            } catch (Throwable th) {
            }
            this.fpCancel = null;
        }
        if (this.bioCancel != null) {
            try {
                this.bioCancel.cancel();
            } catch (Throwable th2) {
            }
            this.bioCancel = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setLockHint(String str) {
        if (this.lockHintView != null) {
            this.lockHintView.setText(str);
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        Runnable runnable;
        super.onDestroy();
        Handler handler = this.handler;
        if (handler == null || (runnable = this.ticker) == null) {
            return;
        }
        handler.removeCallbacks(runnable);
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setSoftInputMode(16);
        applyEdgeToEdge(window);
    }

    static void applyEdgeToEdge(Window window) {
        int i;
        if (window == null) {
            return;
        }
        int i2 = 0;
        try {
            window.addFlags(Integer.MIN_VALUE);
            window.setStatusBarColor(0);
            window.setNavigationBarColor(0);
            window.setNavigationBarDividerColor(0);
        } catch (Throwable th) {
        }
        if (Build.VERSION.SDK_INT >= 30) {
            try {
                window.setDecorFitsSystemWindows(false);
                window.setStatusBarContrastEnforced(false);
                window.setNavigationBarContrastEnforced(false);
                WindowInsetsController windowInsetsController = window.getDecorView().getWindowInsetsController();
                if (windowInsetsController != null) {
                    if (!NIGHT) {
                        i2 = 24;
                    }
                    windowInsetsController.setSystemBarsAppearance(i2, 24);
                    return;
                }
                return;
            } catch (Throwable th2) {
                return;
            }
        }
        if (NIGHT) {
            i = 1792;
        } else {
            i = 10000;
        }
        window.getDecorView().setSystemUiVisibility(i);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33 || checkSelfPermission("android.permission.POST_NOTIFICATIONS") == 0) {
            return;
        }
        requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 4300);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void buildInterface() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        LinearLayout linearLayout = new LinearLayout(this);
        this.root = linearLayout;
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20.0f), dp(18.0f), dp(20.0f), dp(26.0f));
        scrollView.addView(this.root, new FrameLayout.LayoutParams(MATCH, -2));
        setContentView(UiKit.aurora(this, scrollView));
        showPendingCrash();
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        this.root.addView(linearLayout2, matchWrap());
        MotionKit.enter(linearLayout2, this.density, 0);
        TextView textView = new TextView(this);
        textView.setText("¥");
        textView.setTextSize(15.0f);
        textView.setTextColor(HERO_TEXT);
        textView.setGravity(17);
        textView.setTypeface(Typeface.create("sans", 1));
        textView.setBackground(UiKit.diagonal(LOGO_COLORS, dp(10.0f)));
        textView.setElevation(dp(4.0f));
        applyShadowTint(textView);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(30.0f), dp(30.0f));
        layoutParams.rightMargin = dp(10.0f);
        linearLayout2.addView(textView, layoutParams);
        linearLayout2.addView(text("SalaryJump", 19.0f, COLOR_TEXT, 1), new LinearLayout.LayoutParams(0, dp(40.0f), 1.0f));
        ImageView imageView = new ImageView(this);
        imageView.setImageDrawable(UiKit.gear(COLOR_MUTED));
        imageView.setBackground(UiKit.circle(COLOR_SURFACE));
        imageView.setElevation(dp(2.0f));
        imageView.setPadding(dp(9.0f), dp(9.0f), dp(9.0f), dp(9.0f));
        imageView.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.12
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                MainActivity.this.showSettingsDialog();
            }
        });
        linearLayout2.addView(imageView, new LinearLayout.LayoutParams(dp(38.0f), dp(38.0f)));
        LinearLayout heroCard = heroCard();
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(18.0f);
        this.root.addView(heroCard, matchWrap);
        MotionKit.enter(heroCard, this.density, 1);
        LinearLayout linearLayout3 = new LinearLayout(this);
        this.capsRow = linearLayout3;
        linearLayout3.setOrientation(0);
        linearLayout3.setGravity(16);
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.bottomMargin = dp(14.0f);
        heroCard.addView(linearLayout3, matchWrap2);
        this.caps = new TextView[4];
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            TextView text = text("", 12.5f, HERO_TEXT, 0);
            text.setGravity(17);
            text.setSingleLine(true);
            text.setEllipsize(TextUtils.TruncateAt.END);
            text.setPadding(dp(8.0f), 0, dp(8.0f), 0);
            text.setClickable(true);
            text.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.13
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    if (idx == 0) {
                        MainActivity.this.onOvertimeAction();
                        return;
                    }
                    if (idx == 1) {
                        MainActivity.this.onLeaveAction();
                        return;
                    }
                    int i2 = idx;
                    MainActivity mainActivity = MainActivity.this;
                    if (i2 == 2) {
                        mainActivity.onHolidayAction();
                    } else {
                        mainActivity.onExtraAction();
                    }
                }
            });
            text.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.salaryjump.app.MainActivity.14
                @Override // android.view.View.OnLongClickListener
                public boolean onLongClick(View view) {
                    MainActivity.this.onCapLongClick(idx);
                    return true;
                }
            });
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, dp(40.0f), 1.0f);
            if (i > 0) {
                layoutParams2.leftMargin = dp(7.0f);
            }
            linearLayout3.addView(text, layoutParams2);
            this.caps[i] = text;
            MotionKit.press(text);
        }
        LinearLayout linearLayout4 = new LinearLayout(this);
        linearLayout4.setOrientation(0);
        linearLayout4.setGravity(16);
        heroCard.addView(linearLayout4, matchWrap());
        View view = new View(this);
        Drawable breathingDot = MotionKit.breathingDot(dp(6.0f), NIGHT ? -1493172225 : COLOR_ACCENT);
        view.setBackground(breathingDot);
        if (breathingDot instanceof Animatable) {
            ((Animatable) breathingDot).start();
        }
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(dp(14.0f), dp(14.0f));
        layoutParams3.rightMargin = dp(4.0f);
        linearLayout4.addView(view, layoutParams3);
        linearLayout4.addView(heroLabel("今日已赚"), new LinearLayout.LayoutParams(-2, dp(24.0f)));
        // 工作节奏紧跟在「今日已赚」后面，占掉整行剩余宽度（时钟已删）。
        // 字号由 fitScheduleText() 按可用宽度自适应（8–11sp），保证一行放得下。
        this.scheduleView = text("", 10.0f, HERO_SUB2, 0);
        this.scheduleView.setSingleLine(true);
        this.scheduleView.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams scheduleParams =
                new LinearLayout.LayoutParams(0, dp(24.0f), 1.0f);
        scheduleParams.leftMargin = dp(8.0f);
        linearLayout4.addView(this.scheduleView, scheduleParams);
        this.amountView = text("¥ 0.00", HeroFit.AMOUNT_MAX_SP, HERO_TEXT, 1);
        this.amountView.setSingleLine(true);
        this.amountView.setIncludeFontPadding(false);
        // 不用系统 autosize：单行 + horizontallyScrolling 下它并不可靠，
        // 改为在 fitAmountText() 里按实际可用宽度直接算字号（见 HeroFit）。
        this.amountTextSizePx = HeroFit.AMOUNT_MAX_SP * this.density;
        LinearLayout linearLayout5 = new LinearLayout(this);
        this.heroAmountRow = linearLayout5;
        linearLayout5.setOrientation(0);
        linearLayout5.setGravity(16);
        LinearLayout.LayoutParams matchWrap3 = matchWrap();
        matchWrap3.topMargin = dp(10.0f);
        heroCard.addView(linearLayout5, matchWrap3);
        linearLayout5.addView(this.amountView, new LinearLayout.LayoutParams(0, -2, 1.0f));
        LinearLayout linearLayout6 = new LinearLayout(this);
        linearLayout6.setOrientation(1);
        linearLayout6.setGravity(17);
        linearLayout5.addView(linearLayout6, new LinearLayout.LayoutParams(dp(88.0f), -2));
        this.petColumn = linearLayout6;
        this.workerPetView = new WorkerPetView(this);
        this.workerPetView.setScene(true);
        this.workerPetView.setClickable(true);
        this.workerPetView.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.15
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                String pokeText = MainActivity.this.workerPetView.poke();
                MainActivity.this.workerPetView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                if (MainActivity.this.petMoodView != null) {
                    MainActivity.this.petMoodView.setText(pokeText != null ? pokeText : PetState.pokeText(MainActivity.this));
                }
            }
        });
        MotionKit.press(this.workerPetView);
        linearLayout6.addView(this.workerPetView, new LinearLayout.LayoutParams(dp(88.0f), dp(92.0f)));
        this.petMoodView = text("", 10.0f, HERO_SUB2, 0);
        this.petMoodView.setGravity(17);
        // 不能 singleLine：小人那列只有 104dp，「谁也别叫我 · 距发薪 5 天」一行放不下会被砍
        this.petMoodView.setMaxLines(2);
        this.petMoodView.setLineSpacing(0f, 1.05f);
        linearLayout6.addView(this.petMoodView, matchWrap());
        this.petFatigueView = text("", 9.5f, HERO_SUB2, 0);
        this.petFatigueView.setGravity(17);
        this.petFatigueView.setVisibility(8);
        linearLayout6.addView(this.petFatigueView, matchWrap());
        this.statusView = text("", 12.0f, HERO_TEXT, 0);
        this.statusView.setBackground(UiKit.solid(HERO_CHIP, dp(20.0f)));
        this.statusView.setPadding(dp(12.0f), dp(6.0f), dp(12.0f), dp(6.0f));
        LinearLayout.LayoutParams wrapWrap = wrapWrap();
        wrapWrap.topMargin = dp(12.0f);
        heroCard.addView(this.statusView, wrapWrap);
        SheenBar sheenBar = new SheenBar(this);
        this.progressBar = sheenBar;
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(MATCH, dp(8.0f));
        layoutParams4.topMargin = dp(16.0f);
        heroCard.addView(sheenBar, layoutParams4);
        LinearLayout linearLayout7 = new LinearLayout(this);
        linearLayout7.setOrientation(0);
        linearLayout7.setGravity(16);
        LinearLayout.LayoutParams matchWrap4 = matchWrap();
        matchWrap4.topMargin = dp(18.0f);
        heroCard.addView(linearLayout7, matchWrap4);
        this.targetView = addStat(linearLayout7, "今日目标", HERO_TEXT, HERO_SUB2);
        this.hourlyView = addStat(linearLayout7, "每小时", HERO_TEXT, HERO_SUB2);
        this.hourlyLabelView = this.lastStatLabel;
        this.minuteView = addStat(linearLayout7, "每分钟", HERO_TEXT, HERO_SUB2);
        this.secondView = addStat(linearLayout7, "每秒", HERO_TEXT, HERO_SUB2);

        LinearLayout card = card(true);
        LinearLayout.LayoutParams matchWrap6 = matchWrap();
        matchWrap6.topMargin = dp(14.0f);
        this.root.addView(card, matchWrap6);
        MotionKit.enter(card, this.density, 2);
        LinearLayout linearLayout8 = new LinearLayout(this);
        linearLayout8.setOrientation(0);
        linearLayout8.setGravity(16);
        card.addView(linearLayout8, matchWrap());
        LinearLayout linearLayout9 = new LinearLayout(this);
        linearLayout9.setOrientation(0);
        linearLayout9.setGravity(16);
        linearLayout8.addView(linearLayout9, new LinearLayout.LayoutParams(0, dp(40.0f), 1.0f));
        LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(dp(14.0f), dp(14.0f));
        layoutParams6.rightMargin = dp(4.0f);
        View dot = dot(COLOR_BLUE_BRIGHT);
        Drawable breathingDot2 = MotionKit.breathingDot(dp(6.0f), COLOR_BLUE_BRIGHT);
        dot.setBackground(breathingDot2);
        if (breathingDot2 instanceof Animatable) {
            ((Animatable) breathingDot2).start();
        }
        linearLayout9.addView(dot, layoutParams6);
        linearLayout9.addView(label("今日喝水"), new LinearLayout.LayoutParams(-2, -2));
        linearLayout8.addView(link("记录", new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.16
            @Override // android.view.View.OnClickListener
            public void onClick(View view3) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, (Class<?>) WaterRecordsActivity.class));
            }
        }), new LinearLayout.LayoutParams(-2, dp(40.0f)));
        linearLayout8.addView(link("统计", new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.17
            @Override // android.view.View.OnClickListener
            public void onClick(View view3) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, (Class<?>) WaterStatsActivity.class));
            }
        }), new LinearLayout.LayoutParams(-2, dp(40.0f)));
        LinearLayout linearLayout10 = new LinearLayout(this);
        linearLayout10.setOrientation(0);
        linearLayout10.setGravity(16);
        LinearLayout.LayoutParams matchWrap7 = matchWrap();
        matchWrap7.topMargin = dp(12.0f);
        card.addView(linearLayout10, matchWrap7);
        this.waterAmountView = text("", 32.0f, COLOR_BLUE, 1);
        this.waterAmountView.setIncludeFontPadding(false);
        linearLayout10.addView(this.waterAmountView, new LinearLayout.LayoutParams(-2, -2));
        this.waterTargetView = text("", 15.0f, COLOR_MUTED, 0);
        LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams7.leftMargin = dp(3.0f);
        linearLayout10.addView(this.waterTargetView, layoutParams7);
        SheenBar sheenBar2 = new SheenBar(this);
        this.waterProgressBar = sheenBar2;
        LinearLayout.LayoutParams layoutParams8 = new LinearLayout.LayoutParams(MATCH, dp(8.0f));
        layoutParams8.topMargin = dp(13.0f);
        card.addView(sheenBar2, layoutParams8);
        LinearLayout linearLayout11 = new LinearLayout(this);
        linearLayout11.setOrientation(0);
        linearLayout11.setGravity(16);
        LinearLayout.LayoutParams matchWrap8 = matchWrap();
        matchWrap8.topMargin = dp(16.0f);
        card.addView(linearLayout11, matchWrap8);
        Button button = new Button(this);
        this.waterDrinkButton = button;
        button.setTextSize(14.5f);
        button.setTextColor(COLOR_ON_BLUE);
        button.setAllCaps(false);
        button.setTypeface(Typeface.create("sans", 1));
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setPadding(dp(14.0f), 0, dp(14.0f), 0);
        button.setBackground(pillBlue());
        button.setElevation(dp(5.0f));
        applyShadowTint(button, COLOR_BLUE_SHADOW);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.18
            @Override // android.view.View.OnClickListener
            public void onClick(View view3) {
                MainActivity mainActivity = MainActivity.this;
                WaterReminderReceiver.drinkAndReschedule(mainActivity);
                mainActivity.lastNextWaterAt = 0L;
                MainActivity.this.refreshDisplay();
            }
        });
        linearLayout11.addView(button, new LinearLayout.LayoutParams(0, dp(48.0f), 1.0f));
        Button button2 = new Button(this);
        button2.setText("选饮品");
        button2.setTextSize(13.0f);
        button2.setTextColor(COLOR_SOFT_TEXT);
        button2.setAllCaps(false);
        button2.setMinWidth(0);
        button2.setMinHeight(0);
        button2.setPadding(dp(10.0f), 0, dp(10.0f), 0);
        button2.setBackground(UiKit.solid(COLOR_SOFT_BG, dp(24.0f), COLOR_SOFT_BORDER, dp(1.5f)));
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.19
            @Override // android.view.View.OnClickListener
            public void onClick(View view3) {
                MainActivity.this.showDrinkPicker();
            }
        });
        LinearLayout.LayoutParams layoutParams9 = new LinearLayout.LayoutParams(dp(92.0f), dp(48.0f));
        layoutParams9.leftMargin = dp(9.0f);
        linearLayout11.addView(button2, layoutParams9);
        this.waterNextView = text("", 12.0f, COLOR_MUTED, 0);
        LinearLayout.LayoutParams matchWrap9 = matchWrap();
        matchWrap9.topMargin = dp(11.0f);
        card.addView(this.waterNextView, matchWrap9);
        buildCumulativeCard();
    }

    private LinearLayout card() {
        return card(false);
    }

    private LinearLayout card(boolean z) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20.0f), dp(20.0f), dp(20.0f), dp(20.0f));
        boolean isNight = isNight();
        linearLayout.setBackground(UiKit.glowCard(COLOR_SURFACE, z ? COLOR_BLUE_BRIGHT : COLOR_ACCENT_BRIGHT, isNight ? 43 : 46, isNight ? 28 : 43, dp(24.0f)));
        linearLayout.setElevation(dp(4.0f));
        return linearLayout;
    }

    private boolean isNight() {
        return (getResources().getConfiguration().uiMode & 48) == 32;
    }

    static boolean isAppUnlocked() {
        return appUnlocked;
    }

    static int workdayCount(Context context) {
        try {
            SalarySettings load = SalarySettings.load(context.getSharedPreferences("salary_jump", 0));
            Calendar calendar = Calendar.getInstance();
            return WorkdayHelper.workdaysInMonth(context, load, calendar.get(1), calendar.get(2) + 1);
        } catch (Throwable th) {
            return 0;
        }
    }

    private LinearLayout heroCard() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        // 上下收窄：底部原来那条分隔线删了，20dp 底 padding 会留一块空白
        linearLayout.setPadding(dp(20.0f), dp(16.0f), dp(20.0f), dp(14.0f));
        linearLayout.setBackground(UiKit.glassCard(dp(28.0f)));
        linearLayout.setElevation(dp(6.0f));
        applyShadowTint(linearLayout);
        return linearLayout;
    }

    private void applyShadowTint(View view) {
        applyShadowTint(view, COLOR_HERO_SHADOW);
    }

    private void applyShadowTint(View view, int i) {
        if (Build.VERSION.SDK_INT >= 28) {
            view.setOutlineSpotShadowColor(i);
            view.setOutlineAmbientShadowColor(i);
        }
    }

    private TextView heroLabel(String str) {
        TextView text = text(str, 11.5f, HERO_SUB, 1);
        text.setLetterSpacing(0.12f);
        return text;
    }

    private View dot(int i) {
        View view = new View(this);
        view.setBackground(UiKit.circle(i));
        return view;
    }

    private GradientDrawable pill() {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, PILL_COLORS);
        gradientDrawable.setCornerRadius(dp(24.0f));
        return gradientDrawable;
    }

    private GradientDrawable pillBlue() {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, PILL_BLUE_COLORS);
        gradientDrawable.setCornerRadius(dp(24.0f));
        return gradientDrawable;
    }

    private TextView link(String str, View.OnClickListener onClickListener) {
        TextView text = text(str, 12.5f, COLOR_BLUE, 0);
        text.setGravity(17);
        text.setPadding(dp(8.0f), 0, dp(8.0f), 0);
        text.setOnClickListener(onClickListener);
        return text;
    }

    private TextView addStat(LinearLayout linearLayout, String str, int i, int i2) {
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        linearLayout2.setGravity(1);
        // 高度按内容走（原来写死 50dp，两行文字只需约 24dp，多出 26dp 空白堆在卡片底部）
        linearLayout.addView(linearLayout2, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView text = text("", 13.5f, i, 1);
        text.setGravity(17);
        linearLayout2.addView(text, matchWrap());
        TextView text2 = text(str, 10.5f, i2, 0);
        text2.setGravity(17);
        linearLayout2.addView(text2, matchWrap());
        this.lastStatLabel = text2;
        return text;
    }

    private void startTicker() {
        this.handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() { // from class: com.salaryjump.app.MainActivity.20
            @Override // java.lang.Runnable
            public void run() {
                MainActivity.this.refreshDisplay();
                MainActivity.this.handler.postDelayed(this, 1000L);
            }
        };
        this.ticker = runnable;
        this.handler.post(runnable);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void styleCap(TextView textView, int i) {
        Object tag = textView.getTag();
        if ((tag instanceof Integer) && ((Integer) tag).intValue() == i) {
            return;
        }
        textView.setTag(Integer.valueOf(i));
        Object background = textView.getBackground();
        if (background instanceof Animatable) {
            ((Animatable) background).stop();
        }
        int i2 = MATCH;
        if (i == 2) {
            Drawable glowPill = MotionKit.glowPill(dp(20.0f), NIGHT ? MATCH : -15884418, 0, NIGHT ? MATCH : -15419226, true);
            textView.setBackground(glowPill);
            if (glowPill instanceof Animatable) {
                ((Animatable) glowPill).start();
            }
            if (NIGHT) {
                i2 = -16098232;
            }
            textView.setTextColor(i2);
            textView.setTypeface(Typeface.create("sans", 1));
            return;
        }
        if (i == 1) {
            textView.setBackground(UiKit.solid(NIGHT ? 1560281087 : 639016573, dp(20.0f)));
            if (!NIGHT) {
                i2 = -16090526;
            }
            textView.setTextColor(i2);
            textView.setTypeface(Typeface.create("sans", 1));
            return;
        }
        textView.setBackground(UiKit.solid(NIGHT ? HERO_CHIP : 252328753, dp(20.0f)));
        textView.setTextColor(NIGHT ? -419430401 : -1559610575);
        textView.setTypeface(Typeface.create("sans", 0));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onCapLongClick(int i) {
        Calendar calendar = Calendar.getInstance();
        final int i2 = (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
        ArrayList arrayList = new ArrayList();
        final ArrayList arrayList2 = new ArrayList();
        if (i != 0) {
            if (i == 1) {
                if (LeaveRecord.has(this, i2)) {
                    arrayList.add("取消请假");
                    arrayList2.add(new Runnable() { // from class: com.salaryjump.app.MainActivity.22
                        @Override // java.lang.Runnable
                        public void run() {
                            LeaveRecord.remove(MainActivity.this, i2);
                            Toast.makeText(MainActivity.this, "已取消请假", 0).show();
                            SalaryWidgetProvider.refreshAll(MainActivity.this);
                            MainActivity.this.refreshDisplay();
                            MainActivity.this.updateCumulativeCard();
                        }
                    });
                }
            } else if (i == 2) {
                if (HolidayWork.has(this, i2)) {
                    arrayList.add("取消出勤标记");
                    arrayList2.add(new Runnable() { // from class: com.salaryjump.app.MainActivity.23
                        @Override // java.lang.Runnable
                        public void run() {
                            HolidayWork.remove(MainActivity.this, i2);
                            Toast.makeText(MainActivity.this, "已取消出勤记录", 0).show();
                            SalaryWidgetProvider.refreshAll(MainActivity.this);
                            WaterReminderScheduler.scheduleNext(MainActivity.this);
                            MainActivity.this.refreshDisplay();
                            MainActivity.this.updateCumulativeCard();
                            MainActivity.this.updateGoalView();
                        }
                    });
                }
            } else if (ExtraIncome.has(this, i2)) {
                arrayList.add("删除外快记录");
                arrayList2.add(new Runnable() { // from class: com.salaryjump.app.MainActivity.24
                    @Override // java.lang.Runnable
                    public void run() {
                        ExtraIncome.apply(MainActivity.this, i2, 0L, "");
                        Toast.makeText(MainActivity.this, "已删除外快记录", 0).show();
                        SalaryWidgetProvider.refreshAll(MainActivity.this);
                        MainActivity.this.refreshDisplay();
                        MainActivity.this.updateCumulativeCard();
                        MainActivity.this.updateGoalView();
                    }
                });
            }
        } else {
            OvertimeLog.Punch load = OvertimeLog.load(this);
            if (load.day == i2 && load.exists()) {
                arrayList.add("删除今天的加班记录");
                arrayList2.add(new Runnable() { // from class: com.salaryjump.app.MainActivity.21
                    @Override // java.lang.Runnable
                    public void run() {
                        MainActivity.this.deleteOvertimeToday();
                    }
                });
            }
        }
        if (arrayList.isEmpty()) {
            return;
        }
        new AlertDialog.Builder(this).setTitle("快捷操作").setItems((String[]) arrayList.toArray(new String[0]), new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.25
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i3) {
                ((Runnable) arrayList2.get(i3)).run();
            }
        }).setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
    }

    private void updateCaps() {
        boolean z;
        int i;
        if (this.caps == null || this.capsRow == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        int i2 = (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
        boolean isWorkday = WorkdayHelper.isWorkday(this, this.settings, calendar);
        boolean z2 = (calendar.get(11) * 60) + calendar.get(12) >= this.settings.workEndMinutes;
        int[] iArr = new int[4];
        OvertimeLog.Punch load = OvertimeLog.load(this);
        boolean z3 = load.day == i2 && load.exists();
        boolean z4 = z3 && load.running();
        if (isWorkday && this.settings.overtimeEnabled && !LeaveRecord.has(this, i2)) {
            z = z2 || z3;
        } else {
            z = false;
        }
        if (!z) {
            i = 0;
        } else {
            if (z4) {
                OvertimeLog.secondsOfDay(this, i2, calendar.getTimeInMillis());
                this.caps[0].setText("⏱ 加班中");
                styleCap(this.caps[0], 2);
            } else if (z3) {
                this.caps[0].setText("⏱ ¥" + this.moneyFormat.format(OvertimeCalc.earned(this.settings.dailySalary, SalaryCalculator.totalWorkSeconds(this.settings), OvertimeLog.secondsOfDay(this, i2, calendar.getTimeInMillis()), this.settings.overtimeRateX10)));
                styleCap(this.caps[0], 1);
            } else {
                this.caps[0].setText("⏱ 加班");
                styleCap(this.caps[0], 0);
            }
            iArr[0] = 0;
            i = 1;
        }
        if (isWorkday) {
            LeaveRecord.Entry entry = LeaveRecord.get(this, i2);
            TextView[] textViewArr = this.caps;
            if (entry != null) {
                textViewArr[1].setText("🏖 " + entry.name);
                styleCap(this.caps[1], 1);
            } else {
                textViewArr[1].setText("🏖 请假");
                styleCap(this.caps[1], 0);
            }
            iArr[i] = 1;
            i++;
        }
        if (!isWorkday) {
            boolean has = HolidayWork.has(this, i2);
            StatutoryHolidays.isStatutory(calendar);
            if (has) {
                boolean isComp = HolidayWork.isComp(this, i2);
                TextView[] textViewArr2 = this.caps;
                if (!isComp) {
                    textViewArr2[2].setText("✅ 出勤 " + HolidayWork.rateText(HolidayWork.rateX10Of(this, i2)));
                } else {
                    textViewArr2[2].setText("✅ 调休+1");
                }
                styleCap(this.caps[2], 1);
            } else {
                this.caps[2].setText("🏢 出勤");
                styleCap(this.caps[2], 0);
            }
            iArr[i] = 2;
            i++;
        }
        long centsOf = ExtraIncome.centsOf(this, i2);
        TextView[] textViewArr3 = this.caps;
        if (centsOf > 0) {
            textViewArr3[STATE_LUNCH].setText("💰 ¥" + this.moneyFormat.format(centsOf / 100.0d));
            styleCap(this.caps[STATE_LUNCH], 1);
        } else {
            textViewArr3[STATE_LUNCH].setText("💰 外快");
            styleCap(this.caps[STATE_LUNCH], 0);
        }
        int i3 = i + 1;
        iArr[i] = STATE_LUNCH;
        StringBuilder sb = new StringBuilder();
        for (int i4 = 0; i4 < i3; i4++) {
            sb.append(iArr[i4]).append(',');
        }
        String sb2 = sb.toString();
        if (!sb2.equals(this.lastCapLayout)) {
            this.lastCapLayout = sb2;
            this.capsRow.removeAllViews();
            for (int i5 = 0; i5 < i3; i5++) {
                TextView textView = this.caps[iArr[i5]];
                if (textView.getParent() != null) {
                    ((ViewGroup) textView.getParent()).removeView(textView);
                }
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(40.0f), 1.0f);
                if (i5 > 0) {
                    layoutParams.leftMargin = dp(7.0f);
                }
                this.capsRow.addView(textView, layoutParams);
            }
        }
    }

    public void refreshDisplay() {
        String str;
        Calendar calendar = Calendar.getInstance();
        SalarySnapshot calculate = SalaryCalculator.calculate(this, this.settings, calendar);
        if (this.amountIntro) {
            this.amountIntro = false;
            MotionKit.countUpMoney(this.amountView, calculate.earned, this.moneyFormat);
        } else {
            SpannableString spannableString = new SpannableString("¥ " + this.moneyFormat.format(calculate.earned));
            spannableString.setSpan(new RelativeSizeSpan(0.5f), 0, 1, 33);
            this.amountView.setText(spannableString);
        }
        this.progressBar.setProgress((float) calculate.progress);
        if (this.workerPetView != null) {
            this.workerPetView.setState(calculate.state);
            // 倍率的取值必须分三种情况：
            //   法定/计薪出勤 → holidayRateX10（30/20）
            //   调休出勤     → 无薪，传 0（不能回落到加班倍率！）
            //   普通加班     → overtimeRateX10
            int rateX10;
            if (calculate.state == STATE_HOLIDAY_WORK) {
                rateX10 = calculate.holidayRateX10;          // 调休时是 0
            } else if (calculate.state == STATE_OVERTIME) {
                rateX10 = this.settings.overtimeRateX10;
            } else {
                rateX10 = 0;
            }
            updateRateLabels(calculate.state, rateX10, calculate.holidayComp);
            int petFatigue = PetState.fatigueLevel(this);
            this.workerPetView.setFatigue(petFatigue);
            String moodText = WorkerPet.moodText(this.workerPetView.getDisplayPose());
            int daysToPayday = PetState.daysToPayday(this);
            // 小人那列窄，发薪倒数用「·5天」这种紧凑写法
            if (daysToPayday == 0) {
                moodText = "今天发薪 💰";
            } else if (daysToPayday > 0 && daysToPayday <= STATE_HOLIDAY_WORK) {
                moodText = moodText + " ·" + daysToPayday + " 天发薪";
            }
            if (this.petMoodView != null) {
                this.petMoodView.setText(moodText);
                this.petMoodView.setVisibility(moodText.length() == 0 ? 8 : 0);
            }
            fitAmountText();
            if (this.petFatigueView != null) {
                int iFatigue = petFatigue;
                if (iFatigue >= 2) {
                    this.petFatigueView.setText("疲劳 " + iFatigue + "/3\n歇一天回 2 分");
                    this.petFatigueView.setVisibility(0);
                } else {
                    this.petFatigueView.setVisibility(8);
                }
            }
        }
        int i = HERO_TEXT;
        if (calculate.state == 8) {
            i = Color.rgb(196, 181, 253);
            str = (calculate.leaveName == null ? "请假" : calculate.leaveName) + " · " + LeaveRecord.payText(calculate.leavePayPercent) + (calculate.leaveHalf ? " · 半天" : "") + " · 今日 " + this.moneyFormat.format(calculate.earned) + " 元";
        } else {
            if (calculate.state == STATE_HOLIDAY_WORK) {
                i = Color.rgb(253, 224, 71);
                String restNote = WorkdayHelper.restNote(this, this.settings, calendar);
                if (calculate.holidayComp) {
                    str = (restNote != null ? restNote : "休息日") + " · 出勤 调休";
                } else {
                    str = (restNote != null ? restNote : "休息日") + " · 出勤 " + HolidayWork.rateText(calculate.holidayRateX10) + " 结算";
                }
            } else if (calculate.state == 0) {
                i = HERO_SUB;
                String restNote2 = WorkdayHelper.restNote(this, this.settings, calendar);
                if (calculate.extraEarned > 0.0d) {
                    str = (restNote2 != null ? restNote2 : "休息日") + " · 今日外快 ¥" + this.moneyFormat.format(calculate.extraEarned);
                } else {
                    str = (restNote2 != null ? restNote2 : "休息日") + " · 今天不累计";
                }
            } else if (calculate.state == 1) {
                str = "还没上班 · 距开工 " + formatDuration(remainingSeconds(calendar, this.settings.workStartMinutes));
                i = Color.rgb(125, 211, 252);
            } else if (calculate.state == 2) {
                str = "工作中 · 距午休 " + formatDuration(remainingSeconds(calendar, this.settings.breakStartMinutes));
            } else if (calculate.state == STATE_LUNCH) {
                str = "午休中 · 距下午工作 " + formatDuration(remainingSeconds(calendar, this.settings.breakEndMinutes));
                i = Color.rgb(253, 230, 138);
            } else if (calculate.state == 4) {
                str = "工作中 · 距下班 " + formatDuration(remainingSeconds(calendar, this.settings.workEndMinutes));
            } else if (calculate.state == STATE_OVERTIME) {
                i = Color.rgb(253, 224, 71);
                str = "加班费 ¥" + this.moneyFormat.format(calculate.overtimeEarned) + " · " + (this.settings.overtimeRateX10 / 10.0f) + " 倍结算";
            } else {
                i = Color.rgb(125, 211, 252);
                str = "今日下班 · 工资已满额";
            }
        }
        SpannableString spannableString2 = new SpannableString("● " + str);
        spannableString2.setSpan(new ForegroundColorSpan(i), 0, 1, 33);
        this.statusView.setText(spannableString2);
        if (calendar.getTimeInMillis() - this.lastWaterUiUpdate >= 1000) {
            updateWaterCard();
            this.lastWaterUiUpdate = calendar.getTimeInMillis();
        }
        updateCumulativeCard();
        updateGoalView();
        updateCaps();
    }

    private void updateWaterCard() {
        AlarmManager alarmManager;
        WaterReminderSettings load = WaterReminderSettings.load(this);
        this.waterDrinkButton.setText("喝水 +" + load.cupMl + " ml");
        if (!load.enabled) {
            this.waterAmountView.setText("已关闭");
            this.waterAmountView.setTextColor(COLOR_MUTED);
            this.waterTargetView.setText("");
            this.waterProgressBar.setProgress(0.0f);
            this.waterNextView.setText("可在设置中开启");
            return;
        }
        int consumed = WaterReminderScheduler.getConsumed(this);
        boolean z = consumed >= load.targetMl;
        this.waterAmountView.setText(String.valueOf(consumed));
        this.waterAmountView.setTextColor(z ? COLOR_ACCENT : COLOR_BLUE);
        this.waterTargetView.setText(" / " + load.targetMl + " ml");
        this.waterProgressBar.setProgress(Math.min(consumed, load.targetMl) / Math.max(1, load.targetMl));
        long elapsedRealtime = SystemClock.elapsedRealtime();
        CharSequence charSequence = this.lastNextWaterText;
        if (charSequence == null || elapsedRealtime - this.lastNextWaterAt >= 30000) {
            NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
            charSequence = (notificationManager == null || notificationManager.areNotificationsEnabled()) ? (Build.VERSION.SDK_INT < 31 || (alarmManager = (AlarmManager) getSystemService("alarm")) == null || alarmManager.canScheduleExactAlarms()) ? WaterReminderScheduler.nextReminderText(this) : "⚠️ 精确闹钟未授权，提醒可能延迟" : "⚠️ 通知权限未开启，喝水提醒不会弹出";
            this.lastNextWaterText = charSequence;
            this.lastNextWaterAt = SystemClock.elapsedRealtime();
        }
        int i = DrinkTypes.todayKcal(this);
        int i2 = DrinkTypes.todaySugaryCups(this);
        String str = i > 0 ? " · 约 " + i + " kcal" : "";
        if (i2 > 0) {
            str = str + " · 含糖 " + i2 + " 杯";
        }
        this.waterNextView.setText(((Object) charSequence) + str);
    }

    private View drinkCell(final int i, boolean z) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setGravity(17);
        linearLayout.setPadding(dp(10.0f), dp(9.0f), dp(10.0f), dp(9.0f));
        if (z) {
            linearLayout.setBackground(rounded(COLOR_CHIP_GREEN_BG, 12, COLOR_ACCENT, 1));
        } else {
            linearLayout.setBackground(rounded(COLOR_BACKGROUND, 12, COLOR_BORDER, 1));
        }
        linearLayout.setClickable(true);
        TextView text = text(DrinkTypes.name(this, i), 12.5f, z ? COLOR_ACCENT : COLOR_TEXT, 1);
        text.setSingleLine(true);
        text.setEllipsize(TextUtils.TruncateAt.END);
        linearLayout.addView(text, matchWrap());
        TextView text2 = text(DrinkTypes.ml(this, i) + " ml · " + DrinkTypes.unit(this, i), 10.0f, z ? COLOR_ACCENT : COLOR_MUTED, 0);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(2.0f);
        linearLayout.addView(text2, matchWrap);
        linearLayout.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.26
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                WaterReminderReceiver.drinkAndReschedule(MainActivity.this, i);
                MainActivity.this.lastNextWaterAt = 0L;
                MainActivity.this.refreshDisplay();
                if (MainActivity.this.drinkDialog != null) {
                    MainActivity.this.drinkDialog.dismiss();
                }
            }
        });
        linearLayout.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.salaryjump.app.MainActivity.27
            @Override // android.view.View.OnLongClickListener
            public boolean onLongClick(View view) {
                MainActivity.this.showDrinkCellMenu(i);
                return true;
            }
        });
        MotionKit.press(linearLayout);
        return linearLayout;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showDrinkCellMenu(final int i) {
        AlertDialog.Builder items = new AlertDialog.Builder(this).setTitle(DrinkTypes.name(this, i)).setItems(new String[]{"补记（选时间）", "修改容量", "编辑饮品"}, new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.28
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                if (i2 == 0) {
                    if (MainActivity.this.drinkDialog != null) {
                        MainActivity.this.drinkDialog.dismiss();
                    }
                    MainActivity.this.showBackfillDialog(i);
                    return;
                }
                MainActivity mainActivity = MainActivity.this;
                if (i2 == 1) {
                    mainActivity.showDrinkMlDialog(i);
                    return;
                }
                if (mainActivity.drinkDialog != null) {
                    MainActivity.this.drinkDialog.dismiss();
                }
                MainActivity.this.showEditDrinkDialog(i);
            }
        });
        items.setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showDrinkMlDialog(final int i) {
        final EditText numberInputRaw = numberInputRaw(String.valueOf(DrinkTypes.ml(this, i)));
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        linearLayout.setGravity(16);
        linearLayout.setPadding(dp(20.0f), dp(8.0f), dp(20.0f), dp(4.0f));
        linearLayout.addView(numberInputRaw, new LinearLayout.LayoutParams(dp(100.0f), dp(44.0f)));
        linearLayout.addView(text(" ml", 13.0f, COLOR_MUTED, 0), wrapWrap());
        new AlertDialog.Builder(this).setTitle(DrinkTypes.name(this, i) + " · 单次容量").setView(linearLayout).setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("保存", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.29
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                DrinkTypes.setMl(MainActivity.this, i, MainActivity.this.parseMl(numberInputRaw.getText().toString(), DrinkTypes.ml(MainActivity.this, i)));
                Toast.makeText(MainActivity.this, "容量已更新", 0).show();
                MainActivity.this.showDrinkPicker();
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showEditDrinkDialog(final int i) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20.0f), dp(8.0f), dp(20.0f), dp(4.0f));
        linearLayout.addView(text("名称", 12.5f, COLOR_MUTED, 0), wrapWrap());
        final EditText editText = new EditText(this);
        editText.setText(DrinkTypes.name(this, i));
        editText.setTextSize(14.0f);
        editText.setTextColor(COLOR_TEXT);
        editText.setSingleLine(true);
        editText.setPadding(dp(12.0f), dp(10.0f), dp(12.0f), dp(10.0f));
        editText.setBackground(rounded(COLOR_BACKGROUND, 9, COLOR_BORDER, 1));
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(6.0f);
        linearLayout.addView(editText, matchWrap);
        linearLayout.addView(text("单位（1~2 字，如 杯 / 罐 / 瓶）", 12.5f, COLOR_MUTED, 0), wrapWrapTop(12.0f));
        final EditText editText2 = new EditText(this);
        editText2.setText(DrinkTypes.unit(this, i));
        editText2.setTextSize(14.0f);
        editText2.setTextColor(COLOR_TEXT);
        editText2.setSingleLine(true);
        editText2.setPadding(dp(12.0f), dp(10.0f), dp(12.0f), dp(10.0f));
        editText2.setBackground(rounded(COLOR_BACKGROUND, 9, COLOR_BORDER, 1));
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(6.0f);
        linearLayout.addView(editText2, matchWrap2);
        linearLayout.addView(text("每 100ml 热量（kcal）", 12.5f, COLOR_MUTED, 0), wrapWrapTop(12.0f));
        final EditText numberInputRaw = numberInputRaw(String.valueOf(DrinkTypes.kcal(this, i)));
        LinearLayout.LayoutParams matchWrap3 = matchWrap();
        matchWrap3.topMargin = dp(6.0f);
        linearLayout.addView(numberInputRaw, matchWrap3);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        LinearLayout.LayoutParams matchWrap4 = matchWrap();
        matchWrap4.topMargin = dp(12.0f);
        linearLayout.addView(linearLayout2, matchWrap4);
        linearLayout2.addView(text("含糖", 13.0f, COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, -2, 1.0f));
        final Switch r12 = new Switch(this);
        r12.setChecked(DrinkTypes.isSugary(this, i));
        linearLayout2.addView(r12, wrapWrap());
        new AlertDialog.Builder(this).setTitle("编辑「" + DrinkTypes.name(this, i) + "」").setView(linearLayout).setNegativeButton("取消", (DialogInterface.OnClickListener) null).setNeutralButton("删除", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.31
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                dialogInterface.dismiss();
                MainActivity.this.tryRemoveDrink(null, i);
            }
        }).setPositiveButton("保存", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.30
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                DrinkTypes.setName(MainActivity.this, i, editText.getText().toString());
                DrinkTypes.setUnit(MainActivity.this, i, editText2.getText().toString());
                try {
                    DrinkTypes.setKcal(MainActivity.this, i, Integer.parseInt(numberInputRaw.getText().toString().trim()));
                } catch (Throwable th) {
                }
                DrinkTypes.setSugary(MainActivity.this, i, r12.isChecked());
                Toast.makeText(MainActivity.this, "已保存", 0).show();
                MainActivity.this.showDrinkPicker();
            }
        }).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showDrinkPicker() {
        int count = DrinkTypes.count(this);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(18.0f), dp(6.0f), dp(18.0f), dp(6.0f));
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(linearLayout, new FrameLayout.LayoutParams(MATCH, -2));
        final AlertDialog create = new AlertDialog.Builder(this).setTitle("选择饮品").setView(scrollView).setNegativeButton("取消", (DialogInterface.OnClickListener) null).create();
        this.drinkDialog = create;
        List<Integer> frequentIds = DrinkTypes.frequentIds(this);
        ArrayList arrayList = new ArrayList();
        if (!frequentIds.isEmpty()) {
            linearLayout.addView(text("★ 常喝 · 近30天", 11.0f, COLOR_LABEL, 1), wrapWrap());
            GridCursor gridCursor = new GridCursor();
            for (Integer num : frequentIds) {
                addGridCell(linearLayout, gridCursor, drinkCell(num.intValue(), true));
                arrayList.add(num);
            }
            finishGrid(gridCursor);
            linearLayout.addView(text("全部饮品", 11.0f, COLOR_LABEL, 1), wrapWrapTop(13.0f));
        }
        GridCursor gridCursor2 = new GridCursor();
        for (int i = 0; i < count; i++) {
            int idAt = DrinkTypes.idAt(this, i);
            if (!arrayList.contains(Integer.valueOf(idAt))) {
                addGridCell(linearLayout, gridCursor2, drinkCell(idAt, false));
            }
        }
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        linearLayout2.setGravity(17);
        linearLayout2.setPadding(dp(10.0f), dp(9.0f), dp(10.0f), dp(9.0f));
        linearLayout2.setBackground(rounded(COLOR_BACKGROUND, 12, COLOR_BLUE, 1));
        linearLayout2.setClickable(true);
        linearLayout2.addView(text("＋ 添加", 12.5f, COLOR_BLUE, 1), matchWrap());
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(2.0f);
        linearLayout2.addView(text("新饮品", 10.0f, COLOR_MUTED, 0), matchWrap);
        linearLayout2.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.32
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                create.dismiss();
                MainActivity.this.showAddDrinkDialog();
            }
        });
        MotionKit.press(linearLayout2);
        addGridCell(linearLayout, gridCursor2, linearLayout2);
        finishGrid(gridCursor2);
        create.show();
    }

    private static class GridCursor {
        int index;
        LinearLayout row;

        GridCursor() {
        }
    }

    private void addGridCell(LinearLayout linearLayout, GridCursor gridCursor, View view) {
        if (gridCursor.index % 2 == 0) {
            LinearLayout linearLayout2 = new LinearLayout(this);
            linearLayout2.setOrientation(0);
            LinearLayout.LayoutParams matchWrap = matchWrap();
            matchWrap.topMargin = dp(7.0f);
            linearLayout.addView(linearLayout2, matchWrap);
            gridCursor.row = linearLayout2;
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(52.0f), 1.0f);
        if (gridCursor.index % 2 == 1) {
            layoutParams.leftMargin = dp(7.0f);
        }
        gridCursor.row.addView(view, layoutParams);
        gridCursor.index++;
    }

    private void finishGrid(GridCursor gridCursor) {
        if (gridCursor.index % 2 == 1 && gridCursor.row != null) {
            View view = new View(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(52.0f), 1.0f);
            layoutParams.leftMargin = dp(7.0f);
            gridCursor.row.addView(view, layoutParams);
        }
    }

    public void showBackfillDialog(final int i) {
        WaterReminderSettings.load(this);
        final TimePicker timePicker = new TimePicker(this);
        timePicker.setIs24HourView(Boolean.TRUE);
        Calendar calendar = Calendar.getInstance();
        timePicker.setCurrentHour(Integer.valueOf(calendar.get(11)));
        timePicker.setCurrentMinute(Integer.valueOf(calendar.get(12)));
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20.0f), dp(8.0f), dp(20.0f), dp(4.0f));
        linearLayout.addView(text(DrinkTypes.name(this, i) + " · " + DrinkTypes.ml(this, i) + " ml · 补记时间", 13.0f, COLOR_MUTED, 0), matchWrap());
        linearLayout.addView(timePicker, matchWrap());
        new AlertDialog.Builder(this).setTitle("补记喝水").setView(linearLayout).setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("记录", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.33
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                Calendar calendar2 = Calendar.getInstance();
                calendar2.set(11, timePicker.getHour());
                calendar2.set(12, timePicker.getMinute());
                calendar2.set(13, 0);
                calendar2.set(14, 0);
                if (calendar2.getTimeInMillis() > System.currentTimeMillis()) {
                    calendar2.add(5, MainActivity.MATCH);
                }
                WaterReminderScheduler.addDrinkAt(MainActivity.this, i, calendar2.getTimeInMillis());
                MainActivity.this.lastNextWaterAt = 0L;
                MainActivity.this.refreshDisplay();
                SalaryWidgetProvider.refreshAll(MainActivity.this);
                Toast.makeText(MainActivity.this, "已补记 " + DrinkTypes.name(MainActivity.this, i) + " " + DrinkTypes.ml(MainActivity.this, i) + " ml", 0).show();
            }
        }).show();
    }

    public void onExtraAction() {
        Calendar calendar = Calendar.getInstance();
        ExtraIncomeDialog.show(this, (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5), new Runnable() { // from class: com.salaryjump.app.MainActivity.34
            @Override // java.lang.Runnable
            public void run() {
                MainActivity.this.refreshDisplay();
                MainActivity.this.updateCumulativeCard();
                MainActivity.this.updateGoalView();
            }
        });
    }

    public void onLeaveAction() {
        Calendar calendar = Calendar.getInstance();
        int i = (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
        if (LeaveRecord.has(this, i)) {
            LeaveRecord.remove(this, i);
            Toast.makeText(this, "已取消请假标记", 0).show();
            SalaryWidgetProvider.refreshAll(this);
            refreshDisplay();
            return;
        }
        showLeaveTypeDialog(calendar);
    }

    private void showLeaveTypeDialog(final Calendar calendar) {
        String[] strArr = new String[LeaveRecord.TYPE_NAMES.length];
        for (int i = 0; i < LeaveRecord.TYPE_NAMES.length; i++) {
            strArr[i] = LeaveRecord.TYPE_NAMES[i] + " · " + LeaveRecord.payText(LeaveRecord.TYPE_PAY_PERCENT[i]);
            String remText = LeaveAccounts.remText(this, LeaveRecord.TYPE_NAMES[i]);
            if (remText != null) {
                strArr[i] = strArr[i] + " · " + remText;
            }
        }
        new AlertDialog.Builder(this).setTitle("选择请假类型").setItems(strArr, new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.35
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                MainActivity.this.showLeaveDurationDialog(calendar, LeaveRecord.TYPE_NAMES[i2], LeaveRecord.TYPE_PAY_PERCENT[i2]);
            }
        }).setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showLeaveDurationDialog(final Calendar calendar, final String str, final int i) {
        AlertDialog.Builder items = new AlertDialog.Builder(this).setTitle(str + " · " + LeaveRecord.payText(i)).setItems(new String[]{"整天", "半天"}, new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.36
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i2) {
                MainActivity.this.applyLeave(calendar, str, i, i2 == 1);
            }
        });
        items.setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyLeave(Calendar calendar, String str, int i, boolean z) {
        LeaveRecord.put(this, (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5), str, i, z);
        Toast.makeText(this, "已记为" + LeaveRecord.describe(str, i, z) + " · " + this.moneyFormat.format(LeaveRecord.earned(this.settings.dailySalary, i, z)) + " 元", 0).show();
        SalaryWidgetProvider.refreshAll(this);
        refreshDisplay();
    }

    public void onHolidayAction() {
        Calendar calendar = Calendar.getInstance();
        int i = (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
        if (HolidayWork.has(this, i)) {
            HolidayWork.remove(this, i);
            Toast.makeText(this, "已取消出勤记录", 0).show();
        } else {
            boolean isStatutory = StatutoryHolidays.isStatutory(calendar);
            if (WorkRules.useCompFor(this, isStatutory)) {
                HolidayWork.put(this, i, 0, true);
                Toast.makeText(this, "已记为出勤 · 调休 +1 天", 0).show();
            } else {
                int restDayRateX10 = WorkRules.restDayRateX10(this, isStatutory);
                HolidayWork.put(this, i, restDayRateX10, false);
                Toast.makeText(this, "已记为出勤 · " + HolidayWork.rateText(restDayRateX10), 0).show();
            }
        }
        SalaryWidgetProvider.refreshAll(this);
        WaterReminderScheduler.scheduleNext(this);
        refreshDisplay();
    }

    public void onOvertimeAction() {
        Calendar calendar = Calendar.getInstance();
        int i = (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
        OvertimeLog.Punch load = OvertimeLog.load(this);
        if (load.day == i && load.running()) {
            OvertimeLog.stop(this, System.currentTimeMillis());
            archiveTodayOvertime();
            Toast.makeText(this, "已结束加班，时长 " + OvertimeLog.format(OvertimeLog.secondsOfDay(this, i, System.currentTimeMillis())), 0).show();
        } else if (load.day == i && load.exists()) {
            AlertDialog.Builder message = new AlertDialog.Builder(this).setTitle("今天的加班记录").setMessage("已结束加班 " + OvertimeLog.format(OvertimeLog.secondsOfDay(this, i, System.currentTimeMillis())) + "，要重新开始还是删除这条记录？");
            message.setNegativeButton("取消", (DialogInterface.OnClickListener) null).setNeutralButton("删除记录", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.38
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i2) {
                    MainActivity.this.deleteOvertimeToday();
                }
            }).setPositiveButton("重来", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.37
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i2) {
                    OvertimeLog.clear(MainActivity.this);
                    MainActivity.this.startOvertime();
                }
            }).show();
        } else {
            startOvertime();
        }
        this.lastNextWaterAt = 0L;
        refreshDisplay();
    }

    public void deleteOvertimeToday() {
        Calendar calendar = Calendar.getInstance();
        int i = (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
        double earned = OvertimeCalc.earned(this.settings.dailySalary, SalaryCalculator.totalWorkSeconds(this.settings), OvertimeLog.secondsOfDay(this, i, System.currentTimeMillis()), this.settings.overtimeRateX10);
        OvertimeLog.clear(this);
        ArchiveSync.onOvertimeRemoved(this, i, Math.round(earned * 100.0d));
        Toast.makeText(this, "已删除今天的加班记录", 0).show();
        SalaryWidgetProvider.refreshAll(this);
        refreshDisplay();
        updateCumulativeCard();
        updateGoalView();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startOvertime() {
        Calendar calendar = Calendar.getInstance();
        int i = (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
        Calendar calendar2 = (Calendar) calendar.clone();
        calendar2.set(11, this.settings.workEndMinutes / 60);
        calendar2.set(12, this.settings.workEndMinutes % 60);
        calendar2.set(13, 0);
        calendar2.set(14, 0);
        calendar2.add(13, this.settings.overtimeGraceMinutes * 60);
        OvertimeLog.start(this, i, calendar2.getTimeInMillis());
        Toast.makeText(this, "开始计加班，起点 " + formatTime(this.settings.workEndMinutes + this.settings.overtimeGraceMinutes), 0).show();
    }

    private void archiveTodayOvertime() {
        Calendar calendar = Calendar.getInstance();
        if (!WorkdayHelper.isWorkday(this, this.settings, calendar)) {
            return;
        }
        int i = (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
        SalaryHistory.put(this, i, Math.round((this.settings.dailySalary + OvertimeCalc.earned(this.settings.dailySalary, SalaryCalculator.totalWorkSeconds(this.settings), OvertimeLog.secondsOfDay(this, i, System.currentTimeMillis()), this.settings.overtimeRateX10)) * 100.0d) + ExtraIncome.centsOf(this, i));
    }

    private EditText numberInputRaw(String str) {
        EditText editText = new EditText(this);
        editText.setInputType(2);
        editText.setText(str);
        editText.setTextSize(14.0f);
        editText.setTextColor(COLOR_TEXT);
        editText.setGravity(17);
        editText.setSingleLine(true);
        editText.setPadding(dp(8.0f), 0, dp(8.0f), 0);
        editText.setBackground(rounded(COLOR_BACKGROUND, 8, COLOR_BORDER, 1));
        return editText;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int parseMl(String str, int i) {
        try {
            int parseInt = Integer.parseInt(str.trim());
            if (parseInt < 10 || parseInt > 2000) {
                return i;
            }
            return parseInt;
        } catch (NumberFormatException e) {
            return i;
        }
    }

    public void tryRemoveDrink(final Dialog dialog, final int i) {
        String removeCheck = DrinkTypes.removeCheck(this, i);
        if (removeCheck != null) {
            new AlertDialog.Builder(this).setTitle("不能删除").setMessage(removeCheck).setPositiveButton("知道了", (DialogInterface.OnClickListener) null).show();
        } else {
            new AlertDialog.Builder(this).setTitle("删除饮品").setMessage("确定删除「" + DrinkTypes.name(this, i) + "」吗？\n\n该饮品还没有喝过，删除不会影响任何记录。").setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("删除", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.39
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i2) {
                    DrinkTypes.remove(MainActivity.this, i);
                    dialog.dismiss();
                    MainActivity.this.refreshDisplay();
                    Toast.makeText(MainActivity.this, "已删除", 0).show();
                    MainActivity.this.showDrinkPicker();
                }
            }).show();
        }
    }

    public void showAddDrinkDialog() {
        List<DrinkTypes.Preset> availablePresets = DrinkTypes.availablePresets(this);
        if (availablePresets.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("没有可添加的饮品").setMessage("参考表里的饮品都已经添加过了。\n\n可以到「设置 → 参考表」里新增条目，再回来添加。").setPositiveButton("知道了", (DialogInterface.OnClickListener) null).show();
            return;
        }
        LinearLayout linearLayout = new LinearLayout(this);
        int i = 1;
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(20.0f), dp(6.0f), dp(20.0f), dp(4.0f));
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(linearLayout, new FrameLayout.LayoutParams(MATCH, -2));
        final AlertDialog create = new AlertDialog.Builder(this).setTitle("添加饮品").setView(scrollView).setNegativeButton("取消", (DialogInterface.OnClickListener) null).create();
        for (final DrinkTypes.Preset preset : availablePresets) {
            LinearLayout linearLayout2 = new LinearLayout(this);
            linearLayout2.setOrientation(0);
            linearLayout2.setGravity(16);
            linearLayout2.setPadding(dp(14.0f), 0, dp(10.0f), 0);
            linearLayout2.setBackground(rounded(COLOR_BACKGROUND, 10, COLOR_BORDER, i));
            linearLayout2.addView(text(preset.name, 14.0f, COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, dp(46.0f), 1.0f));
            TextView text = text(preset.kcal + " kcal", 12.0f, COLOR_MUTED, 0);
            text.setGravity(21);
            linearLayout2.addView(text, new LinearLayout.LayoutParams(dp(62.0f), dp(46.0f)));
            TextView text2 = text(preset.sugar ? "含糖" : "无糖", 11.5f, preset.sugar ? COLOR_AMBER : COLOR_ACCENT, 0);
            text2.setGravity(21);
            linearLayout2.addView(text2, new LinearLayout.LayoutParams(dp(44.0f), dp(46.0f)));
            linearLayout2.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.40
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    create.dismiss();
                    MainActivity.this.showDrinkFromPresetDialog(preset);
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH, -2);
            layoutParams.topMargin = dp(6.0f);
            linearLayout.addView(linearLayout2, layoutParams);
            i = 1;
        }
        TextView text3 = text("只有参考表里的饮品可以添加；可在「设置 → 参考表」里增删改。", 11.5f, COLOR_LABEL, 0);
        text3.setLineSpacing(dp(2.0f), 1.0f);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(10.0f);
        linearLayout.addView(text3, matchWrap);
        create.show();
    }

    public void showDrinkFromPresetDialog(final DrinkTypes.Preset preset) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(24.0f), dp(10.0f), dp(24.0f), dp(4.0f));
        TextView text = text(preset.name + " · " + preset.kcal + " kcal/100ml · " + (preset.sugar ? "含糖" : "无糖"), 13.0f, COLOR_MUTED, 0);
        text.setBackground(rounded(COLOR_BACKGROUND, 10, COLOR_BORDER, 1));
        text.setPadding(dp(12.0f), dp(10.0f), dp(12.0f), dp(10.0f));
        linearLayout.addView(text, matchWrap());
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(0);
        linearLayout2.setGravity(16);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(12.0f);
        linearLayout.addView(linearLayout2, matchWrap);
        final EditText editText = new EditText(this);
        editText.setText("杯");
        editText.setTextSize(15.0f);
        editText.setTextColor(COLOR_TEXT);
        editText.setGravity(17);
        editText.setSingleLine(true);
        editText.setPadding(dp(8.0f), dp(10.0f), dp(8.0f), dp(10.0f));
        editText.setBackground(rounded(COLOR_BACKGROUND, 8, COLOR_BORDER, 1));
        linearLayout2.addView(editText, new LinearLayout.LayoutParams(0, dp(46.0f), 1.0f));
        TextView text2 = text("单位", 13.0f, COLOR_MUTED, 0);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.leftMargin = dp(10.0f);
        linearLayout2.addView(text2, layoutParams);
        final EditText editText2 = new EditText(this);
        editText2.setText("250");
        editText2.setInputType(2);
        editText2.setTextSize(15.0f);
        editText2.setTextColor(COLOR_TEXT);
        editText2.setGravity(17);
        editText2.setSingleLine(true);
        editText2.setPadding(dp(8.0f), dp(10.0f), dp(8.0f), dp(10.0f));
        editText2.setBackground(rounded(COLOR_BACKGROUND, 8, COLOR_BORDER, 1));
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, dp(46.0f), 1.0f);
        layoutParams2.leftMargin = dp(12.0f);
        linearLayout2.addView(editText2, layoutParams2);
        TextView text3 = text("ml", 13.0f, COLOR_MUTED, 0);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams3.leftMargin = dp(10.0f);
        linearLayout2.addView(text3, layoutParams3);
        new AlertDialog.Builder(this).setTitle("添加「" + preset.name + "」").setView(linearLayout).setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("添加", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.41
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                String addCustom = DrinkTypes.addCustom(MainActivity.this, preset.name, editText.getText().toString(), MainActivity.this.parseMl(editText2.getText().toString(), 250));
                MainActivity mainActivity = MainActivity.this;
                if (addCustom == null) {
                    DrinkTypes.setKcal(mainActivity, DrinkTypes.idAt(MainActivity.this, DrinkTypes.count(MainActivity.this) - 1), preset.kcal);
                    DrinkTypes.setSugary(MainActivity.this, DrinkTypes.idAt(MainActivity.this, DrinkTypes.count(MainActivity.this) - 1), preset.sugar);
                    Toast.makeText(MainActivity.this, "已添加「" + preset.name + "」", 0).show();
                    MainActivity.this.refreshDisplay();
                    MainActivity.this.showDrinkPicker();
                    return;
                }
                Toast.makeText(mainActivity, addCustom, 1).show();
            }
        }).show();
    }

    /**
     * 按可用宽度给「工作节奏」挑字号（8–11sp），保证一行放得下。
     *
     * <p>这一行现在和「今日已赚」「时钟」挤在一起，宽度很紧。
     * 用测量 + 反推字号，比 autosize 更可控（autosize 在单行 TextView 上行为不稳）。
     */
    private void fitScheduleText(String value) {
        if (this.scheduleView == null) {
            return;
        }
        try {
            int availPx = this.scheduleView.getWidth();
            float availDp = availPx > 0 ? availPx / this.density : 0.0f;
            if (availDp <= 0.0f) {
                return;                       // 还没测量出来，下次刷新再调
            }
            Paint measure = new Paint();
            measure.setTextSize(100.0f);
            float widthAt100 = measure.measureText(value);
            if (widthAt100 <= 0.0f) {
                return;
            }
            // 需要多小的字号才能塞进 availPx（按 px 估算）
            float needPx = availPx * 100.0f / widthAt100;
            float minPx = 8.0f * this.density;
            float maxPx = 11.0f * this.density;
            float sizePx = Math.max(minPx, Math.min(maxPx, needPx));
            this.scheduleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizePx);
        } catch (Throwable unused) {
        }
    }

    public void updateRateLabels() {
        updateRateLabels(-1, 0, false);
    }

    public void updateRateLabels(int state, int rateX10) {
        updateRateLabels(state, rateX10, false);
    }

    /**
     * 更新费率栏。
     *
     * @param state       当前状态；-1 表示还不知道（按「不显示」处理）
     * @param rateX10     出勤倍率 ×10（加班 15 / 计薪周末 20 / 法定 30）
     * @param holidayComp 是否「调休出勤」—— 无薪，费率要灰掉
     */
    public void updateRateLabels(int state, int rateX10, boolean holidayComp) {
        double d = SalaryCalculator.totalWorkSeconds(this.settings);
        double d2 = d > 0.0d ? this.settings.dailySalary / d : 0.0d;
        double d3 = d2 * 60.0d;

        this.targetView.setText("¥" + this.moneyFormat.format(this.settings.dailySalary));

        // 标签：普通「每小时」/「加班 1.5 倍」/「2 倍 结算」/「调休 · 不计薪」
        if (this.hourlyLabelView != null) {
            this.hourlyLabelView.setText(RateDisplay.labelFor(state, rateX10, holidayComp));
        }

        // 费率只在「真的在赚」时显示；其余时刻灰掉，避免下班/周末还在暗示在赚钱
        if (RateDisplay.modeFor(state, holidayComp) == RateDisplay.SHOW) {
            double hourly = RateDisplay.hourlyFor(60.0d * d3, state, rateX10, holidayComp);
            this.hourlyView.setText("¥" + this.moneyFormat.format(hourly));
            this.minuteView.setText("¥" + this.rateFormat.format(hourly / 60.0d));
            this.secondView.setText("¥" + this.secondFormat.format(hourly / 3600.0d));
            setRateLabelsDimmed(false);
        } else {
            String dash = RateDisplay.dimText();
            this.hourlyView.setText(dash);
            this.minuteView.setText(dash);
            this.secondView.setText(dash);
            setRateLabelsDimmed(true);
        }
        String scheduleText = (this.settings.workMode == 2 ? "智能 · " : this.settings.workMode == 1 ? "周一至五 · " : "")
                + formatTime(this.settings.workStartMinutes) + "-"
                + formatTime(this.settings.workEndMinutes) + " · 午休 "
                + formatTime(this.settings.breakStartMinutes) + "-"
                + formatTime(this.settings.breakEndMinutes);
        this.scheduleView.setText(scheduleText);
        fitScheduleText(scheduleText);
    }

    /** 费率栏的「灰掉」样式（显示中 / 不显示）。 */
    private void setRateLabelsDimmed(boolean dimmed) {
        int color = dimmed ? HERO_SUB2 : HERO_TEXT;
        try {
            this.hourlyView.setTextColor(color);
            this.minuteView.setTextColor(color);
            this.secondView.setTextColor(color);
        } catch (Throwable unused) {
        }
    }

    public void showSettingsDialog() {
        try {
            startActivity(new Intent(this, (Class<?>) SettingsActivity.class));
        } catch (Throwable th) {
            Toast.makeText(this, "无法打开设置：" + th.getMessage(), 0).show();
        }
    }

    public void requestCalendarPermissionIfNeeded() {
        if (!this.settings.useCalendar || checkSelfPermission("android.permission.READ_CALENDAR") == 0) {
            return;
        }
        requestPermissions(new String[]{"android.permission.READ_CALENDAR"}, 4301);
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i2 != MATCH || intent == null || intent.getData() == null) {
            return;
        }
        if (i == REQ_BACKUP_EXPORT) {
            writeBackup(intent.getData());
        } else if (i == REQ_BACKUP_IMPORT) {
            readBackup(intent.getData());
        } else if (i == REQ_BACKUP_TREE) {
            // 兜底分支：设置对话框目前只在 SettingsActivity 里创建，
            // 所以实际会走 SettingsActivity.onActivityResult。
            // 这里留着是为了万一以后 MainActivity 也直接用这个对话框。
            onBackupTreePicked(intent.getData());
        }
    }

    /**
     * 用户选好自动备份的文件夹。
     *
     * <p>关键：拿 {@code takePersistableUriPermission} 把授权**长期记住**，
     * 之后自动备份就能往这个目录写，不用再弹选择框。
     */
    private void onBackupTreePicked(Uri uri) {
        String label = BackupScheduler.adoptTree(this, uri);
        if (label == null) {
            Toast.makeText(this, "无法使用该文件夹，请换一个试试", 1).show();
            return;
        }
        Toast.makeText(this, "自动备份已开启\n目录：" + label, 1).show();
        updateGoalView();
    }

    private void writeBackup(Uri uri) {
        try {
            OutputStream openOutputStream = getContentResolver().openOutputStream(uri);
            if (openOutputStream == null) {
                Toast.makeText(this, "无法写入该位置", 1).show();
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("{\"app\":\"SalaryJump\",\"version\":2,\"savedAt\":").append(System.currentTimeMillis()).append(",\"data\":{");
            boolean z = true;
            for (Map.Entry<String, ?> entry : this.preferences.getAll().entrySet()) {
                Object value = entry.getValue();
                if (value != null) {
                    if (!z) {
                        sb.append(',');
                    }
                    sb.append('\"').append(escapeJson(entry.getKey())).append("\":");
                    if (value instanceof String) {
                        sb.append('\"').append(escapeJson((String) value)).append('\"');
                    } else if (value instanceof Boolean) {
                        sb.append(value.toString());
                    } else {
                        sb.append(value.toString());
                    }
                    z = false;
                }
            }
            sb.append("}}");
            openOutputStream.write(sb.toString().getBytes("UTF-8"));
            openOutputStream.close();
            Toast.makeText(this, "已导出 " + this.preferences.getAll().size() + " 项数据", 0).show();
        } catch (Throwable th) {
            Toast.makeText(this, "导出失败：" + th.getMessage(), 1).show();
        }
    }

    private void readBackup(Uri uri) {
        try {
            InputStream openInputStream = getContentResolver().openInputStream(uri);
            if (openInputStream == null) {
                Toast.makeText(this, "无法读取该文件", 1).show();
                return;
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] bArr = new byte[4096];
            while (true) {
                int read = openInputStream.read(bArr);
                if (read <= 0) {
                    break;
                } else {
                    byteArrayOutputStream.write(bArr, 0, read);
                }
            }
            openInputStream.close();
            final JSONObject optJSONObject = new JSONObject(new String(byteArrayOutputStream.toByteArray(), "UTF-8")).optJSONObject("data");
            if (optJSONObject == null) {
                Toast.makeText(this, "文件格式不对，不是 SalaryJump 的备份", 1).show();
                return;
            }
            final int length = optJSONObject.length();
            new AlertDialog.Builder(this).setTitle("恢复数据").setMessage("将从备份恢复 " + length + " 项数据，覆盖当前设置与记录。\n\n此操作不可撤销，确定继续？").setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("恢复", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.42
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.applyBackup(optJSONObject, length);
                }
            }).show();
        } catch (Throwable th) {
            Toast.makeText(this, "读取失败：" + th.getMessage(), 1).show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onHolidayDataUpdated() {
        WorkdayHelper.applyOnlineData(this);
        StatutoryHolidays.applyOnlineData(this);
        WorkdayHelper.invalidate();
        SalaryWidgetProvider.refreshAll(this);
        refreshDisplay();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void applyBackup(JSONObject jSONObject, int i) {
        SharedPreferences.Editor edit = this.preferences.edit();
        Iterator<String> keys = jSONObject.keys();
        while (keys.hasNext()) {
            String next = keys.next();
            Object opt = jSONObject.opt(next);
            if (opt instanceof String) {
                edit.putString(next, (String) opt);
            } else if (opt instanceof Boolean) {
                edit.putBoolean(next, ((Boolean) opt).booleanValue());
            } else if (opt instanceof Integer) {
                edit.putInt(next, ((Integer) opt).intValue());
            } else if (opt instanceof Long) {
                edit.putLong(next, ((Long) opt).longValue());
            } else if (opt instanceof Number) {
                edit.putLong(next, ((Number) opt).longValue());
            }
        }
        edit.apply();
        WorkdayHelper.invalidate();
        this.settings = SalarySettings.load(this.preferences);
        this.lastNextWaterAt = 0L;
        WaterReminderScheduler.scheduleNext(this);
        SalaryWidgetProvider.refreshAll(this);
        refreshDisplay();
        updateRateLabels();
        Toast.makeText(this, "已恢复 " + i + " 项数据", 0).show();
    }

    private static String escapeJson(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (charAt == '\"') {
                sb.append("\\\"");
            } else if (charAt == '\\') {
                sb.append("\\\\");
            } else if (charAt == '\n') {
                sb.append("\\n");
            } else if (charAt == '\r') {
                sb.append("\\r");
            } else if (charAt == '\t') {
                sb.append("\\t");
            } else if (charAt < ' ') {
                sb.append(String.format("\\u%04x", Integer.valueOf(charAt)));
            } else {
                sb.append(charAt);
            }
        }
        return sb.toString();
    }

    @Override // android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 4301) {
            WorkdayHelper.invalidate();
            refreshDisplay();
            SalaryWidgetProvider.refreshAll(this);
        }
    }

    public void requestExactAlarmPermissionIfNeeded() {
        AlarmManager alarmManager;
        if (Build.VERSION.SDK_INT < 31 || !WaterReminderSettings.load(this).enabled || (alarmManager = (AlarmManager) getSystemService("alarm")) == null || alarmManager.canScheduleExactAlarms()) {
            return;
        }
        try {
            Intent intent = new Intent("android.settings.REQUEST_SCHEDULE_EXACT_ALARM");
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Throwable th) {
        }
    }

    private void showStartupError(Throwable th) {
        Runnable runnable;
        Handler handler = this.handler;
        if (handler != null && (runnable = this.ticker) != null) {
            handler.removeCallbacks(runnable);
        }
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        linearLayout.setPadding(dp(22.0f), dp(28.0f), dp(22.0f), dp(28.0f));
        linearLayout.setBackgroundColor(COLOR_BACKGROUND);
        linearLayout.addView(text("启动失败", 24.0f, COLOR_TEXT, 1));
        TextView text = text(th.getClass().getSimpleName() + "\n" + String.valueOf(th.getMessage()), 14.0f, COLOR_MUTED, 0);
        text.setPadding(0, dp(16.0f), 0, 0);
        text.setTextIsSelectable(true);
        linearLayout.addView(text);
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.addView(linearLayout, new FrameLayout.LayoutParams(MATCH, -2));
        setContentView(scrollView);
    }

    private TextView text(String str, float f, int i, int i2) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(f);
        textView.setTextColor(i);
        textView.setTypeface(Typeface.create("sans", i2));
        return textView;
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

    private LinearLayout.LayoutParams wrapWrapTop(float f) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.topMargin = dp(f);
        return layoutParams;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(MATCH, -2);
    }

    private LinearLayout.LayoutParams wrapWrap() {
        return new LinearLayout.LayoutParams(-2, -2);
    }

    static GradientDrawable softPill(float f, float f2) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(COLOR_SOFT_BG);
        gradientDrawable.setCornerRadius(f);
        gradientDrawable.setStroke(Math.round(f2), COLOR_SOFT_BORDER);
        return gradientDrawable;
    }

    static GradientDrawable softCard(float f) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(COLOR_SURFACE);
        gradientDrawable.setCornerRadius(f);
        return gradientDrawable;
    }

    /** 按设置调整小人：显示与否、尺寸档位、是否画工位场景。 */
    private void applyPetSettings() {
        try {
            boolean visible = PetSettings.visible(this);
            if (this.petColumn != null) {
                this.petColumn.setVisibility(visible ? 0 : 8);
            }
            if (!visible || this.workerPetView == null || this.petColumn == null) {
                return;
            }
            int requestedDp = PetSettings.widthDp(this);
            int widthDp = HeroFit.petWidthDp(heroRowWidthDp(), requestedDp);
            int width = dp(widthDp);
            ViewGroup.LayoutParams columnParams = this.petColumn.getLayoutParams();
            if (columnParams != null && columnParams.width != width) {
                columnParams.width = width;
                this.petColumn.setLayoutParams(columnParams);
            }
            int height = dp(HeroFit.petHeightDp(widthDp));
            ViewGroup.LayoutParams petParams = this.workerPetView.getLayoutParams();
            if (petParams != null && (petParams.width != width || petParams.height != height)) {
                petParams.width = width;
                petParams.height = height;
                this.workerPetView.setLayoutParams(petParams);
            }
            this.workerPetView.setScene(PetSettings.scene(this));
        } catch (Throwable unused) {
        }
    }

    /**
     * 金额那一行的可用宽度（dp）。
     *
     * 直接按屏幕宽度减掉两侧留白算出来 —— 不用 OnLayoutChangeListener，
     * 避免「在布局过程中修改布局参数」这类风险（真机上出现过闪退）。
     *
     * 393dp 机型 → 393 - 20×2（root padding）- 20×2（卡内 padding）= 313dp。
     */
    /**
     * 上次崩溃过就弹出来 —— 崩溃不该是静默的。
     * 同一条崩溃只提示一次（记 crash_shown_at）。
     */
    private void showPendingCrash() {
        try {
            long crashAt = CrashLogger.lastCrashAt(this);
            if (crashAt <= 0L) {
                return;
            }
            long shownAt = this.preferences.getLong("crash_shown_at", 0L);
            if (crashAt <= shownAt) {
                return;
            }
            this.preferences.edit().putLong("crash_shown_at", crashAt).apply();
            final String detail = CrashLogger.lastCrashText(this);
            View content = findViewById(16908290);   // android.R.id.content
            Runnable show = new Runnable() { // from class: com.salaryjump.app.MainActivity.41
                @Override // java.lang.Runnable
                public void run() {
                    try {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("⚠️ 上次崩溃了")
                                .setMessage(detail == null ? "（无详情）" : detail)
                                .setPositiveButton("知道了", (DialogInterface.OnClickListener) null)
                                .show();
                    } catch (Throwable unused) {
                    }
                }
            };
            // 等窗口就绪再弹，避免 onCreate 期间添加窗口失败
            if (content != null) {
                content.post(show);
            }
        } catch (Throwable unused) {
        }
    }

    private int heroRowWidthDp() {
        try {
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            if (displayMetrics == null || displayMetrics.density <= 0.0f) {
                return 0;
            }
            int screenDp = Math.round(displayMetrics.widthPixels / displayMetrics.density);
            return Math.max(0, screenDp - 80);
        } catch (Throwable unused) {
            return 0;
        }
    }

    /**
     * 按实际可用宽度给金额挑字号 —— 保证「¥ 12345.67」这类长金额永远放得下，
     * 不会被人或小人挤掉一截。
     */
    private void fitAmountText() {
        try {
            if (this.amountView == null) {
                return;
            }
            int availPx = this.amountView.getWidth();
            if (availPx <= 0) {
                return;
            }
            CharSequence text = this.amountView.getText();
            if (text == null || text.length() == 0) {
                return;
            }
            Paint measure = new Paint();
            measure.setTypeface(this.amountView.getTypeface());
            float refPx = 100.0f;
            measure.setTextSize(refPx);
            float widthAtRef = measure.measureText(text.toString());
            float sizePx = HeroFit.amountTextSizePx(availPx, widthAtRef, refPx, this.density);
            if (Math.abs(sizePx - this.amountTextSizePx) > 0.5f) {
                this.amountTextSizePx = sizePx;
                this.amountView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizePx);
            }
        } catch (Throwable unused) {
        }
    }

    private int dp(float f) {
        return Math.round(f * this.density);
    }

    /**
     * 距离目标时刻还有多少秒（向上取整，最小 0）。
     *
     * <p>原来返回的是「分钟」，秒被抹掉了 —— 所以「距下班」只能显示到分钟，
     * 在最后 59 秒里数字是静止的。改成按秒算。
     */
    private static int remainingSeconds(Calendar calendar, int i) {
        double nowSeconds = (calendar.get(11) * 3600.0d) + (calendar.get(12) * 60.0d)
                + calendar.get(13) + (calendar.get(14) / 1000.0d);
        return (int) Math.ceil(Math.max(0.0d, (i * 60.0d) - nowSeconds));
    }

    /** 秒数 → 「时:分:秒」；不足 1 小时显示「分:秒」。 */
    static String formatDuration(int seconds) {
        if (seconds < 0) {
            seconds = 0;
        }
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        if (h > 0) {
            return String.format(Locale.US, "%d:%02d:%02d",
                    Integer.valueOf(h), Integer.valueOf(m), Integer.valueOf(s));
        }
        return String.format(Locale.US, "%02d:%02d",
                Integer.valueOf(m), Integer.valueOf(s));
    }

    public static String formatTime(int i) {
        int i2 = ((i % 1440) + 1440) % 1440;
        return String.format(Locale.US, "%02d:%02d", Integer.valueOf(i2 / 60), Integer.valueOf(i2 % 60));
    }

    static class SalarySettings {
        double dailySalary = 300.0d;
        int workStartMinutes = 480;
        int workEndMinutes = 1020;
        int breakStartMinutes = 690;
        int breakEndMinutes = 750;
        int workMode = 2;
        boolean useCalendar = false;
        boolean overtimeEnabled = false;
        int overtimeRateX10 = 15;
        int overtimeGraceMinutes = 30;

        SalarySettings() {
        }

        SalarySettings copy() {
            SalarySettings salarySettings = new SalarySettings();
            salarySettings.dailySalary = this.dailySalary;
            salarySettings.workStartMinutes = this.workStartMinutes;
            salarySettings.workEndMinutes = this.workEndMinutes;
            salarySettings.breakStartMinutes = this.breakStartMinutes;
            salarySettings.breakEndMinutes = this.breakEndMinutes;
            salarySettings.workMode = this.workMode;
            salarySettings.useCalendar = this.useCalendar;
            salarySettings.overtimeEnabled = this.overtimeEnabled;
            salarySettings.overtimeRateX10 = this.overtimeRateX10;
            salarySettings.overtimeGraceMinutes = this.overtimeGraceMinutes;
            return salarySettings;
        }

        static SalarySettings load(SharedPreferences sharedPreferences) {
            SalarySettings salarySettings = new SalarySettings();
            salarySettings.dailySalary = Double.longBitsToDouble(sharedPreferences.getLong("daily_salary_bits", Double.doubleToRawLongBits(salarySettings.dailySalary)));
            salarySettings.workStartMinutes = sharedPreferences.getInt("work_start", salarySettings.workStartMinutes);
            salarySettings.workEndMinutes = sharedPreferences.getInt("work_end", salarySettings.workEndMinutes);
            salarySettings.breakStartMinutes = sharedPreferences.getInt("break_start", salarySettings.breakStartMinutes);
            salarySettings.breakEndMinutes = sharedPreferences.getInt("break_end", salarySettings.breakEndMinutes);
            salarySettings.workMode = sharedPreferences.getInt("work_mode", salarySettings.workMode);
            salarySettings.useCalendar = sharedPreferences.getBoolean("use_calendar", salarySettings.useCalendar);
            salarySettings.overtimeEnabled = sharedPreferences.getBoolean("overtime_enabled", salarySettings.overtimeEnabled);
            salarySettings.overtimeRateX10 = sharedPreferences.getInt("overtime_rate", salarySettings.overtimeRateX10);
            salarySettings.overtimeGraceMinutes = sharedPreferences.getInt("overtime_grace", salarySettings.overtimeGraceMinutes);
            return salarySettings;
        }

        void save(SharedPreferences sharedPreferences) {
            sharedPreferences.edit().putLong("daily_salary_bits", Double.doubleToRawLongBits(this.dailySalary)).putInt("work_start", this.workStartMinutes).putInt("work_end", this.workEndMinutes).putInt("break_start", this.breakStartMinutes).putInt("break_end", this.breakEndMinutes).putInt("work_mode", this.workMode).putBoolean("use_calendar", this.useCalendar).putBoolean("overtime_enabled", this.overtimeEnabled).putInt("overtime_rate", this.overtimeRateX10).putInt("overtime_grace", this.overtimeGraceMinutes).apply();
        }
    }

    static class SalarySnapshot {
        double earned;
        double extraEarned;
        boolean holidayComp;
        int holidayRateX10;
        boolean leaveHalf;
        String leaveName;
        int leavePayPercent;
        double overtimeEarned;
        double overtimeSeconds;
        double progress;
        int state;

        SalarySnapshot() {
        }
    }

    static class SalaryCalculator {
        SalaryCalculator() {
        }

        static double totalWorkSeconds(SalarySettings salarySettings) {
            return Math.max(0.0d, ((salarySettings.workEndMinutes - salarySettings.workStartMinutes) * 60.0d) - ((salarySettings.breakEndMinutes - salarySettings.breakStartMinutes) * 60.0d));
        }

        static SalarySnapshot calculate(Context context, SalarySettings salarySettings, Calendar calendar) {
            SalarySnapshot calculateBase = calculateBase(context, salarySettings, calendar);
            try {
                double dayTotal = ExtraIncome.dayTotal(context, (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5));
                if (dayTotal > 0.0d) {
                    calculateBase.extraEarned = dayTotal;
                    calculateBase.earned += dayTotal;
                }
            } catch (Throwable th) {
            }
            return calculateBase;
        }

        private static SalarySnapshot calculateBase(Context context, SalarySettings salarySettings, Calendar calendar) {
            double d;
            double d2;
            SalarySnapshot salarySnapshot = new SalarySnapshot();
            double d3 = totalWorkSeconds(salarySettings);
            if (d3 <= 0.0d) {
                salarySnapshot.state = 5;
                return salarySnapshot;
            }
            if (!WorkdayHelper.isWorkday(context, salarySettings, calendar)) {
                int i = (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
                int rateX10Of = HolidayWork.rateX10Of(context, i);
                if (rateX10Of > 0) {
                    salarySnapshot.state = MainActivity.STATE_HOLIDAY_WORK;
                    salarySnapshot.holidayRateX10 = rateX10Of;
                    salarySnapshot.progress = 1.0d;
                    salarySnapshot.earned = HolidayWork.earned(salarySettings.dailySalary, rateX10Of);
                    return salarySnapshot;
                }
                if (HolidayWork.isComp(context, i)) {
                    salarySnapshot.state = MainActivity.STATE_HOLIDAY_WORK;
                    salarySnapshot.holidayComp = true;
                    salarySnapshot.progress = 1.0d;
                    salarySnapshot.earned = 0.0d;
                    return salarySnapshot;
                }
                salarySnapshot.state = 0;
                return salarySnapshot;
            }
            LeaveRecord.Entry entry = LeaveRecord.get(context, (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5));
            if (entry != null) {
                salarySnapshot.state = 8;
                salarySnapshot.leavePayPercent = entry.payPercent;
                salarySnapshot.leaveHalf = entry.half;
                salarySnapshot.leaveName = entry.name;
                salarySnapshot.progress = LeaveRecord.ratio(entry.payPercent, entry.half);
                salarySnapshot.earned = LeaveRecord.earned(salarySettings.dailySalary, entry.payPercent, entry.half);
                return salarySnapshot;
            }
            double d4 = (calendar.get(11) * 3600.0d) + (calendar.get(12) * 60.0d) + calendar.get(13) + (calendar.get(14) / 1000.0d);
            double d5 = salarySettings.workStartMinutes * 60.0d;
            double d6 = salarySettings.workEndMinutes * 60.0d;
            double d7 = salarySettings.breakStartMinutes * 60.0d;
            double d8 = 60.0d * salarySettings.breakEndMinutes;
            if (d4 < d5) {
                salarySnapshot.state = 1;
                d2 = 0.0d;
                d = d3;
            } else if (d4 < d7) {
                salarySnapshot.state = 2;
                d2 = d4 - d5;
                d = d3;
            } else if (d4 < d8) {
                salarySnapshot.state = MainActivity.STATE_LUNCH;
                d2 = d7 - d5;
                d = d3;
            } else if (d4 < d6) {
                salarySnapshot.state = 4;
                d2 = (d4 - d8) + (d7 - d5);
                d = d3;
            } else {
                salarySnapshot.state = 5;
                if (salarySettings.overtimeEnabled && salarySettings.overtimeRateX10 > 0) {
                    double secondsOfDay = OvertimeLog.secondsOfDay(context, (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5), calendar.getTimeInMillis());
                    if (secondsOfDay <= 0.0d) {
                        d = d3;
                    } else {
                        salarySnapshot.state = MainActivity.STATE_OVERTIME;
                        salarySnapshot.overtimeSeconds = secondsOfDay;
                        d = d3;
                        salarySnapshot.overtimeEarned = OvertimeCalc.earned(salarySettings.dailySalary, d, secondsOfDay, salarySettings.overtimeRateX10);
                    }
                } else {
                    d = d3;
                }
                d2 = d;
            }
            salarySnapshot.progress = Math.max(0.0d, Math.min(d, d2)) / d;
            salarySnapshot.earned = (salarySettings.dailySalary * salarySnapshot.progress) + salarySnapshot.overtimeEarned;
            return salarySnapshot;
        }
    }

    static class SettingsDialog extends Dialog {
        private final Context activityContext;
        private Button breakEndButton;
        private Button breakStartButton;
        private CheckBox calendarBox;
        private int checkinHour;
        private int checkinMinute;
        private Switch checkinSwitch;
        private Button checkinTimeBtn;
        private final DecimalFormat decimalFormat;
        private final float density;
        private int[] drinkIds;
        private EditText[] drinkKcalInputs;
        private EditText[] drinkMlInputs;
        private EditText[] drinkNameInputs;
        private Button[] drinkSugarButtons;
        private boolean[] drinkSugarStates;
        private Button holidayBtn;
        private final Runnable holidayPoller;
        private long holidayStartedAt;
        private TextView holidayStatusView;
        private boolean holidayUpdating;
        private final Listener listener;
        private EditText monthlyInput;
        private CheckBox overtimeBox;
        private Spinner overtimeGraceSpinner;
        private int[] overtimeGraceValues;
        private RadioGroup overtimeRateGroup;
        private EditText paydayInput;
        private Switch paydaySwitch;
        private Switch backupAutoSwitch;
        private TextView backupDirView;
        private Button backupPeriodBtn;
        private Button backupKeepBtn;
        private Button backupTimeBtn;
        private TextView backupStateView;
        private int backupPeriodValue;
        private int backupKeepValue;
        private int backupHourValue;
        private int backupMinuteValue;
        /** 小人尺寸档位（对话框内的编辑值，保存时才落盘） */
        private int petSizeValue;
        private Button[] petSizeButtons;
        private Switch petSceneSwitch;
        private Switch petVisibleSwitch;
        private final Handler pollHandler;
        private EditText salaryInput;
        private final int section;
        private CheckBox waterCustomWinBox;
        private CheckBox waterEnabledBox;
        private Spinner waterIntervalSpinner;
        private RadioGroup waterModeGroup;
        private Switch waterSkipBreakSwitch;
        private EditText waterTargetInput;
        private Switch waterWeekendSwitch;
        private LinearLayout waterWinBox;
        private Button waterWinEndBtn;
        private Button waterWinStartBtn;
        private final WaterReminderSettings waterWorking;
        private Button workEndButton;
        private Spinner workModeSpinner;
        private Button workStartButton;
        private final SalarySettings working;

        interface Listener {
            void onSaved(SalarySettings salarySettings);
        }

        SettingsDialog(Context context, SalarySettings salarySettings, Listener listener) {
            this(context, salarySettings, listener, 0);
        }

        SettingsDialog(Context context, SalarySettings salarySettings, Listener listener, int i) {
            super(context);
            this.checkinHour = 20;
            this.checkinMinute = 0;
            this.pollHandler = new Handler(Looper.getMainLooper());
            this.holidayPoller = new Runnable() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.1
                @Override // java.lang.Runnable
                public void run() {
                    if (SettingsDialog.this.holidayUpdating) {
                        try {
                            String currentStage = HolidayUpdater.currentStage(SettingsDialog.this.activityContext);
                            long currentTimeMillis = (System.currentTimeMillis() - SettingsDialog.this.holidayStartedAt) / 1000;
                            if (SettingsDialog.this.holidayStatusView != null) {
                                TextView textView = SettingsDialog.this.holidayStatusView;
                                StringBuilder append = new StringBuilder().append("⏳ ");
                                if (currentStage == null) {
                                    currentStage = "启动中";
                                }
                                textView.setText(append.append(currentStage).append("\u3000（已 ").append(currentTimeMillis).append(" 秒）").toString());
                                SettingsDialog.this.holidayStatusView.setTextColor(MainActivity.COLOR_AMBER);
                            }
                        } catch (Throwable th) {
                        }
                        SettingsDialog.this.pollHandler.postDelayed(this, 700L);
                    }
                }
            };
            this.section = i;
            this.activityContext = context;
            this.decimalFormat = new DecimalFormat("#,##0.00");
            this.working = salarySettings.copy();
            this.waterWorking = WaterReminderSettings.load(context);
            this.listener = listener;
            this.density = context.getResources().getDisplayMetrics().density;
            requestWindowFeature(1);
            build(context);
        }

        @Override // android.app.Dialog, android.content.DialogInterface
        public void dismiss() {
            this.holidayUpdating = false;
            try {
                this.pollHandler.removeCallbacks(this.holidayPoller);
            } catch (Throwable th) {
            }
            super.dismiss();
        }

        private MainActivity hostActivity() {
            return Contexts.activityOf(this.activityContext);
        }

        private void build(Context context) {
            String str;
            float f;
            Context context2 = context;
            ScrollView scrollView = new ScrollView(context2);
            LinearLayout linearLayout = new LinearLayout(context2);
            boolean z = true;
            linearLayout.setOrientation(1);
            linearLayout.setPadding(dp(22.0f), dp(20.0f), dp(22.0f), dp(18.0f));
            if (this.section <= 0) {
                linearLayout.setBackground(rounded(MainActivity.COLOR_SURFACE, 8, MainActivity.COLOR_BORDER, 1));
            } else {
                scrollView.setBackground(MainActivity.bgGradient());
                scrollView.setFillViewport(true);
            }
            scrollView.addView(linearLayout, new FrameLayout.LayoutParams(MainActivity.MATCH, -2));
            if (this.section == 1) {
                str = "薪跳设置";
            } else if (this.section == 2) {
                str = "喝水设置";
            } else {
                str = this.section == MainActivity.STATE_LUNCH ? "工具与安全" : "SalaryJump 设置";
            }
            linearLayout.addView(text(context2, str, 21.0f, MainActivity.COLOR_TEXT, 1), wrapWrap());
            if (this.section == 0 || this.section == 1) {
                View sectionHeader = sectionHeader(context2, "💼", "薪跳", "工资 · 时间 · 加班 · 发薪日");
                LinearLayout.LayoutParams matchWrap = matchWrap();
                matchWrap.topMargin = dp(14.0f);
                linearLayout.addView(sectionHeader, matchWrap);
                f = 16.0f;
                View text = text(context2, "每天工资", 13.0f, MainActivity.COLOR_MUTED, 0);
                LinearLayout.LayoutParams wrapWrap = wrapWrap();
                wrapWrap.topMargin = dp(18.0f);
                linearLayout.addView(text, wrapWrap);
                EditText editText = new EditText(context2);
                this.salaryInput = editText;
                editText.setInputType(8194);
                this.salaryInput.setText(this.decimalFormat.format(this.working.dailySalary));
                this.salaryInput.setTextSize(18.0f);
                this.salaryInput.setTextColor(MainActivity.COLOR_TEXT);
                this.salaryInput.setSingleLine(true);
                this.salaryInput.setPadding(dp(14.0f), dp(10.0f), dp(14.0f), dp(10.0f));
                this.salaryInput.setBackground(rounded(MainActivity.COLOR_BACKGROUND, 8, MainActivity.COLOR_BORDER, 1));
                LinearLayout.LayoutParams matchWrap2 = matchWrap();
                matchWrap2.topMargin = dp(8.0f);
                linearLayout.addView(this.salaryInput, matchWrap2);
                final int[] iArr = {MainActivity.workdayCount(context2)};
                View text2 = text(context2, "月薪（自动折算日薪）", 13.0f, MainActivity.COLOR_MUTED, 0);
                LinearLayout.LayoutParams wrapWrap2 = wrapWrap();
                wrapWrap2.topMargin = dp(16.0f);
                linearLayout.addView(text2, wrapWrap2);
                EditText editText2 = new EditText(context2);
                this.monthlyInput = editText2;
                editText2.setInputType(8194);
                editText2.setTextSize(18.0f);
                editText2.setTextColor(MainActivity.COLOR_TEXT);
                editText2.setSingleLine(true);
                editText2.setPadding(dp(14.0f), dp(10.0f), dp(14.0f), dp(10.0f));
                editText2.setBackground(rounded(MainActivity.COLOR_BACKGROUND, 9, MainActivity.COLOR_BORDER, 1));
                if (iArr[0] > 0) {
                    editText2.setText(this.decimalFormat.format(this.working.dailySalary * iArr[0]));
                }
                LinearLayout.LayoutParams matchWrap3 = matchWrap();
                matchWrap3.topMargin = dp(8.0f);
                linearLayout.addView(editText2, matchWrap3);
                TextView text3 = text(context2, "本月 " + iArr[0] + " 个工作日 · 月薪 = 日薪 × " + iArr[0], 11.5f, MainActivity.COLOR_LABEL, 0);
                text3.setLineSpacing(dp(2.0f), 1.0f);
                LinearLayout.LayoutParams matchWrap4 = matchWrap();
                matchWrap4.topMargin = dp(6.0f);
                linearLayout.addView(text3, matchWrap4);
                final boolean[] zArr = {false};
                editText2.addTextChangedListener(new TextWatcher() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.2
                    @Override // android.text.TextWatcher
                    public void afterTextChanged(Editable editable) {
                        if (zArr[0] || iArr[0] <= 0) {
                            return;
                        }
                        zArr[0] = true;
                        try {
                            double parseDouble = Double.parseDouble(editable.toString().trim().replace(",", ""));
                            if (parseDouble > 0.0d) {
                                SettingsDialog.this.salaryInput.setText(SettingsDialog.this.decimalFormat.format(parseDouble / iArr[0]));
                            }
                        } catch (Throwable th) {
                        }
                        zArr[0] = false;
                    }

                    @Override // android.text.TextWatcher
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    }

                    @Override // android.text.TextWatcher
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    }
                });
                this.salaryInput.addTextChangedListener(new TextWatcher() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.3
                    @Override // android.text.TextWatcher
                    public void afterTextChanged(Editable editable) {
                        if (zArr[0] || iArr[0] <= 0) {
                            return;
                        }
                        zArr[0] = true;
                        try {
                            double parseDouble = Double.parseDouble(editable.toString().trim().replace(",", ""));
                            if (parseDouble > 0.0d) {
                                SettingsDialog.this.monthlyInput.setText(SettingsDialog.this.decimalFormat.format(parseDouble * iArr[0]));
                            }
                        } catch (Throwable th) {
                        }
                        zArr[0] = false;
                    }

                    @Override // android.text.TextWatcher
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    }

                    @Override // android.text.TextWatcher
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    }
                });
                View text4 = text(context, "工作时间", 13.0f, MainActivity.COLOR_MUTED, 0);
                LinearLayout.LayoutParams wrapWrap3 = wrapWrap();
                wrapWrap3.topMargin = dp(20.0f);
                linearLayout.addView(text4, wrapWrap3);
                LinearLayout timeRow = timeRow(context);
                this.workStartButton = timeButton(context, "上班", this.working.workStartMinutes, true);
                this.workEndButton = timeButton(context, "下班", this.working.workEndMinutes, false);
                timeRow.addView(this.workStartButton, timeButtonParams(true));
                timeRow.addView(this.workEndButton, timeButtonParams(false));
                LinearLayout.LayoutParams matchWrap5 = matchWrap();
                matchWrap5.topMargin = dp(8.0f);
                linearLayout.addView(timeRow, matchWrap5);
                View text5 = text(context, "午休时间", 13.0f, MainActivity.COLOR_MUTED, 0);
                LinearLayout.LayoutParams wrapWrap4 = wrapWrap();
                wrapWrap4.topMargin = dp(16.0f);
                linearLayout.addView(text5, wrapWrap4);
                LinearLayout timeRow2 = timeRow(context);
                this.breakStartButton = timeButton(context, "午休开始", this.working.breakStartMinutes, false);
                this.breakEndButton = timeButton(context, "午休结束", this.working.breakEndMinutes, false);
                timeRow2.addView(this.breakStartButton, timeButtonParams(true));
                timeRow2.addView(this.breakEndButton, timeButtonParams(false));
                LinearLayout.LayoutParams matchWrap6 = matchWrap();
                matchWrap6.topMargin = dp(8.0f);
                linearLayout.addView(timeRow2, matchWrap6);
                View text6 = text(context, "工作日规则", 13.0f, MainActivity.COLOR_MUTED, 0);
                LinearLayout.LayoutParams wrapWrap5 = wrapWrap();
                wrapWrap5.topMargin = dp(16.0f);
                linearLayout.addView(text6, wrapWrap5);
                this.workModeSpinner = new Spinner(context);
                ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item, new String[]{"每天都是工作日", "仅周一至周五", "智能（法定节假日+调休）"});
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                this.workModeSpinner.setAdapter((SpinnerAdapter) arrayAdapter);
                this.workModeSpinner.setSelection(Math.max(0, Math.min(2, this.working.workMode)));
                LinearLayout.LayoutParams matchWrap7 = matchWrap();
                matchWrap7.topMargin = dp(8.0f);
                linearLayout.addView(this.workModeSpinner, matchWrap7);
                CheckBox checkBox = new CheckBox(context);
                this.calendarBox = checkBox;
                View text7 = text(context, "加班费", 13.0f, MainActivity.COLOR_MUTED, 0);
                LinearLayout.LayoutParams wrapWrap6 = wrapWrap();
                wrapWrap6.topMargin = dp(18.0f);
                linearLayout.addView(text7, wrapWrap6);
                this.overtimeBox = new CheckBox(context);
                this.overtimeBox.setText("下班后继续累计（按加班倍率）");
                this.overtimeBox.setTextSize(14.0f);
                this.overtimeBox.setTextColor(MainActivity.COLOR_TEXT);
                this.overtimeBox.setChecked(this.working.overtimeEnabled);
                LinearLayout.LayoutParams matchWrap8 = matchWrap();
                matchWrap8.topMargin = dp(6.0f);
                linearLayout.addView(this.overtimeBox, matchWrap8);
                this.overtimeRateGroup = new RadioGroup(context);
                this.overtimeRateGroup.setOrientation(0);
                int[] iArr2 = {15, 20, 30};
                String[] strArr = {"1.5 倍", "2 倍", "3 倍"};
                int i = 0;
                while (i < MainActivity.STATE_LUNCH) {
                    RadioButton radioButton = new RadioButton(context);
                    radioButton.setText(strArr[i]);
                    radioButton.setTextSize(13.0f);
                    radioButton.setTextColor(MainActivity.COLOR_TEXT);
                    radioButton.setChecked(this.working.overtimeRateX10 == iArr2[i] ? z : false);
                    radioButton.setId(i + 7000);
                    this.overtimeRateGroup.addView(radioButton, new LinearLayout.LayoutParams(0, dp(38.0f), 1.0f));
                    i++;
                    z = true;
                }
                LinearLayout.LayoutParams matchWrap9 = matchWrap();
                matchWrap9.topMargin = dp(2.0f);
                linearLayout.addView(this.overtimeRateGroup, matchWrap9);
                this.overtimeGraceSpinner = new Spinner(context);
                this.overtimeGraceValues = new int[]{0, 15, 30, 60};
                ArrayAdapter arrayAdapter2 = new ArrayAdapter(context, android.R.layout.simple_spinner_item, new String[]{"下班即算", "宽限 15 分钟", "宽限 30 分钟", "宽限 1 小时"});
                arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                this.overtimeGraceSpinner.setAdapter((SpinnerAdapter) arrayAdapter2);
                for (int i2 = 0; i2 < this.overtimeGraceValues.length; i2++) {
                    if (this.overtimeGraceValues[i2] == this.working.overtimeGraceMinutes) {
                        this.overtimeGraceSpinner.setSelection(i2);
                    }
                }
                LinearLayout.LayoutParams matchWrap10 = matchWrap();
                matchWrap10.topMargin = dp(6.0f);
                linearLayout.addView(this.overtimeGraceSpinner, matchWrap10);
                TextView text8 = text(context, "无论何时点「开始」，加班费都从「下班时间 + 宽限」起算。", 11.5f, MainActivity.COLOR_LABEL, 0);
                text8.setLineSpacing(dp(2.0f), 1.0f);
                LinearLayout.LayoutParams matchWrap11 = matchWrap();
                matchWrap11.topMargin = dp(6.0f);
                linearLayout.addView(text8, matchWrap11);
                checkBox.setText("读取日历中的休/班标记（优先采用）");
                this.calendarBox.setTextSize(14.0f);
                this.calendarBox.setTextColor(MainActivity.COLOR_TEXT);
                this.calendarBox.setChecked(this.working.useCalendar);
                LinearLayout.LayoutParams wrapWrap7 = wrapWrap();
                wrapWrap7.topMargin = dp(12.0f);
                linearLayout.addView(this.calendarBox, wrapWrap7);
                TextView text9 = text(context, "日历中全天事件含「休/假」视为休息日，含「班」视为工作日；开启需授权日历权限。", 12.0f, MainActivity.COLOR_MUTED, 0);
                text9.setLineSpacing(dp(3.0f), 1.0f);
                LinearLayout.LayoutParams wrapWrap8 = wrapWrap();
                wrapWrap8.topMargin = dp(6.0f);
                linearLayout.addView(text9, wrapWrap8);
                View text10 = text(context, "发薪日提醒", 13.0f, MainActivity.COLOR_MUTED, 0);
                LinearLayout.LayoutParams wrapWrap9 = wrapWrap();
                wrapWrap9.topMargin = dp(20.0f);
                linearLayout.addView(text10, wrapWrap9);
                LinearLayout linearLayout2 = new LinearLayout(context);
                linearLayout2.setOrientation(0);
                linearLayout2.setGravity(16);
                LinearLayout.LayoutParams matchWrap12 = matchWrap();
                matchWrap12.topMargin = dp(8.0f);
                linearLayout.addView(linearLayout2, matchWrap12);
                linearLayout2.addView(text(context, "每月", 14.0f, MainActivity.COLOR_TEXT, 0), wrapWrap());
                EditText editText3 = new EditText(context);
                this.paydayInput = editText3;
                editText3.setInputType(2);
                editText3.setTextSize(16.0f);
                editText3.setTextColor(MainActivity.COLOR_TEXT);
                editText3.setGravity(17);
                editText3.setSingleLine(true);
                editText3.setText(String.valueOf(PaydayReminder.day(context)));
                editText3.setPadding(dp(6.0f), dp(8.0f), dp(6.0f), dp(8.0f));
                editText3.setBackground(rounded(MainActivity.COLOR_BACKGROUND, 9, MainActivity.COLOR_BORDER, 1));
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(58.0f), dp(42.0f));
                layoutParams.leftMargin = dp(8.0f);
                linearLayout2.addView(editText3, layoutParams);
                LinearLayout.LayoutParams wrapWrap10 = wrapWrap();
                wrapWrap10.leftMargin = dp(8.0f);
                linearLayout2.addView(text(context, "号发薪", 14.0f, MainActivity.COLOR_TEXT, 0), wrapWrap10);
                Switch r2 = new Switch(context);
                this.paydaySwitch = r2;
                r2.setChecked(PaydayReminder.isEnabled(context));
                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, dp(42.0f), 1.0f);
                layoutParams2.leftMargin = dp(6.0f);
                linearLayout2.addView(r2, layoutParams2);
                TextView text11 = text(context, "当天与前一天 10:00 提醒，通知里可直接打开上月战报", 11.5f, MainActivity.COLOR_LABEL, 0);
                text11.setLineSpacing(dp(2.0f), 1.0f);
                LinearLayout.LayoutParams matchWrap13 = matchWrap();
                matchWrap13.topMargin = dp(6.0f);
                linearLayout.addView(text11, matchWrap13);
                View text12 = text(context2, "打卡提醒", 13.0f, MainActivity.COLOR_MUTED, 0);
                LinearLayout.LayoutParams wrapWrap11 = wrapWrap();
                wrapWrap11.topMargin = dp(20.0f);
                linearLayout.addView(text12, wrapWrap11);
                this.checkinHour = CheckInReminder.hour(context2);
                this.checkinMinute = CheckInReminder.minute(context2);
                LinearLayout linearLayout3 = new LinearLayout(context2);
                linearLayout3.setOrientation(0);
                linearLayout3.setGravity(16);
                LinearLayout.LayoutParams matchWrap14 = matchWrap();
                matchWrap14.topMargin = dp(8.0f);
                linearLayout.addView(linearLayout3, matchWrap14);
                linearLayout3.addView(text(context2, "每天", 14.0f, MainActivity.COLOR_TEXT, 0), wrapWrap());
                Button button = new Button(context2);
                this.checkinTimeBtn = button;
                button.setText(MainActivity.formatTime((this.checkinHour * 60) + this.checkinMinute));
                button.setTextSize(14.0f);
                button.setTextColor(MainActivity.COLOR_TEXT);
                button.setAllCaps(false);
                button.setMinWidth(0);
                button.setMinHeight(0);
                button.setPadding(dp(10.0f), 0, dp(10.0f), 0);
                button.setBackground(rounded(MainActivity.COLOR_BACKGROUND, 9, MainActivity.COLOR_BORDER, 1));
                LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-2, dp(42.0f));
                layoutParams3.leftMargin = dp(8.0f);
                linearLayout3.addView(button, layoutParams3);
                LinearLayout.LayoutParams wrapWrap12 = wrapWrap();
                wrapWrap12.leftMargin = dp(8.0f);
                linearLayout3.addView(text(context2, "提醒", 14.0f, MainActivity.COLOR_TEXT, 0), wrapWrap12);
                Switch r22 = new Switch(context2);
                this.checkinSwitch = r22;
                r22.setChecked(CheckInReminder.isEnabled(context2));
                LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(0, dp(42.0f), 1.0f);
                layoutParams4.leftMargin = dp(6.0f);
                linearLayout3.addView(r22, layoutParams4);
                button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.4
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        new TimePickerDialog(context2, new TimePickerDialog.OnTimeSetListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.4.1
                            @Override // android.app.TimePickerDialog.OnTimeSetListener
                            public void onTimeSet(TimePicker timePicker, int i3, int i4) {
                                SettingsDialog.this.checkinHour = i3;
                                SettingsDialog.this.checkinMinute = i4;
                                SettingsDialog.this.checkinTimeBtn.setText(MainActivity.formatTime((i3 * 60) + i4));
                            }
                        }, SettingsDialog.this.checkinHour, SettingsDialog.this.checkinMinute, true).show();
                    }
                });
                TextView text13 = text(context2, "只在该记、还没记时提醒：工作日问加班（已打卡/已请假不打扰），休息日问出勤", 11.5f, MainActivity.COLOR_LABEL, 0);
                text13.setLineSpacing(dp(2.0f), 1.0f);
                LinearLayout.LayoutParams matchWrap15 = matchWrap();
                matchWrap15.topMargin = dp(6.0f);
                linearLayout.addView(text13, matchWrap15);
                View dialogButton = dialogButton(context2, "加班与出勤规则", false, new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.5
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        try {
                            Intent intent = new Intent(context2, (Class<?>) WorkRulesActivity.class);
                            if (!(context2 instanceof Activity)) {
                                intent.addFlags(268435456);
                            }
                            context2.startActivity(intent);
                            SettingsDialog.this.dismiss();
                        } catch (Throwable th) {
                            Toast.makeText(context2, "打开失败：" + th.getMessage(), 0).show();
                        }
                    }
                });
                LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(MainActivity.MATCH, dp(42.0f));
                layoutParams5.topMargin = dp(8.0f);
                linearLayout.addView(dialogButton, layoutParams5);
                View dialogButton2 = dialogButton(context2, "假期账户", false, new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.6
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        try {
                            Intent intent = new Intent(context2, (Class<?>) LeaveAccountsActivity.class);
                            if (!(context2 instanceof Activity)) {
                                intent.addFlags(268435456);
                            }
                            context2.startActivity(intent);
                            SettingsDialog.this.dismiss();
                        } catch (Throwable th) {
                            Toast.makeText(context2, "打开失败：" + th.getMessage(), 0).show();
                        }
                    }
                });
                LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(MainActivity.MATCH, dp(42.0f));
                layoutParams6.topMargin = dp(8.0f);
                linearLayout.addView(dialogButton2, layoutParams6);
            } else {
                f = 16.0f;
            }
            if (this.section == 0 || this.section == 2) {
                View sectionHeader2 = sectionHeader(context2, "💧", "喝水提醒", "提醒 · 饮品 · 热量");
                LinearLayout.LayoutParams matchWrap16 = matchWrap();
                matchWrap16.topMargin = dp(26.0f);
                linearLayout.addView(sectionHeader2, matchWrap16);
                CheckBox checkBox2 = new CheckBox(context2);
                this.waterEnabledBox = checkBox2;
                checkBox2.setText("开启工作时间喝水提醒");
                this.waterEnabledBox.setTextSize(14.0f);
                this.waterEnabledBox.setTextColor(MainActivity.COLOR_TEXT);
                this.waterEnabledBox.setChecked(this.waterWorking.enabled);
                LinearLayout.LayoutParams wrapWrap13 = wrapWrap();
                wrapWrap13.topMargin = dp(8.0f);
                linearLayout.addView(this.waterEnabledBox, wrapWrap13);
                View text14 = text(context2, "提醒间隔", 13.0f, MainActivity.COLOR_MUTED, 0);
                LinearLayout.LayoutParams wrapWrap14 = wrapWrap();
                wrapWrap14.topMargin = dp(f);
                linearLayout.addView(text14, wrapWrap14);
                this.waterIntervalSpinner = new Spinner(context2);
                int[] iArr3 = {30, 45, 60, 90, 120};
                ArrayAdapter arrayAdapter3 = new ArrayAdapter(context2, android.R.layout.simple_spinner_item, new String[]{"30 分钟", "45 分钟", "60 分钟", "90 分钟", "120 分钟"});
                arrayAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                this.waterIntervalSpinner.setAdapter((SpinnerAdapter) arrayAdapter3);
                int i3 = 0;
                while (true) {
                    if (i3 < 5) {
                        if (iArr3[i3] == this.waterWorking.intervalMinutes) {
                            break;
                        } else {
                            i3++;
                        }
                    } else {
                        i3 = MainActivity.STATE_LUNCH;
                        break;
                    }
                }
                this.waterIntervalSpinner.setSelection(i3);
                LinearLayout.LayoutParams matchWrap17 = matchWrap();
                matchWrap17.topMargin = dp(6.0f);
                linearLayout.addView(this.waterIntervalSpinner, matchWrap17);
                CheckBox checkBox3 = new CheckBox(context2);
                this.waterCustomWinBox = checkBox3;
                checkBox3.setText("自定义喝水时段（独立于工资设置）");
                checkBox3.setTextSize(14.0f);
                checkBox3.setTextColor(MainActivity.COLOR_TEXT);
                checkBox3.setChecked(this.waterWorking.windowMode == 1);
                LinearLayout.LayoutParams wrapWrap15 = wrapWrap();
                wrapWrap15.topMargin = dp(14.0f);
                linearLayout.addView(checkBox3, wrapWrap15);
                final LinearLayout linearLayout4 = new LinearLayout(context2);
                this.waterWinBox = linearLayout4;
                linearLayout4.setOrientation(1);
                LinearLayout.LayoutParams matchWrap18 = matchWrap();
                matchWrap18.topMargin = dp(8.0f);
                linearLayout.addView(linearLayout4, matchWrap18);
                linearLayout4.setVisibility(this.waterWorking.windowMode == 1 ? 0 : 8);
                LinearLayout linearLayout5 = new LinearLayout(context2);
                linearLayout5.setOrientation(0);
                linearLayout5.setGravity(16);
                linearLayout4.addView(linearLayout5, matchWrap());
                this.waterWinStartBtn = waterWinTimeButton(context2, true);
                this.waterWinEndBtn = waterWinTimeButton(context2, false);
                linearLayout5.addView(this.waterWinStartBtn, new LinearLayout.LayoutParams(0, dp(60.0f), 1.0f));
                LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(0, dp(60.0f), 1.0f);
                layoutParams7.leftMargin = dp(10.0f);
                linearLayout5.addView(this.waterWinEndBtn, layoutParams7);
                LinearLayout linearLayout6 = new LinearLayout(context2);
                linearLayout6.setOrientation(0);
                linearLayout6.setGravity(16);
                LinearLayout.LayoutParams matchWrap19 = matchWrap();
                matchWrap19.topMargin = dp(10.0f);
                linearLayout4.addView(linearLayout6, matchWrap19);
                linearLayout6.addView(text(context2, "午休期间不提醒", 13.5f, MainActivity.COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, -2, 1.0f));
                Switch r23 = new Switch(context2);
                this.waterSkipBreakSwitch = r23;
                r23.setChecked(this.waterWorking.skipBreak);
                linearLayout6.addView(r23, wrapWrap());
                LinearLayout linearLayout7 = new LinearLayout(context2);
                linearLayout7.setOrientation(0);
                linearLayout7.setGravity(16);
                LinearLayout.LayoutParams matchWrap20 = matchWrap();
                matchWrap20.topMargin = dp(6.0f);
                linearLayout4.addView(linearLayout7, matchWrap20);
                linearLayout7.addView(text(context2, "周末也提醒", 13.5f, MainActivity.COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, -2, 1.0f));
                Switch r24 = new Switch(context2);
                this.waterWeekendSwitch = r24;
                r24.setChecked(this.waterWorking.weekend);
                linearLayout7.addView(r24, wrapWrap());
                checkBox3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.7
                    @Override // android.widget.CompoundButton.OnCheckedChangeListener
                    public void onCheckedChanged(CompoundButton compoundButton, boolean z2) {
                        linearLayout4.setVisibility(z2 ? 0 : 8);
                    }
                });
                View text15 = text(context2, "每天喝水总量（ml）", 13.0f, MainActivity.COLOR_MUTED, 0);
                LinearLayout.LayoutParams wrapWrap16 = wrapWrap();
                wrapWrap16.topMargin = dp(f);
                linearLayout.addView(text15, wrapWrap16);
                this.waterTargetInput = numberInput(context2, String.valueOf(this.waterWorking.targetMl));
                LinearLayout.LayoutParams matchWrap21 = matchWrap();
                matchWrap21.topMargin = dp(6.0f);
                linearLayout.addView(this.waterTargetInput, matchWrap21);
                LinearLayout linearLayout8 = new LinearLayout(context2);
                linearLayout8.setOrientation(0);
                linearLayout8.setGravity(16);
                linearLayout8.addView(text(context2, "饮品名称 · 容量 · 热量（每100ml）", 13.0f, MainActivity.COLOR_MUTED, 0), new LinearLayout.LayoutParams(0, dp(30.0f), 1.0f));
                TextView text16 = text(context, "参考表", 12.5f, MainActivity.COLOR_BLUE, 0);
                text16.setGravity(21);
                text16.setPadding(dp(10.0f), 0, dp(2.0f), 0);
                text16.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.8
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        SettingsDialog.this.showPresetDialog();
                    }
                });
                linearLayout8.addView(text16, new LinearLayout.LayoutParams(-2, dp(30.0f)));
                LinearLayout.LayoutParams wrapWrap17 = wrapWrap();
                wrapWrap17.topMargin = dp(f);
                linearLayout.addView(linearLayout8, wrapWrap17);
                int count = DrinkTypes.count(context);
                this.drinkIds = new int[count];
                this.drinkNameInputs = new EditText[count];
                this.drinkMlInputs = new EditText[count];
                this.drinkKcalInputs = new EditText[count];
                this.drinkSugarButtons = new Button[count];
                this.drinkSugarStates = new boolean[count];
                for (int i4 = 0; i4 < count; i4++) {
                    final int slot = i4;
                    final int idAt = DrinkTypes.idAt(context, i4);
                    this.drinkIds[i4] = idAt;
                    this.drinkSugarStates[i4] = DrinkTypes.isSugary(context, idAt);
                    LinearLayout linearLayout9 = new LinearLayout(context);
                    linearLayout9.setOrientation(0);
                    linearLayout9.setGravity(16);
                    EditText editText4 = new EditText(context);
                    editText4.setText(DrinkTypes.name(context, idAt));
                    editText4.setTextSize(13.0f);
                    editText4.setTextColor(MainActivity.COLOR_TEXT);
                    editText4.setSingleLine(true);
                    editText4.setPadding(dp(10.0f), 0, dp(8.0f), 0);
                    editText4.setBackground(MainActivity.softPill(dp(9.0f), dp(1.0f)));
                    editText4.setOnLongClickListener(new View.OnLongClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.9
                        @Override // android.view.View.OnLongClickListener
                        public boolean onLongClick(View view) {
                            SettingsDialog.this.showRemoveDrinkDialog(idAt);
                            return true;
                        }
                    });
                    this.drinkNameInputs[i4] = editText4;
                    LinearLayout.LayoutParams layoutParams8 = new LinearLayout.LayoutParams(0, dp(38.0f), 1.0f);
                    layoutParams8.rightMargin = dp(5.0f);
                    linearLayout9.addView(editText4, layoutParams8);
                    EditText numberInput = numberInput(context, String.valueOf(idAt == 0 ? this.waterWorking.cupMl : DrinkTypes.ml(context, idAt)));
                    numberInput.setTextSize(13.0f);
                    this.drinkMlInputs[i4] = numberInput;
                    LinearLayout.LayoutParams layoutParams9 = new LinearLayout.LayoutParams(dp(54.0f), dp(38.0f));
                    layoutParams9.rightMargin = dp(5.0f);
                    linearLayout9.addView(numberInput, layoutParams9);
                    EditText numberInput2 = numberInput(context, String.valueOf(DrinkTypes.kcal(context, idAt)));
                    numberInput2.setTextSize(13.0f);
                    this.drinkKcalInputs[i4] = numberInput2;
                    LinearLayout.LayoutParams layoutParams10 = new LinearLayout.LayoutParams(dp(56.0f), dp(38.0f));
                    layoutParams10.rightMargin = dp(5.0f);
                    linearLayout9.addView(numberInput2, layoutParams10);
                    Button button2 = new Button(context);
                    button2.setTextSize(11.5f);
                    button2.setAllCaps(false);
                    button2.setMinWidth(0);
                    button2.setMinHeight(0);
                    button2.setPadding(0, 0, 0, 0);
                    this.drinkSugarButtons[i4] = button2;
                    applySugarStyle(button2, this.drinkSugarStates[i4]);
                    button2.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.10
                        @Override // android.view.View.OnClickListener
                        public void onClick(View view) {
                            SettingsDialog.this.drinkSugarStates[slot] = !SettingsDialog.this.drinkSugarStates[slot];
                            SettingsDialog.this.applySugarStyle(SettingsDialog.this.drinkSugarButtons[slot], SettingsDialog.this.drinkSugarStates[slot]);
                        }
                    });
                    linearLayout9.addView(button2, new LinearLayout.LayoutParams(dp(48.0f), dp(38.0f)));
                    LinearLayout.LayoutParams wrapWrap18 = wrapWrap();
                    wrapWrap18.topMargin = dp(6.0f);
                    linearLayout.addView(linearLayout9, wrapWrap18);
                }
                TextView text17 = text(context, "热量按每 100ml 计，点「参考表」查常见饮品数值；含糖标记仅用于统计。", 11.5f, MainActivity.COLOR_LABEL, 0);
                text17.setLineSpacing(dp(2.0f), 1.0f);
                LinearLayout.LayoutParams wrapWrap19 = wrapWrap();
                wrapWrap19.topMargin = dp(7.0f);
                linearLayout.addView(text17, wrapWrap19);
                View text18 = text(context2, "提醒方式", 13.0f, MainActivity.COLOR_MUTED, 0);
                LinearLayout.LayoutParams wrapWrap20 = wrapWrap();
                wrapWrap20.topMargin = dp(18.0f);
                linearLayout.addView(text18, wrapWrap20);
                RadioGroup radioGroup = new RadioGroup(context2);
                this.waterModeGroup = radioGroup;
                radioGroup.setOrientation(1);
                addWaterMode(context2, this.waterModeGroup, "仅铃音", this.waterWorking.reminderMode == 0);
                addWaterMode(context2, this.waterModeGroup, "仅震动", this.waterWorking.reminderMode == 1);
                addWaterMode(context2, this.waterModeGroup, "铃音和震动", this.waterWorking.reminderMode == 2);
                LinearLayout.LayoutParams matchWrap22 = matchWrap();
                matchWrap22.topMargin = dp(6.0f);
                linearLayout.addView(this.waterModeGroup, matchWrap22);
                TextView text19 = text(context2, "午休期间和下班后不会提醒；默认工作时段每 90 分钟提醒一次。周末或节假日标记出勤后，当天也照常提醒。", 12.0f, MainActivity.COLOR_MUTED, 0);
                text19.setLineSpacing(dp(3.0f), 1.0f);
                LinearLayout.LayoutParams matchWrap23 = matchWrap();
                matchWrap23.topMargin = dp(8.0f);
                linearLayout.addView(text19, matchWrap23);
                this.waterEnabledBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.11
                    @Override // android.widget.CompoundButton.OnCheckedChangeListener
                    public void onCheckedChanged(CompoundButton compoundButton, boolean z2) {
                        SettingsDialog.this.setWaterControlsEnabled(z2);
                    }
                });
                setWaterControlsEnabled(this.waterWorking.enabled);
            }
            if (this.section == 0 || this.section == MainActivity.STATE_LUNCH) {
                View autoBackupHeader = sectionHeader(context2, "💾", "自动备份", "目录 · 周期 · 保留份数");
                LinearLayout.LayoutParams abParams = matchWrap();
                abParams.topMargin = dp(26.0f);
                linearLayout.addView(autoBackupHeader, abParams);

                LinearLayout abSwitchRow = new LinearLayout(context2);
                abSwitchRow.setOrientation(0);
                abSwitchRow.setGravity(16);
                LinearLayout.LayoutParams abSwitchParams = matchWrap();
                abSwitchParams.topMargin = dp(16.0f);
                linearLayout.addView(abSwitchRow, abSwitchParams);
                abSwitchRow.addView(text(context2, "自动备份", 14.0f, MainActivity.COLOR_TEXT, 0),
                        new LinearLayout.LayoutParams(0, -2, 1.0f));
                Switch abSwitch = new Switch(context2);
                this.backupAutoSwitch = abSwitch;
                abSwitch.setChecked(BackupScheduler.isEnabled(context2));
                abSwitchRow.addView(abSwitch, new LinearLayout.LayoutParams(-2, dp(42.0f)));

                LinearLayout abDirRow = new LinearLayout(context2);
                abDirRow.setOrientation(0);
                abDirRow.setGravity(16);
                LinearLayout.LayoutParams abDirParams = matchWrap();
                abDirParams.topMargin = dp(14.0f);
                linearLayout.addView(abDirRow, abDirParams);
                abDirRow.addView(text(context2, "备份文件夹", 14.0f, MainActivity.COLOR_TEXT, 0),
                        new LinearLayout.LayoutParams(0, -2, 1.0f));
                Button abDirBtn = dialogButton(context2,
                        BackupScheduler.treeUri(context2).length() == 0 ? "选择…" : "更换",
                        false, new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.44
                            @Override // android.view.View.OnClickListener
                            public void onClick(View view) {
                                try {
                                    Intent pick = new Intent("android.intent.action.OPEN_DOCUMENT_TREE");
                                    pick.addFlags(64);
                                    pick.addFlags(1);
                                    pick.addFlags(2);
                                    Activity host = Contexts.anyActivityOf(SettingsDialog.this.activityContext);
                                    if (host == null) {
                                        throw new IllegalStateException("无法获取宿主 Activity");
                                    }
                                    host.startActivityForResult(pick, MainActivity.REQ_BACKUP_TREE);
                                    SettingsDialog.this.dismiss();
                                } catch (Throwable th) {
                                    Toast.makeText(context2, "无法打开目录选择器", 1).show();
                                }
                            }
                        });
                abDirRow.addView(abDirBtn, new LinearLayout.LayoutParams(dp(84.0f), dp(40.0f)));

                this.backupDirView = text(context2,
                        BackupScheduler.treeUri(context2).length() == 0
                                ? "还没选（自动备份无法工作）"
                                : BackupScheduler.treeLabel(context2),
                        11.0f, MainActivity.COLOR_LABEL, 0);
                LinearLayout.LayoutParams abDirTextParams = wrapWrap();
                abDirTextParams.leftMargin = dp(4.0f);
                abDirTextParams.topMargin = dp(4.0f);
                linearLayout.addView(this.backupDirView, abDirTextParams);

                this.backupPeriodValue = BackupScheduler.period(context2);
                this.backupKeepValue = BackupScheduler.keepOption(context2);
                this.backupHourValue = BackupScheduler.hour(context2);
                this.backupMinuteValue = BackupScheduler.minute(context2);

                LinearLayout abOptRow = new LinearLayout(context2);
                abOptRow.setOrientation(0);
                abOptRow.setGravity(16);
                LinearLayout.LayoutParams abOptParams = matchWrap();
                abOptParams.topMargin = dp(14.0f);
                linearLayout.addView(abOptRow, abOptParams);
                abOptRow.addView(text(context2, "周期", 14.0f, MainActivity.COLOR_TEXT, 0),
                        new LinearLayout.LayoutParams(0, -2, 1.0f));
                Button abPeriodBtn = dialogButton(context2, BackupPlan.periodName(this.backupPeriodValue),
                        false, new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.45
                            @Override // android.view.View.OnClickListener
                            public void onClick(View view) {
                                SettingsDialog.this.backupPeriodValue =
                                        (SettingsDialog.this.backupPeriodValue + 1) % BackupPlan.PERIODS.length;
                                SettingsDialog.this.backupPeriodBtn.setText(
                                        BackupPlan.periodName(SettingsDialog.this.backupPeriodValue));
                            }
                        });
                this.backupPeriodBtn = abPeriodBtn;
                abOptRow.addView(abPeriodBtn, new LinearLayout.LayoutParams(dp(84.0f), dp(40.0f)));
                Button abTimeBtn = dialogButton(context2,
                        BackupPlan.describeAt(this.backupHourValue, this.backupMinuteValue),
                        false, new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.46
                            @Override // android.view.View.OnClickListener
                            public void onClick(View view) {
                                new TimePickerDialog(context2, new TimePickerDialog.OnTimeSetListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.46.1
                                    @Override // android.app.TimePickerDialog.OnTimeSetListener
                                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                        SettingsDialog.this.backupHourValue = hour;
                                        SettingsDialog.this.backupMinuteValue = minute;
                                        SettingsDialog.this.backupTimeBtn.setText(
                                                BackupPlan.describeAt(hour, minute));
                                    }
                                }, SettingsDialog.this.backupHourValue,
                                        SettingsDialog.this.backupMinuteValue, true).show();
                            }
                        });
                this.backupTimeBtn = abTimeBtn;
                LinearLayout.LayoutParams abTimeParams = new LinearLayout.LayoutParams(dp(84.0f), dp(40.0f));
                abTimeParams.leftMargin = dp(6.0f);
                abOptRow.addView(abTimeBtn, abTimeParams);

                LinearLayout abKeepRow = new LinearLayout(context2);
                abKeepRow.setOrientation(0);
                abKeepRow.setGravity(16);
                LinearLayout.LayoutParams abKeepParams = matchWrap();
                abKeepParams.topMargin = dp(14.0f);
                linearLayout.addView(abKeepRow, abKeepParams);
                abKeepRow.addView(text(context2, "保留份数", 14.0f, MainActivity.COLOR_TEXT, 0),
                        new LinearLayout.LayoutParams(0, -2, 1.0f));
                Button abKeepBtn = dialogButton(context2, BackupPlan.keepName(this.backupKeepValue),
                        false, new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.47
                            @Override // android.view.View.OnClickListener
                            public void onClick(View view) {
                                SettingsDialog.this.backupKeepValue =
                                        (SettingsDialog.this.backupKeepValue + 1) % BackupPlan.KEEP_OPTIONS.length;
                                SettingsDialog.this.backupKeepBtn.setText(
                                        BackupPlan.keepName(SettingsDialog.this.backupKeepValue));
                            }
                        });
                this.backupKeepBtn = abKeepBtn;
                abKeepRow.addView(abKeepBtn, new LinearLayout.LayoutParams(dp(84.0f), dp(40.0f)));

                Button abNowBtn = dialogButton(context2, "立即备份一次", false,
                        new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.48
                            @Override // android.view.View.OnClickListener
                            public void onClick(View view) {
                                BackupScheduler.runBackup(SettingsDialog.this.getContext(), true);
                                Toast.makeText(context2, "已触发备份，结果见通知", 0).show();
                            }
                        });
                LinearLayout.LayoutParams abNowParams = matchWrap();
                abNowParams.topMargin = dp(14.0f);
                linearLayout.addView(abNowBtn, abNowParams);

                this.backupStateView = text(context2,
                        this.buildBackupStateText(), 11.0f, MainActivity.COLOR_LABEL, 0);
                this.backupStateView.setLineSpacing(dp(2.0f), 1.0f);
                LinearLayout.LayoutParams abStateParams = matchWrap();
                abStateParams.topMargin = dp(8.0f);
                linearLayout.addView(this.backupStateView, abStateParams);

                View sectionHeader3 = sectionHeader(context2, "🔧", "工具与安全", "备份 · 诊断 · 隐私锁");
                LinearLayout.LayoutParams matchWrap24 = matchWrap();
                matchWrap24.topMargin = dp(26.0f);
                linearLayout.addView(sectionHeader3, matchWrap24);
                LinearLayout linearLayout10 = new LinearLayout(context2);
                linearLayout10.setOrientation(0);
                linearLayout10.setGravity(16);
                LinearLayout.LayoutParams matchWrap25 = matchWrap();
                matchWrap25.topMargin = dp(22.0f);
                linearLayout.addView(linearLayout10, matchWrap25);
                Button dialogButton3 = dialogButton(context2, "更新节假日", false, new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.12
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        SettingsDialog.this.updateHolidays();
                    }
                });
                this.holidayBtn = dialogButton3;
                LinearLayout.LayoutParams layoutParams11 = new LinearLayout.LayoutParams(0, dp(42.0f), 1.0f);
                layoutParams11.rightMargin = dp(5.0f);
                linearLayout10.addView(dialogButton3, layoutParams11);
                Button dialogButton4 = dialogButton(context2, "数据备份", false, new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.13
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        SettingsDialog.this.showBackupDialog();
                    }
                });
                LinearLayout.LayoutParams layoutParams12 = new LinearLayout.LayoutParams(0, dp(42.0f), 1.0f);
                layoutParams12.leftMargin = dp(5.0f);
                linearLayout10.addView(dialogButton4, layoutParams12);
                Button dialogButton5 = dialogButton(context2, "网络诊断", false, new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.14
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        SettingsDialog.this.runNetworkDiagnosis();
                    }
                });
                LinearLayout.LayoutParams layoutParams13 = new LinearLayout.LayoutParams(0, dp(42.0f), 1.0f);
                layoutParams13.topMargin = dp(8.0f);
                layoutParams13.rightMargin = dp(5.0f);
                LinearLayout linearLayout11 = new LinearLayout(context2);
                linearLayout11.setOrientation(0);
                linearLayout11.setGravity(16);
                LinearLayout.LayoutParams matchWrap26 = matchWrap();
                matchWrap26.topMargin = dp(8.0f);
                linearLayout.addView(linearLayout11, matchWrap26);
                linearLayout11.addView(dialogButton5, new LinearLayout.LayoutParams(0, dp(42.0f), 1.0f));
                Button dialogButton6 = dialogButton(context2, "清除崩溃记录", false, new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.15
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        CrashLogger.clear(SettingsDialog.this.activityContext);
                        Toast.makeText(SettingsDialog.this.activityContext, "已清除", 0).show();
                    }
                });
                LinearLayout.LayoutParams layoutParams14 = new LinearLayout.LayoutParams(0, dp(42.0f), 1.0f);
                layoutParams14.leftMargin = dp(5.0f);
                linearLayout11.addView(dialogButton6, layoutParams14);
                this.holidayStatusView = text(context2, MainActivity.lastHolidayUpdateText(context2), 11.5f, MainActivity.lastHolidayUpdateIncomplete(context2) ? MainActivity.COLOR_AMBER : MainActivity.COLOR_LABEL, 0);
                if (context2 instanceof MainActivity) {
                    ((MainActivity) context2).settingsDialogStatusView = this.holidayStatusView;
                }
                this.holidayStatusView.setLineSpacing(dp(2.0f), 1.0f);
                LinearLayout.LayoutParams matchWrap27 = matchWrap();
                matchWrap27.topMargin = dp(7.0f);
                linearLayout.addView(this.holidayStatusView, matchWrap27);
                View text20 = text(context2, "SalaryJump " + MainActivity.versionName(context2), 11.5f, MainActivity.COLOR_LABEL, 0);
                LinearLayout.LayoutParams matchWrap28 = matchWrap();
                matchWrap28.topMargin = dp(4.0f);
                linearLayout.addView(text20, matchWrap28);
                final String lastCrashText = CrashLogger.lastCrashText(context);
                if (lastCrashText != null) {
                    TextView text21 = text(context2, "⚠️ 上次崩溃：" + CrashLogger.summary(context) + "（点击查看）", 11.5f, MainActivity.COLOR_DANGER, 0);
                    text21.setLineSpacing(dp(2.0f), 1.0f);
                    text21.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.16
                        @Override // android.view.View.OnClickListener
                        public void onClick(View view) {
                            SettingsDialog.this.showCrashDialog(lastCrashText);
                        }
                    });
                    LinearLayout.LayoutParams matchWrap29 = matchWrap();
                    matchWrap29.topMargin = dp(6.0f);
                    linearLayout.addView(text21, matchWrap29);
                } else {
                }
                LinearLayout linearLayout12 = new LinearLayout(context2);
                linearLayout12.setOrientation(0);
                linearLayout12.setGravity(16);
                LinearLayout.LayoutParams matchWrap30 = matchWrap();
                matchWrap30.topMargin = dp(18.0f);
                linearLayout.addView(linearLayout12, matchWrap30);
                Button dialogButton7 = dialogButton(context2, "隐私锁", false, new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.17
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        SettingsDialog.this.showPrivacyDialog();
                    }
                });
                LinearLayout.LayoutParams layoutParams15 = new LinearLayout.LayoutParams(0, dp(42.0f), 1.0f);
                layoutParams15.rightMargin = dp(5.0f);
                linearLayout12.addView(dialogButton7, layoutParams15);
                Button dialogButton8 = dialogButton(context2, "月度战报", false, new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.18
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        try {
                            Intent intent = new Intent(context2, (Class<?>) MonthlyReportActivity.class);
                            if (!(context2 instanceof Activity)) {
                                intent.addFlags(268435456);
                            }
                            context2.startActivity(intent);
                        } catch (Throwable th) {
                            Toast.makeText(context2, "打开失败：" + th.getMessage(), 0).show();
                        }
                    }
                });
                LinearLayout.LayoutParams layoutParams16 = new LinearLayout.LayoutParams(0, dp(42.0f), 1.0f);
                layoutParams16.leftMargin = dp(5.0f);
                linearLayout12.addView(dialogButton8, layoutParams16);
                View dialogButton9 = dialogButton(context2, "加班与请假说明", false, new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.19
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        try {
                            Intent intent = new Intent(context2, (Class<?>) GuideActivity.class);
                            if (!(context2 instanceof Activity)) {
                                intent.addFlags(268435456);
                            }
                            context2.startActivity(intent);
                            SettingsDialog.this.dismiss();
                        } catch (Throwable th) {
                            Toast.makeText(context2, "打开失败：" + th.getMessage(), 0).show();
                        }
                    }
                });
                LinearLayout.LayoutParams layoutParams17 = new LinearLayout.LayoutParams(MainActivity.MATCH, dp(42.0f));
                layoutParams17.topMargin = dp(8.0f);
                linearLayout.addView(dialogButton9, layoutParams17);
            }
            if (this.section == 0 || this.section == MainActivity.STATE_LUNCH) {
              try {
                View sectionHeader4 = sectionHeader(context2, "🧍", "打工小人", "显示 · 尺寸 · 工位场景");
                LinearLayout.LayoutParams matchWrap32 = matchWrap();
                matchWrap32.topMargin = dp(26.0f);
                linearLayout.addView(sectionHeader4, matchWrap32);

                LinearLayout petVisibleRow = new LinearLayout(context2);
                petVisibleRow.setOrientation(0);
                petVisibleRow.setGravity(16);
                LinearLayout.LayoutParams matchWrap33 = matchWrap();
                matchWrap33.topMargin = dp(16.0f);
                linearLayout.addView(petVisibleRow, matchWrap33);
                petVisibleRow.addView(text(context2, "显示小人", 14.0f, MainActivity.COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, -2, 1.0f));
                Switch petVisible = new Switch(context2);
                this.petVisibleSwitch = petVisible;
                petVisible.setChecked(PetSettings.visible(context2));
                petVisibleRow.addView(petVisible, new LinearLayout.LayoutParams(-2, dp(42.0f)));

                LinearLayout petSizeRow = new LinearLayout(context2);
                petSizeRow.setOrientation(0);
                petSizeRow.setGravity(16);
                LinearLayout.LayoutParams matchWrap34 = matchWrap();
                matchWrap34.topMargin = dp(14.0f);
                linearLayout.addView(petSizeRow, matchWrap34);
                petSizeRow.addView(text(context2, "尺寸", 14.0f, MainActivity.COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, -2, 1.0f));
                this.petSizeValue = PetSettings.size(context2);
                this.petSizeButtons = new Button[3];
                for (int petPs = 0; petPs < 3; petPs++) {
                    final int petPi = petPs;
                    Button petPb = new Button(context2);
                    petPb.setText(PetSettings.sizeName(petPs));
                    petPb.setTextSize(13.0f);
                    petPb.setAllCaps(false);
                    petPb.setMinWidth(0);
                    petPb.setMinHeight(0);
                    petPb.setPadding(dp(10.0f), 0, dp(10.0f), 0);
                    petPb.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.39
                        @Override // android.view.View.OnClickListener
                        public void onClick(View view) {
                            try {
                                SettingsDialog.this.petSizeValue = petPi;
                                SettingsDialog.this.stylePetSizeButtons();
                            } catch (Throwable th) {
                                // 兜底：万一这里出问题，把原因说出来而不是直接崩掉
                                Toast.makeText(context2, "切换尺寸失败：" + th, 1).show();
                            }
                        }
                    });
                    LinearLayout.LayoutParams petLpB = new LinearLayout.LayoutParams(dp(56.0f), dp(40.0f));
                    if (petPs > 0) {
                        petLpB.leftMargin = dp(6.0f);
                    }
                    petSizeRow.addView(petPb, petLpB);
                    this.petSizeButtons[petPs] = petPb;
                }
                stylePetSizeButtons();

                LinearLayout petSceneRow = new LinearLayout(context2);
                petSceneRow.setOrientation(0);
                petSceneRow.setGravity(16);
                LinearLayout.LayoutParams matchWrap35 = matchWrap();
                matchWrap35.topMargin = dp(14.0f);
                linearLayout.addView(petSceneRow, matchWrap35);
                petSceneRow.addView(text(context2, "工位场景", 14.0f, MainActivity.COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, -2, 1.0f));
                Switch petScene = new Switch(context2);
                this.petSceneSwitch = petScene;
                petScene.setChecked(PetSettings.scene(context2));
                petSceneRow.addView(petScene, new LinearLayout.LayoutParams(-2, dp(42.0f)));

                TextView petHint = text(context2, "关掉小人后金额会居中；「工位场景」控制是否画书桌台灯", 11.5f, MainActivity.COLOR_LABEL, 0);
                petHint.setLineSpacing(dp(2.0f), 1.0f);
                LinearLayout.LayoutParams matchWrap36 = matchWrap();
                matchWrap36.topMargin = dp(8.0f);
                linearLayout.addView(petHint, matchWrap36);
              } catch (Throwable th) {
                // 兜底：分区构建失败时把原因说出来，而不是让设置页直接崩掉
                Toast.makeText(context2, "小人设置加载失败\n" + th, 1).show();
              }
            }
            LinearLayout linearLayout13 = new LinearLayout(context2);
            linearLayout13.setOrientation(0);
            linearLayout13.setGravity(21);
            LinearLayout.LayoutParams matchWrap31 = matchWrap();
            matchWrap31.topMargin = dp(10.0f);
            linearLayout.addView(linearLayout13, matchWrap31);
            Button dialogButton10 = dialogButton(context2, this.section > 0 ? "返回" : "取消", false, new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.20
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    SettingsDialog.this.dismiss();
                }
            });
            Button dialogButton11 = dialogButton(context2, "保存", true, new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.21
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    SettingsDialog.this.save();
                }
            });
            linearLayout13.addView(dialogButton10, buttonParams());
            linearLayout13.addView(dialogButton11, buttonParams());
            setContentView(scrollView);
            Window window = getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(0));
            }
        }

        private EditText numberInput(Context context, String str) {
            EditText editText = new EditText(context);
            editText.setInputType(2);
            editText.setText(str);
            editText.setTextSize(16.0f);
            editText.setTextColor(MainActivity.COLOR_TEXT);
            editText.setSingleLine(true);
            editText.setPadding(dp(14.0f), dp(10.0f), dp(14.0f), dp(10.0f));
            editText.setBackground(rounded(MainActivity.COLOR_BACKGROUND, 8, MainActivity.COLOR_BORDER, 1));
            return editText;
        }

        private Button waterWinTimeButton(final Context context, final boolean z) {
            final Button button = new Button(context);
            StringBuilder append = new StringBuilder().append(z ? "开始 " : "结束 ").append("\n");
            WaterReminderSettings waterReminderSettings = this.waterWorking;
            button.setText(append.append(MainActivity.formatTime(z ? waterReminderSettings.winStartMinutes : waterReminderSettings.winEndMinutes)).toString());
            button.setTextSize(14.0f);
            button.setTextColor(MainActivity.COLOR_TEXT);
            button.setAllCaps(false);
            button.setGravity(17);
            button.setBackground(rounded(MainActivity.COLOR_BACKGROUND, 8, MainActivity.COLOR_BORDER, 1));
            button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.22
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    boolean z2 = z;
                    SettingsDialog settingsDialog = SettingsDialog.this;
                    int i = z2 ? settingsDialog.waterWorking.winStartMinutes : settingsDialog.waterWorking.winEndMinutes;
                    new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.22.1
                        @Override // android.app.TimePickerDialog.OnTimeSetListener
                        public void onTimeSet(TimePicker timePicker, int i2, int i3) {
                            boolean z3 = z;
                                                        if (z3) {
                                SettingsDialog.this.waterWorking.winStartMinutes = (i2 * 60) + i3;
                            } else {
                                SettingsDialog.this.waterWorking.winEndMinutes = (i2 * 60) + i3;
                            }
                            Button button2 = button;
                            StringBuilder append2 = new StringBuilder().append(z ? "开始 " : "结束 ").append("\n");
                            boolean z4 = z;
                                                        button2.setText(append2.append(MainActivity.formatTime(z4 ? SettingsDialog.this.waterWorking.winStartMinutes : SettingsDialog.this.waterWorking.winEndMinutes)).toString());
                        }
                    }, i / 60, i % 60, true).show();
                }
            });
            return button;
        }

        private void addWaterMode(Context context, RadioGroup radioGroup, String str, boolean z) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setText(str);
            radioButton.setTextSize(14.0f);
            radioButton.setTextColor(MainActivity.COLOR_TEXT);
            radioButton.setChecked(z);
            radioGroup.addView(radioButton);
        }

        public void setWaterControlsEnabled(boolean z) {
            this.waterIntervalSpinner.setEnabled(z);
            this.waterTargetInput.setEnabled(z);
            for (int i = 0; i < this.drinkMlInputs.length; i++) {
                this.drinkMlInputs[i].setEnabled(z);
            }
            for (int i2 = 0; i2 < this.waterModeGroup.getChildCount(); i2++) {
                this.waterModeGroup.getChildAt(i2).setEnabled(z);
            }
        }

        private LinearLayout timeRow(Context context) {
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(0);
            return linearLayout;
        }

        private Button timeButton(final Context context, final String str, int i, boolean z) {
            final Button button = new Button(context);
            button.setText(str + "\n" + MainActivity.formatTime(i));
            button.setTextSize(14.0f);
            button.setTextColor(MainActivity.COLOR_TEXT);
            button.setAllCaps(false);
            button.setGravity(17);
            button.setBackground(rounded(MainActivity.COLOR_BACKGROUND, 8, MainActivity.COLOR_BORDER, 1));
            button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.23
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    int i2;
                    if (str.equals("上班")) {
                        i2 = SettingsDialog.this.working.workStartMinutes;
                    } else if (str.equals("下班")) {
                        i2 = SettingsDialog.this.working.workEndMinutes;
                    } else {
                        boolean equals = str.equals("午休开始");
                        SettingsDialog settingsDialog = SettingsDialog.this;
                        i2 = equals ? settingsDialog.working.breakStartMinutes : settingsDialog.working.breakEndMinutes;
                    }
                    new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.23.1
                        @Override // android.app.TimePickerDialog.OnTimeSetListener
                        public void onTimeSet(TimePicker timePicker, int i3, int i4) {
                            int i5 = (i3 * 60) + i4;
                            boolean equals2 = str.equals("上班");
                                                        if (equals2) {
                                SettingsDialog.this.working.workStartMinutes = i5;
                            } else {
                                boolean equals3 = str.equals("下班");
                                                                if (equals3) {
                                    SettingsDialog.this.working.workEndMinutes = i5;
                                } else {
                                    boolean equals4 = str.equals("午休开始");
                                                                        if (equals4) {
                                        SettingsDialog.this.working.breakStartMinutes = i5;
                                    } else {
                                        SettingsDialog.this.working.breakEndMinutes = i5;
                                    }
                                }
                            }
                            button.setText(str + "\n" + MainActivity.formatTime(i5));
                        }
                    }, i2 / 60, i2 % 60, true).show();
                }
            });
            return button;
        }

        private Button dialogButton(Context context, String str, boolean z, View.OnClickListener onClickListener) {
            GradientDrawable rounded;
            Button button = new Button(context);
            button.setText(str);
            button.setTextSize(14.0f);
            button.setAllCaps(false);
            button.setTextColor(z ? MainActivity.COLOR_ON_ACCENT : MainActivity.COLOR_TEXT);
            if (z) {
                rounded = rounded(MainActivity.COLOR_ACCENT, 8, 0, 0);
            } else {
                rounded = rounded(MainActivity.COLOR_BACKGROUND, 8, MainActivity.COLOR_BORDER, 1);
            }
            button.setBackground(rounded);
            button.setOnClickListener(onClickListener);
            return button;
        }

        public void save() {
            int target = SectionSave.targetFor(this.section);
            if (target == SectionSave.SALARY) {
                saveSalarySection();
            } else if (target == SectionSave.WATER) {
                saveWaterSection();
            } else if (target == SectionSave.TOOLS) {
                // 「工具与安全」分区里没有薪跳/喝水的控件，只有「打工小人」是可保存的。
                // 若照旧走 saveAllSections()，会去读没创建的 salaryInput → 空指针崩溃。
                savePetSection();
                dismiss();
            } else {
                saveAllSections();
            }
        }

        private void saveSalarySection() {
            if (this.salaryInput == null) {
                return;
            }
            try {
                double parseDouble = Double.parseDouble(this.salaryInput.getText().toString().trim().replace(",", ""));
                if (parseDouble >= 0.0d && parseDouble <= 1.0E8d) {
                    if (this.working.workEndMinutes <= this.working.workStartMinutes) {
                        showError("下班时间必须晚于上班时间。");
                        return;
                    }
                    if (this.working.breakStartMinutes > this.working.workStartMinutes && this.working.breakEndMinutes < this.working.workEndMinutes && this.working.breakEndMinutes > this.working.breakStartMinutes) {
                        this.working.dailySalary = parseDouble;
                        this.working.workMode = Math.max(0, Math.min(2, this.workModeSpinner.getSelectedItemPosition()));
                        this.working.useCalendar = this.calendarBox.isChecked();
                        this.working.overtimeEnabled = this.overtimeBox.isChecked();
                        int checkedRadioButtonId = this.overtimeRateGroup.getCheckedRadioButtonId();
                        int i = 15;
                        this.working.overtimeRateX10 = checkedRadioButtonId == 7001 ? 20 : checkedRadioButtonId == 7002 ? 30 : 15;
                        this.working.overtimeGraceMinutes = this.overtimeGraceValues[this.overtimeGraceSpinner.getSelectedItemPosition()];
                        if (this.paydaySwitch != null && this.paydayInput != null) {
                            boolean isChecked = this.paydaySwitch.isChecked();
                            try {
                                i = Integer.parseInt(this.paydayInput.getText().toString().trim());
                            } catch (Throwable th) {
                            }
                            if (i < 1 || i > 31) {
                                showError("发薪日请填 1~31 之间的数字。");
                                return;
                            }
                            PaydayReminder.set(getContext(), isChecked, i);
                        }
                        if (this.checkinSwitch != null) {
                            CheckInReminder.set(getContext(), this.checkinSwitch.isChecked(), this.checkinHour, this.checkinMinute);
                        }
                        this.listener.onSaved(this.working);
                        dismiss();
                        return;
                    }
                    showError("午休时间必须位于上班和下班时间之间。");
                    return;
                }
                showError("每天工资的数值不在合理范围内。");
            } catch (NumberFormatException e) {
                showError("请输入正确的每天工资。");
            }
        }

        private void saveWaterSection() {
            if (this.drinkIds == null) {
                return;
            }
            int i;
            int i2;
            boolean z;
            try {
                int i3 = this.waterWorking.targetMl;
                int i4 = this.waterWorking.cupMl;
                int i5 = this.waterWorking.reminderMode;
                int length = this.drinkIds.length;
                int[] iArr = new int[length];
                for (int i6 = 0; i6 < length; i6++) {
                    iArr[i6] = DrinkTypes.ml(getContext(), this.drinkIds[i6]);
                }
                boolean z2 = true;
                if (this.waterEnabledBox.isChecked()) {
                    try {
                        i3 = Integer.parseInt(this.waterTargetInput.getText().toString().trim());
                        for (int i7 = 0; i7 < length; i7++) {
                            iArr[i7] = Integer.parseInt(this.drinkMlInputs[i7].getText().toString().trim());
                        }
                        i4 = iArr[0];
                        if (!DrinkTypes.targetInRange(i3)) {
                            showError("每天喝水总量请设置在 100 到 10000 ml 之间。");
                            return;
                        }
                        for (i = 0; i < length; i++) {
                            if (!DrinkTypes.mlInRange(iArr[i])) {
                                showError(DrinkTypes.name(getContext(), this.drinkIds[i]) + " 容量请设置在 10 到 2000 ml 之间。");
                                return;
                            }
                        }
                        this.waterWorking.intervalMinutes = new int[]{30, 45, 60, 90, 120}[this.waterIntervalSpinner.getSelectedItemPosition()];
                        RadioGroup radioGroup = this.waterModeGroup;
                        int indexOfChild = radioGroup.indexOfChild(radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()));
                        i5 = WaterReminderSettings.modeFor(indexOfChild);
                    } catch (NumberFormatException e) {
                        showError("请输入正确的喝水总量和每次喝水量。");
                        return;
                    }
                }
                int[] iArr2 = new int[length];
                for (i2 = 0; i2 < length; i2++) {
                    String nameCheck = DrinkTypes.nameCheck(this.drinkNameInputs[i2].getText().toString());
                    if (nameCheck != null) {
                        showError("「" + DrinkTypes.baseName(getContext(), this.drinkIds[i2]) + "」的名称有问题：" + nameCheck + "。");
                        return;
                    }
                    try {
                        iArr2[i2] = Integer.parseInt(this.drinkKcalInputs[i2].getText().toString().trim());
                        if (!DrinkTypes.kcalInRange(iArr2[i2])) {
                            showError("「" + DrinkTypes.name(getContext(), this.drinkIds[i2]) + "」的热量请填 0 到 900 之间的整数。");
                            return;
                        }
                    } catch (NumberFormatException e2) {
                        showError("「" + DrinkTypes.name(getContext(), this.drinkIds[i2]) + "」的热量请填 0 到 900 之间的整数。");
                        return;
                    }
                }
                for (int i8 = 0; i8 < length; i8++) {
                    DrinkTypes.setName(getContext(), this.drinkIds[i8], this.drinkNameInputs[i8].getText().toString());
                    DrinkTypes.setKcal(getContext(), this.drinkIds[i8], iArr2[i8]);
                    DrinkTypes.setSugary(getContext(), this.drinkIds[i8], this.drinkSugarStates[i8]);
                }
                if (this.waterCustomWinBox == null || !this.waterCustomWinBox.isChecked()) {
                    this.waterWorking.windowMode = 0;
                } else {
                    if (this.waterWorking.winEndMinutes <= this.waterWorking.winStartMinutes) {
                        showError("喝水时段的结束时间必须晚于开始时间。");
                        return;
                    }
                    this.waterWorking.windowMode = 1;
                    WaterReminderSettings waterReminderSettings = this.waterWorking;
                    if (this.waterSkipBreakSwitch != null && !this.waterSkipBreakSwitch.isChecked()) {
                        z = false;
                        waterReminderSettings.skipBreak = z;
                        WaterReminderSettings waterReminderSettings2 = this.waterWorking;
                        if (this.waterWeekendSwitch != null || !this.waterWeekendSwitch.isChecked()) {
                            z2 = false;
                        }
                        waterReminderSettings2.weekend = z2;
                    }
                    z = true;
                    waterReminderSettings.skipBreak = z;
                    WaterReminderSettings waterReminderSettings22 = this.waterWorking;
                    if (this.waterWeekendSwitch != null) {
                    }
                    z2 = false;
                    waterReminderSettings22.weekend = z2;
                }
                this.waterWorking.enabled = this.waterEnabledBox.isChecked();
                this.waterWorking.targetMl = i3;
                this.waterWorking.cupMl = i4;
                this.waterWorking.reminderMode = i5;
                this.waterWorking.save(getContext());
                for (int i9 = 0; i9 < length; i9++) {
                    if (this.drinkIds[i9] != 0) {
                        DrinkTypes.setMl(getContext(), this.drinkIds[i9], iArr[i9]);
                    }
                }
                this.listener.onSaved(this.working);
                dismiss();
            } catch (NumberFormatException e3) {
                showError("请输入正确的喝水总量。");
            }
        }

        private void saveAllSections() {
            savePetSection();
            saveBackupSection();
            // 防御：完整设置下这些控件一定存在；万一从别的分区误入这里，
            // 宁可什么都不做，也不要空指针崩掉
            if (this.salaryInput == null || this.drinkIds == null) {
                return;
            }
            int i;
            int i2;
            boolean z;
            try {
                double parseDouble = Double.parseDouble(this.salaryInput.getText().toString().trim().replace(",", ""));
                if (parseDouble >= 0.0d && parseDouble <= 1.0E8d) {
                    if (this.working.workEndMinutes <= this.working.workStartMinutes) {
                        showError("下班时间必须晚于上班时间。");
                        return;
                    }
                    if (this.working.breakStartMinutes > this.working.workStartMinutes && this.working.breakEndMinutes < this.working.workEndMinutes && this.working.breakEndMinutes > this.working.breakStartMinutes) {
                        int i3 = this.waterWorking.targetMl;
                        int i4 = this.waterWorking.cupMl;
                        int i5 = this.waterWorking.reminderMode;
                        int length = this.drinkIds.length;
                        int[] iArr = new int[length];
                        for (int i6 = 0; i6 < length; i6++) {
                            iArr[i6] = DrinkTypes.ml(getContext(), this.drinkIds[i6]);
                        }
                        if (this.waterEnabledBox.isChecked()) {
                            try {
                                i3 = Integer.parseInt(this.waterTargetInput.getText().toString().trim());
                                for (int i7 = 0; i7 < length; i7++) {
                                    iArr[i7] = Integer.parseInt(this.drinkMlInputs[i7].getText().toString().trim());
                                }
                                i4 = iArr[0];
                                if (!DrinkTypes.targetInRange(i3)) {
                                    showError("每天喝水总量请设置在 100 到 10000 ml 之间。");
                                    return;
                                }
                                for (i = 0; i < length; i++) {
                                    if (!DrinkTypes.mlInRange(iArr[i])) {
                                        showError(DrinkTypes.name(getContext(), this.drinkIds[i]) + " 容量请设置在 10 到 2000 ml 之间。");
                                        return;
                                    }
                                }
                                this.waterWorking.intervalMinutes = new int[]{30, 45, 60, 90, 120}[this.waterIntervalSpinner.getSelectedItemPosition()];
                                RadioGroup radioGroup = this.waterModeGroup;
                                int indexOfChild = radioGroup.indexOfChild(radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()));
                                i5 = WaterReminderSettings.modeFor(indexOfChild);
                            } catch (NumberFormatException e) {
                                showError("请输入正确的喝水总量和每次喝水量。");
                                return;
                            }
                        }
                        int[] iArr2 = new int[length];
                        i2 = 0;
                        while (i2 < length) {
                            String nameCheck = DrinkTypes.nameCheck(this.drinkNameInputs[i2].getText().toString());
                            if (nameCheck != null) {
                                showError("「" + DrinkTypes.baseName(getContext(), this.drinkIds[i2]) + "」的名称有问题：" + nameCheck + "。");
                                return;
                            }
                            try {
                                iArr2[i2] = Integer.parseInt(this.drinkKcalInputs[i2].getText().toString().trim());
                                if (!DrinkTypes.kcalInRange(iArr2[i2])) {
                                    showError("「" + DrinkTypes.name(getContext(), this.drinkIds[i2]) + "」的热量请填 0 到 900 之间的整数。");
                                    return;
                                }
                            } catch (NumberFormatException e2) {
                                showError("「" + DrinkTypes.name(getContext(), this.drinkIds[i2]) + "」的热量请填 0 到 900 之间的整数。");
                                return;
                            }
                        }
                        for (int i8 = 0; i8 < length; i8++) {
                            DrinkTypes.setName(getContext(), this.drinkIds[i8], this.drinkNameInputs[i8].getText().toString());
                            DrinkTypes.setKcal(getContext(), this.drinkIds[i8], iArr2[i8]);
                            DrinkTypes.setSugary(getContext(), this.drinkIds[i8], this.drinkSugarStates[i8]);
                        }
                        if (this.waterCustomWinBox != null && this.waterCustomWinBox.isChecked()) {
                            if (this.waterWorking.winEndMinutes <= this.waterWorking.winStartMinutes) {
                                showError("喝水时段的结束时间必须晚于开始时间。");
                                return;
                            }
                            this.waterWorking.windowMode = 1;
                            WaterReminderSettings waterReminderSettings = this.waterWorking;
                            if (this.waterSkipBreakSwitch != null && !this.waterSkipBreakSwitch.isChecked()) {
                                z = false;
                                waterReminderSettings.skipBreak = z;
                                this.waterWorking.weekend = this.waterWeekendSwitch == null && this.waterWeekendSwitch.isChecked();
                            }
                            z = true;
                            waterReminderSettings.skipBreak = z;
                            this.waterWorking.weekend = this.waterWeekendSwitch == null && this.waterWeekendSwitch.isChecked();
                        } else {
                            this.waterWorking.windowMode = 0;
                        }
                        this.waterWorking.enabled = this.waterEnabledBox.isChecked();
                        this.waterWorking.targetMl = i3;
                        this.waterWorking.cupMl = i4;
                        this.waterWorking.reminderMode = i5;
                        this.waterWorking.save(getContext());
                        for (int i9 = 0; i9 < length; i9++) {
                            if (this.drinkIds[i9] != 0) {
                                DrinkTypes.setMl(getContext(), this.drinkIds[i9], iArr[i9]);
                            }
                        }
                        this.working.dailySalary = parseDouble;
                        this.working.workMode = Math.max(0, Math.min(2, this.workModeSpinner.getSelectedItemPosition()));
                        this.working.useCalendar = this.calendarBox.isChecked();
                        this.working.overtimeEnabled = this.overtimeBox.isChecked();
                        int checkedRadioButtonId = this.overtimeRateGroup.getCheckedRadioButtonId();
                        int i10 = 15;
                        this.working.overtimeRateX10 = checkedRadioButtonId == 7001 ? 20 : checkedRadioButtonId == 7002 ? 30 : 15;
                        this.working.overtimeGraceMinutes = this.overtimeGraceValues[this.overtimeGraceSpinner.getSelectedItemPosition()];
                        if (this.paydaySwitch != null && this.paydayInput != null) {
                            boolean isChecked = this.paydaySwitch.isChecked();
                            try {
                                i10 = Integer.parseInt(this.paydayInput.getText().toString().trim());
                            } catch (Throwable th) {
                            }
                            if (i10 < 1 || i10 > 31) {
                                showError("发薪日请填 1~31 之间的数字。");
                                return;
                            }
                            PaydayReminder.set(getContext(), isChecked, i10);
                        }
                        if (this.checkinSwitch != null) {
                            CheckInReminder.set(getContext(), this.checkinSwitch.isChecked(), this.checkinHour, this.checkinMinute);
                        }
                        this.listener.onSaved(this.working);
                        dismiss();
                        return;
                    }
                    showError("午休时间必须位于上班和下班时间之间。");
                    return;
                }
                showError("每天工资的数值不在合理范围内。");
            } catch (NumberFormatException e3) {
                showError("请输入正确的每天工资。");
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void showPrivacyDialog() {
            final Context context = getContext();
            if (!PrivacyLock.isEnabled(context)) {
                showPinForm();
                return;
            }
            final boolean isBiometricAllowed = PrivacyLock.isBiometricAllowed(context);
            boolean biometricAvailable = PrivacyLock.biometricAvailable(context);
            ArrayList arrayList = new ArrayList();
            arrayList.add("修改 PIN");
            if (biometricAvailable) {
                arrayList.add("指纹解锁：" + (isBiometricAllowed ? "已开启" : "已关闭"));
            }
            arrayList.add("关闭隐私锁");
            final String[] strArr = (String[]) arrayList.toArray(new String[0]);
            AlertDialog.Builder items = new AlertDialog.Builder(context).setTitle("隐私锁").setItems(strArr, new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.24
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    String str = strArr[i];
                    if ("修改 PIN".equals(str)) {
                        SettingsDialog.this.showPinVerifyDialog();
                        return;
                    }
                    if (str.startsWith("指纹解锁")) {
                        PrivacyLock.setBiometricAllowed(context, !isBiometricAllowed);
                        Toast.makeText(context, !isBiometricAllowed ? "已开启指纹解锁" : "已关闭指纹解锁", 0).show();
                    } else if ("关闭隐私锁".equals(str)) {
                        new AlertDialog.Builder(context).setTitle("关闭隐私锁").setMessage("关闭后应用不再需要 PIN 解锁，确定吗？").setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("关闭", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.24.1
                            @Override // android.content.DialogInterface.OnClickListener
                            public void onClick(DialogInterface dialogInterface2, int i2) {
                                PrivacyLock.clear(context);
                                Toast.makeText(context, "隐私锁已关闭", 0).show();
                            }
                        }).show();
                    }
                }
            });
            items.setNegativeButton("取消", (DialogInterface.OnClickListener) null).show();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void showPinVerifyDialog() {
            final Context context = getContext();
            final EditText editText = new EditText(context);
            editText.setInputType(130);
            editText.setTextSize(18.0f);
            editText.setSingleLine(true);
            editText.setHint("当前 PIN");
            editText.setPadding(dp(14.0f), dp(10.0f), dp(14.0f), dp(10.0f));
            editText.setBackground(rounded(MainActivity.COLOR_BACKGROUND, 9, MainActivity.COLOR_BORDER, 1));
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(1);
            linearLayout.setPadding(dp(22.0f), dp(8.0f), dp(22.0f), dp(4.0f));
            linearLayout.addView(editText, matchWrap());
            new AlertDialog.Builder(context).setTitle("验证当前 PIN").setView(linearLayout).setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("下一步", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.25
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (PrivacyLock.verify(context, editText.getText().toString().trim())) {
                        SettingsDialog.this.showPinForm();
                    } else {
                        Toast.makeText(context, "PIN 不正确", 0).show();
                    }
                }
            }).show();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void showPinForm() {
            final Context context = getContext();
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(1);
            linearLayout.setPadding(dp(22.0f), dp(8.0f), dp(22.0f), dp(4.0f));
            final EditText editText = new EditText(context);
            editText.setInputType(130);
            editText.setTextSize(18.0f);
            editText.setSingleLine(true);
            editText.setHint("新 PIN（4 位数字）");
            editText.setPadding(dp(14.0f), dp(10.0f), dp(14.0f), dp(10.0f));
            editText.setBackground(rounded(MainActivity.COLOR_BACKGROUND, 9, MainActivity.COLOR_BORDER, 1));
            linearLayout.addView(editText, matchWrap());
            final EditText editText2 = new EditText(context);
            editText2.setInputType(130);
            editText2.setTextSize(18.0f);
            editText2.setSingleLine(true);
            editText2.setHint("再输一次");
            editText2.setPadding(dp(14.0f), dp(10.0f), dp(14.0f), dp(10.0f));
            editText2.setBackground(rounded(MainActivity.COLOR_BACKGROUND, 9, MainActivity.COLOR_BORDER, 1));
            LinearLayout.LayoutParams matchWrap = matchWrap();
            matchWrap.topMargin = dp(8.0f);
            linearLayout.addView(editText2, matchWrap);
            final AlertDialog create = new AlertDialog.Builder(context).setTitle("设置隐私锁").setView(linearLayout).setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("保存", (DialogInterface.OnClickListener) null).create();
            create.show();
            create.getButton(MainActivity.MATCH).setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.26
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    final String trim = editText.getText().toString().trim();
                    String trim2 = editText2.getText().toString().trim();
                    if (!PrivacyLock.isValidPin(trim)) {
                        Toast.makeText(context, "PIN 需要 4 位数字", 0).show();
                        return;
                    }
                    if (!trim.equals(trim2)) {
                        Toast.makeText(context, "两次输入不一致", 0).show();
                        return;
                    }
                    if (PrivacyLock.isWeakPin(trim)) {
                        new AlertDialog.Builder(context).setTitle("PIN 过于简单").setMessage("连续或重复的数字容易被猜到，仍要使用吗？").setNegativeButton("重设", (DialogInterface.OnClickListener) null).setPositiveButton("仍要使用", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.26.1
                            @Override // android.content.DialogInterface.OnClickListener
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PrivacyLock.setPin(context, trim);
                                Toast.makeText(context, "隐私锁已开启", 0).show();
                                create.dismiss();
                            }
                        }).show();
                    } else {
                        PrivacyLock.setPin(context, trim);
                        Toast.makeText(context, "隐私锁已开启", 0).show();
                        create.dismiss();
                    }
                }
            });
        }

        public void applySugarStyle(Button button, boolean z) {
            button.setText(z ? "含糖" : "无糖");
            button.setTextColor(z ? MainActivity.COLOR_AMBER : MainActivity.COLOR_ACCENT);
            button.setBackground(rounded(z ? MainActivity.COLOR_CHIP_BG : MainActivity.COLOR_SOFT_BG, 9, z ? MainActivity.COLOR_AMBER : MainActivity.COLOR_SOFT_BORDER, 2));
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void runNetworkDiagnosis() {
            final Context context = this.activityContext;
            Toast.makeText(context, "正在诊断，请稍候…", 0).show();
            final Handler handler = new Handler(Looper.getMainLooper());
            new Thread(new Runnable() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.27
                @Override // java.lang.Runnable
                public void run() {
                    final String run = NetDiag.run(context);
                    handler.post(new Runnable() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.27.1
                        @Override // java.lang.Runnable
                        public void run() {
                            try {
                                TextView textView = new TextView(context);
                                textView.setText(run);
                                textView.setTextSize(12.0f);
                                textView.setTextColor(MainActivity.COLOR_TEXT);
                                textView.setTypeface(Typeface.MONOSPACE);
                                textView.setPadding(SettingsDialog.this.dp(14.0f), SettingsDialog.this.dp(12.0f), SettingsDialog.this.dp(14.0f), SettingsDialog.this.dp(12.0f));
                                textView.setTextIsSelectable(true);
                                ScrollView scrollView = new ScrollView(context);
                                scrollView.addView(textView, new FrameLayout.LayoutParams(MainActivity.MATCH, -2));
                                new AlertDialog.Builder(context).setTitle("网络诊断结果").setView(scrollView).setPositiveButton("知道了", (DialogInterface.OnClickListener) null).show();
                            } catch (Throwable th) {
                                Toast.makeText(context, run, 1).show();
                            }
                        }
                    });
                }
            }).start();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void showCrashDialog(String str) {
            final Context context = getContext();
            TextView textView = new TextView(context);
            textView.setText(str);
            textView.setTextSize(11.5f);
            textView.setTextColor(MainActivity.COLOR_TEXT);
            textView.setTypeface(Typeface.MONOSPACE);
            textView.setPadding(dp(14.0f), dp(12.0f), dp(14.0f), dp(12.0f));
            textView.setTextIsSelectable(true);
            ScrollView scrollView = new ScrollView(context);
            scrollView.addView(textView, new FrameLayout.LayoutParams(MainActivity.MATCH, -2));
            new AlertDialog.Builder(context).setTitle("崩溃详情").setView(scrollView).setNegativeButton("关闭", (DialogInterface.OnClickListener) null).setNeutralButton("清除记录", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.28
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    CrashLogger.clear(context);
                    Toast.makeText(context, "已清除，重新打开设置生效", 0).show();
                }
            }).show();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void updateHolidays() {
            HolidayUpdater.markStage(this.activityContext, "已点击按钮，准备请求");
            try {
                Context context = this.activityContext;
                if (context instanceof MainActivity) {
                    ((MainActivity) context).setHolidayStatus("已点击，正在获取…", false);
                }
            } catch (Throwable th) {
            }
            try {
                final Activity anyActivityOf = Contexts.anyActivityOf(this.activityContext);
                if (anyActivityOf == null) {
                    throw new IllegalStateException("无法获取宿主 Activity");
                }
                this.holidayUpdating = true;
                this.holidayStartedAt = System.currentTimeMillis();
                this.pollHandler.post(this.holidayPoller);
                if (this.holidayBtn != null) {
                    this.holidayBtn.setEnabled(false);
                    this.holidayBtn.setText("获取中…");
                }
                final Button button = this.holidayBtn;
                if (button != null) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.29
                        @Override // java.lang.Runnable
                        public void run() {
                            try {
                                if (!button.isEnabled()) {
                                    button.setEnabled(true);
                                    button.setText("更新节假日");
                                    HolidayUpdater.markStage(SettingsDialog.this.activityContext, "超时（35 秒无响应）");
                                    if (SettingsDialog.this.holidayStatusView != null) {
                                        SettingsDialog.this.holidayStatusView.setText("⚠️ 更新超时（35 秒无响应），请检查网络后重试");
                                        SettingsDialog.this.holidayStatusView.setTextColor(MainActivity.COLOR_DANGER);
                                    }
                                }
                            } catch (Throwable th2) {
                            }
                        }
                    }, 35000L);
                }
                try {
                    Toast.makeText(anyActivityOf, "正在获取节假日数据…", 0).show();
                } catch (Throwable th2) {
                }
                HolidayUpdater.check(anyActivityOf, new HolidayUpdater.Callback() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.30
                    @Override // com.salaryjump.app.HolidayUpdater.Callback
                    public void onResult(boolean z, String str) {
                        try {
                            SettingsDialog.this.onHolidayResult(anyActivityOf, z, str);
                        } catch (Throwable th3) {
                        }
                    }
                });
            } catch (Throwable th3) {
                try {
                    onHolidayResult(Contexts.anyActivityOf(this.activityContext), false, "启动更新失败：" + th3);
                } catch (Throwable th4) {
                }
            }
        }

        public void onHolidayResult(Activity activity, boolean z, String str) {
            Context context = activity;
            if (activity == null) {
                context = hostActivity();
            }
            this.holidayUpdating = false;
            try {
                this.pollHandler.removeCallbacks(this.holidayPoller);
            } catch (Throwable th) {
            }
            try {
                if (this.holidayBtn != null) {
                    this.holidayBtn.setEnabled(true);
                    this.holidayBtn.setText("更新节假日");
                }
            } catch (Throwable th2) {
            }
            if (z && context != null) {
                try {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).onHolidayDataUpdated();
                    } else {
                        WorkdayHelper.applyOnlineData(context);
                        StatutoryHolidays.applyOnlineData(context);
                        WorkdayHelper.invalidate();
                        SalaryWidgetProvider.refreshAll(context);
                    }
                } catch (Throwable th3) {
                    str = str + "\n\n（数据已保存，但界面刷新出错：" + th3 + "）";
                }
            }
            try {
                if (this.holidayStatusView != null) {
                    this.holidayStatusView.setText(str);
                    this.holidayStatusView.setTextColor(z ? MainActivity.COLOR_ACCENT : MainActivity.COLOR_DANGER);
                }
            } catch (Throwable th4) {
            }
            if (context == null) {
                context = getContext();
            }
            try {
                Toast.makeText(context, str, 1).show();
            } catch (Throwable th5) {
            }
            try {
                new AlertDialog.Builder(context).setTitle(z ? "更新成功" : "更新失败").setMessage(str).setPositiveButton("知道了", (DialogInterface.OnClickListener) null).show();
            } catch (Throwable th6) {
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void showBackupDialog() {
            new AlertDialog.Builder(getContext()).setTitle("数据备份").setMessage("所有数据都只存在本机。换手机或重装前，建议先导出备份。").setNegativeButton("取消", (DialogInterface.OnClickListener) null).setNeutralButton("恢复", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.32
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
                        intent.addCategory("android.intent.category.OPENABLE");
                        intent.setType("*/*");
                        Activity anyActivityOf = Contexts.anyActivityOf(SettingsDialog.this.activityContext);
                        if (anyActivityOf == null) {
                            throw new IllegalStateException("无法获取宿主 Activity");
                        }
                        anyActivityOf.startActivityForResult(intent, MainActivity.REQ_BACKUP_IMPORT);
                    } catch (Throwable th) {
                        Toast.makeText(SettingsDialog.this.getContext(), "无法打开文件选择器", 1).show();
                    }
                }
            }).setPositiveButton("导出", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.31
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        Intent intent = new Intent("android.intent.action.CREATE_DOCUMENT");
                        intent.addCategory("android.intent.category.OPENABLE");
                        intent.setType("application/json");
                        intent.putExtra("android.intent.extra.TITLE", "SalaryJump备份");
                        Activity anyActivityOf = Contexts.anyActivityOf(SettingsDialog.this.activityContext);
                        if (anyActivityOf == null) {
                            throw new IllegalStateException("无法获取宿主 Activity");
                        }
                        anyActivityOf.startActivityForResult(intent, MainActivity.REQ_BACKUP_EXPORT);
                    } catch (Throwable th) {
                        Toast.makeText(SettingsDialog.this.getContext(), "无法打开文件选择器", 1).show();
                    }
                }
            }).show();
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void showRemoveDrinkDialog(final int i) {
            String removeCheck = DrinkTypes.removeCheck(getContext(), i);
            if (removeCheck != null) {
                AlertDialog.Builder message = new AlertDialog.Builder(getContext()).setTitle("不能删除").setMessage(removeCheck);
                message.setPositiveButton("知道了", (DialogInterface.OnClickListener) null).show();
            } else {
                new AlertDialog.Builder(getContext()).setTitle("删除饮品").setMessage("确定删除「" + DrinkTypes.name(getContext(), i) + "」吗？\n\n该饮品还没有喝过，删除不会影响任何记录。").setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("删除", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.33
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        DrinkTypes.remove(SettingsDialog.this.getContext(), i);
                        Toast.makeText(SettingsDialog.this.getContext(), "已删除，重新打开设置可看到最新列表", 1).show();
                    }
                }).show();
            }
        }

        public void showPresetDialog() {
            float f;
            Context context = getContext();
            List<DrinkTypes.Preset> presets = DrinkTypes.presets(context);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(1);
            linearLayout.setPadding(dp(20.0f), dp(4.0f), dp(20.0f), dp(4.0f));
            ScrollView scrollView = new ScrollView(context);
            scrollView.addView(linearLayout, new FrameLayout.LayoutParams(MainActivity.MATCH, -2));
            final AlertDialog create = new AlertDialog.Builder(context).setTitle("参考表（点条目可编辑）").setView(scrollView).setNegativeButton("关闭", (DialogInterface.OnClickListener) null).create();
            int[] iArr = new int[presets.size()];
            for (int i = 0; i < presets.size(); i++) {
                iArr[i] = i;
            }
            for (int i2 = 0; i2 < presets.size(); i2++) {
                DrinkTypes.Preset preset = presets.get(i2);
                final int i3 = iArr[i2];
                LinearLayout linearLayout2 = new LinearLayout(context);
                linearLayout2.setOrientation(0);
                linearLayout2.setGravity(16);
                linearLayout2.setPadding(dp(14.0f), 0, dp(12.0f), 0);
                linearLayout2.setBackground(MainActivity.softPill(dp(10.0f), dp(1.0f)));
                linearLayout2.addView(text(context, preset.name, 13.5f, MainActivity.COLOR_TEXT, 0), new LinearLayout.LayoutParams(0, dp(44.0f), 1.0f));
                TextView text = text(context, preset.kcal + " kcal", 12.0f, MainActivity.COLOR_MUTED, 0);
                text.setGravity(21);
                linearLayout2.addView(text, new LinearLayout.LayoutParams(dp(60.0f), dp(44.0f)));
                TextView text2 = text(context, preset.sugar ? "含糖" : "无糖", 11.5f, preset.sugar ? MainActivity.COLOR_AMBER : MainActivity.COLOR_ACCENT, 0);
                text2.setGravity(21);
                linearLayout2.addView(text2, new LinearLayout.LayoutParams(dp(42.0f), dp(44.0f)));
                TextView text3 = text(context, "编辑", 12.0f, MainActivity.COLOR_BLUE, 0);
                text3.setGravity(21);
                linearLayout2.addView(text3, new LinearLayout.LayoutParams(dp(40.0f), dp(44.0f)));
                linearLayout2.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.34
                    @Override // android.view.View.OnClickListener
                    public void onClick(View view) {
                        create.dismiss();
                        SettingsDialog.this.showPresetEditDialog(i3);
                    }
                });
                LinearLayout.LayoutParams matchWrap = matchWrap();
                matchWrap.topMargin = dp(4.0f);
                linearLayout.addView(linearLayout2, matchWrap);
            }
            if (!presets.isEmpty()) {
                f = 10.0f;
            } else {
                f = 10.0f;
                TextView text4 = text(context, "参考表是空的，点下面新增一条吧", 13.0f, MainActivity.COLOR_MUTED, 0);
                text4.setGravity(17);
                text4.setPadding(0, dp(26.0f), 0, dp(26.0f));
                linearLayout.addView(text4, matchWrap());
            }
            Button button = new Button(context);
            button.setText("＋ 添加参考项");
            button.setTextSize(13.5f);
            button.setTextColor(MainActivity.COLOR_BLUE);
            button.setAllCaps(false);
            button.setMinWidth(0);
            button.setMinHeight(0);
            button.setBackground(MainActivity.softPill(dp(20.0f), dp(1.5f)));
            button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.35
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    create.dismiss();
                    SettingsDialog.this.showPresetEditDialog(MainActivity.MATCH);
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MainActivity.MATCH, dp(46.0f));
            layoutParams.topMargin = dp(f);
            linearLayout.addView(button, layoutParams);
            TextView text5 = text(context, "参考表决定「添加饮品」时能选什么；点条目可修改，被饮品使用的条目不能删除。", 11.5f, MainActivity.COLOR_LABEL, 0);
            text5.setLineSpacing(dp(2.0f), 1.0f);
            LinearLayout.LayoutParams matchWrap2 = matchWrap();
            matchWrap2.topMargin = dp(f);
            linearLayout.addView(text5, matchWrap2);
            create.show();
            Window window = create.getWindow();
            if (window != null) {
                window.setLayout((int) (context.getResources().getDisplayMetrics().widthPixels * 0.92d), -2);
            }
        }

        public void showPresetEditDialog(final int i) {
            final Context context = getContext();
            boolean z = i < 0;
            final EditText editText = new EditText(context);
            final EditText editText2 = new EditText(context);
            final boolean[] zArr = new boolean[1];
            if (!z) {
                List<DrinkTypes.Preset> presets = DrinkTypes.presets(context);
                if (i >= presets.size()) {
                    return;
                }
                DrinkTypes.Preset preset = presets.get(i);
                editText.setText(preset.name);
                editText2.setText(String.valueOf(preset.kcal));
                zArr[0] = preset.sugar;
            } else {
                editText2.setText("0");
                zArr[0] = false;
            }
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(1);
            linearLayout.setPadding(dp(24.0f), dp(10.0f), dp(24.0f), dp(4.0f));
            editText.setHint("饮品名称（最多 8 字）");
            editText.setTextSize(15.0f);
            editText.setTextColor(MainActivity.COLOR_TEXT);
            editText.setSingleLine(true);
            editText.setPadding(dp(12.0f), dp(10.0f), dp(12.0f), dp(10.0f));
            editText.setBackground(MainActivity.softPill(dp(9.0f), dp(1.0f)));
            linearLayout.addView(editText, matchWrap());
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setOrientation(0);
            linearLayout2.setGravity(16);
            LinearLayout.LayoutParams matchWrap = matchWrap();
            matchWrap.topMargin = dp(12.0f);
            linearLayout.addView(linearLayout2, matchWrap);
            editText2.setInputType(2);
            editText2.setTextSize(15.0f);
            editText2.setTextColor(MainActivity.COLOR_TEXT);
            editText2.setGravity(17);
            editText2.setSingleLine(true);
            editText2.setPadding(dp(8.0f), dp(10.0f), dp(8.0f), dp(10.0f));
            editText2.setBackground(MainActivity.softPill(dp(9.0f), dp(1.0f)));
            linearLayout2.addView(editText2, new LinearLayout.LayoutParams(0, dp(46.0f), 1.0f));
            TextView text = text(context, "kcal / 100ml", 12.5f, MainActivity.COLOR_MUTED, 0);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
            layoutParams.leftMargin = dp(10.0f);
            linearLayout2.addView(text, layoutParams);
            final Button button = new Button(context);
            button.setTextSize(12.5f);
            button.setAllCaps(false);
            button.setMinWidth(0);
            button.setMinHeight(0);
            applySugarStyle(button, zArr[0]);
            button.setOnClickListener(new View.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.36
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    zArr[0] = !zArr[0];
                    SettingsDialog.this.applySugarStyle(button, zArr[0]);
                }
            });
            LinearLayout.LayoutParams matchWrap2 = matchWrap();
            matchWrap2.topMargin = dp(12.0f);
            linearLayout.addView(button, matchWrap2);
            final boolean z2 = z;
            AlertDialog.Builder positiveButton = new AlertDialog.Builder(context).setTitle(z ? "添加参考项" : "编辑参考项").setView(linearLayout).setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("保存", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.37
                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialogInterface, int i2) {
                    int i3;
                    try {
                        i3 = Integer.parseInt(editText2.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        i3 = 0;
                    }
                    boolean z3 = z2;
                    Context context2 = context;
                    String presetAdd = z3 ? DrinkTypes.presetAdd(context2, editText.getText().toString(), i3, zArr[0]) : DrinkTypes.presetUpdate(context2, i, editText.getText().toString(), i3, zArr[0]);
                    Context context3 = context;
                    if (presetAdd == null) {
                        Toast.makeText(context3, z2 ? "已添加参考项" : "已保存", 0).show();
                        SettingsDialog.this.showPresetDialog();
                    } else {
                        Toast.makeText(context3, presetAdd, 1).show();
                    }
                }
            });
            if (!z2) {
                positiveButton.setNeutralButton("删除", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.38
                    @Override // android.content.DialogInterface.OnClickListener
                    public void onClick(DialogInterface dialogInterface, int i2) {
                        String presetRemoveCheck = DrinkTypes.presetRemoveCheck(context, i);
                        if (presetRemoveCheck != null) {
                            new AlertDialog.Builder(context).setTitle("不能删除").setMessage(presetRemoveCheck).setPositiveButton("知道了", (DialogInterface.OnClickListener) null).show();
                        } else {
                            new AlertDialog.Builder(context).setTitle("删除参考项").setMessage("确定从参考表里删除「" + DrinkTypes.presets(context).get(i).name + "」吗？").setNegativeButton("取消", (DialogInterface.OnClickListener) null).setPositiveButton("删除", new DialogInterface.OnClickListener() { // from class: com.salaryjump.app.MainActivity.SettingsDialog.38.1
                                @Override // android.content.DialogInterface.OnClickListener
                                public void onClick(DialogInterface dialogInterface2, int i3) {
                                    DrinkTypes.presetRemove(context, i);
                                    Toast.makeText(context, "已删除", 0).show();
                                    SettingsDialog.this.showPresetDialog();
                                }
                            }).show();
                        }
                    }
                });
            }
            positiveButton.show();
        }

        /** 高亮当前选中的尺寸档位 */
        private void stylePetSizeButtons() {
            if (this.petSizeButtons == null) {
                return;
            }
            for (int petI = 0; petI < this.petSizeButtons.length; petI++) {
                Button petB = this.petSizeButtons[petI];
                if (petB == null) {
                    continue;
                }
                boolean petSel = petI == this.petSizeValue;
                petB.setTextColor(petSel ? -1 : MainActivity.COLOR_TEXT);
                petB.setBackground(rounded(petSel ? MainActivity.COLOR_ACCENT : MainActivity.COLOR_BACKGROUND, 9,
                        petSel ? MainActivity.COLOR_ACCENT : MainActivity.COLOR_BORDER, 1));
            }
        }

        /** 自动备份的状态说明 */
        private String buildBackupStateText() {
            StringBuilder sb = new StringBuilder();
            long last = BackupScheduler.lastBackupAt(getContext());
            if (last <= 0L) {
                sb.append("上次备份：还没备份过");
            } else {
                java.text.SimpleDateFormat fmt =
                        new java.text.SimpleDateFormat("M 月 d 日 HH:mm", java.util.Locale.getDefault());
                sb.append("上次备份：").append(fmt.format(new java.util.Date(last)));
            }
            String state = BackupScheduler.lastState(getContext());
            if (state.length() > 0) {
                sb.append(" · ").append(state);
            }
            if (!BackupScheduler.canScheduleExact(getContext())) {
                sb.append("\n⚠️ 未授予「精确闹钟」权限，备份时间可能不准");
            }
            sb.append("\n备份文件为明文 JSON，存放在你选的目录里");
            return sb.toString();
        }

        /** 保存自动备份设置 */
        private void saveBackupSection() {
            try {
                Context backupCtx = getContext();
                if (this.backupAutoSwitch != null) {
                    boolean want = this.backupAutoSwitch.isChecked();
                    // 开启但没选目录 → 提示先选目录
                    if (want && BackupScheduler.treeUri(backupCtx).length() == 0) {
                        Toast.makeText(backupCtx, "请先选择「备份文件夹」", 1).show();
                        BackupScheduler.setEnabled(backupCtx, false);
                    } else {
                        BackupScheduler.setEnabled(backupCtx, want);
                    }
                }
                BackupScheduler.setOption(backupCtx, BackupScheduler.KEY_PERIOD, this.backupPeriodValue);
                BackupScheduler.setOption(backupCtx, BackupScheduler.KEY_KEEP, this.backupKeepValue);
                BackupScheduler.setOption(backupCtx, BackupScheduler.KEY_HOUR, this.backupHourValue);
                BackupScheduler.setOption(backupCtx, BackupScheduler.KEY_MINUTE, this.backupMinuteValue);
                BackupScheduler.schedule(backupCtx);
            } catch (Throwable unused) {
            }
        }

        /** 保存「打工小人」三项设置 */
        private void savePetSection() {
            try {
                Context petCtx = getContext();
                if (this.petVisibleSwitch != null) {
                    PetSettings.setVisible(petCtx, this.petVisibleSwitch.isChecked());
                }
                if (this.petSceneSwitch != null) {
                    PetSettings.setScene(petCtx, this.petSceneSwitch.isChecked());
                }
                PetSettings.setSize(petCtx, this.petSizeValue);
            } catch (Throwable unused) {
            }
        }

        private void showError(String str) {
            new AlertDialog.Builder(getContext()).setTitle("设置有误").setMessage(str).setPositiveButton("知道了", (DialogInterface.OnClickListener) null).show();
        }

        private LinearLayout sectionHeader(Context context, String str, String str2, String str3) {
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(1);
            linearLayout.setPadding(dp(16.0f), dp(13.0f), dp(16.0f), dp(13.0f));
            linearLayout.setBackground(UiKit.hero(MainActivity.HERO_COLORS, dp(16.0f)));
            linearLayout.setElevation(dp(4.0f));
            linearLayout.addView(text(context, str + "\u3000" + str2, 15.5f, MainActivity.MATCH, 1), wrapWrap());
            TextView text = text(context, str3, 11.0f, -939524097, 0);
            LinearLayout.LayoutParams wrapWrap = wrapWrap();
            wrapWrap.topMargin = dp(4.0f);
            linearLayout.addView(text, wrapWrap);
            return linearLayout;
        }

        private TextView text(Context context, String str, float f, int i, int i2) {
            TextView textView = new TextView(context);
            textView.setText(str);
            textView.setTextSize(f);
            textView.setTextColor(i);
            textView.setTypeface(Typeface.create("sans", i2));
            return textView;
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
            return new LinearLayout.LayoutParams(MainActivity.MATCH, -2);
        }

        private LinearLayout.LayoutParams wrapWrap() {
            return new LinearLayout.LayoutParams(-2, -2);
        }

        private LinearLayout.LayoutParams timeButtonParams(boolean z) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(60.0f), 1.0f);
            layoutParams.setMargins(z ? 0 : dp(6.0f), 0, z ? dp(6.0f) : 0, 0);
            return layoutParams;
        }

        private LinearLayout.LayoutParams buttonParams() {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dp(88.0f), dp(42.0f));
            layoutParams.leftMargin = dp(8.0f);
            return layoutParams;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public int dp(float f) {
            return Math.round(f * this.density);
        }
    }
}
