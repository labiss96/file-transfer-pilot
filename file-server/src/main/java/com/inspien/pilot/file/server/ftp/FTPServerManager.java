package com.inspien.pilot.file.server.ftp;

import com.inspien.pilot.file.server.AccountInfoProvider;
import com.inspien.pilot.file.server.FileTransferServerManager;
import com.inspien.pilot.file.server.PermissionInfoProvider;
import com.inspien.pilot.file.server.ServerConfig;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class FTPServerManager implements FileTransferServerManager {
    private String rootDir;
    private String configDir;
    private String serverId;
    private int port;

    private FtpServerFactory serverFactory;
    private ListenerFactory factory;
    private FtpServer server;
    private AccountInfoProvider accountInfoProvider;
    private PermissionInfoProvider permissionInfoProvider;
    private final static String USER_PROPERTIES = "users.properties";
    private final static String LISTENER_NAME = "default";

    public FTPServerManager(ServerConfig config, AccountInfoProvider accountInfoProvider, PermissionInfoProvider permissionInfoProvider) {
        this.rootDir = config.getRootDir();
        this.configDir = config.getConfigDir();
        this.serverId = config.getServerId();
        this.port = config.getPort();
        this.accountInfoProvider = accountInfoProvider;
        this.permissionInfoProvider = permissionInfoProvider;
    }

    @Override
    public void setAccountInfo(AccountInfoProvider accountInfoProvider) {
        this.accountInfoProvider = accountInfoProvider;
    }

    @Override
    public void setPermissionInfo(PermissionInfoProvider permissionInfoProvider) {
        this.permissionInfoProvider = permissionInfoProvider;
    }

    @Override
    public void start() throws FtpException, IOException {
        serverFactory = new FtpServerFactory();
        factory = new ListenerFactory();
        factory.setPort(port);
        serverFactory.addListener(LISTENER_NAME, factory.createListener());

        File propFile = new File(configDir + serverId, USER_PROPERTIES);
        propFile.getParentFile().mkdirs();
        if(!propFile.exists())
            propFile.createNewFile();

        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setFile(propFile);
        userManagerFactory.setPasswordEncryptor(new SaltedPasswordEncryptor());
        UserManager um = userManagerFactory.createUserManager();

        File defaultRootPath = new File(rootDir, serverId);
        defaultRootPath.mkdirs();
        List<String> userList = accountInfoProvider.getUserList();
        BaseUser user;
        for(String username : userList) {
            user = new BaseUser();
            user.setName(username);
            user.setPassword(accountInfoProvider.getPasswordByUsername(username));
            user.setHomeDirectory(defaultRootPath.toString());
            user.setAuthorities(Arrays.asList(new WritePermission()));
            um.save(user);
        }

        serverFactory.setFileSystem(new PermissionFileSystemFactory(permissionInfoProvider));
        serverFactory.setUserManager(um);
        server = serverFactory.createServer();

        server.start();
        System.out.println("ftp server listening... [port " + port +"]");
    }

    @Override
    public void restart() throws Exception {
    }

    @Override
    public void stop() throws Exception {
        server.stop();
        System.out.println("FTP Server stopped...");
    }
}
