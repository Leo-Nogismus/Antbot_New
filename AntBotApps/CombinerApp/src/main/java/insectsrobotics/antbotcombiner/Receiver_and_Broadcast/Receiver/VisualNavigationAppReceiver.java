package insectsrobotics.antbotcombiner.Receiver_and_Broadcast.Receiver;

import android.content.res.Resources;
import android.os.Bundle;


public class VisualNavigationAppReceiver {

    private static final String TAG = "CombinerApp";

    Resources resources;
    Bundle mBundle;
    String mainKey = "MainData";
    double error;

/*
    public VisualNavigationAppReceiver(Receiver.ReceiverListener listener, Resources res) {
        super(listener);
        resources = res;
    }

    //TODO: Do something with the visual information!

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(resources.getString(R.string.VisualNavigationErrorBroadcast))) {
            mBundle = intent.getBundleExtra(mainKey);
            error = mBundle.getDouble(resources.getString(R.string.VABundleError));
            Log.e(TAG, "ErrorReceived : " + error);
        }
    }
    */
}
