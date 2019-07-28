package com.src_resources.geometrydrawingtools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import java.io.Serializable
import java.lang.ref.WeakReference
import java.util.concurrent.locks.LockSupport

class AppMainActivity : AppCompatActivity(), Serializable {

    companion object {
        //
        // Constant values for Message.what
        //
        const val MESSAGE_WHAT__HANDLE_PLANE_MOVEMENT = 0x010001

        val LOG_TAG = AppMainActivity::class.simpleName
    }

    private class MyBroadcastReceiver(val outerClassObj: AppMainActivity) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null)
                throw IllegalArgumentException("Argument \"intent\" is null.")
            when (intent.action) {
                TaskExecutionActivity.ACTION__CALLBACK__ON_ACTIVITY_INITIALIZED -> {
                    LockSupport.unpark(outerClassObj.activityInitializingThread)
                }
            }
        }
    }

    private class GeometrySurfaceUpdatingThread(val outerClassObj: AppMainActivity) : Thread() {
        private class ThreadHandler(outerClassObj: GeometrySurfaceUpdatingThread) : Handler() {
            private val outerClassObjWeakRef = WeakReference<GeometrySurfaceUpdatingThread>(outerClassObj)
            private val outerClassObj: GeometrySurfaceUpdatingThread
                    get() = outerClassObjWeakRef.get() ?: throw IllegalStateException(
                            "The outer class object has already destroyed by GC..")

            override fun handleMessage(msg: Message?) {
                if (msg == null)
                    throw IllegalArgumentException("The argument mustn't be null.")
            }
        }

        private var handler: ThreadHandler? = null

        init {
            name = "GeometrySurfaceUpdatingThread-$id"
        }

        override fun run() {
            Looper.prepare()
            initHandler()
            Looper.loop()
        }

        private fun initHandler() {
            handler = ThreadHandler(this)
        }
    }

    private lateinit var mainSurfaceView: SurfaceView
    private var hasSurface = false
    private lateinit var mainSurfaceViewHolder: SurfaceHolder
    private lateinit var mPath: Path
    private lateinit var mPaint: Paint
    private val handler = Handler()
    private val broadcastReceiver = MyBroadcastReceiver(this)
    private val activityInitializingThread = Thread {
        fun doProgress() {
            for (i in 0..99) {
//                for (j in 0..99) {
//                    Thread.sleep(1)
//                    updateSubprogramName("进度:$j")
//                    updateSubprogramProgress(j)
//                }
                updateTaskProgress(i)
            }
        }
        // Wait for mainSurfaceViewHolder to be created and TaskExecutionActivity to be initialized.
        LockSupport.park()
        LockSupport.park()
        // Call mainSurfaceView initializing method.
        initMainSurfaceView()
        //
//        doProgress()
        updateTaskProgress(100)
        updateSubprogramName("界面加载完成")
        Thread.sleep(500)
        // Finish the task.
        finishTask()
    }
    private val geometryUpdatingThread = GeometrySurfaceUpdatingThread(this)
    private var mLastX = 0f
    private var mLastY = 0f
    private var mCacheBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_main)

        mainSurfaceView = findViewById(R.id.mainSurfaceView)
        mainSurfaceViewHolder = mainSurfaceView.holder
        mainSurfaceViewHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder?) {
                hasSurface = true
                LockSupport.unpark(activityInitializingThread)
            }

            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                hasSurface = false
            }
        })
        mainSurfaceView.setOnTouchListener { view, event ->
            Log.v(LOG_TAG, "Handling SurfaceView MotionEvent: $event")
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mLastX = event.x
                    mLastY = event.y
                }
                MotionEvent.ACTION_UP -> {
                    mLastX = 0f
                    mLastY = 0f
                }
                MotionEvent.ACTION_MOVE -> {
                    mainSurfaceViewHolder.lockCanvas().let {
                        if (mCacheBitmap == null) {
                            mCacheBitmap = Bitmap.createBitmap(it.width, it.height, Bitmap.Config.ARGB_8888)
                        }
                        val bitmapCanvas = Canvas(mCacheBitmap)
                        if (mCacheBitmap == null) {
                            bitmapCanvas.drawColor(Color.WHITE)
                        }
                        bitmapCanvas.drawLine(mLastX, mLastY, event.x, event.y, mPaint)
                        mLastX = event.x
                        mLastY = event.y
                        it.drawBitmap(mCacheBitmap, 0f, 0f, mPaint)
                        mainSurfaceViewHolder.unlockCanvasAndPost(it)
                    }
                }
            }
            true
        }

        mPath = Path()
        mPaint = Paint()
        mPaint.color = Color.RED
        mPaint.strokeWidth = 10f
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeJoin = Paint.Join.BEVEL

        val intentObj = Intent(this, TaskExecutionActivity::class.java)
        intentObj.putExtra(TaskExecutionActivity.EXTRA__TASK_NAME,
                resources.getString(R.string.launching))
        startActivity(intentObj)

        activityInitializingThread.start()

        registerMyBroadcastReceiver()
    }

    override fun onDestroy() {
        unregisterMyBroadcastReceiver()
        super.onDestroy()
    }

    private fun initMainSurfaceView() {
        val canvas = mainSurfaceViewHolder.lockCanvas()
        canvas.drawColor(Color.WHITE)
        mainSurfaceViewHolder.unlockCanvasAndPost(canvas)
    }

    private fun updateTaskProgress(progress: Int) {
        Intent().let {
            it.action = TaskExecutionActivity.ACTION__TASK
            // Please note that it's wrong to write like:
            // it.categories.add(TaskExecutionActivity.CATEGORY__UPDATE_PROGRESS)
            it.addCategory(TaskExecutionActivity.CATEGORY__UPDATE_PROGRESS)
            it.putExtra(TaskExecutionActivity.EXTRA__PROGRESS, progress)
            sendBroadcast(it)
        }
    }

    private fun updateSubprogramName(name: String) {
        Intent(TaskExecutionActivity.ACTION__TASK).let {
            it.addCategory(TaskExecutionActivity.CATEGORY__UPDATE_SUBPROGRAM)
            it.putExtra(TaskExecutionActivity.EXTRA__SUBPROGRAM_NAME, name)
            sendBroadcast(it)
        }
    }

    private fun updateSubprogramProgress(progress: Int) {
        Intent(TaskExecutionActivity.ACTION__TASK).let {
            it.addCategory(TaskExecutionActivity.CATEGORY__UPDATE_SUBPROGRAM)
            it.putExtra(TaskExecutionActivity.EXTRA__SUBPROGRAM_PROGRESS, progress)
            sendBroadcast(it)
        }
    }

    private fun finishTask() {
        Intent().let {
            it.action = TaskExecutionActivity.ACTION__TASK
            // Please note that it's wrong to write like:
            // it.categories.add(TaskExecutionActivity.CATEGORY__FINISH)
            it.addCategory(TaskExecutionActivity.CATEGORY__FINISH)
            sendBroadcast(it)
        }
    }

    private fun registerMyBroadcastReceiver() {
        val filter = IntentFilter(TaskExecutionActivity.ACTION__CALLBACK__ON_ACTIVITY_INITIALIZED)
        registerReceiver(broadcastReceiver, filter)
    }

    private fun unregisterMyBroadcastReceiver() {
        unregisterReceiver(broadcastReceiver)
    }
}
