package com.swent.suddenbump.ui.map

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.swent.suddenbump.MainActivity
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  Scaffold(
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      content = { _ -> SimpleMap(userViewModel) })
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun SimpleMap(userViewModel: UserViewModel) {
  val markerState = rememberMarkerState(position = LatLng(1000.0, 1000.0))
  val cameraPositionState = rememberCameraPositionState()
  var zoomDone by remember { mutableStateOf(false) } // Track if the zoom has been performed

  LaunchedEffect(userViewModel.getLocation()) {
    userViewModel.getLocation().let {
      val latLng = LatLng(it.value.latitude, it.value.longitude)
      markerState.position = LatLng(it.value.latitude, it.value.longitude)
      if (!zoomDone) {
        // Perform zoom only the first time the location is set
        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 13f)
        fetchLocationToServer(it.value, userViewModel)
        zoomDone = true // Mark zoom as done
      }
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
  val friends = remember { mutableStateOf<List<User>>(emptyList()) }

  LaunchedEffect(userViewModel) {
    launch {
      userViewModel.loadFriends()
      friends.value = userViewModel.getUserFriends().value
      // Log the friendsLocations
      Log.d("FriendsMarkers", "Friends Locations: $friends")
    }
  }

  friends.value.forEach { friend ->
    Marker(
        state =
            MarkerState(
                position =
                    LatLng(
                        friend.lastKnownLocation.value.latitude,
                        friend.lastKnownLocation.value.longitude)),
        title = friend.firstName,
        snippet = friend.uid,
    )
  }
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

fun fetchLocationToServer(location: Location, userViewModel: UserViewModel) {
  userViewModel.updateLocation(
      userViewModel.getCurrentUser().value,
      location,
      onSuccess = { Log.d("FireStoreLocation", "Successfully updated location") },
      onFailure = { Log.d("FireStoreLocation", "Failure to reach Firestore") })
}

fun showFriendNearbyNotification(context: Context) {
  val channelId = "friend_nearby_channel"
  val channelName = "Friend Nearby Notifications"
  val notificationId = 1

  val notificationChannel =
      NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
  val notificationManager = context.getSystemService(NotificationManager::class.java)
  notificationManager?.createNotificationChannel(notificationChannel)

  // Modify the intent to navigate to Screen.OVERVIEW
  val intent =
      Intent(context, MainActivity::class.java).apply {
        putExtra("destination", "Screen.OVERVIEW")
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      }

  val pendingIntent: PendingIntent =
      PendingIntent.getActivity(
          context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

  val notificationBuilder =
      NotificationCompat.Builder(context, channelId)
          .setSmallIcon(android.R.drawable.ic_dialog_info)
          .setContentTitle("Friend Nearby")
          .setContentText("A friend is within your radius!")
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .setContentIntent(pendingIntent)
          .setAutoCancel(true)

  try {
    with(NotificationManagerCompat.from(context)) {
      notify(notificationId, notificationBuilder.build())
    }
  } catch (e: SecurityException) {
    Log.e("NotificationError", "Notification permission not granted", e)
  }
}
