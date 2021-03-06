package com.example.cameraapp2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.security.auth.callback.Callback;


public class AppendVideo extends AppCompatActivity {
    Uri uri;
    FFmpeg ffmpeg;
    String[] commands;
    Callback activity;
    static String inputFilePath1;
    static String inputFilePath2;
    static String outputFilePath = null;
    public Context trimVideoContext = this;
    TextView onSuccessText = null;
    File mediaStorageDir = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_append_video);
    }

    public void appendTheVideo(String mInputFilePath1, String mInputFilePath2, String mOutputFilePath, Context c) {
        loadFFMpegBinary(c);

        if(mInputFilePath1 != null && mInputFilePath2 != null) {
            String list = generateList(new String[] {mInputFilePath1, mInputFilePath2});
     //String[] complexCommand = {"ffmpeg", "-f", "concat", "-i", list, "-c", "copy", outputFilePath};
            String[] complexCommand = {"-f", "concat","-safe", "0", "-i", list, "-c", "copy", mOutputFilePath};
            execFFmpegBinary(complexCommand, c);
        }
        else {
            DebugMethods.sendToast("File path is null, ", c);
        }
    }

    private static String generateList(String[] inputs) {
        File list;
        Writer writer = null;
        try {
            list = File.createTempFile("ffmpeg-list", ".txt");
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list)));
            for (String input: inputs) {
                writer.write("file '" + input + "'\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "/";
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
     //   Log.d(TAG, "Wrote list file to " + list.getAbsolutePath());
        return list.getAbsolutePath();
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

    private void execFFmpegBinary(final String[] command, final Context c) {

        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                        @Override
                        public void onFailure(String s) {
                            ManualVideoActivity.setFfmpegDone(true);
                            DebugMethods.sendToast("AppendVideo: Failure", c);
                            //   DebugMethods.sendToast("Failed with output" + s, TrimVideo.this);
                           /* CharSequence mText = "Failure with output\n" + s;
                            onSuccessText.setText(mText);
                            System.out.println(mText);*/
                        }

                        @Override
                        public void onSuccess(String s) {
                            ManualVideoActivity.setFfmpegDone(true);
                            DebugMethods.sendToast("AppendVideo: Success", c);

                            /*DebugMethods.sendToast("Success with output" + s, TrimVideo.this);
                            CharSequence mText = "Success with output\n" + s;
                            onSuccessText.setText(mText);*/
                        }

                        @Override
                        public void onProgress(String s) {
                            //DebugMethods.sendToast("Progress", TrimVideo.this);
                        }

                        @Override
                        public void onStart() {
                            DebugMethods.sendToast("Started", c);
                        }

                        @Override
                        public void onFinish() {
                            DebugMethods.sendToast("Finished", c);
                        }
                    }
            );
        } catch (FFmpegCommandAlreadyRunningException e) {
            DebugMethods.sendToast("Ffmpegcommandalreadyrunning", this);
        }
    }

    public void testAppendVideo() {
        mediaStorageDir = SaveMedia.createStorageDirectory(this, "CameraApp2");

        File videoOutput = new File(mediaStorageDir.getPath() + File.separator +
                "appendedVideo.mp4");

        inputFilePath1 = "/storage/emulated/0/Pictures/MyCameraApp/VID_20200416_162058.mp4";
        inputFilePath2 = "/storage/emulated/0/Pictures/MyCameraApp/VID_20200418_104909.mp4";


        outputFilePath = videoOutput.getAbsolutePath();

        onSuccessText = findViewById(R.id.appendErrorMessage);
        onSuccessText.setMovementMethod(new ScrollingMovementMethod());
        appendTheVideo(inputFilePath1, inputFilePath2, outputFilePath,this);
    }


}