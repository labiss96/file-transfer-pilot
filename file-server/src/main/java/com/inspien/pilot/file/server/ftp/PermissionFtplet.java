package com.inspien.pilot.file.server.ftp;

import com.inspien.pilot.file.server.PermissionInfoProvider;
import org.apache.ftpserver.ftplet.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PermissionFtplet extends DefaultFtplet {
    public PermissionFtplet(PermissionInfoProvider permissionInfoProvider) {
        this.permissionInfoProvider = permissionInfoProvider;
    }

    private PermissionInfoProvider permissionInfoProvider;
    private static final String PERMISSION_DENIED_MSG = "Permission Denied";

    @Override
    public FtpletResult onUploadStart(FtpSession session, FtpRequest request)
            throws FtpException, IOException {
        if (checkUserPermission(session, request.getArgument()))
            return FtpletResult.DEFAULT;
        session.write(new DefaultFtpReply(FtpReply.REPLY_450_REQUESTED_FILE_ACTION_NOT_TAKEN, PERMISSION_DENIED_MSG));
        return FtpletResult.SKIP;
    }

    @Override
    public FtpletResult onDownloadStart(FtpSession session, FtpRequest request)
            throws FtpException, IOException {
        if (checkUserPermission(session, request.getArgument()))
            return FtpletResult.DEFAULT;
        session.write(new DefaultFtpReply(FtpReply.REPLY_450_REQUESTED_FILE_ACTION_NOT_TAKEN, PERMISSION_DENIED_MSG));
        return FtpletResult.SKIP;
    }

    @Override
    public FtpletResult onDeleteStart(FtpSession session, FtpRequest request)
            throws FtpException, IOException {
        if (checkUserPermission(session, request.getArgument()))
            return FtpletResult.DEFAULT;
        session.write(new DefaultFtpReply(FtpReply.REPLY_450_REQUESTED_FILE_ACTION_NOT_TAKEN, PERMISSION_DENIED_MSG));
        return FtpletResult.SKIP;
    }

    @Override
    public FtpletResult onRmdirStart(FtpSession session, FtpRequest request)
            throws FtpException, IOException {
        if (checkUserPermission(session, request.getArgument()))
            return FtpletResult.DEFAULT;
        session.write(new DefaultFtpReply(FtpReply.REPLY_450_REQUESTED_FILE_ACTION_NOT_TAKEN, PERMISSION_DENIED_MSG));
        return FtpletResult.SKIP;
    }



    private boolean checkUserPermission(FtpSession session, String pathStr) {
        String username = session.getUser().getName();
        Path path = Paths.get(this.prependSlash(pathStr));
        List<String> userPermissions = permissionInfoProvider.getPermissionByUsername(username);
        boolean isAllowed = false;
        for (String folderPath : userPermissions) {
            if (path.startsWith(prependSlash(folderPath))) {
                isAllowed = true;
                break;
            }
        }
        return isAllowed;
    }

    private String prependSlash(String path) {
        if (path.charAt(0) != '/') {
            return '/' + path;
        } else {
            return path;
        }
    }
}
