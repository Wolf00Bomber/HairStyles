package hairstyle.twod.com.hairstyles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import ui.camera.GraphicOverlay;

/**
 * Created by srikrishna on 23/09/16.
 */

public class FacePlaceHolder extends GraphicOverlay.Graphic {

    private Context context;
    private Bitmap initialFace;
    private Paint paint;
    private boolean isPreprocessed;


    public FacePlaceHolder(GraphicOverlay overlay, Context context, int resId) {
        super(overlay);
        initialFace = BitmapFactory.decodeResource(context.getResources(), resId);
        paint = new Paint();
    }

    @Override
    public void draw(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if(!isPreprocessed)
        {
            float scale = 480f/640f;
            initialFace = Bitmap.createScaledBitmap(initialFace,
                    (int)(width * 7f/8f),
                    (int)(width * 7f/8f * 1f/scale),
                    false);
            isPreprocessed = true;
        }
        canvas.drawBitmap(initialFace,
                width * 1f/16f,
                height * 1f/16f,
                paint);
    }
}
