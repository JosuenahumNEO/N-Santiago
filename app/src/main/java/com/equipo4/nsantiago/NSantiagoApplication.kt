package com.equipo4.nsantiago

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.equipo4.nsantiago.data.local.SessionManager
import com.equipo4.nsantiago.di.NetworkModule
import com.equipo4.nsantiago.util.AppStrings

class NSantiagoApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        AppStrings.init(this)
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient(NetworkModule.provideOkHttpClient(SessionManager(this)))
            .build()
}