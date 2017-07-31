package insectsrobotics.antbotcombiner.Receiver_and_Broadcast;

import android.content.Intent;
import android.util.Log;

import java.util.HashMap;

import insectrobotics.broadcastlibrary.BroadcastValues;
import insectrobotics.broadcastlibrary.Receiver;

public class Receive extends Receiver implements BroadcastValues{

    public interface ReceiveListener{
        void onNewMessageFromSerialConnectionApp(Intent intent, String action);
        void onNewMessageFromCombinerApp(Intent intent, String action);
        void onNewMessageFromIntegratorApp(Intent intent, String action);
        void onNewMessageFromVisualNavigationApp(Intent intent, String action);
        void onNewMessageFromAntEyeApp(Intent intent, String action);
    }

    ReceiveListener listener;
    CombinerListener combinerListener = new CombinerListener() {
        @Override
        public void onNewReceive(Intent intent, HashMap<String, String> actionMap) {
            switch (actionMap.get(SOURCE)){
                case SERIAL:
                    listener.onNewMessageFromSerialConnectionApp(intent, actionMap.get(ACTION));
                    break;
                case COMBINER:
                    listener.onNewMessageFromCombinerApp(intent, actionMap.get(ACTION));
                    break;
                case INTEGRATOR:
                    listener.onNewMessageFromIntegratorApp(intent, actionMap.get(ACTION));
                    break;
                case VISUAL:
                    listener.onNewMessageFromVisualNavigationApp(intent, actionMap.get(ACTION));
                    break;
                case ANTEYE:
                    Log.e("Receive", "Message: " + intent.getAction());
                    listener.onNewMessageFromAntEyeApp(intent, actionMap.get(ACTION));
            }
        }
    };

    public Receive(ReceiveListener listener){
        super();
        super.setCombinerListener(combinerListener);
        this.listener = listener;
    }


}
