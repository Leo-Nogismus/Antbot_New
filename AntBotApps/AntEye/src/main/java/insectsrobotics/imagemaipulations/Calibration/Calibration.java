package insectsrobotics.imagemaipulations.Calibration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import insectsrobotics.imagemaipulations.R;


public class Calibration extends Activity {

    private static final String DEBUG_TAG = "Gestures";

    int PICK_IMAGE_REQUEST = 2;

    //Methods to manage saving and loading of Preferences
    PreferencesManager preferencesManager;

    //Used Views in all the calibration_layout layouts
    DrawImageView mImageView;
    Button mButton;
    Button pickImageButton;
    Button finishButton;
    EditText drumRadiusET;
    EditText squareSizeXET;
    EditText squareSizeYET;
    EditText cornersXET;
    EditText cornersYET;
    EditText imageNumET;
    TextView nextStep;
    TextView cornersYTV;
    TextView cornersXTV;
    TextView imageNumTV;

    //DEBUG ImageView debugView;

    //Transformation Matrix of the displayed Image (Used for Scrolling/ Zooming)
    private Matrix matrix = new Matrix();   //Transformation matrix
    float[] m = new float[9];               //Vector of transformation-matrix-values
    float detectedScale = 1;                //Scale of Image after Zoom

    //External Input from User, needed Data for calibration_layout
    double drumRadius;      //Distance between Camera and chess-grid
    double squareSizeX;     //Size of black squares in x-direction
    double squareSizeY;     //Size of black squares in y-direction
    int cornersX;           //Corners count in x-direction
    int cornersY;           //Corners count in y-direction
    int imageNum;           //Quantity of images used for calibration_layout


    //Variables of user-made inner circle
    float XcInnerCircle;
    float YcInnerCircle;
    float radiusInnerCircle;

    //Variables of user-made outer circle
    float XcOuterCircle;
    float YcOuterCircle;
    float radiusOuterCircle;

    //Points for cross drawing on corners (by user input)
    float[] points = new float[16];

    //Centre Coordinates for calibration_layout
    int imageCentreX;
    int imageCentreY;

    //Coefficients of Newtons-Polynomial
    double[] coeffsTheta;

    //Measured elevations (by user input)
    double[] theta_measured;

    //Sinus adjustment coefficients
    double[] sinCoeffs = new double[4];



    //Booleans to distinguish states of calibration_layout
    boolean drawCircle = false;
    boolean circleDone = false;
    boolean drawCross = false;
    boolean cornerConfirmed = false;
    boolean imageSelected = false;
    boolean nextImage = false;

    //Positions of user selected Corners
    double[][][] corners;       //Corners for elevation calibration_layout
    double[][][] cornersPhi;    //Corners for azimuth calibration_layout

    //Variables for onDraw-Method of the DrawImageView-Activity
    int circleXc = 0;
    int circleYc = 0;

    //Counters for Corner-Selection
    int cornerNumber = 0;
    int degree = 0;
    int imageCounter = 0;

    //Bitmaps to draw Circles and crosses on
    Bitmap imageBitmap;
    Bitmap originalBitmap;

    //Button strings to distinguish state of calibration_layout
    String innerCircleString;
    String outerCircleString;
    String cornerString;

    //Gesture detectors for zooming, scrolling and touch gestures
    private GestureDetectorCompat mDetector;
    private ScaleGestureDetector sDetector;


    // Intent to pass data to following activities
    Intent savedIntent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedIntent = getIntent();
        createPickImagesLayout();
        preferencesManager = new PreferencesManager(getApplicationContext());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////Create Layout to Pick Images for Calibration///////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void createPickImagesLayout(){
        //Reset all state distinguishers after completion of data selection of an image
        circleDone = false;
        drawCircle = false;
        drawCross = false;
        cornerConfirmed = false;
        cornerNumber = 0;
        degree = 0;
        if (imageCounter != 0) {
            mImageView.onReset();
        }

        setContentView(R.layout.pick_images);

        //Set the needed views
        setViews();


        if (imageCounter > 0){
            //Change visibilities
            changeVisibilities();
        }

        if (nextImage){
            //After data input for at least one image, set up a finish button
            setFinishButton();
        }

        pickImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInputData();

                //Start Gallery for Image selection
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_IMAGE_REQUEST);
            }
        });
    }

    //Set all Views of the image selection layout.
    private void setViews(){
        pickImageButton = (Button) findViewById(R.id.pick_image_button);    //Redirection to Gallery and confirmation of input data
        finishButton =(Button) findViewById(R.id.calibration_finish_button);//Button to finish the Calibration process
        drumRadiusET = (EditText) findViewById(R.id.drum_radius_et);        //Edit Text for Drum Radius input
        squareSizeXET = (EditText) findViewById(R.id.square_size_x_et);     //Edit Text for square size in x-Direction
        squareSizeYET = (EditText) findViewById(R.id.square_size_y_et);     //Edit Text for square size in y-Direction
        cornersXET = (EditText) findViewById(R.id.corners_x_et);            //Edit Text for corners count in x-Direction
        cornersYET = (EditText) findViewById(R.id.corners_y_et);            //Edit Text for corners count in y-Direction
        imageNumET = (EditText) findViewById(R.id.image_num_et);            //Edit Text for image count
    }

    //Change Visibility of particular EditTexts, TextViews and Buttons to adjust to the calibration_layout state
    private void changeVisibilities(){
        cornersYET.setVisibility(View.GONE);
        imageNumET.setVisibility(View.GONE);
        cornersYTV = (TextView) findViewById(R.id.cornersY_text_view);
        cornersXTV = (TextView) findViewById(R.id.cornersX_text_view);
        imageNumTV = (TextView) findViewById(R.id.image_num_tv);
        cornersYTV.setVisibility(View.GONE);
        imageNumTV.setVisibility(View.GONE);
    }

    //On Click of the "pickImageButton" get the input data from the EditTexts
    private void getInputData(){
        try {
            drumRadius = Double.parseDouble(drumRadiusET.getText().toString());
            squareSizeX = Double.parseDouble(squareSizeXET.getText().toString());
            squareSizeY = Double.parseDouble(squareSizeYET.getText().toString());
            cornersX = Integer.parseInt(cornersXET.getText().toString());
            cornersY = Integer.parseInt(cornersYET.getText().toString());
            imageNum = Integer.parseInt(imageNumET.getText().toString());

            if (imageCounter == 0) {
                //Initialisation of the elevation-corners-data-array
                corners = new double[cornersX][2][cornersY];
            }
            if (imageCounter==1) {
                //Initialisation of the azimuth-corners-data-array
                cornersPhi = new double[cornersX][2][imageNum-1];
            }
        }catch (NumberFormatException ex){
            //Detects invalid input, declines confirmation and shows the user
            Log.e(DEBUG_TAG, "Invalid Number" + ex);
            Toast toast = Toast.makeText(getApplicationContext(), "Invalid Number", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    //Set-up the finish button and it's onClick-Listener
    private void setFinishButton(){
        finishButton.setVisibility(View.VISIBLE);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //preferencesManager.saveCorners(corners, cornersPhi);                                            //Save Corner Data
                buildCalibrationFunction();
                preferencesManager.saveData(imageCentreX, imageCentreY, theta_measured, coeffsTheta, sinCoeffs);//Save Calibration Data
                setResult(RESULT_OK);
                finish();

            }
        });
    }





    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////Create Layout to Pick Corners and Draw Circles on Images///////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void createCornerSelectionLayout(){
        imageSelected = true;

        //Initialization of the Gesture Listeners
        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
        sDetector = new ScaleGestureDetector(this, new ScaleListener());

        //Load String Resources for state distinguishing on Button-Click
        innerCircleString = getResources().getString(R.string.Inner_Circle_Button);
        outerCircleString = getResources().getString(R.string.Outer_Circle_Button);
        cornerString = getResources().getString(R.string.Corner_Confirmation);

        setContentView(R.layout.calibration_layout);

        //Helping TextView
        nextStep = (TextView) findViewById(R.id.next_step_text_view);

        //Set ImageView
        mImageView = (DrawImageView) findViewById(R.id.newImageView);
        mImageView.setImageBitmap(imageBitmap);

        //Load ImageResources in Bitmaps
        imageBitmap = imageBitmap.copy(imageBitmap.getConfig(), true);
        originalBitmap = imageBitmap.copy(imageBitmap.getConfig(),true);

        if (!circleDone && !drawCircle) {
            mButton = (Button) findViewById(R.id.calibration_confirmation);
        }

        //This distinguishes between the first and the following images.
        //If the first Image is done, we don't paint circles, but jump directly to corner selection.
        if(imageCounter > 1){

            //Sets all needed distinguishers and resets variables
            circleDone = true;
            mButton.setText(cornerString);
            nextStep.setText("Select Corners beginning at 0°");
            mImageView.setDrawCross(true);
            mImageView.setDrawCircle(false);
            detectedScale = 1;
            matrix = new Matrix();
        }

        //The onClickListener distinguishes the state of calibration_layout by the button-text
        //and sets the help-textView accordingly.
        mButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                //Data Selection of first Image
                if (imageCounter == 1) {

                    //confirmation of Inner Circle
                    if (mButton.getText().equals(innerCircleString)) {
                        onDrawInnerCircle();
                        mImageView.setImageBitmap(imageBitmap);
                        nextStep.setText("Place Circle surrounding the wanted View");
                    }

                    //confirmation of Outer Circle
                    else if (mButton.getText().equals(outerCircleString)) {
                        onDrawOuterCircle();
                        mImageView.setImageBitmap(imageBitmap);
                        nextStep.setText("Select Corners beginning at 0°");
                    }

                    //confirmation of Corner
                    else if (mButton.getText().equals(cornerString)) {
                        Log.i(DEBUG_TAG, "Corner Confirmed");
                        drawCrossOnCorner();
                        mImageView.setImageBitmap(imageBitmap);
                        if ((cornersX - (cornerNumber + 1)) > 0) {
                            nextStep.setText("Corner " + (cornerNumber + 1) + " confirmed, " +
                                    (cornersX - (cornerNumber + 1)) + " corners to go");
                        } else {
                            nextStep.setText("Place Corner on next Grid-Line");
                        }

                        drawCross = false;
                        if (cornerNumber < (cornersX - 1)) {
                            cornerNumber++;
                        } else {
                            cornerNumber = 0;
                            degree++;
                            if (degree >= cornersY) {
                                mButton.setText("Confirm Calibration");
                            }
                        }
                        cornerConfirmed = true;

                    }

                    //Data confirmation
                    else if (mButton.getText().equals("Confirm Calibration")) {
                        nextImage = true;

                        createPickImagesLayout();

                        //buildCalibrationFunction();

                    }
                }

                //Corner Selection of following Images
                else{
                    circleDone = true;

                    //Corner confirmation
                    if (mButton.getText().equals(cornerString)) {
                        Log.i(DEBUG_TAG, "Corner Confirmed");
                        drawCrossOnCorner();
                        mImageView.setImageBitmap(imageBitmap);
                        if ((cornersX - (cornerNumber + 1)) > 0) {
                            nextStep.setText("Corner " + (cornerNumber + 1) + " confirmed, " + (cornersX - (cornerNumber + 1)) + " corners to go");
                            cornerNumber++;
                        } else {
                            mButton.setText("Confirm Calibration");
                        }
                        drawCross = false;
                    }

                    //Data confirmation
                    else if (mButton.getText().equals("Confirm Calibration")) {
                        nextImage = true;
                        createPickImagesLayout();
                    }
                }
            }

        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////Pick Image from Gallery Results///////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri photoUri = intent.getData();
                if (photoUri != null) {
                    try {
                        imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);

                        imageCounter++;
                        if (savedIntent.getExtras() != null) {
                            corners = new double                            //Initialize elevation dependent corners
                                    [savedIntent.getIntExtra("Width1",-1)]  //dependent on the saved data
                                    [savedIntent.getIntExtra("Height1",-1)]
                                    [savedIntent.getIntExtra("Depth1",-1)];
                            cornersPhi = new double                         //Initialize azimuth dependent corners
                                    [savedIntent.getIntExtra("Width2",-1)]  //dependent on the saved data
                                    [savedIntent.getIntExtra("Height2",-1)]
                                    [savedIntent.getIntExtra("Depth2",-1)];
                            imageCentreX = savedIntent.getIntExtra("CentreX", 0);
                            imageCentreY = savedIntent.getIntExtra("CentreY", 0);


                            //Get the saved Corner Data and split it.
                            //Every "," , ";" marks beginning of new rows and columns and planes
                            //Actually just a DEBUG function to fasten up corner selections
                            if (savedIntent.getStringExtra("DataType").equals("CORNER_DATA_AVAILABLE")) {
                                for (int n = 1; n <= 2; n++) {
                                    String[] stringArray = savedIntent.getStringArrayExtra("Corners" + n);
                                    int a = 0;
                                    int b;
                                    int c;
                                    for (String aStringArray : stringArray) {
                                        String[] stringArray2 = aStringArray.split(";");
                                        b = 0;
                                        for (String bStringArray : stringArray2) {
                                            String[] stringArray3 = bStringArray.split(",");
                                            c = 0;
                                            for (String cStringArray : stringArray3) {
                                                if (n == 1 && !cStringArray.isEmpty()) {
                                                    corners[a][b][c] = Double.parseDouble(cStringArray);
                                                    c++;
                                                }

                                                if (n != 1 && !cStringArray.isEmpty()) {
                                                    cornersPhi[a][b][c] = Double.parseDouble(cStringArray);
                                                    c++;
                                                }
                                            }
                                            b++;
                                        }
                                        a++;
                                    }
                                }
                                //Calibrate with loaded Corners
                                buildCalibrationFunction();

                                //Save Data after Calibration
                                preferencesManager.saveData(imageCentreX, imageCentreY, theta_measured, coeffsTheta, sinCoeffs);
                                //preferencesManager.saveCorners(corners, cornersPhi);
                                setResult(RESULT_OK);

                                //Finish the Activity, redirection to calling Activity
                                finish();
                            }
                        }
                        else {
                            //If no Data is being detected, rebuild the Corner Selection Layout
                            createCornerSelectionLayout();
                        }



                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    //Connects the ImageView with Gesture Listeners
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Enables Gesture Detectors when an Image is selected
        if (imageSelected) {
            this.mDetector.onTouchEvent(event);
            this.sDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);

    }


    //Gesture Listeners enabling simple gestures like "SingleTap, LongPress, Scroll"
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            matrix.getValues(m);
            if (!drawCircle && !drawCross) {                        //Move the Image, if there is no circle or cross drawing in progress
                if (m[2] <= 0 && m[5] <= 0) {                       //Boarders declaration
                    matrix.postTranslate(-distanceX, -distanceY);   //Transformation Matrix
                } else if (m[2] <= 0) {                             //Boarder control y-Axis (m[5])
                    matrix.postTranslate(-distanceX, 0);            //By looking at the x-Axis a movement in both directions
                    matrix.getValues(m);                            //can be ruled out
                    m[5] = 0;
                    matrix.setValues(m);
                } else if (m[5] <= 0) {                             //Boarder control x-Axis (m[2])
                    matrix.postTranslate(0, -distanceY);            //By looking at the y-Axis a movement in both directions
                    matrix.getValues(m);                            //can be ruled out
                    m[2] = 0;
                    matrix.setValues(m);
                } else {                                            //Boarder control if movement in both
                    matrix.getValues(m);                            //directions over border limits occurs
                    m[2] = 0;
                    m[5] = 0;
                    matrix.setValues(m);
                }
                mImageView.setImageMatrix(matrix);                  //Set translation matrix to the image

                if (circleDone){
                    mImageView.invalidate();
                }
            } else {                                                  //If a circle or cross is in drawing progress move it instead
                circleXc = circleXc - (int) distanceX;              //Get touch position and set the according values in
                circleYc = circleYc - (int) distanceY;              //the DrawImageView
                mImageView.setXc((circleXc));
                mImageView.setYc((circleYc));
                mImageView.invalidate();                            //Invalidate the new positions
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }


        @Override
        public void onLongPress(MotionEvent e) {                    //Long press on the display results in
            if (!circleDone) {                                      //the start of the circle drawing
                circleXc = (int) e.getX();
                circleYc = (int) e.getY();
                mImageView.setDrawCircle(true);
                mImageView.setXc(circleXc);
                mImageView.setYc(circleYc);
                mImageView.invalidate();
                drawCircle = true;                                  //Set calibration_layout state to "circle drawing in progress"
            }
            super.onLongPress(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {        //Single Tap results in cross drawing, assuming circle
                                                                    //drawings are finished
            if (circleDone) {
                mImageView.setDrawCross(true);
                mImageView.setXc((int) e.getX());
                mImageView.setYc((int) e.getY());
                mImageView.invalidate();
                drawCross = true;                                   //Set state to "Cross drawing in progress"
                cornerConfirmed = false;                            //Waiting for confirmation of corners
            }
            return super.onSingleTapConfirmed(e);

        }
    }


    //Gesture Listeners enabling scale gestures
    private class ScaleListener extends ScaleGestureDetector.


            //Either scale image, or circle, if one is in the progress of drawing
            SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (!drawCircle && !drawCross) {
                float scale = detector.getScaleFactor();
                scale = Math.max(0.1f, Math.min(scale, 5.0f));
                matrix.postScale(scale, scale);
                mImageView.setImageMatrix(matrix);
                detectedScale *= detector.getScaleFactor();
            }
            else{
                float radiusScale = detector.getScaleFactor();
                radiusScale = Math.max(0.1f, Math.min(radiusScale, 5.0f));
                mImageView.setRadiusScale(radiusScale);
                mImageView.invalidate();

            }
            return true;
        }

        //Here we need to reset the radius scale in the DrawImageView, so Scrolling doesn't result in scaling
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            if (drawCircle){mImageView.setRadiusScale(1);}
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////Drawing Methods///////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    Paint paint2 = new Paint();                                                         //Initialization of the used Paint for drawing

    //Method to draw the inner circle, confirmation of circle
    private void onDrawInnerCircle(){

        drawCircle = false;                                                             //Set state to circle drawing finished

        mImageView.setDrawCircle(false);                                                //Set state to circle drawing finished in ImageView
        XcInnerCircle = (mImageView.getXc() / detectedScale) - (m[2] / detectedScale);  //Get x-centre of circle depending on the translation matrix
        YcInnerCircle = (mImageView.getYc() / detectedScale) - (m[5] / detectedScale);  //Get y-centre of circle depending on the translation matrix
        radiusInnerCircle = mImageView.getRadius() / detectedScale;                     //Get radius of circle depending on the image scale

        paint2.setStyle(Paint.Style.STROKE);                                            //Stroke = Line surrounding the circle
        paint2.setStrokeWidth(4);
        paint2.setColor(Color.RED);
        Canvas canvas = new Canvas(imageBitmap);                                        //Initialize Canvas with the image as Bitmap
        canvas.drawCircle(XcInnerCircle, YcInnerCircle, radiusInnerCircle, paint2);     //Draw circle
        mButton.setText(outerCircleString);                                             //Switch button text to initiate next phase of calibration_layout


    }

    //Method to draw the outer circle, confirmation of circle
    //Most information: See at "onDrawInnerCircle"
    private void onDrawOuterCircle(){
        mImageView.setDrawCircle(false);

        XcOuterCircle = (mImageView.getXc() / detectedScale) - (m[2] / detectedScale);
        YcOuterCircle = (mImageView.getYc() / detectedScale) - (m[5] / detectedScale);
        radiusOuterCircle = mImageView.getRadius() / detectedScale;

        Bitmap background = Bitmap.createBitmap
                (imageBitmap.getWidth(), imageBitmap.getHeight(), imageBitmap.getConfig());


        Canvas mask = new Canvas(background);                                           //Initialize a black background canvas
        mask.drawColor(Color.BLACK);
        paint2.setStyle(Paint.Style.FILL);
        paint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));            //This is used to remove color/ make it transparent
        paint2.setColor(Color.TRANSPARENT);
        mask.drawCircle(XcOuterCircle, YcOuterCircle, radiusOuterCircle, paint2);       //Remove the outer circle from the background
        paint2.setStyle(Paint.Style.FILL_AND_STROKE);                                   //Ensures, that the red drawn line is blackened out
        paint2.setColor(Color.BLACK);
        paint2.setXfermode(null);                                                       //Set it back to not removing, but drawing color on canvas
        mask.drawCircle(XcInnerCircle, YcInnerCircle, radiusInnerCircle, paint2);       //Draw inner circle filled with black color


        Canvas canvas = new Canvas(imageBitmap);                                        //Create new Canvas with the imageBitmap as drawing object
        canvas.drawBitmap(background, new Matrix(), null);                              //Draw the background bitmap on the imageBitmap
                                                                                        //Resulting in only the "Donut-View"
        circleDone = true;
        drawCircle = false;

        mButton.setText(cornerString);                                                  //Switch Button text to initialise next calibration_layout phase

        imageCentreX = (int) (XcOuterCircle + XcInnerCircle)/2;                         //Average centre position of outer and inner circle
        imageCentreY = (int) (YcOuterCircle + YcInnerCircle)/2;

    }


    private void drawCrossOnCorner(){
        Paint paint = new Paint();                                                      //Reset paint for corner painting
        float radius = mImageView.getRadius() / detectedScale;
        float XcCross = (mImageView.getXc() / detectedScale) - (m[2] / detectedScale);  //Same as at the "DrawCircle"-Methods
        float YcCross = (mImageView.getYc() / detectedScale) - (m[5] / detectedScale);
        mImageView.setDrawCross(false);


        //Sets all parameters to draw a crosshair on the image
        points[0] = XcCross + (radius/3);
        points[1] = YcCross;
        points[2] = XcCross + (3*radius/2);
        points[3] = YcCross;
        points[4] = XcCross - (radius/3);
        points[5] = YcCross;
        points[6] = XcCross - (3*radius/2);
        points[7] = YcCross;
        points[8] = XcCross;
        points[9] = YcCross + (radius/3);
        points[10] = XcCross;
        points[11] = YcCross + (3*radius/2);
        points[12] = XcCross;
        points[13] = YcCross - (radius/3);
        points[14] = XcCross;
        points[15] = YcCross - (3*radius/2);


        Canvas canvas = new Canvas(imageBitmap);                                        //Canvas with the imageBitmap
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(1);
        canvas.drawCircle(XcCross, YcCross, radius, paint);                             //Draws the crosshair circle
        canvas.drawLines(points, paint);                                                //Draws the lines on the canvas


        if (imageCounter == 1) {                                                        //Stores the Data of the first image
            corners[cornerNumber][0][degree] = XcCross;                                 //for elevation calibration_layout
            corners[cornerNumber][1][degree] = YcCross;
        }
        if (imageCounter != 1) {                                                        //Stores the Data of the second image
            cornersPhi[cornerNumber][0][imageCounter - 2] = XcCross;                    //for azimuth calibration_layout
            cornersPhi[cornerNumber][1][imageCounter - 2] = YcCross;
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////Calibration Functions////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The CalibrationFunction is using a polynomial interpolation by Newtons-Method
     * Newtons-Method build up a Newton-Polynomial in the Horner's-Scheme.
     * n calibration Points result in n coefficients and polynomial of the n-1th degree.
     * The Coefficients are defined by the Newton's-Method:
     * n Points with P=(x|f(x))
     * x(0) f(x0)  y(0)                                                                                     = c(0)
     * x(1) f(x1)  f(x0,x1) = (f(x1) - f(x0)) / (x1 - x0)                                                   = c(1)
     * x(2) f(x2)  f(x1,x2) = (f(x2) - f(x1)) / (x2 - x1)   (f(x0,x1,x2) = f(x1,x2) - f(x0,x1)= / (x2 - x1) = c(2)
     *              .
     *              .
     *              .
     *
     * Afterwards the actual Coefficients are identified by the Horner's-Method (Happens in the MainActivity
     */


    /**
     * The surrounding function for the initiation and completion of the calibration_layout
     */
    public void buildCalibrationFunction(){

        /**DEBUG
        if (imageCentreX== 0 || imageCentreY==0){
            imageCentreX = 548;
            imageCentreY = 628;
        }
        */

        //Initialization of needed variables
        double[] x = new double[cornersY];                                  //X-Coordinates of the corners
        double[] y = new double[cornersY];                                  //Y-Coordinates of the corners
        theta_measured = new double[cornersY];                              //x for Newton's-Method
        double[] f_x_theta = new double[cornersY];                          //f_x for Newton's-Method
        coeffsTheta = new double[cornersY];                                 //Coefficients from the Newton's-Method
        double calibratedPhi = 0;                                           //Azimuth of elevation-calibration_layout

        int k_p = 1;                                                        //Used to identify the elevation of the corner
        int k_n = 1;                                                        //k_p counts positive elevation, k_n negative


        //Averaging the corner positions for clearer results
        for (int n = 0; n < cornersY; n++) {
            for (int ind = 0; ind < 2; ind++) {
                x[n] = x[n] + corners[ind][0][n] - imageCentreX;
                y[n] = y[n] + corners[ind][1][n] - imageCentreY;
            }
            x[n] = x[n]/2;
            y[n] = y[n]/2;

            //Calculate the used azimuth for calibration_layout for later distance from centre adjustment
            calibratedPhi = calibratedPhi + Math.atan2(y[n],x[n]);

            //Using pythagoras' for distance calculation
            f_x_theta[n] = Math.sqrt((x[n] * x[n]) + (y[n] * y[n]));

            //Calculate the elevation degree by using User-Input. The first corners always are at
            //0° elevation, further corners can be calculated by the drumRadius and the
            //size of the chess-board-squares
            if (n == 0) {
                theta_measured[0] = 0;
            } else {
                if (f_x_theta[n] < f_x_theta[0]) {
                    theta_measured[n] =
                            Math.asin((squareSizeY * k_p) / drumRadius);
                    k_p++;
                } else {
                    theta_measured[n] = Math.asin(((-squareSizeY * k_n)) / drumRadius);
                    k_n++;
                }
            }

        }

        //Averaging the azimuth of the used corners
        calibratedPhi = calibratedPhi/(cornersY);

        //Determines the Coefficients of the newton's polynomial
        coeffsTheta = PolynomialInterpolation(f_x_theta, theta_measured);



        //Calculate the distance of the 0° line from the image centre dependent on the azimuth
        double[][] combinedPhi = new double[cornersPhi.length*(imageNum-1)][2];
        int ind = 0;
        for (double[][] aCornersPhi : cornersPhi) {
            for (int o = 0; o < imageNum-1; o++) {
                combinedPhi[ind][1] =
                        Math.sqrt(((aCornersPhi[0][o] - imageCentreX) * (aCornersPhi[0][o] - imageCentreX))
                                + ((aCornersPhi[1][o] - imageCentreY) * (aCornersPhi[1][o] - imageCentreY)));
                combinedPhi[ind][0] = Math.atan2(aCornersPhi[1][o] - imageCentreY, aCornersPhi[0][o] - imageCentreX);
                ind++;
            }
        }

        //Sorts the Array by the distance from the centre
        Arrays.sort(combinedPhi, new Comparator<double[]>() {
            @Override
            public int compare(double[] lhs, double[] rhs) {
                return Double.compare(lhs[1], rhs[1]);
            }
        });

        /**
         * The distance from the image centre depending on the elevation changes by the azimuth.
         * This distance drift is caused by lens-displacement on the camera.
         * To adjust this drift we implement a sinus-function for lens-misplacement adjustment.
         *
         * Universal Sinus function:
         * y = a*sin(b*x+c)+d
         *
         * For the calculation of coefficients we need the highest and lowest distance from corners
         * of one elevation from the centre depending on the azimuth. Therefor we take the average
         * of the azimuth degree of the highest 3-4 distances and the lowest 3-4 distances.
         *
         * We need a safety system to secure, that the lowest/ highest distances don't lay
         * at + and - 180°.
         *
         * So if we take the average we check the degree of the azimuth degree.
         * If too many are smaller or bigger than the highest/ lowest value +/- pi/2 we change
         * the point of view to +/- 180°.
         */
        boolean rebuild = true;
        int rebuildCounter = 1;
        while (rebuild) {
            boolean maxOk = true;
            boolean minOk = true;
            sinCoeffs = buildSinusFunction(combinedPhi, calibratedPhi);
            if (sinCoeffs[0] == 404 && sinCoeffs[1] == 404) {
                double[] tmp;
                tmp = combinedPhi[0];
                combinedPhi[0] = combinedPhi[rebuildCounter];
                combinedPhi[rebuildCounter] = tmp;
                maxOk = false;
            }
            if (sinCoeffs[2] == 404 && sinCoeffs[3] == 404) {
                double[] tmp;
                tmp = combinedPhi[combinedPhi.length - 1];
                combinedPhi[combinedPhi.length - 1] = combinedPhi[combinedPhi.length - 1 - rebuildCounter];
                combinedPhi[combinedPhi.length - 1 - rebuildCounter] = tmp;
                minOk = false;
            }
            if (!maxOk || !minOk) {
                buildSinusFunction(combinedPhi, calibratedPhi);
            } else {
                rebuild = false;
            }

            rebuildCounter++;
        }

    }

    /**
     * Here we calculate the coefficients of the adjustment function
     * General sinus:
     * y = a*sin(b*x+c)+d
     *
     * knowing the highest and lowest y-value of the sin function, the coefficients are easily
     * calculated:
     *
     * d = y_max/2 + y_min/2
     * c = PI/2 - ((PI * x(y_max))/(x(y_min) - x(y_max))
     * b = PI/(x(y_min) - x(y_max)
     * a = y_max - d
     *
     * @param combinedPhi An Array in the Form of Array[n][2], n is the amount of corners selected
     *                    on one elevation. The Array[][1] is the distance, sorted descending (y),
     *                    Array[][2] the related azimuth in radians (x).
     * @param calibratedPhi The azimuth, on which the elevation calibration was calculated.
     * @return The coefficients of the adjustment sinus function
     */
    public double[] buildSinusFunction(double[][] combinedPhi, double calibratedPhi){
        double distancePhiMin = combinedPhi[0][1];
        double distancePhiMax = combinedPhi[combinedPhi.length-1][1];
        double degreePhiMax = 0;
        double degreePhiMin = 0;
        double[] sinCoeffsBuild = new double[4];

        int maxCounter = 0;
        int minCounter = 0;

        //Here we check, if we are in the right angle direction (+/- 180°, see explanation at function call)
        for (int n = 0; n< 5; n++){
            if (n!=0 && combinedPhi[combinedPhi.length-1-n][0]>=combinedPhi[combinedPhi.length-1][0]-(Math.PI/2)
                    && combinedPhi[combinedPhi.length-1-n][0]<=combinedPhi[combinedPhi.length-1][0]+(Math.PI/2)) {

                degreePhiMax = degreePhiMax + combinedPhi[combinedPhi.length - 1 - n][0];

                //We count, how many Corners we use as for the average, if there are to few, we
                //rebuild the calibration with changed parameters
                maxCounter++;
            }
            if (n!=0 && combinedPhi[n][0]>=combinedPhi[0][0]-(Math.PI/2)
                    && combinedPhi[n][0]<=combinedPhi[0][0]+(Math.PI/2)) {

                degreePhiMin = degreePhiMin + combinedPhi[n][0];

                //We count, how many Corners we use as for the average, if there are to few, we
                //rebuild the calibration with changed parameters
                minCounter++;
            }
        }

        //Used for the rebuild loop, to check, if enough values were used to average the system
        if (minCounter <= 1 || maxCounter <= 1){
            double[] error = new double[4];
            if (minCounter <= 1){
                error[0] = 404;
                error[1] = 404;
            }
            if (maxCounter <= 1){
                error[2] = 404;
                error[3] = 404;
            }
            return error;
        }

        //If everything is fine, we take the average and continue with the coefficient calculation
        degreePhiMax = degreePhiMax/(maxCounter);
        degreePhiMin = degreePhiMin/(minCounter);

        //See function comment.
        sinCoeffsBuild[3] = (distancePhiMax/2) + (distancePhiMin/2);
        sinCoeffsBuild[2] = Math.PI/(degreePhiMin - degreePhiMax);
        sinCoeffsBuild[1] = (Math.PI/2) - ((Math.PI*degreePhiMax)/(degreePhiMin-degreePhiMax));
        sinCoeffsBuild[0] = distancePhiMax - sinCoeffsBuild[3];

        //we take the distance at the calibrated azimuth
        double calibratedDistance = (sinCoeffsBuild[0]*Math.sin((sinCoeffsBuild[2]*calibratedPhi)+sinCoeffsBuild[2]))+sinCoeffsBuild[3];

        //And divide a and d by the distance. Thereby we receive a "normalised" sinus function
        //This is then used to multiply it with the calculated distance in the
        //MainActivity's-OnCameraStart Method
        sinCoeffsBuild[0] = sinCoeffsBuild[0]/calibratedDistance;
        sinCoeffsBuild[3] = sinCoeffsBuild[3]/calibratedDistance;

        return sinCoeffsBuild;
    }

    //Newton's-Method, see "buildCalibrationFunction
    public double[] PolynomialInterpolation(double[] f_x, double[] x){

        double[] c = new double[f_x.length];

        for (int n = 0; n< f_x.length; n++){
            c[n] = f_x[0];
            for (int m = 0; m< f_x.length-n-1; m++){
                f_x[m] = (f_x[m+1] - f_x[m]) / (x[m+1+n] - x[m]);
            }
        }

        return c;
    }



    /** DEBUG

     private void createDebugLayout(){
     setContentView(R.layout.debug_corners);
     debugView = (ImageView) findViewById(R.id.debug_image_view);
     debugView.setImageBitmap(originalBitmap);
     imageBitmap = imageBitmap.copy(imageBitmap.getConfig(), true);
     Button debug_Button = (Button) findViewById(R.id.debug_button);
     debug_Button.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
    buildCalibrationFunction();
    }
    });

     }
     */

}