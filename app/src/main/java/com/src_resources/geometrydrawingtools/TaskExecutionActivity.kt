package com.src_resources.geometrydrawingtools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
//import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference

class TaskExecutionActivity : AppCompatActivity() {

    companion object {
        //
        // Constant values for Intent extra data
        //
        const val EXTRA__TASK_NAME = "extra_taskName"
        const val EXTRA__PROGRESS = "extra_progress"
        const val EXTRA__SUBPROGRAM_NAME = "extra_subprogramName"
        const val EXTRA__SUBPROGRAM_PROGRESS = "extra_subprogramProgress"

        //
        // Constant values for broadcast actions and categories
        //
        const val ACTION__TASK = "com.src_resources.geometrydrawingtools.TaskExecutionActivity.ACTION__TASK"
        const val ACTION__CALLBACK__ON_ACTIVITY_INITIALIZED =
                "com.src_resources.geometrydrawingtools.TaskExecutionActivity.ACTION__CALLBACK__ON_ACTIVITY_INITIALIZED"
        const val CATEGORY__UPDATE_PROGRESS = "com.src_resources.geometrydrawingtools.TaskExecutionActivity.CATEGORY__UPDATE_PROGRESS"
        const val CATEGORY__FINISH = "com.src_resources.geometrydrawingtools.TaskExecutionActivity.CATEGORY__FINISH"
        const val CATEGORY__UPDATE_SUBPROGRAM = "com.src_resources.geometrydrawingtools.TaskExecutionActivity.CATEGORY__CATEGORY__UPDATE_SUBPROGRAM"
        val ACTIONS = arrayOf(ACTION__TASK, ACTION__CALLBACK__ON_ACTIVITY_INITIALIZED)
        val CATEGORIES = arrayOf(CATEGORY__UPDATE_PROGRESS, CATEGORY__FINISH, CATEGORY__UPDATE_SUBPROGRAM)

        //
        // Constant values for Message.what
        //
        private const val MESSAGE_WHAT__UPDATE_PROGRESS = 1
        private const val MESSAGE_WHAT__UPDATE_SUBPROGRAM_NAME = 2
        private const val MESSAGE_WHAT__UPDATE_SUBPROGRAM_PROGRESS = 3
        private const val MESSAGE_WHAT__EXIT = 4
    }

    private class MyHandler(outerClassObj: TaskExecutionActivity) : Handler() {
        private val outerClassWeakRef = WeakReference(outerClassObj)
        private val outerClassObj: TaskExecutionActivity
                get() = outerClassWeakRef.get() ?: throw NullPointerException(
                            "The outer class object has already destroyed by GC.")

        override fun handleMessage(msg: Message?) {
            if (msg == null)
                throw IllegalArgumentException("Use null object to call this method.")
            Log.v("TaskExecutionActivity", "Received handler message: $msg")
            when (msg.what) {
                MESSAGE_WHAT__UPDATE_PROGRESS -> {
                    val progress = msg.arg1
                    outerClassObj.taskProgressBar.progress = progress
                }
                MESSAGE_WHAT__UPDATE_SUBPROGRAM_NAME -> {
                    val name = msg.data.getString(EXTRA__SUBPROGRAM_NAME,
                            outerClassObj.currentSubprogramTextView.text.toString())
                    outerClassObj.currentSubprogramTextView.text = name
                }
                MESSAGE_WHAT__UPDATE_SUBPROGRAM_PROGRESS -> {
                    val progress = msg.arg1
                    outerClassObj.currentSubprogramProgressBar.progress = progress
                }
                MESSAGE_WHAT__EXIT -> {
                    outerClassObj.exit()
                }
            }
        }
    }

    private class MyBroadcastReceiver(val outerClassObj: TaskExecutionActivity) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null)
                throw IllegalArgumentException("Argument \"intent\" is null.")
            Log.v("TaskExecutionActivity", "Received task-request broadcast: $intent")
            if (intent.categories.contains(CATEGORY__UPDATE_PROGRESS)) {
                val progress = intent.getIntExtra(EXTRA__PROGRESS, 0)
                outerClassObj.updateProgress(progress)
            }
            if (intent.categories.contains(CATEGORY__UPDATE_SUBPROGRAM)) {
                val name = intent.getStringExtra(EXTRA__SUBPROGRAM_NAME)
                val progress = intent.getIntExtra(EXTRA__SUBPROGRAM_PROGRESS, 0)
                outerClassObj.updateSubprogramName(name)
                outerClassObj.updateSubprogramProgress(progress)
            }
            if (intent.categories.contains(CATEGORY__FINISH)) {
                outerClassObj.exit()
            }
        }
    }

    private lateinit var taskNameTextView: TextView
    private lateinit var taskProgressBar: ProgressBar
    private lateinit var currentSubprogramTextView: TextView
    private lateinit var currentSubprogramProgressBar: ProgressBar

    private val handler = MyHandler(this)
    private val broadcastReceiver = MyBroadcastReceiver(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_execution)

        title = intent.getStringExtra(EXTRA__TASK_NAME)

        taskNameTextView = findViewById(R.id.taskNameTextView)
        taskNameTextView.text = intent.getStringExtra(EXTRA__TASK_NAME)
        taskProgressBar = findViewById(R.id.taskProgressBar)
        currentSubprogramTextView = findViewById(R.id.currentSubprogramTextView)
        currentSubprogramProgressBar = findViewById(R.id.currentSubprogramProgressBar)

        registerMyBroadcastReceiver()

        sendActivityInitializationFinishingBroadcast()
    }

    override fun onDestroy() {
        unregisterMyBroadcastReceiver()
        super.onDestroy()
    }

    private fun registerMyBroadcastReceiver() {
        val filter = IntentFilter(ACTION__TASK)
        CATEGORIES.forEach {
            filter.addCategory(it)
        }
        registerReceiver(broadcastReceiver, filter)
    }

    private fun unregisterMyBroadcastReceiver() {
        unregisterReceiver(broadcastReceiver)
    }

    private fun sendActivityInitializationFinishingBroadcast() {
        Log.d("TaskExecutionActivity", "Sending activity initialization finishing broadcast.")
        val intentObj = Intent(ACTION__CALLBACK__ON_ACTIVITY_INITIALIZED)
        sendBroadcast(intentObj)
    }

    private fun updateProgress(progress: Int) {
        handler.obtainMessage(MESSAGE_WHAT__UPDATE_PROGRESS).let {
            it.arg1 = progress
            handler.sendMessage(it)
        }
    }

    private fun updateSubprogramName(name: String?) {
        val bundle = Bundle()
        bundle.putString(EXTRA__SUBPROGRAM_NAME, name)
        handler.obtainMessage(MESSAGE_WHAT__UPDATE_SUBPROGRAM_NAME).let {
            it.data = bundle
            handler.sendMessage(it)
        }
    }

    private fun updateSubprogramProgress(progress: Int) {
        handler.obtainMessage(MESSAGE_WHAT__UPDATE_SUBPROGRAM_PROGRESS).let {
            it.arg1 = progress
            handler.sendMessage(it)
        }
    }

    private fun exit() {
        finish()
    }
}
