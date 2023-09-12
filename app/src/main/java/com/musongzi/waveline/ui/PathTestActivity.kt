package com.musongzi.waveline.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.musongzi.waveline.R


class PathTestActivity : AppCompatActivity() {

    lateinit var view: View
    lateinit var waveLineView: WaveLineView
    private var progress = 0f // 当前进度
    var click = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_path_test)

        view = findViewById(R.id.id_lineview)

        waveLineView = findViewById(R.id.id_lineview)

        Thread {
            while (true) {
                if (click) {
                    Thread.sleep(3000)
                    click = false
                }


                waveLineView.musicDb = (Math.random() * 120).toInt()
            }
        }.start()

    }

    fun lowClick(v: View?) {
        click = true
        waveLineView.musicDb = (Math.random() * 20 + 10).toInt()
    }

    fun midClick(v: View?) {
        click = true
        waveLineView.musicDb = (Math.random() * 30 + 45).toInt()
    }

    fun hightClick(v: View?) {
        click = true
        waveLineView.musicDb = (Math.random() * 30 + 90).toInt()
    }

    override fun onResume() {
        super.onResume()
        if (view.tag == null) {
            Log.i("PathTestActivity", "onResume:11111 " + view.height)
//            view.tag = PathTestDrawable(view)
        }
    }
}