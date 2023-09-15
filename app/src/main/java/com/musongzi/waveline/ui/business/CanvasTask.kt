package com.musongzi.waveline.ui.business

import android.graphics.Canvas

abstract class CanvasTask(private val context: IDrawView) : ICanvasTask, IDrawView by context {

    private var pre: ICanvasTask.IPrepare? = null

    override val prepare: Boolean
        get() {
            return real_prepare
        }

    private var real_prepare = false


    override fun setPrepareListener(pre: ICanvasTask.IPrepare) {
        this.pre = pre
    }

    final override fun drawTask(canvas: Canvas) {
        if(!prepare){
            real_prepare = true
            onPreprae(canvas)
            pre?.prepareNow(getPreparePara())
        }
        drawSecond(canvas)
    }

    protected abstract fun onPreprae(canvas: Canvas)

    abstract fun drawSecond(canvas: Canvas)


    fun getPreparePara(): Any? = null

}