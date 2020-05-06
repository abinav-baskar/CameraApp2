package com.example.cameraapp2;

import android.content.Context;
import android.widget.Toast;

public class DebugMethods {
    private static Toast toast = null;
    public static void sendToast(CharSequence cs, Context c) {
        toast = Toast.makeText(c, cs, Toast.LENGTH_SHORT);
        toast.show();
    }
}
