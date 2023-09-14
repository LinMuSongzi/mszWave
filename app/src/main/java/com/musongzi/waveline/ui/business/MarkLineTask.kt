package com.musongzi.waveline.ui.business

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.view.View

class MarkLineTask(canvas: IDrawView) : CanvasTask(canvas) {


    private val textPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        textSize = (getHodlerContext().resources.displayMetrics.densityDpi / 160 * 15).toFloat()
        isDither = true
        //抗锯齿，降低分辨率，提高绘制效率
        isAntiAlias = true
    }


    private var strokePaint = Paint().apply {
        color = Color.parseColor("#cccccc")
        style = Paint.Style.STROKE
        strokeWidth = 5f
        pathEffect = DashPathEffect(floatArrayOf(3f, 15f), 0f)
        isDither = true
        //抗锯齿，降低分辨率，提高绘制效率
        //抗锯齿，降低分辨率，提高绘制效率
        isAntiAlias = true
    }

    private var valueArray: Array<CharSequence>? = null

    override fun draw(canvas: Canvas) {
        valueArray?.apply {

            for (index in indices) {
                canvas.drawText(this[index] as String, 0f, getHeight() * 1f / size * (index + 1), textPaint)
            }

        }
    }

    fun setData(size: Int, run: (Int) -> CharSequence) {
        valueArray = Array(size, run)

//        val height = View.MeasureSpec.getSize(getLayoutParams()!!.height)
//        height / size

        invalidate()
    }

    fun setTextColor(white: Int) {
        textPaint.color = white
    }

}