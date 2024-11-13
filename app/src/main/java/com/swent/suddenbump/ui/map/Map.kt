package com.swent.suddenbump.ui.map

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapScreen(
    navigationActions: NavigationActions,
    location: Location?,
    userViewModel: UserViewModel
) {
  Scaffold(
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      content = { pd -> SimpleMap(location, userViewModel) })
}

@Composable
fun SimpleMap(location: Location?, userViewModel: UserViewModel) {
  val markerState = rememberMarkerState(position = LatLng(1000.0, 1000.0))
  val cameraPositionState = rememberCameraPositionState()
  var zoomDone by remember { mutableStateOf(false) } // Track if the zoom has been performed

  Log.i("SimpleMap", userViewModel.getUserFriends().toString())

  LaunchedEffect(location) {
    location?.let {
      val latLng = LatLng(it.latitude, it.longitude)
      markerState.position = LatLng(it.latitude, it.longitude)
      if (!zoomDone) {
        // Perform zoom only the first time the location is set
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
        zoomDone = true // Mark zoom as done
      }
    }
  }
  // LaunchedEffect to refresh location every minute
  LaunchedEffect(Unit) {
    while (true) {
      delay(60000) // Wait for 1 minute
      // Refresh location logic here
      location?.let {
        val latLng = LatLng(it.latitude, it.longitude)
        markerState.position = LatLng(it.latitude, it.longitude)
        if (!zoomDone) {
          cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(latLng, 13f))
          zoomDone = true
        }
        // Update location to Firebase
        userViewModel.updateLocation(
            location = it,
            onSuccess = {
              // Handle success
            },
            onFailure = { error ->
              // Handle failure
              Log.e("SimpleMap", "Failed to update location: ${error.message}")
            })
      }
      // Load friends' locations
      userViewModel.loadFriendsLocations()
    }
  }
  Box(modifier = Modifier.fillMaxSize().testTag("mapView")) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(zoomControlsEnabled = false)) {
          val markerBitmap = getLocationMarkerBitmap()
          Marker(
              state = markerState,
              title = "Current Position",
              snippet = "DescriptionTest",
              icon = BitmapDescriptorFactory.fromBitmap(markerBitmap))

          FriendsMarkers(userViewModel)
        }
  }
}

@Composable
fun FriendsMarkers(userViewModel: UserViewModel) {
  val friendsLocations = remember { mutableStateOf<Map<User, Location?>>(emptyMap()) }

  LaunchedEffect(userViewModel) {
    launch {
      userViewModel.loadFriendsLocations()
      friendsLocations.value = userViewModel.friendsLocations.value
      // Log the friendsLocations
      Log.d("FriendsMarkers", "Friends Locations: ${friendsLocations}")
    }
  }

  friendsLocations.value.let { locations ->
    locations.forEach { (friend, location) ->
      location?.let {
        Marker(
            state = MarkerState(position = LatLng(it.latitude, it.longitude)),
            title = friend.firstName,
            snippet = friend.uid,
        )
      }
    }
  }

  /*val mockImageBitmap: ImageBitmap? = null // assuming null for simplicity

  // Create mock users
  val user1 =
      User(
          uid = "1",
          firstName = "John",
          lastName = "Doe",
          phoneNumber = "123456789",
          profilePicture = mockImageBitmap,
          emailAddress = "john.doe@example.com")

  val user2 =
      User(
          uid = "2",
          firstName = "Jane",
          lastName = "Smith",
          phoneNumber = "987654321",
          profilePicture = mockImageBitmap,
          emailAddress = "jane.smith@example.com")

  val user3 =
      User(
          uid = "3",
          firstName = "Alice",
          lastName = "Johnson",
          phoneNumber = "555666777",
          profilePicture = mockImageBitmap,
          emailAddress = "alice.johnson@example.com")

  // Create mock locations
  val location1 =
      Location("mock_provider").apply {
        latitude = 46.5186664
        longitude = 6.568274
      }
  val location2 =
      Location("mock_provider").apply {
        latitude = 46.521083
        longitude = 6.575470
      }
  val location3 =
      Location("mock_provider").apply {
        latitude = 46.522836
        longitude = 6.565142
      }

  // Create Map<User, Location>
  val userLocationMap: Map<User, Location> =
      mapOf(user1 to location1, user2 to location2, user3 to location3)

  userLocationMap.let { locations ->
    locations.forEach { (friend, location) ->
      Marker(
          state = MarkerState(position = LatLng(location.latitude, location.longitude)),
          title = friend.firstName,
          snippet = friend.uid,
      )
    }
  }*/
}

fun getLocationMarkerBitmap(): Bitmap {
  val markerSize = 75 // Adjust size as needed
  val bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(bitmap)

  // Paint for the outer circle (semi-transparent blue)
  val outerPaint = Paint()
  outerPaint.color = Color.parseColor("#4466BBFF") // Semi-transparent blue
  outerPaint.style = Paint.Style.FILL

  // Paint for the inner circle (solid blue)
  val innerPaint = Paint()
  innerPaint.color = Color.parseColor("#0066CC") // Solid blue
  innerPaint.style = Paint.Style.FILL

  // Draw the outer circle (semi-transparent)
  canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 2f, outerPaint)

  // Draw the inner circle (solid blue)
  canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 4f, innerPaint)

  return bitmap
}
