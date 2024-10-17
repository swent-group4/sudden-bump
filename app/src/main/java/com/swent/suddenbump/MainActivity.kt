package com.swent.suddenbump

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
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
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.swent.suddenbump.model.LocationGetter
import com.swent.suddenbump.model.image.TestComposableScreen
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.resources.C
import com.swent.suddenbump.ui.authentication.SignInScreen
import com.swent.suddenbump.ui.map.MapScreen
import com.swent.suddenbump.ui.messages.MessagesScreen
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.overview.AddContactScreen
import com.swent.suddenbump.ui.overview.ConversationScreen
import com.swent.suddenbump.ui.overview.OverviewScreen
import com.swent.suddenbump.ui.profile.ContactScreen
import com.swent.suddenbump.ui.settings.SettingsScreen
import com.swent.suddenbump.ui.theme.SampleAppTheme

class MainActivity : ComponentActivity() {

  private lateinit var auth: FirebaseAuth

  private lateinit var requestMultiplePermissionsLauncher: ActivityResultLauncher<Array<String>>
  private lateinit var locationGetter: LocationGetter

  @SuppressLint("SuspiciousIndentation")
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
    // Initialize Firebase Auth
    auth = FirebaseAuth.getInstance()
    auth.currentUser?.let {
      // Sign out the user if they are already signed in
      // This is useful for testing purposes
      auth.signOut()
    }

    setContent {
      SampleAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              // SuddenBumpApp(newLocation)
              val userViewModel = UserViewModel(UserRepositoryFirestore(Firebase.firestore))
              TestComposableScreen(userViewModel)
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

    // val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)

    NavHost(navController = navController, startDestination = Route.AUTH) {
      navigation(
          startDestination = Screen.AUTH,
          route = Route.AUTH,
      ) {
        composable(Screen.AUTH) { SignInScreen(navigationActions) }
      }
      navigation(
          startDestination = Screen.OVERVIEW,
          route = Route.OVERVIEW,
      ) {
        composable(Screen.OVERVIEW) { OverviewScreen(navigationActions) }
        composable(Screen.ADD_CONTACT) { AddContactScreen(navigationActions) }
        composable(Screen.CONV) { ConversationScreen(navigationActions) }
        composable(Screen.SETTINGS) { SettingsScreen(navigationActions) }
        composable(Screen.CONTACT) { ContactScreen(navigationActions) }
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
        // Toast.makeText(this, "Location Permissions Denied", Toast.LENGTH_SHORT).show()
      }
    }
  }
}
