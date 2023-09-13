package com.musongzi.waveline.ui

import android.app.Application

class MyApplication : Application() {


    companion object {
        val application: Application
            get() {
                return applicationInstance
            }
        private lateinit var applicationInstance: Application
    }

    override fun onCreate() {
        super.onCreate()
        applicationInstance = this
    }


}