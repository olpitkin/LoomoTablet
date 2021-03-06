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
import com.segway.robot.TrackingSample_Phone.model.Info;
import com.segway.robot.TrackingSample_Phone.model.POI;
import com.segway.robot.TrackingSample_Phone.model.Path;
import com.segway.robot.TrackingSample_Phone.repository.RepositoryInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Uto4ko on 02.12.2017.
 */

public class InfoListViewAdapter extends BaseAdapter {

    Context mContext;
    private static LayoutInflater inflater = null;
    private List<Info> infoList;
    private List<Info> selectedInfoList = new ArrayList<>();
    RepositoryInfo repositoryInfo = new RepositoryInfo();
    public InfoListViewAdapter (Context context, List<Info> infoList) {
        this.mContext = context;
        this.infoList = infoList;
        inflater =  LayoutInflater.from(mContext);
    }

    public static class ViewHolder {
        public TextView info_tw;
    }

    public void deleteInfo(Info info) {
        infoList.remove(info);
        notifyDataSetChanged();
    }
    public void addInfo (Info info) {
        infoList.add(info);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return infoList.size();
    }

    @Override
    public Object getItem(int position) {
        return infoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return infoList.get(position).getId();
    }

    public List<Info> getSelectedInfos(){
        return selectedInfoList;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        final InfoListViewAdapter.ViewHolder holder;
        if (view == null) {
            holder = new InfoListViewAdapter.ViewHolder();
            view = inflater.inflate(R.layout.info_item, null);
            // Locate the TextViews in listview_item.xml
            holder.info_tw = (TextView) view.findViewById(R.id.info);
            view.setTag(holder);
        } else {
            holder = (InfoListViewAdapter.ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.info_tw.setText(infoList.get(position).toString());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Info info = infoList.get(position);
                if (selectedInfoList.contains(info)) {
                    selectedInfoList.remove(info);
                    arg0.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    selectedInfoList.add(info);
                    arg0.setBackgroundColor(Color.GRAY);
                }
            }
        });


        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Info info = infoList.get(position);
                View dialogView = inflater.inflate(R.layout.info_update_dialog, null);
                dialogView.setMinimumWidth(500);
                final AlertDialog alertD = new AlertDialog.Builder(mContext).create();
                final EditText poiDesc = (EditText) dialogView.findViewById(R.id.info_desc);
                Button infoUpdate = (Button) dialogView.findViewById(R.id.info_update);

                poiDesc.setText(info.getDescription(), TextView.BufferType.EDITABLE);

                infoUpdate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        info.setDescription(poiDesc.getText().toString());
                        repositoryInfo.updateInfo(info);
                        alertD.dismiss();
                        Toast toast = Toast.makeText(mContext,"info updated", Toast.LENGTH_SHORT);
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
