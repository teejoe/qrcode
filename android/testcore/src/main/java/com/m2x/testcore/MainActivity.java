package com.m2x.testcore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.Logging;
import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String DIR_PATH = "/mnt/sdcard/qrcode/";

    private long mTotalTime;
    private int mTotalCount;
    private int mSuccessCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.test)
    void onTestClicked() {
        startActivity(new Intent(this, TestActivity.class));
    }

    @OnClick(R.id.start)
    void onStartClicked() {
        Toast.makeText(this, "begin test", Toast.LENGTH_SHORT).show();

        mTotalTime = 0;
        mSuccessCount = 0;
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                doTest();
                return null;
            }
        }.execute();
    }

    private void doTest() {
        File file = new File(DIR_PATH);
        if (file == null) {
            Logging.e("no such path:" + DIR_PATH);
            return;
        }

        for (File imageFile: file.listFiles()) {
            if (imageFile.isFile()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());
                if (bitmap != null) {
                    mTotalCount++;
                    Logging.d("begin decode:" + imageFile.getName());
                    long startTime = System.currentTimeMillis();
                    boolean success = decodeBitmap(bitmap);
                    long cost = System.currentTimeMillis() - startTime;
                    mTotalTime += cost;
                    if (success) {
                        mSuccessCount++;
                        Logging.d("success " + "cost:" + cost + "ms\n");
                    } else {
                        Logging.d("failed " + "cost:" + cost + "ms\n");
                    }
                }
            }
        }
        Logging.d("\ntotal count:" + mTotalCount);
        Logging.d("success count:" + mSuccessCount);
        Logging.d("total time:" + mTotalTime);
    }

    private boolean decodeBitmap(Bitmap bitmap) {
        try {
            LuminanceSource source = buildLuminanceImageFromBitmap(bitmap);
            if (source == null) {
                return false;
            }

            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            //BinaryBitmap binaryBitmap = new BinaryBitmap(new GlobalHistogramBinarizer(bitmap));
            QRCodeReader re = new QRCodeReader();
            Result rawResult = re.decode(binaryBitmap);
            if (rawResult != null) {
                Logging.d("result:" + rawResult.getText());
                return true;
            }
        } catch (ReaderException e) {
            //Logging.logStackTrace(e);
            return false;
        }
        return false;
    }

    public static LuminanceSource buildLuminanceImageFromBitmap(Bitmap bmp) {
        if (bmp == null) return null;
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height];

        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        return new RGBLuminanceSource(width, height, pixels);
    }
}
