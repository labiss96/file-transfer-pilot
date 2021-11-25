package com.inspien.pilot.file.server;

import com.inspien.pilot.file.util.FileUtils;
import com.inspien.pilot.file.util.TicketKeyPair;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SFTPServer {


    private String rootDir;
    private String serverId;
    private int port;
    private String privKeyPath;
    private String pubKeyPath;

    private SshServer sshServer;

    public SFTPServer(ServerConfig config) {
        this.rootDir = config.getRootDir();
        this.serverId = config.getServerId();
        this.port = config.getPort();
        this.privKeyPath = config.getPrivKeyPath();
        this.pubKeyPath = config.getPubKeyPath();
    }

    private Map<String, String> passwordMap = new HashMap<>();
    private Map<String, PublicKey> pubKeyMap = new HashMap<>();
    private Map<String, String> permissionMap = new HashMap<>();

    public void setAccountsInfo(Map<String, String> passwordMap, Map<String, PublicKey> pubKeyMap, Map<String, String> permissionMap) {
        this.passwordMap = passwordMap;
        this.pubKeyMap = pubKeyMap;
        this.permissionMap = permissionMap;
    }

    public void start() throws Exception {
        generateKeyPairIfNotExist(privKeyPath, pubKeyPath);

        sshServer = SshServer.setUpDefaultServer();
        sshServer.setPort(port);
        sshServer.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(privKeyPath)));
        sshServer.setFileSystemFactory(new VirtualFileSystemFactory(new File(rootDir, serverId).toPath()));
        sshServer.setCommandFactory(new ScpCommandFactory());
        sshServer.setSubsystemFactories(Arrays.asList(new SftpSubsystemFactory()));

        sshServer.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String username, String password, ServerSession session) {
                if (passwordMap.containsKey(username) && passwordMap.get(username).equals(password)) {
                    sshServer.setFileSystemFactory(new VirtualFileSystemFactory(new File(permissionMap.get(username)).toPath()));
                    return true;
                }
                return false;
            }
        });
        sshServer.setPublickeyAuthenticator(new PublickeyAuthenticator() {
            @Override
            public boolean authenticate(String username, PublicKey key, ServerSession session) {
                if (pubKeyMap.containsKey(username) && key.equals(pubKeyMap.get(username))) {
                    sshServer.setFileSystemFactory(new VirtualFileSystemFactory(new File(permissionMap.get(username)).toPath()));
                    return true;
                }
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
}