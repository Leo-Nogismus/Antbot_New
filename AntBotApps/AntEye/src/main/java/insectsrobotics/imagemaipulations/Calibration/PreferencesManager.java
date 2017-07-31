package insectsrobotics.imagemaipulations.Calibration;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class PreferencesManager {

    SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences("Calibration_Data", Context.MODE_PRIVATE);
    }

    /**
     * Builds a String with all necessary data, saves it to shared preferences (Application-Cache)
     * for later reload on application start. Uses "," and "#" as distinguishers between data-sets
     *
     * @param imageCentreX  ImageCentre, obtained by the outer and inner circle positions
     * @param imageCentreY  ImageCentre, obtained by the outer and inner circle positions
     * @param thetaMeasured Used elevation for calibration (Needed for the Horner's-method)
     * @param c             Coefficients of the Newton's-polynomial
     * @param sinCoeffs     Coefficients of the sinus adjustment function
     */
    public void saveData(double imageCentreX, double imageCentreY, double[] thetaMeasured,
                         double[] c, double[] sinCoeffs) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Xc,").append(imageCentreX).append("#Yc,").append(imageCentreY).append("#");
        stringBuilder.append("theta");
        for (double aThetaMeasured : thetaMeasured) {
            stringBuilder.append(",");
            stringBuilder.append(aThetaMeasured);
        }
        stringBuilder.append("#c_calib");
        for (double aC : c) {
            stringBuilder.append(",");
            stringBuilder.append(aC);
        }
        stringBuilder.append("#sin");
        for (double aSinCoeffs : sinCoeffs) {
            stringBuilder.append(",");
            stringBuilder.append(aSinCoeffs);
        }
        prefs.edit().putString("Data", stringBuilder.toString()).apply();
        prefs.edit().putInt("theta_length", thetaMeasured.length).apply();
        prefs.edit().putInt("c_calib_length", c.length).apply();
        prefs.edit().putInt("CentreX", (int) imageCentreX).apply();
        prefs.edit().putInt("CentreY", (int) imageCentreY).apply();

        Log.e("PreferencesManager", stringBuilder.toString());
    }

    /**
     * Loads Data from previous calibration for use on new run.
     * Splits saved String and assigns it to the according variable.
     *
     * @return Returns a Bundle with all the saved Data.
     */
    public Bundle loadData() {
        Log.e("PreferencesManager", "loadData started");
        double Xc;
        double Yc;
        double[] c_calib;
        double[] theta;
        int theta_length;
        int c_calib_length;
        Bundle savedBundle = new Bundle();
        String savedString = prefs.getString("Data", "NO_DATA_SAVED");
        Log.e("PreferencesManager", savedString);
        if (savedString.equals("NO_DATA_SAVED")) {
            savedBundle.putString("Results", "NO_DATA_SAVED");
            return savedBundle;
        } else {
            savedBundle.putString("Results", "DATA_SAVED");
        }
        theta_length = prefs.getInt("theta_length", -1);
        c_calib_length = prefs.getInt("c_calib_length", -1);

        theta = new double[theta_length];
        c_calib = new double[c_calib_length];
        double[] sinCoeffs = new double[4];

        String[] stringArray = savedString.split("#", -1);
        for (String aStringArray : stringArray) {

            if (aStringArray.split(",")[0].equals("Xc")) {
                Xc = Double.parseDouble(aStringArray.split(",")[1]);
                savedBundle.putDouble("Xc", Xc);


            } else if (aStringArray.split(",")[0].equals("Yc")) {
                Yc = Double.parseDouble(aStringArray.split(",")[1]);
                savedBundle.putDouble("Yc", Yc);


            } else if (aStringArray.split(",")[0].equals("theta")) {
                int n = 0;
                for (String e : aStringArray.split(",")) {
                    if (!e.equals("theta")) {
                        theta[n] = Double.parseDouble(e);
                        n++;
                    }
                }
                savedBundle.putDoubleArray("theta", theta);


            } else if (aStringArray.split(",")[0].equals("c_calib")) {
                int n = 0;
                for (String e : aStringArray.split(",")) {
                    if (!e.equals("c_calib")) {
                        c_calib[n] = Double.parseDouble(e);
                        n++;
                    }
                }
                savedBundle.putDoubleArray("c_calib", c_calib);


            } else if (aStringArray.split(",")[0].equals("sin")) {
                int n = 0;
                for (String e : aStringArray.split(",")) {
                    if (!e.equals("sin")) {
                        sinCoeffs[n] = Double.parseDouble(e);
                        n++;
                    }
                }
                savedBundle.putDoubleArray("sin", sinCoeffs);
            }
        }

        return savedBundle;
    }


    /**
     * DEBUG - System for faster access to previously selected corners
     *
     * @param corners1 corners for elevation calibration
     * @param corners2 corners for azimuth calibration
     */

    public void saveCorners(double[][][] corners1, double[][][] corners2) {
        int width1 = corners1.length;
        int height1 = corners1[0].length;
        int depth1 = corners1[0][0].length;
        int width2 = corners2.length;
        int height2 = corners2[0].length;
        int depth2 = corners2[0][0].length;
        Log.e("Corners Dimensions", "width1 = " + width1 + " Height1 = " + height1 + " Depth1 = " + depth1);
        Log.e("CornersPhi Dimensions", "width2 = " + width2 + " Height2 = " + height2 + " Depth2 = " + depth2);
        prefs.edit().putInt("Width1", width1).apply();
        prefs.edit().putInt("Height1", height1).apply();
        prefs.edit().putInt("Depth1", depth1).apply();
        prefs.edit().putInt("Width2", width2).apply();
        prefs.edit().putInt("Height2", height2).apply();
        prefs.edit().putInt("Depth2", depth2).apply();

        StringBuilder stringBuilder = new StringBuilder();
        for (int a = 0; a < width1; a++) {
            for (int b = 0; b < height1; b++) {
                for (int c = 0; c < depth1; c++) {
                    stringBuilder.append(",");
                    stringBuilder.append(corners1[a][b][c]);
                }
                stringBuilder.append(";");
            }
            stringBuilder.append("#");
        }
        Log.e("Built String", "" + stringBuilder);
        prefs.edit().putString("Corners1", stringBuilder.toString()).apply();

        stringBuilder = new StringBuilder();
        for (int a = 0; a < width2; a++) {
            for (int b = 0; b < height2; b++) {
                for (int c = 0; c < depth2; c++) {
                    stringBuilder.append(",");
                    stringBuilder.append(corners2[a][b][c]);
                }
                stringBuilder.append(";");
            }
            stringBuilder.append("#");
        }
        Log.e("Built String 2", "" + stringBuilder);
        prefs.edit().putString("Corners2", stringBuilder.toString()).apply();
    }

    public Bundle loadCorners() {
        Bundle savedBundle = new Bundle();

        int width1;
        int height1;
        int depth1;
        int width2;
        int height2;
        int depth2;

        width1 = prefs.getInt("Width1", -1);
        height1 = prefs.getInt("Height1", -1);
        depth1 = prefs.getInt("Depth1", -1);
        width2 = prefs.getInt("Width2", -1);
        height2 = prefs.getInt("Height2", -1);
        depth2 = prefs.getInt("Depth2", -1);
        int Xc = prefs.getInt("CentreX", 0);
        int Yc = prefs.getInt("CentreY", 0);

        savedBundle.putInt("Width1", width1);
        savedBundle.putInt("Height1", height1);
        savedBundle.putInt("Depth1", depth1);
        savedBundle.putInt("Width2", width2);
        savedBundle.putInt("Height2", height2);
        savedBundle.putInt("Depth2", depth2);
        savedBundle.putInt("CentreX", Xc);
        savedBundle.putInt("CentreY", Yc);

        String savedString = prefs.getString("Corners1", "CORNERS1_NO_CORNERS_SAVED");
        Log.e("PreferencesManager", savedString);
        if (savedString.equals("CORNERS1_NO_CORNERS_SAVED")) {
            savedBundle.putString("Results1", "CORNERS1_NO_CORNERS_SAVED");
            return savedBundle;
        } else {
            savedBundle.putString("Results1", "CORNERS1_CORNERS_SAVED");
        }

        String[] stringArray = savedString.split("#");
        savedBundle.putStringArray("Corners1_String_Array", stringArray);
        /**
         a = 0;
         for (String aStringArray : stringArray){
         String[] stringArray2 = aStringArray.split(";");
         b=0;
         for (String bStringArray : stringArray2){
         String[] stringArray3 = bStringArray.split(",");
         c=0;
         for (String cStringArray : stringArray3){
         corners[a][b][c] = Double.parseDouble(cStringArray);
         c++;
         }
         b++;
         }
         a++;
         }
         */


        savedString = prefs.getString("Corners2", "CORNERS2_NO_CORNERS_SAVED");
        Log.e("PreferencesManager", savedString);
        if (savedString.equals("CORNERS2_NO_CORNERS_SAVED")) {
            savedBundle.putString("Results2", "CORNERS2_NO_CORNERS_SAVED");
            return savedBundle;
        } else {
            savedBundle.putString("Results2", "CORNERS2_CORNERS_SAVED");
        }
        stringArray = savedString.split("#");
        savedBundle.putStringArray("Corners2_String_Array", stringArray);
        /**
         a = 0;
         for (String aStringArray : stringArray){
         String[] stringArray2 = aStringArray.split(";");
         b=0;
         for (String bStringArray : stringArray2){
         String[] stringArray3 = bStringArray.split(",");
         c=0;
         for (String cStringArray : stringArray3){
         cornersPhi[a][b][c] = Double.parseDouble(cStringArray);
         c++;
         }
         b++;
         }
         a++;
         }
         */


        return savedBundle;
    }
}
