package com.zq.opengl.opengl.utils

import android.util.Size
import androidx.camera.core.CameraX
import androidx.camera.core.CameraX.LensFacing
import androidx.camera.core.Preview
import androidx.camera.core.Preview.OnPreviewOutputUpdateListener
import androidx.camera.core.PreviewConfig
import androidx.lifecycle.LifecycleOwner

/**
 * 打开摄像头
 */
class CameraHelper constructor(
    lifecycleOwner: LifecycleOwner?,
    listener: OnPreviewOutputUpdateListener?
) {
    private val currentFacing: LensFacing = LensFacing.BACK

    init {
        val previewConfig: PreviewConfig = PreviewConfig.Builder()
            .setTargetResolution(Size(640, 480))
            .setLensFacing(currentFacing) //前置或者后置摄像头
            .build()
        val preview: Preview = Preview(previewConfig)
        preview.onPreviewOutputUpdateListener = listener
        CameraX.bindToLifecycle(lifecycleOwner, preview)
    }
}