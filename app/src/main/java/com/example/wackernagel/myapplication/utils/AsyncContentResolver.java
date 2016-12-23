package com.example.wackernagel.myapplication.utils;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

public class AsyncContentResolver extends AsyncQueryHandler {

    public static final int TOKEN_QUERY = 1;
    public static final int TOKEN_INSERT = 2;
    public static final int TOKEN_UPDATE = 3;
    public static final int TOKEN_DELETE = 4;

    @IntDef({TOKEN_QUERY,TOKEN_INSERT,TOKEN_UPDATE,TOKEN_DELETE})
    @Retention( RetentionPolicy.SOURCE)
    public @interface TokenType {}

    public interface OnCompleteListener {
        void onComplete(@TokenType int token, @Nullable Object cookie, @Nullable Cursor cursor, @Nullable Uri uri, @IntRange(from = 0) int result );
    }

    private WeakReference<OnCompleteListener> listener;

    public AsyncContentResolver(ContentResolver cr) {
        super(cr);
    }

    public AsyncContentResolver(ContentResolver cr, OnCompleteListener listener) {
        this( cr );
        setOnCompleteListener( listener );
    }

    public OnCompleteListener getOnCompleteListener() {
        return listener.get();
    }

    public void setOnCompleteListener(OnCompleteListener listener) {
        this.listener = new WeakReference<OnCompleteListener>( listener );
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if( listener.get() != null ) {
            listener.get().onComplete( TOKEN_QUERY, cookie, cursor, null, 0 );
        }
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        if( listener.get() != null ) {
            listener.get().onComplete( TOKEN_INSERT, cookie, null, uri, 0 );
        }
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
        if( listener.get() != null ) {
            listener.get().onComplete( TOKEN_UPDATE, cookie, null, null, result );
        }
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
        if( listener.get() != null ) {
            listener.get().onComplete( TOKEN_DELETE, cookie, null, null, result );
        }
    }
}
