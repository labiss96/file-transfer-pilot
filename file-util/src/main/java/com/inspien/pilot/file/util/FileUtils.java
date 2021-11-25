package com.inspien.pilot.file.util;

import java.io.*;

public class FileUtils {
    public static void writeBytesToFile(File f, byte[] b) throws IOException {
        FileOutputStream out = new FileOutputStream(f);
        try {
            out.write(b);
        } finally {
            out.close();
        }
    }

    public static byte[] readBytesFromFile(File f) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(f));
        byte[] buf = new byte[(int) f.length()];
        try {
            in.readFully(buf);
        } finally {
            in.close();
        }
        return buf;
    }
}
