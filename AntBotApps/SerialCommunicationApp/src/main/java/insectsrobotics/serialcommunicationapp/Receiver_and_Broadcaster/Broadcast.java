package insectsrobotics.serialcommunicationapp.Receiver_and_Broadcaster;

import android.content.Context;
import android.content.res.Resources;

import insectrobotics.broadcastlibrary.BroadcastValues;
import insectrobotics.broadcastlibrary.Broadcaster;


public class Broadcast extends Broadcaster implements BroadcastValues {

    String TAG = "OCVSample::Activity";

    Resources resources;

    public Broadcast(Context context, Resources res) {
        super(context);
        resources = res;
    }

    public void broadcastSerialConnectionEstablished(boolean serialConnectionEstablished){
        initiateBroadcast(SERIAL_CONNECTION_ESTABLISHED_BROADCAST);
        onNewDataToBroadcast(serialConnectionEstablished);
        executeBroadcast();
    }

    public void broadcastServerConnectionEstablished(boolean serverConnectionEstablished){
        initiateBroadcast(SERVER_CONNECTION_ESTABLISHED_BROADCAST);
        onNewDataToBroadcast(serverConnectionEstablished);
        executeBroadcast();
    }


    public void requestNewTask(){
        initiateBroadcast(TASK_EXECUTED_BROADCAST);
        onNewDataToBroadcast(true);
        executeBroadcast();
    }

    public void broadcastHomingRoutingCommand(String command){
        initiateBroadcast(HOMING_ROUTING_BROADCAST);
        onNewDataToBroadcast(command);
        executeBroadcast();
    }

    public void broadcastLearnImageCommand(){
        initiateBroadcast(LEARN_IMAGE_COMMAND_BROADCAST);
        executeBroadcast();
    }

    public void broadcastResetLearningCommand(){
        initiateBroadcast(RESET_LEARNING_COMMAND_BROADCAST);
        executeBroadcast();
    }

    public void broadcastWheelEncoderInformation(String data){
        initiateBroadcast(WHEEL_ENCODER_BROADCAST);
        onNewDataToBroadcast(data);
        executeBroadcast();
    }
}
