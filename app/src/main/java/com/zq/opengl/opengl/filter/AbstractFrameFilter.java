package com.zq.opengl.opengl.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.zq.opengl.opengl.utils.OpenGLUtils;


public abstract class AbstractFrameFilter extends AbstractFilter {

    int[] frameBuffer;
    int[] frameTextures;

    private boolean isFirst = true;

    public AbstractFrameFilter(Context context, int vertexShaderId, int fragmentShaderId) {
        super(context, vertexShaderId, fragmentShaderId);
    }


    public void createFrame(int width, int height) {
        releaseFrame();
        //創建FBO
        /**
         * 1、创建FBO + FBO中的纹理
         */
        frameBuffer = new int[1];
        frameTextures = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffer, 0);
        OpenGLUtils.glGenTextures(frameTextures);

        /**
         * 2、fbo与纹理关联
         */
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameTextures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                null);
        //纹理关联 fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);  //綁定FBO
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
                frameTextures[0],
                0);

        /**
         * 3、解除绑定
         */
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

    }


    @Override
    public int onDraw(int texture, FilterChain filterChain) {
        FilterContext filterContext = filterChain.filterContext;
        if (isFirst) {
            createFrame(filterContext.width, filterContext.height);
            isFirst = false;
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]); //綁定fbo
        super.onDraw(texture, filterChain);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);  //

        return filterChain.proceed(frameTextures[0]);
    }

    @Override
    public void release() {
        super.release();
        releaseFrame();
    }

    private void releaseFrame() {
        if (frameTextures != null) {
            GLES20.glDeleteTextures(1, frameTextures, 0);
            frameTextures = null;
        }

        if (frameBuffer != null) {
            GLES20.glDeleteFramebuffers(1, frameBuffer, 0);
        }
    }
}
