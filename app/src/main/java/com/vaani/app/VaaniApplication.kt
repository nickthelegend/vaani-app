package com.vaani.app

import android.app.Application
import com.vaani.app.data.repository.VaaniRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VaaniApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        VaaniRepository.init(this)
    }
}
