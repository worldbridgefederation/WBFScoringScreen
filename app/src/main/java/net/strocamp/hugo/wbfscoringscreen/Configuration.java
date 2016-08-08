package net.strocamp.hugo.wbfscoringscreen;

import android.content.SharedPreferences;

public class Configuration {
    public final static String DEFAULT_URL = "http://10.100.200.99";
    public final static String DEFAULT_SERVICE_TYPE = "_wbfscreens._tcp";
    public final static String DEFAULT_SERVICE_NAME = "WBF_ScreenServer";

    private final static String KEY_URL = "wbfscreen.url";
    private final static String KEY_SERVICE_TYPE = "wbfscreen.service.type";
    private final static String KEY_SERVICE_NAME = "wbfscreen.service.name";

    private final SharedPreferences sharedPreferences;
    private String defaultUrl = DEFAULT_URL;
    private String serviceType = DEFAULT_SERVICE_TYPE;
    private String serviceName = DEFAULT_SERVICE_NAME;


    public Configuration(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        defaultUrl = sharedPreferences.getString(KEY_URL, DEFAULT_URL);
        serviceType = sharedPreferences.getString(KEY_SERVICE_TYPE, DEFAULT_SERVICE_TYPE);
        serviceName = sharedPreferences.getString(KEY_SERVICE_NAME, DEFAULT_SERVICE_NAME);
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }

    public void setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    private void persist(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
