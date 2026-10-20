package com.salaryjump.app;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import java.util.Calendar;

/* loaded from: classes.dex */
final class WorkerPet {
    static final int CELEBRATE = 15;
    /** 被连续戳烦了：捂耳朵摇头（彩蛋用） */
    static final int POKE_ANNOYED = 16;
    static final int COMMUTE = 0;
    static final int FRIDAY = 10;
    static final int HOLIDAY_WORK = 7;
    static final int LEAVE = 8;
    static final int LUNCH = 5;
    static final int MONDAY = 9;
    static final int OFF_WORK = 4;
    static final int OVERTIME = 3;
    static final int REACT_CHEER = 13;
    static final int REACT_SHY = 14;
    static final int REACT_SURPRISED = 12;
    static final int SUNDAY = 11;
    static final int WEEKEND = 6;
    static final int WORK_AM = 1;
    static final int WORK_PM = 2;
    private static boolean frozenFrame = false;

    private WorkerPet() {
    }

    static int resolvePose(int i, Calendar calendar) {
        return resolvePose(i, calendar, 0);
    }

    static int resolvePose(int i, Calendar calendar, int i2) {
        if (i == 8) {
            return 8;
        }
        if (i == HOLIDAY_WORK) {
            if (i2 <= 0 || (calendar.get(SUNDAY) * 60) + calendar.get(12) < i2) {
                return HOLIDAY_WORK;
            }
            return 4;
        }
        if (i == WEEKEND) {
            return OVERTIME;
        }
        int i3 = calendar.get(HOLIDAY_WORK);
        int i4 = calendar.get(SUNDAY);
        if (i == 0) {
            if (i2 <= 0) {
                i2 = 1020;
            }
            return (i3 != 1 || (i4 * 60) + calendar.get(12) < i2) ? WEEKEND : SUNDAY;
        }
        if (i3 == 2 && (i == 2 || i == 4)) {
            return MONDAY;
        }
        if (i3 == WEEKEND && i4 >= REACT_CHEER && (i == 4 || i == 5)) {
            return 10;
        }
        return poseFor(i, i3);
    }

    static int poseFor(int i, int i2) {
        switch (i) {
            case 1:
                return 0;
            case 2:
                return 1;
            case OVERTIME /* 3 */:
                return 5;
            case 4:
                return 2;
            case 5:
                return 4;
            case WEEKEND /* 6 */:
                return OVERTIME;
            case HOLIDAY_WORK /* 7 */:
                return HOLIDAY_WORK;
            case 8:
                return 8;
            default:
                return WEEKEND;
        }
    }

    /**
     * 现在是不是「该睡觉」的时段：0-8 点、22 点后。
     *
     * <p>周末白天（比如下午三点）应该显示「躺平放松」而不是「冒 ZZZ」。
     */
    static boolean isSleepHour() {
        return isSleepHour(Calendar.getInstance().get(11));
    }

    /** 可测版本：按时钟小时判断。 */
    static boolean isSleepHour(int hour) {
        return hour < 8 || hour >= 22;
    }

    static String moodText(int i) {
        switch (i) {
            case 0:
                return "拖着步子出门";
            case 1:
                return "键盘敲起来";
            case 2:
                return "咖啡续命中";
            case OVERTIME /* 3 */:
                return "加班的第 N 小时";
            case 4:
                return "下班快乐！";
            case 5:
                return "干饭不积极…";
            case WEEKEND /* 6 */:
                return "谁也别叫我";
            case HOLIDAY_WORK /* 7 */:
                return "别人放假我搬砖";
            case 8:
                return "今日休假";
            case MONDAY /* 9 */:
                return "生无可恋周一";
            case 10:
                return "马上就要周末！";
            case SUNDAY /* 11 */:
                return "想到明天就…";
            case 12:
                return "摸鱼被抓！";
            case REACT_CHEER /* 13 */:
                return "加油！";
            case REACT_SHY /* 14 */:
                return "嘿嘿";
            case CELEBRATE /* 15 */:
                return "🎉 达成！";
            case POKE_ANNOYED /* 16 */:
                return "别戳了别戳了";
            default:
                return "";
        }
    }

    /** 连戳到这次数 → 捂耳朵 */
    static final int POKE_ANNOYED_AT = 3;
    /** 连戳到这次数 → 彩蛋语录 */
    static final int POKE_EASTER_EGG_AT = 8;

    /**
     * 按当前姿势 + 疲劳挑一个反应（纯函数，便于回归测试）。
     *
     * 加班/工作被打断 → 惊讶；休息/干饭 → 害羞；下班/周五/出勤 → 欢呼；
     * 通勤/周一等没有明确情绪 → 随机。疲劳 3 级时一律偏「惊讶」（蔫）。
     */
    static int reactionFor(int i, int i2) {
        if (i2 >= 3) {
            return REACT_SURPRISED;
        }
        switch (i) {
            case OVERTIME:
            case WORK_AM:
            case WORK_PM:
                return REACT_SURPRISED;
            case LUNCH:
            case WEEKEND:
            case LEAVE:
            case SUNDAY:
            case POKE_ANNOYED:
                return REACT_SHY;
            case OFF_WORK:
            case FRIDAY:
            case HOLIDAY_WORK:
                return REACT_CHEER;
            default:
                return randomReaction();
        }
    }

    /** 通勤/周一这类没有明确情绪的场景，三种反应随机。 */
    static int randomReaction() {
        return new int[]{REACT_SURPRISED, REACT_CHEER, REACT_SHY}[(int) (Math.random() * 3.0d)];
    }

    /** 连戳档位：0 = 普通，1 = 烦（捂耳朵），2 = 彩蛋 */
    static int pokeStage(int i) {
        if (i >= POKE_EASTER_EGG_AT) {
            return 2;
        }
        return i >= POKE_ANNOYED_AT ? 1 : 0;
    }

    /** 连戳档位对应的文案；0 档返回 null（交给状态语录）。 */
    static String pokeStageText(int i) {
        if (i == 2) {
            return "你有这功夫，不如去干活";
        }
        return i == 1 ? "别戳了别戳了" : null;
    }

    static void draw(Canvas canvas, int i, boolean z) {
        draw(canvas, i, z, 0.0f, false, 0, false);
    }

    static void draw(Canvas canvas, int i, boolean z, float f) {
        draw(canvas, i, z, f, true, 0, false);
    }

    static void draw(Canvas canvas, int i, boolean z, float f, int i2, boolean z2) {
        draw(canvas, i, z, f, true, i2, z2);
    }

    static void drawFrozen(Canvas canvas, int i, boolean z, float f) {
        drawFrozen(canvas, i, z, f, 0, false);
    }

    static void drawFrozen(Canvas canvas, int i, boolean z, float f, int i2, boolean z2) {
        frozenFrame = true;
        try {
            draw(canvas, i, z, f, true, i2, z2);
        } finally {
            frozenFrame = false;
        }
    }

    static float phaseFrom(Calendar calendar) {
        return (((((calendar.get(SUNDAY) * 60) + calendar.get(12)) * 60.0f) + calendar.get(REACT_CHEER)) + (calendar.get(REACT_SHY) / 1000.0f)) % 3600.0f;
    }

    private static void draw(Canvas canvas, int i, boolean z, float f, boolean z2, int i2, boolean z3) {
        Palette palette = z ? Palette.NIGHT : Palette.LIGHT;
        int save = canvas.save();
        if (z3) {
            drawScene(canvas, palette, z);
        }
        if (z2) {
            boolean z4 = i == WEEKEND || i == 8;
            canvas.translate(0.0f, ((float) Math.sin((6.2831855f * f) / (z4 ? 3.0f : 2.6f))) * (z4 ? 0.55f : 0.95f));
        }
        switch (i) {
            case 0:
                drawCommute(canvas, palette, f, z2);
                break;
            case 1:
                drawWorkAm(canvas, palette, f, z2);
                break;
            case 2:
                drawWorkPm(canvas, palette, f, z2);
                break;
            case OVERTIME /* 3 */:
                drawOvertime(canvas, palette, f, z2);
                break;
            case 4:
                drawOffWork(canvas, palette, f, z2);
                break;
            case 5:
                drawLunch(canvas, palette, f, z2);
                break;
            case WEEKEND /* 6 */:
                drawWeekend(canvas, palette, f, z2, isSleepHour());
                break;
            case HOLIDAY_WORK /* 7 */:
                drawHolidayWork(canvas, palette, f, z2);
                break;
            case 8:
                drawLeave(canvas, palette, f, z2);
                break;
            case MONDAY /* 9 */:
                drawMonday(canvas, palette, f, z2);
                break;
            case 10:
                drawFriday(canvas, palette, f, z2);
                break;
            case SUNDAY /* 11 */:
                drawSunday(canvas, palette, f, z2);
                break;
            case 12:
                drawReactSurprised(canvas, palette, f, z2);
                break;
            case REACT_CHEER /* 13 */:
                drawReactCheer(canvas, palette, f, z2);
                break;
            case REACT_SHY /* 14 */:
                drawReactShy(canvas, palette, f, z2);
                break;
            case CELEBRATE /* 15 */:
                drawCelebrate(canvas, palette, f, z2);
                break;
            case POKE_ANNOYED /* 16 */:
                drawPokeAnnoyed(canvas, palette, f, z2);
                break;
            default:
                drawWorkAm(canvas, palette, f, z2);
                break;
        }
        if (i2 > 0) {
            drawFatigue(canvas, palette, i, i2, z);
        }
        canvas.restoreToCount(save);
    }

    private static void drawScene(Canvas canvas, Palette palette, boolean z) {
        int i = -14009537;
        canvas.drawRoundRect(new RectF(2.0f, 86.0f, 98.0f, 93.0f), 2.5f, 2.5f, fill(z ? -14009537 : -2562848));
        canvas.drawRoundRect(new RectF(2.0f, 50.0f, 22.0f, 74.0f), 2.0f, 2.0f, fill(z ? -14799824 : -4601403));
        canvas.drawRoundRect(new RectF(4.0f, 52.0f, 20.0f, 71.5f), 1.2f, 1.2f, fill(z ? -12605300 : -7415356));
        Paint fill = fill(z ? -14536395 : -5653577);
        canvas.drawRect(new RectF(10.5f, 74.0f, 13.5f, 80.0f), fill);
        canvas.drawRoundRect(new RectF(6.0f, 79.5f, 18.0f, 82.0f), 1.2f, 1.2f, fill);
        if (!z) {
            i = -4601403;
        }
        canvas.drawRect(new RectF(94.0f, 56.0f, 96.0f, 86.0f), fill(i));
        canvas.drawRoundRect(new RectF(86.0f, 52.0f, 98.0f, 57.0f), 2.0f, 2.0f, fill(z ? -13417142 : -3549229));
        canvas.drawOval(new RectF(76.0f, 56.0f, 100.0f, 76.0f), fill(z ? 872207181 : 620548941));
        canvas.drawRoundRect(new RectF(78.0f, 76.0f, 89.0f, 86.0f), 2.2f, 2.2f, fill(z ? -13746106 : -854026));
        canvas.drawRoundRect(new RectF(79.5f, 77.5f, 87.5f, 81.0f), 1.4f, 1.4f, fill(-9744582));
        Paint stroke = stroke(z ? 1728053247 : 1304020686, 1.6f);
        Path path = new Path();
        path.moveTo(82.0f, 74.0f);
        path.quadTo(83.5f, 70.0f, 82.0f, 66.0f);
        canvas.drawPath(path, stroke);
        canvas.drawRoundRect(new RectF(24.0f, 78.0f, 38.0f, 86.0f), 1.5f, 1.5f, fill(z ? -12957106 : -133405));
    }

    private static void drawFatigue(Canvas canvas, Palette palette, int i, int i2, boolean z) {
        if (i == WEEKEND || i == 8 || i == CELEBRATE || i == 12 || i == REACT_CHEER || i == REACT_SHY) {
            return;
        }
        if (i2 >= 2) {
            Paint stroke = stroke(1929787951, 2.4f);
            canvas.drawOval(new RectF(39.5f, 56.5f, 47.5f, 63.5f), stroke);
            canvas.drawOval(new RectF(52.5f, 56.5f, 60.5f, 63.5f), stroke);
        }
        if (i2 >= 1) {
            canvas.drawPath(bigSweatPath(73.0f, 38.0f), fill(-8599822));
        }
        if (i2 >= 2) {
            canvas.drawPath(bigSweatPath(25.0f, 42.0f), fill(-8599822));
            Paint stroke2 = stroke(palette.limb, 2.4f);
            canvas.drawLine(43.0f, 33.0f, 40.0f, 25.0f, stroke2);
            canvas.drawLine(50.0f, 32.0f, 50.0f, 23.5f, stroke2);
            canvas.drawLine(57.0f, 33.0f, 60.0f, 25.0f, stroke2);
        }
        if (i2 >= OVERTIME) {
            Paint stroke3 = stroke(z ? -2130706433 : 1500216442, 2.6f);
            Path path = new Path();
            path.moveTo(44.0f, 22.0f);
            path.quadTo(41.0f, 15.0f, 44.0f, 8.0f);
            canvas.drawPath(path, stroke3);
            Path path2 = new Path();
            path2.moveTo(56.0f, 21.0f);
            path2.quadTo(59.0f, 14.0f, 56.0f, 7.0f);
            canvas.drawPath(path2, stroke3);
            Paint stroke4 = stroke(1292253743, 1.6f);
            canvas.drawLine(37.0f, 60.0f, 39.0f, 62.0f, stroke4);
            canvas.drawLine(63.0f, 60.0f, 61.0f, 62.0f, stroke4);
        }
    }

    private static Path bigSweatPath(float f, float f2) {
        Path path = new Path();
        path.moveTo(f, f2);
        float f3 = 4.2f + f2;
        path.quadTo(f + 3.4f, f3, f, 6.6f + f2);
        path.quadTo(f - 3.4f, f3, f, f2);
        path.close();
        return path;
    }

    static final class Palette {
        static final Palette LIGHT = new Palette(-13709408, -15955089, 856734081, -16369105, -1945748945, -16369105, -1945748945, -1945395587, -1938002986, -5207704, -7378102, -13746106, -12033430, -1, -527384, -2299406, -7100492, -9456672, -208051, 436870681, -13746106, new int[]{-757066, -208051, -10443270, -13315175, -290244});
        static final Palette NIGHT = new Palette(-13311060, -15819652, 1292941697, -16369105, -1945748945, -13642844, -1938821176, -1287788364, -1281651976, -3892620, -6326446, -12296084, -10321778, -1, -527384, -2299406, -6047548, -8599822, -208051, 855638016, -12955560, new int[]{-757066, -208051, -10443270, -13315175, -290244});
        final int bgFaint;
        final int bodyBottom;
        final int bodyTop;
        final int bowl;
        final int caseColor;
        final int caseDark;
        final int cloud;
        final int[] conf;
        final int face;
        final int faceFaint;
        final int glow;
        final int laptop;
        final int laptopLight;
        final int limb;
        final int moon;
        final int phone;
        final int pillow;
        final int rain;
        final int rice;
        final int shadow;
        final int zzz;
        final int zzzLeave;

        Palette(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13, int i14, int i15, int i16, int i17, int i18, int i19, int i20, int i21, int[] iArr) {
            this.bodyTop = i;
            this.bodyBottom = i2;
            this.glow = i3;
            this.face = i4;
            this.faceFaint = i5;
            this.limb = i6;
            this.bgFaint = i7;
            this.zzz = i8;
            this.zzzLeave = i9;
            this.caseColor = i10;
            this.caseDark = i11;
            this.laptop = i12;
            this.laptopLight = i13;
            this.bowl = i14;
            this.rice = i15;
            this.pillow = i16;
            this.cloud = i17;
            this.rain = i18;
            this.moon = i19;
            this.shadow = i20;
            this.phone = i21;
            this.conf = iArr;
        }
    }

    private static void shadow(Canvas canvas, Palette palette, float f) {
        shadowScaled(canvas, palette, f, 1.0f);
    }

    private static void shadowScaled(Canvas canvas, Palette palette, float f, float f2) {
        Paint fill = fill(palette.shadow);
        float max = Math.max(0.4f, f2) * 16.0f;
        canvas.drawOval(new RectF(50.0f - max, f - 3.4f, max + 50.0f, f + 3.4f), fill);
    }

    private static void body(Canvas canvas, Palette palette, float f, float f2, float f3, float f4, float f5) {
        int save = canvas.save();
        if (f5 != 0.0f) {
            canvas.rotate(f5, f, f2);
        }
        Paint fill = fill(-16777216);
        float f6 = 0.62f * f3;
        float f7 = f - f6;
        float f8 = 0.83f * f4;
        fill.setShader(new LinearGradient(f7, f2 - f8, f6 + f, f8 + f2, palette.bodyTop, palette.bodyBottom, Shader.TileMode.CLAMP));
        canvas.drawOval(new RectF(f - f3, f2 - f4, f + f3, f2 + f4), fill);
        fill.setShader(null);
        fill.setColor(1308622847);
        canvas.drawOval(new RectF(f7, f2 - (0.78f * f4), f - (0.05f * f3), f2 - (0.36f * f4)), fill);
        canvas.restoreToCount(save);
    }

    private static Paint fill(int i) {
        Paint paint = new Paint(1);
        paint.setColor(i);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    private static Paint stroke(int i, float f) {
        Paint paint = new Paint(1);
        paint.setColor(i);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    private static boolean blinkAt(float f, float f2, boolean z) {
        return z && !frozenFrame && (f + f2) % 4.2f < 0.13f;
    }

    private static void eyesN(Canvas canvas, Palette palette, float f, float f2, float f3) {
        eyesN(canvas, palette, f, f2, f3, false);
    }

    private static void eyesN(Canvas canvas, Palette palette, float f, float f2, float f3, boolean z) {
        if (z) {
            Paint stroke = stroke(palette.face, 2.0f);
            float f4 = f - f3;
            canvas.drawLine(f4 - 2.6f, f2, f4 + 2.6f, f2, stroke);
            float f5 = f + f3;
            canvas.drawLine(f5 - 2.6f, f2, f5 + 2.6f, f2, stroke);
            return;
        }
        Paint fill = fill(palette.face);
        canvas.drawCircle(f - f3, f2, 2.7f, fill);
        canvas.drawCircle(f + f3, f2, 2.7f, fill);
    }

    private static void eyesHappy(Canvas canvas, Palette palette, float f, float f2, float f3) {
        Paint stroke = stroke(palette.face, 2.0f);
        Path path = new Path();
        float f4 = f - f3;
        float f5 = 1.5f + f2;
        path.moveTo(f4 - 3.0f, f5);
        float f6 = f2 - 3.5f;
        path.quadTo(f4, f6, f4 + 3.0f, f5);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        float f7 = f + f3;
        path2.moveTo(f7 - 3.0f, f5);
        path2.quadTo(f7, f6, 3.0f + f7, f5);
        canvas.drawPath(path2, stroke);
    }

    private static void eyesHalf(Canvas canvas, Palette palette, float f, float f2, float f3) {
        Paint stroke = stroke(palette.face, 2.0f);
        Path path = new Path();
        float f4 = f - f3;
        float f5 = 1.0f + f2;
        path.moveTo(f4 - 3.0f, f5);
        float f6 = f2 + 4.6f;
        path.quadTo(f4, f6, f4 + 3.0f, f5);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        float f7 = f + f3;
        path2.moveTo(f7 - 3.0f, f5);
        path2.quadTo(f7, f6, 3.0f + f7, f5);
        canvas.drawPath(path2, stroke);
    }

    private static void eyesSad(Canvas canvas, Palette palette, float f, float f2, float f3) {
        eyesSad(canvas, palette, f, f2, f3, false);
    }

    private static void eyesSad(Canvas canvas, Palette palette, float f, float f2, float f3, boolean z) {
        if (z) {
            Paint stroke = stroke(palette.face, 2.0f);
            float f4 = f - f3;
            float f5 = f2 + 1.0f;
            canvas.drawLine(f4 - 2.6f, f5, f4 + 2.6f, f5, stroke);
            float f6 = f + f3;
            canvas.drawLine(f6 - 2.6f, f5, f6 + 2.6f, f5, stroke);
            return;
        }
        Paint fill = fill(palette.face);
        float f7 = f - f3;
        float f8 = 1.0f + f2;
        canvas.drawCircle(f7, f8, 2.6f, fill);
        float f9 = f + f3;
        canvas.drawCircle(f9, f8, 2.6f, fill);
        Paint stroke2 = stroke(palette.face, 1.6f);
        float f10 = f2 - 3.4f;
        float f11 = f2 - 1.6f;
        canvas.drawLine(f7 - 3.4f, f10, f7 + 2.0f, f11, stroke2);
        canvas.drawLine(f9 + 3.4f, f10, f9 - 2.0f, f11, stroke2);
    }

    private static void eyesDead(Canvas canvas, Palette palette, float f, float f2, float f3) {
        Paint stroke = stroke(palette.face, 2.2f);
        float f4 = f - f3;
        canvas.drawLine(f4 - 3.0f, f2, f4 + 3.0f, f2, stroke);
        float f5 = f + f3;
        canvas.drawLine(f5 - 3.0f, f2, f5 + 3.0f, f2, stroke);
    }

    private static void mouth(Canvas canvas, Palette palette, float f, float f2, int i) {
        switch (i) {
            case 0:
                Paint stroke = stroke(palette.face, 2.0f);
                Path path = new Path();
                path.moveTo(f - 4.0f, f2);
                path.quadTo(f, f2 + 4.0f, 4.0f + f, f2);
                canvas.drawPath(path, stroke);
                break;
            case 1:
                Paint stroke2 = stroke(palette.face, 2.2f);
                Path path2 = new Path();
                float f3 = f2 - 1.0f;
                path2.moveTo(f - 6.0f, f3);
                path2.quadTo(f, f2 + 7.0f, 6.0f + f, f3);
                canvas.drawPath(path2, stroke2);
                break;
            case 2:
                canvas.drawCircle(f, f2 + 1.0f, 2.6f, fill(palette.face));
                break;
            case OVERTIME /* 3 */:
                float f4 = f2 + 1.0f;
                canvas.drawLine(f - 4.5f, f4, f + 4.5f, f4, stroke(palette.face, 2.0f));
                break;
            case 4:
                Paint stroke3 = stroke(palette.face, 1.8f);
                Path path3 = new Path();
                float f5 = 2.0f + f2;
                path3.moveTo(f - 5.0f, f5);
                path3.quadTo(f - 2.5f, f2 - 1.0f, f, f5);
                path3.quadTo(2.5f + f, f2 + 5.0f, f + 5.0f, f5);
                canvas.drawPath(path3, stroke3);
                break;
            default:
                Paint fill = fill(palette.face);
                Path path4 = new Path();
                float f6 = f2 - 1.0f;
                path4.moveTo(f - 4.5f, f6);
                path4.quadTo(f, f2 + 5.5f, 4.5f + f, f6);
                path4.close();
                canvas.drawPath(path4, fill);
                break;
        }
    }

    private static void legsStand(Canvas canvas, Palette palette, float f) {
        Paint stroke = stroke(palette.limb, 3.0f);
        float f2 = f + 7.0f;
        canvas.drawLine(43.0f, f, 41.5f, f2, stroke);
        canvas.drawLine(57.0f, f, 58.5f, f2, stroke);
    }

    private static void legsWalk(Canvas canvas, Palette palette, float f, float f2, boolean z) {
        Paint stroke = stroke(palette.limb, 3.0f);
        float sin = z ? ((float) Math.sin(f2 * 7.0f)) * 3.6f : 0.0f;
        canvas.drawLine(43.0f, f, sin + 37.0f, f + 6.0f, stroke);
        canvas.drawLine(57.0f, f, 63.0f - sin, f + 3.0f, stroke);
    }

    private static void caseBag(Canvas canvas, Palette palette, float f, float f2) {
        float f3 = 15.0f + f;
        canvas.drawRoundRect(new RectF(f, f2, f3, 12.0f + f2), 2.5f, 2.5f, fill(palette.caseColor));
        canvas.drawRoundRect(new RectF(f, f2, f3, f2 + 4.5f), 2.2f, 2.2f, fill(palette.caseDark));
        canvas.drawPath(handlePath(f + 4.5f, f2), stroke(palette.caseDark, 1.8f));
    }

    private static Path handlePath(float f, float f2) {
        Path path = new Path();
        path.moveTo(f, f2);
        float f3 = f2 - 2.5f;
        path.lineTo(f, f3);
        float f4 = f + 5.0f;
        path.quadTo(3.0f + f, f2 - 4.5f, f4, f3);
        path.lineTo(f4, f2);
        return path;
    }

    private static void laptop(Canvas canvas, Palette palette, float f, float f2) {
        float f3 = 9.0f + f2;
        canvas.drawRoundRect(new RectF(f - 16.0f, f3, 16.0f + f, 13.0f + f2), 2.0f, 2.0f, fill(palette.laptop));
        canvas.drawRoundRect(new RectF(f - 12.0f, f2, f + 12.0f, f3), 1.6f, 1.6f, fill(palette.laptopLight));
    }

    private static void sparkle(Canvas canvas, float f, float f2, float f3, int i) {
        Path path = new Path();
        path.moveTo(f, f2 - f3);
        float f4 = 0.3f * f3;
        float f5 = f + f4;
        float f6 = f2 - f4;
        path.lineTo(f5, f6);
        path.lineTo(f + f3, f2);
        float f7 = f2 + f4;
        path.lineTo(f5, f7);
        path.lineTo(f, f2 + f3);
        float f8 = f - f4;
        path.lineTo(f8, f7);
        path.lineTo(f - f3, f2);
        path.lineTo(f8, f6);
        path.close();
        canvas.drawPath(path, fill(i));
    }

    private static void cloud(Canvas canvas, Palette palette, float f, float f2, float f3) {
        Paint fill = fill(palette.cloud);
        float f4 = (1.0f * f3) + f2;
        float f5 = 6.4f * f3;
        canvas.drawCircle(f - (6.0f * f3), f4, f5, fill);
        canvas.drawCircle(f, f2 - (2.0f * f3), 7.6f * f3, fill);
        canvas.drawCircle((6.6f * f3) + f, (1.2f * f3) + f2, 5.8f * f3, fill);
        float f6 = 12.0f * f3;
        float f7 = f3 * 3.0f;
        canvas.drawRoundRect(new RectF(f - f6, f4, f + f6, f2 + f5), f7, f7, fill);
    }

    private static Path sweatPath(float f, float f2) {
        Path path = new Path();
        path.moveTo(f, f2);
        float f3 = 3.0f + f2;
        path.quadTo(f + 2.4f, f3, f, 4.8f + f2);
        path.quadTo(f - 2.4f, f3, f, f2);
        path.close();
        return path;
    }

    private static void sweat(Canvas canvas, float f, float f2) {
        canvas.drawPath(sweatPath(f, f2), fill(-8599822));
    }

    private static void sweatDrop(Canvas canvas, float f, float f2, float f3, float f4, boolean z) {
        float f5 = z ? ((f3 / 1.9f) + f4) % 1.0f : 0.0f;
        Paint fill = fill(-8599822);
        fill.setAlpha(Math.max(0, (int) ((1.0f - (0.75f * f5)) * 255.0f)));
        canvas.drawPath(sweatPath(f, f2 + (f5 * 7.5f)), fill);
    }

    private static void steam(Canvas canvas, float f, float f2, float f3, boolean z) {
        Paint stroke = stroke(-4602162, 1.5f);
        for (int i = 0; i < 2; i++) {
            float f4 = z ? ((f3 / 2.3f) + (i * 0.5f)) % 1.0f : i * 0.5f;
            float f5 = f2 - (9.0f * f4);
            stroke.setAlpha(Math.min(255, ((int) ((1.0f - f4) * 200.0f)) + 40));
            Path path = new Path();
            float f6 = (i * 4.0f) + f;
            path.moveTo(f6, f5);
            path.quadTo(1.2f + f6, f5 - 2.6f, f6, f5 - 5.2f);
            canvas.drawPath(path, stroke);
        }
    }

    private static void zzzFloat(Canvas canvas, float f, float f2, int i, float f3, boolean z, float f4) {
        float[] fArr = {0.0f, 8.0f, 13.0f};
        float[] fArr2 = {0.0f, -9.0f, -16.0f};
        float[] fArr3 = {13.0f, 10.0f, 7.5f};
        float[] fArr4 = {1.0f, 0.95f, 0.72f};
        int i2 = (i >>> 24) & 255;
        Paint paint = new Paint(1);
        paint.setColor(i);
        paint.setStyle(Paint.Style.FILL);
        paint.setFakeBoldText(true);
        int i3 = 0;
        while (i3 < OVERTIME) {
            float f5 = z ? ((f3 / f4) + (i3 * 0.34f)) % 1.0f : i3 * 0.34f;
            paint.setAlpha(Math.max(0, (int) (i2 * fArr4[i3] * (1.0f - (0.8f * f5)))));
            paint.setTextSize(fArr3[i3]);
            canvas.drawText(i3 == 0 ? "Z" : "z", fArr[i3] + f + (2.2f * f5), (f2 + fArr2[i3]) - (f5 * 6.0f), paint);
            i3++;
        }
    }

    private static void confettiFall(Canvas canvas, Palette palette, float f, boolean z) {
        float[] fArr = {24.0f, 76.0f, 34.0f, 66.0f, 50.0f};
        float[] fArr2 = {2.6f, 2.6f, 2.2f, 2.2f, 2.0f};
        for (int i = 0; i < 5; i++) {
            float f2 = z ? ((f / 2.8f) + (i * 0.21f)) % 1.0f : (i * 0.18f) + 0.05f;
            Paint fill = fill(palette.conf[i]);
            fill.setAlpha(Math.max(0, (int) ((1.0f - (f2 * 0.75f)) * 255.0f)));
            canvas.drawCircle(fArr[i] + (z ? ((float) Math.sin(((i * 1.3f) + f) * 1.6f)) * 2.2f : 0.0f), (48.0f * f2) + 3.0f, fArr2[i], fill);
        }
    }

    private static void rainLines(Canvas canvas, Palette palette, float f, boolean z, float[] fArr, float f2, float f3) {
        Paint stroke = stroke(palette.rain, 1.8f);
        int i = (palette.rain >>> 24) & 255;
        for (int i2 = 0; i2 < fArr.length; i2++) {
            float f4 = z ? ((f / 1.5f) + (i2 * 0.37f)) % 1.0f : (i2 * 0.37f) % 0.85f;
            float f5 = ((f3 - f2) * f4) + f2;
            stroke.setAlpha(Math.max(0, (int) (i * (1.0f - (f4 * 0.45f)))));
            canvas.drawLine(fArr[i2], f5, fArr[i2] - 0.8f, f5 + 4.6f, stroke);
        }
    }

    private static void drawCommute(Canvas canvas, Palette palette, float f, boolean z) {
        shadow(canvas, palette, 91.0f);
        body(canvas, palette, 50.0f, 58.0f, 21.0f, 24.0f, 0.0f);
        Paint stroke = stroke(palette.limb, 3.0f);
        Path path = new Path();
        path.moveTo(31.0f, 56.0f);
        path.quadTo(24.0f, 60.0f, 22.0f, 66.0f);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        path2.moveTo(69.0f, 56.0f);
        path2.quadTo(75.0f, 60.0f, 76.0f, 65.0f);
        canvas.drawPath(path2, stroke);
        caseBag(canvas, palette, 70.0f, 66.0f);
        legsWalk(canvas, palette, 80.0f, f, z);
        eyesN(canvas, palette, 50.0f, 54.0f, 6.5f, blinkAt(f, 0.7f, z));
        mouth(canvas, palette, 50.0f, 63.0f, 2);
        Paint stroke2 = stroke(palette.bgFaint, 2.0f);
        if (z) {
            stroke2.setAlpha(Math.max(0, Math.min(255, ((int) (Math.sin(5.2f * f) * 55.0d)) + 140)));
        }
        canvas.drawLine(22.0f, 44.0f, 27.0f, 46.0f, stroke2);
        canvas.drawLine(20.0f, 50.0f, 26.0f, 51.5f, stroke2);
        canvas.drawLine(73.0f, 36.0f, 78.0f, 38.0f, stroke2);
        canvas.drawLine(75.0f, 42.0f, 81.0f, 43.5f, stroke2);
    }

    private static void drawWorkAm(Canvas canvas, Palette palette, float f, boolean z) {
        shadow(canvas, palette, 91.0f);
        body(canvas, palette, 50.0f, 54.0f, 21.0f, 22.0f, 0.0f);
        float sin = z ? ((float) Math.sin(f * 8.4f)) * 1.2f : 0.0f;
        float sin2 = z ? ((float) Math.sin((8.4f * f) + 3.1415927f)) * 1.2f : 0.0f;
        Paint stroke = stroke(palette.limb, 3.0f);
        Path path = new Path();
        path.moveTo(33.0f, 60.0f);
        path.quadTo(40.0f, 68.0f, 45.0f, sin + 71.0f);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        path2.moveTo(67.0f, 60.0f);
        path2.quadTo(60.0f, 68.0f, 55.0f, sin2 + 71.0f);
        canvas.drawPath(path2, stroke);
        laptop(canvas, palette, 50.0f, 64.0f);
        legsWalk(canvas, palette, 76.0f, f, z);
        eyesN(canvas, palette, 50.0f, 50.0f, 6.5f, blinkAt(f, 1.4f, z));
        mouth(canvas, palette, 50.0f, 59.0f, 0);
        sparkle(canvas, 74.0f, 30.0f, (z ? 1.0f + (((float) Math.sin(3.4f * f)) * 0.28f) : 1.0f) * 5.4f, palette.moon);
    }

    private static void drawWorkPm(Canvas canvas, Palette palette, float f, boolean z) {
        shadow(canvas, palette, 91.0f);
        body(canvas, palette, 50.0f, 58.0f, 21.0f, 22.0f, 0.0f);
        Paint stroke = stroke(palette.limb, 3.0f);
        Path path = new Path();
        path.moveTo(31.0f, 56.0f);
        path.quadTo(26.0f, 62.0f, 28.0f, 68.0f);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        path2.moveTo(69.0f, 56.0f);
        path2.quadTo(74.0f, 60.0f, 73.0f, 64.0f);
        canvas.drawPath(path2, stroke);
        canvas.drawRoundRect(new RectF(70.0f, 62.0f, 80.0f, 72.0f), 2.0f, 2.0f, fill(palette.bowl));
        Paint stroke2 = stroke(-1775124, 1.0f);
        canvas.drawRoundRect(new RectF(70.0f, 62.0f, 80.0f, 72.0f), 2.0f, 2.0f, stroke2);
        Path path3 = new Path();
        path3.moveTo(80.0f, 64.5f);
        path3.quadTo(83.6f, 64.5f, 83.6f, 67.0f);
        path3.quadTo(83.6f, 69.5f, 80.0f, 69.5f);
        canvas.drawPath(path3, stroke2);
        steam(canvas, 72.0f, 60.0f, f, z);
        sweat(canvas, 28.0f, 40.0f);
        legsStand(canvas, palette, 80.0f);
        eyesHalf(canvas, palette, 50.0f, 54.0f, 6.5f);
        mouth(canvas, palette, 50.0f, 63.0f, OVERTIME);
    }

    private static void drawOvertime(Canvas canvas, Palette palette, float f, boolean z) {
        shadow(canvas, palette, 91.0f);
        body(canvas, palette, 50.0f, 56.0f, 21.0f, 22.0f, 0.0f);
        Paint stroke = stroke(palette.limb, 3.0f);
        Path path = new Path();
        path.moveTo(33.0f, 62.0f);
        path.quadTo(40.0f, 69.0f, 45.0f, 72.0f);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        path2.moveTo(67.0f, 62.0f);
        path2.quadTo(60.0f, 69.0f, 55.0f, 72.0f);
        canvas.drawPath(path2, stroke);
        laptop(canvas, palette, 50.0f, 65.0f);
        legsWalk(canvas, palette, 77.0f, f, z);
        eyesN(canvas, palette, 50.0f, 51.0f, 6.5f, blinkAt(f, 2.2f, z));
        Paint stroke2 = stroke(1460025903, 1.8f);
        canvas.drawOval(new RectF(41.0f, 54.0f, 47.0f, 60.0f), stroke2);
        canvas.drawOval(new RectF(53.0f, 54.0f, 59.0f, 60.0f), stroke2);
        mouth(canvas, palette, 50.0f, 63.0f, 4);
        sweatDrop(canvas, 28.0f, 42.0f, f, 0.0f, z);
        sweatDrop(canvas, 74.0f, 44.0f, f, 0.5f, z);
        Paint fill = fill(palette.moon);
        Path path3 = new Path();
        path3.moveTo(16.0f, 28.0f);
        path3.arcTo(new RectF(16.0f, 18.0f, 36.0f, 38.0f), 90.0f, 180.0f);
        path3.arcTo(new RectF(24.0f, 18.0f, 36.0f, 38.0f), 270.0f, -180.0f);
        path3.close();
        canvas.drawPath(path3, fill);
        float sin = z ? (((float) Math.sin(3.8f * f)) * 0.45f) + 0.8f : 1.0f;
        Paint fill2 = fill(palette.bgFaint);
        canvas.drawCircle(80.0f, 30.0f, 2.0f * sin, fill2);
        canvas.drawCircle(85.0f, 27.0f, sin * 1.5f, fill2);
    }

    private static void drawOffWork(Canvas canvas, Palette palette, float f, boolean z) {
        float sin = z ? ((float) Math.sin(((f % 2.2f) / 2.2f) * 3.1415927f)) * 4.0f : 0.0f;
        shadowScaled(canvas, palette, 92.0f, 1.0f - (sin / 14.0f));
        int save = canvas.save();
        canvas.translate(0.0f, -sin);
        body(canvas, palette, 50.0f, 52.0f, 21.0f, 23.0f, 0.0f);
        Paint stroke = stroke(palette.limb, 3.0f);
        Path path = new Path();
        path.moveTo(31.0f, 46.0f);
        path.quadTo(22.0f, 40.0f, 20.0f, 32.0f);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        path2.moveTo(69.0f, 46.0f);
        path2.quadTo(78.0f, 40.0f, 80.0f, 32.0f);
        canvas.drawPath(path2, stroke);
        legsStand(canvas, palette, 74.0f);
        eyesHappy(canvas, palette, 50.0f, 48.0f, 6.5f);
        mouth(canvas, palette, 50.0f, 58.0f, 1);
        sparkle(canvas, 50.0f, 12.0f, (z ? 1.0f + (((float) Math.sin(4.4f * f)) * 0.3f) : 1.0f) * 5.0f, palette.conf[1]);
        canvas.restoreToCount(save);
        confettiFall(canvas, palette, f, z);
    }

    private static void drawLunch(Canvas canvas, Palette palette, float f, boolean z) {
        shadow(canvas, palette, 91.0f);
        body(canvas, palette, 50.0f, 54.0f, 21.0f, 22.0f, 0.0f);
        Paint stroke = stroke(palette.limb, 3.0f);
        Path path = new Path();
        path.moveTo(31.0f, 58.0f);
        path.quadTo(26.0f, 63.0f, 28.0f, 68.0f);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        path2.moveTo(69.0f, 58.0f);
        path2.quadTo(74.0f, 63.0f, 72.0f, 68.0f);
        canvas.drawPath(path2, stroke);
        legsStand(canvas, palette, 78.0f);
        canvas.drawOval(new RectF(36.0f, 70.6f, 64.0f, 81.4f), fill(palette.bowl));
        canvas.drawOval(new RectF(39.4f, 70.4f, 60.6f, 78.4f), fill(palette.rice));
        float sin = z ? ((float) Math.sin(5.6f * f)) * 1.2f : 0.0f;
        Paint stroke2 = stroke(-3696037, 2.0f);
        canvas.drawLine(60.0f, sin + 62.0f, 70.0f, 58.0f + sin, stroke2);
        canvas.drawLine(61.6f, sin + 64.4f, 71.6f, sin + 60.4f, stroke2);
        eyesHappy(canvas, palette, 50.0f, 52.0f, 6.5f);
        mouth(canvas, palette, 50.0f, 61.0f, 5);
        Paint stroke3 = stroke(palette.moon, 2.4f);
        if (z) {
            stroke3.setAlpha(Math.max(0, Math.min(255, (int) (((Math.sin(4.2f * f) * 0.44999998807907104d) + 0.6499999761581421d) * 255.0d))));
        }
        canvas.drawLine(74.0f, 36.0f, 76.0f, 31.0f, stroke3);
        canvas.drawLine(78.0f, 40.0f, 83.0f, 38.4f, stroke3);
    }

    /**
     * 周末躺平。
     *
     * <p><b>白天和深夜是两种样子</b>：
     * <ul>
     *   <li>深夜 / 清晨（0-8 点、22 点后）→ 闭眼 + 冒 ZZZ（真的在睡）</li>
     *   <li>白天 → 睁眼放松 + 没有 ZZZ（下午三点还冒 Z 像通宵倒下了）</li>
     * </ul>
     *
     * <p>整体比原来收窄（原来宽 62，比加班姿势宽一倍，看着像「一坨」）。
     *
     * @param asleep 是不是睡着的时段
     */
    private static void drawWeekend(Canvas canvas, Palette palette, float f, boolean z, boolean asleep) {
        // 垫子（收窄到 26~72）
        canvas.drawOval(new RectF(26.0f, 78.0f, 72.0f, 86.0f), fill(palette.shadow));
        // 枕头
        canvas.drawRoundRect(new RectF(24.0f, 66.0f, 40.0f, 81.0f), 7.0f, 7.0f, fill(palette.pillow));
        // 躺着的身体（横向椭圆，比原来窄）
        Paint fill = fill(-16777216);
        fill.setShader(new LinearGradient(32.0f, 50.0f, 72.0f, 80.0f,
                palette.bodyTop, palette.bodyBottom, Shader.TileMode.CLAMP));
        canvas.drawOval(new RectF(32.0f, 50.0f, 72.0f, 80.0f), fill);
        fill.setShader(null);
        // 高光
        fill.setColor(1207959551);
        canvas.drawOval(new RectF(39.0f, 55.0f, 54.0f, 64.0f), fill);

        if (asleep) {
            eyesHappy(canvas, palette, 56.0f, 60.0f, 5.0f);      // 闭眼笑
            mouth(canvas, palette, 56.0f, 68.0f, 0);
            zzzFloat(canvas, 70.0f, 42.0f, palette.zzz, f, z, 2.8f);
        } else {
            // 白天：睁眼放松（半睁），没有 ZZZ
            Paint eye = fill(palette.face);
            canvas.drawOval(new RectF(50.5f, 58.0f, 55.5f, 62.0f), eye);
            canvas.drawOval(new RectF(59.5f, 58.0f, 64.5f, 62.0f), eye);
            mouth(canvas, palette, 56.0f, 68.0f, 0);
        }

        // 两条小腿
        Paint stroke = stroke(palette.limb, 2.6f);
        canvas.drawLine(40.0f, 78.0f, 37.0f, 84.0f, stroke);
        canvas.drawLine(48.0f, 80.0f, 47.0f, 86.0f, stroke);
    }

    private static void drawHolidayWork(Canvas canvas, Palette palette, float f, boolean z) {
        shadow(canvas, palette, 91.0f);
        body(canvas, palette, 50.0f, 60.0f, 21.0f, 24.0f, 0.0f);
        Paint stroke = stroke(palette.limb, 3.0f);
        Path path = new Path();
        path.moveTo(31.0f, 58.0f);
        path.quadTo(25.0f, 64.0f, 26.0f, 70.0f);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        path2.moveTo(69.0f, 58.0f);
        path2.quadTo(74.0f, 63.0f, 73.0f, 68.0f);
        canvas.drawPath(path2, stroke);
        caseBag(canvas, palette, 69.0f, 69.0f);
        legsStand(canvas, palette, 82.0f);
        eyesSad(canvas, palette, 50.0f, 56.0f, 6.5f, blinkAt(f, 3.0f, z));
        mouth(canvas, palette, 50.0f, 65.0f, 4);
        cloud(canvas, palette, 37.0f, 40.0f, 1.0f);
        rainLines(canvas, palette, f, z, new float[]{36.0f, 41.0f}, 52.0f, 59.5f);
        sweat(canvas, 63.0f, 44.0f);
    }

    private static void drawLeave(Canvas canvas, Palette palette, float f, boolean z) {
        canvas.drawOval(new RectF(22.0f, 78.0f, 74.0f, 86.0f), fill(palette.shadow));
        canvas.drawRoundRect(new RectF(20.0f, 68.0f, 46.0f, 83.0f), 7.0f, 7.0f, fill(palette.pillow));
        Paint fill = fill(-16777216);
        fill.setShader(new LinearGradient(30.0f, 48.0f, 82.0f, 80.0f, palette.bodyTop, palette.bodyBottom, Shader.TileMode.CLAMP));
        canvas.drawOval(new RectF(30.0f, 48.0f, 82.0f, 80.0f), fill);
        fill.setShader(null);
        fill.setColor(1207959551);
        canvas.drawOval(new RectF(39.0f, 53.0f, 57.0f, 63.0f), fill);
        eyesHappy(canvas, palette, 60.0f, 60.0f, 5.6f);
        mouth(canvas, palette, 60.0f, 68.0f, 0);
        int save = canvas.save();
        canvas.rotate(18.0f, 56.0f, 55.0f);
        canvas.drawRoundRect(new RectF(52.0f, 48.0f, 61.0f, 62.0f), 2.4f, 2.4f, fill(palette.phone));
        canvas.restoreToCount(save);
        Paint stroke = stroke(palette.limb, 2.6f);
        canvas.drawLine(37.0f, 78.0f, 34.0f, 84.0f, stroke);
        canvas.drawLine(45.0f, 80.0f, 44.0f, 86.0f, stroke);
        zzzFloat(canvas, 76.0f, 40.0f, palette.zzzLeave, f, z, 3.1f);
    }

    private static void drawMonday(Canvas canvas, Palette palette, float f, boolean z) {
        shadow(canvas, palette, 91.0f);
        body(canvas, palette, 50.0f, 60.0f, 21.0f, 23.0f, 0.0f);
        Paint stroke = stroke(palette.limb, 3.0f);
        Path path = new Path();
        path.moveTo(31.0f, 60.0f);
        path.quadTo(27.0f, 66.0f, 29.0f, 71.0f);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        path2.moveTo(69.0f, 60.0f);
        path2.quadTo(73.0f, 66.0f, 71.0f, 71.0f);
        canvas.drawPath(path2, stroke);
        legsStand(canvas, palette, 82.0f);
        eyesDead(canvas, palette, 50.0f, 56.0f, 6.5f);
        mouth(canvas, palette, 50.0f, 65.0f, OVERTIME);
        cloud(canvas, palette, 37.0f, 40.0f, 1.0f);
        rainLines(canvas, palette, f, z, new float[]{35.0f, 40.0f, 45.0f}, 52.0f, 59.0f);
    }

    private static void drawFriday(Canvas canvas, Palette palette, float f, boolean z) {
        float sin = z ? ((float) Math.sin(((f % 2.6f) / 2.6f) * 3.1415927f)) * 2.2f : 0.0f;
        shadowScaled(canvas, palette, 91.0f, 1.0f - (sin / 12.0f));
        int save = canvas.save();
        canvas.translate(0.0f, -sin);
        body(canvas, palette, 50.0f, 56.0f, 21.0f, 24.0f, 0.0f);
        Paint stroke = stroke(palette.limb, 3.0f);
        Path path = new Path();
        path.moveTo(31.0f, 54.0f);
        path.quadTo(24.0f, 49.0f, 22.0f, 43.0f);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        path2.moveTo(69.0f, 54.0f);
        path2.quadTo(76.0f, 49.0f, 78.0f, 43.0f);
        canvas.drawPath(path2, stroke);
        legsStand(canvas, palette, 79.0f);
        eyesHappy(canvas, palette, 50.0f, 52.0f, 6.5f);
        mouth(canvas, palette, 50.0f, 62.0f, 1);
        canvas.restoreToCount(save);
        confettiFall(canvas, palette, f, z);
    }

    private static void drawSunday(Canvas canvas, Palette palette, float f, boolean z) {
        shadow(canvas, palette, 91.0f);
        body(canvas, palette, 50.0f, 58.0f, 21.0f, 24.0f, 0.0f);
        Paint stroke = stroke(palette.limb, 3.0f);
        Path path = new Path();
        path.moveTo(31.0f, 56.0f);
        path.quadTo(27.0f, 62.0f, 29.0f, 67.0f);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        path2.moveTo(69.0f, 56.0f);
        path2.quadTo(73.0f, 62.0f, 71.0f, 67.0f);
        canvas.drawPath(path2, stroke);
        legsStand(canvas, palette, 80.0f);
        eyesN(canvas, palette, 50.0f, 54.0f, 6.5f, blinkAt(f, 1.1f, z));
        mouth(canvas, palette, 50.0f, 63.0f, 4);
        Paint stroke2 = stroke(palette.faceFaint, 1.6f);
        canvas.drawLine(42.0f, 53.4f, 46.0f, 54.4f, stroke2);
        canvas.drawLine(54.0f, 54.4f, 58.0f, 53.4f, stroke2);
        Paint stroke3 = stroke(-2053825, 1.4f);
        Path path3 = new Path();
        path3.moveTo(76.0f, 30.0f);
        path3.lineTo(86.0f, 30.0f);
        path3.lineTo(80.6f, 37.0f);
        path3.lineTo(86.0f, 44.0f);
        path3.lineTo(76.0f, 44.0f);
        path3.lineTo(81.4f, 37.0f);
        path3.close();
        canvas.drawPath(path3, fill(1509214059));
        canvas.drawPath(path3, stroke3);
        Paint fill = fill(-2053825);
        if (z) {
            fill.setAlpha(Math.max(0, Math.min(255, (int) ((Math.sin(2.2f * f) * 45.0d) + 210.0d))));
        }
        Path path4 = new Path();
        path4.moveTo(78.6f, 41.0f);
        path4.lineTo(83.4f, 41.0f);
        path4.lineTo(81.0f, 43.6f);
        path4.close();
        canvas.drawPath(path4, fill);
        Paint fill2 = fill(palette.bgFaint);
        if (z) {
            fill2.setAlpha(Math.max(0, Math.min(255, (int) ((Math.sin(3.6f * f) * 95.0d) + 120.0d))));
        }
        canvas.drawCircle(34.0f, 34.0f, 2.0f, fill2);
        canvas.drawCircle(30.0f, 30.0f, 1.4f, fill2);
    }

    private static void drawReactSurprised(Canvas canvas, Palette palette, float f, boolean z) {
        shadow(canvas, palette, 91.0f);
        float sin = z ? ((float) Math.sin(18.0f * f)) * 0.8f : 0.0f;
        int save = canvas.save();
        canvas.translate(sin, 0.0f);
        body(canvas, palette, 50.0f, 58.0f, 21.0f, 24.0f, 0.0f);
        Paint stroke = stroke(palette.limb, 3.0f);
        Path path = new Path();
        path.moveTo(31.0f, 58.0f);
        path.quadTo(24.0f, 54.0f, 22.0f, 48.0f);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        path2.moveTo(69.0f, 58.0f);
        path2.quadTo(76.0f, 54.0f, 78.0f, 48.0f);
        canvas.drawPath(path2, stroke);
        legsStand(canvas, palette, 82.0f);
        Paint fill = fill(-1);
        Paint fill2 = fill(palette.face);
        canvas.drawCircle(43.5f, 53.0f, 4.2f, fill);
        canvas.drawCircle(56.5f, 53.0f, 4.2f, fill);
        canvas.drawCircle(43.5f, 53.0f, 2.1f, fill2);
        canvas.drawCircle(56.5f, 53.0f, 2.1f, fill2);
        mouth(canvas, palette, 50.0f, 64.0f, 2);
        Paint paint = new Paint(1);
        paint.setColor(-1096636);
        paint.setFakeBoldText(true);
        paint.setTextSize(15.0f);
        float sin2 = z ? ((float) Math.sin(f * 14.0f)) * 1.2f : 0.0f;
        canvas.drawText("!", 24.0f + sin2, 34.0f, paint);
        canvas.drawText("!", 68.0f - sin2, 32.0f, paint);
        canvas.restoreToCount(save);
    }

    private static void drawReactCheer(Canvas canvas, Palette palette, float f, boolean z) {
        shadow(canvas, palette, 91.0f);
        body(canvas, palette, 50.0f, 58.0f, 21.0f, 24.0f, 0.0f);
        float sin = z ? (((float) Math.sin(6.2f * f)) * 1.6f) + 4.2f : 4.2f;
        Paint stroke = stroke(palette.limb, 3.0f);
        Path path = new Path();
        path.moveTo(31.0f, 56.0f);
        path.quadTo(24.0f, 50.0f, 22.0f, 42.0f - sin);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        path2.moveTo(69.0f, 56.0f);
        path2.quadTo(76.0f, 60.0f, 77.0f, 66.0f);
        canvas.drawPath(path2, stroke);
        Paint fill = fill(-16777216);
        fill.setShader(new LinearGradient(18.0f, 36.0f - sin, 26.0f, 44.0f - sin, palette.bodyTop, palette.bodyBottom, Shader.TileMode.CLAMP));
        canvas.drawCircle(22.0f, 40.0f - sin, 4.2f, fill);
        legsStand(canvas, palette, 82.0f);
        eyesHappy(canvas, palette, 50.0f, 54.0f, 6.5f);
        mouth(canvas, palette, 50.0f, 63.0f, 5);
        float sin2 = z ? 1.0f + (((float) Math.sin(7.0f * f)) * 0.3f) : 1.0f;
        sparkle(canvas, 74.0f, 36.0f, 4.5f * sin2, palette.moon);
        sparkle(canvas, 28.0f, 28.0f, sin2 * 3.2f, palette.moon);
    }

    private static void drawReactShy(Canvas canvas, Palette palette, float f, boolean z) {
        shadow(canvas, palette, 91.0f);
        float sin = z ? ((float) Math.sin(f * 2.6f)) * 3.0f : 0.0f;
        int save = canvas.save();
        canvas.rotate(sin, 50.0f, 78.0f);
        body(canvas, palette, 50.0f, 58.0f, 21.0f, 24.0f, 0.0f);
        Paint stroke = stroke(palette.limb, 3.0f);
        Path path = new Path();
        path.moveTo(31.0f, 58.0f);
        path.quadTo(24.0f, 62.0f, 24.0f, 68.0f);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        path2.moveTo(69.0f, 58.0f);
        path2.quadTo(76.0f, 62.0f, 76.0f, 68.0f);
        canvas.drawPath(path2, stroke);
        legsStand(canvas, palette, 82.0f);
        eyesHappy(canvas, palette, 50.0f, 54.0f, 6.5f);
        mouth(canvas, palette, 50.0f, 63.0f, 0);
        Paint fill = fill(1945399990);
        canvas.drawOval(new RectF(32.4f, 58.0f, 41.6f, 64.0f), fill);
        canvas.drawOval(new RectF(58.4f, 58.0f, 67.6f, 64.0f), fill);
        canvas.restoreToCount(save);
    }

    /** 被连续戳烦了：双手捂耳朵、闭眼摇头。 */
    private static void drawPokeAnnoyed(Canvas canvas, Palette palette, float f, boolean z) {
        shadow(canvas, palette, 91.0f);
        float f2 = z ? ((float) Math.sin(f * 9.0f)) * 2.6f : 0.0f;
        int save = canvas.save();
        canvas.rotate(f2, 50.0f, 78.0f);
        body(canvas, palette, 50.0f, 58.0f, 21.0f, 24.0f, 0.0f);
        Paint stroke = stroke(palette.limb, 3.0f);
        // 两只手抬到头两侧（捂耳朵）
        Path path = new Path();
        path.moveTo(31.0f, 55.0f);
        path.quadTo(23.0f, 47.0f, 31.5f, 41.0f);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        path2.moveTo(69.0f, 55.0f);
        path2.quadTo(77.0f, 47.0f, 68.5f, 41.0f);
        canvas.drawPath(path2, stroke);
        legsStand(canvas, palette, 82.0f);
        // 闭眼 + 皱眉
        Paint stroke2 = stroke(palette.face, 2.2f);
        canvas.drawLine(40.0f, 52.0f, 47.0f, 55.0f, stroke2);
        canvas.drawLine(53.0f, 55.0f, 60.0f, 52.0f, stroke2);
        mouth(canvas, palette, 50.0f, 64.0f, 3);
        canvas.restoreToCount(save);
    }

    private static void drawCelebrate(Canvas canvas, Palette palette, float f, boolean z) {
        float abs = z ? Math.abs((float) Math.sin(((f % 1.6f) / 1.6f) * 3.1415927f)) * 5.0f : 0.0f;
        shadowScaled(canvas, palette, 92.0f, 1.0f - (abs / 14.0f));
        int save = canvas.save();
        canvas.translate(0.0f, -abs);
        body(canvas, palette, 50.0f, 54.0f, 21.0f, 23.0f, 0.0f);
        Paint stroke = stroke(palette.limb, 3.0f);
        Path path = new Path();
        path.moveTo(31.0f, 48.0f);
        path.quadTo(23.0f, 40.0f, 21.0f, 32.0f);
        canvas.drawPath(path, stroke);
        Path path2 = new Path();
        path2.moveTo(69.0f, 48.0f);
        path2.quadTo(77.0f, 40.0f, 79.0f, 32.0f);
        canvas.drawPath(path2, stroke);
        legsStand(canvas, palette, 77.0f);
        eyesHappy(canvas, palette, 50.0f, 50.0f, 6.5f);
        mouth(canvas, palette, 50.0f, 60.0f, 1);
        canvas.restoreToCount(save);
        confettiFall(canvas, palette, f, z);
        float sin = z ? 1.0f + (((float) Math.sin(f * 5.0f)) * 0.35f) : 1.0f;
        sparkle(canvas, 50.0f, 10.0f, 5.5f * sin, palette.conf[1]);
        float f2 = sin * 3.6f;
        sparkle(canvas, 30.0f, 16.0f, f2, palette.conf[2]);
        sparkle(canvas, 70.0f, 14.0f, f2, palette.conf[0]);
    }
}
