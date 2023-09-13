package com.musongzi.waveline.ui

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.musongzi.waveline.ui.business.IDrawView
import com.musongzi.waveline.ui.business.ICanvasTask

abstract class BaseDrawViewView<T : ICanvasTask>(context: Context?, attrs: AttributeSet?) : View(context, attrs), IDrawView {

    val canvasTask: T
        get() {
            return nativeTask
        }
    private var nativeTask: T

    init {
        nativeTask = createInstanceTask()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvasTask.draw(canvas)
    }


    protected abstract fun createInstanceTask(): T


    override fun getHodlerContext(): Context {
        return context
    }

}