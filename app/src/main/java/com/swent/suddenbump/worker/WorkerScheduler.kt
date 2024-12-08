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

    WorkManager.getInstance(context.applicationContext)
        .enqueueUniquePeriodicWork(
            "LocationUpdateWorker", ExistingPeriodicWorkPolicy.REPLACE, workRequest)
  }

  fun unscheduleLocationUpdateWorker() {
    WorkManager.getInstance(context.applicationContext)
        .cancelUniqueWork("LocationUpdateWorker") // Use the same unique name used during scheduling
  }
}
