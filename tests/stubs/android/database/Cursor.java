package android.database;
public interface Cursor {
    int getColumnIndex(String name);
    boolean moveToNext();
    long getLong(int i);
    String getString(int i);
    boolean isNull(int i);
    void close();
}
