package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.Constants
import android.content.ContentResolver
import android.net.Uri
import android.os.Looper
import android.text.TextUtils
import androidx.work.Data


/**
 * @author chenchris on 2019/4/17.
 */
class BlurWorker(appContext: Context, param: WorkerParameters) : Worker(appContext, param) {

    private val TAG = BlurWorker::class.java.simpleName

    override fun doWork(): ListenableWorker.Result {
        val applicationContext = applicationContext

        val resourceUri = inputData.getString(Constants.KEY_IMAGE_URI)

        try {
            Log.e(TAG, "is main thread:" + (Looper.myLooper() == Looper.getMainLooper()).toString())
            //this is from drawable
//            val picture = BitmapFactory.decodeResource(
//                    applicationContext.resources,
//                    R.drawable.test)

            //this is from select image input
            if (TextUtils.isEmpty(resourceUri)) {
                Log.e(TAG, "Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }

            val picture = BitmapFactory.decodeStream(
                    applicationContext.contentResolver.openInputStream(Uri.parse(resourceUri)))

            // Blur the bitmap
            val output = WorkerUtils.blurBitmap(picture, applicationContext)

            // Write bitmap to a temp file
            val outputUri = WorkerUtils.writeBitmapToFile(applicationContext, output)

            WorkerUtils.makeStatusNotification("Output is $outputUri", applicationContext)

            // If there were no errors, return SUCCESS
            val outputData = Data.Builder()
                    .putString(Constants.KEY_IMAGE_URI, outputUri.toString())
                    .build()
            return ListenableWorker.Result.success(outputData)
        } catch (throwable: Throwable) {

            // Technically WorkManager will return Result.failure()
            // but it's best to be explicit about it.
            // Thus if there were errors, we're return FAILURE
            Log.e(TAG, "Error applying blur", throwable)
            return ListenableWorker.Result.failure()
        }
    }
}