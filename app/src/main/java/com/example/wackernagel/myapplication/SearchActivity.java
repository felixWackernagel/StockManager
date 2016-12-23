package com.example.wackernagel.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import de.wackernagel.android.sidekick.utils.TintUtils;
import de.wackernagel.android.sidekick.utils.TypefaceUtils;

public class SearchActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar( toolbar );
        if( getSupportActionBar() != null ) {
            getSupportActionBar().setDisplayHomeAsUpEnabled( true );
        }

        final EditText searchInput = ( EditText ) findViewById(R.id.search_input);
        searchInput.setTypeface(TypefaceUtils.getRobotoRegular( this ));
        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView input, int actionId, KeyEvent event) {
                if( actionId == EditorInfo.IME_ACTION_DONE ) {
                    // first clear focus which focus now the focus_thief view
                    input.clearFocus();
                    // then close keyboard otherwise it is visible for the focus_thief view
                    final InputMethodManager imm = ( InputMethodManager ) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(findViewById(R.id.focus_thief).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                    query( input.getText() );
                    return true;
                }
                return false;
            }
        });

        final InputMethodManager imm = ( InputMethodManager ) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
    }

    private void query(CharSequence text) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.menu_search, menu );
        final MenuItem clear = menu.findItem( R.id.action_clear );
        if( clear != null ) {
            clear.setIcon(TintUtils.tint( clear.getIcon(), ContextCompat.getColor( this, R.color.sidekick_icon )) );
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch( item.getItemId() ) {
            case R.id.action_clear:
                final EditText searchInput = ( EditText ) findViewById(R.id.search_input);
                searchInput.setText( null );
                searchInput.requestFocus();
                final InputMethodManager imm = ( InputMethodManager ) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
