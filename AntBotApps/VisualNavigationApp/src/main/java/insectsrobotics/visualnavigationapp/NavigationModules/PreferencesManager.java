package insectsrobotics.visualnavigationapp.NavigationModules;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


public class PreferencesManager {
    String TAG = "SuperClass NavigationModules";

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    int n = 0;
    int m = 0;

    public PreferencesManager(Context context){
        prefs = context.getSharedPreferences("ImageSavings", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveImage(byte[] image){
        StringBuilder stringBuilder = new StringBuilder();
        String prefix = "";
        for (byte aImage : image){
            stringBuilder.append(prefix);
            prefix = ",";
            stringBuilder.append(aImage);
        }

        editor.putString("Image" + n, stringBuilder.toString());
        editor.putInt("Images", n);
        editor.apply();
        Log.d(TAG, "n: " + n + " savingImageString: " + stringBuilder);
        n++;
    }

    public void saveLearnedImage(byte[] image){
        StringBuilder stringBuilder = new StringBuilder();
        String prefix = "";
        for (byte aImage : image){
            stringBuilder.append(prefix);
            prefix = ",";
            stringBuilder.append(aImage);
        }

        editor.putString("LearnedImage" + m, stringBuilder.toString());
        editor.putInt("LearnedImages", m);
        editor.apply();
        Log.d(TAG, "n: " + m + " savingLearnedImageString: " + stringBuilder);
        m++;
    }

    public byte[] loadImage(int imageNumber){
        byte[] image;
        String savedString = prefs.getString("Image" + imageNumber, "");
        Log.d(TAG, "n: " + imageNumber + savedString);
        if (!savedString.isEmpty()) {
            String[] imageString = savedString.split(",");
            image = new byte[imageString.length];
            int o = 0;
            for (String aImageString : imageString) {
                image[o] = Byte.parseByte(aImageString);
                o++;
            }
            return image;
        }
        else {
            return null;
        }
    }

    public byte[] loadLearnedImage(int imageNumber){
        byte[] image;
        String savedString = prefs.getString("LearnedImage" + imageNumber, "");
        Log.d(TAG, savedString);
        if (!savedString.isEmpty()) {
            String[] imageString = savedString.split(",");
            image = new byte[imageString.length];
            int o = 0;
            for (String aImageString : imageString) {
                image[o] = Byte.parseByte(aImageString);
                o++;
            }
            return image;
        }
        else {
            return null;
        }
    }

    public int loadNumber(){
        n = prefs.getInt("Images", -1) + 1;
        m = prefs.getInt("LearnedImages", -1) + 1;
        Log.e(TAG, "Images " + n);
        Log.e(TAG, "Images " + m);
        return prefs.getInt("Images", -1);
    }

    public void clearAll(){

        prefs.edit().putInt("Images",-1).apply();
        prefs.edit().putInt("LearnedImages",-1).apply();
        prefs.edit().clear().apply();
    }

    public void clearLastLearned(){
        int learnedImages;
        learnedImages = prefs.getInt("LearnedImages",-1);
        prefs.edit().remove("LearnedImage" + learnedImages).apply();
        prefs.edit().putInt("LearnedImages",(learnedImages-1)).apply();
    }
}
