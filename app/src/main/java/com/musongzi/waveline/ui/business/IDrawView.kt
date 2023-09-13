package com.musongzi.waveline.ui.business

import android.view.ViewGroup

interface IDrawView : IHolderContext {

    fun invalidate()

    fun getLayoutParams(): ViewGroup.LayoutParams?

    fun getHeight():Int

    fun getWidth():Int
}