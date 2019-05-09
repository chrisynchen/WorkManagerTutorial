package com.example.background.workers

import android.content.Context
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.Constants.OUTPUT_PATH
import java.io.File


/**
 * @author chenchris on 2019/4/18.
 */
class CleanupWorker(appContext: Context, param: WorkerParameters) : Worker(appContext, param) {

    private val TAG = CleanupWorker::class.java.simpleName

    override fun doWork(): ListenableWorker.Result {
        return try {
//            Handler().postDelayed({
                busyWork(applicationContext)
//            }, 5000)

            ListenableWorker.Result.success()
        } catch (exception: Exception) {
            Log.e(TAG, "Error cleaning up", exception)
            ListenableWorker.Result.failure()
        }
    }

    private fun busyWork(appContext: Context) {
        val outputDirectory = File(appContext.filesDir,
                OUTPUT_PATH)
        if (outputDirectory.exists()) {
            val entries = outputDirectory.listFiles()
            if (!entries.isNullOrEmpty()) {
                for (entry in entries) {
                    val name = entry.name
                    if (!TextUtils.isEmpty(name) && name.endsWith(".png")) {
                        val deleted = entry.delete()
                        Log.i(TAG, String.format("Deleted %s - %s",
                                name, deleted))
                    }
                }
            }
        }

        Log.i(TAG, "CleanupWorker finished")
    }
}