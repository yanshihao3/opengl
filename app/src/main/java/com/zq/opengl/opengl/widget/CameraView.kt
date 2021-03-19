package com.zq.opengl.opengl.widget

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.SurfaceHolder
import com.zq.opengl.opengl.record.Speed

/**
 * @program:
 * @description:
 * @author: 闫世豪
 * @create: 2021-03-16 18:34
 */
class CameraView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) :
    GLSurfaceView(context, attrs) {
    private val renderer: CameraRender
    private var mSpeed = Speed.MODE_NORMAL


    init {
        //使用
        setEGLContextClientVersion(2)
        //設置渲染回調接口
        renderer = CameraRender(this)
        setRenderer(renderer)
        /**
         * 刷新方式：
         * RENDERMODE_WHEN_DIRTY 手动刷新，調用requestRender();
         * RENDERMODE_CONTINUOUSLY 自動刷新，大概16ms自動回調一次onDraw方法
         */
        //注意必须在setRenderer 后面。
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        super.surfaceDestroyed(holder)
        renderer.onSurfaceDestroyed()
    }


    fun setSpeed(speed: Speed) {
        this.mSpeed = speed
    }

    fun startRecord() {
        //速度  时间/速度 speed小于就是放慢 大于1就是加快
        var speed = 1f
        speed = when (mSpeed) {
            Speed.MODE_EXTRA_SLOW -> 0.3f
            Speed.MODE_SLOW -> 0.5f
            Speed.MODE_NORMAL -> 1f
            Speed.MODE_FAST -> 2f
            Speed.MODE_EXTRA_FAST -> 3f
        }
        renderer.startRecord(speed)
    }

    fun stopRecord() {
        renderer.stopRecord()
    }
}