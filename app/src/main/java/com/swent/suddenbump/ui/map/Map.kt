package com.swent.suddenbump.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapScreen(context: Context, location: Location?) {

  Scaffold(modifier = Modifier.testTag("overviewScreen"), content = { SimpleMap(location) })
}

@Composable
fun SimpleMap(location: Location?) {
  val markerState = rememberMarkerState(position = LatLng(1000.0, 1000.0))

  LaunchedEffect(location) {
    location?.let { markerState.position = LatLng(it.latitude, it.longitude) }
  }

  GoogleMap(
      modifier = Modifier.fillMaxSize(), uiSettings = MapUiSettings(zoomControlsEnabled = false)) {
        Marker(state = markerState, title = "Current Position", snippet = "DescriptionTest")
      }
}
