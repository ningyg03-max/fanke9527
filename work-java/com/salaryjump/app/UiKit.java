package com.salaryjump.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ScrollView;

/* loaded from: classes.dex */
public final class UiKit {
    private UiKit() {
    }

    public static Drawable vertical(int[] iArr) {
        return new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, iArr);
    }

    public static Drawable diagonal(int[] iArr, float f) {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, iArr);
        gradientDrawable.setCornerRadius(f);
        return gradientDrawable;
    }

    public static Drawable solid(int i, float f) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(f);
        return gradientDrawable;
    }

    public static Drawable solid(int i, float f, int i2, float f2) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(f);
        if (f2 > 0.0f) {
            gradientDrawable.setStroke(Math.round(f2), i2);
        }
        return gradientDrawable;
    }

    public static Drawable circle(int i) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(1);
        gradientDrawable.setColor(i);
        return gradientDrawable;
    }

    public static Drawable hero(int[] iArr, float f) {
        return new HeroDrawable(iArr, f);
    }

    public static Drawable glowCard(int i, int i2, int i3, int i4, float f) {
        return new GlowCardDrawable(i, i2, i3, i4, f);
    }

    public static Drawable gear(int i) {
        return new GearDrawable(i);
    }

    public static View aurora(Activity activity, View view) {
        FrameLayout frameLayout = new FrameLayout(activity);
        frameLayout.addView(new AuroraBackdrop(activity), new FrameLayout.LayoutParams(-1, -1));
        final int paddingLeft = view.getPaddingLeft();
        final int paddingTop = view.getPaddingTop();
        final int paddingRight = view.getPaddingRight();
        final int paddingBottom = view.getPaddingBottom();
        view.setPadding(paddingLeft, systemBarHeight(activity, "status_bar_height", 28.0f) + paddingTop, paddingRight, systemBarHeight(activity, "navigation_bar_height", 0.0f) + paddingBottom);
        if (view instanceof ScrollView) {
            ((ScrollView) view).setClipToPadding(false);
        }
        view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: com.salaryjump.app.UiKit.1
            @Override // android.view.View.OnApplyWindowInsetsListener
            public WindowInsets onApplyWindowInsets(View view2, WindowInsets windowInsets) {
                view2.setPadding(paddingLeft, paddingTop + windowInsets.getSystemWindowInsetTop(), paddingRight, paddingBottom + windowInsets.getSystemWindowInsetBottom());
                return windowInsets;
            }
        });
        frameLayout.addView(view, new FrameLayout.LayoutParams(-1, -1));
        return frameLayout;
    }

    private static int systemBarHeight(Context context, String str, float f) {
        try {
            int identifier = context.getResources().getIdentifier(str, "dimen", "android");
            if (identifier > 0) {
                return context.getResources().getDimensionPixelSize(identifier);
            }
        } catch (Throwable th) {
        }
        return Math.round(f * context.getResources().getDisplayMetrics().density);
    }

    public static Drawable glassCard(float f) {
        return new GlassCardDrawable(f);
    }

    private static class HeroDrawable extends Drawable {
        private final int[] colors;
        private LinearGradient gradient;
        private final float radius;
        private final Paint paint = new Paint(1);
        private final Paint glow = new Paint(1);
        private final Rect rect = new Rect();

        HeroDrawable(int[] iArr, float f) {
            this.colors = iArr;
            this.radius = f;
        }

        @Override // android.graphics.drawable.Drawable
        protected void onBoundsChange(Rect rect) {
            this.rect.set(rect);
            if (rect.width() > 0 && rect.height() > 0) {
                this.gradient = new LinearGradient(rect.left, rect.top, rect.right, rect.bottom, this.colors, (float[]) null, Shader.TileMode.CLAMP);
                this.paint.setShader(this.gradient);
            }
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            if (this.gradient == null) {
                return;
            }
            canvas.drawRoundRect(this.rect.left, this.rect.top, this.rect.right, this.rect.bottom, this.radius, this.radius, this.paint);
            float width = this.rect.width();
            float min = Math.min(width, this.rect.height()) * 0.32f;
            if (min > 0.0f) {
                this.glow.setColor(536870911);
                canvas.drawCircle((this.rect.left + width) - min, this.rect.top + min, min, this.glow);
                this.glow.setColor(352321535);
                canvas.drawCircle(this.rect.left + min, this.rect.bottom - min, min, this.glow);
            }
        }

        @Override // android.graphics.drawable.Drawable
        public void getOutline(Outline outline) {
            outline.setRoundRect(getBounds(), this.radius);
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
            this.paint.setAlpha(i);
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
            this.paint.setColorFilter(colorFilter);
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }
    }

    private static class GlowCardDrawable extends Drawable {
        private final float radius;
        private final Paint paint = new Paint(1);
        private final Paint stroke = new Paint(1);
        private final Path path = new Path();
        private final RectF rect = new RectF();

        GlowCardDrawable(int i, int i2, int i3, int i4, float f) {
            this.radius = f;
            this.stroke.setStyle(Paint.Style.STROKE);
            this.stroke.setStrokeWidth(1.5f);
        }

        @Override // android.graphics.drawable.Drawable
        protected void onBoundsChange(Rect rect) {
            this.rect.set(rect);
            this.path.reset();
            this.path.addRoundRect(this.rect, this.radius, this.radius, Path.Direction.CW);
            boolean z = MainActivity.NIGHT;
            this.paint.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom, z ? 419430399 : -603979777, z ? 150994943 : -1627389953, Shader.TileMode.CLAMP));
            this.stroke.setColor(z ? 452984831 : 436878129);
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            canvas.drawPath(this.path, this.paint);
            canvas.drawPath(this.path, this.stroke);
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

    private static class GlassCardDrawable extends Drawable {
        private final float radius;
        private final Paint paint = new Paint(1);
        private final Paint stroke = new Paint(1);
        private final Paint topLine = new Paint(1);
        private final Path path = new Path();
        private final RectF rect = new RectF();

        GlassCardDrawable(float f) {
            this.radius = f;
            this.stroke.setStyle(Paint.Style.STROKE);
            this.stroke.setStrokeWidth(1.5f);
            this.topLine.setStyle(Paint.Style.STROKE);
            this.topLine.setStrokeWidth(1.5f);
        }

        @Override // android.graphics.drawable.Drawable
        protected void onBoundsChange(Rect rect) {
            this.rect.set(rect);
            this.path.reset();
            this.path.addRoundRect(this.rect, this.radius, this.radius, Path.Direction.CW);
            boolean z = MainActivity.NIGHT;
            this.paint.setShader(new LinearGradient(rect.left, rect.top, rect.right, rect.bottom, z ? 419430399 : -603979777, z ? 150994943 : -1627389953, Shader.TileMode.CLAMP));
            this.stroke.setColor(z ? 452984831 : 436878129);
            this.topLine.setColor(z ? 536870911 : 1728053247);
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            canvas.drawPath(this.path, this.paint);
            canvas.drawPath(this.path, this.stroke);
            int save = canvas.save();
            canvas.clipPath(this.path);
            float strokeWidth = this.topLine.getStrokeWidth() / 2.0f;
            canvas.drawLine(this.rect.left + this.radius, this.rect.top + strokeWidth, this.rect.right - this.radius, this.rect.top + strokeWidth, this.topLine);
            canvas.restoreToCount(save);
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

    private static int withAlpha(int i, int i2) {
        return (i & 16777215) | (Math.max(0, Math.min(255, i2)) << 24);
    }

    private static class GearDrawable extends Drawable {
        private final Paint paint = new Paint(1);

        GearDrawable(int i) {
            this.paint.setColor(i);
            this.paint.setStyle(Paint.Style.STROKE);
            this.paint.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override // android.graphics.drawable.Drawable
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            float min = Math.min(bounds.width(), bounds.height());
            float exactCenterX = bounds.exactCenterX();
            float exactCenterY = bounds.exactCenterY();
            float f = 0.26f * min;
            this.paint.setStrokeWidth(min * 0.085f);
            canvas.drawCircle(exactCenterX, exactCenterY, f, this.paint);
            float f2 = 0.9f * f;
            float f3 = f * 1.35f;
            for (int i = 0; i < 8; i++) {
                double radians = Math.toRadians(i * 45.0d);
                float cos = (float) Math.cos(radians);
                float sin = (float) Math.sin(radians);
                canvas.drawLine(exactCenterX + (f2 * cos), exactCenterY + (f2 * sin), exactCenterX + (cos * f3), exactCenterY + (sin * f3), this.paint);
            }
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicWidth() {
            return 96;
        }

        @Override // android.graphics.drawable.Drawable
        public int getIntrinsicHeight() {
            return 96;
        }

        @Override // android.graphics.drawable.Drawable
        public void setAlpha(int i) {
            this.paint.setAlpha(i);
        }

        @Override // android.graphics.drawable.Drawable
        public void setColorFilter(ColorFilter colorFilter) {
            this.paint.setColorFilter(colorFilter);
        }

        @Override // android.graphics.drawable.Drawable
        public int getOpacity() {
            return -3;
        }
    }
}
