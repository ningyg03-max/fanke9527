package com.salaryjump.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/* loaded from: classes.dex */
public class AuroraBackdrop extends View {
    private static final int UI_MODE_NIGHT_MASK = 48;
    private static final int UI_MODE_NIGHT_YES = 32;
    private ValueAnimator animator;
    private Bitmap aurora;
    private int builtH;
    private int builtW;
    private final Matrix matrix;
    private final boolean night;
    private final Paint paint;
    private float phase;
    private static final int[] BASE_NIGHT = {-16314097, -16247528, -16314343};
    private static final int[] BASE_LIGHT = {-1378318, -1510921, -1183751};
    private static final int[][] ORBS_NIGHT = {new int[]{22, 18, 46, -1945060991}, new int[]{82, 26, 40, 1713558510}, new int[]{62, 88, 40, 1300978934}};
    private static final int[][] ORBS_LIGHT = {new int[]{20, 14, 46, 1460713857}, new int[]{84, 22, 42, 1109578734}, new int[]{58, 92, 40, 730553590}};

    public AuroraBackdrop(Context context) {
        super(context);
        this.paint = new Paint(2);
        this.matrix = new Matrix();
        this.night = (context.getResources().getConfiguration().uiMode & UI_MODE_NIGHT_MASK) == 32;
        this.paint.setFilterBitmap(true);
    }

    @Override // android.view.View
    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (i <= 0 || i2 <= 0) {
            return;
        }
        if (this.aurora == null || i != this.builtW || i2 != this.builtH) {
            rebuild(i, i2);
        }
    }

    private void rebuild(int i, int i2) {
        this.builtW = i;
        this.builtH = i2;
        int max = Math.max(4, i / 2);
        int max2 = Math.max(4, i2 / 2);
        this.aurora = Bitmap.createBitmap(max, max2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(this.aurora);
        Paint paint = new Paint(1);
        float f = max;
        float f2 = max2;
        paint.setShader(new LinearGradient(0.0f, 0.0f, f, f2, this.night ? BASE_NIGHT : BASE_LIGHT, (float[]) null, Shader.TileMode.CLAMP));
        canvas.drawRect(0.0f, 0.0f, f, f2, paint);
        int[][] iArr = this.night ? ORBS_NIGHT : ORBS_LIGHT;
        float sqrt = (float) Math.sqrt((max * max) + (max2 * max2));
        for (int[] iArr2 : iArr) {
            float f3 = f * (iArr2[0] / 100.0f);
            float f4 = f2 * (iArr2[1] / 100.0f);
            float f5 = sqrt * (iArr2[2] / 100.0f);
            paint.setShader(new RadialGradient(f3, f4, f5, iArr2[3], iArr2[3] & 16777215, Shader.TileMode.CLAMP));
            canvas.drawCircle(f3, f4, f5, paint);
        }
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = this.aurora;
        if (bitmap == null) {
            return;
        }
        float width = getWidth();
        float height = getHeight();
        float f = this.phase;
        float f2 = ((0.05f * f) - 0.02f) * width;
        float f3 = ((0.03f * f) - 0.01f) * height;
        float f4 = 9.0f * f;
        float f5 = ((f * 0.09f) + 1.0f) * 1.3f;
        this.matrix.reset();
        this.matrix.postTranslate((-bitmap.getWidth()) / 2.0f, (-bitmap.getHeight()) / 2.0f);
        this.matrix.postRotate(f4);
        this.matrix.postScale((f5 * width) / bitmap.getWidth(), (f5 * height) / bitmap.getHeight());
        this.matrix.postTranslate((width / 2.0f) + f2, (height / 2.0f) + f3);
        canvas.drawBitmap(bitmap, this.matrix, this.paint);
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
        if (i == 0) {
            resume();
        } else {
            pause();
        }
    }

    private void start() {
        if (this.animator != null) {
            return;
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.animator = ofFloat;
        ofFloat.setDuration(30000L);
        ofFloat.setRepeatCount(-1);
        ofFloat.setRepeatMode(2);
        ofFloat.setInterpolator(new AccelerateDecelerateInterpolator());
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.salaryjump.app.AuroraBackdrop.1
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                AuroraBackdrop.this.phase = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                AuroraBackdrop.this.invalidate();
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

    private void pause() {
        ValueAnimator valueAnimator = this.animator;
        if (valueAnimator != null && valueAnimator.isStarted()) {
            valueAnimator.pause();
        }
    }

    private void resume() {
        ValueAnimator valueAnimator = this.animator;
        if (valueAnimator != null && valueAnimator.isPaused()) {
            valueAnimator.resume();
        }
    }
}
