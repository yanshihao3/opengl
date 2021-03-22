package com.zq.opengl.opengl.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.zq.opengl.R;
import com.zq.opengl.face.Face;
import com.zq.opengl.opengl.utils.OpenGLUtils;


public class StickFilter extends AbstractFrameFilter {

    private Bitmap bizi;
    private int[] textures;

    public StickFilter(Context context) {
        super(context, R.raw.base_vert, R.raw.base_frag);

        textures = new int[1];
        OpenGLUtils.glGenTextures(textures);

        // 把图片加载到创建的纹理中
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        //....
        bizi = BitmapFactory.decodeResource(context.getResources(), R.drawable.bizi);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bizi,0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

    }


    @Override
    public int onDraw(int texture, FilterChain filterChain) {
        return super.onDraw(texture, filterChain);
    }

    @Override
    public void afterDraw(FilterContext filterContext) {
        super.afterDraw(filterContext);

        //画鼻子
        Face face = filterContext.face;
        if (face == null) {
            return;
        }

        //开启混合模式
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);


        //计算坐标
        //基于画布的鼻子中心点的x
        float x = face.nose_x / face.imgWidth * filterContext.width;
        float y = (1.0f - face.nose_y / face.imgHeight) * filterContext.height;


        //鼻子贴纸的宽与高

        //通过左右嘴角的x的差作为鼻子装饰品的宽
        float mrx = face.mouseRight_x / face.imgWidth * filterContext.width;
        float mlx = face.mouseLeft_x / face.imgWidth * filterContext.width;
        int width = (int) (mrx - mlx);


        //以嘴角的Y与鼻子中心点的y的差作为鼻子装饰品的高
        float mry = (1.0f - face.mouseRight_y / face.imgHeight) * filterContext.height;
        int height = (int) (y - mry);


        GLES20.glViewport((int) x - width / 2, (int) y - height / 2, width, height);
        //画鼻子

        GLES20.glUseProgram(program);

        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);


        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glUniform1i(vTexture, 0);


        //通知画画
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        //关闭混合模式
        GLES20.glDisable(GLES20.GL_BLEND);

    }
}
