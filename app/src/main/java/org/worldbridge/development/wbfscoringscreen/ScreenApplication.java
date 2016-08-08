package org.worldbridge.development.wbfscoringscreen;

import android.app.Application;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formUri = "http://10.100.200.10:8080/rest-api/reportcrash", formKey = "test")
public class ScreenApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
    }
}
