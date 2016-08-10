package org.worldbridge.development.wbfscoringscreen;

import android.content.res.AssetManager;

import java.io.*;

public class NotificationHelper {
    private static String notificationContent = null;

    public static String getNotificationContent(AssetManager assetManager, String title, String message) throws IOException{
        if (notificationContent == null) {
            notificationContent = new String(read(assetManager.open("notification.html")));
        }
        return notificationContent.replaceAll("##TITLE", title).replaceAll("##MESSAGE", message);
    }

    public static byte[] read(InputStream ios) throws IOException {
        ByteArrayOutputStream ous = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            int read = 0;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        }finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
            }

            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
            }
        }
        return ous.toByteArray();
    }
}
