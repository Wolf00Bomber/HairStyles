package hairstyle.twod.com.hairstyles;/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.speech.tts.TextToSpeech;

import com.google.android.gms.vision.face.Face;

import ui.camera.GraphicOverlay;


/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;

    private Context mContext;
    public boolean shouldDrawFaceBox = true;

    FaceGraphic(GraphicOverlay overlay, Context context) {
        super(overlay);
        mContext = context;
        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null)
            return;

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if (shouldDrawFaceBox) {
//            canvas.drawRect(width / 8, height / 8, (int) (width * 7f / 8f), (int) (height * 7f / 8f), mBoxPaint);
            canvas.drawRect(left, top, right, bottom, mBoxPaint);
            canvas.drawRect(width / 8, height / 8f, (int) (width * 7f / 8f), (int) (height * 7f / 8f), mBoxPaint);
            long choosenTime = System.currentTimeMillis();
            if (!App.tSpeech.isSpeaking() && lastTime + 5 * 1000 < choosenTime) {
                lastTime = choosenTime;
                if (!isInSelectedWindow(left, top, right, bottom, canvas)) {
                    App.tSpeech.speak("Please align your Face in the Center Box.", TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    App.tSpeech.speak("Good, You are in the Center Box.", TextToSpeech.QUEUE_FLUSH, null);
                    shouldDrawFaceBox = false;
                    if (mContext instanceof DemoActivity) {
                        ((DemoActivity) mContext).switchToVideoRecording();
                    }
                }
            }
        }
    }

    long lastTime = 0l;

    private boolean isInSelectedWindow(float left, float top, float right, float bottom, Canvas canvas) {
        boolean isInWindow;
//        int threshold = (canvas.getWidth()/16 + canvas.getHeight()/16)/2;
//        boolean isLeftOk = Math.abs((int)left - canvas.getWidth()/8) < threshold ;
//        boolean isRightOk = Math.abs((int)right - canvas.getWidth()*7f/8f) < threshold ;
//        boolean isTopOk = Math.abs((int)top - canvas.getHeight()/8) < threshold ;
//        boolean isBottomOk = Math.abs((int)bottom - canvas.getHeight()*7f/8f) < threshold ;
//        isInWindow = isBottomOk && isLeftOk && isRightOk && isTopOk;

        // width/8, height/8, (int)(width * 7f/8f), (int)(height * 7f/8f
        // L > 0 && L < width/8
        // R > 7f/8f * width && R < width
        // T > 0 && T < height/8
        // B > 7f*8f * height && B < height

        int wThreshold = canvas.getWidth() / 8;
        int hThreshold = canvas.getHeight() / 8;
        boolean isLeftOk = left > 0 && left < wThreshold;
        boolean isRightOk = right > wThreshold * 7 && right < wThreshold * 8;
        boolean isTopOk = top > 0 && top < hThreshold;
        boolean isBottomOk = bottom > 7 * hThreshold && bottom < 8 * hThreshold;
        isInWindow = isBottomOk && isLeftOk && isRightOk && isTopOk;

        return isInWindow;
    }
}