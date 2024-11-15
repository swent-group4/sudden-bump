package com.swent.suddenbump.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

object WorkerScheduler {

  fun scheduleLocationUpdateWorker(context: Context, uid: String) {
    if (uid.isEmpty()) {
      Log.d("WorkerSuddenBump", "User ID not found")
      return
    }
    Log.d("WorkerSuddenBump", "Uid $uid ")

    val inputData = workDataOf("uid" to uid)
    val workRequest =
        PeriodicWorkRequestBuilder<LocationUpdateWorker>(15, TimeUnit.MINUTES)
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build())
            .build()

    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(
            "LocationUpdateWorker", ExistingPeriodicWorkPolicy.REPLACE, workRequest)

    Log.d("WorkerSuddenBump", "LocationUpdateWorker scheduled")
  }
}
