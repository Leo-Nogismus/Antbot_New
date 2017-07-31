package insectsrobotics.visualnavigationapp.Receiver_and_Broadcaster.Receiver;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import insectsrobotics.visualnavigationapp.R;


public class CombinerReceiver {

    Resources resources;

    public CombinerReceiver(Resources res) {
        resources = res;
    }

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(resources.getString(R.string.VisualNavigationRequestData))) {
            //TODO send new data to the Combiner App
        }
    }
}
