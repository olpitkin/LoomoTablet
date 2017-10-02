package com.segway.robot.TrackingSample_Phone.util;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.segway.robot.TrackingSample_Phone.R;
import com.segway.robot.TrackingSample_Phone.model.POI;
import com.segway.robot.TrackingSample_Phone.repository.RepositoryPOI;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex Pitkin on 28.09.2017.
 */

public class POIListViewAdapter extends BaseAdapter {

    Context mContext;
    private List<POI> poiList;
    private List<POI> filteredList;
    private List<POI> selectedPoiList = new ArrayList<>();
    private static LayoutInflater inflater = null;
    private RepositoryPOI repositoryPOI = new RepositoryPOI();

    public POIListViewAdapter (Context context, List<POI> poiList) {
            this.mContext = context;
            this.poiList = poiList;
            inflater =  LayoutInflater.from(mContext);
            this.filteredList = new ArrayList<POI>();
            this.filteredList.addAll(poiList);
    }

    public static class ViewHolder {
        public TextView poi_tw;
    }

    @Override
    public int getCount() {
        return poiList.size();
    }

    @Override
    public POI getItem(int position) {
        return poiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return poiList.get(position).getId();
    }

    public List<POI> getSelectedPois() {
        return selectedPoiList;
    }

    public void deletePoi(POI poi) {
        poiList.remove(poi);
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.poi_list_item, null);
            // Locate the TextViews in listview_item.xml
            holder.poi_tw = (TextView) view.findViewById(R.id.poi);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.poi_tw.setText(poiList.get(position).toString());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                POI poi = poiList.get(position);
                if (selectedPoiList.contains(poi)) {
                    selectedPoiList.remove(poi);
                    arg0.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    selectedPoiList.add(poi);
                    arg0.setBackgroundColor(Color.GRAY);
                }
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final POI poi = poiList.get(position);
                View dialogView = inflater.inflate(R.layout.poi_update_dialog, null);
                dialogView.setMinimumWidth(500);
                final AlertDialog alertD = new AlertDialog.Builder(mContext).create();
                final EditText poiDesc = (EditText) dialogView.findViewById(R.id.poi_desc);
                EditText poiX = (EditText) dialogView.findViewById(R.id.poi_x);
                EditText poiY = (EditText) dialogView.findViewById(R.id.poi_y);
                Button poiUpdate = (Button) dialogView.findViewById(R.id.poi_update);

                poiDesc.setText(poi.getDescription(), TextView.BufferType.EDITABLE);
                poiX.setText(String.valueOf(poi.getX()), TextView.BufferType.EDITABLE);
                poiY.setText(String.valueOf(poi.getY()), TextView.BufferType.EDITABLE);

                poiUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        poi.setDescription(poiDesc.getText().toString());
                        repositoryPOI.updatePoi(poi);
                        alertD.dismiss();
                        Toast toast = Toast.makeText(mContext,"poi updated", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });

                alertD.setView(dialogView);
                alertD.show();
                return false;
            }
        });

        return view;
    }
}
