package com.swent.suddenbump

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.swent.suddenbump.model.user.SharedPreferencesManager
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.worker.WorkerScheduler

class ActionReceiver(
    private val notificationManagerFactory: (Context) -> NotificationManagerCompat = {
      NotificationManagerCompat.from(it)
    },
    private val userRepositoryFactory: (Context) -> UserRepositoryFirestore = {
      UserRepositoryFirestore(Firebase.firestore, SharedPreferencesManager(it), WorkerScheduler(it))
    }
) : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val extras = intent.extras
    val userUID = extras?.getString("userUID")
    val friendUID = extras?.getString("FriendUID")
    val notifID = extras?.getInt("notificationId")
    val notificationManager = notificationManagerFactory(context)
    val repository = userRepositoryFactory(context)

    when (intent.action) {
      "ACTION_ACCEPT" -> {
        Log.d("IntentSB", "Accept button clicked")
        Log.d("IntentSB", "userUID: $userUID, friendUID: $friendUID")
        notificationManager.cancel(notifID!!)
        repository.shareLocationWithFriend(
            userUID!!,
            friendUID!!,
            onSuccess = { Log.d("IntentSB", "Successfully shared location with friend") },
            onFailure = { Log.d("IntentSB", "Failed to share location with friend") })
      }
      "ACTION_REFUSE" -> {
        Log.d("IntentSB", "Refuse button clicked")
        Log.d("IntentSB", "userUID: $userUID, friendUID: $friendUID")
        notificationManager.cancel(notifID!!)
        repository.stopSharingLocationWithFriend(
            userUID!!,
            friendUID!!,
            onSuccess = { Log.d("IntentSB", "Successfully removed friend") },
            onFailure = { Log.d("IntentSB", "Failed to remove friend") })
      }
      else -> {
        Log.d("IntentSB", "Unknown action")
      }
    }
  }
}
