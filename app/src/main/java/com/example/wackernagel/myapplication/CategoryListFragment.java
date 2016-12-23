package com.example.wackernagel.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.wackernagel.myapplication.db.CategoryContract;
import com.example.wackernagel.myapplication.db.CategoryModel;
import com.example.wackernagel.myapplication.db.StockItemModel;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CategoryListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Object>>, CategoryAdapter.OnItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    interface CategoryListCallback {
        void onCategoryClick( CategoryModel category );
        void onStockItemClick( StockItemModel stockItem );
    }

    private View loading;
    private View empty;
    private RecyclerView list;
    private DrawerLayout drawer;
    private CategoryAdapter adapter;

    private CategoryListCallback callback;

    private boolean changed = false;
    private String sortOrder = null;

    public static CategoryListFragment newInstance() {
        return newInstance( 0 );
    }

    public static CategoryListFragment newInstance( long parentId ) {
        final Bundle arguments = new Bundle();
        arguments.putLong( CategoryContract.COLUMN_PARENT_ID, parentId );

        final CategoryListFragment fragment = new CategoryListFragment();
        fragment.setArguments( arguments );
        return fragment;
    }

    public CategoryListFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if( context instanceof CategoryListCallback ) {
            callback = ( CategoryListCallback ) context;
        } else {
            throw new IllegalArgumentException( "Hosting Activity must implement CategoryListCallback!" );
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener( this );
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable final Bundle savedInstanceState) {
        adapter = new CategoryAdapter( getFragmentManager() );
        adapter.setOnItemClickListener( this );
        drawer = ( DrawerLayout ) view.findViewById(R.id.drawer);
        drawer.setStatusBarBackground(null);
        loading = view.findViewById(R.id.loading);
        empty = view.findViewById(R.id.empty);

        list = ( RecyclerView ) view.findViewById(R.id.list);
        list.setLayoutManager( new LinearLayoutManager( view.getContext() ) );
        list.setHasFixedSize(true);
        list.setAdapter( adapter );

        final NavigationView sorts = ( NavigationView ) view.findViewById(R.id.navigation);
        sorts.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if( !item.isChecked() && setSortOrder( item ) ) {
                    final int position = indexFor( sorts.getMenu(), item );
                    ProjectPreferences.setSortOrderPosition( getContext(), position );

                    loading.setVisibility( VISIBLE );
                    empty.setVisibility( GONE );
                    list.setVisibility( GONE );
                    getLoaderManager().restartLoader( 0, getArguments(), CategoryListFragment.this );
                }
                drawer.closeDrawer( GravityCompat.END );
                return true;
            }
        });

        final Menu sortMenu = sorts.getMenu();
        themeTitle( sortMenu.findItem(R.id.section_name) );

        final int itemPosition = ProjectPreferences.getSortOrderPosition( getContext() );
        if( sortMenu.size() > itemPosition ) {
            final MenuItem selectedSorting = sortMenu.getItem( itemPosition );
            setSortOrder( selectedSorting );
            sorts.setCheckedItem( selectedSorting.setCheckable( true ).getItemId() );
        }
    }

    private int indexFor( Menu menu, MenuItem item ) {
        int position = 0;
        if( menu == null || item == null ) {
            return  position;
        } else if( item.getMenuInfo() instanceof AdapterView.AdapterContextMenuInfo ) {
            position = ( (AdapterView.AdapterContextMenuInfo) item.getMenuInfo() ).position;
        } else {
            for( int index = 0; index < menu.size(); index++ ) {
                if( menu.getItem( index ).equals( item ) ) {
                    position = index;
                    break;
                }
            }
        }
        return position;
    }

    private void themeTitle( @Nullable final MenuItem item ) {
        if( item == null ) {
            return;
        }
        final SpannableString themed = new SpannableString( item.getTitle() );
        themed.setSpan(new ForegroundColorSpan(ContextCompat.getColor( getContext(), R.color.sidekick_text_primary ) ), 0, themed.length(), 0 );
        item.setTitle( themed );
    }

    private boolean setSortOrder( @Nullable final MenuItem item ) {
        if( item == null ) {
            return false;
        }
        switch( item.getItemId() ) {
            case R.id.action_sort_alpha_asc:
                sortOrder = CategoryContract.COLUMN_NAME + " ASC";
                return true;

            case R.id.action_sort_alpha_desc:
                sortOrder = CategoryContract.COLUMN_NAME + " DESC";
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loading.setVisibility( VISIBLE );
        empty.setVisibility( GONE );
        list.setVisibility( GONE );

        // Is the fragment resumed and something was changed
        // then restart the loader
        // otherwise reuse previous loaded result.
        if( changed ) {
            getLoaderManager().restartLoader( 0, getArguments(), this );
            changed = false;
        } else {
            getLoaderManager().initLoader( 0, getArguments(), this );
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate( R.menu.menu_list, menu );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch( item.getItemId() ) {
            case R.id.action_sorts:
                if( drawer.isDrawerOpen(GravityCompat.END) ) {
                    drawer.closeDrawer(GravityCompat.END);
                } else {
                    drawer.openDrawer(GravityCompat.END);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        changed = !isVisible() && key.equals( ProjectPreferences.PREF_SORT_ORDER );
    }

    @Override
    public Loader<List<Object>> onCreateLoader(int id, Bundle args) {
        if( id == 0 ) {
            return new DataLoader(
                    getActivity(),
                    sortOrder,
                    getParentId( args )
            );
        }
        return null;
    }

    private static String getParentId( Bundle args ) {
        return String.valueOf( (args != null) ? args.getLong( CategoryContract.COLUMN_PARENT_ID, 0 ) : 0 );
    }

    @Override
    public void onLoadFinished(Loader<List<Object>> loader, List<Object> data) {
        if( loader.getId() == 0 ) {
            loading.setVisibility( GONE );
            if( data != null ) {
                adapter.clearItems();
                if( !data.isEmpty() ) {
                    adapter.addItems( data );
                    list.setVisibility(VISIBLE);
                    empty.setVisibility(GONE);
                } else {
                    empty.setVisibility(VISIBLE);
                    list.setVisibility(GONE);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Object>> loader) {
        if( loader.getId() == 0 ) {
            adapter.clearItems();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener( this );
    }

    @Override
    public void onCategoryClick(CategoryModel categoryModel) {
        callback.onCategoryClick( categoryModel );
    }

    @Override
    public void onStockItemClick(StockItemModel stockItemModel) {
        callback.onStockItemClick( stockItemModel );
    }

    /**
     * @return true if event was consumed otherwise false
     */
    public boolean onBackPressed() {
        if( drawer.isDrawerOpen( GravityCompat.END ) ) {
            drawer.closeDrawer( GravityCompat.END );
            return true;
        }
        return false;
    }
}
