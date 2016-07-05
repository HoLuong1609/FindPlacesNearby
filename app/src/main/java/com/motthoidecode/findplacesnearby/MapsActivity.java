package com.motthoidecode.findplacesnearby;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import adapters.PlacesResultAdapter;
import adapters.PlacesSuggestAdapter;
import databases.PlaceDbHelper;
import direction.DirectionsJSONParserTask.OnJSONParserCompleteListener;
import direction.MapsDirections;
import direction.MapsDirections.TRAVEL_MODES;
import fragments.MapsFragment;
import fragments.MyFragmentManager;
import fragments.SearchFragment;
import model.Place;
import network.DownloadJSONStringTask;
import utils.Util;
import views.ClickableSlidingDrawer;

public class MapsActivity extends FragmentActivity implements View.OnClickListener {

    private static final float ALPHA_IMAGEVIEW = 0.5f;
    private static final int REQUEST_CODE_OPEN_REPORT_ACTIVITY = 1;
    public static final int RESULT_CODE_ADD_REVIEW_OK = 2;

    private SearchView mSearchView;
    private ImageView mButtonMenu;

    private List<Place> mPlaces;
    private Place mPlaceSelected;

    private MapsFragment mMapsFragment;
    private SearchFragment mSearchFragment;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private SlidingDrawer mSlidingDrawerResults;
    private ClickableSlidingDrawer mSlidingDrawerResultsDetail;
    private ClickableSlidingDrawer mSlidingDrawerDirection;
    private ListView lvResult;
    private TextView tvResult;
    private ImageView btnDriving, btnWalking, btnExchangeRoute;
    private TextView tvOrigin, tvDestination;

    private boolean mQueryTaskIsRunning = false;
    private boolean mIsMapsMODE = true;
    private boolean mQueryByName = false;
    private boolean mIsDrivingMode;
    private boolean mIsReverseRoute = false;

    private int mCategoryId;

    private String resultTitle = "";

    private LatLng mCurrentLocation;
    private LatLng mDestination;

    private DownloadJSONStringTask mDownloadJSONStringTask;

    private OnJSONParserCompleteListener mParserCompleteListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mSearchView = (SearchView) findViewById(R.id.searchView);
        mButtonMenu = (ImageView) findViewById(R.id.btn_menu);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mSlidingDrawerResults = (SlidingDrawer) findViewById(R.id.slidingDrawerResults);
        mSlidingDrawerResultsDetail = (ClickableSlidingDrawer) findViewById(R.id.slidingDrawerResultsDetail);
        mSlidingDrawerDirection = (ClickableSlidingDrawer) findViewById(R.id.slidingDrawerDirection);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_REPORT_ACTIVITY && resultCode == RESULT_CODE_ADD_REVIEW_OK) {
            DownloadJSONStringTask task = new DownloadJSONStringTask(this, true);
            task.execute(Util.URL_GET_ALL_REVIEWS_BY_PLACE_ID + "?placeId=" + mPlaceSelected.getId());
        }
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
            case R.id.ivExchangeRoute:
                if (mIsReverseRoute) {
                    tvOrigin.setText(mPlaceSelected.getName());
                    tvDestination.setText(getString(R.string.current_location));
                } else {
                    tvDestination.setText(mPlaceSelected.getName());
                    tvOrigin.setText(getString(R.string.current_location));
                }
                if (mIsDrivingMode)
                    walkingDirection(findViewById(R.id.ivDetailPlace));
                else
                    drivingDirection(findViewById(R.id.ivDetailPlace));
                mIsReverseRoute = !mIsReverseRoute;
                break;
            case R.id.ivCall:
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mPlaceSelected.getPhone()));
                startActivity(intent);
                break;
            case R.id.ivWebsite:
                Intent website = new Intent(Intent.ACTION_VIEW);
                website.setData(Uri.parse("http://" + mPlaceSelected.getWebsite()));
                startActivity(website);
                break;
            case R.id.ivFavorite:
                ImageView ivFavorite = (ImageView) findViewById(R.id.ivFavorite);
                PlaceDbHelper placeDbHelper = new PlaceDbHelper(MapsActivity.this, null);
                Place favoritePlace = placeDbHelper.getFavoritePlace(mPlaceSelected.getId());
                if (favoritePlace == null) {
                    mPlaceSelected.setFavorite(1);
                    placeDbHelper.addFavoritePlace(mPlaceSelected);
                } else {
                    mPlaceSelected.setFavorite(0);
                    placeDbHelper.deleteFavoritePlace(mPlaceSelected.getId());
                }
                if (mPlaceSelected.getFavorite() == 1)
                    ivFavorite.setImageResource(R.drawable.new_ic_bookmarked);
                else
                    ivFavorite.setImageResource(R.drawable.new_ic_bookmark_star);
                break;
            case R.id.ivComment:
                Intent reviewIntent = new Intent(MapsActivity.this, ReviewActivity.class);
                reviewIntent.putExtra(Util.KEY_ID, mPlaceSelected.getId());
                startActivityForResult(reviewIntent, REQUEST_CODE_OPEN_REPORT_ACTIVITY);
                break;
            case R.id.ivDetailPlace:   // Display Image of Place
                Intent i = new Intent(MapsActivity.this, DisplayImageActivity.class);
                i.putExtra(Util.KEY_IMAGE_URL, mPlaceSelected.getImageUrl());
                startActivity(i);
                break;
        }
    }

    public void onMapsMarkerClick(int markerPos) {
        mPlaceSelected = mPlaces.get(markerPos);

        mDestination = new LatLng(mPlaceSelected.getLatitude(), mPlaceSelected.getLongitude());

        if (mSlidingDrawerResults.isOpened())
            mSlidingDrawerResults.close();
        if (mSlidingDrawerResults.getVisibility() == View.VISIBLE)
            mSlidingDrawerResults.setVisibility(View.INVISIBLE);

        if (mSlidingDrawerDirection.isOpened())
            mSlidingDrawerDirection.close();
        if (mSlidingDrawerDirection.getVisibility() == View.VISIBLE)
            mSlidingDrawerDirection.setVisibility(View.INVISIBLE);

        if (mSlidingDrawerResultsDetail.isOpened())
            mSlidingDrawerResultsDetail.close();
        if (mSlidingDrawerResultsDetail.getVisibility() == View.INVISIBLE)
            mSlidingDrawerResultsDetail.setVisibility(View.VISIBLE);
        // reopen
        mSlidingDrawerResultsDetail.open();
    }

    public void onQueryPlacesComplete(String jsonStr) {
        try {
            // substring Deprecated warning - mysql
            jsonStr = jsonStr.substring(jsonStr.indexOf("["));
        }catch(Exception e){
            showToastMessage("Null Json String!");
            return;
        }
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

    public void onDirectComplete(String distance, String duration, boolean isError, boolean isNoPoints) {
        if (mIsDrivingMode) {
            btnDriving.setAlpha(1f);
            btnWalking.setAlpha(ALPHA_IMAGEVIEW);
        } else {
            btnDriving.setAlpha(ALPHA_IMAGEVIEW);
            btnWalking.setAlpha(1f);
        }
        btnDriving.setEnabled(true);
        btnWalking.setEnabled(true);
        if (!btnExchangeRoute.isEnabled())
            btnExchangeRoute.setEnabled(true);
        btnExchangeRoute.setAlpha(1f);
        mIsDrivingMode = !mIsDrivingMode;

        if (isError) {
            showToastMessage("Error!");
            return;
        } else if (isNoPoints)
            showToastMessage("No Points!");

        TextView tvDistance = (TextView) findViewById(R.id.tvDistance);
        TextView tvDuration = (TextView) findViewById(R.id.tvDuration);
        tvDistance.setText(distance);
        tvDuration.setText(duration);

    }

    public void onGetReviewComplete(String jsonStr) {
        if (jsonStr.contains("null"))
            jsonStr = "[]";
        else
            // substring Deprecated warning - mysql
            jsonStr = jsonStr.substring(jsonStr.indexOf("["));
        StringBuilder strBuilder = new StringBuilder();
        try {
            JSONArray reviewsArray = new JSONArray(jsonStr);
            for (int i = 0; i < reviewsArray.length(); i++) {
                JSONObject placeObject = reviewsArray.getJSONObject(i);
                String content = placeObject.getString(Util.KEY_CONTENT);
                strBuilder.append("- " + content + "\n");
            }
            if (mSlidingDrawerResultsDetail.isOpened() && mSlidingDrawerResultsDetail.getVisibility() == View.VISIBLE) {
                TextView tvComment = (TextView) findViewById(R.id.tvComment);
                tvComment.setText(strBuilder.toString());
            }
        } catch (JSONException e) {
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
                        Intent intent = new Intent();
                        intent.setClass(MapsActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.menu_bookmark:
                        showToastMessage(getString(R.string.nav_item_bookmark));
                        break;
                    case R.id.menu_history:
                        showToastMessage(getString(R.string.nav_item_history));
                        break;
                    case R.id.menu_add_place:
                        Intent myPlacesIntent = new Intent(MapsActivity.this, MyPlacesActivity.class);
                        startActivity(myPlacesIntent);
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

        lvResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPlaceSelected = mPlaces.get(position);

                mMapsFragment.setMarkerSelected(position);

                mDestination = new LatLng(mPlaceSelected.getLatitude(), mPlaceSelected.getLongitude());

                mSlidingDrawerResults.setVisibility(View.INVISIBLE);
                mSlidingDrawerResults.close();
                mSlidingDrawerResultsDetail.setVisibility(View.VISIBLE);
                mSlidingDrawerResultsDetail.open();
            }
        });

        mSlidingDrawerResultsDetail.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {

            @Override
            public void onDrawerOpened() {
                ImageView ivDetailPlaceThumb = (ImageView) findViewById(R.id.ivDetailPlaceThumb);
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                ivDetailPlaceThumb.setLayoutParams(param);
                ivDetailPlaceThumb.setImageResource(0);

                TextView tvDetailPlaceName = (TextView) findViewById(R.id.tvDetailPlaceName);
                TextView tvDetailPlaceAddress = (TextView) findViewById(R.id.tvDetailPlaceAddress);
                TextView tvDetailPlaceCategory = (TextView) findViewById(R.id.tvDetailPlaceCategory);
                TextView tvDetailPlaceDistance = (TextView) findViewById(R.id.tvDetailPlaceDistance);
                ImageView ivDetailCategory = (ImageView) findViewById(R.id.ivDetailCategory);
                ImageView ivDetailPlace = (ImageView) findViewById(R.id.ivDetailPlace);

                Picasso.with(MapsActivity.this).load(mPlaceSelected.getImageUrl()).into(ivDetailPlace);
                tvDetailPlaceName.setText(mPlaceSelected.getName());
                tvDetailPlaceAddress.setText(mPlaceSelected.getAddress());
                tvDetailPlaceCategory.setText(Util.getCategoryName(mPlaceSelected.getCategoryId()));
                ivDetailCategory.setImageResource(Util.getImageResourceID(mPlaceSelected.getCategoryId()));
                tvDetailPlaceDistance.setText(Util.formatDistance(mPlaceSelected.getDistance()));

                ivDetailPlace.setOnClickListener(MapsActivity.this);
                findViewById(R.id.ivCall).setOnClickListener(MapsActivity.this);
                findViewById(R.id.ivWebsite).setOnClickListener(MapsActivity.this);
                findViewById(R.id.ivComment).setOnClickListener(MapsActivity.this);

                // favorite
                ImageView ivFavorite = (ImageView) findViewById(R.id.ivFavorite);
                PlaceDbHelper placeDbHelper = new PlaceDbHelper(MapsActivity.this, null);
                Place favoritePlace = placeDbHelper.getFavoritePlace(mPlaceSelected.getId());
                if (favoritePlace != null && favoritePlace.getFavorite() == 1)
                    ivFavorite.setImageResource(R.drawable.new_ic_bookmarked);
                else
                    ivFavorite.setImageResource(R.drawable.new_ic_bookmark_star);
                ivFavorite.setOnClickListener(MapsActivity.this);

                // get review
                DownloadJSONStringTask task = new DownloadJSONStringTask(MapsActivity.this, true);
                task.execute(Util.URL_GET_ALL_REVIEWS_BY_PLACE_ID + "?placeId=" + mPlaceSelected.getId());
            }
        });

        mSlidingDrawerResultsDetail.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                ImageView ivDetailPlaceThumb = (ImageView) findViewById(R.id.ivDetailPlaceThumb);
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT, 4.0f);
                ivDetailPlaceThumb.setLayoutParams(param);
                ivDetailPlaceThumb.setImageResource(Util.getImageResourceID(mPlaceSelected.getCategoryId()));
            }
        });

        mSlidingDrawerDirection.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                btnDriving = (ImageView) findViewById(R.id.ivDriving);
                btnWalking = (ImageView) findViewById(R.id.ivWalking);
                btnDriving.setAlpha(1f);
                btnWalking.setAlpha(ALPHA_IMAGEVIEW);

                tvOrigin = (TextView) findViewById(R.id.tvOrigin);
                tvDestination = (TextView) findViewById(R.id.tvDestination);
                tvDestination.setText(mPlaceSelected.getName());

                btnExchangeRoute = (ImageView) findViewById(R.id.ivExchangeRoute);
                btnExchangeRoute.setAlpha(ALPHA_IMAGEVIEW);
                btnExchangeRoute.setOnClickListener(MapsActivity.this);
            }
        });
    }

    // onClick
    public void openMenuDrawerOrBackToMaps(View v) {
        if (mIsMapsMODE) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        } else {
            // back to maps
            setMapsModeDefaultInfo();
            mSearchView.setIconified(true);

            MyFragmentManager.backToPreviousFragment();
            mMapsFragment.clearMap();
        }
    }

    // maps directions
    public void direct(View v) {
        if (mCurrentLocation != null) {
            mIsDrivingMode = true;
            MapsDirections mapsDirections = new MapsDirections(mParserCompleteListener);
            if (mIsReverseRoute)
                mapsDirections.direct(mDestination, mCurrentLocation, TRAVEL_MODES.DRIVING);
            else
                mapsDirections.direct(mCurrentLocation, mDestination, TRAVEL_MODES.DRIVING);
            mSlidingDrawerResultsDetail.close();
            mSlidingDrawerResultsDetail.setVisibility(View.INVISIBLE);
            mSlidingDrawerDirection.setVisibility(View.VISIBLE);
            mSlidingDrawerDirection.open();
        }
    }

    public void drivingDirection(View v) {
        if (mIsDrivingMode) {
            if (mCurrentLocation != null) {
                MapsDirections mapsDirections = new MapsDirections(mParserCompleteListener);
                if (mIsReverseRoute)
                    mapsDirections.direct(mDestination, mCurrentLocation, TRAVEL_MODES.DRIVING);
                else
                    mapsDirections.direct(mCurrentLocation, mDestination, TRAVEL_MODES.DRIVING);
                btnDriving.setEnabled(false);
                btnDriving.setAlpha(1f);
                btnWalking.setEnabled(false);
                btnWalking.setAlpha(ALPHA_IMAGEVIEW);
                btnExchangeRoute.setEnabled(false);
                btnExchangeRoute.setAlpha(ALPHA_IMAGEVIEW);
            }
        }
    }

    public void walkingDirection(View v) {
        if (!mIsDrivingMode) {
            if (mCurrentLocation != null) {
                MapsDirections mapsDirections = new MapsDirections(mParserCompleteListener);
                if (mIsReverseRoute)
                    mapsDirections.direct(mDestination, mCurrentLocation, TRAVEL_MODES.WALKING);
                else
                    mapsDirections.direct(mCurrentLocation, mDestination, TRAVEL_MODES.WALKING);
                btnDriving.setEnabled(false);
                btnDriving.setAlpha(ALPHA_IMAGEVIEW);
                btnWalking.setEnabled(false);
                btnWalking.setAlpha(1f);
                btnExchangeRoute.setEnabled(false);
                btnExchangeRoute.setAlpha(ALPHA_IMAGEVIEW);
            }
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

                // add onParserCompleteListener (for direction)
                mParserCompleteListener = mMapsFragment;
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
        // add OnParserCompleteListener (for direction)
        mParserCompleteListener = mMapsFragment;

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

    public LatLng getDestination() {
        return mDestination;
    }

    public void showToastMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        if (mSlidingDrawerResultsDetail.getVisibility() == View.VISIBLE) {
            if (mSlidingDrawerResultsDetail.isOpened())
                mSlidingDrawerResultsDetail.close();
            mSlidingDrawerResultsDetail.setVisibility(View.INVISIBLE);

            resultTitle = tvResult.getText().toString();
            resultTitle = resultTitle.substring(resultTitle.indexOf("\"") + 1, resultTitle.lastIndexOf("\""));
            mSlidingDrawerResults.setVisibility(View.VISIBLE);
            mSlidingDrawerResults.open();
        } else if (mSlidingDrawerResults.getVisibility() == View.VISIBLE) {
            if (mSlidingDrawerResults.isOpened())
                mSlidingDrawerResults.close();
            mSlidingDrawerResults.setVisibility(View.INVISIBLE);

            mSearchView.setQuery("", false);
            mSearchView.setFocusable(false);
            mSearchView.clearFocus();
            mSearchView.setIconified(true);

            mMapsFragment.clearMap();
        } else if (mSlidingDrawerDirection.getVisibility() == View.VISIBLE) {

            if (mSlidingDrawerDirection.isOpened())
                mSlidingDrawerDirection.close();
            mSlidingDrawerDirection.setVisibility(View.INVISIBLE);

            mSlidingDrawerResultsDetail.setVisibility(View.VISIBLE);
            mSlidingDrawerResultsDetail.open();

        } else
            super.onBackPressed();
    }
}
