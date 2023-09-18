package com.musongzi.waveline.ui.business

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import com.musongzi.waveline.ui.WaveLineView
import com.musongzi.waveline.ui.WaveLineView.Companion.EMPTY_VALUE
import com.musongzi.waveline.ui.itf.IValueChange

class ProcessWaveTask(context: IDrawView) : CanvasTask(context) {
    /**
     * 上一次绘制的最后的y值/动画的起始数组
     */
    private lateinit var yValue: IntArray

    /**
     * 最终绘制的完毕终点y值/将要绘制的最后一帧的值
     */
    private lateinit var yRealValue: IntArray

    /**
     * 当前绘制的y值/动画运行中变化的y值
     */
    private lateinit var thisYValue: IntArray

    /**
     * 当时开始水纹位置
     */
    var startHeight = 0f

    /**
     * 上一次的分贝值
     */
    private var musicDbLastValue = 0

    /**
     * 横坐标的数组数量，可变
     */
    var number = 40

    /**
     * x坐标的数目
     */
    var maxSize = 0
    private val sleepTime = 25L
    private val animMoveAllTime = 400f
    private val animMoveAllTime_2 = 600
    val limiTime = 200L
    private var lastTime = 0L
    private val lineWidth = 2
    private var path = Path()
    private var pathBg = Path()
    private val lock = Object()

    var valueChange: IValueChange<Int>? = null


    fun setWaveLineColor(color: Int) {
        wavePaint.color = color
    }

    fun setWaveLineWidth(width: Float) {
        wavePaint.strokeWidth = width
    }

    fun setInnerBgColor(color: Int) {
        paintBg.color = color
    }


    /**
     * 水纹的画笔
     */
    private var wavePaint = Paint().apply {
        color = Color.parseColor("#aaaaaa")
        style = Paint.Style.STROKE
        strokeWidth = lineWidth.toFloat()

        //防抖动
        isDither = true
        //抗锯齿，降低分辨率，提高绘制效率
        isAntiAlias = true

    }

    private var paintBg = Paint().apply {
        color = Color.parseColor("#dddddd")
        alpha = (0.1f * 255).toInt()
        style = Paint.Style.FILL
        isDither = true
        //抗锯齿，降低分辨率，提高绘制效率
        isAntiAlias = true
    }

    override fun onPreprae(canvas: Canvas) {
        //构建y值数组长度，也就是x坐标个数
        val size = (getWidth() * 1.0 / number).let {
            if (it > it.toInt()) {
                (it + 2).toInt()
            } else {
                (it + 1).toInt()
            }

        }
        startHeight = if (startHeight == 0f) getHeight().toFloat() else startHeight
        maxSize = size
        yValue = IntArray(size)
        yRealValue = IntArray(size)
        thisYValue = IntArray(size)
        Log.i(WaveLineView.TAG, "onDraw: mumber = $number , size = $size , size * mumber = ${size * number}")
        reset()
        needDraw()
    }

    fun reset() {
        initYValue();
    }

    private fun initYValue() {
        if (valueChange != null) {
            for (index in yRealValue.indices) {
                yRealValue[index] = valueChange?.onValueChange(yValue[index], thisYValue[index], index, maxSize)!!
                yValue[index] = yRealValue[index]
            }
        } else {
            for (index in yValue.indices) {
                yRealValue[index] = EMPTY_VALUE
                yValue[index] = yRealValue[index]
            }
        }
    }

    fun changeValues(values: IntArray, start: Int, end: Int) {
        if (start > end || start > values.size || end == 0) {
            return
        }
        for (index in start until end) {
            yRealValue[index] = values[start]
        }
        needDraw()
    }

    fun changeSingleValue(value: Int, index: Int) {
        Log.i(TAG, "changeSingleValue: value = $value , index = $index")
        yRealValue[index] = value
        needDraw()
        invalidate()
    }

    private fun needDraw() {
        path.reset()
        var endIndex = yValue.size
        for (index in yValue.indices) {
            val newValue = yRealValue[index]
            if (newValue == WaveLineView.EMPTY_VALUE) {
                endIndex = index + 1
                break
            }
            thisYValue[index] = newValue
        }
        drawPathLine(thisYValue, endIndex)
        pathBg.reset()
        pathBg.set(path)
        if (endIndex > 0 && endIndex <= thisYValue.size) {
            pathBg.lineTo((endIndex * number).toFloat(), startHeight)
            pathBg.lineTo(0f, startHeight)
            pathBg.close()
        }
    }


    override fun drawSecond(canvas: Canvas) {
//        synchronized(lock) {
        canvas.drawPath(path, wavePaint)
        canvas.drawPath(pathBg, paintBg)
//        }
    }

    /**
     * 绘制线核心大代码
     * 具体思路是，从下标 1(x1，y1) 开始，往 2(x2,y2)，3(x3,y3) 寻找
     * 如果发现是折线这需要绘制曲线，绘制方式取3点之间的中点1到2点的中点作为起点，顶点为2点，末点为2到3之间的中点
     * 期间直线区域则直接连线连接
     *
     */
    private fun drawPathLine(yValue: IntArray, endIndex: Int): IntArray {
        var index = 1

        var x1 = 0f
        var y1: Float = getRealYvalue(yValue, 0)
        path.moveTo(x1, y1)

        var x3: Float
        var y3: Float
        var x2: Float
        var y2: Float
        while (index < endIndex) {
            x2 = (number * index).toFloat()
            y2 = getRealYvalue(yValue, index)


            val sumX1 = (number * (index - 1)).toFloat()
            val sumY1 = getRealYvalue(yValue, index - 1)

            val cX = (x1 + x2) / 2
            val cY = (y1 + y2) / 2

            if (x1 != 0f && x1 != sumX1) {
                path.quadTo(sumX1, sumY1, (sumX1 + x2) / 2, (sumY1 + y2) / 2)
//                Log.i(TAG, "sumFiveMethod: 1 path.quadTo($sumX1, $sumY1, ${(sumX1 + x2) / 2}, ${(sumY1 + y2) / 2})")
            } else {
                path.lineTo(cX, cY)
//                Log.i(TAG, "sumFiveMethod: 2 path.lineTo($cX, $cY)")
            }



            if (index + 1 >= endIndex) {
                path.lineTo(x2, y2)
//                Log.i(TAG, "sumFiveMethod: 3 path.lineTo($x2, $y2)")
                break
            }

            x3 = (number * (index + 1)).toFloat()
            y3 = getRealYvalue(yValue, index + 1)


            val aX = (x2 + x3) / 2
            val aY = (y2 + y3) / 2f

            path.quadTo(x2, y2, aX, aY)
//            Log.i(TAG, "sumFiveMethod: 4 path.quadTo($x2, $y2, $aX, $aY)")

            if (index + 2 < endIndex) {
                if ((y3 < y2 && getRealYvalue(yValue, index + 2) < y3) || (y3 > y2 && getRealYvalue(yValue, index + 2) > y3)) {
                    path.lineTo(x3, y3)
//                    Log.i(TAG, "sumFiveMethod: 5 path.lineTo($x3, $y3)")
                    x1 = x3
                    y1 = y3
                } else {
                    x1 = aX
                    y1 = aY
                }
            }

            index += 2
        }
        return yValue
    }

    /**
     * 这个值将会被写入path 的轨迹中
     */
    private fun getRealYvalue(yValue: IntArray, index: Int): Float {
        val yv = yValue[index]
        return yv.toFloat()
    }

    companion object {
        const val TAG = "ProcessWaveTask"
        const val EMPTY_VALUE = Int.MIN_VALUE
    }

}