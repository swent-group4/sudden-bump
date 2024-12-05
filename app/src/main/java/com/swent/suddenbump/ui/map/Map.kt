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
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
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
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.squareup.moshi.Moshi
import com.swent.suddenbump.MainActivity
import com.swent.suddenbump.model.direction.DirectionsRepository
import com.swent.suddenbump.model.direction.GoogleMapsDirectionsService
import com.swent.suddenbump.model.direction.MapViewModel
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Blue
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MapScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
    Scaffold(
        bottomBar = {
            BottomNavigationMenu(
                onTabSelect = { route -> navigationActions.navigateTo(route) },
                tabList = LIST_TOP_LEVEL_DESTINATION,
                selectedItem = navigationActions.currentRoute()
            )
        },
        content = { innerPadding ->
            SimpleMap(userViewModel, modifier = Modifier.padding(innerPadding))
        }
    )
}


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun SimpleMap(userViewModel: UserViewModel, modifier: Modifier = Modifier) {

    // Create an instance of DirectionsRepository
    val directionsRepository = remember {
        DirectionsRepository()
    }
    val context = LocalContext.current
    // Now create the MapViewModel
    val mapViewModel = remember {
        MapViewModel(directionsRepository)
    }
    val markerState = rememberMarkerState(position = LatLng(0.0, 0.0))
    val cameraPositionState = rememberCameraPositionState()
    var zoomDone by remember { mutableStateOf(false) }
    var selectedFriend by remember { mutableStateOf<User?>(null) }
    var transportMode by remember { mutableStateOf("driving") }

    val polylinePoints by mapViewModel.polylinePoints.collectAsState()
    var showConfirmationDialog by remember { mutableStateOf(false) }

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

    Column(modifier = modifier.fillMaxSize()){
        // Sélecteur de mode de transport
        TransportModeSelector { mode ->
            transportMode = mode
        }

        Box(modifier = Modifier.fillMaxSize().testTag("mapView")) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                val markerBitmap = getLocationMarkerBitmap()
                Marker(
                    state = markerState,
                    title = "Position actuelle",
                    snippet = "Vous êtes ici",
                    icon = BitmapDescriptorFactory.fromBitmap(markerBitmap)
                )

                FriendsMarkers(userViewModel) { friend ->
                    selectedFriend = friend
                }

                // Si un ami est sélectionné, afficher l'itinéraire
                selectedFriend?.let { friend ->
                    val currentLocation = markerState.position
                    val friendLocation = LatLng(
                        friend.lastKnownLocation.value.latitude,
                        friend.lastKnownLocation.value.longitude
                    )
                    LaunchedEffect(currentLocation, friendLocation, transportMode) {
                        mapViewModel.fetchDirections(
                            origin = currentLocation,
                            destination = friendLocation,
                            mode = transportMode,
                            apiKey = "MAPS_API_KEY" // Remplacez par votre clé API
                        )
                    }

                    if (polylinePoints.isNotEmpty()) {
                        Polyline(
                            points = polylinePoints,
                            color = Blue,
                            width = 5f
                        )
                    }
                }
            }

            // Bouton pour ouvrir l'itinéraire dans Google Maps
            selectedFriend?.let {
                FloatingActionButton(
                    onClick = {
                        showConfirmationDialog = true
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Ouvrir dans Google Maps")
                }
            }
        }
    }
    // Confirmation Dialog
    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("Confirmation") },
            text = { Text("Do you want to open directions in Google Maps?") },
            confirmButton = {
                Button(
                    onClick = {
                        val currentLocation = markerState.position
                        val friendLocation = LatLng(
                            selectedFriend!!.lastKnownLocation.value.latitude,
                            selectedFriend!!.lastKnownLocation.value.longitude
                        )
                        openGoogleMapsDirections(context, currentLocation, friendLocation, transportMode)
                        showConfirmationDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showConfirmationDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}

fun openGoogleMapsDirections(context: Context, origin: LatLng, destination: LatLng, mode: String) {
    val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&travelmode=$mode")
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
            Text(text = selectedMode.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val modes = listOf("driving", "walking", "transit")
            modes.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(text = mode.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) },
                    onClick = {
                        selectedMode = mode
                        expanded = false
                        onModeSelected(mode)
                    }
                )
            }
        }
    }
}

@Composable
fun FriendsMarkers(
    userViewModel: UserViewModel,
    onFriendMarkerClick: (User) -> Unit
) {
    val friends = remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(userViewModel) {
        launch {
            userViewModel.loadFriends()
            friends.value = userViewModel.getUserFriends().value
        }
    }

    friends.value.forEach { friend ->
        Marker(
            state = MarkerState(
                position = LatLng(
                    friend.lastKnownLocation.value.latitude,
                    friend.lastKnownLocation.value.longitude
                )
            ),
            title = friend.firstName,
            snippet = friend.uid,
            onClick = {
                onFriendMarkerClick(friend)
                true // Indique que l'événement est consommé
            }
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
