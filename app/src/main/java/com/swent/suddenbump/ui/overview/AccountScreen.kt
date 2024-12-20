package com.swent.suddenbump.ui.overview

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.swent.suddenbump.R
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.theme.Purple40
import com.swent.suddenbump.ui.utils.AccountOption
import com.swent.suddenbump.ui.utils.CustomCenterAlignedTopBar
import com.swent.suddenbump.ui.utils.isRunningTest

@Composable
fun AccountScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    meetingViewModel: MeetingViewModel
) {
  var selectedLanguage by remember { mutableStateOf("English") }
  var isLanguageMenuExpanded by remember { mutableStateOf(false) }

  val context = LocalContext.current

  // State to show/hide the delete account confirmation dialog
  var showDeleteAccountDialog by remember { mutableStateOf(false) }

  // Observe status messages from the ViewModel
  val statusMessage by userViewModel.statusMessage.observeAsState()

  // Show a Toast when statusMessage changes
  LaunchedEffect(statusMessage) {
    statusMessage?.let { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
  }

  Scaffold(
      modifier = Modifier.testTag("accountScreen"),
      topBar = {
        CustomCenterAlignedTopBar(title = "Account", navigationActions = navigationActions)
      },
      content = { paddingValues ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black)
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              item {
                AccountOption(
                    label = "Delete Account",
                    backgroundColor = Color.Red,
                    onClick = { navigationActions.navigateTo("AccountScreen") },
                    testTag = "deleteAccountSection")
              }

              item {
                AccountOption(
                    label = "Log out",
                    backgroundColor = Purple40,
                    onClick = {
                      userViewModel.logout()
                      if (isRunningTest()) {
                        navigationActions.navigateTo(Route.AUTH)
                      }

                      FirebaseAuth.getInstance().signOut()
                      val gso =
                          GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                              .requestIdToken(context.getString(R.string.default_web_client_id))
                              .requestEmail()
                              .build()

                      val googleSignInClient = GoogleSignIn.getClient(context, gso)

                      // Sign out from Google account
                      googleSignInClient.signOut().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                          Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_LONG)
                              .show()
                          navigationActions.navigateTo(Route.AUTH)
                        }
                      }

                      // Optional: Revoke access (forces account picker next time)
                      googleSignInClient.revokeAccess().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                          Toast.makeText(context, "Failed to log out!", Toast.LENGTH_LONG).show()
                        }
                      }
                    },
                    testTag = "logoutSection")
              }
            }
      })

  // Confirmation dialog for deleting the account
  if (showDeleteAccountDialog) {
    AlertDialog(
        onDismissRequest = { showDeleteAccountDialog = false },
        title = { Text(text = "Delete Account Confirmation") },
        text = { Text(text = "Are you sure you want to delete your account?") },
        confirmButton = {
          Button(
              onClick = {
                showDeleteAccountDialog = false
                val currentUserUid = userViewModel.getCurrentUser().value.uid
                meetingViewModel.deleteMeetingsForUser(userId = currentUserUid)
                userViewModel.deleteUserAccount(
                    navigationActions) // Calls the repository deletion internally
              },
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = Color.Red, contentColor = Color.White)) {
                Text("Yes")
              }
        },
        dismissButton = {
          Button(
              onClick = { showDeleteAccountDialog = false },
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = Color.Gray, contentColor = Color.White)) {
                Text("No")
              }
        })
  }
}
