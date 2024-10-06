package com.swent.suddenbump

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import com.swent.suddenbump.resources.C
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
              Greeting("Hello there", Modifier.semantics { testTag = C.Tag.greeting })
            }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier.semantics { testTag = C.Tag.greeting })
}

/*@Composable
fun SuddenBumpApp() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)

   // val listToDosViewModel: ListToDosViewModel = viewModel(factory = ListToDosViewModel.Factory)

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
            composable(Screen.OVERVIEW) { OverviewScreen(listToDosViewModel, navigationActions) }
            composable(Screen.ADD_CONTACT) { AddContactScreen(listToDosViewModel, navigationActions) }
            composable(Screen.CONV) { ConvScreen(listToDosViewModel, navigationActions) }
        }

        navigation(
            startDestination = Screen.MAP,
            route = Route.MAP,
        ) {
            composable(Screen.MAP) { MapScreen(navigationActions) }
        }
    }
}*/

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  SampleAppTheme { Greeting("Android") }
}
