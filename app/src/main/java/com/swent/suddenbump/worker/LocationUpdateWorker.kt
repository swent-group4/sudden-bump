package com.swent.suddenbump.worker

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.swent.suddenbump.R
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/**
 * Worker class for updating the user's location in the background.
 *
 * @param context The application context.
 * @param workerParams Parameters to setup the worker.
 */
class LocationUpdateWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
  private val fusedLocationClient: FusedLocationProviderClient =
      LocationServices.getFusedLocationProviderClient(context)

  /**
   * Performs the background work to update the user's location.
   *
   * @return The result of the work.
   */
  override suspend fun doWork(): Result {
    return withContext(Dispatchers.IO) {
      try {
        // Get the current location
        Log.d("WorkerSuddenBump", "Starting doWork")
        val location: Location = getCurrentLocation()
        // Initialize UserRepository
        Log.d("WorkerSuddenBump", "Got location: $location")
        val repository = UserRepositoryFirestore(Firebase.firestore, applicationContext)
        val uid = repository.getSavedUid()

        if (uid == null) {
          Log.d("WorkerSuddenBump", "User ID not found")
          return@withContext Result.failure()
        }

        Log.d("WorkerSuddenBump", "Got userId: $uid")

        val user = uid?.let { getUserAccountSuspend(repository, it) }
        Log.d("WorkerSuddenBump", "Got user: $user")

        if (user != null) {
          Log.d("WorkerSuddenBump", "User $user")

          val timestamp = Timestamp.now()

          repository.updateLocation(
              user = user,
              location = location,
              onSuccess = { /* Handle success */},
              onFailure = { error -> /* Handle failure */ })

          repository.updateTimestamp(
              user,
              timestamp = timestamp,
              onSuccess = { /* Handle success */},
              onFailure = { error -> /* Handle failure */ })

          Result.success()
        } else {
          Log.d("WorkerSuddenBump", "User not found")
          Result.failure()
        }
      } catch (e: Exception) {
        Result.failure()
      }
    }
  }

  /**
   * Suspends the coroutine and retrieves the user account.
   *
   * @param repository The user repository.
   * @param userId The user ID.
   * @return The user account or null if not found.
   */
  private suspend fun getUserAccountSuspend(
      repository: UserRepositoryFirestore,
      userId: String
  ): User? = suspendCoroutine { cont ->
    Log.d("WorkerSuddenBump", "Calling getUserAccount for userId: $userId")
    repository.getUserAccount(
        uid = userId,
        onSuccess = { user ->
          Log.d("WorkerSuddenBump", "getUserAccount success: $user")
          cont.resume(user)
        },
        onFailure = { error ->
          Log.e("WorkerSuddenBump", "getUserAccount failure", error)
          cont.resumeWithException(error)
        })
  }

  /**
   * Creates the foreground notification for the worker.
   *
   * @return The ForegroundInfo containing the notification.
   */
  private fun createForegroundInfo(): ForegroundInfo {
    val channelId = "LocationUpdateWorkerChannel"
    val title = "Location Update"
    val cancel = "Cancel"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT)
      val notificationManager =
          applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }

    val notification =
        NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText("Updating location in the background")
            .setSmallIcon(R.drawable.app_logo)
            .setOngoing(true)
            .build()

    return ForegroundInfo(1, notification)
  }

  /**
   * Retrieves the current location.
   *
   * @return The current location.
   */
  @SuppressLint("MissingPermission")
  private suspend fun getCurrentLocation(): Location = suspendCancellableCoroutine { cont ->
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
          if (location != null) {
            cont.resume(location)
          } else {
            cont.resumeWithException(Exception("Location is null"))
          }
        }
        .addOnFailureListener { exception -> cont.resumeWithException(exception) }
  }
}
