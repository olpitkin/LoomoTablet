package com.segway.robot.TrackingSample_Phone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.atap.tangoservice.Tango;

public class MainActivity extends Activity {

    public static final String USE_AREA_LEARNING = "com.projecttango.examples.java.helloareadescription.usearealearning";
    public static final String LOAD_ADF = "com.projecttango.examples.java.helloareadescription.loadadf";
    public static final int REQUEST_CODE_TANGO_PERMISSION = 0;

    private ToggleButton mLearningModeToggleButton;
    private ToggleButton mLoadAdfToggleButton;

    private boolean mIsUseAreaLearning;
    private boolean mIsLoadAdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setTitle(R.string.app_name);

        mLearningModeToggleButton = (ToggleButton) findViewById(R.id.learning_mode);
        mLoadAdfToggleButton = (ToggleButton) findViewById(R.id.load_adf);

        mIsUseAreaLearning = mLearningModeToggleButton.isChecked();
        mIsLoadAdf = mLoadAdfToggleButton.isChecked();

        startActivityForResult(Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE), 0);
    }
    public void loadAdfClicked(View v) {
        mIsLoadAdf = mLoadAdfToggleButton.isChecked();
    }
    public void learningModeClicked(View v) {
        mIsUseAreaLearning = mLearningModeToggleButton.isChecked();
    }
    public void startClicked(View v) {
        startAreaDescriptionActivity();
    }
    public void adfListViewClicked(View v) {
        startAdfListView();
    }
    public void poiListViewClicked(View v) {
        startPoiListView();
    }
    public void pathListViewClicked(View v) {
        startPathListView();
    }
    private void startAreaDescriptionActivity() {
        Intent startAdIntent = new Intent(this, LocalizationActivity.class);
        startAdIntent.putExtra(USE_AREA_LEARNING, mIsUseAreaLearning);
        startAdIntent.putExtra(LOAD_ADF, mIsLoadAdf);
        startActivity(startAdIntent);
    }
    private void startAdfListView() {
        Intent startAdfListViewIntent = new Intent(this, AdfUuidListViewActivity.class);
        startActivity(startAdfListViewIntent);
    }
    private void startPoiListView() {
        Intent startPoiListViewIntent = new Intent(this, PoiListActivity.class);
        startActivity(startPoiListViewIntent);
    }
    private void startPathListView() {
        Intent startPathListViewIntent = new Intent(this, PathListActivity.class);
        startActivity(startPathListViewIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_TANGO_PERMISSION) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.arealearning_permission, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}