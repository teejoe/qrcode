package com.m2x.testcore;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;
import android.widget.ImageView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by mtj on 2017/10/30.
 */

public class ImageDialog extends Dialog {

    private String mImageFilePath;


    @BindView(R.id.image)
    ImageView mImageView;

    public ImageDialog(@NonNull Context context, String filePath) {
        super(context);
        mImageFilePath = filePath;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_image);
        ButterKnife.bind(this);

        Picasso.with(getContext())
                .load(new File(mImageFilePath))
                .into(mImageView);
    }

    @OnClick(R.id.image)
    void onImageClicked() {
        Picasso.with(getContext())
                .load(new File(mImageFilePath))
                .into(mImageView);
    }

    @OnClick(R.id.hybrid)
    void onHybridClicked() {
        Bitmap origin = BitmapFactory.decodeFile(mImageFilePath);
        LuminanceSource source = TestWrapper.buildLuminanceImageFromBitmap(origin);
        if (source == null) {
            return;
        }

        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            Bitmap bitmap = TestWrapper.binaryBitmap2Bitmap(binaryBitmap);
            mImageView.setImageBitmap(bitmap);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.global)
    void onGlobalClicked() {
        Bitmap origin = BitmapFactory.decodeFile(mImageFilePath);
        LuminanceSource source = TestWrapper.buildLuminanceImageFromBitmap(origin);
        if (source == null) {
            return;
        }

        BinaryBitmap binaryBitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
        try {
            Bitmap bitmap = TestWrapper.binaryBitmap2Bitmap(binaryBitmap);
            mImageView.setImageBitmap(bitmap);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }
}
