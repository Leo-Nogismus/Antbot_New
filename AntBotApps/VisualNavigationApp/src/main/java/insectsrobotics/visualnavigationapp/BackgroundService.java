package insectsrobotics.visualnavigationapp;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import insectrobotics.broadcastlibrary.BroadcastService;
import insectsrobotics.visualnavigationapp.NavigationModules.PerfectMemoryModule;
import insectsrobotics.visualnavigationapp.NavigationModules.WillshawModule;
import insectsrobotics.visualnavigationapp.Receiver_and_Broadcaster.Broadcast;
import insectsrobotics.visualnavigationapp.Receiver_and_Broadcaster.Receive;


public class BackgroundService extends BroadcastService {

    String TAG = "SuperClass NavigationModules";

    boolean firstCall = true;

    String moduleName;
    WillshawModule willshawModule;
    PerfectMemoryModule perfectMemoryModule;

    AsyncTask<Object, Void, Bundle> myTask;

    FileManager fileManager;

    Broadcast broadcast;
    Receive receive;

    Receive.ReceiveListener listener = new Receive.ReceiveListener() {
        @Override
        public void onNewMessageFromSerialConnectionApp(Intent intent, String action) {
            switch (action){
                case LEARN_IMAGE_COMMAND:
                    //TODO: Learn Image(s)
                    break;
                case RESET_LEARNING_COMMAND:
                    //TODO: Reset networks
                    break;
                default:
                    //Some Default Code
                    break;
            }
        }

        @Override
        public void onNewMessageFromCombinerApp(Intent intent, String action) {
            switch (action){
                case REQUEST_VN_DATA:
                    //TODO: Send The Error to the Combiner
                    break;
                default:
                    //Some Default Code if you want to.
                    break;
            }
        }
        @Override
        public void onNewMessageFromIntegratorApp(Intent intent, String action) {
        }
        @Override
        public void onNewMessageFromVisualNavigationApp(Intent intent, String action) {
        }

        @Override
        public void onNewMessageFromAntEyeApp(Intent intent, String action) {
            switch (action){
                case VISUAL_MODULE:
                    moduleName = intent.getStringExtra("MainData");
                    Log.e(TAG, "visualModule: " + moduleName);
                    break;
                case IMAGE:
                    Log.e(TAG,"Received Image");
                    broadcast.broadcastStatusUpdate(true);
                    calculateError(intent);
                    break;
            }
        }
    };



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBindCalled");

        broadcast = new Broadcast(this,getResources());
        receive = new Receive(listener);

        IntentFilter intentFilter = receive.getIntentFilter();
        registerReceiver(receive, intentFilter);

        fileManager = new FileManager();
        int numberOfImages = fileManager.loadNumberOfImages();
        Log.e(TAG, "number of images: " + numberOfImages);
        broadcast.broadcastNumberOfImages(numberOfImages);
        Toast.makeText(getApplicationContext(), "VisualNavigationService Connected", Toast.LENGTH_SHORT).show();
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("onUnbind", "Unbind called");
        Toast.makeText(getApplicationContext(), "VisualNavigationService Disconnected", Toast.LENGTH_SHORT).show();
        unregisterReceiver(receive);
        return super.onUnbind(intent);
    }



    private void calculateError(Intent intent){
        int REQUEST_CODE = intent.getIntExtra(getResources().getString(R.string.AntEyeTransmissionCode), 1);
        int[] imageArray = intent.getIntArrayExtra(getResources().getString(R.string.AntEyeImageArray));
        try {
            myTask = new BackgroundCalculations();
            if (REQUEST_CODE == 7) {
                imageArray = fileManager.loadLearnedImage();
                REQUEST_CODE = 2;
            } else if (REQUEST_CODE == 9) {
                fileManager.saveLearnedImage(imageArray);
                REQUEST_CODE = 2;
            } else if (REQUEST_CODE == 10) {
                fileManager.saveImage(imageArray);
            } else if (REQUEST_CODE > 10) {
                imageArray = fileManager.loadImage();
                REQUEST_CODE = 1;
            }

            if (REQUEST_CODE == 2) {
                if (firstCall) {
                    willshawModule = new WillshawModule(getResources());
                    REQUEST_CODE = 3;
                    firstCall = false;
                }
            }

            if (willshawModule != null) {
                myTask.execute(imageArray, REQUEST_CODE).get();
            } else {
                myTask.execute(imageArray,REQUEST_CODE, "ERROR");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class BackgroundCalculations extends AsyncTask<Object, Void, Bundle> {

        String TAG = this.getClass().getSimpleName();
        public BackgroundCalculations(){

        }
        Bundle mBundle = new Bundle();





        @Override
        protected Bundle doInBackground(Object... params) {

            String tmp;
            tmp = moduleName;
            String moduleName;
            moduleName = tmp;
            Log.e(TAG,"ModuleName: " + moduleName);
            mBundle = new Bundle();
            Bundle secBundle;

            if (moduleName.isEmpty() || params[3].equals("ERROR")) {
                moduleName = getResources().getString(R.string.NoVSModule);
            }
            Log.e("doInBackground123", moduleName);

            if (moduleName.equals(getResources().getString(R.string.VS1))) {

                mBundle = willshawModule.onNewImage((int[]) params[0], (int) params[1]);
                secBundle = perfectMemoryModule.onNewImage((int[]) params[0], (int) params[1]);
                double error = secBundle.getDouble(getResources().getString(R.string.VABundleError));
                mBundle.putDouble("PerfectMemoryError", error);


            } else if (moduleName.equals(getResources().getString(R.string.VS2))) {
                mBundle = perfectMemoryModule.onNewImage((int[]) params[0], (int) params[1]);
            } else {
                mBundle.clear();
            }

            return mBundle;
        }

        @Override
        protected void onPostExecute(Bundle bundle) {
            super.onPostExecute(bundle);
            broadcast.broadcastFamiliarityError(mBundle);
            broadcast.broadcastStatusUpdate(false);

        }
    }

    /*
    int n = 0;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("BroadcastReceiver", "onReceive Called");

            Intent broadcastIntent = new Intent(getResources().getString(R.string.visualNavigationBroadcast));
            broadcastIntent.putExtra(getResources().getString(R.string.VisualAppProcessing), true);
            sendBroadcast(broadcastIntent);

            String action = intent.getAction();

            Log.e("VisualNavigationApp", "Background Process Started");
            if (action.equals(getResources().getString(R.string.AntEyeBroadcast))) {

                int REQUEST_CODE = intent.getIntExtra(getResources().getString(R.string.AntEyeTransmissionCode), 1);
                int[] imageArray = intent.getIntArrayExtra(getResources().getString(R.string.AntEyeImageArray));
                Log.e("Something once again", "imageArray[25] = " + imageArray[25]);
                moduleName = intent.getStringExtra(getResources().getString(R.string.AntEyeModuleSelection));

                azimuth[0] = (int) sensorClass.getAzimuth();
                //Log.e(TAG, "REQUEST_CODE: " + REQUEST_CODE);
                Log.e("onReceive", moduleName);
                try {
                    myTask = new BackgroundCalculations();
                    if (REQUEST_CODE == 7) {
                        //imageArray = preferencesManager.loadLearnedImage(imageCounter);
                        imageArray = fileManager.loadLearnedImage(azimuth);
                        REQUEST_CODE = 2;
                        imageCounter++;
                    } else if (REQUEST_CODE == 9) {
                        //preferencesManager.saveLearnedImage(imageArray);
                        fileManager.saveLearnedImage(imageArray, azimuth);
                        REQUEST_CODE = 2;
                    } else if (REQUEST_CODE == 10) {
                        //preferencesManager.saveImage(imageArray);
                        fileManager.saveImage(imageArray, azimuth);
                    } else if (REQUEST_CODE > 10) {
                        //imageArray = preferencesManager.loadImage(REQUEST_CODE-11);
                        imageArray = fileManager.loadImage(azimuth);
                        REQUEST_CODE = 1;
                    }
                    Log.e(TAG, "Azimuth Background Service: " + azimuth[0]);

                    if (REQUEST_CODE == 2) {
                        if (firstCall) {
                            willshawModule = new WillshawModule(getResources());
                            REQUEST_CODE = 3;
                            firstCall = false;
                        }
                    }

                    myTask.execute(imageArray, REQUEST_CODE).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    };

*/






}













