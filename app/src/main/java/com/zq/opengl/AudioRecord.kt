package com.zq.opengl

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioRecord : Runnable {
    private var minBufferSize: Int = 0
    private lateinit var audioRecord: AudioRecord
    private lateinit var buffer: ByteArray
    private var sampleRateInHz = 44100


    /**
     * 开始录音
     */
    fun startRecord() {
        minBufferSize = AudioRecord.getMinBufferSize(
            sampleRateInHz, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRateInHz,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize
        )
        buffer = ByteArray(minBufferSize)
        audioRecord.startRecording()
    }

    fun release() {
        audioRecord.release()
    }

    override fun run() {
        while (audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            val len = audioRecord.read(buffer, 0, buffer.size)
            if (len > 0) {
                val frame = Frame()
                frame.buffer = ByteBuffer.allocate(len).order(ByteOrder.nativeOrder())
                frame.buffer?.put(buffer, 0, len)
                frame.size = len

            }
        }
    }


}