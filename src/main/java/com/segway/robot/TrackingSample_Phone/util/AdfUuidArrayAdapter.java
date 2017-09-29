package com.segway.robot.TrackingSample_Phone.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.segway.robot.TrackingSample_Phone.R;
import com.segway.robot.TrackingSample_Phone.model.AdfData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by olpit on 28.09.2017.
 */

public class AdfUuidArrayAdapter extends ArrayAdapter<String> {
    private List<AdfData> mAdfDataList;

    public AdfUuidArrayAdapter(Context context, ArrayList<AdfData> adfDataList) {
        super(context, R.layout.adf_list_row);
        setAdfData(adfDataList);
    }

    public void setAdfData(ArrayList<AdfData> adfDataList) {
        mAdfDataList = adfDataList;
    }

    @Override
    public int getCount() {
        return mAdfDataList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;
        if (convertView == null) {
            LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            row = inflator.inflate(R.layout.adf_list_row, parent, false);
        } else {
            row = convertView;
        }
        TextView uuid = (TextView) row.findViewById(R.id.adf_uuid);
        TextView name = (TextView) row.findViewById(R.id.adf_name);

        if (mAdfDataList == null) {
            name.setText(R.string.metadata_not_read);
        } else {
            name.setText(mAdfDataList.get(position).name);
            uuid.setText(mAdfDataList.get(position).uuid);
        }
        return row;
    }
}
