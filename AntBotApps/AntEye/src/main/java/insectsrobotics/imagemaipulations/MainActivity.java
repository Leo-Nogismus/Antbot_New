package insectsrobotics.imagemaipulations;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import insectrobotics.broadcastlibrary.BroadcastValues;
import insectsrobotics.imagemaipulations.Receiver_and_Broadcaster.Broadcast;
import insectsrobotics.imagemaipulations.Receiver_and_Broadcaster.Receive;


public class MainActivity extends Activity implements CvCameraViewListener2, BroadcastValues {
    private static final String TAG = "OCVSample::Activity";
    public Mat processedSourceImage;
    public Mat processedDestImage;
    public Mat displayedImage;
    Button learnImageBtn;
    Button calcErrorBtn;
    //Receiver and Broadcaster
    Broadcast broadcast;
    Receive receive;
    //Image processing variables
    Mat BlueChannel;
    MatOfInt from_to;
    Mat rgba;
    List<Mat> rgbaList;
    List<Mat> BlueChannelList;
    double[] pixel;


    //AdvancedSettings Data
    Bundle advancedSettingsBundle;


    //Calibrated Data variables
    double[] c_calib;
    double[] theta;
    double[] sinCoeffs;
    int imageCentreX;
    int imageCentreY;


    //Camera Parameters
    double imageCorrection;
    int cameraViewStartedWidth = 0;
    int cameraViewStartedHeight = 0;


    //Calibration resulting variables
    Bundle mBundle;
    double[] theta_new = new double[40];
    int[][][] directoryArray = new int[(theta_new.length + 1)][360][2];


    //Unwrapping variables
    int sourceResolution = 1;
    int resolution = 4;


    //Broadcast state distinguishers
    boolean visualNavigationRunning = false;
    boolean transmissionRunning = false;


    //Background processor
    AsyncTask<Object, Integer, Boolean> broadcastImage = null;


    //Broadcast Code (1 = calculate Error; 2 = learn Image)
    int TRANSMISSION_CODE = 1;
    boolean learnImage;
    boolean imageLearned = false;
    boolean firstStart = true;
    boolean firstLearnedImage = true;


    //Module Selection from StartScreen
    String visualModule;
    String pathIntegratorModule;
    String combinerModule;
    boolean notification = true;

    //Layout Views
    TextView serialConnectionTextView;
    TextView serverConnectionTextView;
    ProgressBar serialProgressBar;
    ProgressBar serverProgressBar;
    ImageView serialCheckImageView;
    ImageView serverCheckImageView;

    //DEBUG variables to save and load images from txt files
    boolean saveImages = false;
    boolean loadImages = false;
    int numberOfImages = 0;
    int errorCounter = 0;
    int numberOfErrorCalculations = 50;
    int errorCounter100 = 0;
    Handler learnHandler = new Handler();
    File sdCard = Environment.getExternalStorageDirectory();
    File dir = new File(sdCard.getAbsolutePath() + "/");
    File file = new File(dir, "text.txt");


    //Initiate all ServiceConnections for all Background Services
    ServiceConnection visualNavigationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    ServiceConnection serialServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    ServiceConnection combinerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    ServiceConnection integratorServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    StringBuilder errorString = new StringBuilder();
    StringBuilder perfectMemoryErrorString = new StringBuilder();

    //See learnButton onClickListener
    Runnable learnRunnable = new Runnable() {
        @Override
        public void run() {
            if (!saveImages && !loadImages) {
                errorCounter = 0;
            } else {
                errorCounter = numberOfErrorCalculations;
            }
            learnImage = true;
            transmissionRunning = false;
            notification = true;
            errorString = new StringBuilder();
            perfectMemoryErrorString = new StringBuilder();
            calcErrorBtn.setVisibility(View.VISIBLE);
        }
    };

    //The UI Views
    private CameraBridgeViewBase mOpenCvCameraView;
    //Start OpenCV, open CameraBridge
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    /**
     * Declare a Receiver and its Listener. The Receiver implements the Listener. On a receive with
     * an intent with the in the IntentFilter declared action, the Listeners onReceive Method is
     * called.
     */

    Receive.ReceiveListener receiveListener = new Receive.ReceiveListener() {
        @Override
        public void onNewMessageFromSerialConnectionApp(Intent intent, String action) {
            if (action.equals(SERIAL_CONNECTION_ESTABLISHED)){
                if (intent.getBooleanExtra("MainData", false)){
                    serialProgressBar.setVisibility(View.GONE);
                    serialCheckImageView.setVisibility(View.VISIBLE);
                } else {
                    serialProgressBar.setVisibility(View.VISIBLE);
                    serialCheckImageView.setVisibility(View.GONE);
                }
            } else if (action.equals(SERVER_CONNECTION_ESTABLISHED)){
                if (intent.getBooleanExtra("MainData", false)) {
                    serverProgressBar.setVisibility(View.GONE);
                    serverCheckImageView.setVisibility(View.VISIBLE);
                } else {
                    serverProgressBar.setVisibility(View.VISIBLE);
                    serverCheckImageView.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onNewMessageFromVisualNavigationApp(Intent intent, String action) {
            if (action.equals(NUMBER_OF_IMAGES)){
                numberOfImages = intent.getIntExtra("MainData", 0);
            } else if (action.equals(STATUS_UPDATE)){
                visualNavigationRunning = intent.getBooleanExtra("MainData", false);
            }
        }

        @Override
        public void onNewMessageFromCombinerApp(Intent intent, String action) {
        }
        @Override
        public void onNewMessageFromIntegratorApp(Intent intent, String action) {
        }
        @Override
        public void onNewMessageFromAntEyeApp(Intent intent, String action) {
        }
    };

    //MainActivity Constructor
    public MainActivity() {
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //First Bind all other services from apps to this one
        Intent visualNavigationServiceIntent = new Intent(VISUAL_SERVICE);
        bindService(visualNavigationServiceIntent, visualNavigationServiceConnection, BIND_AUTO_CREATE);

        Intent combinerServiceIntent = new Intent(COMBINER_SERVICE);
        bindService(combinerServiceIntent, combinerServiceConnection, BIND_AUTO_CREATE);

        Intent integratorServiceIntent = new Intent(INTEGRATOR_SERVICE);
        bindService(integratorServiceIntent, integratorServiceConnection, BIND_AUTO_CREATE);

        Intent serialServiceIntent = new Intent(SERIAL_SERVICE);
        bindService(serialServiceIntent, serialServiceConnection, BIND_AUTO_CREATE);

        //Initiate all Receiver and Broadcaster
        broadcast = new Broadcast(this,getResources());
        receive = new Receive(receiveListener);
        IntentFilter intentFilter = receive.getIntentFilter();
        registerReceiver(receive, intentFilter);

        //Inflate Layout and initiate the Views
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_image_manipulations);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById
                (insectsrobotics.imagemaipulations.Build.R.id.main_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        calcErrorBtn = (Button) findViewById(R.id.calcErrorBtn);
        learnImageBtn = (Button) findViewById(R.id.learnImageButton);
        CheckBox saveImageCheckBox = (CheckBox) findViewById(R.id.saveImageCheckBox);
        CheckBox useSavedCheckBox = (CheckBox) findViewById(R.id.useSavedCheckBox);

        serialConnectionTextView = (TextView) findViewById(R.id.serialConnectionTextView);
        serverConnectionTextView = (TextView) findViewById(R.id.serverConnectionTextView);
        serialProgressBar = (ProgressBar) findViewById(R.id.serialProgressBar);
        serverProgressBar = (ProgressBar) findViewById(R.id.serverProgressBar);
        serialCheckImageView = (ImageView) findViewById(R.id.serialCheckImageView);
        serverCheckImageView = (ImageView) findViewById(R.id.serverCheckImageView);
        serialCheckImageView.setImageResource(R.drawable.checksymbol);
        serverCheckImageView.setImageResource(R.drawable.checksymbol);

        //Set View action listeners
        learnImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Learn Image Button Clicked");
                learnHandler.postDelayed(learnRunnable, 10000);
            }
        });

        calcErrorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Calculate Error Button Clicked");
                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        learnHandler.removeCallbacks(learnRunnable);
                        errorCounter = 0;
                        if (loadImages) {
                            numberOfErrorCalculations = numberOfImages; //visualReceiver.getNumberOfImages();
                        }
                        transmissionRunning = false;
                        notification = true;
                        errorString = new StringBuilder();
                        perfectMemoryErrorString = new StringBuilder();
                        Log.d(TAG, "Executing Calculate Error");
                        Log.e(TAG, "Number of Images: " + numberOfImages + " Number of ErrorCalculations: " + numberOfErrorCalculations + "Error Counter " + errorCounter);
                    }
                }, 10000);
            }
        });

        saveImageCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveImages = isChecked;
            }
        });

        useSavedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loadImages = isChecked;
                if (isChecked) {
                    numberOfErrorCalculations = numberOfImages; //visualReceiver.getNumberOfImages();
                    Log.e(TAG, "Number of Images: " + numberOfErrorCalculations);
                } else {
                    numberOfErrorCalculations = 50;
                }
            }
        });


        //Loads calibration_layout data from passed Intent (Coming from StartScreen) and Advanced Settings
        Intent intent = getIntent();
        //Get Advanced Settings selection:
        advancedSettingsBundle = intent.getBundleExtra("ServerConnection");
        Log.e(TAG,"ServerAddress: " + advancedSettingsBundle.get("ServerAddress"));
        //Get Data from Intent
        mBundle = intent.getBundleExtra("Data");
        //Get Module Selection from Bundle
        visualModule = mBundle.getString(getResources().getString(R.string.AntEyeVNModuleSelection), "NoModuleSelected");
        pathIntegratorModule = mBundle.getString(getResources().getString(R.string.AntEyePIModuleSelection), "NoModuleSelected");
        combinerModule = mBundle.getString(getResources().getString(R.string.AntEyeCombinerModuleSelection), "NoModuleSelected");
        //Get Calibration Data From Bundle
        c_calib = mBundle.getDoubleArray("c_calib");
        theta = mBundle.getDoubleArray("theta");
        sinCoeffs = mBundle.getDoubleArray("sin");
        imageCentreX = (int) mBundle.getDouble("Xc");
        imageCentreY = (int) mBundle.getDouble("Yc");
        if (imageCentreY == 0 || imageCentreX == 0) {
            imageCentreX = 542;
            imageCentreY = 635;
        }
        Log.d(TAG, "CentreCoordinates: x: " + imageCentreX + " y: " + imageCentreY);


        //DEBUG: Create new File for error output
        try {
            FileOutputStream f = new FileOutputStream(file);
            f.write(("Measurements: " + "\n").getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }





    }

    //This has to be done since openCV crops images before the go in the "onNewFrame"-Method.
    //Therefore we have to adjust the View to the Camera parameters of the phone
    private void setCameraViewSize() {
        //Get Camera parameters (0 = BackCamera, 1= FrontCamera)
        Camera mCamera = Camera.open(1);
        Camera.Parameters params = mCamera.getParameters();
        Camera.Size pictureSize = params.getPictureSize();
        int pictureHeight = pictureSize.height;
        int pictureWidth = pictureSize.width;
        mCamera.release();

        Log.d(TAG, "Picture Height = " + pictureHeight + " Width = " + pictureWidth);

        //Used to set the Camera View accordingly
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        double width = size.x;

        //openCv also turns the image by 90°, so here the View is adjusted to the new parameters
        mOpenCvCameraView.getLayoutParams().height = (int) (width / (pictureWidth / pictureHeight));
        imageCorrection = pictureWidth;


    }

    /**
     * Disables CameraBridge/ view on activity pause to prevent memory leaks
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    /**
     * Enables view again on resume of the activity
     */
    @Override
    public void onResume() {

        super.onResume();
        Log.d("onResume", "onResume called");
        imageCorrection = 0;
        transmissionRunning = false;
        visualNavigationRunning = false;

        setCameraViewSize();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        if (cameraViewStartedWidth != 0 && cameraViewStartedHeight != 0) {
            onCameraViewStarted(cameraViewStartedWidth, cameraViewStartedHeight);
        }

    }

    /**
     * Disables view to prevent memory leaks after the activity was destroyed
     */
    public void onDestroy() {
        super.onDestroy();
        unbindService(visualNavigationServiceConnection);
        unbindService(combinerServiceConnection);
        unbindService(serialServiceConnection);
        unbindService(integratorServiceConnection);
        unregisterReceiver(receive);
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    /**
     * Called after successful initiation of the camera connection, initiates Variables, sets up
     * a connection array between the "Donut-View" and the unwrapped image.
     */


    public void onCameraViewStarted(int width, int height) {
        Log.e(TAG, "onCameraViewStarted called");

        //Send chosen modules
        broadcast.broadcastModules(visualModule, pathIntegratorModule, combinerModule);
        //visualBroadcast.broadcastModule(visualModule);
        //pathIntegratorBroadcaster.broadcastModule(pathIntegratorModule);
        //combinerBroadcaster.broadcastModule(combinerModule);

        //Send a Bundle with the Server information to the SerialCommunicationApp
        //Bundle mBundle = new Bundle();
        //mBundle.putString("ServerAddress", "192.168.43.161");
        //mBundle.putInt("Port", 8080);
        broadcast.broadcastServerConnection(advancedSettingsBundle);
        //serialCommunicationBroadcast.broadcastServerConnection(advancedSettingsBundle);


        cameraViewStartedWidth = width;
        cameraViewStartedHeight = height;

        if (firstStart) {
            imageCorrection = imageCorrection / width;
            buildConnectivityArray(imageCorrection);
            firstStart = false;
        }

        broadcastImage = new BroadcastImage();


    }

    /**
     * Build up an Array including calibration data for the unwrapping of the image, called in
     * onCameraViewStarted
     *
     * @param imageCorrection variable from the CameraView adjustment
     */
    public void buildConnectivityArray(double imageCorrection) {


        Log.d("buildConnectivityArray", "imageCorrection = " + imageCorrection);
        //Finishing the calibration_layout, using Horner's method to assess the Newtons-Polynomial built in
        //the Calibration-Activity.
        int ind = 0;
        for (int d = 0; d < theta_new.length; d = d + sourceResolution) {             //d equals the elevation angle  of the wanted view
            double b = c_calib[c_calib.length - 1];
            for (int r = c_calib.length - 2; r >= 0; r--) {
                b = b * (Math.toRadians(d) - theta[r]) + c_calib[r];
            }
            theta_new[ind] = b / imageCorrection;
            ind++;
        }

        //Building the connection array with use of the outcome of the Horner's method.
        double[] x_cal = new double[theta_new.length / sourceResolution + 1];      //X-Coordinates of the wanted azimuth and elevation
        double[] y_cal = new double[theta_new.length / sourceResolution + 1];      //Y-Coordinates of the wanted azimuth and elevation

        int degree = 0;
        double correction;
        //phi equals the azimuth of the wanted view
        for (int phi_tmp = -180; phi_tmp < 180; phi_tmp = phi_tmp + sourceResolution) {

            //Image adjustment to compensate errors due to lens misplacement
            //Still not quite right, but I don't know how to do it better. it's now hardcoded, so probably
            //Some of the stuff needs to be changed with a new phone...
            //TODO: Make an interactive adjustment of the sinus function which is then saved in the sharedPreferences
            correction = (Math.sin(Math.toRadians(phi_tmp)) * (13 / sourceResolution) + 13) /26;
            int j = 0;
            for (int i = 0; i < theta_new.length; i = i + sourceResolution) {

                //conversion from polar coordinates into needed cartesian coordinates
                y_cal[j] = (theta_new[j] * correction) * Math.sin(Math.toRadians(phi_tmp));
                x_cal[j] = (theta_new[j] * correction) * Math.cos(Math.toRadians(phi_tmp));

                //Final build up of the connection array
                directoryArray[j][degree][0] = (int) ((x_cal[j] + (imageCentreX / imageCorrection)));
                directoryArray[j][degree][1] = (int) ((y_cal[j] + (imageCentreY / imageCorrection)));

                j++;
            }
            degree++;
        }


    }

    public void onCameraViewStopped() {
    }

    /**
     * openCv Method with inputFrame from FrontCamera, imageProcessing and output to display.
     * At the same time the endless loop to do more or less all the work.
     *
     * @param inputFrame Frame from the cameraViewListener
     * @return the displayed Image for the Screen. Important: the returned Image has to be the same
     * size as the inputFrame!
     */
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        //Initiation of the needed Variables
        rgbaList = new ArrayList<>();
        BlueChannelList = new ArrayList<>();
        from_to = new MatOfInt(2, 0);
        rgba = new Mat();
        processedSourceImage = Mat.zeros(theta_new.length / sourceResolution, 360 / sourceResolution, CvType.CV_8UC1);
        processedDestImage = Mat.zeros(theta_new.length / resolution, 360 / resolution, processedSourceImage.type());

        rgba = inputFrame.rgba();                                           //Input Frame in rgba format
        BlueChannel = new Mat(rgba.rows(), rgba.cols(), CvType.CV_8UC1);    //Mat for later image processing


        double scale = rgba.width() / (360 / resolution);
        double rgbaHeight = rgba.height();
        displayedImage = Mat.zeros((int) (rgbaHeight / scale), 360 / resolution, CvType.CV_8UC1);

        rgbaList.add(rgba);                                                 //Needed for channel extraction from rgba image
        BlueChannelList.add(BlueChannel);                                   //Needed for channel extraction from rgba image
        Core.mixChannels(rgbaList, BlueChannelList, from_to);               //Extract only the blue channel from rgba

        Imgproc.equalizeHist(BlueChannel, BlueChannel);

        //Takes the image and unwraps it to the Azimuth-Elevation format. Always in a 1x1° resolution
        int counter1 = 0;
        int counter3;
        for (int phi = 0; phi < 360; phi = phi + sourceResolution) {
            counter3 = 0;
            for (int theta_tmp = 0; theta_tmp < theta_new.length; theta_tmp = theta_tmp + sourceResolution) {
                pixel = BlueChannel.get(directoryArray[counter3][counter1][0], directoryArray[counter3][counter1][1]);
                processedSourceImage.put(counter3, counter1, pixel);
                counter3++;
            }
            counter1++;
        }


        broadcast.broadcastImageForServer(processedSourceImage);
        //serialCommunicationBroadcast.broadcastImageForServer(processedSourceImage);
        /**
         * If the output resolution is different from 1x1° we have to down-sample the image.
         * openCVs RegionOfInterest method is perfect for this cause.
         *
         */
        if (resolution != 1) {
            int destAzimuthCounter = 0;
            for (int azimuth = 0; azimuth < processedSourceImage.cols(); azimuth = azimuth + resolution) {
                int destElevationCounter = 0;
                for (int elevation = 0; elevation < processedSourceImage.rows(); elevation = elevation + resolution) {
                    Rect roi;
                    //New Rectangle with the target resolution, later the ROI of the frame.
                    roi = new Rect(azimuth, elevation, resolution, resolution);
                    if (processedSourceImage.cols() - azimuth < resolution) {
                        roi = new Rect(azimuth, elevation, processedSourceImage.cols() - azimuth, resolution);
                    }
                    if (processedSourceImage.rows() - elevation < resolution) {
                        roi = new Rect(azimuth, elevation, resolution, processedSourceImage.rows() - elevation);
                    }
                    //Getting the pixels of the Region of Interest and averaging the values.
                    Mat ROI = processedSourceImage.submat(roi);
                    int ROIMean = (int) Core.mean(ROI).val[0];
                    processedDestImage.put(destElevationCounter, destAzimuthCounter, ROIMean);
                    destElevationCounter++;

                }
                destAzimuthCounter++;
            }
        }
        /**
         * If the output resolution is the same as the source resolution, we can use the same Image
         */
        else {
            processedDestImage = processedSourceImage;
        }


        //Image must be flipped due to the double mirror system of the lens
        Core.flip(processedDestImage, processedDestImage, -1);


        Log.d("Before Broadcast", "visualNavigationRunning = " + visualNavigationRunning);
        Log.d("Before Broadcast", "transmissionRunning = " + transmissionRunning);

        /** "visualNavigationRunning" is a boolean to check, if the VisualNavigationApp is finished
         * with the calculations. If not we don't send out a new Image and wait for it to finish up.
         * transmissionRunning is a boolean to check if the background
         * transmission is still in progress.
         */

        if (!transmissionRunning && !visualNavigationRunning) {
            transmissionRunning = true;

            //This Async-Task needs to be initiated every time, since Androids Async is made to only run once
            broadcastImage = new BroadcastImage();
            Log.i(TAG, "ErrorCounter: " + errorCounter + " imageLearned: " + imageLearned);

            //Sends Image to be learned
            if (learnImage) {
                try {
                    TRANSMISSION_CODE = 2;
                    //If its the first Image we will learn we have to set up the learning first.
                    //In the VisualNavigationApp, a 3 stands for setting up the learning algorithm.
                    if (firstLearnedImage) {
                        firstLearnedImage = false;
                        TRANSMISSION_CODE = 3;
                    }
                    transmissionRunning = broadcastImage.execute(processedDestImage, TRANSMISSION_CODE).get();
                    TRANSMISSION_CODE = 1;
                    learnImage = false;
                    imageLearned = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
            //DEBUG: Used for turning experiments to save 50 Error calculations aat every position
            else if (imageLearned && errorCounter < numberOfErrorCalculations) {
                try {
                    Log.d("Async", "Executing, TRANSMISSION_CODE: " + TRANSMISSION_CODE);
                    transmissionRunning = broadcastImage.execute(processedDestImage, TRANSMISSION_CODE).get();

                    errorCounter100++;
                    errorCounter++;

                } catch (Exception e) {
                    e.printStackTrace();

                    transmissionRunning = false;
                }
            }
            //DEBUG: Used for turning experiments to save 50 Error calculations aat every position
            else if (imageLearned && errorCounter == numberOfErrorCalculations) {
                if (notification) {
                    try {
                        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notificationSound);
                        r.play();
                        Log.e(TAG, "WillshawError: " + errorString.toString());
                        Log.e(TAG, "PerfectMemoryError: " + perfectMemoryErrorString.toString());
                        notification = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                transmissionRunning = false;
            }
            //DEBUG: Used for turning experiments to save 50 Error calculations aat every position
            if (imageLearned && errorCounter100 == 50) {


                //Log.e(TAG, "Environment: " + sdCard + " Directory: " + dir);

                try {
                    Log.e(TAG, "Writing to file");
                    FileOutputStream f = new FileOutputStream(file, true);


                    f.write(("WillshawError: " + errorString.toString() + "\n").getBytes());
                    f.write(("PerfectMemoryError: " + perfectMemoryErrorString.toString() + "\n").getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                errorString = new StringBuilder();
                perfectMemoryErrorString = new StringBuilder();
                errorCounter100 = 0;
            }
        }



        //Here we put the processed image back into a Mat with the proportions of the output image
        processedDestImage.copyTo(displayedImage.rowRange(displayedImage.rows() - processedDestImage.rows(), displayedImage.rows())
                .colRange(0, displayedImage.cols()));

        //Resizing of the Image to fit the size of the JavaImageView
        Size size = new Size(rgba.width(), rgba.height());
        Imgproc.resize(displayedImage, displayedImage, size);

        //Normalisation of the Image
        //CLAHE imageNormalizer = Imgproc.createCLAHE();
        //imageNormalizer.apply(processedSourceImage,processedSourceImage);



        return displayedImage;
    }

    /**
     * The broadcast of the image takes place in a different thread so we have a proper running UI
     */
    private class BroadcastImage extends AsyncTask<Object, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Object... transmission) {
            Mat matImage = (Mat) transmission[0];
            int REQUEST_CODE = (int) transmission[1];
            if (saveImages) {
                if (REQUEST_CODE == 2) {
                    REQUEST_CODE = 9;
                } else {
                    REQUEST_CODE = 10;
                }
            } else if (loadImages) {
                if (REQUEST_CODE == 2) {
                    REQUEST_CODE = 7;
                } else {
                    REQUEST_CODE = 11 + errorCounter;
                }
            }
            Log.d("BackgroundThread", "REQUEST_CODE: " + REQUEST_CODE);

            //Since there is no possibility to send a 2D array or Mat file the image is transformed to a 1D int array
            byte[] imageArray_tmp = new byte[matImage.height() * matImage.width()];
            matImage.get(0, 0, imageArray_tmp);
            int[] imageArray = new int[imageArray_tmp.length];
            for (int n = 0; n < imageArray_tmp.length; n++) {
                imageArray[n] = (int) imageArray_tmp[n] & 0xFF;
            }
            broadcast.broadcastImage(imageArray, TRANSMISSION_CODE);

            if (TRANSMISSION_CODE == 2) {
                TRANSMISSION_CODE = 1;
            }
            return false;
        }
    }

    //Broadcast receiver to know, when to send Images

    /* TODO: Include this stuff in other Receivers and Broadcasts
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "On Receive Called");
            String action = intent.getAction();
            Bundle receivedBundle;
            double error = 0;
            double perfectMemoryError = 0;
            String method = "";


            if (action.equals(getResources().getString(R.string.visualNavigationBroadcast))) {

                    boolean dataAvailable = intent.getBooleanExtra(getResources().getString(R.string.VisualAppSuccessful), false);
                    if (!visualNavigationRunning && dataAvailable) {
                        Log.d(TAG, "onReceive, loading Bundle Extra");
                        receivedBundle = intent.getBundleExtra(getResources().getString(R.string.VisualAppBundle));
                        method = receivedBundle.getString(getResources().getString(R.string.VABundleMethod), "");
                        if (method.equals("ErrorCalculation")) {
                            error = receivedBundle.getDouble(getResources().getString(R.string.VABundleError));
                            perfectMemoryError = receivedBundle.getDouble("PerfectMemoryError");
                        }
                    }
                    if (!visualNavigationRunning) {
                        if (method.equals("ErrorCalculation")) {

                            //errorCounter ++;
                            Log.d(TAG, "Method: " + method + " Familiarity = " + error);
                            errorString.append(" ").append(error);
                            perfectMemoryErrorString.append(" ").append(perfectMemoryError);
                        } else {
                            Log.i(TAG, "Method" + method);
                        }
                    }

            }
        }
    };

    */
}