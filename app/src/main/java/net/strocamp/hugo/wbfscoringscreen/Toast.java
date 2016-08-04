package net.strocamp.hugo.wbfscoringscreen;

import android.content.Context;

public class Toast {
    public static void showMessage(Context context, int resId) {
        int duration = android.widget.Toast.LENGTH_SHORT;

        android.widget.Toast toast = android.widget.Toast.makeText(context, resId, duration);
        toast.show();
    }

    public static void showMessage(Context context, String message) {
        int duration = android.widget.Toast.LENGTH_SHORT;

        android.widget.Toast toast = android.widget.Toast.makeText(context, message, duration);
        toast.show();
    }

}
