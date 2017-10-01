package com.segway.robot.TrackingSample_Phone.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.segway.robot.TrackingSample_Phone.R;
import com.segway.robot.TrackingSample_Phone.model.POI;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by olpit on 28.09.2017.
 */

public class POIListViewAdapter extends ArrayAdapter<POI> {

    private Activity activity;
    private List<POI> poiList;
    private List<Integer> selectedPoiList = new ArrayList<>();
    private static LayoutInflater inflater = null;

    public POIListViewAdapter (Activity activity, int textViewResourceId, List<POI> poiList) {
        super(activity, textViewResourceId, poiList);
        try {
            this.activity = activity;
            this.poiList = poiList;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        } catch (Exception e) {

        }
    }

    public void setSelectedIndex(int ind) {
        selectedPoiList.add(ind);
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedPois() {
        return selectedPoiList;
    }

    public void removeSelectedPOI(int ind)
    {
        selectedPoiList.remove(ind);
        notifyDataSetChanged();
    }

    public int getCount() {
        return poiList.size();
    }

    public POI getItem(POI position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        public TextView poi_tw;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.poi_list_item, null);
                holder = new ViewHolder();
                holder.poi_tw = (TextView) vi.findViewById(R.id.poi);
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }
            holder.poi_tw.setText(poiList.get(position).toString());
            if(!selectedPoiList.isEmpty() && selectedPoiList.contains(position))
            {
                holder.poi_tw.setBackgroundColor(Color.BLACK);
            }
            else
            {
                holder.poi_tw.setBackgroundColor(Color.BLUE);
            }
        } catch (Exception e) {
        }
        return vi;
    }
}
