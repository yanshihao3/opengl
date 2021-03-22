package com.zq.opengl.opengl.utils

import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import androidx.camera.core.*
import androidx.camera.core.CameraX.LensFacing
import androidx.camera.core.Preview.OnPreviewOutputUpdateListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.zq.opengl.face.Face
import com.zq.opengl.face.FaceTracker
import java.lang.Exception


/**
 * 打开摄像头
 */
class CameraHelper constructor(
    lifecycleOwner: LifecycleOwner?,
    listener: OnPreviewOutputUpdateListener?
) : ImageAnalysis.Analyzer, LifecycleObserver {
    private val currentFacing: LensFacing = LensFacing.BACK
    private val handlerThread by lazy {
        HandlerThread("Analyze-thread")
    }

    private var face: Face? = null
    private lateinit var faceTracker: FaceTracker

    init {
        lifecycleOwner?.lifecycle?.addObserver(this)
        val previewConfig: PreviewConfig = PreviewConfig.Builder()
            .setTargetResolution(Size(640, 480))
            .setLensFacing(currentFacing) //前置或者后置摄像头
            .build()
        val preview: Preview = Preview(previewConfig)
        preview.onPreviewOutputUpdateListener = listener
        handlerThread.start()
        CameraX.bindToLifecycle(lifecycleOwner, preview, getImageAnalysis())

    }

    private fun getImageAnalysis(): ImageAnalysis? {
        val imageAnalysisConfig = ImageAnalysisConfig.Builder()
            .setCallbackHandler(Handler(handlerThread.looper))
            .setLensFacing(currentFacing).setTargetResolution(Size(640, 480))
            .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            .build()
        val imageAnalysis = ImageAnalysis(imageAnalysisConfig)
        imageAnalysis.analyzer = this
        return imageAnalysis
    }

    override fun analyze(image: ImageProxy?, rotationDegrees: Int) {
        try {
            if (image != null) {
                val bytes: ByteArray = ImageUtils.getBytes(image)
                synchronized(this) {
                    face = faceTracker.detect(bytes, image!!.width, image.height, rotationDegrees)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Synchronized
    fun getFace(): Face? {
        return face
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(owner: LifecycleOwner?) {
        faceTracker = FaceTracker(
            "/sdcard/lbpcascade_frontalface.xml",
            "/sdcard/pd_2_00_pts5.dat"
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart(owner: LifecycleOwner?) {
        faceTracker.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop(owner: LifecycleOwner?) {
        faceTracker.stop()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(owner: LifecycleOwner) {
        faceTracker.release()
    }

    companion object {
        private const val TAG = "CameraHelper"
    }


}