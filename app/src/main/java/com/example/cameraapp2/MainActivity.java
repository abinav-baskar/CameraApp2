package com.example.cameraapp2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private boolean canUseCamera = false;

    static final int REQUEST_CAMERA_CODE = 0;
    static final int REQUEST_AUDIO_CODE = 1;
    static final int REQUEST_READSTORAGE_CODE = 2;
    static final int REQUEST_WRITESTORAGE_CODE = 3;
    private boolean[] permissionsGiven;
    Button tryAgainButton;
    TextView permissionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
         tryAgainButton = findViewById(R.id.tryAgainButton);
         permissionText = findViewById(R.id.permissionText);

        tryAgainButton.setVisibility(View.INVISIBLE);
        permissionsGiven = new boolean[4];
        if(!checkPermissions()) {
            permissionText.setText(R.string.deniedPermission);
            tryAgainButton.setVisibility(View.VISIBLE);
        }
    }

    public void tryAgainButtonPressed(View view) {
        if(checkPermissions()) {
            permissionText.setText(R.string.blank);
            tryAgainButton.setVisibility(View.INVISIBLE);

        }
    }

    public void goToManualVideo(View view) {
        if(canUseCamera) {
            Intent intent = new Intent(this, ManualVideoActivity.class);
            startActivity(intent);
        }
    }

    //Temp method
    public void goToTrimVideo(View view) {
        Intent intent = new Intent(this, TrimVideo.class);
        startActivity(intent);
    }

    public void goToAppendVideo(View view) {
        Intent intent = new Intent(this, AppendVideo.class);
        startActivity(intent);
    }

    public boolean checkPermissions() { //JUST CHANGE A BOOLEAN AND RETURN EACH STEP HERE, AND HAVE A WHILE LOOP IN ON CREATE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
          //  DebugMethods.sendToast("Permission to use Camera is not granted", this);
            canUseCamera = false;
            // Permission is not granted
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_CODE);
           return false;
        }

       else if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
       //     DebugMethods.sendToast("Permission to use Camera is not granted", this);
            canUseCamera = false;
            // Permission is not granted
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_AUDIO_CODE);
          return false;
        }

        else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
          //  DebugMethods.sendToast("Permission to use Camera is not granted", this);
            canUseCamera = false;
            // Permission is not granted
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_READSTORAGE_CODE);
            return false;
           // checkPermissions();
        }

       else  if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
           // DebugMethods.sendToast("Permission to use Camera is not granted", this);
            canUseCamera = false;
            // Permission is not granted
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITESTORAGE_CODE);
            return false;
        }
       else {
            canUseCamera = true;
            return true;
        }
    }

   /*@Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission granted, yay!
                    permissionsGiven[0] = true;
                } else {
                    canUseCamera = false;
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.CAMERA)) {
                        permissionsGiven[0] = false;
                        //User has pressed 'do not ask again'
                        //What a useless fellow; but what we can do is use intent to direct him
                    }
                    //He just said no
                }
              return;
            }
            case REQUEST_AUDIO_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionsGiven[1] = true;
                    //Permission granted, yay!
                }
                else {
                    permissionsGiven[1] = false;
                    return;
                }
            }
            case REQUEST_READSTORAGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionsGiven[2] = true;
                    //Permission granted, yay!
                }
                else {
                    permissionsGiven[2] = false;
                    return;
                }
            }
            case REQUEST_WRITESTORAGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Permission granted, yay!
                    permissionsGiven[3] = true;
                }
                else {
                    permissionsGiven[3] = false;
                    return;
                }
            }
        }
        canUseCamera = true;
    }*/

}
