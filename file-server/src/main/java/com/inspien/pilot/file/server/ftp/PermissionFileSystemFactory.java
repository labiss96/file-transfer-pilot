package com.inspien.pilot.file.server.ftp;

import com.inspien.pilot.file.server.PermissionInfoProvider;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;

import java.io.File;

public class PermissionFileSystemFactory implements FileSystemFactory {

    public PermissionFileSystemFactory(PermissionInfoProvider permissionInfoProvider) {
        this.permissionInfoProvider = permissionInfoProvider;
    }

    private PermissionInfoProvider permissionInfoProvider;
    private boolean createHome;
    private boolean caseInsensitive;

    public boolean isCreateHome() {
        return createHome;
    }

    public void setCreateHome(boolean createHome) {
        this.createHome = createHome;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    @Override
    public FileSystemView createFileSystemView(User user) throws FtpException {
        synchronized (user) {
            if (createHome) {
                String homeDirStr = user.getHomeDirectory();
                File homeDir = new File(homeDirStr);
                if (homeDir.isFile()) {
                    System.out.println("Not a directory :: " + homeDirStr);
                    throw new FtpException("Not a directory :: " + homeDirStr);
                }
                if ((!homeDir.exists()) && (!homeDir.mkdirs())) {
                    System.out.println("Cannot create user home :: " + homeDirStr);
                    throw new FtpException("Cannot create user home :: " + homeDirStr);
                }
            }
            return new PermissionFileSystemView(permissionInfoProvider, user, caseInsensitive);
        }
    }
}