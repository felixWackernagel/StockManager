package com.example.wackernagel.myapplication.animations;

import android.view.animation.Animation;
import android.view.animation.Transformation;

public class TranslateYAnimation extends Animation {
    private float mFromYValue = 0.0f;
    private float mToYValue = 0.0f;

    private float mFromYDelta;
    private float mToYDelta;

    /**
     * Constructor to use when building a TranslateAnimation from code
     *
     * @param fromYValue Change in Y coordinate to apply at the start of the
     *        animation. This value can either be an absolute number if fromYType
     *        is ABSOLUTE, or a percentage (where 1.0 is 100%) otherwise.
     * @param toYValue Change in Y coordinate to apply at the end of the
     *        animation. This value can either be an absolute number if toYType
     *        is ABSOLUTE, or a percentage (where 1.0 is 100%) otherwise.
     */
    public TranslateYAnimation( float fromYValue, float toYValue) {
        mFromYValue = fromYValue;
        mToYValue = toYValue;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float dy = mFromYDelta;
        if (mFromYDelta != mToYDelta) {
            dy = mFromYDelta + ((mToYDelta - mFromYDelta) * interpolatedTime);
        }
        t.getMatrix().setTranslate(0, dy);
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mFromYDelta = resolveSize(ABSOLUTE, mFromYValue, height, parentHeight);
        mToYDelta = resolveSize(ABSOLUTE, mToYValue, height, parentHeight);
    }
}
