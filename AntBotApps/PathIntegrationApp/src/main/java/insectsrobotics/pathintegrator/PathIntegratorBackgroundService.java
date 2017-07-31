package insectsrobotics.pathintegrator;


import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import insectrobotics.broadcastlibrary.BroadcastService;
import insectsrobotics.pathintegrator.Receiver_and_Broadcaster.Broadcast;
import insectsrobotics.pathintegrator.Receiver_and_Broadcaster.Receive;


public class PathIntegratorBackgroundService extends BroadcastService {

    String moduleSelection = "";
    double orientation;
    double xVector;
    double yVector;

    Broadcast broadcast;
    Receive receive;

    Receive.ReceiveListener listener = new Receive.ReceiveListener() {
        @Override
        public void onNewMessageFromSerialConnectionApp(Intent intent, String action) {
            switch(action) {
                case WHEEL_ENCODER:
                    parseData(intent.getStringExtra("MainData"));
                    break;
                default:
                    //Do some Default stuff here if wanted.
                    break;
            }
        }

        @Override
        public void onNewMessageFromCombinerApp(Intent intent, String action) {
            switch(action) {
                case REQUEST_PI_DATA:
                    broadcast.broadcastPathInformation
                            ("O" + orientation + ",X" + xVector + ",Y" + yVector);
                    break;
            }
        }

        @Override
        public void onNewMessageFromIntegratorApp(Intent intent, String action) {

        }

        @Override
        public void onNewMessageFromVisualNavigationApp(Intent intent, String action) {

        }

        @Override
        public void onNewMessageFromAntEyeApp(Intent intent, String action) {

        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Service", "onBindCalled");
        broadcast = new Broadcast(this, getResources());
        receive = new Receive(listener);
        IntentFilter intentFilter = receive.getIntentFilter();
        registerReceiver(receive, intentFilter);
        return null;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        unregisterReceiver(receive);
        return super.onUnbind(intent);
    }


    private void parseData(String data){
        String[] dataArray = data.split(" ");
        if(dataArray[0].equals("t")){
            orientation = orientation + Double.parseDouble(dataArray[1]);
            Log.e("IntegratorService", "Orientation: " + orientation);
        } else if(dataArray[0].equals("m")){
            xVector = xVector + (Double.parseDouble(dataArray[1])*(Math.cos(Math.toRadians(orientation))));
            yVector = yVector + (Double.parseDouble(dataArray[1])*(Math.sin(Math.toRadians(orientation))));
            Log.e("IntegratorService", "X: " + xVector + " Y: " + yVector);
        }
    }
}
