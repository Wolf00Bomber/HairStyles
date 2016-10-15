package hairstyle.twod.com.hairstyles;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.List;

import ui.camera.CameraUtils;
import ui.camera.CameraView;

/**
 * Created by Sri Krishna on 04-10-2016.\
 * 1. Fix the Face onto the Grid.
 * 2. Start Video Recording + Tweened animation.
 * 3. Take a Snap.
 * 4. Splice the Video into many Pieces.
 * 5. Select a Wig Dialog.
 * 5. Apply the Wig to the Image Set.
 * 6. Run the Images in a IV Seeker Stage Up.
 */
public class DemoActivity extends AppCompatActivity {


    private Camera mCamera;
    private CameraView mSurfaceView;
    private View mToggleButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_recorder_recipe);
        mCamera = CameraUtils.getCameraInstance();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        CameraUtils.setCameraDisplayOrientation(mCamera, rotation);
        CameraUtils.setCameraParameters(mCamera);
        mSurfaceView = new CameraView(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mSurfaceView);
        mToggleButton = findViewById(R.id.toggleRecordingButton);
        mToggleButton.setOnClickListener(new OnClickListener() {
            @Override
            // toggle video recording
            public void onClick(View v) {
                if (((ToggleButton) v).isChecked()) {
                    mSurfaceView.startRecording();
                } else {
                    mSurfaceView.stopRecording();
                }
            }
        });
    }


}
