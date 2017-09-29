package com.segway.robot.TrackingSample_Phone;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;

import com.segway.robot.TrackingSample_Phone.sql.MySQLiteHelper;
import com.segway.robot.TrackingSample_Phone.util.PathListViewAdapter;

/**
 * Created by Alex Pitkin on 29.09.2017.
 */

public class PathListActivity  extends ListActivity {

    MySQLiteHelper db = new MySQLiteHelper(this);
    PathListViewAdapter adapter;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.e("path", String.valueOf(db.getAllPathEntries().size()));
        adapter = new PathListViewAdapter(this, android.R.layout.simple_list_item_1, db.getAllPathEntries());
        setListAdapter(adapter);
    }

}
