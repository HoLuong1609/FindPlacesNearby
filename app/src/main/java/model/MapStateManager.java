package model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ran on 5/6/2016.
 */
public class MapStateManager {
    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String ZOOM = "zoom";
    private static final String BEARING = "bearing";
    private static final String TILT = "tilt";
    private static final String MAPTYPE = "MAPTYPE";

    private static final String PREFS_NAME = "mapCameraState";

    private SharedPreferences mapStatePrefs;

    public MapStateManager(Context context){
        mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveMapState(GoogleMap map){
        SharedPreferences.Editor editor = mapStatePrefs.edit();
        CameraPosition cameraPosition = map.getCameraPosition();

        editor.putFloat(LONGITUDE, (float) cameraPosition.target.longitude);
        editor.putFloat(LATITUDE, (float) cameraPosition.target.latitude);
        editor.putFloat(ZOOM, cameraPosition.zoom);
        editor.putFloat(BEARING, cameraPosition.bearing);
        editor.putFloat(TILT, cameraPosition.tilt);
        editor.putInt(MAPTYPE,map.getMapType());

        editor.commit();
    }

    public CameraPosition getSavedCameraPosition(){
        float longitude = mapStatePrefs.getFloat(LONGITUDE, 0);
        if(longitude == 0)
            return null;
        float latitude = mapStatePrefs.getFloat(LATITUDE, 0);
        LatLng target = new LatLng(latitude,longitude);
        float zoom = mapStatePrefs.getFloat(ZOOM, 0);
        float bearing = mapStatePrefs.getFloat(BEARING, 0);
        float tilt = mapStatePrefs.getFloat(TILT, 0);
        return new CameraPosition(target,zoom,tilt,bearing);
    }

    public int getSavedMapType(){
        return mapStatePrefs.getInt(MAPTYPE,0);
    }

    public LatLng getSavedLatLng(){
        return  new LatLng(mapStatePrefs.getFloat(LATITUDE,0),mapStatePrefs.getFloat(LONGITUDE,0));
    }
}
