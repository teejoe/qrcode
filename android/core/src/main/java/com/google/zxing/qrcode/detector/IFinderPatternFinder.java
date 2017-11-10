package com.google.zxing.qrcode.detector;

import com.google.zxing.DecodeHintType;
import com.google.zxing.NotFoundException;

import java.util.Map;

/**
 * Created by mtj on 2017/11/9.
 */

public interface IFinderPatternFinder {
    FinderPatternInfo find(Map<DecodeHintType, ?> hints) throws NotFoundException;
}
