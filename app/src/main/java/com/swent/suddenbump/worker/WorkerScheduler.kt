package com.swent.suddenbump.worker

import android.content.Context
import android.util.Log
import androidx.work.*
import java.util.concurrent.TimeUnit

class WorkerScheduler(private val context: Context) {

  /**
   * Schedules the LocationUpdateWorker to run periodically.
   *
   * @param uid The user ID.
   */
  fun scheduleWorker(uid: String) {
    if (uid.isEmpty()) {
      Log.d("WorkerSuddenBump", "User ID not found")
      return
    }

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
    WorkManager.getInstance(context.applicationContext)
        .enqueueUniquePeriodicWork(
            "LocationUpdateWorker", ExistingPeriodicWorkPolicy.REPLACE, workRequest)
  }

  fun unscheduleLocationUpdateWorker() {
    WorkManager.getInstance(context.applicationContext)
        .cancelUniqueWork("LocationUpdateWorker") // Use the same unique name used during scheduling
  }
}
