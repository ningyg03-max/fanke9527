package com.salaryjump.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import java.security.MessageDigest;
import java.security.SecureRandom;

/* loaded from: classes.dex */
public final class PrivacyLock {
    private static final String KEY_BIO = "lock_bio";
    private static final String KEY_ENABLED = "lock_enabled";
    private static final String KEY_HASH = "lock_pin_hash";
    private static final String KEY_SALT = "lock_pin_salt";
    public static final int MAX_FAILS = 5;
    public static final int PIN_LENGTH = 4;

    private PrivacyLock() {
    }

    static boolean isEnabled(Context context) {
        return context != null && prefs(context).getBoolean(KEY_ENABLED, false) && hasPin(context);
    }

    static boolean hasPin(Context context) {
        if (context == null) {
            return false;
        }
        SharedPreferences prefs = prefs(context);
        String string = prefs.getString(KEY_HASH, "");
        String string2 = prefs.getString(KEY_SALT, "");
        return string != null && string.length() > 0 && string2 != null && string2.length() > 0;
    }

    static boolean isBiometricAllowed(Context context) {
        if (context == null) {
            return false;
        }
        return prefs(context).getBoolean(KEY_BIO, true);
    }

    static void setBiometricAllowed(Context context, boolean z) {
        if (context == null) {
            return;
        }
        prefs(context).edit().putBoolean(KEY_BIO, z).apply();
    }

    public static boolean biometricAvailable(Context context) {
        if (context == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                BiometricManager biometricManager = (BiometricManager) context.getSystemService("biometric");
                if (biometricManager != null) {
                    if (Build.VERSION.SDK_INT >= 30) {
                        if (biometricManager.canAuthenticate(255) == 0 || biometricManager.canAuthenticate(15) == 0) {
                            return true;
                        }
                    } else if (biometricManager.canAuthenticate() == 0) {
                        return true;
                    }
                }
            } catch (Throwable th) {
            }
        }
        try {
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService("fingerprint");
            if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
                return false;
            }
            return fingerprintManager.hasEnrolledFingerprints();
        } catch (Throwable th2) {
            return false;
        }
    }

    static void setPin(Context context, String str) {
        if (context == null) {
            return;
        }
        String newSalt = newSalt();
        prefs(context).edit().putBoolean(KEY_ENABLED, true).putString(KEY_SALT, newSalt).putString(KEY_HASH, hash(str, newSalt)).apply();
    }

    static void clear(Context context) {
        if (context == null) {
            return;
        }
        prefs(context).edit().remove(KEY_ENABLED).remove(KEY_SALT).remove(KEY_HASH).apply();
    }

    static boolean verify(Context context, String str) {
        if (context == null || str == null) {
            return false;
        }
        SharedPreferences prefs = prefs(context);
        String string = prefs.getString(KEY_SALT, "");
        String string2 = prefs.getString(KEY_HASH, "");
        if (string == null || string.length() == 0 || string2 == null || string2.length() == 0) {
            return false;
        }
        return constantTimeEquals(string2, hash(str, string));
    }

    public static String hash(String str, String str2) {
        if (str == null || str2 == null) {
            return "";
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest((str2 + ":" + str).getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 15, 16));
                sb.append(Character.forDigit(b & 15, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean constantTimeEquals(String str, String str2) {
        if (str == null || str2 == null || str.length() != str2.length()) {
            return false;
        }
        int i = 0;
        for (int i2 = 0; i2 < str.length(); i2++) {
            i |= str.charAt(i2) ^ str2.charAt(i2);
        }
        return i == 0;
    }

    public static boolean isValidPin(String str) {
        if (str == null || str.length() != 4) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (charAt < '0' || charAt > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isWeakPin(String str) {
        if (!isValidPin(str)) {
            return true;
        }
        char charAt = str.charAt(0);
        boolean z = true;
        boolean z2 = true;
        for (int i = 1; i < str.length(); i++) {
            if (str.charAt(i) != charAt) {
                z = false;
            }
            if (str.charAt(i) != str.charAt(i - 1) + 1) {
                z2 = false;
            }
        }
        if (z || z2) {
            return true;
        }
        boolean z3 = true;
        for (int i2 = 1; i2 < str.length(); i2++) {
            if (str.charAt(i2) != str.charAt(i2 - 1) - 1) {
                z3 = false;
            }
        }
        return z3 || "1234".equals(str) || "0000".equals(str) || "1111".equals(str) || "2222".equals(str) || "3333".equals(str) || "4444".equals(str) || "5555".equals(str) || "6666".equals(str) || "7777".equals(str) || "8888".equals(str) || "9999".equals(str) || "1212".equals(str) || "6969".equals(str) || "1122".equals(str) || "2580".equals(str) || "1029".equals(str) || "2468".equals(str) || "1357".equals(str);
    }

    private static String newSalt() {
        byte[] bArr = new byte[16];
        new SecureRandom().nextBytes(bArr);
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 16; i++) {
            byte b = bArr[i];
            sb.append(Character.forDigit((b >> 4) & 15, 16));
            sb.append(Character.forDigit(b & 15, 16));
        }
        return sb.toString();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("salary_jump", 0);
    }
}
