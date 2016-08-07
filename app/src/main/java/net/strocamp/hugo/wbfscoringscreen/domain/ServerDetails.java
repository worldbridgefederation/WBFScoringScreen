package net.strocamp.hugo.wbfscoringscreen.domain;

public class ServerDetails {
    private String host;
    private String port;

    public ServerDetails(String host, String port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }
}
