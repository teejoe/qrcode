package com.google.zxing.client.android;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.Logging;
import com.google.zxing.NotFoundException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mtj on 2017/10/25.
 */

public class Utils {

    public static Bitmap binaryBitmap2Bitmap(BinaryBitmap binaryBitmap) throws NotFoundException {
        long start = System.currentTimeMillis();
        int width = binaryBitmap.getWidth();
        int height = binaryBitmap.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (binaryBitmap.getBlackMatrix().get(i, j)) {
                    bitmap.setPixel(i, j, Color.BLACK);
                } else {
                    bitmap.setPixel(i, j, Color.WHITE);
                }
            }
        }
        Logging.d("convert cost:" + (System.currentTimeMillis() - start));
        return bitmap;
    }

    public static void saveBitmap(Bitmap bitmap, String path) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
