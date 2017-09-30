package com.segway.robot.TrackingSample_Phone.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.segway.robot.TrackingSample_Phone.model.POI;
import com.segway.robot.TrackingSample_Phone.sql.DatabaseManager;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by olpit on 30.09.2017.
 */

public class RepositoryPOI {

    // POI TABLE
    public static final String TABLE_POI = "path_poi";
    public static final String KEY_ID = "id";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_TYPE = "type";
    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";

    private static final String[] COLUMNS_POI = {KEY_ID, KEY_DESCRIPTION, KEY_TYPE, KEY_X, KEY_Y};

    public static String createTable() {
        String CREATE_POI_TABLE = "CREATE TABLE " + TABLE_POI + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_DESCRIPTION + " TEXT, "+
                KEY_TYPE + " TEXT, "+
                KEY_X + " INTEGER, " +
                KEY_Y + " INTEGER "
                +")";

        return  CREATE_POI_TABLE;
    }

    public void addPoi(POI poi) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DESCRIPTION, poi.getDescription());
        values.put(KEY_TYPE, poi.getType());
        values.put(KEY_X, poi.getX());
        values.put(KEY_Y, poi.getY());

        db.insert(TABLE_POI, null, values);
        db.close();
    }

    public POI getPOI(int id){
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        Cursor cursor =
                db.query(TABLE_POI, // a. table
                        COLUMNS_POI, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        if (cursor != null)
            cursor.moveToFirst();

        POI poi = new POI();
        poi.setId(Integer.parseInt(cursor.getString(0)));
        poi.setDescription(cursor.getString(1));
        poi.setType(cursor.getString(2));
        poi.setX(cursor.getDouble(3));
        poi.setY(cursor.getDouble(4));

        return poi;
    }

    public List<POI> getAllPOI() {
        List<POI> pois = new LinkedList<>();
        String query = "SELECT  * FROM " + TABLE_POI;
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor = db.rawQuery(query, null);

        POI poi = null;
        if (cursor.moveToFirst()) {
            do {
                poi = new POI();
                poi.setId(Integer.parseInt(cursor.getString(0)));
                poi.setDescription(cursor.getString(1));
                poi.setType(cursor.getString(2));
                poi.setX(cursor.getDouble(3));
                poi.setY(cursor.getDouble(4));

                pois.add(poi);
            } while (cursor.moveToNext());
        }
        return pois;
    }

    public void deletePoi(POI poi) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        db.delete(TABLE_POI, //table name
                KEY_ID+" = ?",  // selections
                new String[] { String.valueOf(poi.getId()) }); //selections args

        db.close();
    }

    public void clearRepo() {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(TABLE_POI, null, null);
        DatabaseManager.getInstance().closeDatabase();
    }

}
