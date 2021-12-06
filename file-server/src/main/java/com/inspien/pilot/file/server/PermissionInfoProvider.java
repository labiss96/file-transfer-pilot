package com.inspien.pilot.file.server;

import java.util.List;

public interface PermissionInfoProvider {
    List<String> getPermissions(String username);
}
