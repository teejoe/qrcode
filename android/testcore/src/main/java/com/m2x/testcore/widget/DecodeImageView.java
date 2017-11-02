package com.m2x.testcore.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.google.zxing.ResultPoint;
import com.google.zxing.qrcode.detector.AlignmentPattern;
import com.google.zxing.qrcode.detector.FinderPattern;

import java.util.ArrayList;

/**
 * Created by mtj on 2017/10/31.
 */

public class DecodeImageView extends android.support.v7.widget.AppCompatImageView {
    private ArrayList<ResultPoint> mFinderPatterns = new ArrayList<>();
    private ArrayList<ResultPoint> mAlignmentPatterns = new ArrayList<>();

    private Paint mFinderPatternPaint;
    private Paint mAlignmentPatternPaint;

    public DecodeImageView(Context context) {
        super(context);
        init();
    }

    public DecodeImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DecodeImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mFinderPatternPaint = new Paint();
        mFinderPatternPaint.setColor(Color.GREEN);
        mFinderPatternPaint.setStrokeWidth(5.0f);

        mAlignmentPatternPaint = new Paint();
        mAlignmentPatternPaint.setColor(Color.RED);
        mAlignmentPatternPaint.setStrokeWidth(3.0f);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mFinderPatterns.size() > 0 || mAlignmentPatterns.size() > 0) {
            if (((getDrawingTime() / 500) & 0x1) == 0) {   // mod 2
                for (ResultPoint point : mFinderPatterns) {
                    canvas.drawCircle(point.getX(), point.getY(), 5.0f, mFinderPatternPaint);
                }
                for (ResultPoint point: mAlignmentPatterns) {
                    canvas.drawCircle(point.getX(), point.getY(), 4.0f, mAlignmentPatternPaint);
                }
            }
            invalidate();
        }
    }

    private ResultPoint translatePoint(ResultPoint point) {
        float[] src = {point.getX(), point.getY()};
        float[] dst = new float[2];

        Matrix matrix = getImageMatrix();
        matrix.mapPoints(dst, src);

        return new ResultPoint(dst[0], dst[1]);
    }

    public void addFinderPattern(FinderPattern pattern) {
        mFinderPatterns.add(translatePoint(pattern));
        invalidate();
    }

    public void addAlignmentPattern(AlignmentPattern pattern) {
        mAlignmentPatterns.add(translatePoint(pattern));
        invalidate();
    }

    public void clearResultPoint() {
        mFinderPatterns.clear();
        mAlignmentPatterns.clear();
        invalidate();
    }
}
