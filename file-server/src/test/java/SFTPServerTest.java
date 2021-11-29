import com.inspien.pilot.file.server.SFTPServer;
import com.inspien.pilot.file.server.ServerConfig;
import com.inspien.pilot.file.util.FileUtils;
import com.jcraft.jsch.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;

public class SFTPServerTest {
    @Before
    public void setup() throws Exception {
        ServerConfig config = new ServerConfig("C:/sftp", "sftp-1", SERVER_PORT,
                "src/test/resources/sftp_server",
                "src/test/resources/sftp_server.pub", "src/test/resources/authorized_keys"
        );
        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put(TEST_USERNAME, TEST_PASSWORD);

        Map<String, List<String>> permissionMap = new HashMap<>();
        permissionMap.put(TEST_USERNAME, Arrays.asList("/folder1"));

        server = new SFTPServer(config);
        server.setAccountsInfo(passwordMap, permissionMap);
        server.start();
    }

    private SFTPServer server;
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 2222;
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

        final String testFileContents = "some file contents";

        sftpChannel.cd("folder1");
        System.out.println(sftpChannel.pwd());
        String uploadedFileName = "uploadFile";
        sftpChannel.put(new ByteArrayInputStream(testFileContents.getBytes()), uploadedFileName);

        String downloadedFileName = "target/downloadFile";
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

    @Test
    public void pubKeyTest() throws JSchException, IOException, SftpException {
        JSch jsch = new JSch();
        Hashtable<String, String> config = new Hashtable<>();
        config.put("StrictHostKeyChecking", "no");
        JSch.setConfig(config);

        File privKey = new File("src/test/resources/id_rsa");
        File pubKey = new File("src/test/resources/id_rsa.pub");

        byte[] privKeyBytes = FileUtils.readBytesFromFile(privKey);
        byte[] pubKeyBytes = FileUtils.readBytesFromFile(pubKey);

        jsch.addIdentity(TEST_USERNAME, privKeyBytes, pubKeyBytes, null);
        Session session = jsch.getSession(TEST_USERNAME, SERVER_HOST, SERVER_PORT);
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
    public void finish() throws IOException {
        if (server != null)
            server.stop();
    }
}