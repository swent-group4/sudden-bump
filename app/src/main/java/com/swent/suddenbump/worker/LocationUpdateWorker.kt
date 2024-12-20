package com.swent.suddenbump.worker

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.swent.suddenbump.model.meeting.MeetingRepositoryFirestore
import com.swent.suddenbump.model.user.SharedPreferencesManager
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.ui.calendar.showMeetingScheduledNotification
import com.swent.suddenbump.ui.map.showFriendNearbyNotification
import java.util.Calendar
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
        val location: Location = getCurrentLocation()
        // Initialize UserRepository
        val repository =
            UserRepositoryFirestore(
                Firebase.firestore,
                SharedPreferencesManager(applicationContext),
                WorkerScheduler(applicationContext))
        val meetingRepository = MeetingRepositoryFirestore(Firebase.firestore)
        val alreadyNotifiedFriends = repository.getSavedAlreadyNotifiedFriends().toMutableList()
        val alreadyNotifiedMeetings = repository.getSavedAlreadyNotifiedMeetings().toMutableList()

        val uid = repository.getSavedUid()
        val radius = repository.getSavedRadius() * 1000

        val user = getUserAccountSuspend(repository, uid)

        if (user != null) {

          val timestamp = Timestamp.now()

          repository.updateUserLocation(
              uid = user.uid,
              location = location,
              onSuccess = { /* Handle success */},
              onFailure = { /* Handle failure */})

          repository.updateTimestamp(
              user.uid,
              timestamp = timestamp,
              onSuccess = { /* Handle success */},
              onFailure = { /* Handle failure */})

          if (repository.getSavedNotificationStatus()) {
            repository.getUserFriends(
                uid = user.uid,
                onSuccess = { friends ->
                  val friendsInRadius =
                      repository.userFriendsInRadius(
                          userLocation = location, friends = friends, radius = radius.toDouble())
                  friendsInRadius.forEach { friend ->
                    if (!(friend.lastKnownLocation.provider == "fused" ||
                        friend.lastKnownLocation.provider == "GPS"))
                        return@forEach
                    if (friend.uid !in alreadyNotifiedFriends) {
                      Log.d("WorkerSuddenBump", "alreadyNotifiedFriends: $alreadyNotifiedFriends")
                      showFriendNearbyNotification(applicationContext, user.uid, friend)
                      alreadyNotifiedFriends.add(friend.uid)
                      repository.saveNotifiedFriends(alreadyNotifiedFriends)
                    }
                  }
                },
                onFailure = {
                  Log.d("WorkerSuddenBump", "Retrieval of friends encountered an issue")
                })

            meetingRepository.getMeetings(
                onSuccess = { meetings ->
                  val filteredMeetings =
                      meetings.filter {
                        it.friendId == user.uid &&
                            !it.accepted &&
                            it.date.toDate().after(Calendar.getInstance().time)
                      }

                  if (filteredMeetings.isNotEmpty()) {
                    filteredMeetings.forEach { meeting ->
                      if (meeting.meetingId in alreadyNotifiedMeetings) {
                        return@forEach
                      }
                      showMeetingScheduledNotification(applicationContext, meeting)
                      alreadyNotifiedMeetings.add(meeting.meetingId)
                      repository.saveNotifiedMeeting(alreadyNotifiedMeetings)
                    }
                  } else {
                    Log.d("MeetingCheck", "No new pending meetings found")
                  }
                },
                onFailure = {
                  Log.d("MeetingCheck", "Retrieval of meetings encountered an issue: ${it.message}")
                })
          }

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
