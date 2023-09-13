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
import kotlin.math.log


class WaveLineView(context: Context?, attrs: AttributeSet?) : BaseWaveView(context, attrs), Runnable, LifecycleObserver {

//    override fun invalidate() {
//        super.invalidate()
//    }

    var automaticInvalidate: Boolean = true
    var pointMode: Int = UN_CLOSE_POINT

    private val defaultHeightDp = 80
    private var valuesYValueAnimator: ValueAnimator? = null

    private var lifeState = 0

    private var path = Path()

    private val lock = Object()
//    private lateinit var mRandom: java.util.Random

    var mOnDbChangeListner: OnDbChangeListner? = null

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
    private var number = 40

    /**
     * x坐标的数目
     */
    private var xMaxSize = 0
    private val sleepTime = 50L
    private val animMoveAllTime = 400f
    val limiTime = 200L
    private var lastTime = 0L
    private val lineWidth = 2


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

    fun setWaveLineWidth(width: Float) {
        wavePaint.strokeWidth = width
    }

    fun setInnerBgColor(color: Int) {
        paintBg.color = color
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onActivityResume() {
        synchronized(lock) {
            lifeState = 1
            lock.notify()
        }
        Log.i(TAG, "onActivityResume: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onActivityStop() {
        valuesYValueAnimator?.pause()
        synchronized(lock) {
            lifeState = 0
        }
        Log.i(TAG, "onActivityStop: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onActivityDeStroy() {
        synchronized(lock) {
            automaticInvalidate = false
            lock.notify()
        }
        Log.i(TAG, "onActivityDeStroy: ")
    }

    init {
        (context as? LifecycleOwner)?.apply {
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

            val state = (context as? LifecycleOwner)?.lifecycle?.currentState
            state?.apply {
                if (this != Lifecycle.State.RESUMED) {
                    return
                }
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
            changeMusicDB(field)
        }


    /**
     * 初始化[yRealValue] 与 [yValue]
     */
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
        //初始化参数，这里可以拿到高度
        if (!this::yRealValue.isInitialized) {

            //构建y值数组长度，也就是x坐标个数
            val size = (width * 1f / number).let {
                if (it > it.toInt()) {
                    (it + 2).toInt()
                } else {
                    (it + 1).toInt()
                }

            }
            xMaxSize = size
            yValue = IntArray(size)
            yRealValue = IntArray(size)
            thisYValue = IntArray(size)
            Log.i(TAG, "onDraw: mumber = $number , size = $size , size * mumber = ${size * number}")
            initYValue()
            if (automaticInvalidate) {
                Thread(this).start()
            }
        }
        synchronized(lock) {
            canvas.drawPath(path, wavePaint)
            canvas.drawPath(path, paintBg)
        }
//        for (index in 1..10) {
//            canvas.drawLine(100f * index, height - 10f, 100f * index, 0f, strokePaint)
//        }
    }

    private fun handlerPathLocation(isInvalidate: Boolean = false) {
        synchronized(lock) {
            path.reset()

            for (index in yValue.indices) {
                val newValue = getRealMixMusicDBYvalue(index)
                thisYValue[index] = newValue
            }
            sumFiveMethod(thisYValue)
            if (pointMode == UN_CLOSE_POINT) {
                path.lineTo((width + lineWidth).toFloat(), (height + lineWidth).toFloat())
                path.lineTo(-5f * lineWidth, (height + lineWidth).toFloat())
                path.close()
            }
        }
        if (isInvalidate) {
            invalidate()
        }
    }

    override fun run() {

        while (automaticInvalidate) {

            handlerPathLocation()
            if (lifeState == 0) {
                synchronized(lock) {
                    if (lifeState == 0 && automaticInvalidate) {
                        Log.i(TAG, "run: thread lock.wait()")
                        lock.wait()
                    }
                }
            } else if (lifeState == 1) {
                Log.i(TAG, "run: post invalidate()")
                post {
                    invalidate()
                }
                Thread.sleep(sleepTime)
            }
        }
        Log.i(TAG, "run: handlerPathLocation end invalidate()")
    }

    /**
     * 绘制线核心大代码
     * 具体思路是，从下标 1(x1，y1) 开始，往 2(x2,y2)，3(x3,y3) 寻找
     * 如果发现是折线这需要绘制曲线，绘制方式取3点之间的中点1到2点的中点作为起点，顶点为2点，末点为2到3之间的中点
     * 期间直线区域则直接连线连接
     *
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
//                Log.i(TAG, "sumFiveMethod: 1 path.quadTo($sumX1, $sumY1, ${(sumX1 + x2) / 2}, ${(sumY1 + y2) / 2})")
            } else {
                path.lineTo(cX, cY)
//                Log.i(TAG, "sumFiveMethod: 2 path.lineTo($cX, $cY)")
            }



            if (index + 1 >= yValue.size) {
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

            if (index + 2 < yValue.size) {
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
     * 音量改变
     */
    private fun changeMusicDB(field: Int) {

        if (musicDbLastValue == field) {
            return
        }
        musicDbLastValue = field
        if (!::yRealValue.isInitialized) {
            return
        }

        /**
         * 音量改变后，
         * 遍历[yRealValue] 给最终数值数组重新赋值
         * 然后把 当前数值[thisYValue] 数值赋值给 动画起始数组[yValue]
         */
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
     * 如果实现了 [mOnDbChangeListner] 那么自己实现数组对应下表的值算法，如果为空则默认实现算法
     *
     */
    private fun dbValueChange(db: Int, index: Int, size: Int): Int {

        return (mOnDbChangeListner?.onDbValueChange(db, index, size) ?: let {
            //将x轴拆分成5份
            val _11 = xMaxSize / 5f
            val _22 = _11 * 2
            val _33 = _11 * 3
            val _44 = _11 * 4
            val _55 = _11 * 5

            /**
             * 检查下标
             */
            if (checkPointState(index)) {
                height
            } else if (index < _11) {
                //当x在小于1/5范畴内时；对应（index,y）中 y值 = 高度(height) * 分贝的占比(db / 120f) * 随机小数范围（0.15 - 0.35）
                (startHeight - db / 120f * height * (Math.random() * 0.2 + 0.15)).toInt()
            } else if (index >= _11 && index < _44 && Math.random() > 0.80) {
                //当x标在大于等于1/5 并且 小于 4/5 && 有百分之80%概率选中的 范畴内时；
                // 对应（index,y）中 y值 = 高度(height) * 分贝的占比(db / 120f) * 随机小数范围（0.5 - 0.8）
                (startHeight - db / 120f * height * (Math.random() * 0.3 + 0.5)).toInt()
            } else if (index >= _33 && index < _55 && Math.random() > 0.6) {
                (startHeight - db / 120f * height * (Math.random() * 0.2 + 0.3)).toInt() // 0.3 - 0.5
            } else {
                (startHeight - db / 120f * height * (Math.random() + 0.15)).toInt() // 0 - 0.15
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

    /**
     * [pointMode]代表开口个末点的处理方式
     */
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

    interface ExecuteMode {

    }

}