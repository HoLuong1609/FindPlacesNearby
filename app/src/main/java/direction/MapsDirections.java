package direction;

import com.google.android.gms.maps.model.LatLng;

import direction.DirectionsJSONParserTask.OnJSONParserCompleteListener;

/**
 * Created by Ran on 12/05/2016.
 */
public class MapsDirections {

   public enum TRAVEL_MODES {
        DRIVING,
        WALKING
    }

    // TravelModes
    private static final String DRIVING = "driving";
    private static final String WALKING = "walking";

    private OnJSONParserCompleteListener mParserCompleteListener;

    public MapsDirections(OnJSONParserCompleteListener listener) {
        mParserCompleteListener = listener;
    }

    public void direct(LatLng origin, LatLng dest, TRAVEL_MODES MODE) {

        // Getting URL to the Google Directions API
        String strUrl = getDirectionsUrl(origin, dest, MODE);

        DownloadJsonDataTask downloadJsonDataTask = new DownloadJsonDataTask(mParserCompleteListener);

        // Start downloading json data from Google Directions API
        downloadJsonDataTask.execute(strUrl);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest, TRAVEL_MODES MODE) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // travelMode
        String travelModes;
        if (MODE == TRAVEL_MODES.DRIVING)
            travelModes = "mode=" + DRIVING;
        else
            travelModes = "mode=" + WALKING;

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + travelModes;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

}
