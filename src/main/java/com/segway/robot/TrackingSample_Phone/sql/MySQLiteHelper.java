package com.segway.robot.TrackingSample_Phone.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.segway.robot.TrackingSample_Phone.model.POI;
import com.segway.robot.TrackingSample_Phone.model.Path;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alex Pitkin on 28.09.2017.
 */

public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "LoomoDB";

    private static final String TABLE_POI = "poi_table";
    private static final String TABLE_PATH = "path_table";
    // POI TABLE
    private static final String KEY_ID = "id";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_TYPE = "type";
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    // PATH TABLE
    private static final String KEY_START = "start_id";
    private static final String KEY_END = "end_id";

    private static final String[] COLUMNS_POI = {KEY_ID, KEY_DESCRIPTION, KEY_TYPE, KEY_X, KEY_Y};
    private static final String[] COLUMNS_PATH = {KEY_ID, KEY_START, KEY_END};

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_POI_TABLE = "CREATE TABLE " + TABLE_POI + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_DESCRIPTION + " TEXT, "+
                KEY_TYPE + " TEXT, "+
                KEY_X + " INTEGER, " +
                KEY_Y + " INTEGER "
                +")";

        String CREATE_PATH_TABLE = "CREATE TABLE " + TABLE_PATH + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_START + " INTEGER, "+
                KEY_END + " INTEGER )";

        sqLiteDatabase.execSQL(CREATE_POI_TABLE);
        sqLiteDatabase.execSQL(CREATE_PATH_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_POI);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PATH);

        this.onCreate(sqLiteDatabase);
    }

    public void addPoi(POI poi) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DESCRIPTION, poi.getDescription());
        values.put(KEY_TYPE, poi.getType());
        values.put(KEY_X, poi.getX());
        values.put(KEY_Y, poi.getY());

        db.insert(TABLE_POI, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        db.close();
    }

    public void addPath(POI start, POI end) {

        SQLiteDatabase db = this.getWritableDatabase();

        if (start  == null || end == null) {
            throw new IllegalArgumentException("start");
        }

        if (getPOI(start.getId()) == null) {
            throw new IllegalArgumentException("start");
        }

        if (getPOI(end.getId()) == null) {
            throw new IllegalArgumentException("end");
        }

        ContentValues values = new ContentValues();
        values.put(KEY_START, start.getId());
        values.put(KEY_END, end.getId());

        db.insert(TABLE_PATH, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        db.close();
    }

    public void addPath(Path path) {

        SQLiteDatabase db = this.getWritableDatabase();

        if (path.getStart()  == null || path.getEnd() == null) {
            throw new IllegalArgumentException("start or end");
        }

        if (getPOI(path.getStart().getId()) == null) {
            throw new IllegalArgumentException("start");
        }

        if (getPOI(path.getEnd().getId()) == null) {
            throw new IllegalArgumentException("end");
        }

        ContentValues values = new ContentValues();
        values.put(KEY_START, path.getStart().getId());
        values.put(KEY_END, path.getEnd().getId());

        db.insert(TABLE_PATH, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        db.close();
    }

    public POI getPOI(int id){

        SQLiteDatabase db = this.getReadableDatabase();

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

    public Path getPath(int id){

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_PATH, // a. table
                        COLUMNS_PATH, // b. column names
                        " id = ?", // c. selections
                        new String[] { String.valueOf(id) }, // d. selections args
                        null, // e. group by
                        null, // f. having
                        null, // g. order by
                        null); // h. limit

        if (cursor != null)
            cursor.moveToFirst();

        Path path = new Path();
        path.setId(Integer.parseInt(cursor.getString(0)));
        path.setStart(getPOI(Integer.parseInt(cursor.getString(1))));
        path.setEnd(getPOI(Integer.parseInt(cursor.getString(2))));

        return path;
    }

    public List<POI> getAllPOI() {
        List<POI> pois = new LinkedList<>();

        String query = "SELECT  * FROM " + TABLE_POI;

        SQLiteDatabase db = this.getWritableDatabase();
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

    public List<Path> getAllPaths() {
        List<Path> paths = new LinkedList<>();

        String query = "SELECT  * FROM " + TABLE_PATH;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Path path = null;
        if (cursor.moveToFirst()) {
            do {
                path = new Path();
                path.setId(Integer.parseInt(cursor.getString(0)));
                path.setStart(getPOI(Integer.parseInt(cursor.getString(1))));
                path.setEnd(getPOI(Integer.parseInt(cursor.getString(2))));

            } while (cursor.moveToNext());
        }

        return paths;
    }

    public void deletePoi(POI poi) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_POI, //table name
                KEY_ID+" = ?",  // selections
                new String[] { String.valueOf(poi.getId()) }); //selections args

        db.close();
    }

    public void deletePath(Path path) {

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_PATH, //table name
                KEY_ID+" = ?",  // selections
                new String[] { String.valueOf(path.getId()) }); //selections args

        db.close();
    }
}
