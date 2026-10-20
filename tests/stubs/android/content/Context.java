package android.content;
public class Context {
    public SharedPreferences getSharedPreferences(String name, int mode) { return null; }
    public Context getApplicationContext() { return this; }
    public Object getSystemService(String name) { return null; }
    public int checkSelfPermission(String p) { return -1; }
    public ContentResolver getContentResolver() { return null; }
}
