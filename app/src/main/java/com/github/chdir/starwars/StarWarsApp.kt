package com.github.chdir.starwars;

import android.app.Application
import io.reactivex.rxjava3.plugins.RxJavaPlugins

class StarWarsApp : Application() {
    companion object {
        lateinit var INSTANCE : StarWarsApp
    }

    override fun onCreate() {
        super.onCreate()

        // prevent crashes when unhandled exception happens
        RxJavaPlugins.setErrorHandler { it.printStackTrace() }

        INSTANCE = this
    }
}
