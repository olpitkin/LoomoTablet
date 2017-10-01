package com.segway.robot.TrackingSample_Phone;

import android.app.Application;
import android.content.Context;

import com.segway.robot.TrackingSample_Phone.sql.DatabaseManager;
import com.segway.robot.TrackingSample_Phone.sql.MySQLiteHelper;

/**
 * Created by Alex Pitkin on 30.09.2017.
 */

public class  App extends Application {
    private static Context context;
    private static MySQLiteHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this.getApplicationContext();
        dbHelper = new MySQLiteHelper();
        DatabaseManager.initializeInstance(dbHelper);
    }

    public static Context getContext(){
        return context;
    }

}
