package com.bondzu.bondzuapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import com.bondzu.bondzuapp.R;
import com.bondzu.bondzuapp.view.CameraPreview;


public class NativeCamera extends Activity {

    // Native camera.
    private Camera mCamera;
    // View to display the camera output.
    private CameraPreview mPreview;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_camera);
    }

    @Override
    public void onResume() {
        super.onResume();
        releaseCameraAndPreview();
            // Create our Preview view and set it as the content of our activity.
            boolean opened = safeCameraOpenInView();

            if(!opened){
                Log.d("CameraGuide", "Error, Camera failed to open");
            }
            mPreview.mCamera.setPreviewCallback(multiple);



    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        releaseCameraAndPreview();
        // do nothing.
        Intent i = new Intent(NativeCamera.this, Home.class);
        startActivity(i);
        finish();
    }


    /**Multiple photos**/
    Camera.PreviewCallback multiple = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(final byte[] data, final Camera camera) {
            new Thread(new Runnable() {
                public void run() {
                    camera.setDisplayOrientation(90);
                }
            }).start();
        }
    };


    @Override
    public void onPause() {
        super.onPause();
        releaseCameraAndPreview();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseCameraAndPreview();
    }

    /**
     * Recommended "safe" way to open the camera.
     * @return
     */
    private boolean safeCameraOpenInView() {
        boolean qOpened = false;
        releaseCameraAndPreview();
        mCamera = getCameraInstance();
        qOpened = (mCamera != null);

        if(qOpened){
            mPreview = new CameraPreview(this, mCamera);
            FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
            preview.addView(mPreview);
            mPreview.startCameraPreview();
        }
        return qOpened;
    }

    // Retrieve an instance of the Camera object.
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            // get a Camera instance
            c = Camera.open(1);
        } catch (Exception e) {
            Log.d("Error", "Camera not available or in use.");
        }
        // return NULL if camera is unavailable, otherwise return the Camera
        // instance
        return c;
    }

    /**
     * Clear any existing preview / camera.
     */
    private void releaseCameraAndPreview() {

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        if(mPreview != null){
            mPreview.destroyDrawingCache();
            mPreview.mCamera = null;
        }
    }

}
