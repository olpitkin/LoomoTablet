package com.segway.robot.TrackingSample_Phone.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.segway.robot.TrackingSample_Phone.model.Info;
import com.segway.robot.TrackingSample_Phone.model.POI;
import com.segway.robot.TrackingSample_Phone.model.Path;
import com.segway.robot.TrackingSample_Phone.sql.DatabaseManager;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alex Pitkin on 02.12.2017.
 */

public class RepositoryInfo {

    public static final String TABLE_INFO = "table_info";
    public static final String KEY_ID = "id";
    public static final String KEY_START = "start";
    public static final String KEY_GOAL = "goal";
    public static final String KEY_NEXT = "next";
    public static final String KEY_DESCRIPTION = "description";

    RepositoryPOI repositoryPOI = new RepositoryPOI();

    private static final String[] COLUMNS_INFO = {KEY_ID, KEY_START, KEY_GOAL, KEY_NEXT, KEY_DESCRIPTION};

    public static String createTable() {
        String CREATE_INFO_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_INFO + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_START + " INTEGER, "+
                KEY_GOAL + " INTEGER, " +
                KEY_NEXT + " INTEGER, " +
                KEY_DESCRIPTION + " TEXT "
                +")";

        return  CREATE_INFO_TABLE;
    }

    public void addInfo(Info info) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_START, info.getStart().getId());
        values.put(KEY_GOAL, info.getGoal().getId());
        values.put(KEY_NEXT, info.getNext().getId());
        values.put(KEY_DESCRIPTION, info.getDescription());
        db.insert(TABLE_INFO, null, values);
        DatabaseManager.getInstance().closeDatabase();
    }

    public Info getInfo(int id) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        Cursor cursor =
                db.query(TABLE_INFO, COLUMNS_INFO, " id = ?", new String[] { String.valueOf(id) },
                        null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Info info = new Info();
        info.setId(cursor.getInt(0));
        POI start = repositoryPOI.getPOI(cursor.getInt(1));
        POI goal = repositoryPOI.getPOI(cursor.getInt(2));
        POI next = repositoryPOI.getPOI(cursor.getInt(3));
        info.setStart(start);
        info.setGoal(goal);
        info.setNext(next);
        info.setDescription(cursor.getString(4));
        DatabaseManager.getInstance().closeDatabase();

        return info;
    }

    public String getInfoOnPOI(POI start, POI goal, POI next) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();

        Cursor cursor =
                db.query(TABLE_INFO, COLUMNS_INFO, " start = ? AND goal = ? AND next = ? ",
                         new String[] { String.valueOf(start.getId()), String.valueOf(goal.getId()), String.valueOf(next.getId())},
                        null, null, null, null);

        String info = cursor.getString(4);
        DatabaseManager.getInstance().closeDatabase();
        return info;
    }

    public void deleteInfo(Info info) {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(TABLE_INFO, KEY_ID+" = ?", new String[] { String.valueOf(info.getId()) });
        DatabaseManager.getInstance().closeDatabase();
    }

    public List<Info> getAllInfo(){
        List<Info> infos = new LinkedList<>();

        String query = "SELECT  * FROM " + TABLE_INFO;

        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Info info = new Info();
                    info.setId(cursor.getInt(0));
                    POI start = repositoryPOI.getPOI(cursor.getInt(1));
                    POI goal = repositoryPOI.getPOI(cursor.getInt(2));
                    POI next = repositoryPOI.getPOI(cursor.getInt(3));
                    info.setStart(start);
                    info.setGoal(goal);
                    info.setNext(next);
                    info.setDescription(cursor.getString(4));
                    infos.add(info);
                } while (cursor.moveToNext());
            }
        }
        DatabaseManager.getInstance().closeDatabase();
        return infos;
    }

    public void clearRepository() {
        SQLiteDatabase db = DatabaseManager.getInstance().openDatabase();
        db.delete(TABLE_INFO, null, null);
        DatabaseManager.getInstance().closeDatabase();
    }
}
