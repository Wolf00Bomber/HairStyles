package hairstyle.twod.com.hairstyles;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by srikrishna on 04-10-2016.
 */
public class CustomImageSeekerListener implements View.OnTouchListener {

    public CustomImageSeekerListener(ImageSeekerInterface listener) {
        this.listener = listener;
    }

    float tempX;
    private static final int SWIPE_MIN_DISTANCE = 10;
    ImageSeekerInterface listener;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            tempX = event.getX();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float currentX = event.getX();
            Log.i("Touch", "Current X=" + currentX + " prev X=" + tempX);
            boolean isLeft = tempX - currentX > 0;
            if (currentX - tempX > SWIPE_MIN_DISTANCE || tempX - event.getX() > SWIPE_MIN_DISTANCE) {
                tempX = currentX;
                if (listener != null)
                    listener.onSwipe(isLeft);
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP ||
                event.getAction() == MotionEvent.ACTION_CANCEL) {
            tempX = 0f;
        }
        return true;
    }
}
