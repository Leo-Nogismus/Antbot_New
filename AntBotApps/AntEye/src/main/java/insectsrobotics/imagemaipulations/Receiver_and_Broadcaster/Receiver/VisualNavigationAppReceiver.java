package insectsrobotics.imagemaipulations.Receiver_and_Broadcaster.Receiver;

import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

/**
 * Class to process information sent by the VisualNavigationApp
 * Will be changed to Listeners.
 * TODO: implement Listeners run by the MainApplication
 */
public class VisualNavigationAppReceiver {


    String TAG = "OCVSample::Activity";

    Resources resources;
    boolean visualNavigationRunning;
    int numberOfImages = 0;

    public VisualNavigationAppReceiver(Resources res) {
        resources = res;
    }

    public int onNumberOfImagesReceive(Intent intent) {
        Log.d(TAG, "onReceive called, action: " + intent.getAction());
        //Check the action of the intent. This first action is for DEBUG purpose. Sends the number of images
        // saved on a txt file by the VNA

        if (intent.getIntExtra("MainData", -2) != -2) {

            if (intent.getIntExtra("MainData", -2) != -1) {
                numberOfImages = intent.getIntExtra("MainData", -2);
                Log.e(TAG, "NumberOfImages: " + numberOfImages);
            }
        }
        return numberOfImages;

    }

    public boolean onStatusUpdate(Intent intent) {
        //Here we get an update on the status of the VNA. If this returns "true" it means, that the
        //VNA is still in processing/ just started processing. "False" means that the processing is
        //finished and VNA waits for a new image to process.
        visualNavigationRunning = intent.getBooleanExtra("MainData", false);
        Log.d(TAG, "Boolean: " + visualNavigationRunning);
        return visualNavigationRunning;
    }

    //Called by the MainActivity to get the number of DEBUG images.
    public int getNumberOfImages() {
        return numberOfImages;
    }

    //Called by the MainActivity to check, if the VNA is still processing.
    public boolean getVisualNavigationRunning() {
        return visualNavigationRunning;
    }
}
