package com.inspien.pilot.file.server.ftp;

import com.inspien.pilot.file.server.AccountInfoProvider;
import com.inspien.pilot.file.server.FileTransferServerManager;
import com.inspien.pilot.file.server.PermissionInfoProvider;
import com.inspien.pilot.file.server.ServerConfig;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.AbstractUserManager;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FTPServerManager implements FileTransferServerManager {

    private final static String LISTENER_NAME = "default";
    private FtpServerFactory serverFactory;
    private ListenerFactory factory;
    private FtpServer server;

    private String rootDir;
    private String serverId;
    private int port;
    private AccountInfoProvider accountInfoProvider;
    private PermissionInfoProvider permissionInfoProvider;

    public FTPServerManager(ServerConfig config, AccountInfoProvider accountInfoProvider, PermissionInfoProvider permissionInfoProvider) {
        this.rootDir = config.getRootDir();
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
    public void start() throws Exception {
        serverFactory = new FtpServerFactory();
        factory = new ListenerFactory();
        factory.setPort(port);
        serverFactory.addListener(LISTENER_NAME, factory.createListener());

        File defaultRootPath = new File(rootDir, serverId);
        defaultRootPath.mkdirs();
        UserManager um = new ProviderUserManager(accountInfoProvider, defaultRootPath);
        serverFactory.setUserManager(um);

        serverFactory.setFileSystem(new PermissionFileSystemFactory(permissionInfoProvider));
        server = serverFactory.createServer();

        server.start();
        System.out.println("ftp server listening... [port " + port +"]");
    }

    @Override
    public void restart() throws Exception {
        stop();
        server.start();
        System.out.println("FTP Server restarted.. ");
    }

    @Override
    public void stop() throws Exception {
        if(server != null)
            server.stop();
        System.out.println("FTP Server stopped...");
    }

    private static class ProviderUserManager extends AbstractUserManager {
        private AccountInfoProvider accountInfoProvider;
        private File defaultRootPath;

        public ProviderUserManager(AccountInfoProvider accountInfoProvider, File defaultRootPath) {
            super(null, new SaltedPasswordEncryptor());
            this.accountInfoProvider = accountInfoProvider;
            this.defaultRootPath = defaultRootPath;
        }

        @Override
        public User getUserByName(String username) throws FtpException {
            return createUser(username, accountInfoProvider.getPasswordByUsername(username));
        }

        @Override
        public String[] getAllUserNames() throws FtpException {
            List<String> userList = accountInfoProvider.getUserList();
            return userList.toArray(new String[0]);
        }

        @Override
        public void delete(String username) throws FtpException {
            throw new UnsupportedOperationException("Deleting of FTP Users is not supported.");
        }

        @Override
        public void save(User user) throws FtpException {
            throw new UnsupportedOperationException("Saving of FTP Users is not supported.");
        }

        @Override
        public boolean doesExist(String username) throws FtpException {
            return accountInfoProvider.getUserList().contains(username);
        }

        @Override
        public User authenticate(Authentication authentication) throws AuthenticationFailedException {
            if(authentication instanceof UsernamePasswordAuthentication) {
                UsernamePasswordAuthentication auth = (UsernamePasswordAuthentication) authentication;
                String username = auth.getUsername();
                String password = auth.getPassword();
                if(password.equals(accountInfoProvider.getPasswordByUsername(username)))
                    return createUser(username, password);
                else
                    throw new AuthenticationFailedException("authentication failed.");
            }
            return null;
        }

        private User createUser(String username, String password) {
            BaseUser user = new BaseUser();
            user.setEnabled(true);
            user.setName(username);
            user.setPassword(password);
            user.setHomeDirectory(defaultRootPath.toString());
            user.setAuthorities(Arrays.asList(
                    new Authority[] {
                            new ConcurrentLoginPermission(1,1),
                            new WritePermission()
                    }));
            user.setMaxIdleTime(10000);
            return user;
        }
    }
}
