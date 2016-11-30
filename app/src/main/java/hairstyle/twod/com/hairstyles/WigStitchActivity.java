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
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wig_stitch_layout);
        iv = (ImageView) findViewById(R.id.iv);
        pBar = (ProgressBar) findViewById(R.id.pBar);
        iv.setOnTouchListener(new CustomImageSeekerListener(this));
        if (getIntent().hasExtra("wigResId")) {
            wigResSelected = getIntent().getIntExtra("wigResId", wigResSelected);
        }
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
        }
        return duration > 1l;
    }

    Bitmap wigBitmap, wigRight, wigLeft;
    Paint p;
    Rect srcLeft, dstLeft, srcRight, dstRight;
    int w, h;

    public void saveFrame(Bitmap mBitmap, long i) throws Exception {

        if (wigBitmap == null) {
            wigBitmap = BitmapFactory.decodeResource(getResources(), wigResSelected);
            wigBitmap = Bitmap.createScaledBitmap(wigBitmap,
                    (int) (mBitmap.getWidth() * 3f / 4f), (int) (mBitmap.getWidth() * 3f / 4f), false);
            w = wigBitmap.getWidth();
            h = wigBitmap.getHeight();

            wigLeft = Bitmap.createBitmap(wigBitmap, 0, 0, w / 2, h);
            wigRight = Bitmap.createBitmap(wigBitmap, w / 2, 0, w / 2, h);
            if (srcLeft == null)
                srcLeft = new Rect(0, 0, w / 2, h);
            if (srcRight == null)
                srcRight = new Rect(w / 2, 0, w / 2, h);

        }
        if (p == null) {
            p = new Paint();
        }


        setupDestRect(i, mBitmap);

        Canvas canvas = new Canvas(mBitmap);
        canvas.drawBitmap(wigLeft, null, dstLeft, p);
        canvas.drawBitmap(wigRight, null, dstRight, p);

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

    private double RADIAN = Math.PI/360;
    private double MAX_SIN = Math.sin(90 * RADIAN);

    private void setupDestRect(long pos, Bitmap mBitmap) {

        int width = mBitmap.getWidth();
        int left = (int) (width * 1f / 16f);
        int top = (int) (width * 1f / 8f);


        if (dstLeft == null)
            dstLeft = new Rect();
        dstLeft.setEmpty();
        dstLeft.left = 0;
        dstLeft.top = - top;
        dstLeft.bottom = h;

        if (dstRight == null)
            dstRight = new Rect();
        dstRight.setEmpty();
        dstRight.right = width;
        dstRight.top = - top;
        dstRight.bottom = h;

        double ratio = (MAX_SIN - Math.sin(30 * (pos/200)  * RADIAN)) / 2d;
        Log.i("Sine ratio", "For pos "+ pos/200 +" ratio is "+ ratio);
//        dstLeft.right = width + (int) ((width - 2 * left) * ratio);
        dstLeft.right = (int) (width * ratio);

        dstRight.left = dstLeft.right;

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
        Log.i("tempValue", "" + tempValue);
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
