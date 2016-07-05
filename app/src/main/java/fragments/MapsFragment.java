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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.motthoidecode.findplacesnearby.MapsActivity;
import com.motthoidecode.findplacesnearby.R;

import java.util.ArrayList;
import java.util.List;

import direction.DirectionsJSONParserTask.OnJSONParserCompleteListener;
import direction.DirectionsJSONParserTask;
import model.MapStateManager;
import utils.Util;

/**
 * Created by Administrator on 6/30/2016.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener, OnJSONParserCompleteListener {

    private static final float DEFAULT_ZOOM = 15;
    private static final int TIME_OUT = 40;
    private static final long MIN_TIME = 400;
    private static final int PADDING = 100;

    private static GoogleMap mMap;
    private MapsActivity mMapsActivity;

    private Location mCurrentLocation;
    private LocationManager mLocationManager;

    private List<MarkerOptions> mMarkersOptions = new ArrayList<MarkerOptions>();
    private List<Marker> mListMarker;
    private List<LatLng> mCoordinates;
    private Marker mMarkerSelected;

    private boolean mIsAnimate = false;

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

    // For drawing direction on Map
    private Polyline mPolyline;

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

    // On Direct Complete
    @Override
    public void onJSONParserComplete(DirectionsJSONParserTask task) {
        // Drawing polyline in the Google Map for the i-th route
        if (mPolyline != null)
            mPolyline.remove();
        if (!task.isError() && !task.isNoPoints()) {
            mPolyline = mMap.addPolyline(task.getPolylineOptions());

            List<MarkerOptions> markers = new ArrayList<MarkerOptions>();
            markers.add(new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())));
            markers.add(new MarkerOptions().position(mMapsActivity.getDestination()));
            mIsAnimate = true;
            zoomMapsToShowAllTheMarkers(markers);
        }

        // Set OnDirectCompleteListener to MapsActivity
        mMapsActivity.onDirectComplete(task.getDistance(), task.getDuration(), task.isError(), task.isNoPoints());

    }

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

    public void showAllTheResults(Bundle data) {

        mCurrentLocation = mMap.getMyLocation();
        if (mCurrentLocation == null) {
            MapStateManager mSM = new MapStateManager(getContext());
            LatLng latLng = mSM.getSavedLatLng();
            if (latLng != null) {
                mCurrentLocation = new Location("");
                mCurrentLocation.setLatitude(latLng.latitude);
                mCurrentLocation.setLongitude(latLng.longitude);
                mMapsActivity.setCurrentLocation(latLng);
            }
        }
        if (mCurrentLocation == null)
            return;

        ArrayList<String> placeLocations = data.getStringArrayList(Util.KEY_PLACE_LOCATIONS);
        setListCoordinates(placeLocations);

        if (mCoordinates.size() > 0) {
            mMarkersOptions.clear();
            mMarkersOptions.add(new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())));
            for (int i = 0; i < mCoordinates.size(); i++) {
                if (i == 0)
                    mMarkersOptions.add(new MarkerOptions().position(new LatLng(mCoordinates.get(i).latitude, mCoordinates.get(i).longitude)).
                            icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_poi_selected)));
                else
                    mMarkersOptions.add(new MarkerOptions().position(new LatLng(mCoordinates.get(i).latitude, mCoordinates.get(i).longitude)).
                            icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_poi)));
            }

            mListMarker = new ArrayList<Marker>();
            mMap.clear();
            for (int i = 1; i < mMarkersOptions.size(); i++) {
                Marker marker = mMap.addMarker(mMarkersOptions.get(i).snippet(String.valueOf(i - 1)));
                mListMarker.add(marker);
            }
            mMarkerSelected = mListMarker.get(0);

            mIsAnimate = false;
            zoomMapsToShowAllTheMarkers(mMarkersOptions);
        }
    }

    public void showTheSuggestion(Bundle data) {

        mCurrentLocation = mMap.getMyLocation();
        if (mCurrentLocation == null) {
            MapStateManager mSM = new MapStateManager(getContext());
            LatLng latLng = mSM.getSavedLatLng();
            if (latLng != null) {
                mCurrentLocation = new Location("");
                mCurrentLocation.setLatitude(latLng.latitude);
                mCurrentLocation.setLongitude(latLng.longitude);
                mMapsActivity.setCurrentLocation(latLng);
            }
        }
        if (mCurrentLocation == null)
            return;

        String placeLocation = data.getString(Util.KEY_PLACE_LOCATION);
        ArrayList<String> placeLocations = new ArrayList<>();
        placeLocations.add(placeLocation);
        Log.v("placeLocation", placeLocation + " - " + placeLocations);
        setListCoordinates(placeLocations);

        if (mCoordinates.size() > 0) {
            mMarkersOptions.clear();
            mMarkersOptions.add(new MarkerOptions().position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude())));
            for (int i = 0; i < mCoordinates.size(); i++) {
                if (i == 0)
                    mMarkersOptions.add(new MarkerOptions().position(new LatLng(mCoordinates.get(i).latitude, mCoordinates.get(i).longitude)).
                            icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_poi_selected)));
                else
                    mMarkersOptions.add(new MarkerOptions().position(new LatLng(mCoordinates.get(i).latitude, mCoordinates.get(i).longitude)).
                            icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_poi)));
            }

            mListMarker = new ArrayList<Marker>();
            mMap.clear();
            for (int i = 1; i < mMarkersOptions.size(); i++) {
                Marker marker = mMap.addMarker(mMarkersOptions.get(i).snippet(String.valueOf(i - 1)));
                mListMarker.add(marker);
            }
            mMarkerSelected = mListMarker.get(0);

            mIsAnimate = false;
            zoomMapsToShowAllTheMarkers(mMarkersOptions);
        }
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

    public void setMarkerSelected(int markerPosition) {
        mMarkerSelected.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_poi));
        Marker newSelectedMarker = mListMarker.get(markerPosition);
        newSelectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_poi_selected));
        mMarkerSelected = newSelectedMarker;
    }

    // parse Coordinates from ArrayList String
    private void setListCoordinates(ArrayList<String> placeLocations) {
        mCoordinates = new ArrayList<LatLng>();
        for (int i = 0; i < placeLocations.size(); i++) {
            String placeLocation = placeLocations.get(i);
            double lat = Double.parseDouble(placeLocation.substring(0, placeLocation.indexOf(",")));
            double lng = Double.parseDouble(placeLocation.substring(placeLocation.indexOf(",") + 1, placeLocation.length()));
            mCoordinates.add(new LatLng(lat, lng));
        }
    }

    private void zoomMapsToShowAllTheMarkers(List<MarkerOptions> markers) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (MarkerOptions marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = PADDING; // offset from edges of the map in pixels
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        if (mIsAnimate)
            mMap.animateCamera(cameraUpdate);
        else
            mMap.moveCamera(cameraUpdate);
    }

}
