package com.google.zxing;

/**
 * Created by mtj on 2017/12/1.
 */

public class NativeLib {
    static {
        System.loadLibrary("qrcodeplus");
    }

    public static native byte[] downscaleByHalf(byte[] luminance, int width, int height);

    public static native void increaseContrast(byte[] luminance, int width, int height);
}
