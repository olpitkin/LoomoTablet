package com.segway.robot.TrackingSample_Phone;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.segway.robot.TrackingSample_Phone.model.POI;
import com.segway.robot.TrackingSample_Phone.sql.MySQLiteHelper;
import com.segway.robot.TrackingSample_Phone.util.POIListViewAdapter;

import java.util.List;

/**
 * Created by Alex Pitkin on 28.09.2017.
 */

public class PoiListActivity extends ListActivity {

    MySQLiteHelper db = new MySQLiteHelper(this);

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        List<POI> list = db.getAllPOI();

        String[] values = new String[list.size()];
        int i = 0;
        for (POI poi : list) {
            values[i] = poi.toString();
            i++;
        }

        POIListViewAdapter adapter = new POIListViewAdapter(this,
                android.R.layout.simple_list_item_1, list);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final POI poi = (POI) getListAdapter().getItem(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Delete Poi");
        builder.setMessage("Are you sure want to delete " + poi + " ?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                db.deletePoi(poi);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }
}
