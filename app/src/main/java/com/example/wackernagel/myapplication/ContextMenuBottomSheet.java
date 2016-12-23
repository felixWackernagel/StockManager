package com.example.wackernagel.myapplication;

import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.wackernagel.myapplication.db.CategoryContract;
import com.example.wackernagel.myapplication.db.CategoryModel;
import com.example.wackernagel.myapplication.db.DataProvider;
import com.example.wackernagel.myapplication.db.StockItemContract;
import com.example.wackernagel.myapplication.db.StockItemModel;

import java.util.ArrayList;

import de.wackernagel.android.sidekick.utils.TintUtils;

public class ContextMenuBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_CATEGORY = "arg:category";
    private static final String ARG_STOCK_ITEM = "arg:stock:item";

    public static ContextMenuBottomSheet newInstance( final CategoryModel categoryModel ) {
        final ContextMenuBottomSheet sheet = new ContextMenuBottomSheet();
        final Bundle arguments = new Bundle(1);
        arguments.putParcelable( ARG_CATEGORY, new CategoryParcelable( categoryModel ) );
        sheet.setArguments( arguments );
        return sheet;
    }

    public static ContextMenuBottomSheet newInstance( final StockItemModel stockItemModel ) {
        final ContextMenuBottomSheet sheet = new ContextMenuBottomSheet();
        final Bundle arguments = new Bundle(1);
        arguments.putParcelable( ARG_STOCK_ITEM, new StockItemParcelable( stockItemModel ) );
        sheet.setArguments( arguments );
        return sheet;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        final View content = LayoutInflater.from( getContext() ).inflate( R.layout.bottom_sheet_context_menu, null, false );
        dialog.setContentView( content );

        final TextView actionEdit = (TextView) content.findViewById( R.id.action_edit );
        final TextView actionDelete = (TextView) content.findViewById( R.id.action_delete );
        final int iconGray = ContextCompat.getColor( getContext(), R.color.sidekick_icon );
        TintUtils.tintCompoundDrawables( actionEdit, iconGray );
        TintUtils.tintCompoundDrawables( actionDelete, iconGray );

        final CategoryParcelable parcelable = getArguments().getParcelable( ARG_CATEGORY );
        if( parcelable != null ) {
            final CategoryModel categoryModel = parcelable.getCategory();
            actionEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    CategoryEditorFragment.update( categoryModel ).show( getFragmentManager(), "editor" );
                }
            });
            actionDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();

                    final ArrayList<ContentProviderOperation> statements = new ArrayList<>(2);
                    statements.add( ContentProviderOperation.newUpdate( CategoryContract.CONTENT_URI )
                            .withValues( new CategoryModel.Builder().setParentId( categoryModel.getParentId() ).build() )
                            .withSelection( CategoryContract.COLUMN_PARENT_ID + "=?", new String[]{ String.valueOf( categoryModel.getId() ) } )
                            .withYieldAllowed( true )
                            .build() );
                    statements.add( ContentProviderOperation.newDelete( ContentUris.withAppendedId(CategoryContract.CONTENT_URI, categoryModel.getId() ) )
                            .withYieldAllowed( true )
                            .build() );
                    try {
                        v.getContext().getContentResolver().applyBatch(DataProvider.AUTHORITY, statements );
                    } catch( RemoteException | OperationApplicationException e ) {
                        Log.e( "App", "Error during category deletion.", e );
                    }
                }
            });
        }

        final StockItemParcelable stockItemParcelable = getArguments().getParcelable( ARG_STOCK_ITEM );
        if( stockItemParcelable != null ) {
            final StockItemModel stockItemModel = stockItemParcelable.getStockItem();
            actionEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    StockItemEditorFragment.update( stockItemModel ).show( getFragmentManager(), "editor" );
                }
            });
            actionDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    v.getContext().getContentResolver().delete( ContentUris.withAppendedId(StockItemContract.CONTENT_URI, stockItemModel.getId() ), null, null );
                }
            });
        }
    }
}
