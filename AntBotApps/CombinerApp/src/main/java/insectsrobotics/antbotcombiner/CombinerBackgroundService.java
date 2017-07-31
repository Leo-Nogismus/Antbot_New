package insectsrobotics.antbotcombiner;


import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import insectrobotics.broadcastlibrary.BroadcastService;
import insectsrobotics.antbotcombiner.Receiver_and_Broadcast.Broadcast;
import insectsrobotics.antbotcombiner.Receiver_and_Broadcast.Receive;


public class CombinerBackgroundService extends BroadcastService {

    private final String TAG = this.getClass().getSimpleName();

    Broadcast broadcast;
    Receive receive;
    String path;
    double xVector;
    double yVector;
    double orientation;

    boolean homing = false;

    Receive.ReceiveListener listener = new Receive.ReceiveListener() {
        @Override
        public void onNewMessageFromSerialConnectionApp(Intent intent, String action) {
            switch (action){
                case TASK_EXECUTED:
                    //TODO: Send out a new Task
                    break;
                case HOMING_ROUTING:
                    startHoming();
                    break;
                default:
                    //Here could go some default stuff.
                    break;
            }
        }
        @Override
        public void onNewMessageFromCombinerApp(Intent intent, String action) {
        }
        @Override
        public void onNewMessageFromIntegratorApp(Intent intent, String action) {
            switch (action){
                case VECTOR:
                    if(homing){
                        parseVector(intent.getStringExtra("MainData"));
                    }
                    break;
                default:
                    //Here could go some default stuff.
                    break;
            }
        }
        @Override
        public void onNewMessageFromVisualNavigationApp(Intent intent, String action) {
            switch (action){
                case ERROR:
                    Log.e("CombinerApplication","Combiner Received Error: " + intent.getIntExtra("MainData", -1));
                    break;
                default:
                    //Here could go some default stuff.
                    break;
            }
        }
        @Override
        public void onNewMessageFromAntEyeApp(Intent intent, String action) {
            switch (action){
                case COMBINER_MODULE:
                    //TODO: Switch between Combiner Modules
                    break;
                default:
                    //Here could go some default stuff.
                    break;
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind called");

        broadcast = new Broadcast(this);
        receive = new Receive(listener);
        registerReceiver(receive, receive.getIntentFilter());
        return null;
    }


    @Override
    public boolean onUnbind(Intent intent) {

        unregisterReceiver(receive);

        return super.onUnbind(intent);
    }

    private void startHoming(){
        homing = true;
        broadcast.requestNewIntegratorData();
    }

    private void parseVector(String path){
        String[] tokens = path.split(",");
        for (int n = 0; n < tokens.length; n++){
            if (tokens[n].contains("O")){
                tokens[n] = tokens[n].replace("O","");
                orientation = Double.parseDouble(tokens[n]);
            } else if (tokens[n].contains("X")){
                tokens[n] = tokens[n].replace("X","");
                xVector = Double.parseDouble(tokens[n]);
            } else if (tokens[n].contains("Y")){
                tokens[n] = tokens[n].replace("Y","");
                yVector = Double.parseDouble(tokens[n]);
            }
        }
        Log.e(TAG,"O: " + orientation + " X: " + xVector + " Y: " + yVector);
        makeDecision();
    }

    private void makeDecision(){
        double alpha, beta;
        int gamma;
        double distance;
        alpha = Math.toDegrees(Math.atan2(yVector, xVector));
        Log.e(TAG,"Alpha: " + alpha);
        beta = orientation;
        Log.e(TAG, "Beta: " + beta);
        gamma = (int)(-beta + alpha + 180);
        Log.e(TAG, "Gamma: " + gamma);
        distance = ((int)Math.sqrt((xVector*xVector)+(yVector*yVector)))/100;
        broadcast.broadcastDecision("t " + gamma + " m " + distance + " n");
    }


}
