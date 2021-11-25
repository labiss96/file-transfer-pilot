package com.inspien.pilot.file.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class TicketKeyPair {

    private byte[] privKey;
    private byte[] pubKey;

    public byte[] getPrivKey() {
        return privKey;
    }

    public byte[] getPubKey() {
        return pubKey;
    }

    public static TicketKeyPair generate() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.genKeyPair();
        TicketKeyPair ticketKeyPair = new TicketKeyPair();
        ticketKeyPair.privKey = keyPair.getPrivate().getEncoded();
        ticketKeyPair.pubKey = keyPair.getPublic().getEncoded();
        return ticketKeyPair;
    }
}
