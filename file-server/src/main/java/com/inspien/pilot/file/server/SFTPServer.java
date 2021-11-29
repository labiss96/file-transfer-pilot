package com.inspien.pilot.file.server;

import com.inspien.pilot.file.util.FileUtils;
import com.inspien.pilot.file.util.TicketKeyPair;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.SftpEventListenerManager;
import org.apache.sshd.server.subsystem.sftp.SftpFileSystemAccessor;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;

import java.io.File;
import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.FileLock;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.*;

public class SFTPServer {

    public static void main(String[] args) throws Exception {
        ServerConfig config = new ServerConfig("C:/sftp", "svc-1", 2222, "file-server/src/test/resources/sftp_server", "file-server/src/test/resources/sftp_server.pub", "file-server/src/test/resources/authorized_keys");
        SFTPServer server = new SFTPServer(config);

        Map<String, String> passwordMap = new HashMap<>();
        Map<String, List<String>> permissionMap = new HashMap<>();
        passwordMap.put("test", "1234");
        permissionMap.put("test", Arrays.asList("C:/sftp/svc-1"));
        server.setAccountsInfo(passwordMap, permissionMap);
        server.start();
        while (true);
    }

    private String rootDir;
    private String serverId;
    private int port;
    private String privKeyPath;
    private String pubKeyPath;
    private String authorizedKeysPath;

    private SshServer sshServer;

    public SFTPServer(ServerConfig config) {
        this.rootDir = config.getRootDir();
        this.serverId = config.getServerId();
        this.port = config.getPort();
        this.privKeyPath = config.getPrivKeyPath();
        this.pubKeyPath = config.getPubKeyPath();
        this.authorizedKeysPath = config.getAuthorizedKeysPath();
    }

    private Map<String, String> passwordMap = new HashMap<>();
    private Map<String, List<String>> permissionMap = new HashMap<>();

    public void setAccountsInfo(Map<String, String> passwordMap, Map<String, List<String>> permissionMap) {
        this.passwordMap = passwordMap;
        this.permissionMap = permissionMap;
    }

    public void start() throws Exception {
        generateKeyPairIfNotExist(privKeyPath, pubKeyPath);

        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(port);
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(privKeyPath)));
        sshServer.setFileSystemFactory(new VirtualFileSystemFactory(new File(rootDir, serverId).toPath()));

        SftpSubsystemFactory factory = new SftpSubsystemFactory.Builder()
                .withFileSystemAccessor(new PermissionFileSystemAccessor()).build();
        sshServer.setSubsystemFactories(Collections.singletonList(factory));


        sshServer.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String username, String password, ServerSession session) {
                return (passwordMap.containsKey(username) && passwordMap.get(username).equals(password));
            }
        });
        sshServer.setPublickeyAuthenticator(new AuthorizedKeysAuthenticator(new File(authorizedKeysPath)));

        sshServer.start();
        System.out.println("SFTP Server Started.. [port :: " + port + "]");
    }

    private static void generateKeyPairIfNotExist(String privKeyPath, String pubKeyPath) throws Exception {
        File privKeyFile = new File(privKeyPath);
        File pubKeyFile = new File(pubKeyPath);
        if (!privKeyFile.exists()) {
            TicketKeyPair ticketKeyPair = TicketKeyPair.generate();
            FileUtils.writeBytesToFile(privKeyFile, ticketKeyPair.getPrivKey());
            FileUtils.writeBytesToFile(pubKeyFile, ticketKeyPair.getPubKey());
        }
    }

    public void restart() throws IOException {
        stop();
        sshServer.start();
        System.out.println("SFTP Server restarted.. ");
    }

    public void stop() throws IOException {
        if (sshServer != null)
            sshServer.stop();
        System.out.println("SFTP Server Stopped.. ");
    }

    private class PermissionFileSystemAccessor implements SftpFileSystemAccessor {
        @Override
        public SeekableByteChannel openFile(ServerSession session, SftpEventListenerManager subsystem, Path file, String handle, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
            if(checkUserPermission(session, file)) {
                return SftpFileSystemAccessor.super.openFile(session, subsystem, file, handle, options, attrs);
            } else {
                throw new AccessDeniedException("Access denied.");
            }
        }
        @Override
        public FileLock tryLock(ServerSession session, SftpEventListenerManager subsystem, Path file, String handle, Channel channel, long position, long size, boolean shared) throws IOException {
            return SftpFileSystemAccessor.super.tryLock(session, subsystem, file, handle, channel, position, size, shared);
        }
        @Override
        public void syncFileData(ServerSession session, SftpEventListenerManager subsystem, Path file, String handle, Channel channel) throws IOException {
            SftpFileSystemAccessor.super.syncFileData(session, subsystem, file, handle, channel);
        }
        @Override
        public DirectoryStream<Path> openDirectory(ServerSession session, SftpEventListenerManager subsystem, Path dir, String handle) throws IOException {
            if(checkUserPermission(session, dir)) {
                return SftpFileSystemAccessor.super.openDirectory(session, subsystem, dir, handle);
            } else {
                throw new AccessDeniedException("Access denied.");
            }
        }
        private boolean checkUserPermission(ServerSession session, Path path) {
            List<String> userPaths = permissionMap.get(session.getUsername());
            boolean isAllowed = false;
            for(String userPath : userPaths) {
                if (path.startsWith(userPath)) {
                    isAllowed = true;
                    break;
                }
            }
            return isAllowed;
        }
    }
}