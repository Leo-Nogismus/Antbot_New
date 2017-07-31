package insectsrobotics.visualnavigationapp.Receiver_and_Broadcaster.Receiver;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import insectsrobotics.visualnavigationapp.NavigationModules.PerfectMemoryModule;
import insectsrobotics.visualnavigationapp.NavigationModules.WillshawModule;
import insectsrobotics.visualnavigationapp.R;
import insectsrobotics.visualnavigationapp.Receiver_and_Broadcaster.Broadcast;


public class AntEyeReceiver {

    String TAG = "SuperClass NavigationModules";

    boolean firstCall = true;
    Resources resources;
    String moduleSelection;
    BackgroundCalculations backgroundCalculations;
    Broadcast broadcast;

    WillshawModule willshawModule;
    PerfectMemoryModule perfectMemoryModule;



    public AntEyeReceiver(Context context, Resources res) {
        resources = res;
        backgroundCalculations = new BackgroundCalculations(res);
        broadcast = new Broadcast(context, res);
        willshawModule = new WillshawModule(res);
        perfectMemoryModule = new PerfectMemoryModule(res);
    }

    public void onModuleReceive(String moduleSelection){
        this.moduleSelection = moduleSelection;
        backgroundCalculations.setModuleName(moduleSelection);
    }

    public void onImageReceive(int[] image, int transmissionCode){

    }


    public void onReceive(Intent intent) {
        if (intent.getAction().equals(resources.getString(R.string.VisualNavigationModuleBroadcast))){
            Log.e(TAG, "onReceive called, action: " + intent.getAction());
            Bundle mBundle = intent.getExtras();
            moduleSelection = (String) mBundle.get("MainData");
            backgroundCalculations.setModuleName(moduleSelection);
        }
        else if (intent.getAction().equals(resources.getString(R.string.AntEyeImageBroadcast))){
            broadcast.broadcastStatusUpdate(true);
            backgroundCalculations = new BackgroundCalculations(resources);
            if (firstCall){

                firstCall = false;
            }
            backgroundCalculations.setModuleName(moduleSelection);
            Bundle bundle = intent.getExtras();
            backgroundCalculations.execute(bundle.getIntArray("MainData"), bundle.getInt(resources.getString(R.string.AntEyeTransmissionCode)));
        }
    }


    public class BackgroundCalculations extends AsyncTask<Object, Void, Bundle> {

        String TAG = "SuperClass NavigationModules";

        Resources resources;



        public BackgroundCalculations(Resources res){
            resources = res;

        }

        String moduleName;
        Bundle mBundle = new Bundle();





        @Override
        protected Bundle doInBackground(Object... params) {

            Log.e(TAG,"ModuleName: " + moduleName);
            mBundle = new Bundle();
            Bundle secBundle;
            if (moduleName.isEmpty()) {
                moduleName = resources.getString(R.string.NoVSModule);
            }
            Log.e("doInBackground123", moduleName);

            if (moduleName.equals(resources.getString(R.string.VS1))) {

                mBundle = willshawModule.onNewImage((int[]) params[0], (int) params[1]);
                secBundle = perfectMemoryModule.onNewImage((int[]) params[0], (int) params[1]);
                double error = secBundle.getDouble(resources.getString(R.string.VABundleError));
                mBundle.putDouble("PerfectMemoryError", error);


            } else if (moduleName.equals(resources.getString(R.string.VS2))) {
                mBundle = perfectMemoryModule.onNewImage((int[]) params[0], (int) params[1]);
            } else {
                mBundle.clear();
            }

            return mBundle;
        }

        @Override
        protected void onPostExecute(Bundle bundle) {
            super.onPostExecute(bundle);
            broadcast.broadcastFamiliarityError(mBundle);
            broadcast.broadcastStatusUpdate(false);

        }

        public void setModuleName(String moduleName) {
            this.moduleName = moduleName;
            Log.e(TAG,moduleName);
        }
    }

}
