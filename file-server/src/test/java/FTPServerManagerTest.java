import com.inspien.pilot.file.server.AccountInfoProvider;
import com.inspien.pilot.file.server.FileTransferServerManager;
import com.inspien.pilot.file.server.PermissionInfoProvider;
import com.inspien.pilot.file.server.ftp.ServerConfig;
import com.inspien.pilot.file.server.ftp.FTPServerManager;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;
import java.util.*;

public class FTPServerManagerTest {

    @Before
    public void setup() throws Exception {
        ServerConfig config = new ServerConfig("C:/file-transfer/ftp", "svc-1", 2221);

        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("test", "1234");
        accountInfoProvider = new TestAccountInfoProvider(passwordMap);

        Map<String, List<String>> permissionMap = new HashMap<>();
        permissionMap.put("test", Arrays.asList("/folder-1"));
        permissionInfoProvider = new TestPermissionInfoProvider(permissionMap);

        server = new FTPServerManager(config, accountInfoProvider, permissionInfoProvider);
        server.start();
    }
    private FileTransferServerManager server;
    private TestPermissionInfoProvider permissionInfoProvider;
    private TestAccountInfoProvider accountInfoProvider;

    @Test
    public void ConnectTest() throws IOException {
        FTPClient ftpClient;
        ftpClient= new FTPClient();
        ftpClient.connect("localhost", 2221);

        int reply = ftpClient.getReplyCode();
        if(!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            System.out.println("FTP Server refused connection");
        } else {
            ftpClient.login("test", "1234");

            File putFile = new File("C:/file-transfer/put_test.txt");
            try(InputStream inputStream = new FileInputStream(putFile);) {
                boolean result = ftpClient.storeFile("folder-1/test.txt", inputStream);
                System.out.println("ftp store file ==> " + result);
                Assert.assertTrue(result);
            }
            ftpClient.logout();
        }
        if(ftpClient != null && ftpClient.isConnected())
            ftpClient.disconnect();
    }

    @Test
    public void updatePermissionTest() throws Exception {
        FTPClient ftpClient;
        ftpClient= new FTPClient();
        ftpClient.connect("localhost", 2221);

        int reply = ftpClient.getReplyCode();
        if(!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            System.out.println("FTP Server refused connection");
        } else {
            ftpClient.login("test", "1234");

            File putFile = new File("C:/file-transfer/put_test.txt");
            try(InputStream inputStream = new FileInputStream(putFile);) {
                boolean result = ftpClient.storeFile("folder-2/test.txt", inputStream);
                System.out.println("ftp store file ==> " + result);
                Assert.assertFalse(result);
            }

            Map<String, List<String>> permissionMap = new HashMap<>();
            permissionMap.put("test", Arrays.asList("/folder-1", "/folder-2"));
            permissionInfoProvider.setPermissionMap(permissionMap);

            try(InputStream inputStream = new FileInputStream(putFile);) {
                boolean result = ftpClient.storeFile("folder-2/test.txt", inputStream);
                System.out.println("ftp store file ==> " + result);
                Assert.assertTrue(result);
            }
            ftpClient.logout();
        }
        if(ftpClient != null && ftpClient.isConnected())
            ftpClient.disconnect();
    }

    @Test
    public void updateAccountTest() throws Exception {
        FTPClient ftpClient;
        ftpClient= new FTPClient();
        ftpClient.connect("localhost", 2221);

        int reply = ftpClient.getReplyCode();
        if(!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            System.out.println("FTP Server refused connection");
        } else {
            ftpClient.login("test", "1234");
            ftpClient.logout();
        }
        if(ftpClient != null && ftpClient.isConnected())
            ftpClient.disconnect();


        Map<String, String> passwordMap = new HashMap<>();
        passwordMap.put("test2", "12345");
        accountInfoProvider.setPasswordMap(passwordMap);

        ftpClient= new FTPClient();
        ftpClient.connect("localhost", 2221);

        reply = ftpClient.getReplyCode();
        if(!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            System.out.println("FTP Server refused connection");
        } else {
            ftpClient.login("test2","12345");
            ftpClient.logout();
        }
        if(ftpClient != null && ftpClient.isConnected())
            ftpClient.disconnect();
    }


    @After
    public void shutdown() throws Exception {
        if(server != null)
            server.stop();
    }


    private static class TestAccountInfoProvider implements AccountInfoProvider {
        private Map<String, String> passwordMap;

        public TestAccountInfoProvider(Map<String, String> passwordMap) {
            this.passwordMap = passwordMap;
        }

        @Override
        public List<String> getUserList() {
            return new ArrayList<>(passwordMap.keySet());
        }

        @Override
        public String getPasswordByUsername(String username) {
            return passwordMap.get(username);
        }

        @Override
        public String getUsernameByPublicKey(PublicKey key) {
            return "test";
        }

        public void setPasswordMap(Map<String, String> passwordMap) {
            this.passwordMap = passwordMap;
        }
    }

    private static class TestPermissionInfoProvider implements PermissionInfoProvider {
        private Map<String, List<String>> permissionMap;

        public TestPermissionInfoProvider(Map<String, List<String>> permissionMap) {
            this.permissionMap = permissionMap;
        }

        @Override
        public List<String> getPermissionByUsername(String username) {
            return permissionMap.get(username);
        }

        public void setPermissionMap(Map<String, List<String>> permissionMap) {
            this.permissionMap = permissionMap;
        }
    }
}
