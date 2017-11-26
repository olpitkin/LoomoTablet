package com.segway.robot.TrackingSample_Phone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
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
import com.segway.robot.TrackingSample_Phone.repository.RepositoryPOI;
import com.segway.robot.TrackingSample_Phone.util.PathFinding;
import com.segway.robot.mobile.sdk.connectivity.BufferMessage;
import com.segway.robot.mobile.sdk.connectivity.MobileException;
import com.segway.robot.mobile.sdk.connectivity.MobileMessageRouter;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.baseconnectivity.Message;
import com.segway.robot.sdk.baseconnectivity.MessageConnection;
import com.segway.robot.sdk.baseconnectivity.MessageRouter;

import org.w3c.dom.Text;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Alex Pitkin on 28.09.2017.
 */

public class LocalizationActivity extends Activity implements
        SetAdfNameDialog.CallbackListener,
        SaveAdfTask.SaveAdfListener {

    // LOOMO
    private static final String TAG = "TrackingActivity_Phone";
    private EditText mEditText;

    private String mRobotIP;
    private MobileMessageRouter mMobileMessageRouter = null;
    private MessageConnection mMessageConnection = null;
    private LinkedList<PointF> mPointList;
    private LinkedList<POI> mPOIList;
    private RepositoryPOI repositoryPOI = new RepositoryPOI();

    //TANGO
    private Tango mTango;
    private TangoConfig mConfig;
    private TangoPoseData poses[] = new TangoPoseData[3];

    private TextView logText;
    private TextView mUuidTextView;
    private TextView mRelocalizationTextView;
    private TextView relocPose;
    private TextView path;
    private int relocCount;

    private Button mSaveAdfButton;
    private Button mSendButton;
    private Button mStopButton;
    private Button wButton;
    private Button sButton;
    private Button aButton;
    private Button dButton;
    private Button debugButton;

    private boolean mIsRelocalized;
    private boolean mIsLearningMode;
    private boolean mIsConstantSpaceRelocalize;

    private SaveAdfTask mSaveAdfTask;
    private final Object mSharedLock = new Object();

    public static final String USE_AREA_LEARNING =
            "com.segway.robot.TrackingSample_Phone.usearealearning";
    public static final String LOAD_ADF =
            "com.segway.robot.TrackingSample_Phone.loadadf";

    //NAVIGATION
    private POI start;
    private POI goal;
    private boolean moving = false;
    private int control;

    private ServiceBinder.BindStateListener mBindStateListener = new ServiceBinder.BindStateListener() {
        @Override
        public void onBind() {
            Log.d(TAG, "onBind: ");
            try {
                mMobileMessageRouter.register(mMessageConnectionListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUnbind(String reason) {
            Log.e(TAG, "onUnbind: " + reason);
        }
    };

    private MessageRouter.MessageConnectionListener mMessageConnectionListener = new MessageRouter.MessageConnectionListener() {
        @Override
        public void onConnectionCreated(final MessageConnection connection) {
            Log.d(TAG, "onConnectionCreated: " + connection.getName());
            mMessageConnection = connection;
            try {
                mMessageConnection.setListeners(mConnectionStateListener, mMessageListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private MessageConnection.ConnectionStateListener mConnectionStateListener = new MessageConnection.ConnectionStateListener() {
        @Override
        public void onOpened() {
            Log.d(TAG, "onOpened: " + mMessageConnection.getName());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "connected to: " + mMessageConnection.getName(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onClosed(String error) {
            Log.e(TAG, "onClosed: " + error + ";name=" + mMessageConnection.getName());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "disconnected to: " + mMessageConnection.getName(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private MessageConnection.MessageListener mMessageListener = new MessageConnection.MessageListener() {
        @Override
        public void onMessageReceived(final Message message) {
            byte[] bytes = (byte[]) message.getContent();
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                final int code = buffer.getInt();
            LocalizationActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LocalizationActivity.this, "123" + code, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onMessageSentError(Message message, String error) {
            Log.d(TAG, "Message send error");
        }

        @Override
        public void onMessageSent(Message message) {
            Log.d(TAG, "onMessageSent: id=" + message.getId() + ";timestamp=" + message.getTimestamp());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.localization_activity);
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
        mTango = new Tango(LocalizationActivity.this, new Runnable() {
            // Pass in a Runnable to be called from UI thread when Tango is ready; this Runnable
            // will be running on a new thread.
            // When Tango is ready, we can call Tango functions safely here only when there are no
            // UI thread changes involved.
            @Override
            public void run() {
                synchronized (LocalizationActivity.this) {
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
                        synchronized (LocalizationActivity.this) {
                            setupTextViewsAndButtons(mTango, mIsLearningMode, mIsConstantSpaceRelocalize);
                        }
                    }
                });
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

    private void showsToastAndFinishOnUiThread(final int resId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LocalizationActivity.this,
                        getString(resId), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void startupTango() {
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
                synchronized (mSharedLock) {
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
                            mRelocalizationTextView.setText(mIsRelocalized ? getString(R.string.localized ) + " " + relocCount : getString(R.string.not_localized));
                            if (poses[1] != null){
                                relocPose.setText(poseToString(poses[1]));
                            }
                        }
                    }
                });
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {}

            @Override
            public void onPointCloudAvailable(TangoPointCloudData pointCloudData) {
                //logPointCloud(pointCloudData);
            }

            @Override
            public void onTangoEvent(final TangoEvent event) {}

            @Override
            public void onFrameAvailable(int cameraId) {}
        });
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

        // TODO IF DEPTH
        //config.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        //config.putInt(TangoConfig.KEY_INT_DEPTH_MODE, TangoConfig.TANGO_DEPTH_MODE_POINT_CLOUD);

        return config;
    }

    private void setupTextViewsAndButtons(Tango tango, boolean isLearningMode, boolean isLoadAdf) {
        mSaveAdfButton = (Button) findViewById(R.id.save_adf_button);
        mUuidTextView = (TextView) findViewById(R.id.adf_uuid_textview);
        mRelocalizationTextView = (TextView) findViewById(R.id.relocalization_textview);
        mEditText = (EditText) findViewById(R.id.etIP);
        relocPose = (TextView) findViewById(R.id.relocalization_pose_textview);
        logText = (TextView) findViewById(R.id.log);
        path = (TextView) findViewById(R.id.best_path);

        mEditText = (EditText) findViewById(R.id.etIP);
        mSendButton = (Button) findViewById(R.id.btnSend);
        mStopButton = (Button) findViewById(R.id.btnStop);

        wButton = (Button) findViewById(R.id.control_w);
        sButton = (Button) findViewById(R.id.control_s);
        aButton = (Button) findViewById(R.id.control_a);
        dButton = (Button) findViewById(R.id.control_d);

        debugButton = (Button) findViewById(R.id.debug_button);

        if (isLearningMode) {
            mSaveAdfButton.setEnabled(true);
        } else {
            mSaveAdfButton.setEnabled(false);
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

        wButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    sendControl(1, 100);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    sendControl(0, 100);
                }
                return true;
            }
        });

        sButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    sendControl(2, 100);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    sendControl(0, 100);
                }
                return true;
            }
        });

        aButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    sendControl(3, 100);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    sendControl(0, 100);
                }
                return true;
            }
        });

        dButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    sendControl(4, 100);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    sendControl(0, 100);
                }
                return true;
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(LocalizationActivity.this);
                builderSingle.setTitle("Select Start :-");
                final ArrayAdapter<POI> arrayAdapter = new ArrayAdapter<POI>(LocalizationActivity.this, android.R.layout.simple_selectable_list_item);
                arrayAdapter.addAll(repositoryPOI.getAllPOI());

                builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        POI poiTarget = arrayAdapter.getItem(which);
                        PathFinding pathFinding = new PathFinding();
                        mPOIList = new LinkedList<>();
                        POI myLocation = null;
                        if (poses[1] != null) {
                            myLocation = new POI ("myLoc", "-", poses[1].translation[0], poses[1].translation[1]);
                            mPOIList.add(myLocation);
                        }
                        POI startPoi = pathFinding.getNearestPOI(myLocation);
                        pathFinding.computePaths(startPoi);
                        List<POI> path = pathFinding.getShortestPathTo(poiTarget);
                        mPOIList.addAll(path);
                        startMoving();
                        controlRobot();
                    }
                });
                builderSingle.show();
            }
        });

        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRobot();
            }
        });

        debugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO FILL POI LIST
                mPOIList = new LinkedList<>();
                mPOIList.add(new POI("","", 0 ,0));
                mPOIList.add(new POI("","", 0 ,1));

                 startMoving();
                 controlRobot();
            }
        });
    }

    public void startMoving() {
        moving = true;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mPOIList == null || mPOIList.size() < 2) {
                    return;
                }
                start = mPOIList.poll();
                goal = mPOIList.poll();
                printPOI("(" + goal.getX() + " , " + goal.getY() + ")");
                while (moving) {
                    POI myLocation = new POI ("myLoc", "-", poses[1].translation[0], poses[1].translation[1]);
                    if (myLocation.isNear(goal)) {
                        if (mPOIList.isEmpty()) {
                            printPOI("");
                            moving = false;
                            break;
                        }
                        start = goal;
                        goal = mPOIList.poll();
                        printPOI(goal.toString());
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void controlRobot() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (moving){
                    if (poses[1] != null && goal != null) {
                        float[] q = poses[1].getRotationAsFloats();
                        float[] myLoc = poses[1].getTranslationAsFloats();
                        //double yaw = Math.atan2(2.0*(q[1]*q[2] + q[3]*q[0]), q[3]*q[3] - q[0]*q[0] - q[1]*q[1] + q[2]*q[2]);
                        //double pitch = Math.asin(-2.0*(q[0]*q[2] - q[3]*q[1]));
                        double roll = Math.round(Math.toDegrees(Math.atan2(2.0*(q[0]*q[1] + q[3]*q[2]), q[3]*q[3] + q[0]*q[0] - q[1]*q[1] - q[2]*q[2])));
                        double angle =  Math.round(90 - Math.toDegrees(Math.atan2(goal.getY() - myLoc[1], goal.getX() - myLoc[0])));
                        double angDif = angle - roll;

                        final String log = " orientation : " + roll +
                                           " angle : " + angle +
                                           " dif : " + Math.round(angDif);
                        printLog(log);
                        if (angDif > 180) {
                            angDif = 360 - angDif;
                        }
                        if (angDif < -180){
                            angDif = -(360 + angDif);
                        }
                        // TODO ANGLES ADJ
                        if (angDif < 5 && angDif > -5) {
                            // GO AHEAD - NO ANG VELOCITY
                            sendControl(3, 50);
                            continue;
                        } else if (angDif < 25 && angDif > -25) {
                            // GO AHEAD WITH ANG VELOCITY
                            if (angDif > 0) {
                                // POSITIVE VEL
                                sendControl(4,50);
                                continue;
                            } else {
                                // NEGATIVE VEL
                               sendControl(2, 50);
                            }
                        } else {
                            if (angDif > 0) {
                                // LEFT TURN
                                sendControl(1, 50);
                            } else {
                                // RIGHT TURN
                                sendControl(5, 50);
                            }
                        }
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // STOP
                sendControl(0, 50);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mIsRelocalized = false;
        synchronized (this) {
            try {
                mTango.disconnect();
            } catch (TangoErrorException e) {
                Log.e(TAG, getString(R.string.tango_error), e);
            }
        }
    }

    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btnBind:
                // init connection to Robot
                initConnection();
                break;
            case R.id.save_poi_button:
                createPOIDlg();
                break;
        }
    }

    private void createPOIDlg(){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(LocalizationActivity.this);
        //builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Create POI");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(LocalizationActivity.this,
                android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("Waypoint");
        arrayAdapter.add("Door");
        arrayAdapter.add("Stair");
        arrayAdapter.add("Elevator");

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                if (poses[1] != null) {
                    POI poi = new POI (null, arrayAdapter.getItem(which), poses[1].translation[0], poses[1].translation[1]);
                    repositoryPOI.addPoi(poi);
                }
            }
        });
        builderSingle.show();
    }

    private void initConnection() {
        // get the MobileMessageRouter instance
        mMobileMessageRouter = MobileMessageRouter.getInstance();

        // you can read the IP from the robot app.
        mRobotIP = mEditText.getText().toString();
        try {
            mMobileMessageRouter.setConnectionIp(mRobotIP);

            // bind the connection service in robot
            mMobileMessageRouter.bindService(this, mBindStateListener);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Connection init FAILED", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Connection init FAILED", e);
        }
    }

    private byte[] packFile() {
        ByteBuffer buffer = ByteBuffer.allocate(mPointList.size() * 2 * 4 + 4);
        buffer.putInt(1);
        for(PointF pf : mPointList) {
            System.out.println(pf.x + " " + pf.y);
            buffer.putFloat(pf.x);
            buffer.putFloat(pf.y);
        }

        buffer.flip();
        byte[] messageByte = buffer.array();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
               // logText.setText("");
            }
        });

        return messageByte;
    }

    public void stopRobot() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        // protocol: the first 4 bytes is indicator of data or STOP message
        // 1 represent tracking data, 0 represent STOP message
        buffer.putInt(0);
        byte[] messageByte = buffer.array();
        try {
            mMessageConnection.sendMessage(new BufferMessage(messageByte));
        } catch(Exception e) {
            Log.e(TAG, "send STOP message failed", e);
        }
    }

    public void sendControl(int c, int size) {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        // protocol: the first 4 bytes is indicator of data or STOP message
        // 1 represent tracking data, 0 represent STOP message
        //printLog("command " + c);
        if (control != c) {
            control = c;
            Log.i("CONTROL", String.valueOf(c));
            buffer.putInt(c);
            byte[] messageByte = buffer.array();
            buffer.flip();
            try {
                mMessageConnection.sendMessage(new BufferMessage(messageByte));
            } catch(Exception e) {
            }
        }
    }

    private void logPointCloud(TangoPointCloudData pointCloudData) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Point count: " + pointCloudData.numPoints);
        stringBuilder.append(". Average depth (m): " +
                calculateAveragedDepth(pointCloudData.points, pointCloudData.numPoints));
        Log.i(TAG, stringBuilder.toString());
    }

    private float calculateAveragedDepth(FloatBuffer pointCloudBuffer, int numPoints) {
        float totalZ = 0;
        float averageZ = 0;
        if (numPoints != 0) {
            int numFloats = 4 * numPoints;
            for (int i = 2; i < numFloats; i = i + 4) {
                totalZ = totalZ + pointCloudBuffer.get(i);
            }
            averageZ = totalZ / numPoints;
        }
        return averageZ;
    }

    private String poseToString(TangoPoseData pose) {

        StringBuilder stringBuilder = new StringBuilder();
        DecimalFormat threeDec = new DecimalFormat("0.000");
        float translation[] = pose.getTranslationAsFloats();
        stringBuilder.append("Position: ( " +
                threeDec.format(translation[0]) + " , "+
                threeDec.format(translation[1]) + " ) \n");

        float orientation[] = pose.getRotationAsFloats();
        stringBuilder.append("Orientation: ( " +
                threeDec.format(orientation[0]) + " , " +
                threeDec.format(orientation[1]) + " , " +
                threeDec.format(orientation[2]) + " , " +
                threeDec.format(orientation[3]) + " )");

        return stringBuilder.toString();
    }

    private void printLog(final String log){
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              logText.setText(log + " " + control);
                          }
                      }
        );
    }

    private void printPOI(final String p){
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              path.setText(p);
                          }
                      }
        );
    }


}
