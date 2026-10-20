package android.os;
/** 测试替身 */
public class Handler {
    public Handler() { }
    public Handler(Looper looper) { }
    public boolean post(Runnable r) { return true; }
    public boolean postDelayed(Runnable r, long delay) { return true; }
    public void removeCallbacks(Runnable r) { }
}
