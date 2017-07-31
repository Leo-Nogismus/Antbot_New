package insectsrobotics.serialcommunicationapp.Receiver_and_Broadcaster.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class AntEyeReceiver extends BroadcastReceiver{
    private static final String TAG = "AntEyeReceiver";

    Intent intent;

    public AntEyeReceiver (){
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Map<String, String> actionMap;
        Log.e(TAG,intent.getAction());
        String action = intent.getAction();
        String[] actionArray = action.split("\\.");
        Log.e(TAG, "StringArray: " + actionArray.length);
        if (intent.getAction().contains("insectsrobotics.imagemaipulations.serialcommunicationapp")) {
            if (actionArray[2].equals("serialcommunicationapp")) {
                Log.e(TAG, intent.getBundleExtra("MainData").getString("ServerAddress"));
            }
        }
    }

    public Bundle getServerConnection(Intent intent){
        Log.e(TAG,"getServerConnectionCalled: " + intent.getBundleExtra("MainData").getString("ServerAddress"));
        return intent.getBundleExtra("MainData");
    }
}
