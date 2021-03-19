package com.zq.opengl

import android.os.Bundle
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.zq.opengl.opengl.record.Speed
import com.zq.opengl.opengl.widget.CameraView
import com.zq.opengl.opengl.widget.RecordButton
import com.zq.opengl.opengl.widget.RecordButton.OnRecordListener

class MainActivity : AppCompatActivity(), OnRecordListener, RadioGroup.OnCheckedChangeListener {
    private lateinit var cameraView: CameraView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cameraView = findViewById(R.id.cameraView)
        val btnRecord = findViewById<RecordButton>(R.id.btn_record)
        btnRecord.setOnRecordListener(this)
        //速度
        val rgSpeed = findViewById<RadioGroup>(R.id.rg_speed)
        rgSpeed.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        when (checkedId) {
            R.id.btn_extra_slow -> cameraView.setSpeed(Speed.MODE_EXTRA_SLOW)
            R.id.btn_slow -> cameraView.setSpeed(Speed.MODE_SLOW)
            R.id.btn_normal -> cameraView.setSpeed(Speed.MODE_NORMAL)
            R.id.btn_fast -> cameraView.setSpeed(Speed.MODE_FAST)
            R.id.btn_extra_fast -> cameraView.setSpeed(Speed.MODE_EXTRA_FAST)
        }
    }

    override fun onRecordStart() {
        cameraView.startRecord()
    }

    override fun onRecordStop() {
        cameraView.stopRecord()
    }
}