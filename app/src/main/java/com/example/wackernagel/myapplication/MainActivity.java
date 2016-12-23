package com.example.wackernagel.myapplication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.example.wackernagel.myapplication.db.CategoryModel;
import com.example.wackernagel.myapplication.db.StockItemModel;
import com.example.wackernagel.myapplication.utils.VisibilityUtils;

import java.util.Locale;

import de.wackernagel.android.sidekick.widgets.CircularRevealView;

import static com.example.wackernagel.myapplication.R.id.fab;
import static com.example.wackernagel.myapplication.R.id.reveal;

public class MainActivity extends AppCompatActivity implements CategoryListFragment.CategoryListCallback, CircularRevealView.OnStateChangeListener, Animation.AnimationListener {

    private DrawerLayout drawer;
    private CircularRevealView circularReveal;
    private FloatingActionButton floatingActionButton;
    private View bottomToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme( R.style.AppTheme );
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // draw navigation drawer under status bar
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            getWindow().setStatusBarColor(ContextCompat.getColor( this, android.R.color.transparent ));
        }

        final Toolbar toolbar = (Toolbar ) findViewById(R.id.toolbar);
        setSupportActionBar( toolbar );
        if( getSupportActionBar() != null ) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        final NavigationView navigation = (NavigationView) findViewById( R.id.navigation );
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch( item.getItemId() ) {
                    case R.id.nav_stock:
                        return true;
                    case R.id.nav_orders:
                        return true;
                    default:
                        return false;
                }
            }
        });

        final TabLayout tabs = (TabLayout) findViewById(R.id.tabLayout);
        tabs.addTab( tabs.newTab().setText( R.string.app_name ) );

        drawer = (DrawerLayout ) findViewById(R.id.drawer);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener( toggle );
        toggle.syncState();

        circularReveal = (CircularRevealView) findViewById(reveal);
        circularReveal.setOnStateChangeListener(this);
        bottomToolbar = findViewById(R.id.button_bar);

        floatingActionButton = ( FloatingActionButton ) findViewById(fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                translateFab(true, true, MainActivity.this);
            }
        });
        floatingActionButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ViewTreeObserver vto = floatingActionButton.getViewTreeObserver();
                if( vto != null && vto.isAlive() ) {
                    if( Build.VERSION.SDK_INT >= 16 ) {
                        floatingActionButton.getViewTreeObserver().removeOnGlobalLayoutListener( this );
                    } else {
                        floatingActionButton.getViewTreeObserver().removeGlobalOnLayoutListener( this );
                    }

                    if( circularReveal.getState() == CircularRevealView.STATE_REVEALED ) {
                        VisibilityUtils.setVisible(bottomToolbar, true);
                        translateFab(true, false, null);
                    }
                }
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                final FragmentManager fm = getSupportFragmentManager();
                int tabCount = tabs.getTabCount() - 1;
                int stackCount = fm.getBackStackEntryCount();
                if( stackCount > tabCount ) {
                    tabs.addTab( tabs.newTab().setCustomView(R.layout.custom_tab_view).setText( fm.getBackStackEntryAt( stackCount - 1 ).getBreadCrumbTitle().toString().toUpperCase(Locale.getDefault()) ) );
                } else if( stackCount < tabCount ) {
                    tabs.removeTabAt( tabCount );
                } else {
                    Log.i( "App", "BackStack and Tabs has same count." );
                }
                tabs.post(new Runnable() {
                    @Override
                    public void run() {
                        tabs.fullScroll(View.FOCUS_RIGHT);
                    }
                });
            }
        });
        final int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        if( backStackCount > 0 ) {
            final FragmentManager fm = getSupportFragmentManager();
            for( int index = 0; index < backStackCount; index++ ) {
                tabs.addTab( tabs.newTab().setCustomView(R.layout.custom_tab_view).setText( fm.getBackStackEntryAt( index ).getBreadCrumbTitle().toString().toUpperCase(Locale.getDefault()) ) );
            }
        }

        if( savedInstanceState == null ) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add( R.id.slot_main, CategoryListFragment.newInstance() )
                    .commitNow();
        }
    }

    private void translateFab(boolean openIt, boolean animate, Animation.AnimationListener listener) {
        final int[] to = getFabEnd();
        final Interpolator fast = new AccelerateInterpolator();
        final Interpolator slow = new DecelerateInterpolator();

        final Animation toX = new TranslateXAnimation( openIt ? 0 : -to[0], openIt ? -to[0] : 0 );
        toX.setInterpolator( openIt ? fast : slow );

        final Animation toY = new TranslateYAnimation( openIt ? 0 : to[1], openIt ? to[1] : 0 );
        toY.setInterpolator( openIt ? slow : fast );

        final AnimationSet set = new AnimationSet(false);
        set.setDuration( animate ? 200 : 0 );
        set.addAnimation(toX);
        set.addAnimation(toY);
        set.setFillAfter(true);

        if( listener != null ) {
            set.setAnimationListener( listener );
        }

        if( floatingActionButton.getAnimation() != null ) {
            floatingActionButton.getAnimation().cancel();
        }
        floatingActionButton.startAnimation(set);
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        int[] from = new int[2];
        from[0] = bottomToolbar.getWidth() / 2;
        from[1] = bottomToolbar.getHeight() / 2;
        circularReveal.reveal( from );
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    private int[] getFabEnd() {
        int toY = 0;
        int toX = (bottomToolbar.getWidth() / 2) - (floatingActionButton.getWidth() / 2);
        if( floatingActionButton.getLayoutParams() instanceof ViewGroup.MarginLayoutParams ) {
            toY += ( (ViewGroup.MarginLayoutParams ) floatingActionButton.getLayoutParams() ).bottomMargin;
            toX -= ( (ViewGroup.MarginLayoutParams ) floatingActionButton.getLayoutParams() ).leftMargin;
        }

        int[] end = new int[2];
        end[0] = toX;
        end[1] = toY;
        return end;
    }

    @Override
    public void onStateChange(int state) {
        final View container = findViewById(R.id.container);
        switch( state ) {
            case CircularRevealView.STATE_CONCEALED:
                VisibilityUtils.setVisible(bottomToolbar, false);
                translateFab( false, true, null );
                break;

            case CircularRevealView.STATE_CONCEAL_STARTED:
                ViewCompat.animate(container).alpha(0f).setDuration(200).start();
                break;

            case CircularRevealView.STATE_REVEALED:
                ViewCompat.animate(container).alpha(1f).setDuration(200).start();
                break;

            case CircularRevealView.STATE_REVEAL_STARTED:
                VisibilityUtils.setVisible(bottomToolbar, true);
                ViewCompat.setAlpha(container, 0f);
                break;
        }
    }

    public void openStockEditor(View v) {
        long parentId = 0;
        if( getSupportFragmentManager().getBackStackEntryCount() > 0 ) {
            parentId = Long.valueOf( getSupportFragmentManager().getBackStackEntryAt( getSupportFragmentManager().getBackStackEntryCount() - 1 ).getName() );
        }
        StockItemEditorFragment.create( parentId ).show( getSupportFragmentManager(), "editor");
    }

    public void openCategoryEditor(View v) {
        long parentId = 0;
        if( getSupportFragmentManager().getBackStackEntryCount() > 0 ) {
            parentId = Long.valueOf( getSupportFragmentManager().getBackStackEntryAt( getSupportFragmentManager().getBackStackEntryCount() - 1 ).getName() );
        }
        CategoryEditorFragment.create( parentId ).show( getSupportFragmentManager(), "editor");
    }

    @Override
    public void onBackPressed() {
        if( drawer.isDrawerOpen(GravityCompat.START) ) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }
        if( fragmentConsumedBackPress() ) {
            return;
        }
        if( circularReveal.getState() == CircularRevealView.STATE_REVEALED ) {
            int[] to = new int[2];
            to[0] = circularReveal.getWidth() / 2;
            to[1] = circularReveal.getHeight() / 2;
            circularReveal.conceal( to );
            return;
        }
        super.onBackPressed();
    }

    private boolean fragmentConsumedBackPress() {
        final Fragment attachedFragment = getSupportFragmentManager().findFragmentById(R.id.slot_main);
        return attachedFragment instanceof CategoryListFragment
                && ( (CategoryListFragment ) attachedFragment ).onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch( item.getItemId() ) {
            case R.id.action_search:
                ActivityCompat.startActivity(this, new Intent(this, SearchActivity.class),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCategoryClick( CategoryModel category ) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
                .replace( R.id.slot_main, CategoryListFragment.newInstance( category.getId() ) )
                .addToBackStack( String.valueOf( category.getId() ) )
                .setBreadCrumbTitle( category.getName() )
                .commit();
    }

    @Override
    public void onStockItemClick(StockItemModel stockItem) {

    }
}
