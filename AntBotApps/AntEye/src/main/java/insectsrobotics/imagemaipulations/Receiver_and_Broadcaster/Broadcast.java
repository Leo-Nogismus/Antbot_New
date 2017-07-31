package insectsrobotics.imagemaipulations.Receiver_and_Broadcaster;


import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;

import org.opencv.core.Mat;

import insectrobotics.broadcastlibrary.BroadcastValues;
import insectrobotics.broadcastlibrary.Broadcaster;
import insectrobotics.broadcastlibrary.R;

public class Broadcast extends Broadcaster implements BroadcastValues {

    Resources resources;

    public Broadcast(Context context, Resources res) {
        super(context);
        resources = res;
    }

    public void broadcastServerConnection(Bundle bundle){
        initiateBroadcast(SERVER_CONNECTION_BROADCAST);
        onNewDataToBroadcast(bundle);
        executeBroadcast();
    }

    public void broadcastImageForServer(Mat image){
        TranslateImage translateImage = new TranslateImage();
        translateImage.execute(image);
    }
    private class TranslateImage extends AsyncTask<Mat,String,byte[]> {

        @Override
        protected byte[] doInBackground(Mat... params) {
            Mat matImage = params[0];
            byte[] byteImage = new byte[matImage.cols()*matImage.rows()];
            matImage.get(0, 0, byteImage);
            return byteImage;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            initiateBroadcast(IMAGE_SERVER_BROADCAST);
            onNewDataToBroadcast(bytes);
            executeBroadcast();
        }
    }

    public void broadcastImage(int[] image, int CODE){
        initiateBroadcast(IMAGE_BROADCAST);
        onNewDataToBroadcast(image);
        addData(resources.getString(R.string.AntEyeTransmissionCode), CODE);
        executeBroadcast();
    }


    public void  broadcastModules(String visualModule, String integratorModule, String combinerModule){
        broadcastVisualModule(visualModule);
        broadcastIntegratorModule(integratorModule);
        broadcastCombinerModule(combinerModule);
    }
    private void broadcastVisualModule(String module){
        initiateBroadcast(VISUAL_MODULE_BROADCAST);
        onNewDataToBroadcast(module);
        executeBroadcast();
    }

    private void broadcastCombinerModule(String module){
        initiateBroadcast(COMBINER_MODULE_BROADCAST);
        onNewDataToBroadcast(module);
        executeBroadcast();
    }

    private void broadcastIntegratorModule(String module){
        initiateBroadcast(INTEGRATOR_MODULE_BROADCAST);
        onNewDataToBroadcast(module);
        executeBroadcast();
    }
}
