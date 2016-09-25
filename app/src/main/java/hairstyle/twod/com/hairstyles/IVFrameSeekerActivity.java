package hairstyle.twod.com.hairstyles;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by srikrishna on 26/09/16.
 */

public class IVFrameSeekerActivity extends AppCompatActivity {

    int minValue = 80;
    int maxValue = 138;

    int currentImageId = 0;
    private static final int SWIPE_MIN_DISTANCE = 20;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private ImageView iv;

    int [] resId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view_seeker);
        iv = (ImageView) findViewById(R.id.iv);

        int to = maxValue;
        int from = minValue;
        resId = new int[maxValue - minValue + 1];
        int j = from;
        while (from <= to) {
            String name = "out_" + String.format("%04d", from);
            resId[from - j] = getResources().getIdentifier(name, "drawable", getPackageName());
            from++;
        }

        iv.setOnTouchListener(new View.OnTouchListener() {
            float tempX;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    tempX = event.getX();
                }
                else if(event.getAction() == MotionEvent.ACTION_MOVE)
                {
                    float currentX = event.getX();
                    Log.i("Touch", "Current X="+currentX+" prev X="+tempX);
                    boolean isLeft = tempX - currentX > 0;
                    if (currentX - tempX > SWIPE_MIN_DISTANCE) {
                        tempX = currentX;
                        onSwipe(isLeft);
                    }
                    // left to right swipe
                    else if (tempX - event.getX() > SWIPE_MIN_DISTANCE) {
                        tempX = currentX;
                        onSwipe(isLeft);
                    }
//                    else {
//                        float delta = currentX - tempX;
//                        tempX += delta;
//                    }

                }
                else if(event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_CANCEL)
                {
                    tempX = 0f;
                }

                return true;
            }
        });
        iv.setImageResource(resId[0]);

    }

    private void onSwipe(boolean isLeft)
    {
        int tempValue = currentImageId;
        if(isLeft){
            int maxValueCur = Math.max(--tempValue, 0);
            tempValue = maxValueCur;
        }
        else{
            int minValueCur = Math.min(++tempValue, maxValue - minValue);
            tempValue = minValueCur;
        }
        Log.i("tempValue", ""+tempValue);
        iv.setImageResource(resId[tempValue]);
        currentImageId = tempValue;
    }

}
