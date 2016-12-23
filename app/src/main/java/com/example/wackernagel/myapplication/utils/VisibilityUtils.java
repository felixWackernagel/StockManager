package com.example.wackernagel.myapplication.utils;

import android.support.annotation.Nullable;
import android.view.View;

public class VisibilityUtils {

    private VisibilityUtils() {
    }

    public static void setVisible(@Nullable  final View v, final boolean isVisible ) {
        if( v != null ) {
            if( isVisible ) {
                if( v.getVisibility() != View.VISIBLE ) {
                    v.setVisibility(View.VISIBLE);
                }
            } else {
                if( v.getVisibility() == View.VISIBLE ) {
                    v.setVisibility(View.GONE);
                }
            }
        }
    }
}
