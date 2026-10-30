package com.salaryjump.app;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

/**
 * 一条水平虚线（用作图表里的目标线）。
 *
 * <p>用 ShapeDrawable 的 stroke 画的是「矩形边框」，不是一条线 ——
 * 所以这里直接画。
 */
final class DashedLine extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    DashedLine(int color, float widthPx, float dashPx, float gapPx) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(widthPx);
        paint.setPathEffect(new DashPathEffect(new float[]{dashPx, gapPx}, 0.0f));
    }

    @Override
    public void draw(Canvas canvas) {
        float y = getBounds().height() / 2.0f;
        canvas.drawLine(getBounds().left, y, getBounds().right, y, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(android.graphics.ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }
}
