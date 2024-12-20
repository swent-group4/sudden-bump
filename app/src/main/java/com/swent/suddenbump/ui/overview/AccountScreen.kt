package com.swent.suddenbump.ui.overview

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Route
import com.swent.suddenbump.ui.theme.Pink40
import com.swent.suddenbump.ui.utils.AccountOption
import com.swent.suddenbump.ui.utils.CustomCenterAlignedTopBar

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
                // Language Section with Pop-Up Menu
                Box(modifier = Modifier.testTag("languageSection")) {
                  AccountOption(
                      label = "Language",
                      backgroundColor = Color.White,
                      onClick = { isLanguageMenuExpanded = true },
                      testTag = "")
                  DropdownMenu(
                      expanded = isLanguageMenuExpanded,
                      onDismissRequest = { isLanguageMenuExpanded = false }) {
                        listOf("English", "French", "German").forEach { language ->
                          DropdownMenuItem(
                              text = { Text(language) },
                              onClick = {
                                selectedLanguage = language
                                isLanguageMenuExpanded = false
                              },
                              modifier = Modifier.testTag("languageMenuItem_$language"))
                        }
                      }
                }
              }

              item {
                AccountOption(
                    label = "Delete Account",
                    backgroundColor = Pink40,
                    onClick = { showDeleteAccountDialog = true },
                    testTag = "deleteAccountSection")
              }

              item {
                AccountOption(
                    label = "Log out",
                    backgroundColor = Pink40,
                    onClick = {
                      userViewModel.logout()
                      navigationActions.navigateTo(Route.AUTH)
                      Toast.makeText(context, "Logged out successfully !", Toast.LENGTH_LONG).show()
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

@Composable
fun ChangeSection(
    title: String,
    placeholder: String,
    sectionTag: String,
    placeholderTag: String,
    buttonTag: String
) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .background(Color.White, RoundedCornerShape(8.dp))
              .padding(16.dp)
              .testTag(sectionTag)) {
        Text(
            text = title,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black))
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(48.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                    .testTag(placeholderTag)) {
              Text(
                  text = placeholder,
                  style =
                      MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, color = Color.Gray),
                  modifier = Modifier.align(Alignment.CenterStart).padding(8.dp))
            }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { /* Do nothing for now */},
            modifier = Modifier.fillMaxWidth().testTag(buttonTag),
            colors = ButtonDefaults.buttonColors(containerColor = Pink40)) {
              Text(
                  text = "Update ${title.split(" ")[1]}",
                  style =
                      MaterialTheme.typography.bodyLarge.copy(
                          fontWeight = FontWeight.Bold, color = Color.White))
            }
      }
}
