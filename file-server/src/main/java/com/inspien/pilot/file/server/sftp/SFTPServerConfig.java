package com.inspien.pilot.file.server.sftp;

import com.inspien.pilot.file.server.ServerConfig;

public class SFTPServerConfig extends ServerConfig {
    private String privKeyPath;
    private String pubKeyPath;

    public SFTPServerConfig(String rootDir, String configDir, String serverId, int port, String privKeyPath, String pubKeyPath) {
        super(rootDir, configDir, serverId, port);
        this.privKeyPath = privKeyPath;
        this.pubKeyPath = pubKeyPath;
    }

    public String getPrivKeyPath() {
        return privKeyPath;
    }

    public String getPubKeyPath() {
        return pubKeyPath;
    }
}
