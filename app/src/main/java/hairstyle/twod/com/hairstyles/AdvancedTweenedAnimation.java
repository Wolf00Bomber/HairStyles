package hairstyle.twod.com.hairstyles;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
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
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by srikrishna on 24/09/16.
 * Camera Implementation as read in the following SO
 * http://stackoverflow.com/questions/23948573/record-video-using-surface-view-android
 * And Android Documentation:
 * https://developer.android.com/guide/topics/media/camera.html#custom-camera
 * Camera Not Released:
 * http://stackoverflow.com/questions/8132273/release-android-camera-without-restart
 */
@SuppressWarnings("ALL")
public class AdvancedTweenedAnimation extends AppCompatActivity implements SurfaceHolder.Callback {

    ImageView ivTweened;
    ProgressBar pBar;
    Button viewVideo;
    int cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;

    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private ToggleButton mToggleButton;
    private boolean mInitSuccesful;


    String DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/cameraTest/";
    String VIDEO_FILE_NAME = "FaceMap.mp4";
    String IMAGE_FILE_NAME = "FaceMap.jpg";

    private final String VIDEO_PATH_NAME = DIRECTORY + VIDEO_FILE_NAME/*"/mnt/sdcard/Ditto.mp4"*/;
    private final String DITTO_IMAGE_PATH_NAME = DIRECTORY + IMAGE_FILE_NAME/*"/mnt/sdcard/Ditto_Profile.jpg"*/;
    private static final int MIN_DURATION = 20;

    private AnimationDrawable animation;

    private HashMap<Integer, Drawable> hmImages = new HashMap<>();

    private void playVideo() {
        Uri uri = Uri.parse(VIDEO_PATH_NAME);
        Intent startVideo = new Intent(Intent.ACTION_VIEW);
        startVideo.setDataAndType(uri, "video/*");
        startActivity(startVideo);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.advanced_tween_layout);
        // we shall take the video in landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ivTweened = (ImageView) findViewById(R.id.ivTweened);
        pBar = (ProgressBar) findViewById(R.id.pBar);
        viewVideo = (Button) findViewById(R.id.viewVideo);
        File videoFile = new File(VIDEO_PATH_NAME);
        Log.i("Advanced", "Video Path: " + videoFile.getAbsolutePath());
        if (videoFile.exists()) {
            viewVideo.setVisibility(View.VISIBLE);
            viewVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    playVideo();
                }
            });
        } else {
            viewVideo.setVisibility(View.INVISIBLE);
        }

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mToggleButton = (ToggleButton) findViewById(R.id.toggleRecordingButton);
        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            // toggle video recording
            public void onClick(View v) {
                if (!mInitSuccesful) {
                    showToast("Initialization Failed");
                    mToggleButton.setChecked(!((ToggleButton) v).isChecked());
                    return;
                }
                if (((ToggleButton) v).isChecked()) {
                    if (mMediaRecorder != null)
                        mMediaRecorder.start();
                } else {
                    if (mMediaRecorder != null) {
                        mMediaRecorder.stop();
                        mMediaRecorder.reset();
                        mMediaRecorder.release();
                        mMediaRecorder = null;
                    }
                    initRecorder(mHolder.getSurface());
                    takeSnap();
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
        try {
            initRecorder(mHolder.getSurface());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void takeSnap()
    {

        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

            }
        });
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback(){
        @Override
        public void onPictureTaken(byte[] data, Camera camera){
            File pictureFile = getOutputMediaFile();

            if(pictureFile == null){
                Log.d("TEST", "Error creating media file, check storage permissions");
                return;
            }

            try{
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            }catch(FileNotFoundException e){
                Log.d("TEST","File not found: "+e.getMessage());
            } catch (IOException e){
                Log.d("TEST","Error accessing file: "+e.getMessage());
            }
        }
    };

    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"HairStyles");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        // Create a media file name
        File mediaFile = new File(mediaStorageDir.getPath()+File.separator+"Ditto_profile.jpg");
        return mediaFile;
    }

    private CustomAnimationDrawable getAnimation() {
        int minValue = 80;
        int maxValue = 138;
        CustomAnimationDrawable animation = new CustomAnimationDrawable();
        animation.setAnimationFinishListener(new CustomAnimationDrawable.IAnimationFinishListener() {
            @Override
            public void onAnimationFinished() {
                if (mMediaRecorder != null) {
                    mMediaRecorder.stop();
                    mMediaRecorder.release();
                    mMediaRecorder = null;
                }
                initRecorder(mHolder.getSurface());
            }
        });
        animation.setOneShot(true);
        Resources res = getResources();
        int defGlobeId = res.getIdentifier("out_" + String.format("%04d", 79), "drawable", getPackageName());
        animation.addFrame(res.getDrawable(defGlobeId), 40);
        setUpAnimations(minValue, maxValue, res, animation, false);
        setUpAnimations(maxValue, minValue, res, animation, false);
        setUpAnimations(maxValue, minValue, res, animation, true);
        setUpAnimations(maxValue, minValue, res, animation, true);
        animation.addFrame(res.getDrawable(defGlobeId), 40);
        return animation;
    }

    private void setUpAnimations(int from, int to, Resources res, AnimationDrawable animation, boolean fetchFromReflect) {
        boolean isIncreasing = from < to;
        while (isIncreasing ? from <= to : from >= to) {
            String name = "out_" + String.format("%04d", from);
            int globeId = res.getIdentifier(name, "drawable", getPackageName());
            Drawable rotateDrawable = fetchFromReflect ? getReflectedDrawable(globeId) : getReflectedDrawable(globeId);
            animation.addFrame(rotateDrawable, MIN_DURATION);
            if (isIncreasing) from++;
            else from--;
        }
    }

    public Drawable getReflectedDrawable(int resId) {
        if (hmImages.containsKey(resId)) {
            return hmImages.get(resId);
        }
        Bitmap originalImage = BitmapFactory.decodeResource(getResources(),
                resId);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(-1, 1);
        // Create reflection images (which is half the size of the original image)
        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, 0, width, height, matrix, false);
        originalImage.recycle();
        Drawable d = new BitmapDrawable(reflectionImage);
        hmImages.put(resId, d);
        return d;
    }

    /* Init the MediaRecorder, the order the methods are called is vital to
 * its correct functioning */
    public void initRecorder(Surface surface) {

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.reset();
//        rotateBackVideo(mMediaRecorder);
        mMediaRecorder.setOrientationHint(270);
        mMediaRecorder.setPreviewDisplay(surface);

        // It is very important to unlock the camera before doing setCamera
        // or it will results in a black preview
        try {
//            releaseCameraAndPreview();
            if (mCamera == null) {
                mCamera = Camera.open(cameraId);
                setCameraDisplayOrientation(mCamera);
                mCamera.setPreviewDisplay(mHolder);
                mCamera.unlock();
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
                // This is thrown if the previous calls are not called with the proper order
                e.printStackTrace();
            }

            mInitSuccesful = true;
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
            shutdown();
        }
    }

    public void rotateBackVideo(MediaRecorder mMediaRecorder) {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        /**
         * Define Orientation of video in here,
         * if in portrait mode, use value = 90,
         * if in landscape mode, use value = 0
         */
        switch (rotation) {
            case 0:
                mMediaRecorder.setOrientationHint(90);
                break;
            case 90:
                mMediaRecorder.setOrientationHint(180);
                break;
            case 180:
                mMediaRecorder.setOrientationHint(270);
                break;
            case 270:
                mMediaRecorder.setOrientationHint(0);
                break;
        }
    }

    public void setCameraDisplayOrientation(Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        Camera.Parameters parameters = mCamera.getParameters();

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        Log.d("Advanced", "Result = " + result);
        camera.setDisplayOrientation(result);

        parameters.set("orientation", "portrait");
        parameters.setRotation(result);
        camera.setParameters(parameters);
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
        if (!mInitSuccesful){
            initRecorder(mHolder.getSurface());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        shutdown();
        mInitSuccesful = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    private void shutdown() {
        // Release MediaRecorder and especially the Camera as it's a shared
        // object that can be used by other applications
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
        }
        if (mCamera != null) {
            showToast("Camera Released.");
            mCamera.release();
        }

        // once the objects have been released they can't be reused
        mMediaRecorder = null;
        mCamera = null;
    }

    private void showToast(String message) {
        Toast.makeText(AdvancedTweenedAnimation.this, message, Toast.LENGTH_SHORT).show();
    }

    /*private static File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "HairStyles");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("HairStyles", "failed to create directory");
                return null;
            }
        }
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "Ditto" + ".mp4");

        return mediaFile;
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        shutdown();
    }
}
