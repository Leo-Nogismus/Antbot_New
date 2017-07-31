package insectrobotics.broadcastlibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.HashMap;

/**
 * Here will be a few changes later. The superclass will be changed to Listeners.
 * TODO: implement Listeners (See also SerialCommunicationApp)
 */
public class Receiver extends BroadcastReceiver implements BroadcastValues{

    public interface AntEyeListener{
        void onNewReceive(Intent intent, HashMap<String, String> actionMap);
        String SOURCE = "Source";
        String DESTINATION = "Destination";
        String ACTION = "Action";
    }
    public interface SerialCommunicationListener{
        void onNewReceive(Intent intent, HashMap<String, String> actionMap);
        String SOURCE = "Source";
        String DESTINATION = "Destination";
        String ACTION = "Action";
    }
    public interface CombinerListener{
        void onNewReceive(Intent intent, HashMap<String, String> actionMap);
        String SOURCE = "Source";
        String DESTINATION = "Destination";
        String ACTION = "Action";
    }
    public interface IntegratorListener{
        void onNewReceive(Intent intent, HashMap<String, String> actionMap);
        String SOURCE = "Source";
        String DESTINATION = "Destination";
        String ACTION = "Action";
    }
    public interface VisualNavigationListener{
        void onNewReceive(Intent intent, HashMap<String, String> actionMap);
        String SOURCE = "Source";
        String DESTINATION = "Destination";
        String ACTION = "Action";
    }


    AntEyeListener antEyeListener;
    SerialCommunicationListener serialCommunicationListener;
    CombinerListener combinerListener;
    IntegratorListener integratorListener;
    VisualNavigationListener visualNavigationListener;
    IntentFilter intentFilter;
    String SOURCE = "Source";
    String DESTINATION = "Destination";
    String ACTION = "Action";

    public Receiver(){

    }

    public void setAntEyeListener(AntEyeListener antEyeListener) {
        this.antEyeListener = antEyeListener;
        intentFilter = new IntentFilter();
        intentFilter.addAction(SERIAL_CONNECTION_ESTABLISHED_BROADCAST);
        intentFilter.addAction(SERVER_CONNECTION_ESTABLISHED_BROADCAST);
        intentFilter.addAction(NUMBER_OF_IMAGES_BROADCAST);
        intentFilter.addAction(STATUS_UPDATE_BROADCAST);
    }

    public void setSerialCommunicationListener(SerialCommunicationListener serialCommunicationListener) {
        this.serialCommunicationListener = serialCommunicationListener;
        intentFilter = new IntentFilter();
        intentFilter.addAction(SERVER_CONNECTION_BROADCAST);
        intentFilter.addAction(IMAGE_SERVER_BROADCAST);
        intentFilter.addAction(DECISION_BROADCAST);
    }

    public void setCombinerListener(CombinerListener combinerListener) {
        this.combinerListener = combinerListener;
        intentFilter = new IntentFilter();
        intentFilter.addAction(COMBINER_MODULE_BROADCAST);
        intentFilter.addAction(ERROR_BROADCAST);
        intentFilter.addAction(VECTOR_BROADCAST);
        intentFilter.addAction(TASK_EXECUTED_BROADCAST);
        intentFilter.addAction(HOMING_ROUTING_BROADCAST);
    }

    public void setIntegratorListener(IntegratorListener integratorListener) {
        this.integratorListener = integratorListener;
        intentFilter = new IntentFilter();
        intentFilter.addAction(INTEGRATOR_MODULE_BROADCAST);
        intentFilter.addAction(WHEEL_ENCODER_BROADCAST);
        intentFilter.addAction(REQUEST_PI_DATA_BROADCAST);
    }

    public void setVisualNavigationListener(VisualNavigationListener visualNavigationListener) {
        this.visualNavigationListener = visualNavigationListener;
        intentFilter = new IntentFilter();
        intentFilter.addAction(VISUAL_MODULE_BROADCAST);
        intentFilter.addAction(IMAGE_BROADCAST);
        intentFilter.addAction(LEARN_IMAGE_COMMAND_BROADCAST);
        intentFilter.addAction(RESET_LEARNING_COMMAND_BROADCAST);
        intentFilter.addAction(REQUEST_VN_DATA_BROADCAST);
    }

    public Receiver(AntEyeListener antEyeListener){
        setAntEyeListener(antEyeListener);
    }
    public Receiver(SerialCommunicationListener serialCommunicationListener){
        setSerialCommunicationListener(serialCommunicationListener);
    }
    public Receiver(CombinerListener combinerListener){
        setCombinerListener(combinerListener);
    }
    public Receiver(IntegratorListener integratorListener){
        setIntegratorListener(integratorListener);
    }
    public Receiver(VisualNavigationListener visualNavigationListener){
        setVisualNavigationListener(visualNavigationListener);
    }

    public IntentFilter getIntentFilter() {
        return intentFilter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        HashMap<String, String> actionMap = new HashMap<>(3);
        String[] actions = intent.getAction().split("\\.");
        actionMap.put(SOURCE, actions[1]);
        actionMap.put(DESTINATION, actions[2]);
        actionMap.put(ACTION, actions[3]);
        if (actionMap.get(DESTINATION).equals(ANTEYE)){
            antEyeListener.onNewReceive(intent,actionMap);
        } else if (actionMap.get(DESTINATION).equals(SERIAL)){
            serialCommunicationListener.onNewReceive(intent, actionMap);
        } else if (actionMap.get(DESTINATION).equals(COMBINER)){
            combinerListener.onNewReceive(intent, actionMap);
        } else if (actionMap.get(DESTINATION).equals(INTEGRATOR)){
            integratorListener.onNewReceive(intent, actionMap);
        } else if (actionMap.get(DESTINATION).equals(VISUAL)){
            visualNavigationListener.onNewReceive(intent, actionMap);
        }
    }
}
