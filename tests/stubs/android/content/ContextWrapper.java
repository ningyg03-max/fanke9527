package android.content;
/** 测试替身：模拟 Dialog 对 context 的包装 */
public class ContextWrapper extends Context {
    private final Context base;
    public ContextWrapper(Context base) { this.base = base; }
    public Context getBaseContext() { return this.base; }
}
