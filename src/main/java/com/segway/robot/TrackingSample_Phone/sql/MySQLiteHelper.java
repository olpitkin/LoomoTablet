package com.segway.robot.TrackingSample_Phone.sql;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.segway.robot.TrackingSample_Phone.App;
import com.segway.robot.TrackingSample_Phone.repository.RepositoryPOI;
import com.segway.robot.TrackingSample_Phone.repository.RepositoryPath;

/**
 * Created by Alex Pitkin on 28.09.2017.
 */

public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "LoomoDB";

    public MySQLiteHelper() {
        super(App.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(RepositoryPOI.createTable());
        sqLiteDatabase.execSQL(RepositoryPath.createTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RepositoryPOI.TABLE_POI);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RepositoryPath.TABLE_PATH);

        this.onCreate(sqLiteDatabase);
    }
}
