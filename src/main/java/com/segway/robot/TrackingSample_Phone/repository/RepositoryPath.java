package com.segway.robot.TrackingSample_Phone.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.segway.robot.TrackingSample_Phone.model.POI;
import com.segway.robot.TrackingSample_Phone.model.Path;
import com.segway.robot.TrackingSample_Phone.sql.DatabaseManager;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alex Pitkin on 30.09.2017.
 */

public class RepositoryPath {

    public static final String TABLE_PATH = "path_table";
    public static final String KEY_ID = "id";
    public static final String KEY_START = "start_id";
    public static final String KEY_END = "end_id";
    private static final String[] COLUMNS_PATH = {KEY_ID, KEY_START, KEY_END};

    RepositoryPOI repositoryPOI = new RepositoryPOI();

    public static String createTable() {
        String CREATE_PATH_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PATH + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_START + " INTEGER, "+
                KEY_END + " INTEGER )";
        return  CREATE_PATH_TABLE;
    }

    public void addPath(POI start, POI end) {

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        if (start  == null || end == null) {
            throw new IllegalArgumentException("start");
        }

        ContentValues values1 = new ContentValues();
        ContentValues values2 = new ContentValues();
        values1.put(KEY_START, start.getId());
        values1.put(KEY_END, end.getId());

        values2.put(KEY_START, end.getId());
        values2.put(KEY_END, start.getId());

        db.insert(TABLE_PATH, null, values1);
        db.insert(TABLE_PATH, null, values2);
        DatabaseManager.getInstance().closeDatabase();
    }

    public void addPath(Path path) {

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        if (path.getStart()  == null || path.getEnd() == null) {
            throw new IllegalArgumentException("start or end");
        }

        ContentValues values1 = new ContentValues();
        ContentValues values2 = new ContentValues();
        values1.put(KEY_START, path.getStart().getId());
        values1.put(KEY_END, path.getEnd().getId());

        values2.put(KEY_START, path.getEnd().getId());
        values2.put(KEY_END, path.getStart().getId());

        db.insert(TABLE_PATH, null, values1);
        db.insert(TABLE_PATH, null, values2);
        DatabaseManager.getInstance().closeDatabase();
    }

    public Path getPath(int id) {

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

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
        path.setStart(repositoryPOI.getPOI(Integer.parseInt(cursor.getString(1))));
        path.setEnd(repositoryPOI.getPOI(Integer.parseInt(cursor.getString(2))));

        DatabaseManager.getInstance().closeDatabase();
        return path;
    }

    public List<Path> getAllPaths() {
        List<Path> paths = new LinkedList<>();

        String query = "SELECT  * FROM " + TABLE_PATH;

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Path path = new Path();
                path.setId(Integer.parseInt(cursor.getString(0)));
                POI start = repositoryPOI.getPOI(Integer.parseInt(cursor.getString(1)));
                POI end = repositoryPOI.getPOI(Integer.parseInt(cursor.getString(2)));
                path.setStart(start);
                path.setEnd(end);
                path.setEnd(repositoryPOI.getPOI(Integer.parseInt(cursor.getString(2))));
                path.setWeight(Math.hypot(start.getX() - end.getX(), start.getY() - end.getY()));

                paths.add(path);
            } while (cursor.moveToNext());
        }
        DatabaseManager.getInstance().closeDatabase();
        return paths;
    }

    public List<Path> getAllPathsFromPoi(POI poi) {
        List<Path> paths = new LinkedList<>();

        String query = "SELECT  * FROM " + TABLE_PATH + " WHERE start_id = " + poi.getId();

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Path path = null;
        if (cursor.moveToFirst()) {
            do {
                path = new Path();
                path.setId(Integer.parseInt(cursor.getString(0)));
                POI start = repositoryPOI.getPOI(Integer.parseInt(cursor.getString(1)));
                POI end = repositoryPOI.getPOI(Integer.parseInt(cursor.getString(2)));
                path.setStart(start);
                path.setEnd(end);
                path.setEnd(repositoryPOI.getPOI(Integer.parseInt(cursor.getString(2))));
                path.setWeight(Math.hypot(start.getX() - end.getX(), start.getY() - end.getY()));

                paths.add(path);
            } while (cursor.moveToNext());
        }
        DatabaseManager.getInstance().closeDatabase();
        return paths;
    }

    public void setAdjacencies(POI poi) {
        List<Path> paths = getAllPathsFromPoi(poi);
        poi.setAdjacencies(paths);
    }

    public void deletePath(Path path) {

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        db.delete(TABLE_PATH, //table name
                KEY_ID+" = ?",  // selections
                new String[] { String.valueOf(path.getId()) }); //selections args

        DatabaseManager.getInstance().closeDatabase();
    }

    public void updatePath(Path path) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_START, path.getStart().getId());
        values.put(KEY_END, path.getEnd().getId());

        db.update(TABLE_PATH, values, KEY_ID + " = ?",
                new String[] { String.valueOf(path.getId())});

        DatabaseManager.getInstance().closeDatabase();
    }

    public void clearRepository() {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(TABLE_PATH, null, null);
        DatabaseManager.getInstance().closeDatabase();
    }
}



