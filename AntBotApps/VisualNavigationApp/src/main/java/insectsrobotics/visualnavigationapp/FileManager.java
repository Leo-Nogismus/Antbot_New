package insectsrobotics.visualnavigationapp;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;


public class FileManager {
    String TAG = "SuperClass NavigationModules";

    String fileNameImages = "images";
    String fileNameLearnedImages = "learnedImages";
    File sdCard = Environment.getExternalStorageDirectory();
    File dir = new File(sdCard.getAbsolutePath() + "/");
    File fileImages = new File(dir, fileNameImages + ".txt");
    File fileLearnedImages = new File(dir, fileNameLearnedImages + ".txt");

    InputStream inputStream;
    InputStream inputLearnedStream;
    BufferedReader bufferedReader;
    BufferedReader bufferedLearnedReader;


    int m = 0;
    int n = 0;

    public FileManager() {
        FileOutputStream f1;
        FileOutputStream f2;

        if (!fileImages.exists()) {
            try {
                f1 = new FileOutputStream(fileImages);
                f1.write("".getBytes());
            } catch (Exception e) {
                Log.e(TAG, "Constructor Exception could not create file " + e);
                e.printStackTrace();
            }
        }
        if (!fileLearnedImages.exists()) {
            try {
                f2 = new FileOutputStream(fileLearnedImages);
                f2.write("".getBytes());
            } catch (Exception e) {
                Log.e(TAG, "Constructor Exception could not create file " + e);
                e.printStackTrace();
            }
        }
        try {
            inputStream = new FileInputStream(fileImages);
            inputLearnedStream = new FileInputStream(fileLearnedImages);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        bufferedLearnedReader = new BufferedReader(new InputStreamReader(inputLearnedStream));
    }

    public void saveImage(int[] image) {

        StringBuilder imageString = new StringBuilder();
        String prefix = "";
        for (int aImage : image) {
            imageString.append(prefix);
            prefix = ",";
            imageString.append(aImage);
        }
        try {
            FileOutputStream f = new FileOutputStream(fileImages, true);
            f.write((imageString.toString() + "\n").getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        n++;
        Log.e(TAG, "N: " + n);
    }

    public void saveLearnedImage(int[] image) {
        StringBuilder imageString = new StringBuilder();
        String prefix = "";
        for (int aImage : image) {
            imageString.append(prefix);
            prefix = ",";
            imageString.append(aImage);
        }
        try {
            FileOutputStream f = new FileOutputStream(fileLearnedImages, true);
            f.write((imageString.toString() + "\n").getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int[] loadImage() {
        Log.e(TAG, "LoadImage called");
        String imageString = "";
        String[] imageStringArray;
        int[] image;
        if (fileImages.exists()) {
            Log.e(TAG, "File exists");

            Log.e(TAG, "Buffer initiated");
            try {
                Log.e(TAG, "Reads line");
                imageString = bufferedReader.readLine();
                Log.e(TAG, "M: " + m);
                try {
                    Log.e(TAG, "String Length: " + imageString.length());
                } catch (Exception e1) {
                    Log.e(TAG, "String Length not possible " + e1);
                }
                m++;


            } catch (Exception e) {
                Log.e(TAG, "Exception" + e);
                e.printStackTrace();
            }

        }

        if (!imageString.isEmpty()) {
            imageStringArray = imageString.split(",");
            image = new int[imageStringArray.length - 1];
            int counter = 0;
            for (String aImageStringArray : imageStringArray) {
                if (counter == 0) {
                } else {
                    image[counter - 1] = Byte.parseByte(aImageStringArray);
                }
                counter++;
            }
        } else {
            image = null;
        }


        if (image != null) {
            Log.e(TAG, "Loaded Image Size: " + image.length);
            Log.e(TAG, "Image Pixel 1: " + image[1] + " " + image[2] + " " + image[3] + " " + image[4] + " ");
        } else {
            Log.e(TAG, "Loaded Image Size: null");
        }

        return image;
    }


    public int[] loadLearnedImage() {
        Log.e(TAG, "LoadLearnedImage called");
        String imageString = "";
        String[] imageStringArray;
        int[] image;
        if (fileLearnedImages.exists()) {
            Log.e(TAG, "File exists");
            try {
                Log.e(TAG, "Reads line");
                imageString = bufferedLearnedReader.readLine();


            } catch (Exception e) {
                Log.e(TAG, "Exception");
                e.printStackTrace();
            }

        }

        if (!imageString.isEmpty()) {
            Log.e(TAG, "Image String not empty");
            imageStringArray = imageString.split(",");
            image = new int[imageStringArray.length - 1];
            int counter = 0;
            for (String aImageStringArray : imageStringArray) {
                if (counter != 0) {
                    image[counter - 1] = Byte.parseByte(aImageStringArray);
                }
                counter++;
            }
        } else {
            image = null;
        }


        if (image != null) {
            Log.e(TAG, "Loaded Learned Image Size: " + image.length);
            Log.e(TAG, "Image Pixel Learned 1: " + image[1] + " " + image[2] + " " + image[3] + " " + image[4] + " ");
        } else {
            Log.e(TAG, "Loaded Learned Image Size: null");
        }


        return image;
    }


    public int loadNumberOfImages() {
        int n = 0;
        if (fileImages.exists()) {
            try {
                Log.e(TAG, "Reads line");
                while (!bufferedReader.readLine().isEmpty()) {
                    n++;
                }

            } catch (Exception e) {
                Log.e(TAG, "Exception 1" + e);
                e.printStackTrace();
            }
            Log.e(TAG, "Number Of Images FileManager: " + n);
        }


        try {
            inputStream = new FileInputStream(fileImages);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        return n;
    }
}
