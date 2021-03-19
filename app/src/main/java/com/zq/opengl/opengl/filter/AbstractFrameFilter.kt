package com.zq.opengl.opengl.filter

import android.content.Context
import android.opengl.GLES20
import com.zq.opengl.opengl.utils.OpenGLUtils

/**
 * @program: opencv
 * @description:帧缓存fbo
 * @author: 闫世豪
 * @create: 2021-03-17 16:44
 */
open class AbstractFrameFilter(context: Context, vertexShaderId: Int, fragmentShaderId: Int) :
    AbstractFilter(context, vertexShaderId, fragmentShaderId) {
    private lateinit var frameBuffer: IntArray
    private lateinit var frameTextures: IntArray
    override fun setSize(width: Int, height: Int) {
        super.setSize(width, height)
        //创建FBO
        /**
         * 1.创建FBO  + FBO 纹理
         *
         */
        frameBuffer = IntArray(1)
        frameTextures = IntArray(1)
        GLES20.glGenBuffers(1, frameBuffer, 0)
        OpenGLUtils.glGenTextures(frameTextures)
        /**
         * 2.fbo 与纹理关联
         */
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameTextures[0])
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            width,
            height,
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            null
        )
        //纹理关联 fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]) //綁定FBO
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
            frameTextures[0],
            0
        )
    }

    override fun onDraw(texture: Int): Int {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]) //綁定fbo
        super.onDraw(texture)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0) //
        return frameTextures[0]
    }

    /**
     * 释放资源
     */
    override fun release() {
        super.release()
        GLES20.glDeleteTextures(1, frameTextures, 0)
        GLES20.glDeleteFramebuffers(1, frameBuffer, 0)
    }
}