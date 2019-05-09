package com.example.background.workers

import android.content.Context
import android.util.Log
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

/**
 * @author chenchris on 2019/5/9.
 */
class RxSampleWorker(appContext: Context, param: WorkerParameters) : RxWorker(appContext, param) {

    private val TAG = RxSampleWorker::class.java.simpleName

    override fun createWork(): Single<Result> {
        return Observable.range(0, 100)
                .doOnNext {
                    Log.i(TAG, it.toString())
                }
                .toList()
                .map { Result.success() }
    }

    override fun getBackgroundScheduler(): Scheduler {
        return Schedulers.computation()
    }
}