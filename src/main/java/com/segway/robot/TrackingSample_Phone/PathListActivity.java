package com.segway.robot.TrackingSample_Phone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.segway.robot.TrackingSample_Phone.model.POI;
import com.segway.robot.TrackingSample_Phone.model.Path;
import com.segway.robot.TrackingSample_Phone.repository.RepositoryPOI;
import com.segway.robot.TrackingSample_Phone.repository.RepositoryPath;
import com.segway.robot.TrackingSample_Phone.util.PathListViewAdapter;

/**
 * Created by Alex Pitkin on 29.09.2017.
 */

public class PathListActivity  extends Activity {

    PathListViewAdapter adapter;
    RepositoryPath repositoryPath = new RepositoryPath();
    RepositoryPOI repositoryPOI = new RepositoryPOI();
    ListView listView;
    Button deletePathButton;
    Button createPathButton;

    POI poiStart = null;
    POI poiEnd = null;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.path_list);
        listView = (ListView) findViewById(R.id.list);
        adapter = new PathListViewAdapter(this, repositoryPath.getAllPaths());
        listView.setAdapter(adapter);

        deletePathButton = (Button) findViewById(R.id.delete_path);
        deletePathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int size = adapter.getSelectedPaths().size();
                for (Path p : adapter.getSelectedPaths()) {
                    repositoryPath.deletePath(p);
                    adapter.deletePath(p);
                    adapter.notifyDataSetChanged();
                }
                Toast toast = Toast.makeText(PathListActivity.this, size + " paths deleted", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        createPathButton = (Button) findViewById(R.id.create_path);
        createPathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(PathListActivity.this);
                builderSingle.setTitle("Select Start :-");
                final ArrayAdapter<POI> arrayAdapter = new ArrayAdapter<POI>(PathListActivity.this, android.R.layout.simple_selectable_list_item);
                arrayAdapter.addAll(repositoryPOI.getAllPOI());

                builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        poiStart = arrayAdapter.getItem(which);
                        Toast toast = Toast.makeText(PathListActivity.this, "START: " + poiStart.toString(), Toast.LENGTH_SHORT);
                        toast.show();

                        AlertDialog.Builder builderInner = new AlertDialog.Builder(PathListActivity.this);
                        builderInner.setTitle("Select END :-");
                        arrayAdapter.remove(poiStart);
                        builderInner.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int which) {
                                dialog.dismiss();
                            }
                        });

                        builderInner.setAdapter(arrayAdapter, new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                poiEnd = arrayAdapter.getItem(which);
                                Path p = new Path(poiStart, poiEnd);
                                repositoryPath.addPath(p);
                                adapter.addPath(p);
                                Toast toast = Toast.makeText(PathListActivity.this, "path created", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        });

                        builderInner.show();
                    }
                });
                builderSingle.show();
            }
        });
    }
}
