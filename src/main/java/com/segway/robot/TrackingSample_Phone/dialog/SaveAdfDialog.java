package com.segway.robot.TrackingSample_Phone.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.segway.robot.TrackingSample_Phone.R;

/**
 * Created by Alex Pitkin on 28.09.2017.
 */

public class SaveAdfDialog extends AlertDialog {
    private static final String TAG = SaveAdfDialog.class.getSimpleName();
    private ProgressBar mProgressBar;

    public SaveAdfDialog(Context context) {
        super(context);
    }

    public void setProgress(int progress) {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(progress);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_adf_dialog);
        setCancelable(false);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        if (mProgressBar == null) {
            Log.e(TAG, "Unable to find view progress_bar.");
        }
    }
}