package com.segway.robot.TrackingSample_Phone;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.segway.robot.TrackingSample_Phone.model.POI;
import com.segway.robot.TrackingSample_Phone.sql.MySQLiteHelper;
import com.segway.robot.TrackingSample_Phone.util.POIListViewAdapter;

/**
 * Created by Alex Pitkin on 28.09.2017.
 */

public class PoiListActivity extends ListActivity {

    MySQLiteHelper db = new MySQLiteHelper(this);
    POIListViewAdapter adapter;

    POI start;
    POI end;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        adapter = new POIListViewAdapter(this, android.R.layout.simple_list_item_1, db.getAllPOI());
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final POI poi = (POI) getListAdapter().getItem(position);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View dialogView = layoutInflater.inflate(R.layout.poi_dialog, null);
        dialogView.setMinimumWidth(500);
        final AlertDialog alertD = new AlertDialog.Builder(this).create();
        Button createPathButton = (Button) dialogView.findViewById(R.id.create_path);
        Button createPathAButton = (Button) dialogView.findViewById(R.id.create_path_A);
        Button createPathBButton = (Button) dialogView.findViewById(R.id.create_path_B);

        Button deletePoiButton = (Button) dialogView.findViewById(R.id.delete_poi);
        Button updatePoiButton = (Button) dialogView.findViewById(R.id.update_poi);
        Button cancelButton = (Button) dialogView.findViewById(R.id.cancel);

        createPathAButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                start = poi;
                alertD.dismiss();
                Toast.makeText(PoiListActivity.this, "START " + poi.getType() + poi.getId(), Toast.LENGTH_SHORT).show();
            }
        });

        createPathBButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                end = poi;
                alertD.dismiss();
                Toast.makeText(PoiListActivity.this, "START" + poi.getType() + poi.getId(), Toast.LENGTH_SHORT).show();
            }
        });

        createPathButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(PoiListActivity.this, "START" + start.getId() + "STOP" + end.getId(), Toast.LENGTH_SHORT).show();
                db.addPath(start,end);
                alertD.dismiss();
            }
        });

        deletePoiButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                db.deletePoi(poi);
                alertD.dismiss();
            }
        });

        updatePoiButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //db.updatePoi(poi);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertD.dismiss();
            }
        });

        alertD.setView(dialogView);
        alertD.show();

    }
}
