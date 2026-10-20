package android.graphics;
/** 测试桩：空实现（WorkerPet 绘制调用不验证像素，只验证姿势推导逻辑） */
public class Canvas {
    public int save() { return 0; }
    public void restoreToCount(int i) { }
    public void translate(float dx, float dy) { }
    public void rotate(float degrees, float px, float py) { }
    public void scale(float sx, float sy) { }
    public void drawOval(RectF oval, Paint paint) { }
    public void drawRoundRect(RectF rect, float rx, float ry, Paint paint) { }
    public void drawRect(RectF rect, Paint paint) { }
    public void drawRect(float l, float t, float r, float b, Paint paint) { }
    public void drawPath(Path path, Paint paint) { }
    public void drawCircle(float cx, float cy, float radius, Paint paint) { }
    public void drawText(String text, float x, float y, Paint paint) { }
    public void drawLine(float startX, float startY, float stopX, float stopY, Paint paint) { }
    public void clipPath(Path path) { }
    public void clipRect(float l, float t, float r, float b) { }
}
