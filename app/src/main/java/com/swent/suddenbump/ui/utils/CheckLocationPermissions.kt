package com.swent.suddenbump.ui.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.swent.suddenbump.model.LocationGetter

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

/*fun checkLocationPermissions(context: Context) {
    val fineLocationGranted =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    val coarseLocationGranted =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    lateinit var requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>

    requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                permissions ->
            handlePermissionResults(permissions)
        }

    if (fineLocationGranted || coarseLocationGranted) {
        locationGetter.requestLocationUpdates()
    } else {
        // Request permissions

        requestMultiplePermissionsLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
}*/

private fun handlePermissionResults(permissions: Map<String, Boolean>) {
    val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
    val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
    val backgroundLocationGranted = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: false
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
        else -> {
            // Toast.makeText(this, "Location Permissions Denied", Toast.LENGTH_SHORT).show()
        }
    }
}