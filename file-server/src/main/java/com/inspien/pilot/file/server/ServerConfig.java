package com.inspien.pilot.file.server;

public class ServerConfig {
    private String rootDir;
    private String configDir;
    private String serverId;
    private int port;
    public ServerConfig(String rootDir, String configDir, String serverId, int port) {
        this.rootDir = rootDir;
        this.configDir = configDir;
        this.serverId = serverId;
        this.port = port;
    }

    public String getRootDir() {
        return rootDir;
    }

    public String getConfigDir() {
        return configDir;
    }

    public String getServerId() {
        return serverId;
    }

    public int getPort() {
        return port;
    }

//    public static enum ProtocolType {
//        SFTP("SFTP"),
//        FTP("FTP");
//
//        private String value;
//
//        ProtocolType(String value) {
//            this.value = value;
//        }
//
//        public String getValue() {
//            return value;
//        }
//    }
}
