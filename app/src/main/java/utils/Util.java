package utils;

import android.content.Context;
import android.preference.PreferenceManager;

import com.motthoidecode.findplacesnearby.R;

/**
 * Created by Administrator on 7/2/2016.
 */
public class Util {
    public static final String KEY_QUERY_CATEGORY = "QueryCategory";
    public static final String KEY_OPEN_MAPS_FOR_THE_FIRST_TIME = "OpenMapsForTheFirstTime";

    public static final int ID_RESTAURANT = 1;
    public static final int ID_COFFEE = 2;
    public static final int ID_ATM = 3;
    public static final int ID_PETROL = 4;
    public static final int ID_EDUCATION = 5;

    public static final String RESTAURANT = "Restaurant";
    public static final String COFFEE = "Coffee";
    public static final String ATM = "ATM";
    public static final String PETROL = "Petrol";
    public static final String EDUCATION = "Education";

    public static final String URL_PREFIX = "http://nearby.16mb.com";
    public static final String URL_QUERY_BY_CATEGORY_ID = URL_PREFIX + "/findPlacesNearMeByCategoryId.php";
    public static final String URL_QUERY_BY_NAME = URL_PREFIX + "/findPlacesNearMeByName.php";
    public static final String URL_ADD_NEW_PLACE = URL_PREFIX + "/addNewPlace.php";
    public static final String URL_ADD_REVIEW = URL_PREFIX + "/addReview.php";
    public static final String URL_GET_ALL_REVIEWS_BY_PLACE_ID = URL_PREFIX + "/getAllReviewsByPlaceId.php";

    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_CATEGORY_ID = "categoryId";
    public static final String KEY_IMAGE_URL = "imageUrl";
    public static final String KEY_WEBSITE = "website";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_PLACE_LOCATIONS = "placeLocations";
    public static final String KEY_PLACE_LOCATION = "placeLocation";
    public static final String KEY_DISTANCE = "distance";

    public static final String KEY_RADIUS_PICKER = "RADIUS_PICKER";
    public static final String UNKOWN = "Không xác định";
    public static final String KEY_CONTENT = "content";

    public static int getImageResourceID(int categoryID) {
        int imageResID = 0;
        switch (categoryID) {
            case ID_RESTAURANT:
                imageResID = R.drawable.ic_category_green_36dp_restaurant;
                break;
            case ID_COFFEE:
                imageResID = R.drawable.ic_category_green_36dp_cafe;
                break;
            case ID_ATM:
                imageResID = R.drawable.ic_category_green_36dp_atm;
                break;
            case ID_PETROL:
                imageResID = R.drawable.ic_category_green_36dp_petrol;
                break;
            case ID_EDUCATION:
                imageResID = R.drawable.ic_category_green_36dp_education;
                break;
            case 0:
                imageResID = R.drawable.ic_category_green_36dp_unknown;
        }
        return imageResID;
    }

    public static int getSuggestionImageResourceID(int categoryID) {
        int imageResID = 0;
        switch (categoryID) {
            case ID_RESTAURANT:
                imageResID = R.drawable.ic_category_gray_24dp_restaurant;
                break;
            case ID_COFFEE:
                imageResID = R.drawable.ic_category_gray_24dp_cafe;
                break;
            case ID_ATM:
                imageResID = R.drawable.ic_category_gray_24dp_atm;
                break;
            case ID_PETROL:
                imageResID = R.drawable.ic_category_gray_24dp_petrol;
                break;
            case ID_EDUCATION:
                imageResID = R.drawable.ic_category_gray_24dp_education;
                break;
            case 0:
                imageResID = R.drawable.ic_category_gray_24dp_unknown;
        }
        return imageResID;
    }

    public static String getCategoryName(int categoryID) {
        String categoryName = "";
        switch (categoryID) {
            case ID_RESTAURANT:
                categoryName = RESTAURANT;
                break;
            case ID_COFFEE:
                categoryName = COFFEE;
                break;
            case ID_ATM:
                categoryName = ATM;
                break;
            case ID_PETROL:
                categoryName = PETROL;
                break;
            case ID_EDUCATION:
                categoryName = EDUCATION;
                break;
            case 0:
                categoryName = UNKOWN;
        }
        return categoryName;
    }

    public static String[] getArrayCategoryName() {
        String[] arr = {RESTAURANT, COFFEE, ATM, PETROL, EDUCATION};
        return arr;
    }

    public static String formatDistance(double distance) {
        return String.valueOf((double) ((int) (distance * 100)) / 100) + " Km";
    }

    public static String getMaxDistance(Context context) {    // in Km
        return String.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY_RADIUS_PICKER, 5));
    }

    public static double getDistanceFromLatLonInKm(double lat1,double lon1,double lat2,double lon2) {
        double R = 6371.0008; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d;
    }

    private static double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }
}
