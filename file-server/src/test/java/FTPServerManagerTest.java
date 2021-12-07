import com.inspien.pilot.file.server.AccountInfoProvider;
import com.inspien.pilot.file.server.FileTransferServerManager;
import com.inspien.pilot.file.server.PermissionInfoProvider;
import com.inspien.pilot.file.server.ServerConfig;
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
import java.util.Arrays;
import java.util.List;

public class FTPServerManagerTest {

    @Before
    public void setup() throws Exception {
        ServerConfig config = new ServerConfig("C:/file-transfer/ftp", "svc-1", 2221);
        AccountInfoProvider accountInfoProvider = new TestAccountInfoProvider();
        PermissionInfoProvider permissionInfoProvider = new TestPermissionInfoProvider();
        server = new FTPServerManager(config, accountInfoProvider, permissionInfoProvider);
        server.start();
    }
    private FileTransferServerManager server;

    @Test
    public void test() throws IOException {
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


    @After
    public void shutdown() throws Exception {
        if(server != null)
            server.stop();
    }


    private static class TestAccountInfoProvider implements AccountInfoProvider {
        @Override
        public List<String> getUserList() {
            return Arrays.asList("test");
        }

        @Override
        public String getPasswordByUsername(String username) {
            if(username.equals("test"))
                return "1234";
            return null;
        }

        @Override
        public PublicKey getPublicKeyByUsername(String username) {
            return null;
        }
    }

    private static class TestPermissionInfoProvider implements PermissionInfoProvider {
        @Override
        public List<String> getPermissions(String username) {
            return Arrays.asList("/folder-1");
        }
    }
}
