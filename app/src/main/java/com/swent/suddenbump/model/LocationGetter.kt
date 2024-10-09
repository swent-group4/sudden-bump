package com.swent.suddenbump.model

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.example.locationpermission.LocationPermissionHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationGetter(private val context: Context, private val listener: LocationListener) {

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
      listener.onLocationFailure("Location permission not granted")
      val locationPermissionHelper = LocationPermissionHelper(context as ComponentActivity)
      locationPermissionHelper.requestLocationPermission()
      requestLocationUpdates()
    }

    val locationRequest =
        LocationRequest.create().apply {
          interval = 5000 // 5 seconds
          fastestInterval = 1000 // 1 seconds
          priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

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
