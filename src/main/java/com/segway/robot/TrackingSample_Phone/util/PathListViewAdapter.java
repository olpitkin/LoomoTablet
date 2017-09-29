package com.segway.robot.TrackingSample_Phone.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.segway.robot.TrackingSample_Phone.R;
import com.segway.robot.TrackingSample_Phone.model.Path;

import java.util.List;

/**
 * Created by olpit on 29.09.2017.
 */

public class PathListViewAdapter extends ArrayAdapter<Path> {

    private Activity activity;
    private List<Path> pathList;
    private static LayoutInflater inflater = null;

    public PathListViewAdapter (Activity activity, int textViewResourceId, List<Path> pathList) {
        super(activity, textViewResourceId, pathList);
        try {
            this.activity = activity;
            this.pathList = pathList;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        } catch (Exception e) {

        }
    }

    public int getCount() {
        return pathList.size();
    }

    public Path getItem(Path position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        public TextView path_tw;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.path_list_item, null);
                holder = new ViewHolder();

                holder.path_tw = (TextView) vi.findViewById(R.id.path);


                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            holder.path_tw.setText(pathList.get(position).toString());

        } catch (Exception e) {


        }
        return vi;
    }
}