package org.worldbridge.development.wbfscoringscreen.domain;

public class StatusTaskDetails {
    Status status;
    ServerDetails destination;

    public StatusTaskDetails(Status status, ServerDetails destination) {
        this.status = status;
        this.destination = destination;
    }

    public Status getStatus() {
        return status;
    }

    public ServerDetails getDestination() {
        return destination;
    }
}
