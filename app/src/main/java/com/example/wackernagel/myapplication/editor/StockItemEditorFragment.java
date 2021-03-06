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
import com.example.wackernagel.myapplication.db.CategoryModel;
import com.example.wackernagel.myapplication.db.StockItemModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import de.wackernagel.android.sidekick.compats.CursorCompat;

/*
 * Issue: http://code.google.com/p/android/issues/detail?id=202691
 * Black statusbar with BottomSheetDialogFragment
 */
public class StockItemEditorFragment extends EditorBottomSheet {

    private TextInputLayout nameContainer;
    private TextInputLayout stockContainer;
    private TextInputLayout orderLimitContainer;

    private StockItemModel editable;
    private CategoryModel parentCategory;

    private static final String ARG_CATEGORY = "arg:parent";
    private static final String ARG_EDITABLE = "arg:editable";

    private static final int ROOT_CATEGORY = 0;

    public static StockItemEditorFragment create(final long categoryId ) {
        return newInstance( categoryId, null );
    }

    public static StockItemEditorFragment update(final StockItemModel editable ) {
        return newInstance( ROOT_CATEGORY, editable );
    }

    private static StockItemEditorFragment newInstance(final long categoryId, final StockItemModel editable ) {
        final Bundle arguments = new Bundle();
        arguments.putLong(ARG_CATEGORY, categoryId );
        if( editable != null ) {
            arguments.putParcelable( ARG_EDITABLE, new StockItemParcelable( editable ) );
        }

        final StockItemEditorFragment fragment = new StockItemEditorFragment();
        fragment.setArguments( arguments );
        return fragment;
    }

    @Override
    int getEditorLayoutResource() {
        return R.layout.editor_stock_item;
    }

    @Override
    void onCreateEditor(final View sheetLayout, final CoordinatorLayout.Behavior behavior ) {
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

        nameContainer = (TextInputLayout ) sheetLayout.findViewById(R.id.edit_container);
        expandBottomSheetOnFocus( nameContainer.getEditText(), behavior );

        stockContainer = (TextInputLayout ) sheetLayout.findViewById(R.id.stock_container);
        expandBottomSheetOnFocus( stockContainer.getEditText(), behavior );

        orderLimitContainer = (TextInputLayout ) sheetLayout.findViewById(R.id.order_limit_container);
        expandBottomSheetOnFocus( orderLimitContainer.getEditText(), behavior );
        orderLimitContainer.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

        final StockItemParcelable parcel = getArguments().getParcelable(ARG_EDITABLE);
        final boolean isUpdate = parcel != null;
        if( isUpdate ) {
            editable = parcel.getStockItem();
            nameContainer.getEditText().setText( editable.getName() );
            stockContainer.getEditText().setText( String.valueOf( editable.getStock() ));
            orderLimitContainer.getEditText().setText( String.valueOf( editable.getOrderLimit() ) );
            idText.setText( String.valueOf( editable.getId() ));
            parentCategory = loadCategoryById( editable.getCategoryId() );

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

        title.setText( isUpdate ? R.string.editor_stock_item_title_change : R.string.editor_stock_item_title_create );

        setVisibility( isUpdate, idText, parentText, created, changed );
    }

    private void save() {
        final boolean isInsert = (editable == null);

        // CHECK: name isn't empty
        final String name = nameContainer.getEditText().getText().toString();
        if( TextUtils.isEmpty( name ) ) {
            nameContainer.setError( getString( R.string.editor_stock_item_error_empty ) );
            focusEditText( nameContainer );
            return;
        }

        // CHECK: name doesn't exist
        if( existStockItem( name ) && ( isInsert || !name.equals( editable.getName() ) ) ) {
            nameContainer.setError( getString( R.string.editor_stock_item_error_exist ) );
            focusEditText( nameContainer );
            return;
        }

        // CHECK: stock isn't empty
        final String stock = stockContainer.getEditText().getText().toString();
        if( TextUtils.isEmpty( stock ) ) {
            stockContainer.setError( getString( R.string.editor_stock_item_error_empty ) );
            focusEditText( stockContainer );
            return;
        }

        // CHECK: orderLimit isn't empty
        final String orderLimit = orderLimitContainer.getEditText().getText().toString();
        if( TextUtils.isEmpty( orderLimit ) ) {
            orderLimitContainer.setError( getString( R.string.editor_stock_item_error_empty ) );
            focusEditText( orderLimitContainer );
            return;
        }

        // CHECK: only digits without leading zero
        final Pattern validNumber = Pattern.compile( "^(0|[1-9][0-9]*)$" );
        if( !validNumber.matcher( stock ).matches() ) {
            stockContainer.setError( getString( R.string.editor_stock_item_error_no_number ) );
            focusEditText( stockContainer );
            return;
        }

        if( !validNumber.matcher( orderLimit ).matches() ) {
            orderLimitContainer.setError( getString( R.string.editor_stock_item_error_no_number ) );
            focusEditText( orderLimitContainer );
            return;
        }

        if( !isInsert ) {
            getContext().getContentResolver().update(
                    ContentUris.withAppendedId(StockItemModel.Contract.CONTENT_URI, editable.getId()),
                    StockItemModel.builder()
                            .setName( name )
                            .setStock( Integer.valueOf( stock ) )
                            .setOrderLimit( Integer.valueOf( orderLimit ) )
                            .setChanged( new Date() )
                            .build(),
                    null,
                    null );
        } else {
            getContext().getContentResolver().insert(
                    StockItemModel.Contract.CONTENT_URI,
                    StockItemModel.builder()
                            .setName( name )
                            .setStock( Integer.valueOf( stock ) )
                            .setOrderLimit( Integer.valueOf( orderLimit ) )
                            .setCategoryId( parentCategory != null ? parentCategory.getId() : 0 )
                            .setType(StockItemModel.Contract.TABLE)
                            .build() );
        }

        dismiss();
    }

    private boolean existStockItem( @NonNull  final String name ) {
        final Cursor c = getContext().getContentResolver().query(
                StockItemModel.Contract.CONTENT_URI,
                StockItemModel.Contract.PROJECTION,
                StockItemModel.Contract.COLUMN_NAME + "=?",
                new String[]{ name }, null );
        final boolean exist = c != null && c.getCount() == 1;
        CursorCompat.close(c);
        return exist;
    }
}
