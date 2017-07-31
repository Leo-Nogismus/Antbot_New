package insectsrobotics.pathintegrator.Receiver_and_Broadcaster;

import android.content.Context;
import android.content.res.Resources;

import insectrobotics.broadcastlibrary.BroadcastValues;
import insectrobotics.broadcastlibrary.Broadcaster;


public class Broadcast extends Broadcaster implements BroadcastValues {

    private static final String TAG = "OCVSample::Activity";

    Resources resources;

    public Broadcast(Context context, Resources res) {
        super(context);
        resources = res;
    }

    public void broadcastPathInformation(String path){
        initiateBroadcast(VECTOR_BROADCAST);
        onNewDataToBroadcast(path);
        executeBroadcast();
    }







}
