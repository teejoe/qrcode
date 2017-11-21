package com.m2x.testcore;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Matrix;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ContrastedLuminanceSource;
import com.google.zxing.DecodeHintType;
import com.google.zxing.DecodeState;
import com.google.zxing.DecodeState.BinarizerAlgorithm;
import com.google.zxing.DecodeState.FinderPatternAlgorithm;
import com.google.zxing.DecodeState.SpecifiedParams;
import com.google.zxing.DownscaledLuminanceSource;
import com.google.zxing.Logging;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.AdjustedHybridBinarizer;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.EnumMap;
import java.util.Map;

import static com.google.zxing.DecodeState.BinarizerAlgorithm.ADJUSTED_HYBRID;
import static com.google.zxing.DecodeState.BinarizerAlgorithm.GLOBAL_HISTOGRAM;
import static com.google.zxing.DecodeState.BinarizerAlgorithm.HYBRID;

/**
 * Created by mtj on 2017/10/30.
 */

public class TestWrapper {

    public static class DecodeResult {
        public DecodeResult(boolean success, String msg) {
            this.success = success;
            this.msg = msg;
        }

        public boolean success;
        public String msg;
    }

    public static LuminanceSource buildLuminanceImageFromBitmap(Bitmap bmp) {
        if (bmp == null) return null;
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height];

        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        return new RGBLuminanceSource(width, height, pixels);
    }

    public static DecodeResult decodeBitmap(Bitmap bitmap,
                                            BinarizerAlgorithm binarizer,
                                            Map<DecodeHintType, Object> hints) {
        try {
            //Bitmap bmp = getSizeLimitBitmap(bitmap, 200, 200);
            LuminanceSource source = buildLuminanceImageFromBitmap(bitmap);
            if (source == null) {
                return new DecodeResult(false, "build luminance failed");
            }

            QRCodeReader re = new QRCodeReader();
            Result rawResult = null;

            BinaryBitmap binaryBitmap = null;
            if (binarizer == HYBRID) {
                binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            } else if (binarizer == GLOBAL_HISTOGRAM) {
                binaryBitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
            } else if (binarizer == ADJUSTED_HYBRID) {
                ReaderException lastError = null;
                for (float i = 1.0f; i < 1.2f; i += 0.01f) {
                    binaryBitmap = new BinaryBitmap(new AdjustedHybridBinarizer(source, i));
                    try {
                        rawResult = re.decode(binaryBitmap, hints);
                        if (rawResult != null) {
                            return new DecodeResult(true, rawResult.getText());
                        }
                    } catch (ReaderException e) {
                        lastError = e;
                    }
                }
                if (lastError.getStackTrace().length > 0) {
                    return new DecodeResult(false, lastError.getStackTrace()[0].toString());
                } else {
                    return new DecodeResult(false, lastError.getClass().getName());
                }
            }

            rawResult = re.decode(binaryBitmap, hints);
            if (rawResult != null) {
                Logging.d("result:" + rawResult.getText());
                return new DecodeResult(true, rawResult.getText());
            }
        } catch (ReaderException e) {
            Logging.logStackTrace(e);
            if (e.getStackTrace().length > 0) {
                return new DecodeResult(false, e.getStackTrace()[0].toString());
            } else {
                return new DecodeResult(false, e.getClass().getName());
            }
        }
        return new DecodeResult(false, "unknown");
    }

    public static DecodeResult decodeBitmap(Bitmap bitmap, int maxDecodeRound) {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        DecodeState state = new DecodeState();
        hints.put(DecodeHintType.DECODE_STATE, state);

        Bitmap bmp = getSizeLimitBitmap(bitmap, 1024, 1024);

        for (int i = 0; i < maxDecodeRound; i++) {
            try {
                Result rawResult = decodeWithState(bmp, hints);
                if (rawResult != null) {
                    Logging.d("result:" + rawResult.getText());
                    return new DecodeResult(true, rawResult.getText());
                }
            } catch (ReaderException e) {
                // ignore.
            }
        }

        return new DecodeResult(false, "unknown");
    }

    private static Result decodeWithState(Bitmap bitmap, Map<DecodeHintType, Object> hints)
        throws ReaderException {

        DecodeState decodeState = hints == null ? null : (DecodeState) hints.get(DecodeHintType.DECODE_STATE);
        decodeState.currentRound++;

        LuminanceSource source = buildLuminanceImageFromBitmap(bitmap);
        if (source == null) {
            return null;
        }

        if ((System.currentTimeMillis() & 0x03) == 0) {
            // randomly increase contrast.
            source = new ContrastedLuminanceSource(source);
        }

        if ((System.currentTimeMillis() & 0x07) <= 2) {   // mod 8  (probability 3/8)
            // randomly downscale
            source = new DownscaledLuminanceSource(source);
            decodeState.scaleFactor = 0.5f;
            if ((System.currentTimeMillis() & 0x01) == 0) {     // mod 2
                source = new DownscaledLuminanceSource(source);
                decodeState.scaleFactor = 0.25f;
                if ((System.currentTimeMillis() & 0x01) == 0) {     // mod 2
                    source = new DownscaledLuminanceSource(source);
                    decodeState.scaleFactor = 0.125f;
                    if ((System.currentTimeMillis() & 0x01) == 0) {
                        source = new DownscaledLuminanceSource(source);
                        decodeState.scaleFactor = 0.0625f;
                    }
                }
            }
        } else {
            decodeState.scaleFactor = 1.0f;
        }

        BinaryBitmap binaryBitmap = null;
        if (decodeState.currentRound % 3 == 0) {
            binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        } else if (decodeState.currentRound % 3 == 1) {
            binaryBitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
        } else {
            binaryBitmap = new BinaryBitmap(new AdjustedHybridBinarizer(source, 0.9f
                    + decodeState.currentRound * 0.01f));
        }
        return new QRCodeReader().decode(binaryBitmap, hints);
    }

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


    public static Bitmap getSizeLimitBitmap(Bitmap bm, int maxWidth, int maxHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        if (width < maxWidth && height < maxHeight) {
            return bm;
        }

        int newWidth;
        int newHeight;
        if ((width / (float)maxWidth) > (height / (float)maxHeight)) {
            newWidth = maxWidth;
            newHeight = (int) ((maxWidth / (float)width) * height);
        } else {
            newHeight = maxHeight;
            newWidth = (int) ((maxHeight / (float) height) * width);
        }

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }
}
