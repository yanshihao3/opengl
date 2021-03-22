package com.zq.opengl.opengl.filter;

import android.content.Context;
import android.opengl.GLES20;


import com.zq.opengl.R;
import com.zq.opengl.face.Face;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BigEyeFilter extends AbstractFrameFilter {
    private FloatBuffer left;
    private FloatBuffer right;
    int left_eye;
    int right_eye;
    Face face;

    public BigEyeFilter(Context context) {
        super(context, R.raw.base_vert, R.raw.bigeye_frag);

        left = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder()).asFloatBuffer();
        right = ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    @Override
    public void initGL(Context context, int vertexShaderId, int fragmentShaderId) {
        super.initGL(context, vertexShaderId, fragmentShaderId);
        left_eye = GLES20.glGetUniformLocation(program, "left_eye");
        right_eye = GLES20.glGetUniformLocation(program, "right_eye");
    }


    @Override
    public int onDraw(int texture, FilterChain filterChain) {
        FilterContext filterContext = filterChain.filterContext;
        face = filterContext.face;
        return super.onDraw(texture, filterChain);
    }

    @Override
    public void beforeDraw() {
        super.beforeDraw();

        if (face == null) {
            return;
        }

        float x = (float) (face.left_x / face.imgWidth);
        float y = (float) (1.0f - face.left_y / face.imgHeight);

        left.clear();
        left.put(x).put(y).position(0);

        GLES20.glUniform2fv(left_eye, 1, left);


        x = (float) (face.right_x / face.imgWidth);
        y = (float) (1.0f - face.right_y / face.imgHeight);

        right.clear();
        right.put(x).put(y).position(0);

        GLES20.glUniform2fv(right_eye, 1, right);


    }
}
