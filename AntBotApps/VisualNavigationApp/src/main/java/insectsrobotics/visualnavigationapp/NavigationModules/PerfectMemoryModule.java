package insectsrobotics.visualnavigationapp.NavigationModules;


import android.content.res.Resources;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import insectsrobotics.visualnavigationapp.NavigationModules._Superclasses.NavigationModules;

public class PerfectMemoryModule extends NavigationModules {

    String TAG = "SuperClass NavigationModules";

    List<int[]> learnedImages;

    public PerfectMemoryModule(Resources res) {
        super(res);
    }

    @Override
    public void setupLearningAlgorithm(int[] image) {
        super.setupLearningAlgorithm(image);
        learnedImages = new ArrayList<>();
    }

    @Override
    public void learnImage(int[] image) {
        super.learnImage(image);
        Log.e(TAG, "PM Image Length: " + image.length);
        learnedImages.add(image);
    }

    @Override
    public double calculateFamiliarity(int[] image) {
        super.calculateFamiliarity(image);
        double error = 0;
        double smallestError = 0;
        Log.e(TAG,"List length: " + learnedImages.size());
        for (int imageCounter = 0; imageCounter < learnedImages.size(); imageCounter++){
            int[] learnedImage = learnedImages.get(imageCounter);
            int pixelCounter;
            for (pixelCounter = 0; pixelCounter < image.length; pixelCounter++){
                int difference = (learnedImage[pixelCounter])-(image[pixelCounter]);
                int squaredError = difference * difference;
                error = error + squaredError;
            }
            error = Math.sqrt(error/pixelCounter);
            if (imageCounter != 0){
                if (error < smallestError){
                    smallestError = error;
                }
            } else {
                smallestError = error;
            }
        }
        return smallestError;
    }
}
