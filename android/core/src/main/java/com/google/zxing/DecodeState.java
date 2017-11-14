package com.google.zxing;

/**
 * Created by mtj on 2017/11/14.
 */

public class DecodeState {
    public enum FinderPatternAlgorithm {
        REGULAR,
        YET_ANOTHER
    }

    public static class PreviousFailureHint {
        public boolean lowContrastImage;
        public boolean finderPatternNotEnough;
        public boolean finderPatternTooMany;
        public boolean finderPatternInCredible;
        public boolean moduleSizeIncredible;
        public boolean dimensionIncredible;
        public FinderPatternAlgorithm finderPatternAlgorithm;

        public void clear() {
            lowContrastImage = false;
            finderPatternInCredible = false;
            finderPatternNotEnough = false;
            finderPatternTooMany = false;
            moduleSizeIncredible = false;
            dimensionIncredible = false;
            finderPatternAlgorithm = FinderPatternAlgorithm.REGULAR;
        }
    }

    public int currentRound;
    public PreviousFailureHint previousFailureHint = new PreviousFailureHint();

    public void reset() {
        currentRound = 0;
        previousFailureHint.clear();
    }
}
