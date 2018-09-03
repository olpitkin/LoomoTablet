package com.segway.robot.TrackingSample_Phone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
import com.segway.robot.TrackingSample_Phone.repository.RepositoryInfo;
import com.segway.robot.TrackingSample_Phone.repository.RepositoryPOI;
import com.segway.robot.TrackingSample_Phone.util.PathFinding;
import com.segway.robot.mobile.sdk.connectivity.MobileMessageRouter;
import com.segway.robot.mobile.sdk.connectivity.StringMessage;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.baseconnectivity.Message;
import com.segway.robot.sdk.baseconnectivity.MessageConnection;
import com.segway.robot.sdk.baseconnectivity.MessageRouter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Alex Pitkin on 28.09.2017.
 */

public class LocalizationActivity extends Activity implements
        SetAdfNameDialog.CallbackListener,
        SaveAdfTask.SaveAdfListener {

    // UI
    private TextView logText;
    private TextView mUuidTextView;
    private TextView mRelocalizationTextView;
    private TextView relocPose;
    private TextView path;
    private int relocCount;

  //  private Button mSaveAdfButton;
    private ToggleButton mManualButton;
    private ToggleButton mAudioButton;
    private ToggleButton mTactileButton;
    private Button debugButton;

    private Button wButton;
    private Button sButton;
    private Button aButton;
    private Button dButton;

    // LOOMO
    private static final String TAG = "TrackingActivity_Phone";
    private EditText mEditText;
    private String mRobotIP;
    private MobileMessageRouter mMobileMessageRouter = null;
    private MessageConnection mMessageConnection = null;

    private LinkedList<POI> mPOIList;
    private RepositoryPOI repositoryPOI = new RepositoryPOI();
    private RepositoryInfo repositoryInfo = new RepositoryInfo();

    //TANGO
    private Tango mTango;
    private TangoConfig mConfig;
    private TangoPoseData poses[] = new TangoPoseData[3];

    private boolean mIsRelocalized;
    private boolean mIsLearningMode;
    private boolean mIsConstantSpaceRelocalize;
    private SaveAdfTask mSaveAdfTask;
    private final Object mSharedLock = new Object();

    public static final String USE_AREA_LEARNING ="com.segway.robot.TrackingSample_Phone.usearealearning";
    public static final String LOAD_ADF = "com.segway.robot.TrackingSample_Phone.loadadf";

    //NAVIGATION
    private POI start;
    private POI goal;
    private boolean isTurning = false;
    private boolean turningLock = false;
    private boolean isCorrecting = false;
    private int control;

    private boolean isTactile = false;
    private boolean isAudio = true;
    private boolean isManual = false;

    Thread movingThread;
    Thread controlThread;

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
            if (message instanceof StringMessage) {
                //message received is StringMessage
                final String mes = message.getContent().toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mUuidTextView.setText(mes);
                    }
                });
                    switch (mes) {
                        default:
                            //mPOIList = (LinkedList) repositoryPOI.getPOIonDescription(mes);
                            POI poiTarget = repositoryPOI.getPOIonDescription(mes).get(0);
                            if (poiTarget == null)
                                break;
                            Log.e("target", poiTarget.toString());
                            PathFinding pathFinding = new PathFinding();
                            POI myLocation = new POI ("myLoc", "-", poses[1].translation[0], poses[1].translation[1]);
                            POI startPoi = pathFinding.getNearestPOI(myLocation);
                            pathFinding.computePaths(startPoi);
                            mPOIList = pathFinding.getShortestPathTo(poiTarget);
                            new AsyncRequest().execute("GO", null, null);
                            isTurning = true;
                            turningLock = false;
                            break;
                    }
                        startMoving();
                        controlRobot();
            }
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

    private void runDebug() {
        if (false) {
            isTurning = true;
            turningLock = false;
            POI poiTarget = repositoryPOI.getPOIonDescription("start").get(0);
            PathFinding pathFinding = new PathFinding();
            POI myLocation = new POI ("myLoc", "-", poses[1].translation[0], poses[1].translation[1]);
            POI startPoi = pathFinding.getNearestPOI(myLocation);
            pathFinding.computePaths(startPoi);
            mPOIList = pathFinding.getShortestPathTo(poiTarget);
            startMoving();
            controlRobot();
        }

        new AsyncRequest().execute("TURNR", null, null);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.localization_activity);
        Intent intent = getIntent();
        mIsLearningMode = intent.getBooleanExtra(MainActivity.USE_AREA_LEARNING, false);
        mIsConstantSpaceRelocalize = intent.getBooleanExtra(MainActivity.LOAD_ADF, false);
    }

    static class AsyncRequest extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... arg) {
            try {
                String IP = "127.0.0.1";
                String answer = "not connected";
                int PORT = 50000;
                String command = "!PlayPattern,";
                if (arg[0].equals("TURNL")) {
                    command += "turn_left";
                }
                else if (arg[0].equals("TURNR")) {
                    command += "turn_right";
                }
                else if (arg[0].equals("GO")) {
                    command += "move_forward";
                }
                else if (arg[0].equals("GOAL")) {
                    command += "two_circles";
                }
                InetAddress ipAddress = InetAddress.getByName(IP);
                DatagramSocket clientSocket = new DatagramSocket();
                clientSocket.setSoTimeout(500);

                byte[] receiveData = new byte[2048];
                byte[] sendData = command.trim().getBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, PORT);
                clientSocket.send(sendPacket);
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    clientSocket.receive(receivePacket);
                    answer = new String(receivePacket.getData());
                    answer = answer.substring(0,receivePacket.getLength());
                } catch (SocketTimeoutException e) {
                    System.out.println("SocketTimeoutException");
                }
            } catch (SocketException e) {
                System.out.println("SocketException");
            } catch (UnknownHostException e) {
                System.out.println("UnknownHostException");
            } catch (IOException e1) {
                System.out.println("IOException");
            }
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

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
    public void onAdfNameCancelled() {}

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
        TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        if (isLearningMode) {
            config.putBoolean(TangoConfig.KEY_BOOLEAN_LEARNINGMODE, true);
            config.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);
        }
        if (isLoadAdf) {
            ArrayList<String> fullUuidList;
            fullUuidList = tango.listAreaDescriptions();
            if (fullUuidList.size() > 0) {
                config.putString(TangoConfig.KEY_STRING_AREADESCRIPTION,
                        //fullUuidList.get(fullUuidList.size() - 1));
                        //fullUuidList.get(1));
                        fullUuidList.get(0));
            }
        }
        // TODO DEPTH OBSTACLE RECOGNITION
        //config.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        //config.putInt(TangoConfig.KEY_INT_DEPTH_MODE, TangoConfig.TANGO_DEPTH_MODE_POINT_CLOUD);
        return config;
    }

    private void setupTextViewsAndButtons(Tango tango, boolean isLearningMode, boolean isLoadAdf) {
       // mSaveAdfButton = (Button) findViewById(R.id.save_adf_button);
        mUuidTextView = (TextView) findViewById(R.id.adf_uuid_textview);
        mRelocalizationTextView = (TextView) findViewById(R.id.relocalization_textview);
        mEditText = (EditText) findViewById(R.id.etIP);
        mEditText.setText("192.168.1.2");
        relocPose = (TextView) findViewById(R.id.relocalization_pose_textview);
        logText = (TextView) findViewById(R.id.log);
        path = (TextView) findViewById(R.id.best_path);

        mEditText = (EditText) findViewById(R.id.etIP);
        mManualButton = (ToggleButton) findViewById(R.id.btnManual);
        mAudioButton = (ToggleButton) findViewById(R.id.btnAudio);
        mTactileButton = (ToggleButton) findViewById(R.id.btnTactile);

        mAudioButton.setChecked(true);

        wButton = (Button) findViewById(R.id.control_w);
        sButton = (Button) findViewById(R.id.control_s);
        aButton = (Button) findViewById(R.id.control_a);
        dButton = (Button) findViewById(R.id.control_d);

        debugButton = (Button) findViewById(R.id.debug_button);

//        if (isLearningMode) {
//            mSaveAdfButton.setEnabled(true);
//        } else {
//            mSaveAdfButton.setEnabled(false);
//        }

        if (isLoadAdf) {
            ArrayList<String> fullUuidList;
            // Returns a list of ADFs with their UUIDs.
            fullUuidList = tango.listAreaDescriptions();
            if (fullUuidList.size() == 0) {
                mUuidTextView.setText(R.string.no_uuid);
            } else {
                mUuidTextView.setText(getString(R.string.number_of_adfs) + fullUuidList.size());
                  //      + getString(R.string.latest_adf_is)
                  //      + fullUuidList.get(fullUuidList.size() - 1));
            }
        }

        wButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isManual) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        sendString("3", true);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        sendString("0", true);
                    }
                }
                return true;
            }
        });

        sButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isManual) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        sendString("6", true);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        sendString("0", true);
                    }
                }
                return true;
            }
        });

        aButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isManual) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        sendString("1", true);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        sendString("0", true);
                    }
                }
                return true;
            }
        });

        dButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isManual) {
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        sendString("5",true);
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        sendString("0",true);
                    }
                }
                return true;
            }
        });

        mManualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isManual = !isManual;
                sendString("MANUAL",false);
            }
        });

        mAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAudio = !isAudio;
            }
        });

        mTactileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isTactile = !isTactile;
            }
        });

        debugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runDebug();
            }
        });
    }

    public void startMoving() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mPOIList != null && !mPOIList.isEmpty() && goal == null) {
                        goal = mPOIList.poll();
                    }
                    if ((mPOIList == null && goal == null)) {
                        sendString("0",true);
                    }
                    if (poses[1] != null && goal != null) {
                       // printPOI(goal.toString());
                        POI myLocation = new POI ("myLoc", "-", poses[1].translation[0], poses[1].translation[1]);
                        if (myLocation.isVeryNear(goal)) {
                            if (mPOIList != null && mPOIList.isEmpty()) {
                                printPOI("");
                                sendString("GOAL", false);
                                new AsyncRequest().execute("GOAL", null, null);
                                isTurning = true;
                                turningLock = false;
                                isCorrecting = false;
                                start = null;
                                goal = null;
                                mPOIList = null;
                                continue;
                            }
                            start = goal;
                            goal = mPOIList.poll();
                            isTurning = true;
                            if (!mPOIList.isEmpty()) {
                                POI next = mPOIList.get(0);
                                String info = repositoryInfo.getInfoOnPOI(start,goal,next);
                                if (!info.isEmpty()) {
                                    if (isAudio) {
                                        sendString(info, false);
                                    }
                                    if (isTactile) {
                                        new AsyncRequest().execute(info, null, null);
                                    }
                                }
                                printPOI(goal.toString() + " " + info);
                            }
                        }
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        if (movingThread == null) {
            movingThread = new Thread(runnable);
            movingThread.start();
        } else if (movingThread.isInterrupted()) {
            movingThread.start();
        }
    }

    public void controlRobot() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                        if (goal == null || poses[1] == null)
                            continue;
                        float[] q = poses[1].getRotationAsFloats();
                        float[] myLoc = poses[1].getTranslationAsFloats();
                        //double yaw = Math.atan2(2.0*(q[1]*q[2] + q[3]*q[0]), q[3]*q[3] - q[0]*q[0] - q[1]*q[1] + q[2]*q[2]);
                        //double pitch = Math.asin(-2.0*(q[0]*q[2] - q[3]*q[1]));
                        double roll = Math.round(Math.toDegrees(Math.atan2(2.0 * (q[0] * q[1] + q[3] * q[2]), q[3] * q[3] + q[0] * q[0] - q[1] * q[1] - q[2] * q[2])));
                        double angleDirty = Math.toDegrees(Math.atan2(goal.getY() - myLoc[1], goal.getX() - myLoc[0]));
                        double angle = Math.round(angleDirty - 90);
                        double angDifDirty = angle - roll;
                        double angDif = angDifDirty;
                        if (angDifDirty > 180) {
                            angDif = 360 - angDifDirty;
                        } else if (angDifDirty < -180) {
                            angDif = 360 + angDifDirty;
                        }
                        if (isTurning) {
                            if (angDif < 5 && angDif > -5) {
                                isTurning = false;
                                turningLock = false;
                            }
                            else if (angDif > 0) {
                                final String log =
                                        " orientation: " + roll + " \n" +
                                  //      " angleD:" + Math.round(angleDirty) + " \n" +
                                        " angle: " + angle + " \n" +
                                        " dif: " + Math.round(angDifDirty) + " \n" +
                                        " dif2:" + Math.round(angDif) + " \n" + "TURNING";
                                    printLog(log);
                                if (!isManual && !turningLock) {
                                    // LEFT TURN
                                    turningLock = true;
                                    printControlSig("1T");
                                    sendString("1", true);
                                }
                            } else if (angDif < 0) {
                                final String log =
                                        " orientation: " + roll + " \n" +
                                                //      " angleD:" + Math.round(angleDirty) + " \n" +
                                        " angle: " + angle + " \n" +
                                        " dif: " + Math.round(angDifDirty) + " \n" +
                                        " dif2:" + Math.round(angDif) + " \n" + "TURNING";
                                printLog(log);
                                if (!isManual && !turningLock) {
                                    // RIGHT TURN
                                    turningLock = true;
                                    printControlSig("5T");
                                    sendString("5", true);
                                }
                            }
                        } else {
                            if (angDif < 15 && angDif > -15 && !isCorrecting) {
                                final String log = " orientation: " + roll + " \n" +
                                        //     " angleD:" + Math.round(angleDirty) + " \n" +
                                        " angle: " + angle + " \n" +
                                        " dif: " + Math.round(angDifDirty) + " \n" +
                                        " dif2:" + Math.round(angDif) + " \n" + "MOVING";
                                printLog(log);
                                if (!isManual) {
                                    // GO AHEAD - NO ANG VELOCITY
                                    printControlSig("3M");
                                    sendString("3", true);
                                }
                            }
                            else if (angDif < 70 && angDif > -70) {
                                isCorrecting = true;
                                if (angDif < 3 && angDif > -3) {
                                    isCorrecting = false;
                                }
                                // GO AHEAD WITH ANG VELOCITY
                                if (angDif > 0) {
                                    final String log = " orientation: " + roll + " \n" +
                                            " angleD:" + Math.round(angleDirty) + " \n" +
                                            " angle: " + angle + " \n" +
                                            " dif: " + Math.round(angDifDirty) + " \n" +
                                            " dif2:" + Math.round(angDif) + " \n" + "MOVING";
                                    printLog(log);
                                    if (!isManual) {
                                        // POSITIVE VEL
                                        printControlSig("2M");
                                        sendString("2", true);
                                    }
                                } else if (angDif < 0) {
                                    final String log = " orientation: " + roll + " \n" +
                                            " angleD:" + Math.round(angleDirty) + " \n" +
                                            " angle: " + angle + " \n" +
                                            " dif: " + Math.round(angDifDirty) + " \n" +
                                            " dif2:" + Math.round(angDif) +  " \n" + "MOVING";
                                    printLog(log);
                                    if (!isManual) {
                                        // NEGATIVE VEL
                                        printControlSig("4M");
                                        sendString("4", true);
                                    }
                                }
                            }
                            else {
                                    isTurning = true;
                                    turningLock = false;
                                }
                        }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        if (controlThread == null) {
            controlThread = new Thread(runnable);
            controlThread.start();
        } else if (controlThread.isInterrupted()) {
            controlThread.start();
        }
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
        mMobileMessageRouter = MobileMessageRouter.getInstance();
        mRobotIP = mEditText.getText().toString();
        try {
            mMobileMessageRouter.setConnectionIp(mRobotIP);
            mMobileMessageRouter.bindService(this, mBindStateListener);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Connection init FAILED", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Connection init FAILED", e);
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
                              logText.setText(log);
                          }
                      }
        );
    }

    private void sendString(String string, boolean control) {
        if (control) {
            if (this.control != Integer.parseInt(string)) {
                this.control = Integer.parseInt(string);
                if(mMessageConnection != null) {
                    try {
                        mMessageConnection.sendMessage(new StringMessage(string));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            if(mMessageConnection != null) {
                try {
                    mMessageConnection.sendMessage(new StringMessage(string));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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

    private void printControlSig(final String p){
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              mUuidTextView.setText(p);
                          }
                      }
        );
    }

}
