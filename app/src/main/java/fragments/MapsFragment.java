package fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.motthoidecode.findplacesnearby.MapsActivity;
import com.motthoidecode.findplacesnearby.R;

import model.MapStateManager;
import utils.Util;

/**
 * Created by Administrator on 6/30/2016.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener {

    private static final float DEFAULT_ZOOM = 15;
    private static final int TIME_OUT = 40;
    private static final long MIN_TIME = 400;

    private static GoogleMap mMap;
    private MapsActivity mMapsActivity;

    private Location mCurrentLocation;
    private LocationManager mLocationManager;

    private Handler mHandler = new Handler();
    private int count = 0;
    private Runnable mGetMyLocation = new Runnable() {
        @Override
        public void run() {
            count++;
            if (mMap == null)
                return;
            mCurrentLocation = mMap.getMyLocation();
            if (mCurrentLocation == null && count < TIME_OUT) {
                mHandler.postDelayed(this, 100);
            } else {
                if (mCurrentLocation == null) {
                    getSavedMapState();
                } else {
                    // Add a marker in current Location and move the captureImage
                    LatLng mLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, DEFAULT_ZOOM));
                    mMapsActivity.setCurrentLocation(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                }
            }
        }
    };

    public MapsFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mMapsActivity = (MapsActivity) activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.maps_fragment, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();
    }

    @Override
    public void onResume() {
        super.onResume();
        String provider = mLocationManager.getBestProvider(new Criteria(), false);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.requestLocationUpdates(provider, MIN_TIME, 1, this);
    }

    @Override
    public void onDestroy() {
        if (mMap != null) {
            MapStateManager mSM = new MapStateManager(getContext());
            mSM.saveMapState(mMap);
        }
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Save maps state
        if (mMap != null) {
            MapStateManager mSM = new MapStateManager(getContext());
            mSM.saveMapState(mMap);
        }
        mCurrentLocation = location;
        mMapsActivity.setCurrentLocation(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}


    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        Bundle b = this.getArguments();
        if (b.getBoolean(Util.KEY_OPEN_MAPS_FOR_THE_FIRST_TIME, false)) {
            mCurrentLocation = null;
            mHandler.postDelayed(mGetMyLocation, 100);
        }
    }

    public void clearMap() {
        mMap.clear();
    }

    private void getSavedMapState() {
        MapStateManager mSM = new MapStateManager(getContext());
        CameraPosition position = mSM.getSavedCameraPosition();
        if (position != null) {
            LatLng latLng = mSM.getSavedLatLng();
            mCurrentLocation = new Location("");
            mCurrentLocation.setLatitude(latLng.latitude);
            mCurrentLocation.setLongitude(latLng.longitude);

            mMap.setMapType(mSM.getSavedMapType());
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
            mMap.moveCamera(update);
        }
    }

}
