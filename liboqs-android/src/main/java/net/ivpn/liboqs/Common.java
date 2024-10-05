package net.ivpn.liboqs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;

public class Common {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static void wipe(byte[] array) {
        Arrays.fill(array, (byte) 0);
    }

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isMac() {
        return OS.contains("mac");
    }

    public static boolean isLinux() {
        return OS.contains("nux");
    }

    public static void loadNativeLibrary() {
        // If the library is in the java library path, load it directly. (e.g., -Djava.library.path=src/main/resources)
        try {
            System.loadLibrary("oqs-jni");
        // Otherwise load the library from the liboqs-java.jar
        } catch (UnsatisfiedLinkError e) {
            String libName = "llliboqs-jni.so";
            if (Common.isLinux()) {
                libName = "liboqs-jni.so";
            } else if (Common.isMac()) {
                libName = "liboqs-jni.jnilib";
            } else if (Common.isWindows()) {
                libName = "oqs-jni.dll";
            }
            URL url = KEMs.class.getResource("/" + libName);
            File tmpDir;
            try {
                tmpDir = Files.createTempDirectory("oqs-native-lib").toFile();
                tmpDir.deleteOnExit();
                File nativeLibTmpFile = new File(tmpDir, libName);
                nativeLibTmpFile.deleteOnExit();
                InputStream in = url.openStream();
                Files.copy(in, nativeLibTmpFile.toPath());
                System.load(nativeLibTmpFile.getAbsolutePath());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public static <E, T extends Iterable<E>> void print_list(T list) {
        for (Object element : list){
            System.out.print(element);
            System.out.print(" ");
        }
        System.out.println();
    }

    public static String to_hex(byte[] bytes) {
        final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            int v = aByte & 0xFF;
            sb.append(HEX_ARRAY[v >>> 4]);
            sb.append(HEX_ARRAY[v & 0x0F]);
            sb.append(" ");
        }
        return sb.toString();
    }

    public static String chop_hex(byte[] bytes) {
        final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder();
        int num = 8;
        for (int i = 0; i < num; i++) {
            int v = bytes[i] & 0xFF;
            sb.append(HEX_ARRAY[v >>> 4]);
            sb.append(HEX_ARRAY[v & 0x0F]);
            sb.append(" ");
        }
        if (bytes.length > num*2) {
            sb.append("... ");
        }
        for (int i = bytes.length - num; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            sb.append(HEX_ARRAY[v >>> 4]);
            sb.append(HEX_ARRAY[v & 0x0F]);
            sb.append(" ");
        }
        return sb.toString();
    }

}