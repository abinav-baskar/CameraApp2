package com.example.cameraapp2;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Context c; //BUB added

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        c = context;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        System.out.println("Test_camera preview has been created");
        DebugMethods.sendToast("Is this called sufficiently?", c);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        CharSequence text = "surface created!";
        Toast toast = Toast.makeText(c, text, Toast.LENGTH_SHORT);
        toast.show();

        ///////byub adding this
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(profile.videoFrameWidth,profile.videoFrameHeight);
        mCamera.setParameters(parameters);

        // The Surface has been created, now tell the camera where to draw the preview.
        System.out.println("test_trying to surface cretae");
        Log.d(TAG, "test_surface created");
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            System.out.println("test_CameraPreview_surfaceCreated");
            Log.d(TAG, "test_Error setting camera preview: " + e.getMessage());
            DebugMethods.sendToast("Error setting camera preview", c);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

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
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }


}
