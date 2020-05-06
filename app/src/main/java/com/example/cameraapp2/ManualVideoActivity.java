package com.example.cameraapp2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.CamcorderProfile;

import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import java.io.File;

import java.io.IOException;

public class ManualVideoActivity extends AppCompatActivity {
    private ManualVideoActivity manualVideoActivity;
    private Camera vCamera;
    private CameraPreview vPreview;
    private MediaRecorder vMediaRecorder;

    private boolean recordingVideo = false;
    private Button captureButton;
    private Button flipButton;
    private int camcorderQuality = CamcorderProfile.QUALITY_720P; //later can add a settings tab
    private int numberOfCameras;
    private int cameraUsed = 1;
    private int numberOfTimesPressed;
    private Context mvContext = this;
    TrimVideo trimV;

    private final int interval = 7000; // 1 Second
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable(){
        public void run() {

        }
    };


    private File mMediaRecorderOutputFile;
    private File videoFile;

    private final Object mCameraLock = new Object();
    private final Context mvaContext = this;
    ConstraintLayout preview;


    public ManualVideoActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FFmpeg ffmpeg;
         trimV = new TrimVideo();

        handler.postAtTime(runnable, System.currentTimeMillis()+interval);
        handler.postDelayed(runnable, interval);

        numberOfCameras = Camera.getNumberOfCameras();
        setUpPreview();

        vCamera.startPreview();
    }

    public void setUpPreview() {
        setContentView(R.layout.activity_manual_video);

        captureButton = findViewById(R.id.button_video_capture);
        flipButton = findViewById(R.id.flipCameraButton);

        vCamera = getCameraInstance(cameraUsed);
        vCamera.setDisplayOrientation(getDeviceOrientation(this)); //we need this to give preview before video starts

        vPreview = new CameraPreview(this, vCamera);
        //  FrameLayout preview = (FrameLayout) findViewById(R.id.video_preview); //must be referencing that bit in activity_manual_photo.xml

        preview = (ConstraintLayout) findViewById(R.id.video_preview);
        preview.addView(vPreview); //ok both Camera.setPreviewDisplay and Camera.startPreview() have occurred
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCapture(View v) throws IOException {
        numberOfTimesPressed += 1;
        if (!recordingVideo) {
            recordingVideo = startVideo();
            captureButton.setText(getString(R.string.manual_button_stopCapture));
            flipButton.setBackgroundColor(Color.parseColor("#ABA1A1"));
            flipButton.setTextColor(Color.parseColor("#B52929"));
        } else {
            stopVideo();
            recordingVideo = false;
            captureButton.setText(getString(R.string.manual_button_startCapture));
        }
    }

    public void onFlip(View v) {
        if(!recordingVideo) {
            if (cameraUsed != numberOfCameras - 1) {
                cameraUsed++;
            } else {
                cameraUsed = 0;
            }
            CharSequence text = "camera used is " + cameraUsed;
            DebugMethods.sendToast(text, this);
            // vCamera.stopPreview(); //seems to be not needed
            releaseCamera(); //see if these two are necessarily needed to be there - this may have to be there actually
            setUpPreview();
        }
        //TODO: Relaunch camera and dat. Also make it such that you can't press onFlip when video is recording
    }

    public static Camera getCameraInstance(int cam) {
        Camera c = null;
        try {
            c = Camera.open(cam);
        } catch (Exception e) {
            // Camera is not available (in use or doesn't exist)
        }
        return c;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean startVideo() throws IOException {
        vCamera = getCameraInstance(cameraUsed);
        vCamera.setDisplayOrientation(getDeviceOrientation(this)); //rechent
        vMediaRecorder = new MediaRecorder(); //only need to make a new mediaRecorder if release() is called
        vMediaRecorder.setOrientationHint(getDeviceOrientation(this));
        vCamera.unlock();                                                                                           //tuesday its now here
        vMediaRecorder.setCamera(vCamera);
        vMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        vMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        vMediaRecorder.setProfile(CamcorderProfile.get(camcorderQuality));
        vMediaRecorder.setOutputFile(SaveMedia.getOutputMediaFile(SaveMedia.MEDIA_TYPE_VIDEO, this).toString());                    //tuesday add (obv the 'this' wasn't there before)
        vMediaRecorder.setPreviewDisplay(vPreview.getHolder().getSurface());

        try {
            vMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            DebugMethods.sendToast("startVideo: IllegalStateExceptio", this);
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            DebugMethods.sendToast("startVideo: IOException", this);
            releaseMediaRecorder();
            return false;
        }
        vMediaRecorder.start();
        recordingVideo = true;
        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void stopVideo() throws IOException {

        if (recordingVideo) {
            vMediaRecorder.setOnErrorListener(null);
            vMediaRecorder.setOnInfoListener(null);
            vMediaRecorder.setPreviewDisplay(null);
            try {
                vMediaRecorder.stop();
            } catch (RuntimeException e) {
                DebugMethods.sendToast("stopVideo: RuntimeException", this);
            }
            Uri uri = SaveMedia.getOutputMediaFileUri(2, this);
           // Intent openTrimVideo = new Intent(this, TrimVideo.class);
         //   openTrimVideo.putExtra("uri", uri.toString());
         //   startActivity(openTrimVideo);

            SaveMedia.galleryAddPic(uri, this);

            releaseMediaRecorder();
        } else {
            DebugMethods.sendToast("stopVideo: Video never started", this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        vCamera.stopPreview(); //Bub added
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder() {
        if (vMediaRecorder != null) {
            //Optional //vMediaRecorder.reset(); // clear recorder configuration
            vMediaRecorder.release(); // release the recorder object
            vMediaRecorder = null;
            vCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (vCamera != null) {
            vCamera.release();        // release the camera for other applications
            vCamera = null;
        }
    }

    public static int getDeviceOrientation(Context context) {
        int degrees = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 90; //was 90 // was 0
                break;
            case Surface.ROTATION_90:
                degrees = 0; //was 0 //was 90
                break;
            case Surface.ROTATION_180:
                degrees = 270; //was 270 //was 180
                break;
            case Surface.ROTATION_270:
                degrees = 180; //was 180 //was 270
                break;
        }

        return degrees;
    }
}

