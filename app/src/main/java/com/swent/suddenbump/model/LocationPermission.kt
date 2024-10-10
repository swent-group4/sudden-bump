package com.swent.suddenbump.model

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class LocationPermissionHelper(private val activity: Activity) {

  companion object {
    const val LOCATION_PERMISSION_REQUEST_CODE = 1
  }

  // Check if location permission is granted
  fun isLocationPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
  }

  // Request location permission
  fun requestLocationPermission() {
    if (!isLocationPermissionGranted()) {
      ActivityCompat.requestPermissions(
          activity,
          arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
          LOCATION_PERMISSION_REQUEST_CODE)
    } else {
      Toast.makeText(activity, "Location Permission Already Granted", Toast.LENGTH_SHORT).show()
    }
  }

  // Handle the result of the permission request
  fun handlePermissionResult(requestCode: Int, grantResults: IntArray): Boolean {
    if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(activity, "Location Permission Granted", Toast.LENGTH_SHORT).show()
        return true
      } else {
        Toast.makeText(activity, "Location Permission Denied", Toast.LENGTH_SHORT).show()
        return false
      }
    }
    return false
  }
}
