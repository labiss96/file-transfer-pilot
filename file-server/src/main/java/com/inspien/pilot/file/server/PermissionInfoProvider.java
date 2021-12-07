package com.inspien.pilot.file.server;

import java.util.List;
import java.util.Map;

public interface PermissionInfoProvider {
    List<String> getPermissionByUsername(String username);
    void setPermissionMap(Map<String, List<String>> permissionMap);
}
