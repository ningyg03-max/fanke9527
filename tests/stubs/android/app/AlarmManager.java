package android.app;
public class AlarmManager {
    public boolean canScheduleExactAlarms() { return false; }
    public void setExactAndAllowWhileIdle(int type, long triggerAtMillis, PendingIntent op) { }
    public void setAndAllowWhileIdle(int type, long triggerAtMillis, PendingIntent op) { }
    public void set(int type, long triggerAtMillis, PendingIntent op) { }
    public void cancel(PendingIntent op) { }
}
