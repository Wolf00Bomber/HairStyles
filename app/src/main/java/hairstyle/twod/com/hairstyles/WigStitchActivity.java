package hairstyle.twod.com.hairstyles;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
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

    Bitmap wigBitmap;
    Paint p;

    public void saveFrame(Bitmap mBitmap, long i) throws Exception {

        if (wigBitmap == null) {
            wigBitmap = BitmapFactory.decodeResource(getResources(), wigResSelected);
            wigBitmap = Bitmap.createScaledBitmap(wigBitmap,
                    (int) (mBitmap.getWidth() * 3f / 4f), (int) (mBitmap.getWidth() * 3f / 4f), false);
        }
        if (p == null) {
            p = new Paint();
        }
        Matrix matrix = new Matrix();
        setScale(matrix, i);
        Bitmap wigBitmapRotated = Bitmap.createBitmap(wigBitmap, 0, 0, wigBitmap.getWidth(), wigBitmap.getHeight(), matrix, true);

        Canvas canvas = new Canvas(mBitmap);
        canvas.drawBitmap(wigBitmapRotated, (int) (mBitmap.getWidth() * 1f / 8f), (int) (mBitmap.getWidth() * 1f / 8f), p);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
        File f = new File(saveFolder, ("frame" + i + ".jpg"));
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.flush();
        fo.close();
        Log.i(TAG, "Wig Frame " + i + " Generated");
        if (wigBitmapRotated != null) {
            wigBitmapRotated.recycle();
            wigBitmapRotated = null;
        }
    }

    private void setScale(Matrix matrix, long pos) {
        int steps = duration / 4 + 1;
        int quarter = steps / 4;
        double scaleX = 1f;
        if (pos >= 0 && pos <= quarter) {
            scaleX = Math.cos(10 * pos);
        } else if (pos > quarter && pos <= quarter * 2) {
            scaleX = Math.cos(10 * (quarter - (pos - quarter)));
        } else if (pos > quarter * 2 && pos <= quarter * 3) {
            scaleX = Math.cos(-10 * (pos - 2 * quarter));
        } else if (pos > quarter * 3 && pos <= steps) {
            scaleX = Math.cos(-10 * (steps - pos));
        }
        matrix.setScale((float) scaleX, 1);

    }

    long cITime = 0;
    HashMap<Long, Bitmap> hashMap = new HashMap<>();

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
        if (hashMap.containsKey(tempValue)) {
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
