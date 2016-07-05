package databases;

/**
 * Created by Ran on 25/05/2016.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import model.Place;
import utils.Util;

/**
 * Created by Ran on 4/20/2016.
 */
public class PlaceDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "data";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_FAVORITE_PLACE = "favoritePlace";
    public static final String TABLE_MY_PLACE = "myPlace";
    private static final String KEY_FAVORITE = "favorite";

    public static final String TABLE_FAVORITE_PLACE_CREATE = "CREATE TABLE " + TABLE_FAVORITE_PLACE
            + " (" + Util.KEY_ID + " INTEGER PRIMARY KEY, " + Util.KEY_NAME + " TEXT NOT NULL, "
            + Util.KEY_ADDRESS + " TEXT NOT NULL, " + Util.KEY_LATITUDE + " REAL, "
            + Util.KEY_LONGITUDE + " REAL, " + Util.KEY_CATEGORY_ID + " INTEGER, "
            + Util.KEY_IMAGE_URL + " TEXT, "
            + Util.KEY_PHONE + " TEXT, " + Util.KEY_WEBSITE + " TEXT, " + KEY_FAVORITE + " INTEGER);";

    public static final String TABLE_MY_PLACE_CREATE = "CREATE TABLE " + TABLE_MY_PLACE
            + " (" + Util.KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Util.KEY_NAME + " TEXT NOT NULL, "
            + Util.KEY_ADDRESS + " TEXT NOT NULL, " + Util.KEY_LATITUDE + " REAL, "
            + Util.KEY_LONGITUDE + " REAL, " + Util.KEY_CATEGORY_ID + " INTEGER, "
            + Util.KEY_IMAGE_URL + " TEXT, "
            + Util.KEY_PHONE + " TEXT, " + Util.KEY_WEBSITE + " TEXT);";

    private static final String TAG = "PlaceDbHelper";

    public PlaceDbHelper(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_FAVORITE_PLACE_CREATE);
        db.execSQL(TABLE_MY_PLACE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITE_PLACE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MY_PLACE);
        onCreate(db);
    }

    public List<Place> getListFavoritePlaces() {
        List<Place> places = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_FAVORITE_PLACE, new String[]{Util.KEY_ID, Util.KEY_NAME, Util.KEY_ADDRESS, Util.KEY_CATEGORY_ID,Util.KEY_IMAGE_URL,
                        Util.KEY_LATITUDE, Util.KEY_LONGITUDE,Util.KEY_PHONE, Util.KEY_WEBSITE, KEY_FAVORITE},
                null, null, null, null, Util.KEY_ID);

        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndex(Util.KEY_ID));
                String name = c.getString(c.getColumnIndex(Util.KEY_NAME));
                String address = c.getString(c.getColumnIndex(Util.KEY_ADDRESS));
                int categoryId = c.getInt(c.getColumnIndex(Util.KEY_CATEGORY_ID));
                double lat = c.getDouble(c.getColumnIndex(Util.KEY_LATITUDE));
                double lng = c.getDouble(c.getColumnIndex(Util.KEY_LONGITUDE));
                String imageUrl = c.getString(c.getColumnIndex(Util.KEY_IMAGE_URL));
                String phone = c.getString(c.getColumnIndex(Util.KEY_PHONE));
                String website = c.getString(c.getColumnIndex(Util.KEY_WEBSITE));
                int favorite = c.getInt(c.getColumnIndex(KEY_FAVORITE));

                Place place = new Place(id,name,address,lat,lng,categoryId,imageUrl,website, phone,0);
                place.setFavorite(favorite);
                places.add(place);
            } while (c.moveToNext());
            c.close();
        }

        db.close();
        return places;
    }

    public Place getFavoritePlace(int placeId){
        Place place;
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_FAVORITE_PLACE + " WHERE "
                + Util.KEY_ID + " = " +  placeId;
        Cursor c = db.rawQuery(sql,null);
        if(c.moveToFirst()){
            String name = c.getString(c.getColumnIndex(Util.KEY_NAME));
            String address = c.getString(c.getColumnIndex(Util.KEY_ADDRESS));
            int categoryId = c.getInt(c.getColumnIndex(Util.KEY_CATEGORY_ID));
            double lat = c.getDouble(c.getColumnIndex(Util.KEY_LATITUDE));
            double lng = c.getDouble(c.getColumnIndex(Util.KEY_LONGITUDE));
            String imageUrl = c.getString(c.getColumnIndex(Util.KEY_IMAGE_URL));
            String phone = c.getString(c.getColumnIndex(Util.KEY_PHONE));
            String website = c.getString(c.getColumnIndex(Util.KEY_WEBSITE));
            int favorite = c.getInt(c.getColumnIndex(KEY_FAVORITE));
            place = new Place(placeId,name,address,lat,lng,categoryId,imageUrl,website, phone,0);
            place.setFavorite(favorite);
        }else
            place = null;
        c.close();
        db.close();
        return place;
    }

    public int addFavoritePlace(Place place) {
        int ret = -1;
        ContentValues values = new ContentValues();
        values.put(Util.KEY_ID,place.getId());
        values.put(Util.KEY_NAME, place.getName());
        values.put(Util.KEY_ADDRESS, place.getAddress());
        values.put(Util.KEY_CATEGORY_ID, place.getCategoryId());
        values.put(Util.KEY_LATITUDE, place.getLatitude());
        values.put(Util.KEY_LONGITUDE, place.getLongitude());
        values.put(Util.KEY_IMAGE_URL, place.getImageUrl());
        values.put(Util.KEY_WEBSITE, place.getWebsite());
        values.put(Util.KEY_PHONE,place.getPhone());
        values.put(KEY_FAVORITE,place.getFavorite());

        SQLiteDatabase db = this.getWritableDatabase();
        ret = (int) db.insert(TABLE_FAVORITE_PLACE, null, values);
        db.close();
        return ret;
    }

    public int deleteFavoritePlace(int placeId){
        int ret;
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ret = db.delete(TABLE_FAVORITE_PLACE,Util.KEY_ID + "=?",new String[]{String.valueOf(placeId)});
        }catch (Exception e){
            ret = -1;
        }
        db.close();
        return ret;
    }

    // My Place
    public List<Place> getListMyPlaces() {
        List<Place> places = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_MY_PLACE, new String[]{Util.KEY_ID, Util.KEY_NAME, Util.KEY_ADDRESS, Util.KEY_CATEGORY_ID,Util.KEY_IMAGE_URL,
                        Util.KEY_LATITUDE, Util.KEY_LONGITUDE,Util.KEY_PHONE, Util.KEY_WEBSITE},
                null, null, null, null, Util.KEY_ID);

        if (c.moveToFirst()) {
            do {
                int id = c.getInt(c.getColumnIndex(Util.KEY_ID));
                String name = c.getString(c.getColumnIndex(Util.KEY_NAME));
                String address = c.getString(c.getColumnIndex(Util.KEY_ADDRESS));
                int categoryId = c.getInt(c.getColumnIndex(Util.KEY_CATEGORY_ID));
                double lat = c.getDouble(c.getColumnIndex(Util.KEY_LATITUDE));
                double lng = c.getDouble(c.getColumnIndex(Util.KEY_LONGITUDE));
                String imageUrl = c.getString(c.getColumnIndex(Util.KEY_IMAGE_URL));
                String phone = c.getString(c.getColumnIndex(Util.KEY_PHONE));
                String website = c.getString(c.getColumnIndex(Util.KEY_WEBSITE));

                Place place = new Place(id,name,address,lat,lng,categoryId,imageUrl,website, phone,0);
                places.add(place);
            } while (c.moveToNext());
            c.close();
        }

        db.close();
        return places;
    }

    public long addMyPlace(Place place) {
        long ret = -1;
        ContentValues values = new ContentValues();
        values.put(Util.KEY_NAME, place.getName());
        values.put(Util.KEY_ADDRESS, place.getAddress());
        values.put(Util.KEY_CATEGORY_ID, place.getCategoryId());
        values.put(Util.KEY_LATITUDE, place.getLatitude());
        values.put(Util.KEY_LONGITUDE, place.getLongitude());
        values.put(Util.KEY_IMAGE_URL, place.getImageUrl());
        values.put(Util.KEY_WEBSITE, place.getWebsite());
        values.put(Util.KEY_PHONE,place.getPhone());

        SQLiteDatabase db = this.getWritableDatabase();
        ret = db.insert(TABLE_MY_PLACE, null, values);
        db.close();
        return ret;
    }

    public int deleteMyPlace(int placeId){
        int ret;
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ret = db.delete(TABLE_MY_PLACE,Util.KEY_ID + "=?",new String[]{String.valueOf(placeId)});
        }catch (Exception e){
            ret = -1;
        }
        db.close();
        return ret;
    }
}

