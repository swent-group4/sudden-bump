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
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.squareup.moshi.Moshi
import com.swent.suddenbump.ActionReceiver
import com.swent.suddenbump.MainActivity
import com.swent.suddenbump.model.direction.DirectionsRepository
import com.swent.suddenbump.model.direction.GoogleMapsDirectionsService
import com.swent.suddenbump.model.direction.MapViewModel
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions
import java.text.SimpleDateFormat
import java.util.Locale
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    meetingViewModel: MeetingViewModel
) {
  Scaffold(
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      content = { innerPadding ->
        SimpleMap(userViewModel, meetingViewModel, Modifier.padding(innerPadding))
      })
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun SimpleMap(
    userViewModel: UserViewModel,
    meetingViewModel: MeetingViewModel,
    modifier: Modifier = Modifier
) {
  // Initialize necessary variables and states
  val context = LocalContext.current
  val markerState = rememberMarkerState(position = LatLng(0.0, 0.0))
  val cameraPositionState = rememberCameraPositionState()
  var zoomDone by remember { mutableStateOf(false) }
  var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
  var transportMode by remember { mutableStateOf("driving") }
  var showConfirmationDialog by remember { mutableStateOf(false) }
  val currentUserId = userViewModel.getCurrentUser().value.uid ?: ""

  val directionsService = remember {
    val retrofit =
        Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder().build()))
            .build()

    retrofit.create(GoogleMapsDirectionsService::class.java)
  }

  // Create an instance of DirectionsRepository
  val directionsRepository = remember { DirectionsRepository(directionsService) }

  // Now create the MapViewModel
  val mapViewModel = remember { MapViewModel(directionsRepository) }

  // Update user location and camera position
  LaunchedEffect(userViewModel.getLocation()) {
    userViewModel.getLocation().let {
      val latLng = LatLng(it.value.latitude, it.value.longitude)
      markerState.position = latLng
      if (!zoomDone) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 13f)
        fetchLocationToServer(it.value, userViewModel)
        zoomDone = true
      }
    }
  }

  // Fetch meetings
  val meetings by meetingViewModel.meetings.collectAsState()
  val filteredMeetings =
      meetings.filter {
        (it.creatorId == currentUserId || it.friendId == currentUserId) && it.accepted
      }

  Column(modifier = modifier.fillMaxSize()) {
    // Transport Mode Selector
    TransportModeSelector { mode -> transportMode = mode }

    Box(modifier = Modifier.fillMaxSize().testTag("mapView")) {
      GoogleMap(
          modifier = Modifier.fillMaxSize(),
          cameraPositionState = cameraPositionState,
          uiSettings = MapUiSettings(zoomControlsEnabled = false),
          properties = MapProperties(mapType = MapType.SATELLITE)) {
            // Your own position marker with click handling
            val markerBitmap = getLocationMarkerBitmap()
            Marker(
                state = markerState,
                title = "Current Location",
                snippet = "You are here",
                icon = BitmapDescriptorFactory.fromBitmap(markerBitmap),
                onClick = {
                  selectedLocation = markerState.position
                  true
                })

            // Friends' markers
            FriendsMarkers(userViewModel) { friendLatLng ->
              selectedLocation = friendLatLng
              showConfirmationDialog = true // Show confirmation dialog when info window is clicked
            }

            // Meeting markers
            filteredMeetings.forEach { meeting ->
              val meetingLatLng =
                  LatLng(
                      meeting.location?.latitude ?: 48.7855465,
                      meeting.location?.longitude ?: 2.3147013)

              Marker(
                  state = rememberMarkerState(position = meetingLatLng),
                  title = "Meeting at ${meeting.location?.name ?: "Unknown Location"}",
                  snippet =
                      "Scheduled on ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(meeting.date.toDate())}",
                  icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            }
          }

      // FloatingActionButton to open directions in Google Maps
      selectedLocation?.let {
        FloatingActionButton(
            onClick = { showConfirmationDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
              Icon(
                  imageVector = Icons.Filled.Place, // Use the desired icon
                  contentDescription = "Open in Google Maps")
            }
      }
    }
  }

  // Confirmation Dialog
  if (showConfirmationDialog) {
    AlertDialog(
        onDismissRequest = { showConfirmationDialog = false },
        title = { Text("Confirmation") },
        text = { Text("Do you want to open Google Maps?") },
        confirmButton = {
          Button(
              onClick = {
                val currentLocation = markerState.position
                val destinationLocation = selectedLocation!!
                openGoogleMapsDirections(
                    context, currentLocation, destinationLocation, transportMode)
                showConfirmationDialog = false
                selectedLocation = null
              }) {
                Text("Yes")
              }
        },
        dismissButton = { Button(onClick = { showConfirmationDialog = false }) { Text("No") } })
  }
}

fun openGoogleMapsDirections(context: Context, origin: LatLng, destination: LatLng, mode: String) {
  val uri =
      Uri.parse(
          "https://www.google.com/maps/dir/?api=1&origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&travelmode=$mode")
  val intent = Intent(Intent.ACTION_VIEW, uri)
  intent.setPackage("com.google.android.apps.maps")
  context.startActivity(intent)
}

@Composable
fun TransportModeSelector(onModeSelected: (String) -> Unit) {
  var expanded by remember { mutableStateOf(false) }
  var selectedMode by remember { mutableStateOf("driving") }

  Box {
    Button(onClick = { expanded = true }) {
      Text(text = selectedMode.replaceFirstChar { it.uppercaseChar() })
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      val modes = listOf("driving", "walking", "transit")
      modes.forEach { mode ->
        DropdownMenuItem(
            modifier = Modifier.testTag("ModeOption_$mode"), // Added testTag
            text = { Text(text = mode.replaceFirstChar { it.uppercaseChar() }) },
            onClick = {
              selectedMode = mode
              expanded = false
              onModeSelected(mode)
            })
      }
    }
  }
}

@Composable
fun FriendsMarkers(userViewModel: UserViewModel, onFriendMarkerInfoWindowClick: (LatLng) -> Unit) {
  val friends by userViewModel.getUserFriends().collectAsState(initial = emptyList())
  val markerStates = remember { mutableStateMapOf<String, MarkerState>() }

  // Initialize marker states for each friend
  friends.forEach { friend ->
    val friendLatLng =
        LatLng(friend.lastKnownLocation.value.latitude, friend.lastKnownLocation.value.longitude)
    if (!markerStates.containsKey(friend.uid)) {
      markerStates[friend.uid] = MarkerState(position = friendLatLng)
    } else {
      // Update marker position if it has changed
      val markerState = markerStates[friend.uid]!!
      markerState.position = friendLatLng
    }
  }

  friends.forEach { friend ->
    val markerState = markerStates[friend.uid] ?: return@forEach

    // Show info window with friend's name when marker is clicked
    MarkerInfoWindow(
        state = markerState,
        onClick = {
          // Do nothing on marker click, allow info window to be shown
          false
        },
        onInfoWindowClick = {
          // When the info window is clicked, proceed to confirmation dialog
          onFriendMarkerInfoWindowClick(markerState.position)
        },
        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
        content = {
          // Custom info window content displaying friend's name in blue
          Column {
            Text(
                text = friend.firstName,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
          }
        })
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
      onFailure = { Log.d("FireStoreLocation", "Failed to reach Firestore") })
}

fun showFriendNearbyNotification(context: Context, userUID: String, friend: User) {
  val channelId = "friend_nearby_channel"
  val channelName = "Friend Nearby Notifications"
  val notificationId = friend.uid.hashCode()

  val notificationChannel =
      NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
  val notificationManager = context.getSystemService(NotificationManager::class.java)
  notificationManager?.createNotificationChannel(notificationChannel)

  val intent =
      Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      }

  val pendingIntent: PendingIntent =
      PendingIntent.getActivity(
          context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

  val bundle =
      Bundle().apply {
        putString("userUID", userUID) // Add key-value pairs to the bundle
        putString("FriendUID", friend.uid)
        putInt("notificationId", notificationId)
      }

  // Create intents for Accept and Refuse actions
  val acceptIntent =
      Intent(context, ActionReceiver::class.java).apply {
        action = "ACTION_ACCEPT"
        putExtras(bundle)
      }

  val refuseIntent =
      Intent(context, ActionReceiver::class.java).apply {
        action = "ACTION_REFUSE"
        putExtras(bundle)
      }

  val acceptPendingIntent: PendingIntent =
      PendingIntent.getBroadcast(
          context,
          1,
          acceptIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

  val refusePendingIntent: PendingIntent =
      PendingIntent.getBroadcast(
          context,
          2,
          refuseIntent,
          PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

  // Build the notification with actions
  val notificationBuilder =
      NotificationCompat.Builder(context, channelId)
          .setSmallIcon(android.R.drawable.ic_dialog_info)
          .setContentTitle("Friend Nearby")
          .setContentText(
              "${friend.firstName} ${friend.lastName} is within your radius! \n Would you like to share your location with them?")
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .setContentIntent(pendingIntent)
          .setAutoCancel(true)
          .addAction(android.R.drawable.ic_menu_compass, "Accept", acceptPendingIntent)
          .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Refuse", refusePendingIntent)

  try {
    with(NotificationManagerCompat.from(context)) {
      notify(notificationId, notificationBuilder.build())
    }
  } catch (e: SecurityException) {
    Log.e("NotificationError", "Notification permission not granted", e)
  }
}
