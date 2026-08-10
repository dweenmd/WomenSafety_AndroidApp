package com.dweenmd.womensafety.sos;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class AudioRecorderHelper {
    private static final String TAG = "AudioRecorderHelper";
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private final Context context;

    public AudioRecorderHelper(Context context) {
        this.context = context;
    }

    public void startRecording() {
        if (isRecording) return;
        
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "RECORD_AUDIO permission not granted");
            return;
        }
        
        File outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (outputDir == null) return;
        
        String filePath = outputDir.getAbsolutePath() + "/SOS_Audio_" + System.currentTimeMillis() + ".3gp";

        mediaRecorder = new MediaRecorder();
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(filePath);
            
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Log.d(TAG, "Recording started: " + filePath);
            
            // Auto stop after 15 seconds
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this::stopRecording, 15000);
        } catch (Exception e) {
            Log.e(TAG, "Error starting recording", e);
            releaseRecorder();
        }
    }

    public void stopRecording() {
        if (isRecording && mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                Log.d(TAG, "Recording stopped");
            } catch (RuntimeException stopException) {
                Log.e(TAG, "Error stopping recording", stopException);
            } finally {
                releaseRecorder();
            }
        }
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
        isRecording = false;
    }
}
