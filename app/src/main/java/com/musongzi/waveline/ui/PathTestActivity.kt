package com.musongzi.waveline.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
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
    var click = 0

    private val lock = Object()

    val handler = Handler(Looper.getMainLooper())

    private var musicDb = -1
        set(value) {
            handler.removeCallbacksAndMessages(null)
            if (value in 0..120) {
                field = value
                waveLineView.musicDb = field
                if (Thread.currentThread() != Looper.getMainLooper().thread) {
                    runOnUiThread {
                        dbTv.text = "$value DB"
                    }
                } else {
                    dbTv.text = "$value DB"
                }
            }
        }

    val runnable减LongClick = object : Runnable {
        override fun run() {
            ++musicDb
            vibrator()
            handler.postDelayed(this, 100)
        }
    }

    val runnable加LongClick = object : Runnable {
        override fun run() {
            ++musicDb
            vibrator()
            handler.postDelayed(this, 100)
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }


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
                        if (click == 1) {
                            synchronized(lock) {
                                if (click == 1) {
                                    lock.wait()
                                }
                            }
                        }

                        Thread.sleep((((Math.random() * 0.1 + 0.2) * 400).toLong()))
                        (Math.random() * 120).toInt().apply {
                            musicDb = this
                        }
                    }
                }.start()
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    fun lowClick(v: View?) {
        synchronized(lock) {
            click = 1
        }
        (Math.random() * 20 + 10).toInt().apply {
            musicDb = this
        }
    }

    fun midClick(v: View?) {
        synchronized(lock) {
            click = 1
        }
        (Math.random() * 30 + 45).toInt().apply {
            musicDb = this
        }
    }

    fun hightClick(v: View?) {
        synchronized(lock) {
            click = 1
        }
        (Math.random() * 30 + 90).toInt().apply {
            musicDb = this
        }
    }

    fun autoClick(v: View?) {
        synchronized(lock) {
            click = 0
            lock.notify()
        }
    }

    fun jiaClick(v: View?) {
        synchronized(lock) {
            click = 1
        }
        vibrator()
        ++musicDb
    }

    fun jianClick(v: View?) {
        synchronized(lock) {
            click = 1
        }
        vibrator()
        --musicDb
    }

    private fun vibrator() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(50)
    }


}