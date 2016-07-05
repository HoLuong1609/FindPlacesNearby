package com.motthoidecode.findplacesnearby;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import utils.Util;

public class SelectPlaceLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener{

    private static final float DEFAULT_ZOOM = 15;

    private MapView mMapView;
    private GoogleMap mMap;
    private LatLng mCurrentLocation;
    private LatLng mSelectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_place_location);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        double lat = getIntent().getDoubleExtra(Util.KEY_LATITUDE,0);
        double lng = getIntent().getDoubleExtra(Util.KEY_LONGITUDE,0);
        mCurrentLocation = new LatLng(lat,lng);

        mMapView = (MapView) findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_select_place_location,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_select:
                Intent intent = new Intent();
                if(mSelectedLocation != null){
                    intent.putExtra(Util.KEY_LATITUDE,mSelectedLocation.latitude);
                    intent.putExtra(Util.KEY_LONGITUDE,mSelectedLocation.longitude);
                    this.setResult(AddPlaceActivity.RESULT_CODE_PLACE_SELECTED,intent);
                }
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this);

        // Move the captureImage
        LatLng mLatLng = new LatLng(mCurrentLocation.latitude, mCurrentLocation.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, DEFAULT_ZOOM));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_poi_selected)));
        mSelectedLocation = latLng;
    }
}
