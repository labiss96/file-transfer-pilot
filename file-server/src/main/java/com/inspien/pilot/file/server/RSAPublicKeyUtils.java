package com.inspien.pilot.file.server;

import org.apache.mina.util.Base64;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

public class RSAPublicKeyUtils {

    public static PublicKey decodePublicKey(byte[] pubKey) {
        try {
            String[] parts = new String(pubKey).split(" ");
            for (String part : parts) {
                if (part.startsWith("AAAA")) {
                    byte[] decodeBuffer = Base64.decodeBase64(part.getBytes());
                    ByteBuffer bb = ByteBuffer.wrap(decodeBuffer);
                    int len = bb.getInt();
                    byte[] type = new byte[len];
                    bb.get(type);
                    if ("ssh-rsa".equals(new String(type))) {
                        BigInteger exponent = decodeBigInt(bb);
                        BigInteger modulus = decodeBigInt(bb);
                        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
                        return KeyFactory.getInstance("RSA").generatePublic(spec);
                    } else {
                        throw new IllegalArgumentException("Only support RSA");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static BigInteger decodeBigInt(ByteBuffer bb) {
        int len = bb.getInt();
        byte[] bytes = new byte[len];
        bb.get(bytes);
        return new BigInteger(bytes);
    }
}
