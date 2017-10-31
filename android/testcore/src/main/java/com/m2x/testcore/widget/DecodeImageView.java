package com.m2x.testcore.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.google.zxing.ResultPoint;

import java.util.ArrayList;

/**
 * Created by mtj on 2017/10/31.
 */

public class DecodeImageView extends android.support.v7.widget.AppCompatImageView {
    private ArrayList<ResultPoint> mResultPoints = new ArrayList<>();
    private Paint mResultPointPaint;

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
        mResultPointPaint = new Paint();
        mResultPointPaint.setColor(Color.GREEN);
        mResultPointPaint.setStrokeWidth(5.0f);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mResultPoints.size() > 0) {
            if (((getDrawingTime() / 500) & 0x1) == 0) {   // mod 2
                for (ResultPoint point : mResultPoints) {
                    canvas.drawCircle(point.getX(), point.getY(), 5.0f, mResultPointPaint);
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

    public void addResultPoint(ResultPoint point) {
        mResultPoints.add(translatePoint(point));
        invalidate();
    }

    public void clearResultPoint() {
        mResultPoints.clear();
        invalidate();
    }
}
