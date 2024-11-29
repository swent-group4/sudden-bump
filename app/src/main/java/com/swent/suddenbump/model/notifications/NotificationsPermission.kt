package com.swent.suddenbump.model.notifications

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class NotificationsPermission(val activity: Activity) {

  /**
   * Checks if the notification permission is granted for the application.
   *
   * This method requires API level 33 (TIRAMISU) or higher.
   *
   * @return `true` if the notification permission is granted, `false` otherwise.
   */
  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  fun isNotificationPermissionGranted(): Boolean {
    val notificationPermissionGranted =
        ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    return notificationPermissionGranted
  }
}
