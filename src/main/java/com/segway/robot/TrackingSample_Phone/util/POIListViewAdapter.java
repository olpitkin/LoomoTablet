package com.segway.robot.TrackingSample_Phone.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.segway.robot.TrackingSample_Phone.R;
import com.segway.robot.TrackingSample_Phone.model.POI;

import java.util.List;

/**
 * Created by olpit on 28.09.2017.
 */

public class POIListViewAdapter extends ArrayAdapter<POI> {

    private Activity activity;
    private List<POI> poiList;
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

        } catch (Exception e) {


        }
        return vi;
    }
}
