package com.zq.opengl.opengl.record;

import android.content.Context;
import android.opengl.EGLContext;


public class VideoEncodeRecode extends BaseVideoEncoder implements AudioCapture.AudioCaptureListener {

    private AudioCapture audioCapture;

    public VideoEncodeRecode(Context context, EGLContext eglContext, String path, int width, int height) {
        super(context, eglContext, path, width, height);

        audioCapture = new AudioCapture();

        audioCapture.setCaptureListener(this);
    }


    @Override
    public void onCaptureListener(byte[] audioSource, int audioReadSize) {
        putPcmData(audioSource, audioReadSize);
    }

    @Override
    public void startRecode() {
        super.startRecode();
        audioCapture.start();
    }

    @Override
    public void stopRecode() {
        super.stopRecode();
        audioCapture.stop();
    }
}
