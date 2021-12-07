package com.inspien.pilot.file.server;

public interface FileTransferServerManager {
    void start() throws Exception;
    void restart() throws Exception;
    void stop() throws Exception;
}
