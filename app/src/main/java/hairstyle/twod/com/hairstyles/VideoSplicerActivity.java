package hairstyle.twod.com.hairstyles;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

import wseemann.media.FFmpegMediaMetadataRetriever;

/**
 * Created by srikrishna on 04-10-2016.
 */
public class VideoSplicerActivity extends AppCompatActivity implements ImageSeekerInterface {

    private static final String TAG = VideoSplicerActivity.class.getSimpleName();
    File saveFolder;
    String DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/cameraTest/";
    String VIDEO_FILE_NAME = "FaceMap.mp4";
    private final String VIDEO_PATH_NAME = DIRECTORY + VIDEO_FILE_NAME;

    Button btnVideoSplits, btnViewVideo, btnApplyWig;
    ImageView ivWigOne, ivWigTwo;
    ProgressBar pBar;
    ImageView ivVideoSeeker;
    Thread th;
    Runnable r = new Runnable() {
        @Override
        public void run() {
            setUpFolders();
            processVideoSplit(true);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnVideoSplits.setEnabled(true);
                    pBar.setVisibility(View.INVISIBLE);
                    ivVideoSeeker.setVisibility(View.VISIBLE);
                    ivVideoSeeker.setOnTouchListener(new CustomImageSeekerListener(VideoSplicerActivity.this));
                }
            });
        }
    };
    private int wigResSelected = -1;
    private View.OnClickListener ivListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.ivWigOne) {
                ivWigOne.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                ivWigTwo.setBackgroundColor(Color.TRANSPARENT);
                wigResSelected = R.drawable.short_hairstyles_001_wig;
            } else if (v.getId() == R.id.ivWigTwo) {
                ivWigOne.setBackgroundColor(Color.TRANSPARENT);
                ivWigTwo.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                wigResSelected = R.drawable.cool_mens_curly_hairstyles_wig;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_splicer);
        btnVideoSplits = (Button) findViewById(R.id.btnVideoSplits);
        btnViewVideo = (Button) findViewById(R.id.btnViewVideo);
        btnApplyWig = (Button) findViewById(R.id.btnApplyWig);
        pBar = (ProgressBar) findViewById(R.id.pBar);
        ivVideoSeeker = (ImageView) findViewById(R.id.ivVideoSeeker);
        ivWigOne = (ImageView) findViewById(R.id.ivWigOne);
        ivWigTwo = (ImageView) findViewById(R.id.ivWigTwo);
        ivWigOne.setOnClickListener(ivListener);
        ivWigTwo.setOnClickListener(ivListener);
        btnApplyWig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (processVideoSplit(false)) {
                    if (wigResSelected == -1) {
                        Toast.makeText(VideoSplicerActivity.this, "Please select a Wig!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent i = new Intent(VideoSplicerActivity.this, WigStitchActivity.class);
                    i.putExtra("wigResId", wigResSelected);
                    startActivity(i);
                } else {
                    Toast.makeText(VideoSplicerActivity.this, "Please record a Selfie Video!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        btnVideoSplits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                pBar.setVisibility(View.VISIBLE);
                ivVideoSeeker.setVisibility(View.INVISIBLE);
                ivVideoSeeker.setOnTouchListener(null);
                th = new Thread(r);
                th.start();
            }
        });
        btnViewVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (processVideoSplit(false)) {
            ivVideoSeeker.setOnTouchListener(new CustomImageSeekerListener(VideoSplicerActivity.this));
            onSwipe(true);
        }
    }

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

    public void setUpFolders() {
        saveFolder = new File(DIRECTORY + "VideoSlices/");
        if (saveFolder.exists() && saveFolder.isDirectory()) {
            String[] children = saveFolder.list();
            for (int i = 0; i < children.length; i++) {
                new File(saveFolder, children[i]).delete();
            }
            saveFolder.delete();
        }
        saveFolder.mkdirs();
    }

    public boolean processVideoSplit(boolean processVideo) {
        File videoFile = new File(DIRECTORY, VIDEO_FILE_NAME);
        Uri videoFileUri = Uri.parse(videoFile.toString());
        if (!videoFile.exists() || videoFile.length() == 0)
            return false;
        MediaPlayer mp = MediaPlayer.create(getBaseContext(), videoFileUri);
        duration = mp.getDuration();
        if (processVideo) {
            FFmpegMediaMetadataRetriever retriever = new FFmpegMediaMetadataRetriever();
            retriever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            retriever.setDataSource(videoFile.getAbsolutePath());
            for (long i = 0; i < duration; i += 200) {
                Bitmap bitmap = retriever.getFrameAtTime(i * 1000, FFmpegMediaMetadataRetriever.OPTION_CLOSEST);
                try {
                    if (bitmap != null)
                        saveFrame(bitmap, saveFolder, i);
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

    public void saveFrame(Bitmap mBitmap, File saveFolder, long i) throws Exception {

        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        Bitmap resizedBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        File f = new File(saveFolder, ("frame" + i + ".jpg"));
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.flush();
        fo.close();
        Log.i(TAG, "Frame " + i + " Generated");
        if (resizedBitmap != null) {
            resizedBitmap.recycle();
            resizedBitmap = null;
        }
    }

    long cITime = 0;
    HashMap<Long, Bitmap> hashMap = new HashMap<>();
    private long duration;

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
        if (hashMap.containsKey(tempValue) && hashMap.get(tempValue) != null && !hashMap.get(tempValue).isRecycled()) {
            b = hashMap.get(tempValue);
        } else {
            try {
                b = BitmapFactory.decodeFile(DIRECTORY + "VideoSlices/" + "frame" + tempValue + ".jpg");
            } catch (Exception e) {
                e.printStackTrace();
            }
            hashMap.put(tempValue, b);
        }
        if (b != null)
            ivVideoSeeker.setImageBitmap(b);
        cITime = tempValue;
    }

    private void playVideo() {
        Uri uri = Uri.parse(VIDEO_PATH_NAME);
        Intent startVideo = new Intent(Intent.ACTION_VIEW);
        startVideo.setDataAndType(uri, "video/*");
        startActivity(startVideo);
    }
}