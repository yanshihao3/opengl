package com.zq.opengl.opengl.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaRecorder;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Environment;

import androidx.camera.core.Preview;
import androidx.lifecycle.LifecycleOwner;


import com.zq.opengl.opengl.filter.AbstractFilter;
import com.zq.opengl.opengl.filter.BigEyeFilter;
import com.zq.opengl.opengl.filter.CameraFilter;
import com.zq.opengl.opengl.filter.FilterChain;
import com.zq.opengl.opengl.filter.FilterContext;
import com.zq.opengl.opengl.filter.ScreenFilter;
import com.zq.opengl.opengl.record.VideoEncodeRecode;
import com.zq.opengl.opengl.utils.CameraHelper;
import com.zq.opengl.opengl.utils.OpenGLUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRender implements GLSurfaceView.Renderer, Preview.OnPreviewOutputUpdateListener, SurfaceTexture.OnFrameAvailableListener {
    private CameraView cameraView;
    private CameraHelper cameraHelper;
    // 摄像头的图像  用OpenGL ES 画出来
    private SurfaceTexture mCameraTexure;

    private int[] textures;

    float[] mtx = new float[16];
    private MediaRecorder mRecorder;
    private FilterChain filterChain;

    private VideoEncodeRecode mVideoEncodeRecode;

    public CameraRender(CameraView cameraView) {
        this.cameraView = cameraView;
        OpenGLUtils.copyAssets2SdCard(cameraView.getContext(), "lbpcascade_frontalface.xml", "/sdcard/lbpcascade_frontalface.xml");
        OpenGLUtils.copyAssets2SdCard(cameraView.getContext(), "pd_2_00_pts5.dat", "/sdcard/pd_2_00_pts5.dat");
        LifecycleOwner lifecycleOwner = (LifecycleOwner) cameraView.getContext();
        cameraHelper = new CameraHelper(lifecycleOwner, this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //创建OpenGL 纹理 ,把摄像头的数据与这个纹理关联
        textures = new int[1];  //当做能在opengl用的一个图片的ID
        mCameraTexure.attachToGLContext(textures[0]);
        // 当摄像头数据有更新回调 onFrameAvailable
        mCameraTexure.setOnFrameAvailableListener(this);

        Context context = cameraView.getContext();

        List<AbstractFilter> filters = new ArrayList<>();
        filters.add(new CameraFilter(context));
        filters.add(new BigEyeFilter(context));
        filters.add(new ScreenFilter(context));

        filterChain = new FilterChain(filters, 0, new FilterContext());

        //录制视频的宽、高
        mVideoEncodeRecode = new VideoEncodeRecode(
                context, EGL14.eglGetCurrentContext(),
                Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                        "testRecode.mp4",
                480,
                640//相机的宽和高是相反的
        );
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        filterChain.setSize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //todo 更新纹理
        mCameraTexure.updateTexImage();

        mCameraTexure.getTransformMatrix(mtx);

        filterChain.setTransformMatrix(mtx);
        filterChain.setFace(cameraHelper.getFace());
        int id = filterChain.proceed(textures[0]);

        mVideoEncodeRecode.fireFrame(id, mCameraTexure.getTimestamp());
    }

    public void onSurfaceDestroyed() {

        filterChain.release();
    }


    /**
     * 更新
     *
     * @param output 預覽輸出
     */
    @Override
    public void onUpdated(Preview.PreviewOutput output) {
        mCameraTexure = output.getSurfaceTexture();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //  请求执行一次 onDrawFrame
        cameraView.requestRender();
    }

    public void startRecord(float speed) {
        try {
            mVideoEncodeRecode.startRecode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecord() {
        mVideoEncodeRecode.stopRecode();
    }
}
