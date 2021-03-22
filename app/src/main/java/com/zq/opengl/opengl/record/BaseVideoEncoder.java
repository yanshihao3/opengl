package com.zq.opengl.opengl.record;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGLContext;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;


import com.zq.opengl.opengl.filter.AbstractFilter;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;


public abstract class BaseVideoEncoder {
    private Surface mSurface;

    private EGLContext mEGLContext;

    private Context mContext;


    private MediaMuxer mMediaMuxer;

    private MediaCodec.BufferInfo mVideoBuffInfo;
    private MediaCodec mVideoEncodec;

    private MediaCodec.BufferInfo mAudioBuffInfo;
    private MediaCodec mAudioEncodec;


    private VideoEncodecThread mVideoEncodecThread;

    private AudioEncodecThread mAudioEncodecThread;

    private boolean encodeStart;
    private boolean audioExit;
    private boolean videoExit;

    public final int sampleBit = 16;

    public final int channel = 2;

    public final int sampleRate = 44100;

    private Handler mHandler;

    private EGLEnv mEglEnv;


    private boolean isStart;

    private String path;

    private int width;

    private int height;

    public BaseVideoEncoder(Context context, EGLContext eglContext, String path, int width, int height) {
        this.mEGLContext = eglContext;
        this.path = path;
        this.width = width;
        this.height = height;
        this.mContext = context;
    }


    public void startRecode() {
        initMediaEncoder();
        if (mSurface != null && mEGLContext != null) {

            audioPts = 0;
            audioExit = false;
            videoExit = false;
            encodeStart = false;

            mVideoEncodecThread = new VideoEncodecThread(new WeakReference<>(this));
            mAudioEncodecThread = new AudioEncodecThread(new WeakReference<>(this));

            //創建OpenGL 的 環境
            HandlerThread handlerThread = new HandlerThread("codec-gl");
            handlerThread.start();
            mHandler = new Handler(handlerThread.getLooper());

            mHandler.post(() -> {
                mEglEnv = new EGLEnv(mContext, mEGLContext, mSurface, width, height);
                isStart = true;
            });
            mVideoEncodecThread.start();
            mAudioEncodecThread.start();
        }
    }

    public void fireFrame(int textureId, long timestamp) {
        if (!isStart) {
            return;
        }
        //录制用的opengl已经和handler的线程绑定了 ，所以需要在这个线程中使用录制的opengl
        mHandler.post(() -> {
            //画画
            mEglEnv.draw(textureId, timestamp);
        });
    }

    public void stopRecode() {
        if (mVideoEncodecThread != null) {
            mVideoEncodecThread.exit();
            mVideoEncodecThread = null;
        }

        if (mAudioEncodecThread != null) {
            mAudioEncodecThread.exit();
            mAudioEncodecThread = null;
        }

        audioPts = 0;
        encodeStart = false;

        isStart = false;

        mHandler.post(() -> {
            mEglEnv.release();
            mEglEnv = null;
            mSurface = null;
            mHandler.getLooper().quitSafely();
            mHandler = null;
        });


    }


    private void initMediaEncoder() {
        try {
            mMediaMuxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            // h264
            initVideoEncoder(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            // aac
            initAudioEncoder(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channel);

            if (onStatusChangeListener != null) {
                onStatusChangeListener.onStatusChange(OnStatusChangeListener.STATUS.INIT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initVideoEncoder(String mineType, int width, int height) {
        try {
            mVideoEncodec = MediaCodec.createEncoderByType(mineType);

            MediaFormat videoFormat = MediaFormat.createVideoFormat(mineType, width, height);
            videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);//30帧
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 4);//RGBA
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            //设置压缩等级  默认是baseline
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                videoFormat.setInteger(MediaFormat.KEY_PROFILE, MediaCodecInfo.CodecProfileLevel.AVCProfileMain);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    videoFormat.setInteger(MediaFormat.KEY_LEVEL, MediaCodecInfo.CodecProfileLevel.AVCLevel3);
                }
            }

            mVideoEncodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mVideoBuffInfo = new MediaCodec.BufferInfo();
            mSurface = mVideoEncodec.createInputSurface();
        } catch (IOException e) {
            e.printStackTrace();
            mVideoEncodec = null;
            mVideoBuffInfo = null;
            mSurface = null;
        }
    }


    private void initAudioEncoder(String mineType, int sampleRate, int channel) {
        try {
            mAudioEncodec = MediaCodec.createEncoderByType(mineType);
            MediaFormat audioFormat = MediaFormat.createAudioFormat(mineType, sampleRate, channel);
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 10);
            mAudioEncodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);

            mAudioBuffInfo = new MediaCodec.BufferInfo();
        } catch (IOException e) {
            e.printStackTrace();
            mAudioEncodec = null;
            mAudioBuffInfo = null;
        }
    }

    public void putPcmData(byte[] buffer, int size) {
        if (mAudioEncodecThread != null && !mAudioEncodecThread.isExit && buffer != null && size > 0) {
            int inputBufferIndex = mAudioEncodec.dequeueInputBuffer(0);
            if (inputBufferIndex >= 0) {
                ByteBuffer byteBuffer = mAudioEncodec.getInputBuffers()[inputBufferIndex];
                byteBuffer.clear();
                byteBuffer.put(buffer);
                long pts = getAudioPts(size);
                Log.e("zzz", "AudioTime = " + pts / 1000000.0f);
                mAudioEncodec.queueInputBuffer(inputBufferIndex, 0, size, pts, 0);
            }
        }
    }


    private long audioPts;

    //176400
    private long getAudioPts(int size) {
        audioPts += (long) (1.0 * size / (sampleRate * channel * (sampleBit / 8)) * 1000000.0);
        return audioPts;
    }

    static class VideoEncodecThread extends Thread {
        private WeakReference<BaseVideoEncoder> encoderWeakReference;
        private boolean isExit;

        private int videoTrackIndex;
        private long pts;

        private MediaCodec videoEncodec;
        private MediaCodec.BufferInfo videoBufferinfo;
        private MediaMuxer mediaMuxer;


        public VideoEncodecThread(WeakReference<BaseVideoEncoder> encoderWeakReference) {
            this.encoderWeakReference = encoderWeakReference;

            videoEncodec = encoderWeakReference.get().mVideoEncodec;
            videoBufferinfo = encoderWeakReference.get().mVideoBuffInfo;
            mediaMuxer = encoderWeakReference.get().mMediaMuxer;
            pts = 0;
            videoTrackIndex = -1;
        }

        @Override
        public void run() {
            super.run();
            isExit = false;
            videoEncodec.start();
            while (true) {
                if (isExit) {
                    videoEncodec.stop();
                    videoEncodec.release();
                    videoEncodec = null;
                    encoderWeakReference.get().videoExit = true;

                    if (encoderWeakReference.get().audioExit) {
                        mediaMuxer.stop();
                        mediaMuxer.release();
                        mediaMuxer = null;


                        if (encoderWeakReference.get().onStatusChangeListener != null) {
                            encoderWeakReference.get().onStatusChangeListener.onStatusChange(OnStatusChangeListener.STATUS.END);
                        }
                    }

                    break;
                }

                int outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    videoTrackIndex = mediaMuxer.addTrack(videoEncodec.getOutputFormat());
                    if (encoderWeakReference.get().mAudioEncodecThread.audioTrackIndex != -1) {
                        mediaMuxer.start();
                        encoderWeakReference.get().encodeStart = true;

                        if (encoderWeakReference.get().onStatusChangeListener != null) {
                            encoderWeakReference.get().onStatusChangeListener.onStatusChange(OnStatusChangeListener.STATUS.START);
                        }
                    }
                } else {
                    while (outputBufferIndex >= 0) {
                        if (!encoderWeakReference.get().encodeStart) {
                            SystemClock.sleep(10);
                            continue;
                        }
                        ByteBuffer outputBuffer = videoEncodec.getOutputBuffers()[outputBufferIndex];
                        outputBuffer.position(videoBufferinfo.offset);
                        outputBuffer.limit(videoBufferinfo.offset + videoBufferinfo.size);

                        //设置时间戳
                        if (pts == 0) {
                            pts = videoBufferinfo.presentationTimeUs;
                        }
                        videoBufferinfo.presentationTimeUs = videoBufferinfo.presentationTimeUs - pts;
                        //写入数据
                        mediaMuxer.writeSampleData(videoTrackIndex, outputBuffer, videoBufferinfo);
                        Log.e("zzz", "VideoTime = " + videoBufferinfo.presentationTimeUs / 1000000.0f);
                        if (encoderWeakReference.get().onMediaInfoListener != null) {
                            encoderWeakReference.get().onMediaInfoListener.onMediaTime((int) (videoBufferinfo.presentationTimeUs / 1000000));
                        }
                        videoEncodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = videoEncodec.dequeueOutputBuffer(videoBufferinfo, 0);
                    }
                }
            }
        }

        public void exit() {
            isExit = true;
        }
    }

    static class AudioEncodecThread extends Thread {
        private WeakReference<BaseVideoEncoder> encoderWeakReference;
        private boolean isExit;


        private MediaCodec audioEncodec;
        private MediaCodec.BufferInfo audioBufferinfo;
        private MediaMuxer mediaMuxer;

        private int audioTrackIndex;
        private long pts;


        public AudioEncodecThread(WeakReference<BaseVideoEncoder> encoderWeakReference) {
            this.encoderWeakReference = encoderWeakReference;
            audioEncodec = encoderWeakReference.get().mAudioEncodec;
            audioBufferinfo = encoderWeakReference.get().mAudioBuffInfo;
            mediaMuxer = encoderWeakReference.get().mMediaMuxer;
            pts = 0;
            audioTrackIndex = -1;
        }


        @Override
        public void run() {
            super.run();
            isExit = false;
            audioEncodec.start();

            while (true) {
                if (isExit) {
                    audioEncodec.stop();
                    audioEncodec.release();
                    audioEncodec = null;
                    encoderWeakReference.get().audioExit = true;

                    //如果video退出了
                    if (encoderWeakReference.get().videoExit) {
                        mediaMuxer.stop();
                        mediaMuxer.release();
                        mediaMuxer = null;

                        if (encoderWeakReference.get().onStatusChangeListener != null) {
                            encoderWeakReference.get().onStatusChangeListener.onStatusChange(OnStatusChangeListener.STATUS.END);
                        }

                    }
                    break;
                }

                int outputBufferIndex = audioEncodec.dequeueOutputBuffer(audioBufferinfo, 0);
                if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    audioTrackIndex = mediaMuxer.addTrack(audioEncodec.getOutputFormat());
                    if (encoderWeakReference.get().mVideoEncodecThread.videoTrackIndex != -1) {
                        mediaMuxer.start();
                        encoderWeakReference.get().encodeStart = true;
                        if (encoderWeakReference.get().onStatusChangeListener != null) {
                            encoderWeakReference.get().onStatusChangeListener.onStatusChange(OnStatusChangeListener.STATUS.START);
                        }
                    }
                } else {
                    while (outputBufferIndex >= 0) {
                        if (!encoderWeakReference.get().encodeStart) {
                            SystemClock.sleep(10);
                            continue;
                        }

                        ByteBuffer outputBuffer = audioEncodec.getOutputBuffers()[outputBufferIndex];
                        outputBuffer.position(audioBufferinfo.offset);
                        outputBuffer.limit(audioBufferinfo.offset + audioBufferinfo.size);

                        //设置时间戳
                        if (pts == 0) {
                            pts = audioBufferinfo.presentationTimeUs;
                        }
                        audioBufferinfo.presentationTimeUs = audioBufferinfo.presentationTimeUs - pts;
                        //写入数据
                        mediaMuxer.writeSampleData(audioTrackIndex, outputBuffer, audioBufferinfo);

                        audioEncodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = audioEncodec.dequeueOutputBuffer(audioBufferinfo, 0);
                    }
                }

            }

        }

        public void exit() {
            isExit = true;
        }
    }


    private OnMediaInfoListener onMediaInfoListener;

    public void setOnMediaInfoListener(OnMediaInfoListener onMediaInfoListener) {
        this.onMediaInfoListener = onMediaInfoListener;
    }

    public interface OnMediaInfoListener {
        void onMediaTime(int times);
    }

    private OnStatusChangeListener onStatusChangeListener;

    public void setOnStatusChangeListener(OnStatusChangeListener onStatusChangeListener) {
        this.onStatusChangeListener = onStatusChangeListener;
    }

    public interface OnStatusChangeListener {
        void onStatusChange(STATUS status);

        enum STATUS {
            INIT,
            START,
            END
        }

    }

}
