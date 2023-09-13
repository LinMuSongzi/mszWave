package com.musongzi.waveline.ui

import android.os.Bundle
import android.util.Log
import android.view.TextureView
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.musongzi.waveline.R


class PathTestActivity : AppCompatActivity() {

    lateinit var view: View
    lateinit var waveLineView: WaveLineView
    lateinit var dbTv: TextView
    private var progress = 0f // 当前进度
    var click = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_path_test)

        view = findViewById(R.id.id_lineview)
        dbTv = findViewById(R.id.id_db_tv)
        waveLineView = findViewById(R.id.id_lineview)

        runningMusicSet()
    }

    private fun runningMusicSet() {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                Thread {
                    while (true) {
                        if (click) {
                            Thread.sleep(3000)
                            click = false
                        }

                        Thread.sleep((((Math.random() * 0.1 + 0.2) * 400).toLong()))
                        waveLineView.musicDb = (Math.random() * 120).toInt().apply {
                            runOnUiThread {
                                dbTv.text = "${this@apply} DB"
                            }
                        }
                    }
                }.start()
                owner.lifecycle.removeObserver(this)
            }
        })
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

}