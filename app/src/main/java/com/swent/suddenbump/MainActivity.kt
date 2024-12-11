package com.swent.suddenbump

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
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
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.swent.suddenbump.model.location.LocationGetter
import com.swent.suddenbump.model.location.LocationPermission
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.notifications.NotificationsPermission
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.resources.C
import com.swent.suddenbump.ui.authentication.SignInScreen
import com.swent.suddenbump.ui.authentication.SignUpScreen
import com.swent.suddenbump.ui.calendar.AddMeetingScreen
import com.swent.suddenbump.ui.calendar.CalendarMeetingsScreen
import com.swent.suddenbump.ui.calendar.EditMeetingScreen
import com.swent.suddenbump.ui.calendar.PendingMeetingsScreen
import com.swent.suddenbump.ui.chat.ChatScreen
import com.swent.suddenbump.ui.contact.AddContactScreen
import com.swent.suddenbump.ui.contact.ContactScreen
import com.swent.suddenbump.ui.map.MapScreen
import com.swent.suddenbump.ui.messages.MessagesScreen
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.overview.AccountScreen
import com.swent.suddenbump.ui.overview.BlockedUsersScreen
import com.swent.suddenbump.ui.overview.ConfidentialityScreen
import com.swent.suddenbump.ui.overview.ConversationScreen
import com.swent.suddenbump.ui.overview.DiscussionScreen
import com.swent.suddenbump.ui.overview.FriendsListScreen
import com.swent.suddenbump.ui.overview.OverviewScreen
import com.swent.suddenbump.ui.overview.SettingsScreen
import com.swent.suddenbump.ui.theme.SampleAppTheme
import com.swent.suddenbump.ui.utils.isRunningTest
import com.swent.suddenbump.worker.WorkerScheduler.scheduleLocationUpdateWorker

class MainActivity : ComponentActivity() {

  private lateinit var auth: FirebaseAuth

  private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
  private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
  private lateinit var locationGetter: LocationGetter
  private var newLocation by mutableStateOf<Location?>(null)
  private val userViewModel: UserViewModel by viewModels { UserViewModel.provideFactory(this) }

  @SuppressLint("SuspiciousIndentation")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

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

    // Initialize permission launchers
    locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->
          val allGranted = permissions.all { it.value }
          if (allGranted) {
            locationGetter.requestLocationUpdates()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              checkNotificationPermission()
            }
          } else {
            Log.e("Permissions", "Location permissions were denied")
          }
        }

    notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
          if (isGranted) {
            Log.i("Permissions", "Notification permission granted")
          } else {
            Log.e("Permissions", "Notification permission denied")
          }
        }

    val notificationChannel =
        NotificationChannel("1", "FriendsNear", NotificationManager.IMPORTANCE_HIGH)
    val notificationManager = getSystemService(NotificationManager::class.java)
    notificationManager?.createNotificationChannel(notificationChannel)

    FirebaseApp.initializeApp(this)
    // Initialize Firebase Auth
    auth = FirebaseAuth.getInstance()
    if (isRunningTest()) {
      auth.currentUser?.let {
        // Sign out the user if they are already signed in
        // This is useful for testing purposes
        auth.signOut()
      }
    }

    setContent {
      SampleAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
            color = MaterialTheme.colorScheme.background) {
              SuddenBumpApp()
            }
      }
    }
  }

  override fun onStart() {
    super.onStart()
    // Update online status when the activity starts
    userViewModel.updateUserStatus(
        uid = userViewModel.getCurrentUser().value.uid,
        status = true,
        onSuccess = { Log.d("UserStatus", "Online status updated") },
        onFailure = { e -> Log.e("UserStatus", "Error updating online status: ${e.message}") })
  }

  override fun onStop() {
    super.onStop()
    // Update offline status when the activity stops
    userViewModel.updateUserStatus(
        uid = userViewModel.getCurrentUser().value.uid,
        status = false,
        onSuccess = { Log.d("UserStatus", "Offline status updated") },
        onFailure = { e -> Log.e("UserStatus", "Error updating offline status: ${e.message}") })
  }

  private fun checkLocationPermissions(onResult: () -> Unit) {

    if (!isRunningTest()) {
      if (!LocationPermission(this).isLocationPermissionGranted()) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
      } else {
        locationGetter.requestLocationUpdates()
        onResult()
      }
    }
  }

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  @SuppressLint(
      "UnrememberedMutableState", "StateFlowValueCalledInComposition", "SuspiciousIndentation")
  private fun checkNotificationPermission() {
    if (!NotificationsPermission(this).isNotificationPermissionGranted() && !isRunningTest()) {
      ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1003)
    }
  }

  @Composable
  fun SuddenBumpApp() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)

    val meetingViewModel: MeetingViewModel = viewModel(factory = MeetingViewModel.Factory)
    val userViewModel: UserViewModel by viewModels { UserViewModel.provideFactory(this) }

    newLocation?.let { it1 ->
      userViewModel.updateLocation(location = it1, onSuccess = {}, onFailure = {})
    }

    val startRoute =
        if (!isRunningTest() && userViewModel.isUserLoggedIn()) {
          val uid = userViewModel.getSavedUid()
          Log.d("MainActivity", "User logged in: $uid")
          userViewModel.setCurrentUser(
              uid,
              onSuccess = {
                Log.i("MainActivity", "User set: ${userViewModel.getCurrentUser().value}")
              },
              onFailure = { e -> Log.e("MainActivity", e.toString()) })
          // Schedule the LocationUpdateWorker
          scheduleLocationUpdateWorker(this, uid)
          Route.OVERVIEW
        } else {
          Route.AUTH
        }

    NavHost(navController = navController, startDestination = startRoute) {
      navigation(
          startDestination = Screen.AUTH,
          route = Route.AUTH,
      ) {
        composable(Screen.AUTH) { SignInScreen(navigationActions, userViewModel) }
        composable(Screen.SIGNUP) { SignUpScreen(navigationActions, userViewModel) }
      }
      navigation(
          startDestination = Screen.OVERVIEW,
          route = Route.OVERVIEW,
      ) {
        composable(Screen.OVERVIEW) {
          // Start permission requests
          checkLocationPermissions {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              checkNotificationPermission()
            }
          }
          newLocation?.let { it1 ->
            userViewModel.updateLocation(location = it1, onSuccess = {}, onFailure = {})
          }
          OverviewScreen(navigationActions, userViewModel)
        }
        composable(Screen.FRIENDS_LIST) { FriendsListScreen(navigationActions, userViewModel) }
        composable(Screen.ADD_CONTACT) { AddContactScreen(navigationActions, userViewModel) }
        composable(Screen.CONV) { ConversationScreen(navigationActions) }
        composable(Screen.SETTINGS) {
          SettingsScreen(
              navigationActions, userViewModel, meetingViewModel, onNotificationsEnabledChange = {})
        }
        composable(Screen.CONTACT) { ContactScreen(navigationActions, userViewModel) }
        composable(Screen.CHAT) { ChatScreen(userViewModel, navigationActions) }
        composable(Screen.ADD_MEETING) {
          AddMeetingScreen(navigationActions, userViewModel, meetingViewModel)
        }
      }
      navigation(
          startDestination = Screen.CALENDAR,
          route = Route.CALENDAR,
      ) {
        composable(Screen.CALENDAR) {
          CalendarMeetingsScreen(navigationActions, meetingViewModel, userViewModel)
        }
        composable(Screen.EDIT_MEETING) { EditMeetingScreen(navigationActions, meetingViewModel) }
        composable(Screen.PENDING_MEETINGS) {
          PendingMeetingsScreen(navigationActions, meetingViewModel, userViewModel)
        }
      }

      navigation(
          startDestination = Screen.MAP,
          route = Route.MAP,
      ) {
        composable(Screen.MAP) { MapScreen(navigationActions, userViewModel) }
      }
      navigation(
          startDestination = Screen.MESS,
          route = Route.MESS,
      ) {
        composable(Screen.MESS) { MessagesScreen(userViewModel, navigationActions) }
      }

      // Add new screens from Settings.kt
      composable("AccountScreen") { AccountScreen(navigationActions) }
      composable("ConfidentialityScreen") {
        ConfidentialityScreen(navigationActions, userViewModel = userViewModel)
      }
      composable("DiscussionsScreen") { DiscussionScreen(navigationActions, userViewModel) }
      composable("BlockedUsersScreen") { BlockedUsersScreen(navigationActions, userViewModel) }
    }
  }
}
