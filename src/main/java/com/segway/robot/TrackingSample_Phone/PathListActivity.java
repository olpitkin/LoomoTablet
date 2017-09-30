package com.segway.robot.TrackingSample_Phone;

import android.app.ListActivity;
import android.os.Bundle;

import com.segway.robot.TrackingSample_Phone.repository.RepositoryPath;
import com.segway.robot.TrackingSample_Phone.sql.MySQLiteHelper;
import com.segway.robot.TrackingSample_Phone.util.PathListViewAdapter;

/**
 * Created by Alex Pitkin on 29.09.2017.
 */

public class PathListActivity  extends ListActivity {

    PathListViewAdapter adapter;
    RepositoryPath repositoryPath = new RepositoryPath();

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        adapter = new PathListViewAdapter(this, android.R.layout.simple_list_item_1, repositoryPath.getAllPaths());
        setListAdapter(adapter);
    }

}
