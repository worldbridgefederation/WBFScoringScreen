package org.worldbridge.development.wbfscoringscreen;

import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import org.worldbridge.development.wbfscoringscreen.domain.HardwareDetails;
import org.worldbridge.development.wbfscoringscreen.domain.ScreenDetails;
import org.worldbridge.development.wbfscoringscreen.domain.VersionDetails;

public class StatusHelper {
    public static HardwareDetails getHardwareDetails() {
        HardwareDetails hardwareDetails = new HardwareDetails();
        hardwareDetails.setHardware(Build.HARDWARE);

        try {
            hardwareDetails.setReleaseVersion(Build.VERSION.RELEASE);
        } catch (NoSuchFieldError e) {
            // Ignore
        }

        hardwareDetails.setBrand(Build.BRAND);
        hardwareDetails.setDevice(Build.DEVICE);
        hardwareDetails.setDisplay(Build.DISPLAY);
        hardwareDetails.setManufacturer(Build.MANUFACTURER);
        hardwareDetails.setModel(Build.MODEL);
        hardwareDetails.setType(Build.TYPE);
        hardwareDetails.setSerial(Build.SERIAL);

        return hardwareDetails;
    }

    public static VersionDetails getVersionDetails() {
        VersionDetails versionDetails = new VersionDetails();
        versionDetails.setApplicationId(BuildConfig.APPLICATION_ID);
        versionDetails.setVersionName(BuildConfig.VERSION_NAME);
        versionDetails.setVersionId(Integer.toString(BuildConfig.VERSION_CODE));

        return versionDetails;
    }

    public static ScreenDetails getScreenDetails(DisplayMetrics metrics) {
        ScreenDetails screenDetails = new ScreenDetails();
        screenDetails.setHeigth(metrics.heightPixels);
        screenDetails.setWidth(metrics.widthPixels);
        screenDetails.setXdpi(metrics.xdpi);
        screenDetails.setYdpi(metrics.ydpi);
        return screenDetails;
    }

}
