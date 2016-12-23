package com.example.wackernagel.myapplication;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.wackernagel.myapplication.db.CategoryContract;
import com.example.wackernagel.myapplication.db.CategoryModel;
import com.example.wackernagel.myapplication.db.StockItemContract;
import com.example.wackernagel.myapplication.db.StockItemModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.wackernagel.android.sidekick.compats.CursorCompat;
import de.wackernagel.android.sidekick.frameworks.objectcursor.ObjectLoader;

class DataLoader extends ObjectLoader<List<Object>> {

    private final String categoryId;
    private final String sortOrder;
    private boolean observing = false;
    private ForceLoadContentObserver categoryObserver = new ForceLoadContentObserver();
    private ForceLoadContentObserver stockItemObserver = new ForceLoadContentObserver();

    DataLoader(@NonNull Context context, @Nullable final String sortOrder, final String categoryId ) {
        super(context);
        this.categoryId = categoryId;
        this.sortOrder = sortOrder;
    }

    @Override
    protected void onRegisterObserver() {
        if( !observing ) {
            getContext().getContentResolver().registerContentObserver( CategoryContract.CONTENT_URI, true, categoryObserver );
            getContext().getContentResolver().registerContentObserver( StockItemContract.CONTENT_URI, true, stockItemObserver );
            observing = true;
        }
    }

    @Override
    protected void onUnregisterObserver() {
        if( observing ) {
            getContext().getContentResolver().unregisterContentObserver( categoryObserver );
            getContext().getContentResolver().unregisterContentObserver( stockItemObserver );
            observing = false;
        }
    }

    @Override
    public List<Object> loadInBackground() {
        final List<CategoryModel> categories = loadCategories();
        final List<StockItemModel> stockItems = loadStockItems();
        final List<Object> result = new ArrayList<>( categories.size() + stockItems.size() + 2 );
        if( !categories.isEmpty() ) {
            result.add( getContext().getString( R.string.list_header_categories ) );
        }
        result.addAll( categories );
        if( !stockItems.isEmpty() ) {
            result.add( getContext().getString( R.string.list_header_products ) );
        }
        result.addAll( stockItems );
        return result;
    }

    private List<CategoryModel> loadCategories() {
        final Cursor cursor = getContext().getContentResolver().query(
                CategoryContract.CONTENT_URI,
                CategoryContract.PROJECTION,
                CategoryContract.COLUMN_PARENT_ID + "=?",
                new String[]{ categoryId },
                sortOrder );
        if( cursor != null && cursor.moveToFirst() ) {
            final int size = cursor.getCount();
            final List<CategoryModel> categoryModels = new ArrayList<>( size );
            do {
                categoryModels.add( CategoryModel.FACTORY.createFromCursor( cursor ) );
            } while( cursor.moveToNext() );
            CursorCompat.close( cursor );
            return categoryModels;
        } else {
            CursorCompat.close( cursor );
            return Collections.emptyList();
        }
    }

    private List<StockItemModel> loadStockItems() {
        final Cursor cursor = getContext().getContentResolver().query(
                StockItemContract.CONTENT_URI,
                StockItemContract.PROJECTION,
                StockItemContract.COLUMN_CATEGORY_ID + "=?",
                new String[]{ String.valueOf( categoryId ) },
                sortOrder );
        if( cursor != null && cursor.moveToFirst() ) {
            final int size = cursor.getCount();
            final List<StockItemModel> stockItemModels = new ArrayList<>( size );
            do {
                stockItemModels.add( StockItemModel.FACTORY.createFromCursor( cursor ) );
            } while( cursor.moveToNext() );
            CursorCompat.close( cursor );
            return stockItemModels;
        } else {
            CursorCompat.close( cursor );
            return Collections.emptyList();
        }
    }
}
