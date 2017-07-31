package insectsrobotics.visualnavigationapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {


    //PreferencesManager preferencesManager;
    String fileNameImages = "images";
    String fileNameLearnedImages = "learnedImages";
    File sdCard = Environment.getExternalStorageDirectory();
    File dir = new File(sdCard.getAbsolutePath() + "/");
    File fileImages = new File(dir, fileNameImages + ".txt");
    File fileLearnedImages = new File(dir, fileNameLearnedImages + ".txt");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //preferencesManager = new PreferencesManager(getApplicationContext());
        //Toast toast = Toast.makeText(getApplicationContext(),"Second Activity Started", Toast.LENGTH_LONG);
        //toast.show();
        setContentView(R.layout.activity_main);

        Button clearBtn = (Button) findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //preferencesManager.clearLastLearned();
                FileOutputStream f1;
                FileOutputStream f2;
                try {
                    f1 = new FileOutputStream(fileImages);
                    f2 = new FileOutputStream(fileLearnedImages);
                    try {
                        f1.write("".getBytes());
                        f2.write("".getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Deletion Unsuccessful - Could not write to File", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getApplicationContext(), "Deletion Successful", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Deletion Unsuccessful - Could not find Files", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


}
