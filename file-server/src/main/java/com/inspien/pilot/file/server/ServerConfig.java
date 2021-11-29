package com.inspien.pilot.file.server;

public class ServerConfig {
    private String rootDir;
    private String serverId;
    private int port;
    private String privKeyPath;
    private String pubKeyPath;
    private String authorizedKeysPath;

    public ServerConfig(String rootDir, String serverId, int port, String privKeyPath, String pubKeyPath, String authorizedKeysPath) {
        this.rootDir = rootDir;
        this.serverId = serverId;
        this.port = port;
        this.privKeyPath = privKeyPath;
        this.pubKeyPath = pubKeyPath;
        this.authorizedKeysPath = authorizedKeysPath;
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

    public String getAuthorizedKeysPath() {
        return authorizedKeysPath;
    }
}
