package com.example.wackernagel.myapplication.editor;

import android.content.ContentUris;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.example.wackernagel.myapplication.R;
import com.example.wackernagel.myapplication.db.CategoryContract;
import com.example.wackernagel.myapplication.db.CategoryModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.wackernagel.android.sidekick.compats.CursorCompat;

/*
 * Issue: http://code.google.com/p/android/issues/detail?id=202691
 * Black statusbar with BottomSheetDialogFragment
 */
public class CategoryEditorFragment extends EditorBottomSheet {

    private TextInputLayout editContainer;
    private TextInputEditText editText;

    private CategoryModel editable;
    private CategoryModel parentCategory;

    private static final String ARG_CATEGORY = "arg:parent";
    private static final String ARG_EDITABLE = "arg:editable";

    private static final int ROOT_CATEGORY = 0;

    public static CategoryEditorFragment create(final long categoryId ) {
        return newInstance( categoryId, null );
    }

    public static CategoryEditorFragment update(final CategoryModel editable ) {
        return newInstance( ROOT_CATEGORY, editable );
    }

    private static CategoryEditorFragment newInstance( final long categoryId, final CategoryModel editable ) {
        final Bundle arguments = new Bundle();
        arguments.putLong(ARG_CATEGORY, categoryId );
        if( editable != null ) {
            arguments.putParcelable( ARG_EDITABLE, new CategoryParcelable( editable ) );
        }

        final CategoryEditorFragment fragment = new CategoryEditorFragment();
        fragment.setArguments( arguments );
        return fragment;
    }

    @Override
    int getEditorLayoutResource() {
        return R.layout.editor_category;
    }

    @Override
    void onCreateEditor( final View sheetLayout, final CoordinatorLayout.Behavior behavior ) {
        final Toolbar toolbar = ( Toolbar ) sheetLayout.findViewById(R.id.toolbar);
        toolbar.inflateMenu( R.menu.menu_category );
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch( item.getItemId() ) {
                    case R.id.action_save_category:
                        save();
                    default:
                        return false;
                }
            }
        });

        editContainer = (TextInputLayout ) sheetLayout.findViewById(R.id.edit_container);
        editText = ( TextInputEditText ) editContainer.findViewById(R.id.edit);
        expandBottomSheetOnFocus( editText, behavior );
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if( actionId == EditorInfo.IME_ACTION_DONE ) {
                    save();
                    return true;
                }
                return false;
            }
        });

        final TextView title = ( TextView ) sheetLayout.findViewById(R.id.title);
        final TextInputEditText idText = ( TextInputEditText ) sheetLayout.findViewById(R.id.id);
        final TextInputEditText parentText = ( TextInputEditText ) sheetLayout.findViewById(R.id.parent);
        final TextInputEditText created = ( TextInputEditText ) sheetLayout.findViewById(R.id.created);
        final TextInputEditText changed = ( TextInputEditText ) sheetLayout.findViewById(R.id.changed);

        final CategoryParcelable parcel = getArguments().getParcelable(ARG_EDITABLE);
        final boolean isUpdate = parcel != null;
        if( isUpdate ) {
            editable = parcel.getCategory();
            editText.setText( editable.getName() );
            idText.setText( String.valueOf( editable.getId() ));
            parentCategory = loadCategoryById( editable.getParentId() );

            final SimpleDateFormat format = new SimpleDateFormat( "EEE dd. MMM HH:mm", Locale.getDefault() );
            created.setText( format.format( editable.getCreated() ) );
            changed.setText( format.format( editable.getChanged() ) );
        } else {
            idText.setText( R.string.none );
            parentCategory = loadCategoryById( getArguments().getLong( ARG_CATEGORY, 0 ) );
            created.setText( R.string.none );
            changed.setText( R.string.none );
        }

        if( parentCategory != null ) {
            parentText.setText( parentCategory.getName() );
        } else {
            parentText.setText(R.string.none);
        }

        title.setText( isUpdate ? R.string.editor_category_title_change : R.string.editor_category_title_create );

        setVisibility( isUpdate, idText, parentText, created, changed );
    }

    private void save() {
        final boolean isInsert = (editable == null);

        // CHECK: empty name
        final String name = editText.getText().toString();
        if( TextUtils.isEmpty( name ) ) {
            editContainer.setError( getString( R.string.editor_category_error_empty ) );
            focusEditText( editContainer );
            return;
        }

        // CHECK: name doesn't exist
        if( existCategory( name ) && ( isInsert || !editable.getName().equals( name ) ) ) {
            editContainer.setError( getString( R.string.editor_category_error_exist ) );
            focusEditText( editContainer );
            return;
        }

        if( !isInsert ) {
            getContext().getContentResolver().update(
                    ContentUris.withAppendedId(CategoryContract.CONTENT_URI, editable.getId()),
                    new CategoryModel.Builder()
                            .setName( name )
                            .setChanged( new Date() )
                            .build(),
                    null,
                    null );
        } else {
            getContext().getContentResolver().insert(
                    CategoryContract.CONTENT_URI,
                    new CategoryModel.Builder()
                            .setName( name )
                            .setParentId( parentCategory != null ? parentCategory.getId() : 0 )
                            .setType( CategoryContract.TABLE )
                            .build() );
        }

        dismiss();
    }

    private boolean existCategory( @NonNull  final String name ) {
        final Cursor c = getContext().getContentResolver().query(
                CategoryContract.CONTENT_URI,
                CategoryContract.PROJECTION,
                CategoryContract.COLUMN_NAME + "=?",
                new String[]{ name }, null );
        final boolean exist = c != null && c.getCount() == 1;
        CursorCompat.close(c);
        return exist;
    }
}
