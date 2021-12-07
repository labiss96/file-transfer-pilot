package com.inspien.pilot.file.server;

import java.security.PublicKey;
import java.util.List;

public interface AccountInfoProvider {
    List<String> getUserList();
    String getPasswordByUsername(String username);
    String getUsernameByPublicKey(PublicKey key);
}
