package hairstyle.twod.com.hairstyles;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnDitto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, FaceTrackerActivity.class);
                startActivity(i);
            }
        });
        findViewById(R.id.btnTweened).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, TweenedActivity.class);
                startActivity(i);
            }
        });
        findViewById(R.id.btnFullTweenedWithVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AdvancedTweenedAnimation.class);
                startActivity(i);
            }
        });
        findViewById(R.id.btnSeeker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, IVFrameSeekerActivity.class);
                startActivity(i);
            }
        });



    }
}
