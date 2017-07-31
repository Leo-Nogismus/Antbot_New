package insectsrobotics.imagemaipulations.Receiver_and_Broadcaster.Receiver;

import android.content.res.Resources;


public class SerialCommunicationReceiver {

    Resources resources;
    boolean serialConnected = false;
    boolean serverConnected = false;

    public SerialCommunicationReceiver(Resources res) {
        resources = res;
    }


}
