package com.inspien.pilot.file.server;

import java.security.PublicKey;
import java.util.List;
import java.util.Map;

public interface AccountInfoProvider {
    List<String> getUserList();
    String getPasswordByUsername(String username);
    PublicKey getPublicKeyByUsername(String username);
    void setPasswordMap(Map<String, String> passwordMap);
    void setPublicKeyMap(Map<String, PublicKey> publicKeyMap);
}
