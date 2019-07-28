package com.src_resources.geometrydrawingtools

import android.app.Activity
import android.app.Application
import android.app.Service
import kotlin.reflect.KClass

class MainApplication : Application() {

    val serviceInfoMap = HashMap<KClass<out Service>, ServiceInfo>()

    override fun onCreate() {
        super.onCreate()
        serviceInfoMap.put(GeometryManagerService::class, ServiceInfo())
    }
}

val Service.mainApplicationObj: MainApplication
        get() = application as MainApplication

val Activity.mainApplicationObj: MainApplication
    get() = application as MainApplication
