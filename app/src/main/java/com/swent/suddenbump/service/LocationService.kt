package com.swent.suddenbump.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.google.android.gms.location.*
import com.swent.suddenbump.R

class LocationService : Service() {

  private lateinit var fusedLocationClient: FusedLocationProviderClient
  private lateinit var locationCallback: LocationCallback

  override fun onCreate() {
    super.onCreate()
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    locationCallback =
        object : LocationCallback() {
          override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location ->
              // Handle location updates here, e.g., send to a ViewModel or save to database
            }
          }
        }
    startForegroundService()
    requestLocationUpdates()
  }

  private fun startForegroundService() {
    val notificationChannelId = "LocationServiceChannel"
    val channel =
        NotificationChannel(
            notificationChannelId, "Location Service", NotificationManager.IMPORTANCE_LOW)
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)

    val notification: Notification =
        NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Location Service")
            .setContentText("Updating location in background")
            .setSmallIcon(R.drawable.profile)
            .build()
    startForeground(1, notification)
  }

  private fun requestLocationUpdates() {
    val locationRequest =
        LocationRequest.create().apply {
          interval = 10000 // Update interval in milliseconds
          fastestInterval = 5000
          priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED ||
        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {
      fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    fusedLocationClient.removeLocationUpdates(locationCallback)
  }

  override fun onBind(intent: Intent?): IBinder? = null
}
