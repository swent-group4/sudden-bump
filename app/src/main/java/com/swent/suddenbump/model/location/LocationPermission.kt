package com.swent.suddenbump.model.location

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class LocationPermission(val activity: Activity) {

  // Check if location permission is granted
  fun isLocationPermissionGranted(): Boolean {
    val fineLocationGranted =
        ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    val coarseLocationGranted =
        ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    return fineLocationGranted && coarseLocationGranted
  }
}
