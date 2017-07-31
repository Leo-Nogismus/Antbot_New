package insectsrobotics.imagemaipulations.Calibration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;


public class DrawImageView extends ImageView {

    public void setDrawCircle(boolean drawCircle) {
        this.drawCircle = drawCircle;
    }

    public void setXc(int xc) {
        Xc = xc;
    }


    public void setYc(int yc) {
        Yc = yc;
    }

    public void setRadiusScale(float radiusScale) {
        this.radiusScale = radiusScale;
    }

    public void setDrawCross(boolean drawCross) {
        this.drawCross = drawCross;
    }


    boolean drawCircle;
    boolean drawCross;
    int Xc = 0;
    int Yc = 0;
    float radius = 200;
    float radiusScale = 1;
    float[] points = new float[16];

    Paint paint = new Paint();

    public float[] getPoints() {
        return points;
    }

    public int getYc() {
        return Yc;
    }

    public int getXc() {
        return Xc;
    }

    public float getRadius() {
        return radius;
    }

    public DrawImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onReset(){
        Xc = 0;
        Yc = 0;
        radius = 200;
        radiusScale = 1;
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawCircle) {
            radius = radius*radiusScale;
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(5);
            canvas.drawCircle(Xc, Yc,radius, paint);
        }
        if (drawCross){
            radius = 20;

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.RED);

            points[0] = Xc + (radius/3);
            points[1] = Yc;
            points[2] = Xc + (3*radius/2);
            points[3] = Yc;
            points[4] = Xc - (radius/3);
            points[5] = Yc;
            points[6] = Xc - (3*radius/2);
            points[7] = Yc;
            points[8] = Xc;
            points[9] = Yc + (radius/3);
            points[10] = Xc;
            points[11] = Yc + (3*radius/2);
            points[12] = Xc;
            points[13] = Yc - (radius/3);
            points[14] = Xc;
            points[15] = Yc - (3*radius/2);

            paint.setStrokeWidth(2);
            canvas.drawCircle(Xc, Yc, radius, paint);
            canvas.drawLines(points,paint);
        }
    }
}
