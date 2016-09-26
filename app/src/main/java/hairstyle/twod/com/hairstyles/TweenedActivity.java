package hairstyle.twod.com.hairstyles;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * Created by srikrishna on 21-09-2016.
 */
public class TweenedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tween_layout);
        ImageView ivTweened = (ImageView) findViewById(R.id.ivTweened);
        ivTweened.setImageResource(R.drawable.animation_sequence);

        // Get the background, which has been compiled to an AnimationDrawable object.
        AnimationDrawable frameAnimation = (AnimationDrawable) ivTweened.getDrawable();

        // Start the animation (looped playback by default).
        frameAnimation.start();

    }
}
