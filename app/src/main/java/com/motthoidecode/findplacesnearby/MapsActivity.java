package com.motthoidecode.findplacesnearby;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import adapters.PlacesResultAdapter;
import adapters.PlacesSuggestAdapter;
import fragments.MapsFragment;
import fragments.MyFragmentManager;
import fragments.SearchFragment;
import model.Place;
import network.DownloadJSONStringTask;
import utils.Util;

public class MapsActivity extends FragmentActivity implements View.OnClickListener{

    private SearchView mSearchView;
    private ImageView mButtonMenu;

    private List<Place> mPlaces;
    private Place mPlaceSelected;

    private MapsFragment mMapsFragment;
    private SearchFragment mSearchFragment;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private SlidingDrawer mSlidingDrawerResults;
    private ListView lvResult;
    private TextView tvResult;

    private boolean mQueryTaskIsRunning = false;
    private boolean mIsMapsMODE = true;
    private boolean mQueryByName = false;

    private int mCategoryId;

    private String resultTitle = "";

    private LatLng mCurrentLocation;

    private DownloadJSONStringTask mDownloadJSONStringTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mSearchView = (SearchView) findViewById(R.id.searchView);
        mButtonMenu = (ImageView) findViewById(R.id.btn_menu);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mSlidingDrawerResults = (SlidingDrawer) findViewById(R.id.slidingDrawerResults);
        tvResult = (TextView) findViewById(R.id.tvResult);
        // ListView result for mSlidingDrawerResults
        lvResult = (ListView) findViewById(R.id.lvResult);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivRestaurant:
                mSearchView.setIconified(true);
                hideSoftKeyboard();
                mCategoryId = Util.ID_RESTAURANT;
                getPlacesByCategoryId(mCategoryId);
                break;
            case R.id.ivCafe:
                mSearchView.setIconified(true);
                hideSoftKeyboard();
                mCategoryId = Util.ID_COFFEE;
                getPlacesByCategoryId(mCategoryId);
                break;
            case R.id.ivATM:
                mSearchView.setIconified(true);
                hideSoftKeyboard();
                mCategoryId = Util.ID_ATM;
                getPlacesByCategoryId(mCategoryId);
                break;
            case R.id.ivPetrol:
                mSearchView.setIconified(true);
                hideSoftKeyboard();
                mCategoryId = Util.ID_PETROL;
                getPlacesByCategoryId(mCategoryId);
                break;
            case R.id.ivEducation:
                mSearchView.setIconified(true);
                hideSoftKeyboard();
                mCategoryId = Util.ID_EDUCATION;
                getPlacesByCategoryId(mCategoryId);
                break;
        }
    }

    public void onQueryPlacesComplete(String jsonStr) {
        // substring Deprecated warning - mysql
        jsonStr = jsonStr.substring(jsonStr.indexOf("["));
        mQueryTaskIsRunning = false;
        try {
            JSONArray placesArray = new JSONArray(jsonStr);
            mPlaces = new ArrayList<Place>();
            for (int i = 0; i < placesArray.length(); i++) {
                JSONObject placeObject = placesArray.getJSONObject(i);
                int id = placeObject.getInt(Util.KEY_ID);
                String name = placeObject.getString(Util.KEY_NAME);
                String address = placeObject.getString(Util.KEY_ADDRESS);
                double latitude = placeObject.getDouble(Util.KEY_LATITUDE);
                double longitude = placeObject.getDouble(Util.KEY_LONGITUDE);
                int categoryId = placeObject.getInt(Util.KEY_CATEGORY_ID);
                String imageUrl = placeObject.getString(Util.KEY_IMAGE_URL);
                String website = placeObject.getString(Util.KEY_WEBSITE);
                String phone = placeObject.getString(Util.KEY_PHONE);
                double distance = placeObject.getDouble(Util.KEY_DISTANCE);

                Place place = new Place(id, name, address, latitude, longitude, categoryId, imageUrl, website, phone, distance);
                mPlaces.add(place);
            }
            if (mQueryByName)
                showListSuggestion();
            else
                showLocationsNearMe(mCategoryId);
        } catch (JSONException e) {
            mPlaces.clear();
            if (!mQueryByName)
                // null json string
                showToastMessage("No place found!");
            e.printStackTrace();
        }
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

                        if (mSlidingDrawerResults.getVisibility() == View.VISIBLE) {
                            mSlidingDrawerResults.close();
                            mSlidingDrawerResults.setVisibility(View.INVISIBLE);
                        }

                    }
                }
            }
        });

        searchViewEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!mIsMapsMODE)
                    if (s.length() == 0) {
                        resetDataListViewSuggestion();
                    } else {
                        getPlacesSuggestion(s.toString());
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

        mSlidingDrawerResults.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {

            @Override
            public void onDrawerOpened() {
                tvResult.setText(getString(R.string.result) + " \"" + resultTitle + "\":");
                PlacesResultAdapter placesResultAdapter = new PlacesResultAdapter(MapsActivity.this, R.layout.result_row, mPlaces);
                lvResult.setAdapter(placesResultAdapter);
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

    private void resetDataListViewSuggestion() {
        mPlaces = new ArrayList<>();
        ListView lvSuggestion = (ListView) findViewById(R.id.lvSuggestion);
        PlacesSuggestAdapter placesSuggestAdapter = new PlacesSuggestAdapter(MapsActivity.this, R.layout.suggestion_row, mPlaces);
        lvSuggestion.setAdapter(placesSuggestAdapter);
    }

    private void getPlacesSuggestion(String name) {

        try {
            name = URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        resetDataListViewSuggestion();

        if (mQueryTaskIsRunning && !mDownloadJSONStringTask.isCancelled()) {
            mDownloadJSONStringTask.cancel(true);
        }
        mDownloadJSONStringTask = new DownloadJSONStringTask(MapsActivity.this);
        name = name.replace(" ", "%20");
        String strUrl = Util.URL_QUERY_BY_NAME + "?name=" + name +
                "&lat=" + mCurrentLocation.latitude +
                "&lng=" + mCurrentLocation.longitude +
                "&distance=" + Util.getMaxDistance(this);
        mDownloadJSONStringTask.execute(strUrl);

        mQueryTaskIsRunning = true;
        mQueryByName = true;
    }

    private void getPlacesByCategoryId(int categoryId) {
        // back to maps
        setMapsModeDefaultInfo();
        mSearchView.setQuery(Util.getCategoryName(categoryId), false);
        MyFragmentManager.backToPreviousFragment();

        if (mQueryTaskIsRunning && !mDownloadJSONStringTask.isCancelled()) {
            mDownloadJSONStringTask.cancel(true);
        }

        mDownloadJSONStringTask = new DownloadJSONStringTask(MapsActivity.this);
        String strUrl = Util.URL_QUERY_BY_CATEGORY_ID + "?categoryid=" + categoryId +
                "&lat=" + mCurrentLocation.latitude +
                "&lng=" + mCurrentLocation.longitude +
                "&distance=" + Util.getMaxDistance(this);
        mDownloadJSONStringTask.execute(strUrl);

        mQueryTaskIsRunning = true;
        mQueryByName = false;
    }

    private void showListSuggestion() {
        ListView lvSuggestion = (ListView) findViewById(R.id.lvSuggestion);
        PlacesSuggestAdapter placesSuggestAdapter = new PlacesSuggestAdapter(this, R.layout.suggestion_row, mPlaces);
        lvSuggestion.setAdapter(placesSuggestAdapter);

        lvSuggestion.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPlaceSelected = mPlaces.get(position);
                mPlaces.clear();
                mPlaces.add(mPlaceSelected);

                // back to maps and show suggest locations
                setMapsModeDefaultInfo();
                mSearchView.setIconified(true);
                MyFragmentManager.backToPreviousFragment();

                // show all markers in the maps
                Bundle data = new Bundle();
                String placeLocation = mPlaces.get(0).getLatitude() + "," + mPlaces.get(0).getLongitude();
                data.putString(Util.KEY_PLACE_LOCATION, placeLocation);

                mMapsFragment.showTheSuggestion(data);

                resultTitle = "";
                mSlidingDrawerResults.setVisibility(View.VISIBLE);
                mSlidingDrawerResults.open();
            }
        });
    }


    private void showLocationsNearMe(int categoryId) {

        // show all markers in the maps
        Bundle data = new Bundle();
        ArrayList<String> placeLocations = new ArrayList<String>();
        for (int i = 0; i < mPlaces.size(); i++) {
            placeLocations.add(mPlaces.get(i).getLatitude() + "," + mPlaces.get(i).getLongitude());
        }
        data.putStringArrayList(Util.KEY_PLACE_LOCATIONS, placeLocations);
        data.putInt(Util.KEY_QUERY_CATEGORY, categoryId);

        mMapsFragment.showAllTheResults(data);


        resultTitle = Util.getCategoryName(categoryId);
        mSlidingDrawerResults.setVisibility(View.VISIBLE);
        mSlidingDrawerResults.open();
    }

    private void setMapsModeDefaultInfo() {
        mIsMapsMODE = true;
        mButtonMenu.setImageResource(R.drawable.button_menu);
        mSearchView.setQuery("", false);
        mSearchView.setFocusable(false);
        mSearchView.clearFocus();
    }

    public void setCurrentLocation(LatLng currentLocation) {
        this.mCurrentLocation = currentLocation;
    }

    public void showToastMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

}
