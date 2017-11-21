package com.m2x.testcore;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.DecodeState;
import com.google.zxing.DecodeState.BinarizerAlgorithm;
import com.google.zxing.DecodeState.FinderPatternAlgorithm;
import com.google.zxing.DecodeState.SpecifiedParams;
import com.google.zxing.Logging;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.AdjustedHybridBinarizer;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.detector.AlignmentPattern;
import com.google.zxing.qrcode.detector.FinderPattern;
import com.google.zxing.qrcode.detector.FinderPatternInfo;
import com.m2x.testcore.TestWrapper.DecodeResult;
import com.m2x.testcore.widget.DecodeImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.google.zxing.DecodeState.BinarizerAlgorithm.ADJUSTED_HYBRID;
import static com.google.zxing.DecodeState.BinarizerAlgorithm.GLOBAL_HISTOGRAM;
import static com.google.zxing.DecodeState.BinarizerAlgorithm.HYBRID;

/**
 * Created by mtj on 2017/10/30.
 */

public class ImageDialog extends Dialog {

    private String mImageFilePath;

    private BinarizerAlgorithm mBinarizer = GLOBAL_HISTOGRAM;

    private Map<DecodeHintType, Object> mDecodeHint;
    private DecodeState mDecodeState = new DecodeState();
    private ResultPointCallback mResultPointCallback;
    private FinderPatternAlgorithm mFinderPatternAlgorithm = FinderPatternAlgorithm.REGULAR;

    @BindView(R.id.image)
    DecodeImageView mImageView;

    @BindView(R.id.regular)
    RadioButton mRegularFinderView;

    @BindView(R.id.weak)
    RadioButton mWeakFinderView;

    @BindView(R.id.weak2)
    RadioButton mWeak2FinderView;

    @BindView(R.id.hybrid)
    RadioButton mHybridBinarizerView;

    @BindView(R.id.global_histogram)
    RadioButton mGlobalHistogramBinarizerView;

    @BindView(R.id.adjust_hybrid)
    RadioButton mAdjustedHybridBinarizerView;

    @BindView(R.id.sensitivity)
    SeekBar mSensitivityView;

    public ImageDialog(@NonNull Context context, String filePath) {
        super(context);
        mImageFilePath = filePath;

        mResultPointCallback = new ResultPointCallback() {
            @Override
            public void foundPossibleResultPoint(ResultPoint point) {
                if (point instanceof FinderPattern) {
                    mImageView.addFinderPattern((FinderPattern)point);
                } else if (point instanceof AlignmentPattern) {
                    mImageView.addAlignmentPattern((AlignmentPattern)point);
                }
            }

            @Override
            public void foundBestFinderPattern(FinderPatternInfo info) {
                mImageView.addBestFinderPattern(info);
            }
        };

        mDecodeHint = new EnumMap<>(DecodeHintType.class);
        mDecodeHint.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, mResultPointCallback);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_image);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);

        ButterKnife.bind(this);

        Picasso.with(getContext())
                .load(new File(mImageFilePath))
                .into(mImageView);
    }

    @OnClick(R.id.image)
    void onImageClicked() {
        mImageView.clearResultPoint();
        Picasso.with(getContext())
                .load(new File(mImageFilePath))
                .into(mImageView);
    }

    @OnClick(R.id.close)
    void onCloseClicked() {
        dismiss();
    }

    @OnClick(R.id.hybrid)
    void onHybridClicked() {
        mHybridBinarizerView.setChecked(true);
        mAdjustedHybridBinarizerView.setChecked(false);
        mGlobalHistogramBinarizerView.setChecked(false);

        mBinarizer = HYBRID;
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

    @OnClick(R.id.adjust_hybrid)
    void onAdjustHybridClicked() {
        mHybridBinarizerView.setChecked(false);
        mAdjustedHybridBinarizerView.setChecked(true);
        mGlobalHistogramBinarizerView.setChecked(false);

        mBinarizer = ADJUSTED_HYBRID;
        Bitmap origin = BitmapFactory.decodeFile(mImageFilePath);
        LuminanceSource source = TestWrapper.buildLuminanceImageFromBitmap(origin);
        if (source == null) {
            return;
        }

        float scale = mSensitivityView.getProgress() / 100.0f;
        BinaryBitmap binaryBitmap = new BinaryBitmap(new AdjustedHybridBinarizer(source, scale));
        try {
            Bitmap bitmap = TestWrapper.binaryBitmap2Bitmap(binaryBitmap);
            mImageView.setImageBitmap(bitmap);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.global_histogram)
    void onGlobalClicked() {
        mGlobalHistogramBinarizerView.setChecked(true);
        mAdjustedHybridBinarizerView.setChecked(false);
        mHybridBinarizerView.setChecked(false);

        mBinarizer = GLOBAL_HISTOGRAM;
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

    @OnClick(R.id.regular)
    void onRegularClicked() {
        mFinderPatternAlgorithm = FinderPatternAlgorithm.REGULAR;
        mRegularFinderView.setChecked(true);
        mWeakFinderView.setChecked(false);
        mWeak2FinderView.setChecked(false);
    }

    @OnClick(R.id.weak)
    void onWeakClicked() {
        mFinderPatternAlgorithm = FinderPatternAlgorithm.WEAK;
        mRegularFinderView.setChecked(false);
        mWeakFinderView.setChecked(true);
        mWeak2FinderView.setChecked(false);
    }

    @OnClick(R.id.weak2)
    void onWeak2Clicked() {
        mFinderPatternAlgorithm = FinderPatternAlgorithm.WEAK2;
        mRegularFinderView.setChecked(false);
        mWeakFinderView.setChecked(false);
        mWeak2FinderView.setChecked(true);
    }

    @OnClick(R.id.decode)
    void onDecodeClicked() {
        mImageView.clearResultPoint();

        mDecodeState.specifiedParams = new SpecifiedParams();
        mDecodeState.specifiedParams.finderPatternAlgorithm = mFinderPatternAlgorithm;
        mDecodeState.specifiedParams.finderPatternSensitivity = mSensitivityView.getProgress() / 100.0f;
        Logging.d("progress:" + mSensitivityView.getProgress());
        mDecodeState.previousFailureHint.binarizerAlgorithm = mBinarizer;
        mDecodeHint.put(DecodeHintType.DECODE_STATE, mDecodeState);
        Bitmap bitmap = BitmapFactory.decodeFile(mImageFilePath);
        DecodeResult result = TestWrapper.decodeBitmap(bitmap, mBinarizer, mDecodeHint);
        Toast.makeText(getContext(), "success:" + result.success + "\nmsg:" + result.msg,
                Toast.LENGTH_LONG).show();
    }
}
