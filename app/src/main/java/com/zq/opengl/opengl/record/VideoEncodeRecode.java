package com.zq.opengl.opengl.record;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.opengl.EGLContext;

import java.io.IOException;

/**
 * 一个音频轨道
 */
public class VideoEncodeRecode extends BaseVideoEncoder implements AudioCapture.AudioCaptureListener {

    private AudioCapture audioCapture;

    private MediaCodec.BufferInfo mOtherAudioBuffInfo;
    private MediaCodec mOtherAudioEncodec;


    public VideoEncodeRecode(Context context, EGLContext eglContext, String path, int width, int height) {
        super(context, eglContext, path, width, height);

        audioCapture = new AudioCapture();

        audioCapture.setCaptureListener(this);

    }


    private void initOtherEncoder(String mineType, int sampleRate, int channel) {
        try {
            mOtherAudioEncodec = MediaCodec.createEncoderByType(mineType);
            MediaFormat audioFormat = MediaFormat.createAudioFormat(mineType, sampleRate, channel);
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 10);
            mOtherAudioEncodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            mOtherAudioBuffInfo = new MediaCodec.BufferInfo();
        } catch (IOException e) {
            e.printStackTrace();
            mOtherAudioBuffInfo = null;
            mOtherAudioEncodec = null;
        }
    }

    @Override
    public void onCaptureListener(byte[] audioSource, int audioReadSize) {
        putPcmData(audioSource, audioReadSize);
    }

    @Override
    public void startRecode() {
        super.startRecode();
        initOtherEncoder(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channel);

        audioCapture.start();
    }

    @Override
    public void stopRecode() {
        super.stopRecode();
        audioCapture.stop();
    }
}
