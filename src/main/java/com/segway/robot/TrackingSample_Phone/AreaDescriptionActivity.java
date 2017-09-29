package com.segway.robot.TrackingSample_Phone;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoAreaDescriptionMetaData;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.segway.robot.TrackingSample_Phone.model.POI;
import com.segway.robot.TrackingSample_Phone.sql.MySQLiteHelper;
import com.segway.robot.mobile.sdk.connectivity.BufferMessage;
import com.segway.robot.mobile.sdk.connectivity.MobileException;
import com.segway.robot.mobile.sdk.connectivity.MobileMessageRouter;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.baseconnectivity.Message;
import com.segway.robot.sdk.baseconnectivity.MessageConnection;
import com.segway.robot.sdk.baseconnectivity.MessageRouter;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Alex Pitkin on 28.09.2017.
 */

public class AreaDescriptionActivity extends Activity implements
        SetAdfNameDialog.CallbackListener,
        SaveAdfTask.SaveAdfListener {

    //TANGO
    private static final String TAG = AreaDescriptionActivity.class.getSimpleName();
    private Tango mTango;
    private TangoConfig mConfig;
    private TangoPoseData poses[] = new TangoPoseData[3];

    private TextView mUuidTextView;
    private TextView mRelocalizationTextView;
    private int relocCount;
    private TextView pose0;
    private TextView pose1;

    private TextView lastPoi;

    private Button mSaveAdfButton;

    private boolean mIsRelocalized;
    private boolean mIsLearningMode;
    private boolean mIsConstantSpaceRelocalize;

    //LOOMO
    private MobileMessageRouter mMobileMessageRouter = null;
    private MessageConnection mMessageConnection = null;
    private LinkedList<PointF> mPointList;
    private String mRobotIP;
    private EditText mEditText;

    MySQLiteHelper db = new MySQLiteHelper(this);

    private static final DecimalFormat threeDec = new DecimalFormat("00.000");
    private SaveAdfTask mSaveAdfTask;
    private final Object mSharedLock = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_learning);
        Intent intent = getIntent();
        mIsLearningMode = intent.getBooleanExtra(MainActivity.USE_AREA_LEARNING, false);
        mIsConstantSpaceRelocalize = intent.getBooleanExtra(MainActivity.LOAD_ADF, false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initialize Tango Service as a normal Android Service. Since we call mTango.disconnect()
        // in onPause, this will unbind Tango Service, so every time onResume gets called we
        // should create a new Tango object.
        mTango = new Tango(AreaDescriptionActivity.this, new Runnable() {
            // Pass in a Runnable to be called from UI thread when Tango is ready; this Runnable
            // will be running on a new thread.
            // When Tango is ready, we can call Tango functions safely here only when there are no
            // UI thread changes involved.
            @Override
            public void run() {
                synchronized (AreaDescriptionActivity.this) {
                    try {
                        mConfig = setTangoConfig(
                                mTango, mIsLearningMode, mIsConstantSpaceRelocalize);
                        mTango.connect(mConfig);
                        startupTango();
                    } catch (TangoOutOfDateException e) {
                        Log.e(TAG, getString(R.string.tango_out_of_date_exception), e);
                        showsToastAndFinishOnUiThread(R.string.tango_out_of_date_exception);
                    } catch (TangoErrorException e) {
                        Log.e(TAG, getString(R.string.tango_error), e);
                        showsToastAndFinishOnUiThread(R.string.tango_error);
                    } catch (TangoInvalidException e) {
                        Log.e(TAG, getString(R.string.tango_invalid), e);
                        showsToastAndFinishOnUiThread(R.string.tango_invalid);
                    } catch (SecurityException e) {
                        // Area Learning permissions are required. If they are not available,
                        // SecurityException is thrown.
                        Log.e(TAG, getString(R.string.no_permissions), e);
                        showsToastAndFinishOnUiThread(R.string.no_permissions);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (AreaDescriptionActivity.this) {
                            setupTextViewsAndButtons(mTango, mIsLearningMode,
                                    mIsConstantSpaceRelocalize);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Clear the relocalization state; we don't know where the device will be since our app
        // will be paused.
        mIsRelocalized = false;
        synchronized (this) {
            try {
                mTango.disconnect();
            } catch (TangoErrorException e) {
                Log.e(TAG, getString(R.string.tango_error), e);
            }
        }
    }

    private void setupTextViewsAndButtons(Tango tango, boolean isLearningMode, boolean isLoadAdf) {
        mSaveAdfButton = (Button) findViewById(R.id.save_adf_button);
        mUuidTextView = (TextView) findViewById(R.id.adf_uuid_textview);
        mRelocalizationTextView = (TextView) findViewById(R.id.relocalization_textview);
        mEditText = (EditText) findViewById(R.id.etIP);
        pose0 = (TextView) findViewById(R.id.pose0);
        pose1 = (TextView) findViewById(R.id.pose1);

        lastPoi = (TextView) findViewById(R.id.last_poi);

        if (isLearningMode) {
            // Disable save ADF button until Tango relocalizes to the current ADF.
            mSaveAdfButton.setEnabled(false);
        } else {
            // Hide to save ADF button if leanring mode is off.
            mSaveAdfButton.setVisibility(View.GONE);
        }

        if (isLoadAdf) {
            ArrayList<String> fullUuidList;
            // Returns a list of ADFs with their UUIDs.
            fullUuidList = tango.listAreaDescriptions();
            if (fullUuidList.size() == 0) {
                mUuidTextView.setText(R.string.no_uuid);
            } else {
                mUuidTextView.setText(getString(R.string.number_of_adfs) + fullUuidList.size()
                        + getString(R.string.latest_adf_is)
                        + fullUuidList.get(fullUuidList.size() - 1));
            }
        }
    }

    private TangoConfig setTangoConfig(Tango tango, boolean isLearningMode, boolean isLoadAdf) {
        // Use default configuration for Tango Service.
        TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        // Check if learning mode.
        if (isLearningMode) {
            // Set learning mode to config.
            config.putBoolean(TangoConfig.KEY_BOOLEAN_LEARNINGMODE, true);
            config.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);

        }
        // Check for Load ADF/Constant Space relocalization mode.
        if (isLoadAdf) {
            ArrayList<String> fullUuidList;
            // Returns a list of ADFs with their UUIDs.
            fullUuidList = tango.listAreaDescriptions();
            // Load the latest ADF if ADFs are found.
            if (fullUuidList.size() > 0) {
                config.putString(TangoConfig.KEY_STRING_AREADESCRIPTION,
                        fullUuidList.get(fullUuidList.size() - 1));
            }
        }
        return config;
    }

    private void startupTango() {
        // Set Tango listeners for Poses Device wrt Start of Service, Device wrt
        // ADF and Start of Service wrt ADF.
        ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                TangoPoseData.COORDINATE_FRAME_DEVICE));
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE));

        mTango.connectListener(framePairs, new Tango.OnTangoUpdateListener() {

            @Override
            public void onPoseAvailable(TangoPoseData pose) {
                // Make sure to have atomic access to Tango data so that UI loop doesn't interfere
                // while Pose call back is updating the data.
                synchronized (mSharedLock) {
                    // Check for Device wrt ADF pose, Device wrt Start of Service pose, Start of
                    // Service wrt ADF pose (this pose determines if the device is relocalized or
                    // not).
                    // 1 COORDINATE_FRAME_AREA_DESCRIPTION
                    // 2 COORDINATE_FRAME_START_OF_SERVICE
                    // 4 COORDINATE_FRAME_DEVICE
                    if (pose.baseFrame == 1 && pose.targetFrame == 2 && pose.statusCode == 1){
                        mIsRelocalized = true;
                        relocCount++;
                    }
                    if (pose.baseFrame == 2 && pose.targetFrame == 4) {
                        if (pose.statusCode == 1) {
                            poses[0] = pose;
                        }
                    }
                    if (pose.baseFrame == 1 && pose.targetFrame == 4) {
                        if (pose.statusCode == 1) {
                            poses[1] = pose;
                            return;
                        } else {
                            mIsRelocalized = false;
                            relocCount = 0;
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (mSharedLock) {
                            mSaveAdfButton.setEnabled(mIsRelocalized);
                            mRelocalizationTextView.setText(mIsRelocalized ? getString(R.string.localized ) + " " + relocCount : getString(R.string.not_localized));
                            if (poses[0] != null){
                                pose0.setText(poses[0].toString());
                            }
                            if (poses[1] != null){
                                pose1.setText(poses[1].toString());
                            }
                        }
                    }
                });
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                // We are not using onXyzIjAvailable for this app.
            }

            @Override
            public void onPointCloudAvailable(TangoPointCloudData xyzij) {
                // We are not using onPointCloudAvailable for this app.
            }

            @Override
            public void onTangoEvent(final TangoEvent event) {
                // Ignoring TangoEvents.
            }

            @Override
            public void onFrameAvailable(int cameraId) {
                // We are not using onFrameAvailable for this application.
            }
        });
    }

    @Override
    public void onAdfNameOk(String name, String uuid) {
        saveAdf(name);
    }

    @Override
    public void onAdfNameCancelled() {
        // Continue running.
    }

    public void saveAdfClicked(View view) {
        showSetAdfNameDialog();
    }

    private void saveAdf(String adfName) {
        mSaveAdfTask = new SaveAdfTask(this, this, mTango, adfName);
        mSaveAdfTask.execute();
    }

    public void savePoiClicked (View view) {
        // OPEN DIALOG
        POI poi = new POI("test", "test", poses[1].translation[0], poses[1].translation[1]);
        db.addPoi(poi);
        lastPoi.setText(poi.toString());
    }

    @Override
    public void onSaveAdfFailed(String adfName) {
        String toastMessage = String.format(
                getResources().getString(R.string.save_adf_failed_toast_format),
                adfName);
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        mSaveAdfTask = null;
    }

    @Override
    public void onSaveAdfSuccess(String adfName, String adfUuid) {
        String toastMessage = String.format(
                getResources().getString(R.string.save_adf_success_toast_format),
                adfName, adfUuid);
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();
        mSaveAdfTask = null;
        finish();
    }

    private void showSetAdfNameDialog() {
        Bundle bundle = new Bundle();
        bundle.putString(TangoAreaDescriptionMetaData.KEY_NAME, "New ADF");
        // UUID is generated after the ADF is saved.
        bundle.putString(TangoAreaDescriptionMetaData.KEY_UUID, "");

        FragmentManager manager = getFragmentManager();
        SetAdfNameDialog setAdfNameDialog = new SetAdfNameDialog();
        setAdfNameDialog.setArguments(bundle);
        setAdfNameDialog.show(manager, "ADFNameDialog");
    }

    private String getTranslationString(TangoPoseData pose) {
        return "[" + threeDec.format(pose.translation[0]) + ","
                + threeDec.format(pose.translation[1]) + "," + threeDec.format(pose.translation[2])
                + "] ";

    }

    private String getQuaternionString(TangoPoseData pose) {

        return "[" + threeDec.format(
                pose.rotation[TangoPoseData.INDEX_ROTATION_X]) + "," + threeDec.format(
                pose.rotation[TangoPoseData.INDEX_ROTATION_Y])
                + "," + threeDec.format(
                pose.rotation[TangoPoseData.INDEX_ROTATION_Z]) + "," + threeDec.format(
                pose.rotation[TangoPoseData.INDEX_ROTATION_W])
                + "] ";

    }

    private void showsToastAndFinishOnUiThread(final int resId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AreaDescriptionActivity.this,
                        getString(resId), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}