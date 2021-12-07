package com.inspien.pilot.file.server;

public class ServerConfig {
    private String rootDir;
    private String serverId;
    private int port;

    public ServerConfig(String rootDir, String serverId, int port) {
        this.rootDir = rootDir;
        this.serverId = serverId;
        this.port = port;
    }

    public String getRootDir() {
        return rootDir;
    }

    public String getServerId() {
        return serverId;
    }

    public int getPort() {
        return port;
    }
}
