package insectsrobotics.imagemaipulations;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashMap;

import insectsrobotics.imagemaipulations.Calibration.Calibration;
import insectsrobotics.imagemaipulations.Calibration.PreferencesManager;


public class StartScreen extends Activity {

    Context context;
    PreferencesManager preferencesManager;
    Bundle savedBundle;
    RadioGroup VS_radioGroup;
    RadioGroup PI_radioGroup;
    RadioGroup Combiner_radioGroup;
    HashMap<Integer, Integer> moduleSelectionMap;

    int[] navigationModules = {-1, -1, -1};

    boolean defaultSettings = true;

    Intent startMainActivityIntent;

    // DEBUG Bundle savedCorners;


    int SELECT_IMAGE_POINTS = 1;
    int ADVANCED_SETTINGS = 2;
    boolean firstClick = true;
    RadioGroup.OnCheckedChangeListener radioGroupOnChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (group.equals(VS_radioGroup)) {
                navigationModules[0] = checkedId;
            } else if (group.equals(PI_radioGroup)) {
                navigationModules[1] = checkedId;
            } else if (group.equals(Combiner_radioGroup)) {
                navigationModules[2] = checkedId;
            }

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);
        preferencesManager = new PreferencesManager(getApplicationContext());
        context = this;
        moduleSelectionMap = new HashMap<>();
        moduleSelectionMap.put(R.id.VS1_radioBtn, R.string.VS1);
        moduleSelectionMap.put(R.id.VS2_radioBtn, R.string.VS2);
        moduleSelectionMap.put(-1, R.string.NoVSModule);
        moduleSelectionMap.put(R.id.PI1_radioBtn, R.string.PI1);
        moduleSelectionMap.put(R.id.PI2_radioBtn, R.string.PI2);
        moduleSelectionMap.put(-2, R.string.NoPIModule);
        moduleSelectionMap.put(R.id.Combiner1_radioBtn, R.string.Combiner1);
        moduleSelectionMap.put(R.id.Combiner2_radioBtn, R.string.Combiner2);
        moduleSelectionMap.put(-3, R.string.NoCombinerModule);

        TextView advancedSettingsTextView = (TextView) findViewById(R.id.AdvancedSettingTextView);
        advancedSettingsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                defaultSettings = false;
                Intent advancedSettingsIntent = new Intent(StartScreen.this, AdvancedSetting.class);
                advancedSettingsIntent.putExtra("Default", defaultSettings);
                startActivityForResult(advancedSettingsIntent, ADVANCED_SETTINGS);
            }
        });

        startMainActivityIntent = new Intent(StartScreen.this, MainActivity.class);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LinearLayout defaultSettingsLayout = (LinearLayout) findViewById(R.id.defaultSettingsLayout);
        defaultSettingsLayout.setVisibility(View.VISIBLE);
        firstClick = true;
        if (PI_radioGroup != null && VS_radioGroup != null && Combiner_radioGroup != null) {
            PI_radioGroup.setVisibility(View.GONE);
            VS_radioGroup.setVisibility(View.GONE);
            Combiner_radioGroup.setVisibility(View.GONE);
        }
    }

    public void onClick(View v) {

        if (v instanceof Button) {
            if (firstClick && !((Button) v).getText().toString().equals("Start")) {
                LinearLayout defaultSettingsLayout = (LinearLayout) findViewById(R.id.defaultSettingsLayout);
                defaultSettingsLayout.setVisibility(View.GONE);
                firstClick = false;
            }
            VS_radioGroup = (RadioGroup) findViewById(R.id.VS_radioGroup);
            VS_radioGroup.getChildCount();
            PI_radioGroup = (RadioGroup) findViewById(R.id.PI_radioGroup);
            Combiner_radioGroup = (RadioGroup) findViewById(R.id.Combiner_radioGroup);
            Button button = (Button) v;
            String buttonText = button.getText().toString();
            switch (buttonText) {
                case "VS":
                    PI_radioGroup.setVisibility(View.GONE);
                    Combiner_radioGroup.setVisibility(View.GONE);
                    VS_radioGroup.setVisibility(View.VISIBLE);
                    VS_radioGroup.setOnCheckedChangeListener(radioGroupOnChangeListener);
                    break;
                case "PI":
                    VS_radioGroup.setVisibility(View.GONE);
                    Combiner_radioGroup.setVisibility(View.GONE);
                    PI_radioGroup.setVisibility(View.VISIBLE);
                    PI_radioGroup.setOnCheckedChangeListener(radioGroupOnChangeListener);
                    break;
                case "Combiner":
                    VS_radioGroup.setVisibility(View.GONE);
                    PI_radioGroup.setVisibility(View.GONE);
                    Combiner_radioGroup.setVisibility(View.VISIBLE);
                    Combiner_radioGroup.setOnCheckedChangeListener(radioGroupOnChangeListener);
                    break;
                case "Start":
                    if (navigationModules[0] == -1 && navigationModules[1] == -1 && navigationModules[2] == -1) {
                        navigationModules[0] = R.id.VS1_radioBtn;
                        navigationModules[1] = R.id.PI1_radioBtn;
                        navigationModules[2] = R.id.Combiner1_radioBtn;
                        onStartButtonClick(findViewById(R.id.startBtn));
                    } else if (navigationModules[0] != -1 && navigationModules[1] != -1 && navigationModules[2] != -1) {
                        onStartButtonClick(findViewById(R.id.startBtn));
                    } else {
                        if (navigationModules[0] == -1)
                            navigationModules[0] = R.id.VS1_radioBtn;
                        if (navigationModules[1] == -1)
                            navigationModules[1] = R.id.PI1_radioBtn;
                        if (navigationModules[2] == -1)
                            navigationModules[2] = R.id.Combiner1_radioBtn;
                    }


            }

        }
    }

    //(RadioGroup group, int checkedId){

    //}

    /**
     * Check if calibration_layout data is available in SharedPreferences, if yes -> load them and redirect
     * to the MainActivity, if not redirect to the Calibration Activity (Only happens if either
     * first run of the app, or if the Cache has been emptied)
     */
    public void onStartButtonClick(View btn) {

        savedBundle = preferencesManager.loadData();
        String serverConnection = this.getPreferences(MODE_PRIVATE).getString("ServerConnection", "192.168.43.161:8080");
        Bundle mBundle = new Bundle();
        mBundle.putString("ServerAddress", serverConnection.split(":")[0]);
        mBundle.putInt("Port", Integer.parseInt(serverConnection.split(":")[1]));
        //savedCorners = preferencesManager.loadCorners();
        if (savedBundle.getString("Results", "NO_DATA_SAVED").equals("DATA_SAVED")) {
            Log.i("StartScreen", savedBundle.getString("Results"));

            Log.e("HashMap Keys", "VS1 Button ID = " + R.id.VS1_radioBtn + "VS2 Button ID = " + R.id.VS2_radioBtn);
            Log.e("Module Array Keys", "Module Array VS ID = " + getResources().getString(moduleSelectionMap.get(navigationModules[0]))
                    + "Module Array PI ID = " + moduleSelectionMap.get(navigationModules[1])
                    + "Module Array Combiner ID = " + moduleSelectionMap.get(navigationModules[2]));


            startMainActivityIntent.putExtra("DataType", "CALIBRATION_DATA_AVAILABLE");
            savedBundle.putString(getResources().getString(R.string.AntEyeVNModuleSelection), getResources().getString(moduleSelectionMap.get(navigationModules[0])));
            savedBundle.putString(getResources().getString(R.string.AntEyePIModuleSelection), getResources().getString(moduleSelectionMap.get(navigationModules[1])));
            savedBundle.putString(getResources().getString(R.string.AntEyeCombinerModuleSelection), getResources().getString(moduleSelectionMap.get(navigationModules[2])));
            startMainActivityIntent.putExtra("Data", savedBundle);
            startMainActivityIntent.putExtra("ServerConnection",mBundle);
            startActivity(startMainActivityIntent);

        } else {

            Intent switchIntent = new Intent(StartScreen.this, Calibration.class);
            /**
             * DEBUG, loads saved corners for faster debugging
             *
             Log.i("SavedData", savedBundle.getString("Results"));
             Log.i("SavedCorners", savedBundle.getString("Results"));
             if (savedCorners.getString("Results1","CORNERS1_NO_CORNERS_SAVED").equals("CORNERS1_CORNERS_SAVED") &&
             savedCorners.getString("Results2","CORNERS2_NO_CORNERS_SAVED").equals("CORNERS2_CORNERS_SAVED")){
             switchIntent.putExtra("DataType","CORNER_DATA_AVAILABLE");
             switchIntent.putExtra("Corners1",savedCorners.getStringArray("Corners1_String_Array"));
             switchIntent.putExtra("Corners2",savedCorners.getStringArray("Corners2_String_Array"));
             switchIntent.putExtra("Width1", savedCorners.getInt("Width1"));
             switchIntent.putExtra("Height1", savedCorners.getInt("Height1"));
             switchIntent.putExtra("ImageNumber", savedCorners.getInt("ImageNumber"));
             switchIntent.putExtra("Depth1", savedCorners.getInt("Depth1"));
             switchIntent.putExtra("Width2", savedCorners.getInt("Width2"));
             switchIntent.putExtra("Height2", savedCorners.getInt("Height2"));
             switchIntent.putExtra("Depth2", savedCorners.getInt("Depth2"));
             switchIntent.putExtra("CentreX", savedCorners.getInt("CentreX"));
             switchIntent.putExtra("CentreY", savedCorners.getInt("CentreY"));
             }*/
            startActivityForResult(switchIntent, SELECT_IMAGE_POINTS);
        }
    }


    /**
     * Waits for the Calibration to be finished before redirecting to the MainActivity with the
     * calibration_layout data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("StartScreen", "onActivityResult called");
        if (requestCode == SELECT_IMAGE_POINTS) {
            if (resultCode == RESULT_OK) {
                savedBundle = preferencesManager.loadData();
                Intent switchIntent = new Intent(StartScreen.this, MainActivity.class);
                switchIntent.putExtra("Data", savedBundle);
                startActivity(switchIntent);
            }
        } /*else if (requestCode == ADVANCED_SETTINGS){
            if (resultCode == RESULT_OK){
                Log.e("StartScreen", "ResultCodeOK");
                Bundle mBundle = data.getBundleExtra("Selection");
                startMainActivityIntent.putExtra("Selection", mBundle);
            }
        }*/
    }
}
