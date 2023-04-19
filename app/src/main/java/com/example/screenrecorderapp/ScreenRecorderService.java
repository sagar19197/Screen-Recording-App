package com.example.screenrecorderapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class ScreenRecorderService extends Service {

    private static final String CHANNEL_ID = "ScreenRecordingServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private boolean isRecording = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Screen Recording Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, ScreenRecorderService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Screen Recording")
                .setContentText("Recording your screen...")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);

        return builder.build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (isRecording) {
            stopRecording();
            stopForeground(true);
            stopSelf();
        } else {

            startForeground(NOTIFICATION_ID, createNotification());
            if (MainActivity.getMediaProjection() != null) {

            // Get the media projection from the activity
            mediaProjection = MainActivity.getMediaProjection();
           startRecording();
        }
        }
        return START_NOT_STICKY;
    }

    // SCREEN RECORDING CODE FOLLOWS FROM HERE -
    private static final int REQUEST_CODE_SCREEN_CAPTURE = 1;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private MediaRecorder mediaRecorder;
    private VirtualDisplay virtualDisplay;
    private int screenDensity;
    private int screenWidth;
    private int screenHeight;
    private String videoPath;


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




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
