package com.segway.robot.TrackingSample_Phone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.segway.robot.TrackingSample_Phone.model.POI;
import com.segway.robot.TrackingSample_Phone.repository.RepositoryPOI;
import com.segway.robot.TrackingSample_Phone.util.POIListViewAdapter;

/**
 * Created by Alex Pitkin on 28.09.2017.
 */

public class PoiListActivity extends Activity {

    RepositoryPOI repositoryPOI = new RepositoryPOI();

    ListView listView;
    POIListViewAdapter adapter;

    Button deletePoiButton;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.poi_list);
        listView = (ListView) findViewById(R.id.list);
        adapter = new POIListViewAdapter(this, repositoryPOI.getAllPOI());
        listView.setAdapter(adapter);

        deletePoiButton = (Button) findViewById(R.id.delete_poi);
        deletePoiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int size = adapter.getSelectedPois().size();
                for (POI p : adapter.getSelectedPois()) {
                    repositoryPOI.deletePoi(p);
                    adapter.deletePoi(p);
                    adapter.notifyDataSetChanged();
                }
                Toast toast = Toast.makeText(getApplicationContext(), size + " poi deleted", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
