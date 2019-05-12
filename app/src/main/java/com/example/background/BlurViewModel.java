/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.example.background.workers.BlurWorker;
import com.example.background.workers.CleanupWorker;
import com.example.background.workers.LogWorker;
import com.example.background.workers.RxSampleWorker;
import com.example.background.workers.SaveImageToFileWorker;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import static com.example.background.Constants.IMAGE_MANIPULATION_WORK_NAME;
import static com.example.background.Constants.KEY_IMAGE_URI;
import static com.example.background.Constants.TAG_OUTPUT;
import static com.example.background.Constants.TAG_SAVE;

public class BlurViewModel extends ViewModel {

    private static final String TAG = BlurViewModel.class.getSimpleName();

    private Uri mImageUri;

    private WorkManager mWorkManager;

    private LiveData<List<WorkInfo>> mSavedWorkInfo;

    public BlurViewModel() {
        mWorkManager = WorkManager.getInstance();
        mSavedWorkInfo = mWorkManager.getWorkInfosByTagLiveData(TAG_OUTPUT);
    }

    /**
     * Creates the input data bundle which includes the Uri to operate on
     *
     * @return Data which contains the Image Uri as a String
     */
    private Data createInputDataForUri() {
        Data.Builder builder = new Data.Builder();
        if (mImageUri != null) {
            builder.putString(KEY_IMAGE_URI, mImageUri.toString());
        }
        return builder.build();
    }

    private Data createLogCounter() {
        Data.Builder builder = new Data.Builder();
        builder.putInt(KEY_IMAGE_URI, 0);
        return builder.build();
    }

    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     *
     * @param blurLevel The amount to blur the image
     */
    void applyBlur(int blurLevel) {

//        WorkContinuation continuation = mWorkManager.beginWith(OneTimeWorkRequest.from(CleanupWorker.class));

        //beginUniqueWork run in sequence
        WorkContinuation continuation = mWorkManager
                .beginUniqueWork(IMAGE_MANIPULATION_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        OneTimeWorkRequest.from(CleanupWorker.class));

        for (int i = 0; i < blurLevel; i++) {

            OneTimeWorkRequest.Builder blurBuilder =
                    new OneTimeWorkRequest.Builder(BlurWorker.class);

            // Input the Uri if this is the first blur operation
            // After the first blur operation the input will be the output of previous
            // blur operations.
            if (i == 0) {
                blurBuilder.setInputData(createInputDataForUri());
            }

            continuation = continuation.then(blurBuilder.build());
        }

        OneTimeWorkRequest save =
                new OneTimeWorkRequest.Builder(SaveImageToFileWorker.class)
                        .addTag(TAG_OUTPUT)
                        .build();
        continuation = continuation.then(save);

        // Actually start the work
        continuation.enqueue();
    }

    void applyBlurOneTime() {

        OneTimeWorkRequest blurWorkRequest =
                new OneTimeWorkRequest.Builder(BlurWorker.class)
                        .setConstraints(new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .setRequiresCharging(true).build())
                        .setInputData(createInputDataForUri()).build();

        OneTimeWorkRequest save =
                new OneTimeWorkRequest.Builder(SaveImageToFileWorker.class)
                        .addTag(TAG_SAVE)
                        .setInitialDelay(5, TimeUnit.SECONDS)
                        .build();

        mWorkManager.beginWith(blurWorkRequest).then(save).enqueue();
        ListenableFuture<WorkInfo> blurWorkInfo = mWorkManager.getWorkInfoById(blurWorkRequest.getId());
        ListenableFuture<WorkInfo> saveWorkInfo = mWorkManager.getWorkInfoById(save.getId());
        try {
            Log.i(TAG, "saveWorkInfo:" + saveWorkInfo.get().getState().toString());
            Log.i(TAG, "blurWorkInfo:" + blurWorkInfo.get().getState().toString());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void startLogWork() {

        //PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS
//        PeriodicWorkRequest periodicWorkRequest =
//                new PeriodicWorkRequest.Builder(LogWorker.class, 3, TimeUnit.SECONDS)
//                        .setInputData(createLogCounter())
//                        .build();
//        mWorkManager.enqueue(periodicWorkRequest);

        OneTimeWorkRequest rxSampleRequest =
                new OneTimeWorkRequest.Builder(RxSampleWorker.class)
                        .setInputData(createLogCounter()).build();
        mWorkManager.enqueue(rxSampleRequest);
    }

    private Uri uriOrNull(String uriString) {
        if (!TextUtils.isEmpty(uriString)) {
            return Uri.parse(uriString);
        }
        return null;
    }

    /**
     * Setters
     */
    void setImageUri(String uri) {
        mImageUri = uriOrNull(uri);
    }

    /**
     * Getters
     */
    Uri getImageUri() {
        return mImageUri;
    }

    LiveData<List<WorkInfo>> getOutputWorkInfo() { return mSavedWorkInfo; }
}