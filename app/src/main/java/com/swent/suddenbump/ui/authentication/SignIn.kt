package com.swent.suddenbump.ui.authentication

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.swent.suddenbump.R
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.navigation.TopLevelDestinations
import com.swent.suddenbump.ui.theme.violetColor
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun SignInScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val context = LocalContext.current

  val launcher =
      rememberFirebaseAuthLauncher(
          onAuthComplete = { result ->
            Log.d("SignInScreen", "User signed in: ${result.user?.displayName}")
            Toast.makeText(context, "Login successful!", Toast.LENGTH_LONG).show()
            userViewModel.verifyNoAccountExists(
                result.user?.email ?: "",
                onSuccess = { bool ->
                  if (bool) {
                    Log.d("SignInScreen", "Account does not exist")
                    navigationActions.navigateTo(Screen.SIGNUP)
                  } else {
                    Log.d("SignInScreen", "Account exists")
                    userViewModel.setCurrentUser()
                    navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                  }
                },
                onFailure = {
                  Log.d("SignInScreen", "Failure to reach Firestore")
                  Toast.makeText(context, "Failure to reach database", Toast.LENGTH_LONG).show()
                })
          },
          onAuthError = {
            Log.e("SignInScreen", "Failed to sign in: ${it.statusCode}")
            Toast.makeText(context, "Login Failed!", Toast.LENGTH_LONG).show()
          })
  val token = stringResource(R.string.default_web_client_id)
  // The main container for the screen

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      content = { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
          Spacer(modifier = Modifier.height(16.dp))

          // Welcome Text
          Text(
              modifier =
                  Modifier.testTag("loginTitle").fillMaxWidth(), // j'ai add fillMaxWidth(0.8f)
              text = "SuddenBump!",
              style =
                  MaterialTheme.typography.headlineLarge.copy(
                      fontSize = 48.sp, // j'ai changé, c'était 36.sp de base
                      lineHeight = 64.sp),
              color = violetColor,
              fontWeight = FontWeight.Bold,
              // center the text

              textAlign = TextAlign.Center)

          Spacer(
              modifier = Modifier.height(24.dp)) // c'était 48 de base, I modified it so that there
          // is less spacing between the text and the button

          // Authenticate With Google Button
          GoogleSignInButton(
              onSignInClick = {
                val gso =
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(token)
                        .requestEmail()
                        .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                launcher.launch(googleSignInClient.signInIntent)
              })
        }
      })
}

@Composable
fun GoogleSignInButton(onSignInClick: () -> Unit) {
  Button(
      onClick = onSignInClick,
      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC3A1E8)), // Button color
      shape =
          RoundedCornerShape(
              8.dp), // (avant y avait juste 50, sans le dp) // Circular edges for the button
      border = BorderStroke(1.dp, Color.White),
      modifier =
          Modifier.padding(16.dp)
              .height(48.dp) // Adjust height as needed //c'était 48 avant
              .width(300.dp) // j'ai add cette ligne
              .testTag("loginButton")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
              // Load the Google logo from resources
              Image(
                  painter = painterResource(id = R.drawable.google_logo),
                  contentDescription = "Google Logo",
                  modifier =
                      Modifier.size(30.dp) // Size of the Google logo
                          .padding(end = 8.dp))

              // Text for the button
              Text(
                  text = "Sign in with Google",
                  color = Color.White, // Text color
                  fontSize = 18.sp, // Font size // c'était 16
                  fontWeight = FontWeight.SemiBold,
                  letterSpacing = 1.sp) // c'était medium
        }
      }
}

@Composable
fun rememberFirebaseAuthLauncher(
    onAuthComplete: (AuthResult) -> Unit,
    onAuthError: (ApiException) -> Unit
): ManagedActivityResultLauncher<Intent, ActivityResult> {
  val scope = rememberCoroutineScope()
  return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      result ->
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    try {
      val account = task.getResult(ApiException::class.java)!!
      val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
      scope.launch {
        val authResult = Firebase.auth.signInWithCredential(credential).await()
        onAuthComplete(authResult)
      }
    } catch (e: ApiException) {
      onAuthError(e)
    }
  }
}

/*@Preview(showBackground = true)
@Composable
fun PreviewSignInScreen() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  SignInScreen(navigationActions = navigationActions)
}*/
