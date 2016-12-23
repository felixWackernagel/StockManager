package com.example.wackernagel.myapplication;

import android.content.Context;

import de.wackernagel.android.sidekick.utils.PreferenceUtils;

class ProjectPreferences {

    static final String PREF_SORT_ORDER = "sortOrder";

    static int getSortOrderPosition(Context context ) {
        return PreferenceUtils.getInt( context, PREF_SORT_ORDER, 0 );
    }

    static void setSortOrderPosition(Context context, int position ) {
        PreferenceUtils.setInt( context, PREF_SORT_ORDER, position );
    }

}
