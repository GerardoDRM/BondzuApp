package com.bondzu.bondzuapp.view;

/**
 * Created by gerardo on 17/05/15.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.List;

@SuppressLint("ViewConstructor")
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";
    private SurfaceHolder mHolder;
    public Camera mCamera;

    // Camera Sizing (For rotation, orientation changes)
    private Camera.Size mPreviewSize;

    // List of supported preview sizes
    private List<Camera.Size> mSupportedPreviewSizes;

    // View holding this camera.
    private View mCameraView;
    private int mDesiredCameraPreviewWidth = 1024;

    @SuppressWarnings("deprecation")
    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    /**
     * Begin the preview of the camera input.
     */
    public void startCameraPreview()
    {
        try{
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    /**
     * React to surface changed events
     * @param holder
     * @param format
     * @param w
     * @param h
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        try {
            initializeCameraParameters();

            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    // configure camera parameters like preview size
    private void initializeCameraParameters() {
        Camera.Parameters parameters = mCamera.getParameters();
        // Get a list of supported preview sizes.
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        int currentWidth = 0;
        int currentHeight = 0;
        boolean foundDesiredWidth = false;
        for(Camera.Size s: sizes)
        {
            if (s.width == mDesiredCameraPreviewWidth)
            {
                currentWidth = s.width;
                currentHeight = s.height;
                foundDesiredWidth = true;
                break;
            }
        }
        if(foundDesiredWidth) {
            mCamera.setDisplayOrientation(90);
            parameters.setPreviewSize( currentWidth, currentHeight);
            // Set the auto-focus mode to "continuous"
            //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

            // Preview size must exist.
            if(mPreviewSize != null) {
                Camera.Size previewSize = mPreviewSize;
                parameters.setPreviewSize(previewSize.width, previewSize.height);
            }
        }
        mCamera.setParameters(parameters);
    }

    /**
     * Calculate the measurements of the layout
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        // Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null){
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }

        Log.d("AQUI", mPreviewSize + "");
    }

    /**
     *
     * @param sizes
     * @param width
     * @param height
     * @return
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height){
        // Source: http://stackoverflow.com/questions/7942378/android-camera-will-not-work-startpreview-fails
        Camera.Size optimalSize = null;

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) height / width;

        // Try to find a size match which suits the whole screen minus the menu on the left.
        for (Camera.Size size : sizes){

            if (size.height != width) continue;
            double ratio = (double) size.width / size.height;
            if (ratio <= targetRatio + ASPECT_TOLERANCE && ratio >= targetRatio - ASPECT_TOLERANCE){
                optimalSize = size;

            }
        }

        // If we cannot find the one that matches the aspect ratio, ignore the requirement.
        if (optimalSize == null) {
            // TODO : Backup in case we don't get a size.
        }
        Log.d("AQUI", optimalSize + "");
        return optimalSize;
    }

}
