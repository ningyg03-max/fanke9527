package com.salaryjump.app;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

/* loaded from: classes.dex */
public final class Contexts {
    private Contexts() {
    }

    public static Activity anyActivityOf(Context context) {
        for (int i = 0; i < 6 && context != null; i++) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            if (context instanceof ContextWrapper) {
                context = ((ContextWrapper) context).getBaseContext();
            } else {
                return null;
            }
        }
        return null;
    }

    public static MainActivity activityOf(Context context) {
        for (int i = 0; i < 6 && context != null; i++) {
            if (context instanceof MainActivity) {
                return (MainActivity) context;
            }
            if (context instanceof ContextWrapper) {
                context = ((ContextWrapper) context).getBaseContext();
            } else {
                return null;
            }
        }
        return null;
    }
}
