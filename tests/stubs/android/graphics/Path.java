package android.graphics;
/** 测试桩：空实现 */
public class Path {
    public void moveTo(float x, float y) { }

    public void lineTo(float x, float y) { }

    public void quadTo(float x1, float y1, float x2, float y2) { }

    public void arcTo(RectF oval, float startAngle, float sweepAngle) { }

    public void close() { }

    public void reset() { }

    public void addRoundRect(RectF rect, float rx, float ry, Direction direction) { }

    public enum Direction { CW, CCW }
}
