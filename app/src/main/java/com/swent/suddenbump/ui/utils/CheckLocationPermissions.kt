package com.swent.suddenbump.ui.utils

import android.Manifest
import android.content.Context
import android.location.Location
import android.util.Log
import com.swent.suddenbump.model.location.LocationGetter

private lateinit var locationGetter: LocationGetter

fun initLocationGetter(context: Context) {
  locationGetter =
      LocationGetter(
          context,
          object : LocationGetter.LocationListener {
            override fun onLocationResult(location: Location?) {
              // Handle location update
            }

            override fun onLocationFailure(message: String) {
              Log.e("MainActivity", "Location Error: $message")
            }
          })
}

private fun handlePermissionResults(permissions: Map<String, Boolean>) {
  val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
  val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
  val backgroundLocationGranted =
      permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: false
  when {
    fineLocationGranted -> {
      locationGetter.requestLocationUpdates()
    }
    coarseLocationGranted -> {
      locationGetter.requestLocationUpdates()
    }
    backgroundLocationGranted -> {
      locationGetter.requestLocationUpdates()
    }
  }
}
