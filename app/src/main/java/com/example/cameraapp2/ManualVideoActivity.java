package com.example.cameraapp2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;

import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Debug;
import android.os.Handler;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import java.io.File;

import java.io.IOException;
import java.util.List;

public class ManualVideoActivity extends AppCompatActivity {
    private ManualVideoActivity manualVideoActivity;
    private Camera vCamera;
    private Camera secondCamera;
    private CameraPreview vPreview;
    private MediaRecorder vMediaRecorder;
    private MediaRecorder secondMediaRecorder;

    private boolean recordingVideo = false;
    private Button captureButton;
    private Button flipButton;
    private int camcorderQuality = CamcorderProfile.QUALITY_720P; //later can add a settings tab
    private int numberOfCameras;
    private int cameraUsed = 1;
    private int numberOfTimesPressed;
   // private  Context mvContext;
    TrimVideo trimV;
    AppendVideo appendV;
    Camera.Size myVideoSize;

    private Handler handler = new Handler();
    private Runnable runnable;
    int delay = 30*1000;
    int savedLength = 10*1000;
    int recordLength = 10*1000;
    long startTime;
    int elapsedTimeMillis;
    private File tempOutputFile;

     static boolean ffmpegDone = false;

    //ok so as of now we are using both the handler and nanoTime together; don't think that's ideal tbh
    //ok instead we shall use a simple AsyncTask and shtuff. Nope Handler is by far the best
    //TODO: also just saying those 30 second save cycles should reset upon the user pressing the save media button


   File file1, file2;
   boolean firstOfRecordings = true;

    private final Object mCameraLock = new Object();
    private final Context mvaContext = this;
    int numberOfTimeAutoStartHasBeenCalled = 0;
    ConstraintLayout preview;


    public ManualVideoActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FFmpeg ffmpeg;
         trimV = new TrimVideo();
         appendV = new AppendVideo();
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
    //    handler.postAtTime(runnable, System.currentTimeMillis()+interval);
      //  handler.postDelayed(runnable, interval);

       //deprecated since api 23. for more recent For recent Android, just substitute in the adjustStreamVolume() call with ADJUST_MUTE

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

        preview = (ConstraintLayout) findViewById(R.id.video_preview);
        preview.addView(vPreview); //ok both Camera.setPreviewDisplay and Camera.startPreview() have occurred
        Camera.Parameters params = vCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
        myVideoSize = previewSizes.get(0);
        params.setPreviewSize(myVideoSize.width, myVideoSize.height);
        vCamera.setParameters(params);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void onCapture(View v) throws IOException, InterruptedException {
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
       // vCamera = getCameraInstance(cameraUsed);
      //  vCamera.setDisplayOrientation(getDeviceOrientation(this)); //rechent
        startTime = System.nanoTime();
        vMediaRecorder = new MediaRecorder(); //only need to make a new mediaRecorder if release() is called
        vMediaRecorder.setOrientationHint(getDeviceOrientation(this));
        //vCamera.unlock();                                                                                           //tuesday its now here

        vMediaRecorder.setCamera(vCamera);
        vMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        vMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        vMediaRecorder.setProfile(CamcorderProfile.get(camcorderQuality));

        file1 = SaveMedia.getOutputMediaFile(SaveMedia.MEDIA_TYPE_VIDEO, this);
        vMediaRecorder.setOutputFile(file1.toString());

        vMediaRecorder.setPreviewDisplay(vPreview.getHolder().getSurface());
        DebugMethods.sendToast( ("Initial "+ (System.nanoTime() - startTime)) , this );
        startTime = System.nanoTime();
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

        vCamera.unlock();
        vMediaRecorder.start();


        recordingVideo = true;
        startTime = System.nanoTime();

        handler.postDelayed( runnable = new Runnable() {
            public void run() {
                startTime = System.nanoTime();
                handler.postDelayed(runnable, recordLength);
                autoStopVideo();
                autoStartVideo(); //i dont think this makes sense; this isn't really threading is it...?

            }
        }, recordLength);
        return true;
    }

    public boolean autoStartVideo() {

        vMediaRecorder = new MediaRecorder(); //only need to make a new mediaRecorder if release() is called
        vMediaRecorder.setOrientationHint(getDeviceOrientation(this)); //in short, you can't change any of this stuff unless media recorder is in 'prepare mode' (or maybe the mode before)
     //   vCamera.unlock();                                                                                           //tuesday its now here

//Use myVideoSize from above
        //vMediaRecorder.setVideoSize(myVideoSize.width, myVideoSize.height);
        vMediaRecorder.setCamera(vCamera);
        vMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        vMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        vMediaRecorder.setProfile(CamcorderProfile.get(camcorderQuality));
        file2 = SaveMedia.getOutputMediaFile(SaveMedia.MEDIA_TYPE_VIDEO, this);
        vMediaRecorder.setOutputFile(file2.toString());                 //TODO: this was file1 but im changing to file 2
        vMediaRecorder.setPreviewDisplay(vPreview.getHolder().getSurface());
        vCamera.unlock();
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
        numberOfTimeAutoStartHasBeenCalled++;
        return true;
    }

    public void autoStopVideo() {
        if (recordingVideo) {
            if(firstOfRecordings) {
                firstOfRecordings = false;
            }
            else {
                if(!file1.delete()) { //delete file1
                    DebugMethods.sendToast("File1 wasn't deleted ", this);
                }
               file1 = file2;  //make file1 be the contents of file2
                //file 2 will be redefined shortly
            }
            vMediaRecorder.reset();
            releaseMediaRecorder();

        } else {
            DebugMethods.sendToast("stopVideo: Video never started", this);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void stopVideo() throws IOException, InterruptedException { //TODO: What I realised is if they press stop video then they clearly don't care about its contents; no needio to savio. But as of now we will use stopVideo to test keeping just 30 seconds


        if (recordingVideo) {

            elapsedTimeMillis = (int) ((System.nanoTime() - startTime)/1000000); //Max value of nano is 60*1000,000,000/1000,000 = 60,000 miliseconds which is fine
            int startTime = recordLength-1000-savedLength+elapsedTimeMillis; //-1000 because each video is a good second shorter
            int endTime = recordLength-1000;
            vMediaRecorder.setOnErrorListener(null);
            vMediaRecorder.setOnInfoListener(null);
            vMediaRecorder.setPreviewDisplay(null);
            try {
                vMediaRecorder.stop();
            } catch (RuntimeException e) {
                DebugMethods.sendToast("stopVideo: RuntimeException", this);
            }

            handler.removeCallbacks(runnable); //stop handler when activity not visible. As of now is before saving the media file.

             tempOutputFile = trimV.createTempStorageDirectory();

            trimV.trimTheVideo(file1.getAbsolutePath(), tempOutputFile.getAbsolutePath(), startTime, endTime, this);
            //synchronized (this) {
                Thread waitForTrim = new Thread(stopVideoRunnable);
                waitForTrim.start();
           //     DebugMethods.sendToast("postrim", this);
          //  }




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

    public static void setFfmpegDone(boolean b) {
        ffmpegDone = b;

    }

    Runnable stopVideoRunnable = new Runnable() {
        @Override
        public void run() {
         //   DebugMethods.sendToast("thread started", mvaContext);
            while(!ffmpegDone) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //A second runnable is only needed if you want to affect UI
            }
           // DebugMethods.sendToast("Thread broken", mvaContext);

            file2 = SaveMedia.getOutputMediaFile(2,mvaContext); //so file2 is the most recent recording
            File finalFile = SaveMedia.getOutputMediaFile(2, mvaContext);
            ffmpegDone = false;
            appendV.appendTheVideo(tempOutputFile.getAbsolutePath(), file2.getAbsolutePath(), finalFile.getAbsolutePath(), mvaContext );

            while(!ffmpegDone) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Uri uri = SaveMedia.getOutputMediaFileUri(finalFile,2, mvaContext);
            SaveMedia.galleryAddPic(uri, mvaContext);

            firstOfRecordings = true;
            releaseMediaRecorder(); //so everything at the end of stopVideo is here
            System.out.println("We done with the mediarecorder");

            return; //I think this wouldn't be needed; breaking from the loop should end the thread anyway
        }
    };

//https://stackoverflow.com/questions/16827444/accessing-the-output-video-while-recording
    //https://stackoverflow.com/questions/33081495/how-to-change-the-output-file-of-a-mediarecorder-without-stopping-the-mediarecor very useful
        /*
        Sadly I don't think that's possible. What might be possible however, is to have two mediaRecorder instances. You'll get into a slight problem where
        you can't set them both to the Camera source at the same time, but you could do all of the rest of the configuration on your 2nd instance while recording on
        the first one, then as soon as you've released the first one, assign the Camera source to the 2nd and start recording, then just ping-pong between the
        two mediaRecorders – Matt Taylor Oct 20 '15 at 7:25
         */

}

