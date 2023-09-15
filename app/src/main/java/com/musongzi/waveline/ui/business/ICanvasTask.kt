package com.musongzi.waveline.ui.business

import android.graphics.Canvas

interface ICanvasTask {

    fun drawTask(canvas: Canvas)


    val prepare:Boolean

    fun setPrepareListener(pre: IPrepare)

    interface IPrepare {
        fun prepareNow(para: Any?)
    }
}