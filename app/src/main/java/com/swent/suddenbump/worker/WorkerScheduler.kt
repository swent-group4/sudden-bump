package com.swent.suddenbump.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

/** Object responsible for scheduling the LocationUpdateWorker. */
object WorkerScheduler {

  /**
   * Schedules the LocationUpdateWorker to run periodically.
   *
   * @param context The application context.
   * @param uid The user ID.
   */
  fun scheduleLocationUpdateWorker(context: Context, uid: String) {
    if (uid.isEmpty()) {
      Log.d("WorkerSuddenBump", "User ID not found")
      return
    }
    Log.d("WorkerSuddenBump", "Uid $uid ")

    // Prepare input data for the worker
    val inputData = workDataOf("uid" to uid)

    // Create a periodic work request to run every 15 minutes
    val workRequest =
        PeriodicWorkRequestBuilder<LocationUpdateWorker>(15, TimeUnit.MINUTES)
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build())
            .build()

    // Enqueue the work request with a unique name to avoid duplication
    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(
            "LocationUpdateWorker", ExistingPeriodicWorkPolicy.REPLACE, workRequest)

    Log.d("WorkerSuddenBump", "LocationUpdateWorker scheduled")
  }
}
