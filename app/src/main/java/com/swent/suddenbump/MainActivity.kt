package com.swent.suddenbump

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.google.firebase.auth.FirebaseAuth
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
import com.swent.suddenbump.ui.theme.SampleAppTheme

class MainActivity : ComponentActivity() {

  private lateinit var auth: FirebaseAuth

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

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
              SuddenBumpApp()
            }
      }
    }
  }
}

@Composable
fun SuddenBumpApp() {
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
      composable(Screen.MAP) { MapScreen(navigationActions) }
    }

    navigation(
        startDestination = Screen.MESS,
        route = Route.MESS,
    ) {
      composable(Screen.MESS) { MessagesScreen(navigationActions) }
    }
  }
}
