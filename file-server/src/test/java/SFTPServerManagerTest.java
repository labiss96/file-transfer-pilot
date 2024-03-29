import com.inspien.pilot.file.server.AccountInfoProvider;
import com.inspien.pilot.file.server.FileTransferServerManager;
import com.inspien.pilot.file.server.PermissionInfoProvider;
import com.inspien.pilot.file.server.sftp.SFTPServerConfig;
import com.inspien.pilot.file.server.sftp.SFTPServerManager;
import com.jcraft.jsch.*;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.PublicKeyEntry;
import org.apache.sshd.common.config.keys.PublicKeyEntryResolver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class SFTPServerManagerTest {
    @Before
    public void setup() throws Exception {
        SFTPServerConfig config = new SFTPServerConfig("C:/file-transfer/sftp", "svc-1", SERVER_PORT,
                SERVER_PRIV_KEY_PATH, SERVER_PUB_KEY_PATH
        );

        accountInfoProvider = new TestAccountInfoProvider();
        permissionInfoProvider = new TestPermissionInfoProvider();
        server = new SFTPServerManager(config, accountInfoProvider, permissionInfoProvider);
        server.start();
    }

    private AccountInfoProvider accountInfoProvider;
    private PermissionInfoProvider permissionInfoProvider;

    private FileTransferServerManager server;
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 2222;
    private static final String SERVER_PRIV_KEY_PATH = "src/test/resources/sftp_server";
    private static final String SERVER_PUB_KEY_PATH = "src/test/resources/sftp_server.pub";
    private static final String TEST_USERNAME = "test";
    private static final String TEST_PASSWORD = "1234";

    @Test
    public void passwordTest() throws SftpException, JSchException, IOException {
        JSch jsch = new JSch();
        Hashtable<String, String> config = new Hashtable<>();
        config.put("StrictHostKeyChecking", "no");
        JSch.setConfig(config);

        Session session = jsch.getSession(TEST_USERNAME, SERVER_HOST, SERVER_PORT);
        session.setPassword(TEST_PASSWORD);
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();

        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.cd("/folder2");
        System.out.println(sftpChannel.ls("/folder2"));

//        String testFileContents = "some file contents";
//        String uploadedFileName = "uploadFile.txt";
//        sftpChannel.put(new ByteArrayInputStream(testFileContents.getBytes()), uploadedFileName);
//
//        String downloadedFileName = "target/downloadFile.txt";
//        sftpChannel.get(uploadedFileName, downloadedFileName);
//        File downloadedFile = new File(downloadedFileName);
//        Assert.assertTrue(downloadedFile.exists());
//
//        String fileData = getFileContents(downloadedFile);
//        Assert.assertEquals(testFileContents, fileData);

        if (sftpChannel.isConnected())
            sftpChannel.exit();
        if (session.isConnected())
            session.disconnect();
    }

    @Test
    public void pubKeyTest() throws JSchException, IOException, SftpException {
        JSch jsch = new JSch();
        Hashtable<String, String> config = new Hashtable<>();
        config.put("StrictHostKeyChecking", "no");
        JSch.setConfig(config);

        String privKey = "src/test/resources/id_rsa";
        String pubKey = "src/test/resources/id_rsa.pub";

        jsch.addIdentity(privKey, pubKey, null);
        Session session = jsch.getSession("test", SERVER_HOST, SERVER_PORT);
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();

        ChannelSftp sftpChannel = (ChannelSftp) channel;

        final String testFileContents = "some file contents";

        sftpChannel.cd("folder1");
        String uploadedFileName = "pubUploadFile";
        sftpChannel.put(new ByteArrayInputStream(testFileContents.getBytes()), uploadedFileName);

        String downloadedFileName = "target/pubDownloadFile";
        sftpChannel.get(uploadedFileName, downloadedFileName);

        File downloadedFile = new File(downloadedFileName);
        Assert.assertTrue(downloadedFile.exists());

        String fileData = getFileContents(downloadedFile);
        Assert.assertEquals(testFileContents, fileData);

        if (sftpChannel.isConnected()) {
            sftpChannel.exit();
            System.out.println("Disconnected channel");
        }

        if (session.isConnected()) {
            session.disconnect();
            System.out.println("Disconnected session");
        }
    }

    private String getFileContents(File downloadedFile) throws IOException {
        StringBuffer fileData = new StringBuffer();
        try (BufferedReader reader = new BufferedReader(new FileReader(downloadedFile))) {
            char[] buf = new char[1024];
            for (int numRead = 0; (numRead = reader.read(buf)) != -1; buf = new char[1024]) {
                fileData.append(String.valueOf(buf, 0, numRead));
            }
        }
        return fileData.toString();
    }

    @After
    public void finish() throws Exception {
        if (server != null)
            server.stop();
    }

    private static class TestAccountInfoProvider implements AccountInfoProvider {

        @Override
        public List<String> getUserList() {
            return null;
        }

        @Override
        public String getPasswordByUsername(String username) {
            if(username.equals(TEST_USERNAME))
                return TEST_PASSWORD;
            return null;
        }

        @Override
        public String getUsernameByPublicKey(PublicKey key) {
            PublicKey knownKey = null;
            try {
                String pubKeyStr = "<SSH-RSA KEY>";
                knownKey = PublicKeyEntry.parsePublicKeyEntry(pubKeyStr).resolvePublicKey(PublicKeyEntryResolver.IGNORING);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            if(KeyUtils.compareKeys(key, knownKey)) {
                return "test";
            }
            return null;
        }
    }

    private static class TestPermissionInfoProvider implements PermissionInfoProvider {
        @Override
        public List<String> getPermissionByUsername(String username) {
            if(username.equals(TEST_USERNAME))
                return Arrays.asList("/folder1");
            return null;
        }
    }
}