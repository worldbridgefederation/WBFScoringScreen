package org.worldbridge.development.wbfscoringscreen.domain;

public class StatusResponse {
    private Boolean showNotitification;
    private Notification notification;

    public Boolean getShowNotitification() {
        return showNotitification;
    }

    public void setShowNotitification(Boolean showNotitification) {
        this.showNotitification = showNotitification;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }
}
