package com.inspien.pilot.file.client;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

public class Activator implements BundleActivator {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 2222;
    private static final String TEST_USERNAME = "test";
    private static final String TEST_PASSWORD = "1234";

    public void start(BundleContext context) throws Exception {
        JSch jsch = new JSch();
        Hashtable<String, String> config = new Hashtable<>();
        config.put("StrictHostKeyChecking", "no");
        JSch.setConfig(config);

        session = jsch.getSession(TEST_USERNAME, SERVER_HOST, SERVER_PORT);
        session.setPassword(TEST_PASSWORD);
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();

        sftpChannel = (ChannelSftp) channel;

        System.out.println("file client started ...");
    }

    private ChannelSftp sftpChannel;
    private Session session;

    public void stop(BundleContext context) throws Exception {
        if (sftpChannel.isConnected())
            sftpChannel.exit();
        if (session.isConnected())
            session.disconnect();
        System.out.println("file client stopped ...");
    }
}
