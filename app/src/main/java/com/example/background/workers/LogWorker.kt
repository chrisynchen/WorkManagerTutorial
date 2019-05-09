package com.example.background.workers

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.Constants

/**
 * @author chenchris on 2019/5/9.
 */
class LogWorker(appContext: Context, param: WorkerParameters) : Worker(appContext, param) {
    private val TAG = LogWorker::class.java.simpleName

    override fun doWork(): ListenableWorker.Result {
        var counter = inputData.getInt(Constants.KEY_COUNTER, 0)
        Log.i(TAG, "counter:$counter")
        val counterData = Data.Builder()
                .putInt(Constants.KEY_COUNTER, ++counter)
                .build()
        return ListenableWorker.Result.success(counterData)
    }
}