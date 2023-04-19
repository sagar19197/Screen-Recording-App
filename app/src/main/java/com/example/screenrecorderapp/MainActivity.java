package com.example.screenrecorderapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SCREEN_CAPTURE = 1;
    private MediaProjectionManager mediaProjectionManager;
    private static MediaProjection mediaProjection;


    private ToggleButton my_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if(mediaProjectionManager == null){
            Toast.makeText(this, "Permission Denied",Toast.LENGTH_SHORT);
        }


        // Toggle Button
        my_button = findViewById(R.id.my_button);
        my_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!my_button.isChecked()) {
                    // Stop recording
                    stopRecording();
                    my_button.setChecked(false);
                } else {
                    // Start recording
                    startRecording();
                    my_button.setChecked(true);
                }
            }
        });

    }

    private void requestScreenCapture() {
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE_SCREEN_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            if (resultCode == RESULT_OK) {

                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
                Intent intent = new Intent(this, ScreenRecorderService.class);
                startService(intent);

            } else {
                // Permission denied or user canceled
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startRecording() {
        requestScreenCapture();
    }

    private void stopRecording() {
        Intent intent = new Intent(this, ScreenRecorderService.class);
        stopService(intent);
        mediaProjection = null;

    }

    public static MediaProjection getMediaProjection() {
        return mediaProjection;
    }
}