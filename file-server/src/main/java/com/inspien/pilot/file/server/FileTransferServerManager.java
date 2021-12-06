package com.inspien.pilot.file.server;

public interface FileTransferServerManager {
    void start() throws Exception;
    void setAccountInfo(AccountInfoProvider accountInfoProvider);
    void setPermissionInfo(PermissionInfoProvider permissionInfoProvider);
    void restart() throws Exception;
    void stop() throws Exception;
}
