package com.motthoidecode.findplacesnearby;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.SearchView;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import fragments.MapsFragment;
import fragments.MyFragmentManager;
import fragments.SearchFragment;
import utils.Util;

public class MapsActivity extends FragmentActivity{

    private SearchView mSearchView;
    private ImageView mButtonMenu;

    private MapsFragment mMapsFragment;
    private SearchFragment mSearchFragment;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;


    private boolean mIsMapsMODE = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mSearchView = (SearchView) findViewById(R.id.searchView);
        mButtonMenu = (ImageView) findViewById(R.id.btn_menu);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);

        mMapsFragment = new MapsFragment();
        Bundle b = new Bundle();
        b.putBoolean(Util.KEY_OPEN_MAPS_FOR_THE_FIRST_TIME, true);
        mMapsFragment.setArguments(b);

        // manager Fragment
        MyFragmentManager.initFragmentStack();
        MyFragmentManager.setFragmentManager(getSupportFragmentManager());
        MyFragmentManager.displayFragment(mMapsFragment, this);

        addEventListener();
    }

    private void addEventListener() {

        EditText searchViewEditText = (EditText) mSearchView.findViewById(R.id.search_src_text);
        searchViewEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (mIsMapsMODE) {
                        // open fragment search
                        mSearchFragment = new SearchFragment();
                        MyFragmentManager.displayFragment(mSearchFragment, MapsActivity.this);

                        mButtonMenu.setImageResource(R.drawable.button_back);
                        mSearchView.setQuery("", false);
                        mIsMapsMODE = false;

                    }
                }
            }
        });

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                item.setChecked(true);
                mDrawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.menu_location:
                        showToastMessage(getString(R.string.nav_item_location));
                        break;
                    case R.id.menu_settings:
                        showToastMessage(getString(R.string.nav_item_settings));
                        break;
                    case R.id.menu_bookmark:
                        showToastMessage(getString(R.string.nav_item_bookmark));
                        break;
                    case R.id.menu_history:
                        showToastMessage(getString(R.string.nav_item_history));
                        break;
                    case R.id.menu_add_place:
                        showToastMessage(getString(R.string.nav_item_add_place));
                        break;
                    case R.id.menu_about:
                        showToastMessage(getString(R.string.nav_item_about));
                        break;
                }
                return true;
            }
        });

    }

    public void openMenuDrawerOrBackToMaps(View v) {
        if (mIsMapsMODE){
            mDrawerLayout.openDrawer(GravityCompat.START);
        } else {
            // back to maps
            setMapsModeDefaultInfo();
            mSearchView.setIconified(true);

            MyFragmentManager.backToPreviousFragment();
            mMapsFragment.clearMap();
        }
    }

    private void setMapsModeDefaultInfo() {
        mIsMapsMODE = true;
        mButtonMenu.setImageResource(R.drawable.button_menu);
        mSearchView.setQuery("", false);
        mSearchView.setFocusable(false);
        mSearchView.clearFocus();
    }

    public void showToastMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
