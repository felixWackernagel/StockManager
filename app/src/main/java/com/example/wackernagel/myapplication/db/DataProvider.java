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
        super( AUTHORITY, "myapplication.db", 6);
        addContract(new CategoryContract());
        addContract(new StockItemContract());
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
            db.execSQL("DROP TABLE " + CategoryContract.TABLE);
            getSQLiteOpenHelper().onCreate(db);
        }
        if( oldVersion == 3 && newVersion == 4 ) {
            new StockItemContract().onCreate(db);
        }
        if( oldVersion == 5 && newVersion == 6 ) {
            db.execSQL("DROP TABLE " + CategoryContract.TABLE);
            db.execSQL("DROP TABLE " + StockItemContract.TABLE);
            getSQLiteOpenHelper().onCreate(db);
        }
    }

    @Override
    public void onAfterUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
