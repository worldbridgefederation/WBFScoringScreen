package org.worldbridge.development.wbfscoringscreen.domain;

public class Status {
    private String deviceId;
    private String currentUrl;
    private ScreenDetails screenDetails;
    private HardwareDetails hardwareDetails;
    private VersionDetails versionDetails;

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getCurrentUrl() {
        return currentUrl;
    }

    public void setCurrentUrl(String currentUrl) {
        this.currentUrl = currentUrl;
    }

    public ScreenDetails getScreenDetails() {
        return screenDetails;
    }

    public void setScreenDetails(ScreenDetails screenDetails) {
        this.screenDetails = screenDetails;
    }

    public HardwareDetails getHardwareDetails() {
        return hardwareDetails;
    }

    public void setHardwareDetails(HardwareDetails hardwareDetails) {
        this.hardwareDetails = hardwareDetails;
    }

    public VersionDetails getVersionDetails() {
        return versionDetails;
    }

    public void setVersionDetails(VersionDetails versionDetails) {
        this.versionDetails = versionDetails;
    }
}
