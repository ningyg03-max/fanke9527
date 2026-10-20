package android.graphics;
/** 测试桩：空实现 */
public class Paint {
    public static final int ANTI_ALIAS_FLAG = 1;

    public Paint() { }

    public Paint(int flags) { }

    public void setColor(int color) { }

    public void setAlpha(int alpha) { }

    public void setStyle(Style style) { }

    public void setStrokeWidth(float width) { }

    public void setStrokeCap(Cap cap) { }

    public void setShader(Shader shader) { }

    public void setFakeBoldText(boolean bold) { }

    public void setTextSize(float size) { }

    public enum Style { FILL, STROKE }

    public enum Cap { ROUND }
}
