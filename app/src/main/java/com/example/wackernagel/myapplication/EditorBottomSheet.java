package com.example.wackernagel.myapplication;

import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.database.Cursor;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.wackernagel.myapplication.db.CategoryContract;
import com.example.wackernagel.myapplication.db.CategoryModel;
import com.example.wackernagel.myapplication.utils.VisibilityUtils;

public abstract class EditorBottomSheet extends BottomSheetDialogFragment {

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        final ViewGroup sheetLayout = ( ViewGroup ) LayoutInflater.from( getContext() ).inflate( getEditorLayoutResource(), null, false );
        dialog.setContentView(sheetLayout);

        final CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) ((View ) sheetLayout.getParent()).getLayoutParams();
        layoutParams.height = CoordinatorLayout.LayoutParams.MATCH_PARENT;

        final CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if( behavior != null && behavior instanceof BottomSheetBehavior ) {
                    int height = 0;
                    height += sheetLayout.getPaddingTop() + sheetLayout.getPaddingBottom();
                    for( int i = 0; i < sheetLayout.getChildCount(); i++ ) {
                        height += getOuterHeight( sheetLayout.getChildAt( i ) );
                    }
                    ((BottomSheetBehavior) behavior).setPeekHeight( height );
                }
            }

            private int getOuterHeight( View v ) {
                int height = 0;
                if( v == null ) {
                    return height;
                }

                if( v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams layout = ( ViewGroup.MarginLayoutParams ) v.getLayoutParams();
                    height += layout.topMargin;
                    height += layout.bottomMargin;
                }

                return (height + v.getHeight());
            }
        });

        onCreateEditor( sheetLayout, behavior );
    }

    abstract void onCreateEditor( View layout, CoordinatorLayout.Behavior behavior );

    @LayoutRes
    abstract int getEditorLayoutResource();

    void expandBottomSheetOnFocus(@Nullable final View view, @Nullable final CoordinatorLayout.Behavior behavior ) {
        if( view != null && behavior != null ) {
            view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if( hasFocus ) {
                        if( behavior instanceof BottomSheetBehavior && ((BottomSheetBehavior) behavior).getState() != BottomSheetBehavior.STATE_EXPANDED ) {
                            ((BottomSheetBehavior) behavior).setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                }
            });
        }
    }

    void setVisibility( final boolean isVisible, View... views ) {
        if( views == null || views.length == 0 ) {
            return;
        }

        final int size = views.length;
        for( int index = 0; index < size; index++ ) {
            VisibilityUtils.setVisible( views[index], isVisible );
        }
    }

    @Nullable
    CategoryModel loadCategoryById(final long id ) {
        CategoryModel parent = null;
        if( id == 0 ) {
            return null;
        }

        final Cursor cursor = getContext().getContentResolver().query(
                ContentUris.withAppendedId(CategoryContract.CONTENT_URI, id),
                CategoryContract.PROJECTION,
                null,
                null,
                null
        );
        if( cursor != null && cursor.moveToFirst() ) {
            parent = CategoryModel.FACTORY.createFromCursor(cursor);
        }
        if( cursor != null ) {
            cursor.close();
        }
        return parent;
    }
}
