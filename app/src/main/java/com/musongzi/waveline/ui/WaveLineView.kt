package com.musongzi.waveline.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent


class WaveLineView(context: Context?, attrs: AttributeSet?) : View(context, attrs), Runnable, LifecycleObserver {


    var pointMode: Int = UN_CLOSE_POINT

    private val defaultHeightDp = 80
    private var valuesYValueAnimator: ValueAnimator? = null
    private var resume = false
    private var path = Path()

    private val lock = Object()
    private lateinit var mRandom: java.util.Random

    var mOnDbChangeListner: OnDbChangeListner? = null

    /**
     * 上一次绘制的最后的y值
     */
    private lateinit var yValue: IntArray

    /**
     * 最终绘制的终点y值
     */
    private lateinit var yRealValue: IntArray

    /**
     * 当前绘制的y值
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
    private var number = 40
    private val sleepTime = 50L
    private val animMoveAllTime = 400f
    val limiTime = 200L
    private var lastTime = 0L
    private var lineWidth = 2


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

    private var strokePaint = Paint().apply {
        color = Color.parseColor("#cccccc")
        style = Paint.Style.STROKE
        strokeWidth = 5f
        pathEffect = DashPathEffect(floatArrayOf(3f, 10f), 0f)
        isDither = true
        //抗锯齿，降低分辨率，提高绘制效率
        //抗锯齿，降低分辨率，提高绘制效率
        isAntiAlias = true
    }

     fun setWaveLineColor(color: Int) {
        wavePaint.color = color
    }

     fun setInnerBgColor(color: Int) {

        paintBg.color = color
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onActivityResume() {
        synchronized(lock) {
            resume = true
//            valuesYValueAnimator.start()
            lock.notify()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onActivityStop() {
        valuesYValueAnimator?.pause()
        resume = false
    }

    init {
        (context as LifecycleOwner).apply {
            lifecycle.addObserver(this@WaveLineView)
        }
        if (layoutParams == null) {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, generatedDefaultHeight())
        } else {
            val size = MeasureSpec.getSize(layoutParams.height)
            if (size == 0) {
                layoutParams.height = generatedDefaultHeight()
            }
        }
    }

    private fun generatedDefaultHeight() = (defaultHeightDp * context.resources.displayMetrics.densityDpi / 160f).toInt()

    var musicDb: Int = 0
        set(value) {

            if ((context as? LifecycleOwner)?.lifecycle?.currentState != Lifecycle.State.RESUMED) {
                return
            }

            val thisTime = System.currentTimeMillis()
            if (thisTime - lastTime <= limiTime) {
                return
            }

            lastTime = thisTime

            field = if (value >= 120) {
                120
            } else if (value < 0) {
                0
            } else {
                value
            }
//            resetYvalue(field)
            changeMusicDB(field)

        }


    private fun initYValue() {
        if (!this::yValue.isInitialized) {
            return
        }
        synchronized(lock) {
            for (index in yValue.indices) {
                val sunHeight = if (startHeight == 0f) height.toFloat() else startHeight
                if (startHeight == 0f) {
                    startHeight = sunHeight
                }
                val i = (startHeight - 1).toInt() //(sunHeight - musicDb / 60f * height * 0.25f * Math.random() * 0.5f).toInt()
                yValue[index] = i //(sunHeight - ((musicDb / 120f) * Math.random() * 0.5 * height / 3f)).toInt()
                yRealValue[index] = i
            }
        }
    }


    override fun onDraw(canvas: Canvas) {
        if (!this::mRandom.isInitialized) {
            mRandom = java.util.Random()
            val size = (width * 1f / number).let {
                if (it > it.toInt()) {
                    (it + 2).toInt()
                } else {
                    (it + 1).toInt()
                }
            }
//            mumber = size
            yValue = IntArray(size)
            yRealValue = IntArray(size)
            thisYValue = IntArray(size)
            Log.i(TAG, "onDraw: mumber = $number , size = $size , size * mumber = ${size * number}")
            initYValue()
            Thread(this).start()
        }
        canvas.drawPath(path, wavePaint)
//        path.close()
        canvas.drawPath(path, paintBg)
        for (index in 1..10) {
            canvas.drawLine(100f * index, height - 10f, 100f * index, 0f, strokePaint)
        }
    }


    override fun run() {

        while (true) {
            path.reset()
            synchronized(lock) {
                for (index in yValue.indices) {
                    val newValue = getRealMixMusicDBYvalue(index)
                    thisYValue[index] = newValue
                }
                Log.i(TAG, "run: sumFiveMethod ")

                sumFiveMethod(thisYValue)
                if (pointMode == UN_CLOSE_POINT) {
                    path.lineTo((width + lineWidth).toFloat(), (height + lineWidth).toFloat())
                    path.lineTo(-5f*lineWidth, (height + lineWidth).toFloat())
                    path.close()
                }
            }

            synchronized(lock) {
                if ((context as LifecycleOwner).lifecycle.currentState != Lifecycle.State.RESUMED) {
                    lock.wait()
                } else {
                    post {
                        invalidate()
                    }
                }
            }
            Thread.sleep(sleepTime)
        }
    }

    /**
     * 绘制线核心大代码
     */
    private fun sumFiveMethod(yValue: IntArray): IntArray {
        var index = 1
        var x1 = 0f
        var y1: Float = getRealYvalue(yValue, 0)
        path.moveTo(x1, y1)

        var x3: Float
        var y3: Float
        var x2: Float
        var y2: Float
        while (index < yValue.size) {
            x2 = (number * index).toFloat()
            y2 = getRealYvalue(yValue, index)


            val sumX1 = (number * (index - 1)).toFloat()
            val sumY1 = getRealYvalue(yValue, index - 1)

            val cX = (x1 + x2) / 2
            val cY = (y1 + y2) / 2

            if (x1 != 0f && x1 != sumX1) {
                path.quadTo(sumX1, sumY1, (sumX1 + x2) / 2, (sumY1 + y2) / 2)
                Log.i(TAG, "sumFiveMethod: 1 path.quadTo($sumX1, $sumY1, ${(sumX1 + x2) / 2}, ${(sumY1 + y2) / 2})")
//                path.lineTo(x2, y2)
            } else {
                path.lineTo(cX, cY)
                Log.i(TAG, "sumFiveMethod: 2 path.lineTo($cX, $cY)")
            }



            if (index + 1 >= yValue.size) {
                path.lineTo(x2, y2)
                Log.i(TAG, "sumFiveMethod: 3 path.lineTo($x2, $y2)")
                break
            }

            x3 = (number * (index + 1)).toFloat()
            y3 = getRealYvalue(yValue, index + 1)


            val aX = (x2 + x3) / 2
            val aY = (y2 + y3) / 2f

            path.quadTo(x2, y2, aX, aY)
            Log.i(TAG, "sumFiveMethod: 4 path.quadTo($x2, $y2, $aX, $aY)")

            if (index + 2 < yValue.size) {
                if ((y3 < y2 && getRealYvalue(yValue, index + 2) < y3) || (y3 > y2 && getRealYvalue(yValue, index + 2) > y3)) {
                    path.lineTo(x3, y3)
                    Log.i(TAG, "sumFiveMethod: 5 path.lineTo($x3, $y3)")
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
     * 音量改变
     */
    private fun changeMusicDB(field: Int) {
        if (musicDbLastValue == field) {
            return
        }



        if (!::yRealValue.isInitialized) {
            return
        }

        synchronized(lock) {
            for (index in yRealValue.indices) {
                yRealValue[index] = dbValueChange(field, index, yRealValue.size)
                yValue[index] = thisYValue[index]
            }
        }
        Log.i(TAG, "changeMusicDB: musicdb = $field")
    }

    /**
     * 赋值最终的[yRealValue]的y值
     */
    private fun dbValueChange(db: Int, index: Int, size: Int): Int {

        return (mOnDbChangeListner?.onDbValueChange(db, index, size) ?: let {
            val _11 = yRealValue.size / 5f
            val _22 = _11 * 2
            val _33 = _11 * 3
            val _44 = _11 * 4
            val _55 = _11 * 5
            if (checkPointState(index)) {
                height
            } else if (index <= _11) {
                (startHeight - db / 100f * height * (Math.random() * 0.2 + 0.15)).toInt() // 0.15 - 0.35
            } else if (index >= _11 && index < _44 && Math.random() > 0.80) {
                (startHeight - db / 100f * height * (Math.random() * 0.3 + 0.5)).toInt()//0.5 - 0.8
            } else if (index >= _33 && index < _55 && Math.random() > 0.6) {
                (startHeight - db / 100f * height * (Math.random() * 0.2 + 0.3)).toInt() // 0.3 - 0.5
            } else {
                (startHeight - db / 100f * height * (Math.random() + 0.15)).toInt() // 0 - 0.15
            }
        }).let {
            if (it >= height) {
                height - lineWidth
            } else if (it <= 0) {
                0 + lineWidth
            } else {
                it
            }
        }

    }

    private fun checkPointState(index: Int): Boolean {
        return when (pointMode) {
            CLOSE_START_AND_END_POINT -> {
                index == 0 || (number * (index + 1)) >= width
            }

            CLOSE_START_POINT -> {
                index == 0
            }

            CLOSE_END_POINT -> {
                (number * (index + 1)) >= width
            }

            else -> {
                false
            }
        }
    }


    /**
     * 这个值将会被写入path 的轨迹中
     */
    private fun getRealYvalue(yValue: IntArray, index: Int): Float {
        val yv = yValue[index]
        return yv.toFloat()
    }

    private fun getRealMixMusicDBYvalue(index: Int): Int {
        var old = yValue[index]
        var new = yRealValue[index]
        val thisv = thisYValue[index]

        val checkValue = new - thisv

        if (thisv < 2 && thisv > -2) {
            return new
        }
        return (thisYValue[index] + checkValue / animMoveAllTime * sleepTime).toInt()
    }


    companion object {
        const val TAG = "PathTestDrawable"
        const val UN_CLOSE_POINT = 0
        const val CLOSE_START_POINT = 1
        const val CLOSE_START_AND_END_POINT = 2
        const val CLOSE_END_POINT = 3
    }

    interface OnDbChangeListner {
        fun onDbValueChange(db: Int, index: Int, size: Int): Int
    }

}