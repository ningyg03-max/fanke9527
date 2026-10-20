package android.graphics;
/** 测试桩：空实现 */
public class RectF {
    public float left;
    public float top;
    public float right;
    public float bottom;

    public RectF() { }

    public RectF(float l, float t, float r, float b) {
        this.left = l;
        this.top = t;
        this.right = r;
        this.bottom = b;
    }

    public void set(float l, float t, float r, float b) {
        this.left = l;
        this.top = t;
        this.right = r;
        this.bottom = b;
    }

    public float width() { return this.right - this.left; }

    public float height() { return this.bottom - this.top; }
}
