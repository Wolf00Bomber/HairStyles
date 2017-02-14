package hairstyle.twod.com.hairstyles;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.vision.face.FaceDetector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

/**
 * Created by srikrishna on 04-10-2016.
 */
public class WigStitchActivity extends AppCompatActivity implements ImageSeekerInterface {

    int wigResSelected = R.drawable.cool_mens_curly_hairstyles_wig;
    ImageView iv;
    ProgressBar pBar;
    String TO_DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/cameraTest/Wigs/";
    String VIDEO_DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/cameraTest/";
    String FROM_DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/cameraTest/VideoSlices/";
    private static final String TAG = WigStitchActivity.class.getSimpleName();
    String VIDEO_FILE_NAME = "FaceMap.mp4";
    File saveFolder;
    int duration;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wig_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.two_part:
                if(state == TWO)
                    return true;
                state = TWO;
                startPrep();
                return true;
            case R.id.three_part:
                if(state == THREE)
                    return true;
                state = THREE;
                startPrep();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    FaceDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wig_stitch_layout);
        iv = (ImageView) findViewById(R.id.iv);
        pBar = (ProgressBar) findViewById(R.id.pBar);
        iv.setOnTouchListener(new CustomImageSeekerListener(this));
        if (getIntent().hasExtra("wigResId")) {
            wigResSelected = getIntent().getIntExtra("wigResId", wigResSelected);
        }
        startPrep();
    }

    private void startPrep(){
        pBar.setVisibility(View.VISIBLE);
        iv.setVisibility(View.INVISIBLE);
        Thread th = new Thread(r);
        th.start();
    }


    Runnable r = new Runnable() {
        @Override
        public void run() {
            setUpFolders();
            processVideoSplitWithWigs(true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pBar.setVisibility(View.INVISIBLE);
                    iv.setVisibility(View.VISIBLE);
                    iv.setOnTouchListener(new CustomImageSeekerListener(WigStitchActivity.this));
                    onSwipe(true);
                }
            });
        }
    };

    public void setUpFolders() {
        saveFolder = new File(TO_DIRECTORY + wigResSelected + File.separatorChar);
        if (saveFolder.exists() && saveFolder.isDirectory()) {
            String[] children = saveFolder.list();
            for (int i = 0; i < children.length; i++) {
                new File(saveFolder, children[i]).delete();
            }
            saveFolder.delete();
        }
        saveFolder.mkdirs();
    }

    public boolean processVideoSplitWithWigs(boolean processWigs) {
        File videoFile = new File(VIDEO_DIRECTORY, VIDEO_FILE_NAME);
        Uri videoFileUri = Uri.parse(videoFile.toString());
        if (!videoFile.exists())
            return false;
        MediaPlayer mp = MediaPlayer.create(getBaseContext(), videoFileUri);
        duration = mp.getDuration();
        if (processWigs) {
//            detector = new FaceDetector.Builder(this)
//                    .setTrackingEnabled(false)
//                    .setLandmarkType(FaceDetector.ALL_LANDMARKS)
//                    .build();
            for (long i = 0; i < duration; i += 200) {
                Bitmap bitmap = BitmapFactory.decodeFile(FROM_DIRECTORY + "frame" + i + ".jpg");

                try {
                    if (bitmap != null) {
                        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                        saveFrame(bitmap, i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (bitmap != null) {
                        bitmap.recycle();
                        bitmap = null;
                    }
                }
            }
//            detector.release();
        }
        return duration > 1l;
    }

    Bitmap wigBitmap, wigRight, wigLeft, wigMiddle;
    Paint p;
    Rect srcLeft, dstLeft, srcRight, dstRight, srcMiddle, dstMiddle;
    int w, h;

    private static final int TWO = 2;
    private static final int THREE = 3;
    private int state = TWO;

    public void saveFrame(Bitmap mBitmap, long i) throws Exception {

        if (wigBitmap == null) {
            wigBitmap = BitmapFactory.decodeResource(getResources(), wigResSelected);
            wigBitmap = Bitmap.createScaledBitmap(wigBitmap,
                    (int) (mBitmap.getWidth() * 3f / 4f), (int) (mBitmap.getWidth() * 3f / 4f), false);
            w = wigBitmap.getWidth();
            h = wigBitmap.getHeight();

            if (state == TWO) {
                wigLeft = Bitmap.createBitmap(wigBitmap, 0, 0, w / 2, h);
                wigRight = Bitmap.createBitmap(wigBitmap, w / 2, 0, w / 2, h);
                if (srcLeft == null)
                    srcLeft = new Rect(0, 0, w / 2, h);
                if (srcRight == null)
                    srcRight = new Rect(w / 2, 0, w / 2, h);
            } else if (state == THREE) {
                wigLeft = Bitmap.createBitmap(wigBitmap, 0, 0, w / 3, h);
                wigMiddle = Bitmap.createBitmap(wigBitmap, w / 3, 0, w / 3, h);
                wigRight = Bitmap.createBitmap(wigBitmap, (2 * w) / 3, 0, w / 3, h);
                if (srcLeft == null)
                    srcLeft = new Rect(0, 0, w / 3, h);
                if (srcRight == null)
                    srcRight = new Rect((2 * w) / 3, 0, w / 3, h);
                if (srcMiddle == null)
                    srcMiddle = new Rect(0, w / 3, w / 3, h);
            }
        }
        if (p == null) {
            p = new Paint();
        }

        setupDestRect(i, mBitmap);

        Canvas canvas = new Canvas(mBitmap);
        if (state == TWO) {
            canvas.drawBitmap(wigLeft, null, dstLeft, p);
            canvas.drawBitmap(wigRight, null, dstRight, p);
        } else if (state == THREE) {
            canvas.drawBitmap(wigLeft, null, dstLeft, p);
            canvas.drawBitmap(wigMiddle, null, dstMiddle, p);
            canvas.drawBitmap(wigRight, null, dstRight, p);
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
        File f = new File(saveFolder, ("frame" + i + ".jpg"));
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.flush();
        fo.close();
//        Log.i(TAG, "Wig Frame " + i + " Generated");
    }

    private double RADIAN = Math.PI / 360;
    private double MAX_SIN = Math.sin(90 * RADIAN);

    int angles[] = {0, 30, 70, 80, 90, 80, 50, 25, 0, -30, -70, -80, -90, -80, -70, -30, 0};


    private void setupDestRect(long pos, Bitmap mBitmap) {

        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        int diff = Math.abs(height - width);
        int left = 0/*(int) (minValue * 1f / 32f)*/;
        int top = 0/*(int) (minValue * 1f / 32f)*/;
        int bottom = height - diff;

        // Frame Detection.
//        Frame frame = new Frame.Builder().setBitmap(mBitmap).build();
//        SparseArray<Face> faces = detector.detect(frame);
//        int key = faces.keyAt(0);
//        Face ourFace = faces.get(key);

        if (state == TWO) {
            if (dstLeft == null)
                dstLeft = new Rect();
            dstLeft.setEmpty();
            dstLeft.left =  left;
            dstLeft.top = +top;
            dstLeft.bottom = bottom;

            if (dstRight == null)
                dstRight = new Rect();
            dstRight.setEmpty();
            dstRight.right = width - left;
            dstRight.top = +top;
            dstRight.bottom = bottom;

            // Non-linear Angle Selection
//            int angle = angles[(int) (pos/200)];
            // Linear Angles.
            // 0, 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330, 360, ...
//            double angle = (MAX_SIN - Math.sin(30 * (pos / 200) * RADIAN)) / 2d;
//            float angle = ourFace.getEulerY();
//            Log.i("EulerX,Y at "+ pos/200, ourFace.getEulerZ() + " "+ angle + "");



            double ratio = (MAX_SIN - Math.sin(32 * (pos / 200) * RADIAN)) / 2d;
            Log.i("Sine ratio", "For pos " + pos / 200 + " ratio is " + ratio);
            dstLeft.right = (int) (width * ratio);
            dstRight.left = dstLeft.right;

        } else if (state == THREE) {
            if (dstLeft == null)
                dstLeft = new Rect();
            dstLeft.setEmpty();
            dstLeft.left = 0;
            dstLeft.top = -top;
            dstLeft.bottom = h;

            if (dstMiddle == null)
                dstMiddle = new Rect();
            dstMiddle.setEmpty();
            dstMiddle.top = -top;
            dstMiddle.bottom = h;

            if (dstRight == null)
                dstRight = new Rect();
            dstRight.setEmpty();
            dstRight.right = width;
            dstRight.top = -top;
            dstRight.bottom = h;

            long steps = (pos / 200);
            double stepAngle = 30 * RADIAN;
            Log.i("Angle", "" + stepAngle);
            double angle = steps * stepAngle;
            double cosValue = Math.cos(angle);

            dstLeft.right = (int) (width * (0.5f - cosValue / 6f));
            dstMiddle.left = dstLeft.right;
            dstMiddle.right = (int) (width * (0.5f + cosValue / 6f));
            dstRight.left = dstMiddle.right;
        }
    }

    long cITime = 0;
    HashMap<Long, Bitmap> hashMap = new HashMap<>();

    @Override
    protected void onPause() {
        super.onPause();
        if (hashMap != null) {
            for (long i = 0; i < duration; i += 200) {
                Bitmap b = hashMap.get(i);
                hashMap.remove(i);
                if (b != null && !b.isRecycled()) {
                    b.recycle();
                    b = null;
                }
            }
        }
    }

    @Override
    public void onSwipe(boolean isLeft) {
        long tempValue = cITime;
        Bitmap b = null;
        if (isLeft) {
            tempValue = tempValue - 200;
            long maxValueCur = Math.max(tempValue, 0);
            tempValue = maxValueCur;
        } else {
            tempValue = tempValue + 200;
            long maxIndex = duration / 200;
            long minValueCur = Math.min(tempValue, maxIndex * 200);
            tempValue = minValueCur;
        }
//        Log.i("tempValue", "" + tempValue);
        if (hashMap.containsKey(tempValue) &&
                hashMap.get(tempValue) != null &&
                !hashMap.get(tempValue).isRecycled()) {
            b = hashMap.get(tempValue);
        } else {
            try {
                b = BitmapFactory.decodeFile(saveFolder.getPath() + File.separatorChar + "frame" + tempValue + ".jpg");
            } catch (Exception e) {
                e.printStackTrace();
            }
            hashMap.put(tempValue, b);
        }
        if (b != null)
            iv.setImageBitmap(b);
        cITime = tempValue;
    }
}