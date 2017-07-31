package insectsrobotics.serialcommunicationapp.Receiver_and_Broadcaster.Receiver;

import android.content.Intent;


public class CombinerReceiver {

    public CombinerReceiver() {
    }

    public String getDecision(Intent intent){
        return intent.getStringExtra("MainData");
    }
}
