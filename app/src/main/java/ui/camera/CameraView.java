package ui.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by srikrishna on 15-10-2016.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = CameraView.class.getSimpleName();
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size mPreviewSize;
    private MediaRecorder mMediaRecorder;
    private Context mContext;

    String DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/cameraTest/";
    String VIDEO_FILE_NAME = "FaceMap.mp4";
    String IMAGE_FILE_NAME = "FaceMap.jpg";

    private final String VIDEO_PATH_NAME = DIRECTORY + VIDEO_FILE_NAME/*"/mnt/sdcard/Ditto.mp4"*/;
    private final String DITTO_IMAGE_PATH_NAME = DIRECTORY + IMAGE_FILE_NAME/*"/mnt/sdcard/Ditto_Profile.jpg"*/;
    private static final int MIN_DURATION = 20;


    public CameraView(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mContext = context;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        releaseMediaRecorder();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        if (mPreviewSize != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            mCamera.setParameters(parameters);
        }
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = CameraUtils.getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }


    private void initRecorder() {
        // It is very important to unlock the camera before doing setCamera
        // or it will results in a black preview
        if (mCamera == null) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            CameraUtils.setCameraDisplayOrientation(mCamera, ((Activity) mContext).getWindowManager().getDefaultDisplay().getRotation());
            CameraUtils.setCameraParameters(mCamera);
        }

        mCamera.unlock();

        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();

        mMediaRecorder.reset();

        mMediaRecorder.setPreviewDisplay(getHolder().getSurface());
        mMediaRecorder.setOrientationHint(270);

        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setVideoEncodingBitRate(512 * 1000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(640, 480);
        mMediaRecorder.setOutputFile(VIDEO_PATH_NAME);

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            e.printStackTrace();
        } catch (IOException e) {
            releaseMediaRecorder();
            e.printStackTrace();
        }
    }

    public void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    public void startRecording() {
        try {
            initRecorder();
            mMediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stopRecording() {
        try {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}