package net.strocamp.hugo.wbfscoringscreen;

import android.content.Context;
import android.util.Log;

public class Toast {
    public static void showMessage(Context context, int resId) {
        int duration = android.widget.Toast.LENGTH_SHORT;

        Log.i("Toast", "resource id " + resId);

        android.widget.Toast toast = android.widget.Toast.makeText(context, resId, duration);
        if (toast != null) {
            toast.show();
        }
    }

    public static void showMessage(Context context, String message) {
        int duration = android.widget.Toast.LENGTH_SHORT;

        Log.i("Toast", message);

        android.widget.Toast toast = android.widget.Toast.makeText(context, message, duration);
        if (toast != null) {
            toast.show();
        }
    }

}
