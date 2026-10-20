package com.salaryjump.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;
import android.view.animation.LinearInterpolator;

/* loaded from: classes.dex */
public class SheenBar extends View {
    private static final int UI_MODE_NIGHT_MASK = 48;
    private static final int UI_MODE_NIGHT_YES = 32;
    private ValueAnimator animator;
    private final Paint paint;
    private final int[] progColors;
    private float progress;
    private final RectF rect;
    private float sheen;
    private final int sheenColor;
    private final int trackColor;

    public SheenBar(Context context) {
        super(context);
        this.paint = new Paint(1);
        this.rect = new RectF();
        if ((context.getResources().getConfiguration().uiMode & UI_MODE_NIGHT_MASK) == 32) {
            this.trackColor = 452984831;
            this.progColors = new int[]{-15681151, -13315175, -13773633};
            this.sheenColor = -1711276033;
        } else {
            this.trackColor = 386546481;
            this.progColors = new int[]{-15815028, -15419226, -13773633};
            this.sheenColor = -1627389953;
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:11:0x000b, code lost:
    
        if (r3 > 1.0f) goto L4;
     */
    public void setProgress(float r3) {
        if (r3 < 0.0f) {
            r3 = 0.0f;
        } else if (r3 > 1.0f) {
            r3 = 1.0f;
        }
        if (Math.abs(r3 - this.progress) > 5.0E-4f) {
            this.progress = r3;
            invalidate();
        }
    }

    public void animateProgress(float f) {
        setProgress(f);
    }

    @Override // android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        start();
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    @Override // android.view.View
    protected void onWindowVisibilityChanged(int i) {
        super.onWindowVisibilityChanged(i);
        ValueAnimator valueAnimator = this.animator;
        if (valueAnimator == null) {
            return;
        }
        if (i == 0) {
            if (valueAnimator.isPaused()) {
                valueAnimator.resume();
            }
        } else if (valueAnimator.isStarted()) {
            valueAnimator.pause();
        }
    }

    private void start() {
        if (this.animator != null) {
            return;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.animator = ofFloat;
        ofFloat.setDuration(2900L);
        ofFloat.setRepeatCount(-1);
        ofFloat.setInterpolator(new LinearInterpolator());
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.salaryjump.app.SheenBar.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                SheenBar.this.sheen = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                SheenBar.this.invalidate();
            }
        });
        ofFloat.start();
    }

    private void stop() {
        ValueAnimator valueAnimator = this.animator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.animator = null;
        }
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        if (width <= 0.0f || height <= 0.0f) {
            return;
        }
        float f = height / 2.0f;
        this.rect.set(0.0f, 0.0f, width, height);
        this.paint.setShader(null);
        this.paint.setColor(this.trackColor);
        canvas.drawRoundRect(this.rect, f, f, this.paint);
        float f2 = width * this.progress;
        if (f2 > 0.0f) {
            float min = Math.min(f, f2 / 2.0f);
            this.paint.setShader(new LinearGradient(0.0f, 0.0f, f2, 0.0f, this.progColors, (float[]) null, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(new RectF(0.0f, 0.0f, f2, height), min, min, this.paint);
            if (f2 > height) {
                int save = canvas.save();
                canvas.clipRect(0.0f, 0.0f, f2, height);
                float f3 = ((this.sheen * 1.45f) - 0.35f) * width;
                float f4 = 0.16f * width;
                this.paint.setShader(new LinearGradient(f3 - f4, 0.0f, f3 + f4, 0.0f, new int[]{this.sheenColor & 16777215, this.sheenColor, 16777215 & this.sheenColor}, (float[]) null, Shader.TileMode.CLAMP));
                canvas.drawRect(0.0f, 0.0f, width, height, this.paint);
                canvas.restoreToCount(save);
            }
        }
    }
}
