package com.salaryjump.app;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;
import java.text.DecimalFormat;

/* loaded from: classes.dex */
public final class MotionKit {
    private MotionKit() {
    }

    public static Drawable breathingDot(float f, int i) {
        return new BreathingDot(f, i);
    }

    private static class BreathingDot extends Drawable implements Animatable {
        private ValueAnimator animator;
        private final int color;
        private float p;
        private final Paint paint = new Paint(1);
        private final float size;

        BreathingDot(float f, int i) {
            this.size = f;
            this.color = i;
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            float exactCenterX = bounds.exactCenterX();
            float exactCenterY = bounds.exactCenterY();
            float f = (this.p * 0.16f) + 0.9f;
            int i = (int) ((this.p * 115.0f) + 140.0f);
            float f2 = (this.size * f) / 2.0f;
            this.paint.setColor(this.color);
            float f3 = i;
            this.paint.setAlpha((int) (0.3f * f3));
            canvas.drawCircle(exactCenterX, exactCenterY, 2.1f * f2, this.paint);
            this.paint.setAlpha((int) (f3 * 0.55f));
            canvas.drawCircle(exactCenterX, exactCenterY, 1.5f * f2, this.paint);
            this.paint.setAlpha(i);
            canvas.drawCircle(exactCenterX, exactCenterY, f2, this.paint);
        }

        @Override // android.graphics.drawable.Animatable
        public void start() {
            if (this.animator != null) {
                return;
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            this.animator = ofFloat;
            ofFloat.setDuration(2400L);
            ofFloat.setRepeatCount(-1);
            ofFloat.setRepeatMode(2);
            ofFloat.setInterpolator(new AccelerateDecelerateInterpolator());
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.salaryjump.app.MotionKit.BreathingDot.1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    BreathingDot.this.p = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    BreathingDot.this.invalidateSelf();
                }
            });
            ofFloat.start();
        }

        @Override // android.graphics.drawable.Animatable
        public void stop() {
            ValueAnimator valueAnimator = this.animator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
                this.animator = null;
            }
        }

        @Override // android.graphics.drawable.Animatable
        public boolean isRunning() {
            ValueAnimator valueAnimator = this.animator;
            return valueAnimator != null && valueAnimator.isRunning();
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }

        @Override // android.graphics.drawable.Drawable
        protected boolean onLevelChange(int i) {
            return false;
        }
    }

    public static Drawable glowPill(float f, int i, int i2, int i3, boolean z) {
        return new GlowPill(f, i, i2, i3, z);
    }

    private static class GlowPill extends Drawable implements Animatable {
        private ValueAnimator animator;
        private final int fill;
        private final int glow;
        private float p;
        private final boolean pulse;
        private final float radius;
        private final int stroke;
        private final Paint paint = new Paint(1);
        private final Paint strokePaint = new Paint(1);
        private final RectF rect = new RectF();

        GlowPill(float f, int i, int i2, int i3, boolean z) {
            this.radius = f;
            this.fill = i;
            this.stroke = i2;
            this.glow = i3;
            this.pulse = z;
            this.strokePaint.setStyle(Paint.Style.STROKE);
            this.strokePaint.setStrokeWidth(1.5f);
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            Canvas canvas2;
            this.rect.set(getBounds());
            if (!this.pulse || this.animator == null) {
                canvas2 = canvas;
            } else {
                float f = this.p;
                float f2 = 7.0f * f;
                int i = (int) ((1.0f - f) * 110.0f);
                if (i <= 0) {
                    canvas2 = canvas;
                } else {
                    this.paint.setColor(this.glow);
                    this.paint.setAlpha(i);
                    canvas2 = canvas;
                    canvas2.drawRoundRect(this.rect.left - f2, this.rect.top - f2, this.rect.right + f2, this.rect.bottom + f2, this.radius + f2, this.radius + f2, this.paint);
                }
            }
            this.paint.setColor(this.fill);
            this.paint.setAlpha(255);
            canvas2.drawRoundRect(this.rect, this.radius, this.radius, this.paint);
            if ((this.stroke >>> 24) != 0) {
                this.strokePaint.setColor(this.stroke);
                canvas2.drawRoundRect(this.rect, this.radius, this.radius, this.strokePaint);
            }
        }

        @Override // android.graphics.drawable.Animatable
        public void start() {
            if (!this.pulse || this.animator != null) {
                return;
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
            this.animator = ofFloat;
            ofFloat.setDuration(2600L);
            ofFloat.setRepeatCount(-1);
            ofFloat.setInterpolator(new AccelerateDecelerateInterpolator());
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.salaryjump.app.MotionKit.GlowPill.1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    GlowPill.this.p = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    GlowPill.this.invalidateSelf();
                }
            });
            ofFloat.start();
        }

        @Override // android.graphics.drawable.Animatable
        public void stop() {
            ValueAnimator valueAnimator = this.animator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
                this.animator = null;
            }
        }

        @Override // android.graphics.drawable.Animatable
        public boolean isRunning() {
            ValueAnimator valueAnimator = this.animator;
            return valueAnimator != null && valueAnimator.isRunning();
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }
    }

    public static void countUp(final TextView textView, final String str, final double d, final DecimalFormat decimalFormat) {
        if (textView == null) {
            return;
        }
        Object tag = textView.getTag();
        final double doubleValue = tag instanceof Double ? ((Double) tag).doubleValue() : 0.0d;
        textView.setTag(Double.valueOf(d));
        if (Math.abs(doubleValue - d) < 1.0E-4d) {
            textView.setText(str + decimalFormat.format(d));
            return;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(300L);
        ofFloat.setInterpolator(new DecelerateInterpolator());
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.salaryjump.app.MotionKit.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                textView.setText(str + decimalFormat.format(doubleValue + ((d - doubleValue) * ((Float) valueAnimator.getAnimatedValue()).floatValue())));
            }
        });
        ofFloat.start();
    }

    public static void countUpFromZero(TextView textView, String str, double d, DecimalFormat decimalFormat) {
        if (textView != null) {
            textView.setTag(Double.valueOf(0.0d));
        }
        countUp(textView, str, d, decimalFormat);
    }

    public static void countUpMoney(final TextView textView, final double d, final DecimalFormat decimalFormat) {
        if (textView == null) {
            return;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setDuration(420L);
        ofFloat.setInterpolator(new DecelerateInterpolator());
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.salaryjump.app.MotionKit.2
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                SpannableString spannableString = new SpannableString("¥ " + decimalFormat.format(d * ((Float) valueAnimator.getAnimatedValue()).floatValue()));
                spannableString.setSpan(new RelativeSizeSpan(0.5f), 0, 1, 33);
                textView.setText(spannableString);
            }
        });
        ofFloat.start();
    }

    public static void enter(View view, float f, int i) {
        if (view == null) {
            return;
        }
        view.setAlpha(0.0f);
        view.setTranslationY(f * 12.0f);
        view.animate().alpha(1.0f).translationY(0.0f).setStartDelay(i * 40).setDuration(400L).setInterpolator(new DecelerateInterpolator()).start();
    }

    public static void enterChildren(final ViewGroup viewGroup, final float f) {
        if (viewGroup == null) {
            return;
        }
        viewGroup.post(new Runnable() { // from class: com.salaryjump.app.MotionKit.3
            @Override // java.lang.Runnable
            public void run() {
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount && i < 8; i++) {
                    MotionKit.enter(viewGroup.getChildAt(i), f, i);
                }
            }
        });
    }

    public static void press(View view) {
        if (view == null) {
            return;
        }
        view.setOnTouchListener(new View.OnTouchListener() { // from class: com.salaryjump.app.MotionKit.4
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                int actionMasked = motionEvent.getActionMasked();
                if (actionMasked == 0) {
                    view2.animate().scaleX(0.96f).scaleY(0.96f).setDuration(90L).setInterpolator(new DecelerateInterpolator()).start();
                    return false;
                }
                if (actionMasked == 1 || actionMasked == 3) {
                    view2.animate().scaleX(1.0f).scaleY(1.0f).setDuration(420L).setInterpolator(new OvershootInterpolator(2.2f)).start();
                    return false;
                }
                return false;
            }
        });
    }
}
