package com.inspien.pilot.file.server.sftp;

import com.inspien.pilot.file.server.AccountInfoProvider;
import com.inspien.pilot.file.server.FileTransferServerManager;
import com.inspien.pilot.file.server.PermissionInfoProvider;
import com.inspien.pilot.file.util.FileUtils;
import com.inspien.pilot.file.util.TicketKeyPair;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
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
import java.security.PublicKey;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SFTPServerManager implements FileTransferServerManager {

    private static final Session.AttributeKey<String> USERNAME = new Session.AttributeKey<>();

    private String rootDir;
    private String serverId;
    private int port;
    private String privKeyPath;
    private String pubKeyPath;

    private SshServer sshServer;
    private AccountInfoProvider accountInfoProvider;
    private PermissionInfoProvider permissionInfoProvider;

    public SFTPServerManager(SFTPServerConfig config, AccountInfoProvider accountInfoProvider, PermissionInfoProvider permissionInfoProvider) {
        this.rootDir = config.getRootDir();
        this.serverId = config.getServerId();
        this.port = config.getPort();
        this.privKeyPath = config.getPrivKeyPath();
        this.pubKeyPath = config.getPubKeyPath();
        this.accountInfoProvider = accountInfoProvider;
        this.permissionInfoProvider = permissionInfoProvider;
    }

    @Override
    public void start() throws Exception {
        generateKeyPairIfNotExist(privKeyPath, pubKeyPath);

        File defaultRootDir = new File(rootDir, serverId);
        defaultRootDir.mkdirs();

        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(port);
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(privKeyPath)));
        sshServer.setFileSystemFactory(new VirtualFileSystemFactory(defaultRootDir.toPath()));

        SftpSubsystemFactory factory = new SftpSubsystemFactory.Builder()
                .withFileSystemAccessor(new PermissionFileSystemAccessor()).build();
        sshServer.setSubsystemFactories(Collections.singletonList(factory));

        sshServer.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String username, String password, ServerSession session) {
                session.setAttribute(USERNAME, username);
                String pw = accountInfoProvider.getPasswordByUsername(username);
                if (pw == null)
                    return false;
                return pw.equals(password);
            }
        });
        sshServer.setPublickeyAuthenticator(new PublickeyAuthenticator() {
            @Override
            public boolean authenticate(String username, PublicKey publicKey, ServerSession serverSession) {
                if (username.equals(accountInfoProvider.getUsernameByPublicKey(publicKey))) {
                    serverSession.setAttribute(USERNAME, username);
                    return true;
                } else
                    return false;
            }
        });

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

    @Override
    public void restart() throws IOException {
        stop();
        sshServer.start();
        System.out.println("SFTP Server restarted.. ");
    }

    @Override
    public void stop() throws IOException {
        if (sshServer != null)
            sshServer.stop();
        System.out.println("SFTP Server Stopped.. ");
    }

    private class PermissionFileSystemAccessor implements SftpFileSystemAccessor {
        @Override
        public SeekableByteChannel openFile(ServerSession session, SftpEventListenerManager subsystem, Path file, String handle, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
            if (checkUserPermission(session, file)) {
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
            return SftpFileSystemAccessor.super.openDirectory(session, subsystem, dir, handle);
        }

        private boolean checkUserPermission(ServerSession session, Path path) {
            System.out.println("check user permission!");
            List<String> userPermissions = permissionInfoProvider.getPermissionByUsername(session.getAttribute(USERNAME));
            boolean isAllowed = false;
            if (userPermissions != null) {
                for (String folderPath : userPermissions) {
                    if (path.startsWith(folderPath)) {
                        isAllowed = true;
                        break;
                    }
                }
            }
            return isAllowed;
        }
    }
}