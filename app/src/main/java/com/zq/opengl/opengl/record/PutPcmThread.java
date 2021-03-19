package com.zq.opengl.opengl.record;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

public class PutPcmThread extends Thread {

    private boolean isExit;
    private Context context;
    private VideoEncodeRecode videoEncodeRecode;

    public PutPcmThread(Context context, VideoEncodeRecode videoEncodeRecode) {
        this.context = context;
        this.videoEncodeRecode = videoEncodeRecode;
    }

    public void setExit(boolean exit) {
        isExit = exit;
    }

    @Override
    public void run() {
        super.run();
        isExit = false;
        InputStream inputStream = null;
        try {
            // mydream.pcm  44100hz   16bit  立体声
            int s_ = 44100 * 2 * (16 / 2);
            int bufferSize = s_ / 100;

            inputStream = context.getAssets().open("mydream.pcm");
            byte[] buffer = new byte[bufferSize];
            int size = 0;
            while ((size = inputStream.read(buffer, 0, bufferSize)) != -1) {
                try {
                    Thread.sleep(1000 / 100); // 10毫秒写入一次
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (videoEncodeRecode == null || isExit) {
                    Log.e("zzz", "videoEncodeRecode == null or isExit-->  break");
                    break;
                }
                videoEncodeRecode.putPcmData(buffer, size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
