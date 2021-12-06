package com.inspien.pilot.file.server.ftp;

import com.inspien.pilot.file.server.PermissionInfoProvider;
import org.apache.ftpserver.filesystem.nativefs.impl.NativeFileSystemView;
import org.apache.ftpserver.filesystem.nativefs.impl.NativeFtpFile;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PermissionFileSystemView extends NativeFileSystemView {

    private String rootDir;
    private String currDir;

    private final User user;

    private final boolean caseInsensitive;
    private PermissionInfoProvider provider;

    public PermissionFileSystemView(PermissionInfoProvider provider, User user, boolean caseInsensitive) throws FtpException {
        super(user, caseInsensitive);
        this.caseInsensitive = caseInsensitive;
        this.provider = provider;

        String rootDir = user.getHomeDirectory();
        rootDir = normalizeSeparateChar(rootDir);
        rootDir = appendSlash(rootDir);

        this.rootDir = rootDir;
        this.user = user;

        currDir = "/";
    }

    @Override
    public FtpFile getFile(String file) {
        if(checkUserPermission(file)) {
            String physicalName = getPhysicalName(rootDir, currDir, file, caseInsensitive);
            File fileObj = new File(physicalName);

            String userFileName = physicalName.substring(rootDir.length() - 1);
            return new PermissionFtpFile(userFileName, fileObj, user);
        } else {
            System.out.println("permission denied");
            return null;
        }
    }


    private String appendSlash(String path) {
        if (path.charAt(path.length() - 1) != '/') {
            return path + '/';
        } else {
            return path;
        }
    }

    private String prependSlash(String path) {
        if (path.charAt(0) != '/') {
            return '/' + path;
        } else {
            return path;
        }
    }

    private String normalizeSeparateChar(final String pathName) {
        String normalizedPathName = pathName.replace(File.separatorChar, '/');
        normalizedPathName = normalizedPathName.replace('\\', '/');
        return normalizedPathName;
    }

    private boolean checkUserPermission(String pathStr) {
        Path path = Paths.get(this.prependSlash(pathStr));
        List<String> userPermissions = provider.getPermissions(user.getName());
        boolean isAllowed = false;
        for(String folderPath: userPermissions) {
            if(path.startsWith(prependSlash(folderPath))) {
                isAllowed = true;
                break;
            }
        }
        return isAllowed;
    }

    private static class PermissionFtpFile extends NativeFtpFile {
        protected PermissionFtpFile(String fileName, File file, User user) {
            super(fileName, file, user);
        }
    }
}
