package android.content;
import java.util.Map;
public interface SharedPreferences {
    Map<String, ?> getAll();
    String getString(String key, String def);
    int getInt(String key, int def);
    long getLong(String key, long def);
    boolean getBoolean(String key, boolean def);
    boolean contains(String key);
    Editor edit();
    interface Editor {
        Editor putString(String k, String v);
        Editor putInt(String k, int v);
        Editor putLong(String k, long v);
        Editor putBoolean(String k, boolean v);
        void apply();
        boolean commit();
        Editor remove(String k);
    }
}
