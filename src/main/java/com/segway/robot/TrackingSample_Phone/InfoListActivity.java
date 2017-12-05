package com.segway.robot.TrackingSample_Phone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.segway.robot.TrackingSample_Phone.model.Info;
import com.segway.robot.TrackingSample_Phone.model.POI;
import com.segway.robot.TrackingSample_Phone.repository.RepositoryInfo;
import com.segway.robot.TrackingSample_Phone.repository.RepositoryPOI;
import com.segway.robot.TrackingSample_Phone.util.InfoListViewAdapter;

/**
 * Created by Alex Pitkin on 02.12.2017.
 */

public class InfoListActivity  extends Activity {

    InfoListViewAdapter adapter;
    ListView listView;
    RepositoryPOI repositoryPOI = new RepositoryPOI();
    RepositoryInfo repositoryInfo = new RepositoryInfo();
    Button deleteInfoButton;
    Button createInfoButton;

    POI start;
    POI goal;
    POI next;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.info_list);
        listView = (ListView) findViewById(R.id.list);
        adapter = new InfoListViewAdapter(this, repositoryInfo.getAllInfo());
        listView.setAdapter(adapter);

        deleteInfoButton = (Button) findViewById(R.id.delete_info);
        deleteInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int size = adapter.getSelectedInfos().size();
                for (Info p : adapter.getSelectedInfos()) {
                    repositoryInfo.deleteInfo(p);
                    adapter.deleteInfo(p);
                    adapter.notifyDataSetChanged();
                }
                Toast toast = Toast.makeText(InfoListActivity.this, size + " infos deleted", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        createInfoButton = (Button) findViewById(R.id.create_info);
        createInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(InfoListActivity.this);
                builderSingle.setTitle("Select Start :-");
                final ArrayAdapter<POI> arrayAdapter = new ArrayAdapter<POI>(InfoListActivity.this, android.R.layout.simple_selectable_list_item);
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
                        start = arrayAdapter.getItem(which);
                        AlertDialog.Builder builderInner = new AlertDialog.Builder(InfoListActivity.this);
                        builderInner.setTitle("Select END :-");
                        arrayAdapter.remove(start);
                        builderInner.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,int which) {
                                dialog.dismiss();
                            }
                        });

                        builderInner.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                goal = arrayAdapter.getItem(which);
                                AlertDialog.Builder builderInner = new AlertDialog.Builder(InfoListActivity.this);
                                builderInner.setTitle("Select NEXT :-");
                                arrayAdapter.remove(goal);
                                builderInner.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,int which) {
                                        dialog.dismiss();
                                    }
                                });

                                builderInner.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        next = arrayAdapter.getItem(which);
                                        if(goal  != null && start != null && next != null){
                                            Info info = new Info();
                                            info.setGoal(goal);
                                            info.setStart(start);
                                            info.setNext(next);
                                            info.setDescription("TURN");
                                            repositoryInfo.addInfo(info);
                                        }
                                    }
                                });
                                builderInner.show();
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
