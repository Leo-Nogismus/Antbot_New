package insectsrobotics.imagemaipulations;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AdvancedSetting extends Activity {

    SharedPreferences prefs;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = this.getPreferences(MODE_PRIVATE);
        setContentView(R.layout.advanced_settings);
        Intent intent = getIntent();

        Button confirmButton = (Button) findViewById(R.id.confirmAdvancedSettingsButton);
        final EditText serverAddressEditText = (EditText) findViewById(R.id.serverAddressEditText);
        final EditText portEditText = (EditText) findViewById(R.id.portEditText);





        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serverAddress = serverAddressEditText.getText().toString();
                int port = Integer.parseInt(portEditText.getText().toString());
                Bundle mBundle = new Bundle();
                mBundle.putString("ServerAddress", serverAddress);
                mBundle.putInt("Port", port);
                prefs.edit().putString("ServerConnection", serverAddress + ":" + port).apply();
                Intent intent = new Intent();
                intent.putExtra("Selection", mBundle);
                Log.e("AdvancedSettings", serverAddress + ":" + port);
                Log.e("AdvancedSettings", mBundle.toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
