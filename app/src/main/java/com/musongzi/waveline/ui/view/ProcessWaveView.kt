package com.musongzi.waveline.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.musongzi.waveline.ui.BaseDrawViewView
import com.musongzi.waveline.ui.business.ProcessWaveTask

class ProcessWaveView(context: Context?, attrs: AttributeSet?) : BaseDrawViewView<ProcessWaveTask>(context, attrs) {
    override fun createInstanceTask(): ProcessWaveTask {
        return ProcessWaveTask(this)
    }


}