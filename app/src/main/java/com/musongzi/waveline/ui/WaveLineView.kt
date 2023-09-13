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
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent


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
    private var pathBg = Path()
    private val lock = Object()
//    private lateinit var mRandom: java.util.Random

    var waveCallBack: WaveCallBack? = null

    var executeListner: ExecuteListner? = null

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
    private val animMoveAllTime_2 = 600
    val limiTime = 200L
    private var lastTime = 0L
    private val lineWidth = 2

    private val pathThread = Thread(this, "wavePathName")


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
//        startHeight = MeasureSpec.getSize(layoutParams.height).toFloat()
    }

    private fun generatedDefaultHeight() = (defaultHeightDp * context.resources.displayMetrics.densityDpi / 160f).toInt()

//    private var musicDb: Int = 0
//        set(value) {
//
//
//            changeMusicDB(field)
//        }

//    fun change

    /**
     * 初始化[yRealValue] 与 [yValue]
     */
    private fun initYValue() {
        if (!this::yValue.isInitialized) {
            return
        }
        synchronized(lock) {
            var i = 0
            val startHeight = (if (startHeight == 0f) height.apply {
                startHeight = this.toFloat()
            } else startHeight).toInt()
            Log.i(TAG, "initYValue: startHeight = $startHeight")
            for (index in yValue.indices) {
                i = if (waveCallBack == null) {
                    startHeight - 1
                } else {
                    startHeight - waveCallBack!!.initRealValue(index)
                }
                Log.i(TAG, "initYValue: index = $index , value = $i")
                yValue[index] = i
                yRealValue[index] = i
            }
        }
    }


    override fun onDraw(canvas: Canvas) {
        for (index in 1..xMaxSize) {
            canvas.drawLine(100f * index, height - 10f, 100f * index, 0f, strokePaint)
        }
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
            if (automaticInvalidate && !pathThread.isInterrupted) {
                pathThread.start()
            }
        }
        synchronized(lock) {
            canvas.drawPath(path, wavePaint)
            canvas.drawPath(pathBg, paintBg)
        }

    }

//    fun


    fun handlerPathLocationReal() {
        synchronized(lock) {
            automaticInvalidate = false
            path.reset()
            var endIndex = yValue.size
            for (index in yValue.indices) {
                val newValue = yRealValue[index]
                if (newValue == Int.MIN_VALUE) {
                    endIndex = index + 1
                    break
                }
                thisYValue[index] = newValue
            }
            sumFiveMethod(thisYValue, endIndex)
            if (pointMode == UN_CLOSE_POINT) {
                path.lineTo((width + lineWidth).toFloat(), (height + lineWidth).toFloat())
                path.lineTo(-5f * lineWidth, (height + lineWidth).toFloat())
                path.close()
            }
        }
        invalidate()
    }

    private fun handlerPathLocation() {
        synchronized(lock) {
            path.reset()
            var endIndex = yValue.size
            for (index in yValue.indices) {
                val newValue = getRealMixMusicDBYvalue(index)
                if (newValue == Int.MIN_VALUE) {
                    endIndex = index + 1
                    break
                }
                thisYValue[index] = newValue
            }
            sumFiveMethod(thisYValue, endIndex)
            if (pointMode == UN_CLOSE_POINT) {
                pathBg.set(path)
                val lastHeight = yRealValue[endIndex - 1].toFloat()
                pathBg.lineTo((width + lineWidth).toFloat(), lastHeight)
                pathBg.lineTo((width + lineWidth).toFloat(), startHeight)
                pathBg.lineTo(-2f * lineWidth, startHeight)
                pathBg.close()
            }
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

                if (System.currentTimeMillis() - lastTime >= animMoveAllTime_2) {
                    synchronized(lock) {
                        Log.i(TAG, "run: post invalidate() but no busy")
                        lock.wait()
                    }
                    if (lifeState == 0) {
                        break
                    }
                }
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
    private fun sumFiveMethod(yValue: IntArray, endIndex: Int): IntArray {
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
    private fun changeMusicDB(value: Int) {

        val state = (context as? LifecycleOwner)?.lifecycle?.currentState
        state?.apply {
            if (this != Lifecycle.State.RESUMED) {
                return
            }
        }


        val thisTime = System.currentTimeMillis()
        val sum = thisTime - lastTime
        if (sum <= limiTime) {
            return
        } else if (sum >= animMoveAllTime_2) {
            synchronized(lock) {
                lock.notify()
            }
        }

        lastTime = thisTime

        val field = if (value >= 120) {
            120
        } else if (value < 0) {
            0
        } else {
            value
        }

        if (musicDbLastValue == field) {
            return
        }
        musicDbLastValue = field
        if (!::yRealValue.isInitialized) {
            Log.i(TAG, "changeMusicDB: yRealValue is not Initialize ")
            return
        }

        /**
         * 音量改变后，
         * 遍历[yRealValue] 给最终数值数组重新赋值
         * 然后把 当前数值[thisYValue] 数值赋值给 动画起始数组[yValue]
         */
        synchronized(lock) {
            for (index in yRealValue.indices) {
                yRealValue[index] = dbValueChange(field, yRealValue[index], index, yRealValue.size)
                yValue[index] = thisYValue[index]
            }
        }
        executeListner?.onDbValueChange()
        Log.i(TAG, "changeMusicDB: musicdb = $field")
    }

    private fun defaultValueChange(db: Int, lastValues: Int, index: Int, size: Int): Int {

        //将x轴拆分成5份
        val _11 = xMaxSize / 5f
        val _22 = _11 * 2
        val _33 = _11 * 3
        val _44 = _11 * 4
        val _55 = _11 * 5

        /**
         * 检查下标
         */
        val value = if (checkPointState(index)) {
            height
        } else if (index < _11) {
            //当x在小于1/5范畴内时；对应（index,y）中 y值 = 高度(height) * 分贝的占比(db / 120f) * 随机小数范围（0.15 - 0.35）
            (startHeight - db / 90f * height * (Math.random() * 0.3 + 0.6)).toInt()
        } else if (index >= _11 && index < _44) {
            //当x标在大于等于1/5 并且 小于 4/5 && 有百分之80%概率选中的 范畴内时；
            // 对应（index,y）中 y值 = 高度(height) * 分贝的占比(db / 120f) * 随机小数范围（0.5 - 0.8）
            (startHeight - db / 90f * height * (Math.random() * 0.3 + 0.5)).toInt()
        } else if (index >= _44) {
            (startHeight - db / 90f * height * (Math.random() * 0.2 + 0.4)).toInt() // 0.3 - 0.5
        } else {
            (startHeight - db / 90f * height * (Math.random() + 0.8)).toInt() // 0 - 0.15
        }

        Log.i(TAG, "defaultValueChange: db = $db , value = $value , startHeight = $startHeight , height = $height")
        return value

    }

    /**
     * 赋值最终的[yRealValue]的y值
     * 如果实现了 [waveCallBack] 那么自己实现数组对应下表的值算法，如果为空则默认实现算法
     *
     */
    private fun dbValueChange(db: Int, lastValues: Int, index: Int, size: Int): Int {

        return (waveCallBack?.onDbValueChange(db, lastValues, index, size) ?: defaultValueChange(db, lastValues, index, size)).let {
            if (it >= height) {
                height - lineWidth
            } else if (it <= 0) {
                (0 + Math.random() * 10).toInt()
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


        fun WaveLineView.simpleSetting(firstLSet: Int = 0): WaveCallBack {


            val callBack = object : WaveCallBack {

                override fun onDbValueChange(db: Int, lastValues: Int, index: Int, size: Int): Int {
                    return defaultValueChange(db, lastValues, index, size)
                }

                override fun initRealValue(index: Int) = 0

                override fun valueChangeByAutomaticInvalidate(value: Int) {
                    if (automaticInvalidate) {
                        changeMusicDB(value)
                    }
                }

            }

            (context as? LifecycleOwner)?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    post {
                        callBack.valueChangeByAutomaticInvalidate(firstLSet)
                    }
                    owner.lifecycle.removeObserver(this)
                }
            })

            waveCallBack = callBack

            return waveCallBack!!
        }

    }

    interface WaveCallBack {
        fun onDbValueChange(db: Int, lastValues: Int, index: Int, size: Int): Int
        fun initRealValue(index: Int): Int
        fun valueChangeByAutomaticInvalidate(value: Int)
    }

    interface ExecuteListner {
        fun onDbValueChange()
    }

}