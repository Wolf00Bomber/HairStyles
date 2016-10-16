package ui.camera;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.HashMap;

import hairstyle.twod.com.hairstyles.CustomAnimationDrawable;

/**
 * Created by srikrishna on 16-10-2016.
 */
public class TweenedAnimationUtils {

    private static HashMap<Integer, Drawable> hmImages;
    private static final int MIN_DURATION = 20;

    public static CustomAnimationDrawable getAnimation(Context context, CustomAnimationDrawable.IAnimationFinishListener listener) {
        if (hmImages == null)
            hmImages = new HashMap<>();
        int minValue = 80;
        int maxValue = 138;
        CustomAnimationDrawable animation = new CustomAnimationDrawable();
        animation.setAnimationFinishListener(listener);
        animation.setOneShot(true);
        Resources res = context.getResources();
        int defGlobeId = res.getIdentifier("out_" + String.format("%04d", 79), "drawable", context.getPackageName());
        animation.addFrame(res.getDrawable(defGlobeId), 40);
        setUpAnimations(minValue, maxValue, res, animation, false, context);
        setUpAnimations(maxValue, minValue, res, animation, false, context);
        setUpAnimations(minValue, maxValue, res, animation, true, context);
        setUpAnimations(maxValue, minValue, res, animation, true, context);
        animation.addFrame(res.getDrawable(defGlobeId), 40);
        return animation;
    }

    private static void setUpAnimations(int from, int to, Resources res, AnimationDrawable animation, boolean fetchFromReflect, Context context) {
        boolean isIncreasing = from < to;
        while (isIncreasing ? from <= to : from >= to) {
            String name = "out_" + String.format("%04d", from);
            int globeId = res.getIdentifier(name, "drawable", context.getPackageName());
            Drawable rotateDrawable = fetchFromReflect ?
                    getReflectedDrawable(globeId, context) :
                    getNormalDrawable(globeId, context);
            animation.addFrame(rotateDrawable, MIN_DURATION);
            if (isIncreasing) from++;
            else from--;
        }
    }

    public static Drawable getNormalDrawable(int resId, Context context) {
        Drawable d = context.getResources().getDrawable(resId);
        return d;
    }

    public static Drawable getReflectedDrawable(int resId, Context context) {
        if (hmImages.containsKey(resId)) {
            return hmImages.get(resId);
        }
        Bitmap originalImage = BitmapFactory.decodeResource(context.getResources(),
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
}
