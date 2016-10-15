package ui.camera;

import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;

import java.util.List;

/**
 * Created by srikrishna on 15-10-2016.
 */
public class CameraUtils {

    private static int mWidth = 480;
    private static int mHeight = 640;
    private static int mFps = 30;

    public static void setCameraDisplayOrientation(Camera camera, int rotation) {
        if (camera == null)
            return;
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);

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

        Camera.Parameters parameters = camera.getParameters();

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

    public static Camera getCameraInstance() {
        Camera mCamera = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraIndex = 0; cameraIndex < Camera.getNumberOfCameras(); cameraIndex++) {
            Camera.getCameraInfo(cameraIndex, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    mCamera = Camera.open(cameraIndex);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
        return mCamera;
    }

    public static void setCameraParameters(Camera mCamera) {
        if (mCamera == null)
            return;
        boolean closeCamera = false;
        if (mCamera == null) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            closeCamera = true;
        }
        Camera.Parameters mCameraParameters = mCamera.getParameters();

        if (closeCamera) {
            mCamera.release();
            mCamera = null;
        }

        int closestSize[] = findClosestSize(mWidth, mHeight, mCameraParameters);
        mWidth = closestSize[0];
        mHeight = closestSize[1];
        mCameraParameters.setPreviewSize(mWidth, mHeight);

        int closestRange[] = findClosestFpsRange(mFps, mCameraParameters);

        mCameraParameters.setPreviewFpsRange(closestRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                closestRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);

        mCamera.setParameters(mCameraParameters);
    }

    private static int[] findClosestSize(int width, int height, Camera.Parameters parameters) {
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        int closestWidth = -1;
        int closestHeight = -1;
        int smallestWidth = previewSizes.get(0).width;
        int smallestHeight = previewSizes.get(0).height;
        for (Camera.Size size : previewSizes) {
            // Best match defined as not being larger in either dimension than
            // the requested size, but as close as possible. The below isn't a
            // stable selection (reording the size list can give different
            // results), but since this is a fallback nicety, that's acceptable.
            if (size.width <= width &&
                    size.height <= height &&
                    size.width >= closestWidth &&
                    size.height >= closestHeight) {
                closestWidth = size.width;
                closestHeight = size.height;
            }
            if (size.width < smallestWidth &&
                    size.height < smallestHeight) {
                smallestWidth = size.width;
                smallestHeight = size.height;
            }
        }
        if (closestWidth == -1) {
            // Requested size is smaller than any listed size; match with smallest possible
            closestWidth = smallestWidth;
            closestHeight = smallestHeight;
        }

        int[] closestSize = {closestWidth, closestHeight};
        return closestSize;
    }

    private static int[] findClosestFpsRange(int fps, Camera.Parameters params) {
        List<int[]> supportedFpsRanges = params.getSupportedPreviewFpsRange();
        int[] closestRange = supportedFpsRanges.get(0);
        for (int[] range : supportedFpsRanges) {
            if (range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] < fps * 1000 &&
                    range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX] > fps * 1000 &&
                    range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] >
                            closestRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] &&
                    range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX] <
                            closestRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]) {
                closestRange = range;
            }
        }

        return closestRange;
    }

    public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}
