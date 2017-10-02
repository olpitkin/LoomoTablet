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
import com.segway.robot.TrackingSample_Phone.model.Path;
import com.segway.robot.TrackingSample_Phone.repository.RepositoryPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex Pitkin on 29.09.2017.
 */

public class PathListViewAdapter extends BaseAdapter {

    Context mContext;
    private static LayoutInflater inflater = null;
    private List<Path> pathList;
    private List<Path> selectedPathsList = new ArrayList<>();

    public PathListViewAdapter (Context context, List<Path> pathList) {
        this.mContext = context;
        this.pathList = pathList;
        inflater =  LayoutInflater.from(mContext);
    }

    public static class ViewHolder {
        public TextView path_tw;
    }

    @Override
    public int getCount() {
        return pathList.size();
    }

    @Override
    public Path getItem(int position) {
        return pathList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return pathList.get(position).getId();
    }

    public List<Path> getSelectedPaths() {
        return selectedPathsList;
    }

    public void deletePath(Path path) {
        pathList.remove(path);
        notifyDataSetChanged();
    }
    public void addPath (Path path) {
        pathList.add(path);
        notifyDataSetChanged();
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.path_list_item, null);
            holder.path_tw = (TextView) view.findViewById(R.id.path);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.path_tw.setText(pathList.get(position).toString());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Path path = pathList.get(position);
                if (selectedPathsList.contains(path)) {
                    selectedPathsList.remove(path);
                    arg0.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    selectedPathsList.add(path);
                    arg0.setBackgroundColor(Color.GRAY);
                }
            }
        });

        return view;
    }
}