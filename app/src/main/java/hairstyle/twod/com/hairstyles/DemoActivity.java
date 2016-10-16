package hairstyle.twod.com.hairstyles;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

import ui.camera.CameraSourcePreview;
import ui.camera.CameraUtils;
import ui.camera.GraphicOverlay;
import ui.camera.VideoRecordView;

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

    private static final String TAG = DemoActivity.class.getSimpleName();

    private static final int MODE_ONE = 1;
    private static final int MODE_TWO = 2;

    private int mode = MODE_ONE;

    // Camera Sequence One with Face Recognition.
    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private static final int RC_HANDLE_GMS = 9001;

    // Camera Sequence Two.
    private Camera mCamera;
    private VideoRecordView mSurfaceView;
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
        if (arePermissionGranted(permissions)) {
            initFirstCameraForFaceRecognition();
        } else {
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
        }
    }

    private void initFirstCameraForFaceRecognition() {
        mode = MODE_ONE;
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);
        createCameraSource();
    }

    public void switchToVideoRecording() {
//        mPreview.stop();
//        mPreview.release();
//        mPreview.releaseSurface();
//        mPreview.setVisibility(View.GONE);
        initSecondCameraForVideoRecording();
    }

    private void initSecondCameraForVideoRecording() {
        mode = MODE_TWO;
        mCamera = CameraUtils.getCameraInstance();
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        CameraUtils.setCameraDisplayOrientation(mCamera, rotation);
        CameraUtils.setCameraParameters(mCamera);
        mSurfaceView = new VideoRecordView(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeAllViews();
        preview.addView(mSurfaceView);
        mToggleButton = findViewById(R.id.toggleRecordingButton);
        mToggleButton.setVisibility(View.VISIBLE);
        mToggleButton.setOnClickListener(new OnClickListener() {
            @Override
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mode == MODE_ONE) {
            startCameraSource();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null && mode == MODE_ONE)
            mPreview.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
        if (mCamera != null) {
            mCamera.release();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initFirstCameraForFaceRecognition();
                } else {
                    finish();
                }
            }
        }
    }

    // Face Tracker API
    private void startCameraSource() {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private void createCameraSource() {
        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();
        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());
        if (!detector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }
        DisplayMetrics dM = getResources().getDisplayMetrics();
        int w = dM.widthPixels;
        int h = dM.heightPixels;
        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;
        private FacePlaceHolder mFaceHolder;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay, DemoActivity.this);
            mFaceHolder = new FacePlaceHolder(overlay, DemoActivity.this, R.drawable.out_0079);
            mOverlay.add(mFaceHolder);
        }

        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }
}
