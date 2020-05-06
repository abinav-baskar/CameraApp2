package com.example.cameraapp2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveMedia {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static void galleryAddPic(Uri m_uri, Context context) {

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(m_uri);
        context.sendBroadcast(mediaScanIntent);
    }

    public static Uri getOutputMediaFileUri(int type, Context c) {
        File f = getOutputMediaFile(type, c);
        return Uri.fromFile(getOutputMediaFile(type, c));
    }


    public static File getOutputMediaFile(int type, Context c) {


        File mediaStorageDir = createStorageDirectory(c, "MyCameraApp");

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static File createStorageDirectory(Context c, String directoryName) {
        File m_mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), directoryName); //the other one was myCameraApp, remember?
        if (!m_mediaStorageDir.exists()) {
            if (!m_mediaStorageDir.mkdirs()) {
                DebugMethods.sendToast("Not cannot make directory", c); //later we should make the thing to put it in gallery
                return null;
            }
        }
        return m_mediaStorageDir;
    }

}
