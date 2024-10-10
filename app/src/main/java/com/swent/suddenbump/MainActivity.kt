package com.swent.suddenbump

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.google.firebase.auth.FirebaseAuth
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.swent.suddenbump.model.LocationGetter
import com.swent.suddenbump.resources.C
import com.swent.suddenbump.ui.map.MapScreen
import com.swent.suddenbump.ui.messages.MessagesScreen
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.overview.AddContactScreen
import com.swent.suddenbump.ui.overview.ConversationScreen
import com.swent.suddenbump.ui.overview.OverviewScreen
import com.swent.suddenbump.ui.overview.SettingsScreen
import com.swent.suddenbump.ui.map.MapScreen
import com.swent.suddenbump.ui.theme.SampleAppTheme

class MainActivity : ComponentActivity() {

  private lateinit var auth: FirebaseAuth

  private lateinit var requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>
  private lateinit var locationGetter: LocationGetter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    var newLocation by mutableStateOf<Location?>(null)

    locationGetter =
        LocationGetter(
            this,
            object : LocationGetter.LocationListener {
              override fun onLocationResult(location: Location?) {
                // Handle location update
                newLocation = location
              }

              override fun onLocationFailure(message: String) {
                Log.e("MainActivity", "Location Error: $message")
              }
            })
      FirebaseApp.initializeApp(this)
    //// Initialize Firebase Auth
    //auth = FirebaseAuth.getInstance()
    //auth.currentUser?.let {
    //  // Sign out the user if they are already signed in
    //  // This is useful for testing purposes
    //  auth.signOut()
    //}

    setContent {
      SampleAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              SuddenBumpApp(newLocation)
              //MapScreen(this, newLocation)

              // LocationHandler(this) { location -> newLocation = location }
              //
            }
      }
    }

    // Initialize permission launcher
    requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->
          handlePermissionResults(permissions)
        }
  }

  private fun checkLocationPermissions() {
    val fineLocationGranted =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    val coarseLocationGranted =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    if (fineLocationGranted || coarseLocationGranted) {
      locationGetter.requestLocationUpdates()
    } else {
      // Request permissions
      requestMultiplePermissionsLauncher.launch(
          arrayOf(
              Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
  }
@SuppressLint("UnrememberedMutableState")
@Composable
fun SuddenBumpApp(location: Location?) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)

  // val listToDosViewModel: ListToDosViewModel = viewModel(factory = ListToDosViewModel.Factory)

  NavHost(navController = navController, startDestination = Route.OVERVIEW) {
    navigation(
        startDestination = Screen.OVERVIEW,
        route = Route.OVERVIEW,
    ) {
      composable(Screen.OVERVIEW) { OverviewScreen(navigationActions) }
      composable(Screen.ADD_CONTACT) { AddContactScreen(navigationActions) }
      composable(Screen.CONV) { ConversationScreen(navigationActions) }
      composable(Screen.SETTINGS) { SettingsScreen(navigationActions) }
    }

    navigation(
        startDestination = Screen.MAP,
        route = Route.MAP,
    ) {
      composable(Screen.MAP) {
          MapScreen(navigationActions, location)
          checkLocationPermissions()
      }
    }
      navigation(
          startDestination = Screen.MESS,
          route = Route.MESS,
      ) {
          composable(Screen.MESS) { MessagesScreen(navigationActions) }
      }
  }
}

  private fun handlePermissionResults(permissions: Map<String, Boolean>) {
    val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
    val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

    when {
      fineLocationGranted -> {
        locationGetter.requestLocationUpdates()
      }
      coarseLocationGranted -> {
        locationGetter.requestLocationUpdates()
      }
      else -> {
        Toast.makeText(this, "Location Permissions Denied", Toast.LENGTH_SHORT).show()
      }
    }
  }

  //    @Composable
  //    fun LocationHandler(context: Context, onLocationUpdate: (Location?) -> Unit) {
  //        var locationGetter = remember {
  //            LocationGetter(
  //                context,
  //                object : LocationGetter.LocationListener {
  //                    override fun onLocationResult(location: Location?) {
  //                        onLocationUpdate(location)
  //                    }
  //
  //                    override fun onLocationFailure(message: String) {
  //                        Log.e("LocationHandler", "Location Error: $message")
  //                    }
  //                })
  //        }
  //
  //        LaunchedEffect(Unit) {
  //            Log.e("CREATED", "LocationHandler, LaunchedEffect")
  //            val locationPermissionHelper = LocationPermissionHelper(context as
  // ComponentActivity)
  //
  //            if (!locationPermissionHelper.isLocationPermissionGranted()) {
  //                locationPermissionHelper.requestLocationPermission()
  //            }
  //            locationGetter.requestLocationUpdates()
  //            Log.e("CREATED", "LocationHandler, after requestLocationUpdates")
  //        }
  //
  //        DisposableEffect(Unit) { onDispose { locationGetter.stopLocationUpdates() } }
  //
  //    }


}
