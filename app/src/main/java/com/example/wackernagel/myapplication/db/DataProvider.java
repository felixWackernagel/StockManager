package com.example.wackernagel.myapplication.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract.ContractContentProvider;
import de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract.OnSQLiteSchemaListener;
import de.wackernagel.android.sidekick.frameworks.contentproviderprocessor.contract.SQLiteTableOpenHelper;

public class DataProvider extends ContractContentProvider implements OnSQLiteSchemaListener {

    public static final String AUTHORITY = "com.example.wackernagel.myapplication";

    public DataProvider() {
        super( AUTHORITY, "myapplication.db", 7);
        addContract(new CategoryModel.Contract());
        addContract(new StockItemModel.Contract());
    }

    @NonNull
    @Override
    public SQLiteOpenHelper onCreateSQLiteOpenHelper() {
        final SQLiteOpenHelper helper = super.onCreateSQLiteOpenHelper();
        if( helper instanceof SQLiteTableOpenHelper ) {
            ( (SQLiteTableOpenHelper) helper ).setOnSQLiteSchemaListener(this);
        }
        return helper;
    }

    @Override
    public void onBeforeCreate(SQLiteDatabase sqLiteDatabase) {
    }

    @Override
    public void onAfterCreate(SQLiteDatabase sqLiteDatabase) {
    }

    @Override
    public void onBeforeUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if( oldVersion == 1 && newVersion == 2 ) {
            db.execSQL("DROP TABLE " + CategoryModel.Contract.TABLE);
            getSQLiteOpenHelper().onCreate(db);
        }
        if( oldVersion == 3 && newVersion == 4 ) {
            new StockItemModel.Contract().onCreate(db);
        }
        if( oldVersion == 5 && newVersion == 6 ) {
            db.execSQL("DROP TABLE " + CategoryModel.Contract.TABLE);
            db.execSQL("DROP TABLE " + StockItemModel.Contract.TABLE);
            getSQLiteOpenHelper().onCreate(db);
        }
        if( oldVersion == 6 && newVersion == 7 ) {
            db.execSQL("DROP TABLE " + CategoryModel.Contract.TABLE);
            db.execSQL("DROP TABLE " + StockItemModel.Contract.TABLE);
            getSQLiteOpenHelper().onCreate(db);
        }
    }

    @Override
    public void onAfterUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
