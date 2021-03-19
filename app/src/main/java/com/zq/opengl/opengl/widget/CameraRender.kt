package com.zq.opengl.opengl.widget

import android.content.Context
import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.opengl.EGL14
import android.opengl.GLSurfaceView
import android.os.Environment
import androidx.camera.core.Preview.OnPreviewOutputUpdateListener
import androidx.camera.core.Preview.PreviewOutput
import androidx.lifecycle.LifecycleOwner
import com.zq.opengl.opengl.filter.CameraFilter
import com.zq.opengl.opengl.filter.ScreenFilter
import com.zq.opengl.opengl.record.BaseVideoEncoder
import com.zq.opengl.opengl.record.PutPcmThread
import com.zq.opengl.opengl.record.VideoEncodeRecode
import com.zq.opengl.opengl.utils.CameraHelper
import java.io.File
import java.io.IOException
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * @program:
 * @description:
 * @author: 闫世豪
 * @create: 2021-03-17 10:18
 */
class CameraRender(private val cameraView: CameraView) : GLSurfaceView.Renderer,
    OnFrameAvailableListener, OnPreviewOutputUpdateListener {
    // 摄像头的图像  用OpenGL ES 画出来
    private lateinit var mCameraTexture: SurfaceTexture
    private lateinit var screenFilter: ScreenFilter
    private lateinit var textures: IntArray
    var mtx: FloatArray = FloatArray(16)
    private val context: Context = cameraView.context
    private lateinit var cameraFilter: CameraFilter
    private val lifecycleOwner: LifecycleOwner = cameraView.context as LifecycleOwner
    private lateinit var mVideoEncodeRecode: VideoEncodeRecode


    protected lateinit var putPcmThread: PutPcmThread

    public override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        //创建OpenGL 纹理 ,把摄像头的数据与这个纹理关联
        textures = IntArray(1) //当做能在opengl用的一个图片的ID
        mCameraTexture.attachToGLContext(textures.get(0))
        // 当摄像头数据有更新回调 onFrameAvailable
        mCameraTexture.setOnFrameAvailableListener(this)
        cameraFilter = CameraFilter(context)
        screenFilter = ScreenFilter(context)

        mVideoEncodeRecode = VideoEncodeRecode(
            cameraView.context, EGL14.eglGetCurrentContext(),
            Environment.getExternalStorageDirectory().absolutePath + File.separator +
                    "testRecode.mp4",
            480,
            640//相机的宽和高是相反的
        )

//
//        mVideoEncodeRecode.setOnStatusChangeListener { status ->
//            if (status === BaseVideoEncoder.OnStatusChangeListener.STATUS.START) {
//                putPcmThread =
//                    PutPcmThread(context, mVideoEncodeRecode)
//                putPcmThread.start()
//            }
//        }

    }

    public override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        screenFilter.setSize(width, height)
        cameraFilter.setSize(width, height)
    }

    public override fun onDrawFrame(gl: GL10) {
        mCameraTexture.updateTexImage()
        mCameraTexture.getTransformMatrix(mtx)
        cameraFilter.setTransformMatrix(mtx)
        var id: Int = cameraFilter.onDraw(textures[0])
        id = screenFilter.onDraw(id)
        mVideoEncodeRecode.fireFrame(id, mCameraTexture.timestamp)
    }

    fun onSurfaceDestroyed() {
        cameraFilter.release()
        screenFilter.release()
    }

    public override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        cameraView.requestRender()
    }

    public override fun onUpdated(output: PreviewOutput) {
        mCameraTexture = output.surfaceTexture
    }

    fun startRecord(speed: Float) {
        try {

            mVideoEncodeRecode.startRecode()
            //    mRecorder.start(speed)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopRecord() {
        mVideoEncodeRecode.stopRecode()
     //   putPcmThread.setExit(true)

    }

    init {
        CameraHelper(lifecycleOwner, this)
    }


}