package com.swent.suddenbump.model

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class   LocationGetter(private val context: Context, private val listener: LocationListener) {

  private lateinit var fusedLocationClient: FusedLocationProviderClient

  interface LocationListener {
    fun onLocationResult(location: Location?)

    fun onLocationFailure(message: String)
  }

  init {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
  }

  @SuppressLint("MissingPermission")
  fun requestLocationUpdates() {
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
      Log.e("CREATED", "requestLocationUpdates, Pas de pot")
      return
    }

    val locationRequest =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(false)
            .setMaxUpdateDelayMillis(2000)
            .build()

    fusedLocationClient.requestLocationUpdates(
        locationRequest, locationCallback, Looper.getMainLooper())
  }

  private val locationCallback =
      object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
          super.onLocationResult(locationResult)
          if (locationResult.locations.isNotEmpty()) {
            listener.onLocationResult(locationResult.lastLocation)
          } else {
            listener.onLocationFailure("Location not available")
          }
        }
      }

  fun stopLocationUpdates() {
    fusedLocationClient.removeLocationUpdates(locationCallback)
  }
}
