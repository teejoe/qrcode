package com.google.zxing;

/**
 * Created by mtj on 2017/11/14.
 */

public class DecodeState {
    public enum FinderPatternAlgorithm {
        REGULAR,
        WEAK,
        WEAK2
    }

    public static class FinderPatternHint {
        public boolean notEnough;           // found less than 3 finder patterns.
        public boolean tooMany;             // found more than MAX_CANDIDATES finder patterns.
        public float sensitivityIncrease; // [0, 1.0)  factor to increase finder pattern sensitive.

        public void clear() {
            notEnough = false;
            tooMany = false;
            sensitivityIncrease = 0.0f;
        }
    }

    public static class FailureHint {
        public boolean lowContrastImage;                // need to increase image contrast.
        public boolean finderPatternIncredible;         // may found wrong finder patterns.
        public boolean dimensionIncredible;             // dimension may be wrong.

        public FinderPatternHint finderPatternFinderHint = new FinderPatternHint();
        public FinderPatternHint weakFinderPatternFinderHint = new FinderPatternHint();
        public FinderPatternHint weakFinderPatternFinder2Hint = new FinderPatternHint();
        public FinderPatternAlgorithm finderPatternAlgorithm;

        public void clear() {
            lowContrastImage = false;
            finderPatternIncredible = false;
            dimensionIncredible = false;
            finderPatternFinderHint.clear();
            weakFinderPatternFinderHint.clear();
            weakFinderPatternFinder2Hint.clear();
            finderPatternAlgorithm = FinderPatternAlgorithm.REGULAR;
        }
    }

    public int currentRound;
    public FailureHint previousFailureHint = new FailureHint();

    public void reset() {
        currentRound = 0;
        previousFailureHint.clear();
    }
}
