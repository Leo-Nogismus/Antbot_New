package insectsrobotics.visualnavigationapp.Receiver_and_Broadcaster;

import android.content.Intent;

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
    VisualNavigationListener visualListener = new VisualNavigationListener() {
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
                    listener.onNewMessageFromAntEyeApp(intent, actionMap.get(ACTION));
            }
        }
    };

    public Receive(ReceiveListener listener){
        super();
        super.setVisualNavigationListener(visualListener);
        this.listener = listener;
    }


}
