package com.inspien.pilot.file.server;

public class ServerConfig {
    private String rootDir;
    private String serverId;
    private int port;
    private String privKeyPath;
    private String pubKeyPath;

    public ServerConfig(String rootDir, String serverId, int port, String privKeyPath, String pubKeyPath) {
        this.rootDir = rootDir;
        this.serverId = serverId;
        this.port = port;
        this.privKeyPath = privKeyPath;
        this.pubKeyPath = pubKeyPath;
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

    public String getPrivKeyPath() {
        return privKeyPath;
    }

    public String getPubKeyPath() {
        return pubKeyPath;
    }
}
