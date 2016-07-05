package com.motthoidecode.findplacesnearby;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;
import databases.PlaceDbHelper;
import model.MapStateManager;
import model.Place;
import utils.Util;

public class AddPlaceActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private static final float DEFAULT_ZOOM = 14;
    private static final int TIME_OUT = 40;
    private static final int OPEN_SELECT_PLACE_ACTIVITY = 1;
    public static final int RESULT_CODE_PLACE_SELECTED = 2;

    private static final int REQUEST_CODE_CAMERA_CAPTURE_IMAGE = 10;
    private static final int REQUEST_CODE_SELECT_IMAGE_FROM_GALLERY = 20;

    private Uri fileUri; // file url to store image/video

    private Spinner mSpPlaceCAtegory;
    private MapView mMapView;
    private GoogleMap mMap;
    private Location mCurrentLocation;
    private LatLng mSelectedLatLng;

    private int mCategoryId = 1;

    private EditText etPlaceName, etPlaceAddress, etPlacePhone, etPlaceWebsite;
    private ImageView mImageView;

    private Bitmap mSelectedBitmap;

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
                setCurrentLocation();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etPlaceName = (EditText) findViewById(R.id.etPlaceName);
        etPlaceAddress = (EditText) findViewById(R.id.etPlaceAddress);
        etPlacePhone = (EditText) findViewById(R.id.etPlacePhone);
        etPlaceWebsite = (EditText) findViewById(R.id.etPlaceWebsite);
        mImageView = (ImageView) findViewById(R.id.imageView);

        mSpPlaceCAtegory = (Spinner) findViewById(R.id.spPlaceCAtegory);
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Util.getArrayCategoryName());
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpPlaceCAtegory.setAdapter(adapterSpinner);

        mSpPlaceCAtegory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCategoryId = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mMapView = (MapView) findViewById(R.id.mapView);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this);

        mCurrentLocation = null;
        mHandler.postDelayed(mGetMyLocation, 100);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Intent selectPlaceIntent = new Intent(this, SelectPlaceLocationActivity.class);
        selectPlaceIntent.putExtra(Util.KEY_LATITUDE, mCurrentLocation.getLatitude());
        selectPlaceIntent.putExtra(Util.KEY_LONGITUDE, mCurrentLocation.getLongitude());
        startActivityForResult(selectPlaceIntent, OPEN_SELECT_PLACE_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OPEN_SELECT_PLACE_ACTIVITY && resultCode == RESULT_CODE_PLACE_SELECTED) {
            double lat = data.getDoubleExtra(Util.KEY_LATITUDE, 0);
            double lng = data.getDoubleExtra(Util.KEY_LONGITUDE, 0);
            if (lat != 0) {
                mSelectedLatLng = new LatLng(lat, lng);
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(mSelectedLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_poi_selected)));
                findViewById(R.id.mapViewLine).setVisibility(View.INVISIBLE);
            }
        }

        if (requestCode == REQUEST_CODE_CAMERA_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            mSelectedBitmap = null;
            mSelectedBitmap = (Bitmap) data.getExtras().get("data");
            Uri imageUri = data.getData();
            mSelectedBitmap = rotateImageIfRequired(mSelectedBitmap, imageUri);
            mSelectedBitmap = scaleBitmap(mSelectedBitmap,640,360,true);
            mImageView.setImageBitmap(mSelectedBitmap);
            findViewById(R.id.imageViewLine).setVisibility(View.INVISIBLE);
        }

        if (requestCode == REQUEST_CODE_SELECT_IMAGE_FROM_GALLERY && resultCode == RESULT_OK) {
            mSelectedBitmap = null;
            if (data != null) {
                try {
                    mSelectedBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                    mSelectedBitmap = scaleBitmap(mSelectedBitmap,640,360,true);
                    mImageView.setImageBitmap(mSelectedBitmap);
                    findViewById(R.id.imageViewLine).setVisibility(View.INVISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void setCurrentLocation() {
        if (mCurrentLocation == null) {
            getSavedMapState();
        } else {
            // Move the captureImage
            LatLng mLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, DEFAULT_ZOOM));
        }
    }

    private void getSavedMapState() {
        MapStateManager mSM = new MapStateManager(this);
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

    public void captureImage(View v) {
        // Kiểm tra Camera trong thiết bị
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // Mở camera mặc định
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            // Tiến hành gọi Capture Image intent
            startActivityForResult(intent, REQUEST_CODE_CAMERA_CAPTURE_IMAGE);
        } else {
            Toast.makeText(getApplication(), "Camera không được hỗ trợ", Toast.LENGTH_LONG).show();
        }
    }

    public void selectImageFromGallery(View v) {
        //open album to select image
        Intent gallaryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallaryIntent, REQUEST_CODE_SELECT_IMAGE_FROM_GALLERY);
    }

    public void savePlace(View v) {
        String name = etPlaceName.getText().toString().trim();
        if (name.length() == 0) {
            etPlaceName.setError("");
            etPlaceName.requestFocus();
            return;
        }
        String address = etPlaceAddress.getText().toString().trim();
        if (address.length() == 0) {
            etPlaceAddress.setError("");
            etPlaceAddress.requestFocus();
            return;
        }
        if (mSelectedLatLng == null) {
            View errorLine = findViewById(R.id.mapViewLine);
            errorLine.setVisibility(View.VISIBLE);
            errorLine.requestFocus();
            return;
        }
        if (mSelectedBitmap == null) {
            View errorLine = findViewById(R.id.imageViewLine);
            errorLine.setVisibility(View.VISIBLE);
            errorLine.requestFocus();
            return;
        }
        String phone = etPlacePhone.getText().toString().trim();
        String website = etPlaceWebsite.getText().toString().trim();

        Place place = new Place();
        place.setName(name);
        place.setAddress(address);
        place.setCategoryId(mCategoryId);
        place.setLatitude(mSelectedLatLng.latitude);
        place.setLongitude(mSelectedLatLng.longitude);
        place.setPhone(phone);
        place.setWebsite(website);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mSelectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        byte[] array = stream.toByteArray();
        String encoded_string = Base64.encodeToString(array, Base64.DEFAULT);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String image_name = "IMG_" + timeStamp + ".jpg";

        uploadToServer(image_name, encoded_string, place);

    }

    private void uploadToServer(final String image_name, final String encoded_string, final Place place) {

        final ProgressDialog dialog = new ProgressDialog(AddPlaceActivity.this);
        dialog.setTitle("Please wait...");
        dialog.setMessage("Saving place...");
        dialog.show();

        AsyncHttpClient client = new AsyncHttpClient();
        client.setConnectTimeout(1000 * 30);
        client.setResponseTimeout(1000 * 20);
        RequestParams params = new RequestParams();

        params.put("imagename", image_name);
        params.put("name", place.getName());
        params.put("address", place.getAddress());
        params.put("categoryId", String.valueOf(place.getCategoryId()));
        params.put("phone", place.getPhone());
        params.put("website", place.getWebsite());
        params.put("latitude", Double.toString(place.getLatitude()));
        params.put("longitude", Double.toString(place.getLongitude()));
        params.put("base64", encoded_string);
        client.post(Util.URL_ADD_NEW_PLACE, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {
                if (dialog.isShowing()) dialog.dismiss();
                // TODO Auto-generated method stub
                try {
                    int success = response.getInt("success");
                    if (success == 1) {
                        showToastMessage("Thêm mới địa điểm thành công");
                        PlaceDbHelper placeDbHelper = new PlaceDbHelper(AddPlaceActivity.this, null);
                        placeDbHelper.addMyPlace(place);
                        if (dialog.isShowing()) dialog.dismiss();
                        AddPlaceActivity.this.setResult(MyPlacesActivity.RESULT_ADD_PLACE_OK);
                        AddPlaceActivity.this.finish();
                    } else {
                        String msg = response.getString("message");
                        showToastMessage(msg.substring(0, msg.length() > 50 ? 50 : msg.length()));
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    String msg = e.toString();
                    showToastMessage(msg.substring(0, msg.length() > 50 ? 50 : msg.length()));
                    if (dialog.isShowing()) dialog.dismiss();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                if (errorResponse != null)
                    Log.v("Error", errorResponse.toString());
                showToastMessage("Error!");
                if (dialog.isShowing()) dialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                if (responseString != null)
                    Log.v("Error", responseString);
                showToastMessage("Error!");
                if (dialog.isShowing()) dialog.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                if (errorResponse != null)
                    Log.v("Error", errorResponse.toString());
                showToastMessage("Error!");
                if (dialog.isShowing()) dialog.dismiss();
            }
        });
    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) {

        // Detect rotation
        int rotation = getRotation(selectedImage);
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
            img.recycle();
            return rotatedImg;
        } else {
            return img;
        }
    }

    /**
     * @return
     */
    private int getRotation(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int rotation = 0;
        rotation = cursor.getInt(0);
        cursor.close();
        return rotation;
    }

    public Bitmap scaleBitmap(Bitmap realImage, int maxImageWith, int maxImageHeight, boolean filter) {

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, maxImageWith,
                maxImageHeight, filter);
        return newBitmap;
    }

    public void showToastMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

}
