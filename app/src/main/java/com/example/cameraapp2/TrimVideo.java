package com.example.cameraapp2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;

import javax.security.auth.callback.Callback;

public class TrimVideo extends AppCompatActivity {
    Uri uri;
    FFmpeg ffmpeg;
    String[] commands;
    Callback activity;
    static String filePath = null;
    static String outputFilePath = null;
    public Context trimVideoContext = this;
    TextView onSuccessText = null;
    File mediaStorageDir = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim_video);
       // ffmpeg = FFmpeg.getInstance(this); //ok lets just make a video and hardcode the file path
    //    loadFFMpegBinary(); ok this wasn't getting called it seems
    createStorageDirectory();
    File trimmedVid = new File(mediaStorageDir.getPath() + File.separator +
            "panyangarra.mp4");

       filePath = "/storage/emulated/0/Pictures/MyCameraApp/VID_20200416_162058.mp4";
        //outputFilePath = "/storage/emulated/0/Pictures/MyCameraApp/VID_20200416_144839.mp4";
        outputFilePath = trimmedVid.getAbsolutePath();

        trimTheVideo(this);


        onSuccessText = findViewById(R.id.errorMessage);
        onSuccessText.setMovementMethod(new ScrollingMovementMethod());

       // String[] complexCommand = {"-ss", "" + startMs / 1000, "-y", "-i", inputFileAbsolutePath, "-t", "" + (endMs - startMs) / 1000, "-s", "320x240", "-r", "15", "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", outputFileAbsolutePath};

    }

    public static void setFilePath(String path) {
        filePath = path;
    }
    public void createStorageDirectory() {
       mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraApp2"); //the other one was myCameraApp, remember?
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                DebugMethods.sendToast("Not cannot make directory", this); //later we should make the thing to put it in gallery
            }
        }
    }

    public void trimTheVideo(Context c) {
        loadFFMpegBinary(c);
        int startMs = 0;
        int endMs = 2100;
        if(filePath != null) {
           String[] complexCommand = { "-y", "-i", filePath,  "-ss",  ""+ startMs / 1000, "-t",  ""+ (endMs-startMs) / 1000, "-c", "copy", outputFilePath};
           execFFmpegBinary(complexCommand);
        }
        else {
            DebugMethods.sendToast("File path is null, ", c);
        }
    }



    private void loadFFMpegBinary(Context c) {
            if (ffmpeg == null) {
                ffmpeg = FFmpeg.getInstance(c);
            }

            try {
                ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    }
                });
            } catch (FFmpegNotSupportedException e) {
                }
        }

    private void execFFmpegBinary(final String[] command) {

        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                        @Override
                        public void onFailure(String s) {
                         //   DebugMethods.sendToast("Failed with output" + s, TrimVideo.this);

                            CharSequence mText = "Failure with output\n" + s;
                            onSuccessText.setText(mText);
                        }

                        @Override
                        public void onSuccess(String s) {
                         //   DebugMethods.sendToast("Success with output" + s, TrimVideo.this);
                            onSuccessText = findViewById(R.id.errorMessage);
                            CharSequence mText = "Success with output\n" + s;
                            onSuccessText.setText(mText);
                            //Stuff here
                        }

                        @Override
                        public void onProgress(String s) {
                     //       DebugMethods.sendToast("Progress", TrimVideo.this);
                        }

                        @Override
                        public void onStart() {
                            DebugMethods.sendToast("Started", TrimVideo.this);
                        }

                        @Override
                        public void onFinish() {
                            DebugMethods.sendToast("Finished", TrimVideo.this);

                        }
                    }
            );
        } catch (FFmpegCommandAlreadyRunningException e) {
            DebugMethods.sendToast("Ffmpegcommandalreadyrunning", this);
    }
}


}

/*
As other people mentioned, putting -ss before (much faster) or after (more accurate) the -i makes a big difference.
The section "Fast And Accurate Seeking" on the ffmpeg seek page tells you how to get both, and I have used it, and it makes a big difference.
 Basically you put -ss before AND after the -i, just make sure to leave enough time before where you want to start cutting to have another key frame.
 Example: If you want to make a 1-minute clip, from 9min0sec to 10min 0sec in Video.mp4, you could do it both quickly and accurately using:

ffmpeg -ss 00:08:00 -i Video.mp4 -ss 00:01:00 -t 00:01:00 -c copy VideoClip.mp4
The first -ss seeks fast to (approximately) 8min0sec, and then the second -ss seeks accurately to 9min0sec, and the -t 00:01:00 takes out a 1min0sec clip.

Also note this important point from that page: "If you use -ss with -c:v copy, the resulting bitstream might end up being choppy,
not playable, or out of sync with the audio stream, since ffmpeg is forced to only use/split on i-frames."

This means you need to re-encode the video, even if you want to just copy it,
 or risk it being choppy and out of sync. You could try just -c copy first, but if the video sucks you'll need to re-do it.
*/