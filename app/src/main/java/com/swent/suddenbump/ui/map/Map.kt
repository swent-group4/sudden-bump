package com.swent.suddenbump.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.swent.suddenbump.model.LocationGetter
import com.example.locationpermission.LocationPermissionHelper
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapScreen(context: Context) {
    var location by remember { mutableStateOf<Location?>(null) }

    LocationHandler(context) { newLocation ->
        location = newLocation
    }

    Scaffold(
        modifier = Modifier.testTag("overviewScreen"),
        content = { SimpleMap(location) }
    )
}

@Composable
fun LocationHandler(context: Context, onLocationUpdate: (Location?) -> Unit) {
    val locationGetter = remember { LocationGetter(context, object : LocationGetter.LocationListener {
        override fun onLocationResult(location: Location?) {
            onLocationUpdate(location)
        }

        override fun onLocationFailure(message: String) {
            Log.e("LocationHandler", "Location Error: $message")
        }
    }) }

    LaunchedEffect(Unit) {
//        val locationPermissionHelper = LocationPermissionHelper(context as ComponentActivity)
//
//        if (locationPermissionHelper.isLocationPermissionGranted()) {
//            locationGetter.requestLocationUpdates()
//        } else {
//            locationPermissionHelper.requestLocationPermission()
//        }
        locationGetter.requestLocationUpdates()
    }

    DisposableEffect(Unit) {
        onDispose {
            locationGetter.stopLocationUpdates()
        }
    }
}


@Composable
fun SimpleMap(location: Location?) {
    val markerState = rememberMarkerState(position = LatLng(35.0, 139.0))

    LaunchedEffect(location) {
        location?.let {
            markerState.position = LatLng(it.latitude, it.longitude)
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        uiSettings = MapUiSettings(zoomControlsEnabled = false)
    ) {
        Marker(
            state = markerState,
            title = "Current Position",
            snippet = "DescriptionTest"
        )
    }
}
