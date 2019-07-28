package com.src_resources.geometrydrawingtools

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class GeometryManagerService : Service() {

    private val logTag = this::class.simpleName

    override fun onCreate() {
        var info = mainApplicationObj.serviceInfoMap[this::class]
        if (info == null) {
            Log.i(logTag, "ServiceInfo of this service is null. " +
                    "Creating ServiceInfo by itself.")
            info = ServiceInfo()
            mainApplicationObj.serviceInfoMap.put(this::class, info)
        }
        info.isStarted = true
        Log.i(logTag, "${this::class.simpleName} started.")
    }

    override fun onDestroy() {
        val info = mainApplicationObj.serviceInfoMap[this::class]
        info?.isStarted = false
        Log.i(logTag, "${this::class.simpleName} stopped.")
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }
}
