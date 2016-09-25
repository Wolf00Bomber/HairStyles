package hairstyle.twod.com.hairstyles;

import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

/**
 * Created by srikrishna on 24/09/16.
 * Camera Implementation as read in the following SO
 * http://stackoverflow.com/questions/23948573/record-video-using-surface-view-android
 * And Android Documentation:
 * https://developer.android.com/guide/topics/media/camera.html#custom-camera
 */
@SuppressWarnings("ALL")
public class AdvancedTweenedAnimation extends AppCompatActivity implements SurfaceHolder.Callback{

    ImageView ivTweened;
    ProgressBar pBar;
    int cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;

    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private View mToggleButton;
    private boolean mInitSuccesful;
    private final String VIDEO_PATH_NAME = "/mnt/sdcard/Ditto.mp4";

    private AnimationDrawable animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advanced_tween_layout);
        // we shall take the video in landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ivTweened = (ImageView) findViewById(R.id.ivTweened);
        pBar = (ProgressBar) findViewById(R.id.pBar);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mToggleButton = (ToggleButton) findViewById(R.id.toggleRecordingButton);
        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            // toggle video recording
            public void onClick(View v) {
                if (((ToggleButton)v).isChecked()) {
                    mMediaRecorder.start();
                }
                else {
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    try {
                        initRecorder(mHolder.getSurface());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                animation = getAnimation();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pBar.setVisibility(View.INVISIBLE);
                        mToggleButton.setVisibility(View.VISIBLE);
                        ivTweened.setImageDrawable(animation);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private CustomAnimationDrawable getAnimation()
    {
        int minValue = 80;
        int maxValue = 138;
        CustomAnimationDrawable animation = new CustomAnimationDrawable();
        animation.setAnimationFinishListener(new CustomAnimationDrawable.IAnimationFinishListener() {
            @Override
            public void onAnimationFinished() {
                mMediaRecorder.stop();
            }
        });
        animation.setOneShot(true);
        Resources res = getResources();

        int defGlobeId = res.getIdentifier("out_" + String.format("%04d", 79), "drawable", getPackageName());
        animation.addFrame(res.getDrawable(defGlobeId), 40);

        int to = maxValue;
        int from = minValue;
        while (from <= to) {
            String name = "out_" + String.format("%04d", from);
            int globeId = res.getIdentifier(name, "drawable", getPackageName());
            animation.addFrame(res.getDrawable(globeId), 20);
            from++;
        }
        from = maxValue;
        to = minValue;
        while (from >= to) {
            String name = "out_" + String.format("%04d", from);
            int globeId = res.getIdentifier(name, "drawable", getPackageName());
            animation.addFrame(res.getDrawable(globeId), 20);
            from--;
        }
        from = minValue;
        to = maxValue;
        while (from <= to) {
            String name = "out_" + String.format("%04d", from);
            int globeId = res.getIdentifier(name, "drawable", getPackageName());
            Drawable rotateDrawable = getReflectedDrawable(globeId);
            animation.addFrame(rotateDrawable, 20);
            from++;
        }
        from = maxValue;
        to = minValue;
        while (from >= to) {
            String name = "out_" + String.format("%04d", from);
            int globeId = res.getIdentifier(name, "drawable", getPackageName());
            Drawable rotateDrawable = getReflectedDrawable(globeId);
            animation.addFrame(rotateDrawable, 20);
            from--;
        }
        animation.addFrame(res.getDrawable(defGlobeId), 40);

        return animation;
    }

    public Drawable getReflectedDrawable(int resId) {
        Bitmap originalImage = BitmapFactory.decodeResource(getResources(),
                resId);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);
        // Create reflection images (which is half the size of the original image)
        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, 0, width, height, matrix, false);
        originalImage.recycle();
        return new BitmapDrawable(reflectionImage);
    }

    /* Init the MediaRecorder, the order the methods are called is vital to
 * its correct functioning */
    private void initRecorder(Surface surface) throws IOException {

        if(mMediaRecorder == null)  mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setPreviewDisplay(surface);

        // It is very important to unlock the camera before doing setCamera
        // or it will results in a black preview
        try {
//            releaseCameraAndPreview();
            if(mCamera == null) {
                mCamera = Camera.open(cameraId);
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();
                mCamera.unlock();
            }
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }

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
            // This is thrown if the previous calls are not called with the
            // proper order
            e.printStackTrace();
        }

        mInitSuccesful = true;
    }

    private void releaseCameraAndPreview() {
        mMediaRecorder.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if(!mInitSuccesful)
                initRecorder(mHolder.getSurface());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        shutdown();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    private void shutdown() {
        // Release MediaRecorder and especially the Camera as it's a shared
        // object that can be used by other applications
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mCamera.release();

        // once the objects have been released they can't be reused
        mMediaRecorder = null;
        mCamera = null;
    }

    private static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "HairStyles");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("HairStyles", "failed to create directory");
                return null;
            }
        }
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "Ditto" + ".mp4");

        return mediaFile;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mCamera != null)
            mCamera.release();
    }
}
