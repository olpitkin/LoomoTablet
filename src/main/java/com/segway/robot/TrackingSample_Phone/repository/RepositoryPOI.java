package com.segway.robot.TrackingSample_Phone.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.segway.robot.TrackingSample_Phone.model.POI;
import com.segway.robot.TrackingSample_Phone.sql.DatabaseManager;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alex Pitkin on 30.09.2017.
 */

public class RepositoryPOI {

    public static final String TABLE_POI = "path_poi";
    public static final String KEY_ID = "id";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_TYPE = "type";
    public static final String KEY_X = "x";
    public static final String KEY_Y = "y";

    private static final String[] COLUMNS_POI = {KEY_ID, KEY_DESCRIPTION, KEY_TYPE, KEY_X, KEY_Y};

    public static String createTable() {
        String CREATE_POI_TABLE = "CREATE TABLE " + TABLE_POI + " (" +
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
        DatabaseManager.getInstance().closeDatabase();
    }

    public POI getPOI(int id){
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        Cursor cursor =
                db.query(TABLE_POI, COLUMNS_POI, " id = ?", new String[] { String.valueOf(id) },
                        null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        POI poi = new POI();
        poi.setId(Integer.parseInt(cursor.getString(0)));
        poi.setDescription(cursor.getString(1));
        poi.setType(cursor.getString(2));
        poi.setX(cursor.getDouble(3));
        poi.setY(cursor.getDouble(4));
        DatabaseManager.getInstance().closeDatabase();

        return poi;
    }

    public List<POI> getAllPOI() {
        List<POI> pois = new LinkedList<>();
        String query = "SELECT * FROM " + TABLE_POI;
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
        DatabaseManager.getInstance().closeDatabase();

        return pois;
    }

    public void deletePoi(POI poi) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(TABLE_POI, KEY_ID+" = ?", new String[] { String.valueOf(poi.getId()) });
        DatabaseManager.getInstance().closeDatabase();
    }

    public void updatePoi(POI poi) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DESCRIPTION, poi.getDescription());
        values.put(KEY_TYPE, poi.getType());
        values.put(KEY_X, poi.getX());
        values.put(KEY_Y, poi.getY());

        db.update(TABLE_POI, values, KEY_ID + " = ?",
                new String[] { String.valueOf(poi.getId())});

        DatabaseManager.getInstance().closeDatabase();
    }

    public void clearRepository() {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(TABLE_POI, null, null);
        DatabaseManager.getInstance().closeDatabase();
    }
}
