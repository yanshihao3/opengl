package com.zq.opengl.opengl.filter

import android.content.Context
import android.opengl.GLES20
import com.zq.opengl.R

/**
 * 摄像头过滤器
 */
class CameraFilter constructor(context: Context) :
    AbstractFrameFilter(context, R.raw.camera_vert, R.raw.camera_frag) {
    private lateinit var mtx: FloatArray
    private var vMatrix: Int = 0
    public override fun initGL(context: Context, vertexShaderId: Int, fragmentShaderId: Int) {
        super.initGL(context, vertexShaderId, fragmentShaderId)
        vMatrix = GLES20.glGetUniformLocation(program, "vMatrix")
    }

    public override fun beforeDraw() {
        super.beforeDraw()
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mtx, 0)
    }

    fun setTransformMatrix(mtx: FloatArray) {
        this.mtx = mtx
    }
}