package insectsrobotics.visualnavigationapp.Receiver_and_Broadcaster;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import insectrobotics.broadcastlibrary.BroadcastValues;
import insectrobotics.broadcastlibrary.Broadcaster;


public class Broadcast extends Broadcaster implements BroadcastValues {

    private static final String TAG = "OCVSample::Activity";

    Resources resources;

    public Broadcast(Context context, Resources res) {
        super(context);
        resources = res;

    }


    public void broadcastStatusUpdate(boolean inProgress){
        initiateBroadcast(STATUS_UPDATE_BROADCAST);
        onNewDataToBroadcast(inProgress);
        executeBroadcast();
    }

    public void broadcastNumberOfImages(int numberOfImages){
        initiateBroadcast(NUMBER_OF_IMAGES_BROADCAST);
        onNewDataToBroadcast(numberOfImages);
        executeBroadcast();
    }

    public void broadcastFamiliarityError(Bundle error){
        Log.e(TAG, "Broadcast Error: " + error);
        initiateBroadcast(ERROR_BROADCAST);
        onNewDataToBroadcast(error);
        executeBroadcast();
    }













}
