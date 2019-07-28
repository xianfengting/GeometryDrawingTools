package com.src_resources.geometrydrawingtools

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlin.reflect.KClass

class AppLaunchingActivity : AppCompatActivity() {

    private val logTag = this::class.simpleName

    companion object {
        const val REQUEST_CODE_APP_MAIN_ACTIVITY = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_launching)

        checkAndStartGeometryManagerService()

        // 延迟两秒启动 AppMainActivity 。
        object : Thread("AppMainActivity-LauncherThread") {
            override fun run() {
                Thread.sleep(2000)
                val intent = Intent(this@AppLaunchingActivity, AppMainActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_APP_MAIN_ACTIVITY)
            }
        }.let {
            it.isDaemon = true
            it.start()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            // 如果是 AppMainActivity 返回过来的结果（说明 AppMainActivity 结束了）。
            REQUEST_CODE_APP_MAIN_ACTIVITY -> {
                // 结束当前 Activity （退出程序）。
                finish()
            }
        }
    }

    private fun checkAndStartGeometryManagerService() {
        val info = mainApplicationObj.serviceInfoMap[GeometryManagerService::class] ?: return
        if (!info.isStarted) {
            Log.i(logTag, "GeometryManagerService isn't started. Starting it...")
            showShortDurationToast(R.string.toast_startingGeometryManagerService)
            Intent(this, GeometryManagerService::class.java).let {
                startService(it)
            }
        }
    }
}
