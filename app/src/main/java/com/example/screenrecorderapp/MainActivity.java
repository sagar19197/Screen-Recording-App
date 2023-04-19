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
import android.view.Display;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SCREEN_CAPTURE = 1;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private MediaRecorder mediaRecorder;
    private VirtualDisplay virtualDisplay;
    private int screenDensity;
    private int screenWidth;
    private int screenHeight;
    private String videoPath;
    private boolean isRecording;

    private ToggleButton my_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if(mediaProjectionManager == null){
            Toast.makeText(this, "Permission Denied",Toast.LENGTH_SHORT);
        }
        else{
            requestScreenCapture();
        }
        screenDensity = getResources().getDisplayMetrics().densityDpi;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;


        // Toggle Button
        my_button = findViewById(R.id.my_button);
        my_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (my_button.isChecked()) {
                    // Stop recording
                    my_button.setChecked(false);
                    stopRecording();
                } else {
                    // Start recording
                    Toast.makeText(MainActivity.this, "Recording Started",Toast.LENGTH_SHORT).show();
                    my_button.setChecked(true);
                    startRecording();
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
            } else {
                // Permission denied or user canceled
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startRecording() {
        // Create a MediaRecorder object
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setVideoSize(screenWidth, screenHeight);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoEncodingBitRate(3000000);
        mediaRecorder.setOrientationHint(0);

        // Create a file to save the video
        videoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/screen_recording_" + System.currentTimeMillis() + ".mp4";
        mediaRecorder.setOutputFile(videoPath);

        // Create a VirtualDisplay object and start recording
        virtualDisplay = mediaProjection.createVirtualDisplay("ScreenCapture", screenWidth, screenHeight, screenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.getSurface(), null, null);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
        isRecording = true;
    }

    private void stopRecording() {
        if (isRecording) {
            isRecording = false;
            mediaRecorder.stop();
            mediaRecorder.release();
            virtualDisplay.release();
            mediaProjection.stop();
            Toast.makeText(this, "Screen recording saved to " + videoPath, Toast.LENGTH_LONG).show();
        }
    }


}