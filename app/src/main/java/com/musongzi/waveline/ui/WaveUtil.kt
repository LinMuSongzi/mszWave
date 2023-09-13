package com.musongzi.waveline.ui

object WaveUtil {










    internal class PointWaveCallBack(var waveLineView: WaveLineView) : WaveLineView.WaveCallBack {




        override fun initRealValue(index: Int) = WaveLineView.EMPTY_VALUE

        override fun onValueChange(db: Int, lastValues: Int, index: Int, size: Int): Int {
            TODO("Not yet implemented")
        }

        override fun changeValuesAndInvalidate(value: Int) {

        }

        override fun onFirstDraw(size: Int) {

        }

    }

}