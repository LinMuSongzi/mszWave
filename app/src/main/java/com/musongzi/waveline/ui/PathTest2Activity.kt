package com.musongzi.waveline.ui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.musongzi.waveline.R
import com.musongzi.waveline.databinding.ActivityPathTest2Binding
import com.musongzi.waveline.ui.WaveLineView.Companion.simpleSetting

class PathTest2Activity : AppCompatActivity() {

//    lateinit var waveLineView: WaveLineView

    lateinit var dataBinding: ActivityPathTest2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_path_test2)
        dataBinding.idLineview.setInnerBgColor(ActivityCompat.getColor(this, R.color.teal_700))
        dataBinding.idLineview.setWaveLineColor(ActivityCompat.getColor(this, R.color.purple_200))
        dataBinding.idLineview.setWaveLineWidth(5f)
        dataBinding.idLineview.simpleSetting(100)


        dataBinding.idMarkLineView.canvasTask.setTextColor(Color.WHITE)
        dataBinding.idMarkLineView.canvasTask.setData(120 / 60) {
            ((it + 1) * 20).toString()
        }

    }
}