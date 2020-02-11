/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

public class NativeUtils {

    public static native String[] getIfconfig() throws IllegalArgumentException;

    static native void jniclose(int fdint);

    public static String getNativeAPI() {
        return getJNIAPI();
    }

    private static native String getJNIAPI();

    static {
        System.loadLibrary("opvpnutil");
    }
}
