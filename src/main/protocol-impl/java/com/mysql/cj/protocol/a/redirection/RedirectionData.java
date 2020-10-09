package com.mysql.cj.protocol.a.redirection;

public class RedirectionData {

    private final String host;
    private final int port;
    private final String user;
    private final int ttl;

    RedirectionData(String host, int port, String user, int ttl) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.ttl = ttl;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public int getTtl() {
        return ttl;
    }
}
