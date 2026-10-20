package com.salaryjump.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.animation.LinearInterpolator;
import com.salaryjump.app.MainActivity;
import java.util.Calendar;

/* loaded from: classes.dex */
public class WorkerPetView extends View {
    private ValueAnimator animator;
    private final Context appContext;
    private long deskCheckedAtMs;
    private boolean deskVisible;
    private int fatigue;
    private long lastFrameMs;
    private final boolean night;
    /** 连戳判定窗口：两次点击间隔超过它就算重新开始 */
    private static final long POKE_CHAIN_WINDOW_MS = 1200L;

    private long lastPokeMs;
    private int pokeCount;
    private int pose;
    private int reactionPose;
    private long reactionUntilMs;
    private boolean scene;
    private float t;

    static /* synthetic */ float access$116(WorkerPetView workerPetView, float f) {
        float f2 = workerPetView.t + f;
        workerPetView.t = f2;
        return f2;
    }

    public WorkerPetView(Context context) {
        super(context);
        this.pose = 1;
        this.reactionPose = -1;
        this.night = MainActivity.NIGHT;
        this.appContext = context;
    }

    public void setState(int i) {
        int resolvePose = WorkerPet.resolvePose(i, Calendar.getInstance(), workEndMinutes(this.appContext));
        if (resolvePose != this.pose) {
            this.pose = resolvePose;
            invalidate();
        }
    }

    private static int workEndMinutes(Context context) {
        try {
            return MainActivity.SalarySettings.load(context.getSharedPreferences("salary_jump", 0)).workEndMinutes;
        } catch (Throwable th) {
            return 0;
        }
    }

    public void setPose(int i) {
        if (i != this.pose) {
            this.pose = i;
            invalidate();
        }
    }

    public int getPose() {
        return this.pose;
    }

    public void setFatigue(int i) {
        if (i < 0) {
            i = 0;
        } else if (i > 3) {
            i = 3;
        }
        if (i != this.fatigue) {
            this.fatigue = i;
            invalidate();
        }
    }

    public void setScene(boolean z) {
        if (z != this.scene) {
            this.scene = z;
            invalidate();
        }
    }

    public int getDisplayPose() {
        if (this.reactionPose != -1 && SystemClock.uptimeMillis() < this.reactionUntilMs) {
            return this.reactionPose;
        }
        return this.pose;
    }

    /**
     * 戳一下小人。
     *
     * 反应跟着当前状态走，而不是纯随机 —— 加班到崩溃时戳他不会再突然欢呼。
     * 连续快戳会升级：3 次捂耳朵，8 次触发彩蛋语录。
     *
     * @return 需要显示的气泡文案；null 表示交给调用方按状态生成
     */
    public String poke() {
        long now = SystemClock.uptimeMillis();
        if (now - this.lastPokeMs > POKE_CHAIN_WINDOW_MS) {
            this.pokeCount = 0;
        }
        this.lastPokeMs = now;
        this.pokeCount++;

        int stage = WorkerPet.pokeStage(this.pokeCount);
        if (stage > 0) {
            react(WorkerPet.POKE_ANNOYED, 3000L);
            return WorkerPet.pokeStageText(stage);
        }
        react(WorkerPet.reactionFor(this.pose, this.fatigue), 2000L);
        return null;
    }

    /** 达成存钱目标时撒花庆祝（带一次较重的触觉反馈）。 */
    public void celebrate() {
        performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        react(15, 3000L);
    }

    private void react(int i, long j) {
        this.reactionPose = i;
        this.reactionUntilMs = SystemClock.uptimeMillis() + j;
        invalidate();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }
        float min = Math.min(width, height) / 100.0f;
        int save = canvas.save();
        float f = 100.0f * min;
        canvas.translate((width - f) / 2.0f, (height - f) / 2.0f);
        canvas.scale(min, min);
        if (this.reactionPose != -1 && SystemClock.uptimeMillis() >= this.reactionUntilMs) {
            this.reactionPose = -1;
        }
        int displayPose = getDisplayPose();
        long uptimeMillis = SystemClock.uptimeMillis();
        if (uptimeMillis - this.deskCheckedAtMs > 1000) {
            this.deskCheckedAtMs = uptimeMillis;
            this.deskVisible = this.scene && PetState.isAtDesk(this.appContext, Calendar.getInstance());
        }
        WorkerPet.draw(canvas, displayPose, this.night, this.t, this.fatigue, this.deskVisible);
        canvas.restoreToCount(save);
    }

    @Override // android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startLoop();
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopLoop();
    }

    @Override // android.view.View
    protected void onWindowVisibilityChanged(int i) {
        super.onWindowVisibilityChanged(i);
        if (i == 0) {
            resumeLoop();
        } else {
            pauseLoop();
        }
    }

    private void startLoop() {
        if (this.animator != null) {
            return;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.animator = ofFloat;
        ofFloat.setDuration(1000L);
        ofFloat.setRepeatCount(-1);
        ofFloat.setInterpolator(new LinearInterpolator());
        this.lastFrameMs = 0L;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.salaryjump.app.WorkerPetView.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                long uptimeMillis = SystemClock.uptimeMillis();
                if (WorkerPetView.this.lastFrameMs != 0) {
                    long j = uptimeMillis - WorkerPetView.this.lastFrameMs;
                    if (j > 120) {
                        j = 120;
                    }
                    WorkerPetView.access$116(WorkerPetView.this, j / 1000.0f);
                }
                WorkerPetView.this.lastFrameMs = uptimeMillis;
                WorkerPetView.this.invalidate();
            }
        });
        ofFloat.start();
    }

    private void stopLoop() {
        ValueAnimator valueAnimator = this.animator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.animator = null;
        }
    }

    private void pauseLoop() {
        ValueAnimator valueAnimator = this.animator;
        if (valueAnimator != null && valueAnimator.isStarted()) {
            valueAnimator.pause();
        }
        this.lastFrameMs = 0L;
    }

    private void resumeLoop() {
        this.lastFrameMs = 0L;
        ValueAnimator valueAnimator = this.animator;
        if (valueAnimator != null && valueAnimator.isPaused()) {
            valueAnimator.resume();
        }
    }

    static Bitmap renderBitmap(Context context, int i, int i2) {
        return renderBitmap(context, i, i2, 0.0f);
    }

    static Bitmap renderBitmap(Context context, int i, int i2, float f) {
        return renderBitmap(context, i, i2, f, 0, false);
    }

    static Bitmap renderBitmap(Context context, int i, int i2, float f, int i3, boolean z) {
        boolean z2 = true;
        try {
            if ((context.getResources().getConfiguration().uiMode & 48) != 32) {
                z2 = false;
            }
        } catch (Throwable th) {
        }
        Bitmap createBitmap = Bitmap.createBitmap(i2, i2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        float f2 = i2 / 100.0f;
        canvas.scale(f2, f2);
        WorkerPet.drawFrozen(canvas, i, z2, f, i3, z);
        return createBitmap;
    }

    static int poseForState(Context context, int i) {
        return WorkerPet.resolvePose(i, Calendar.getInstance(), workEndMinutes(context));
    }
}
