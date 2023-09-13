package com.musongzi.waveline.ui

import android.content.Context
import android.util.AttributeSet
import com.musongzi.waveline.ui.business.MarkLineTask

/**
 * 刻度
 */
class MarkLineView(context: Context, attrs: AttributeSet?) : BaseDrawViewView<MarkLineTask>(context, attrs) {

    override fun createInstanceTask(): MarkLineTask {
        return MarkLineTask(this)
    }


}