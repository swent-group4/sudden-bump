package com.swent.suddenbump.worker

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.model.user.UserViewModel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class LocationUpdateWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
  private val fusedLocationClient: FusedLocationProviderClient =
      LocationServices.getFusedLocationProviderClient(context)

  override suspend fun doWork(): Result =
      withContext(Dispatchers.IO) {
        try {
          // Get the current location (you need to implement this method)
          val location: Location = getCurrentLocation()

          // Initialize UserRepository
          val repository = UserRepositoryFirestore(FirebaseFirestore.getInstance())

          // Initialize UserViewModel with the repository
          val userViewModel = UserViewModel(repository)

          // Update location to Firebase
          userViewModel.updateLocation(
              location = location,
              onSuccess = {
                // Handle success
              },
              onFailure = { error ->
                // Handle failure
              })

          // Load friends' locations
          userViewModel.loadFriendsLocations()

          Result.success()
        } catch (e: Exception) {
          Result.failure()
        }
      }

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
