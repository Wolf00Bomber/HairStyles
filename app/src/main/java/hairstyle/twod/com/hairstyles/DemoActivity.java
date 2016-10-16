package hairstyle.twod.com.hairstyles;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ToggleButton;

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

    private static final int MY_PERMISSIONS_REQUEST = 24;
    String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private boolean arePermissionGranted(String[] permissions) {
        boolean permissionGranted = true;
        for (int i = 0; i < permissions.length; i++) {
            permissionGranted &= ContextCompat.checkSelfPermission(this, permissions[i]) == PackageManager.PERMISSION_GRANTED;
        }
        return permissionGranted;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_recorder_recipe);
        mToggleButton = findViewById(R.id.toggleRecordingButton);
        mToggleButton.setOnClickListener(new OnClickListener() {
            @Override
            // toggle video recording
            public void onClick(View v) {
                if (mSurfaceView == null)
                    return;
                if (((ToggleButton) v).isChecked()) {
                    mSurfaceView.startRecording();
                } else {
                    mSurfaceView.stopRecording();
                }
            }
        });
        if (arePermissionGranted(permissions)) {
            initView();
        } else {
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
        }
    }

    private void initView() {
        mCamera = CameraUtils.getCameraInstance();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        CameraUtils.setCameraDisplayOrientation(mCamera, rotation);
        CameraUtils.setCameraParameters(mCamera);
        mSurfaceView = new CameraView(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mSurfaceView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initView();
                } else {
                    finish();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCamera != null) {
            mCamera.release();
        }

    }
}
