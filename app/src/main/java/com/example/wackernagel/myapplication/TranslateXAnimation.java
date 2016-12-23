package com.example.wackernagel.myapplication;

import android.view.animation.Animation;
import android.view.animation.Transformation;

public class TranslateXAnimation extends Animation {
    private float mFromXValue = 0.0f;
    private float mToXValue = 0.0f;

    private float mFromXDelta;
    private float mToXDelta;

    /**
     * Constructor to use when building a TranslateAnimation from code
     *
     * @param fromXValue Change in X coordinate to apply at the start of the
     *        animation. This value can either be an absolute number if fromXType
     *        is ABSOLUTE, or a percentage (where 1.0 is 100%) otherwise.
     * @param toXValue Change in X coordinate to apply at the end of the
     *        animation. This value can either be an absolute number if toXType
     *        is ABSOLUTE, or a percentage (where 1.0 is 100%) otherwise.
     */
    public TranslateXAnimation( float fromXValue, float toXValue) {
        mFromXValue = fromXValue;
        mToXValue = toXValue;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float dx = mFromXDelta;
        if (mFromXDelta != mToXDelta) {
            dx = mFromXDelta + ((mToXDelta - mFromXDelta) * interpolatedTime);
        }
        t.getMatrix().setTranslate(dx, 0);
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mFromXDelta = resolveSize(ABSOLUTE, mFromXValue, width, parentWidth);
        mToXDelta = resolveSize(ABSOLUTE, mToXValue, width, parentWidth);
    }
}
