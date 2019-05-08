package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.background.Constants
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author chenchris on 2019/4/18.
 */
class SaveImageToFileWorker(appContext: Context, param: WorkerParameters) : Worker(appContext, param) {

    private val TAG = SaveImageToFileWorker::class.java.simpleName
    private val TITLE = "Blurred Image"
    private val DATE_FORMATTER = SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z", Locale.getDefault())

    override fun doWork(): ListenableWorker.Result {
        val applicationContext = applicationContext

        val resolver = applicationContext.contentResolver
        try {
            val resourceUri = inputData
                    .getString(Constants.KEY_IMAGE_URI)
            val bitmap = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)))
            val outputUri = MediaStore.Images.Media.insertImage(
                    resolver, bitmap, TITLE, DATE_FORMATTER.format(Date()))
            if (TextUtils.isEmpty(outputUri)) {
                Log.e(TAG, "Writing to MediaStore failed")
                return ListenableWorker.Result.failure()
            }
            val outputData = Data.Builder()
                    .putString(Constants.KEY_IMAGE_URI, outputUri)
                    .build()
            return ListenableWorker.Result.success(outputData)
        } catch (exception: Exception) {
            Log.e(TAG, "Unable to save image to Gallery", exception)
            return ListenableWorker.Result.failure()
        }

    }
}