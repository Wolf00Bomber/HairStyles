package hairstyle.twod.com.hairstyles;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by srikrishna on 26/09/16.
 */

public class IVFrameSeekerActivity extends AppCompatActivity implements ImageSeekerInterface {

    int minValue = 80;
    int maxValue = 138;

    int currentImageId = 0;

    private ImageView iv;

    int[] resId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_view_seeker);
        setUp();
        iv = (ImageView) findViewById(R.id.iv);
        iv.setOnTouchListener(new CustomImageSeekerListener(this));
        iv.setImageResource(resId[0]);
    }

    private void setUp() {
        int to = maxValue;
        int from = minValue;
        resId = new int[maxValue - minValue + 1];
        int j = from;
        while (from <= to) {
            String name = "out_" + String.format("%04d", from);
            resId[from - j] = getResources().getIdentifier(name, "drawable", getPackageName());
            from++;
        }
    }

    @Override
    public void onSwipe(boolean isLeft) {
        int tempValue = currentImageId;
        if (isLeft) {
            int maxValueCur = Math.max(--tempValue, 0);
            tempValue = maxValueCur;
        } else {
            int minValueCur = Math.min(++tempValue, maxValue - minValue);
            tempValue = minValueCur;
        }
        Log.i("tempValue", "" + tempValue);
        iv.setImageResource(resId[tempValue]);
        currentImageId = tempValue;
    }

}
