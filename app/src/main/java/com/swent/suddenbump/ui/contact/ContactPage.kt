package com.swent.suddenbump.ui.contact

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.utils.UserProfileImage

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ContactScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val user = userViewModel.getSelectedContact().collectAsState().value
  var showDialog by remember { mutableStateOf(false) }

  var isFriend =
      userViewModel.getUserFriends().collectAsState().value.map { it.uid }.contains(user.uid)
  var isFriendRequest =
      userViewModel.getUserFriendRequests().collectAsState().value.map { it.uid }.contains(user.uid)
  var isFriendRequestSent =
      userViewModel.getSentFriendRequests().collectAsState().value.map { it.uid }.contains(user.uid)

  Scaffold(
      modifier = Modifier.fillMaxSize().background(Color.Black).testTag("contactScreen"),
      topBar = {
        TopAppBar(
            modifier = Modifier.background(Color.Black),
            title = { Text("Contact") },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("backButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            },
            actions = {
              var expanded by remember { mutableStateOf(false) }

              IconButton(
                  onClick = { expanded = true }, modifier = Modifier.testTag("moreOptionsButton")) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.White)
                  }

              DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    modifier = Modifier.testTag("blockUserButton"),
                    onClick = {
                      expanded = false
                      showDialog = true
                    },
                    text = { Text("Block User") })
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White))
      },
      content = { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black)
                    .padding(padding)
                    .testTag("contactContent"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Top,
          ) {
            UserProfileImage(user, 120)
            Column(
                Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Text(
                  user.firstName + " " + user.lastName,
                  style =
                      androidx.compose.ui.text.TextStyle(
                          fontSize = 20.sp, // Adjust the size as needed
                          fontWeight = FontWeight.Bold),
                  color = Color.White,
                  modifier = Modifier.testTag("userName"))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 50.dp, vertical = 10.dp)
                        .testTag("phoneCard")) {
                  Text(modifier = Modifier.padding(10.dp), text = "Phone: " + user.phoneNumber)
                }

            Card(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 50.dp, vertical = 10.dp)
                        .testTag("emailCard")) {
                  Text(modifier = Modifier.padding(10.dp), text = "Email: " + user.emailAddress)
                }
          }

          if (isFriend) {
            Button(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 50.dp, vertical = 30.dp)
                        .testTag("sendMessageButton"),
                colors = ButtonDefaults.buttonColors(com.swent.suddenbump.ui.theme.Purple40),
                onClick = {
                  userViewModel.user = user
                  navigationActions.navigateTo(Screen.CHAT)
                }) {
                  Text("Send a message", color = Color.White)
                }
          } else if (isFriendRequest) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 50.dp)) {
              Button(
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(vertical = 5.dp)
                          .testTag("acceptRequestButton"),
                  colors = ButtonDefaults.buttonColors(com.swent.suddenbump.ui.theme.Purple40),
                  onClick = {
                    userViewModel.acceptFriendRequest(
                        userViewModel.getCurrentUser().value,
                        friend = user,
                        onSuccess = {
                          isFriend = true
                          isFriendRequest = false
                        },
                        onFailure = { println("Error accepting friend request") })
                  }) {
                    Text(
                        "Accept friend request",
                        color = Color.White,
                    )
                  }
              Button(
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(vertical = 10.dp)
                          .testTag("declineRequestButton"),
                  colors = ButtonDefaults.buttonColors(com.swent.suddenbump.ui.theme.Purple40),
                  onClick = {
                    userViewModel.declineFriendRequest(
                        userViewModel.getCurrentUser().value,
                        friend = user,
                        onSuccess = {
                          isFriend = false
                          isFriendRequest = false
                        },
                        onFailure = { println("Error declining friend request") })
                  }) {
                    Text(
                        "Decline friend request",
                        color = Color.White,
                    )
                  }
            }
          } else if (isFriendRequestSent) {
            Text(
                "Friend request sent",
                color = Color.White,
                modifier = Modifier.testTag("friendRequestSentText"),
            )
          } else {
            Button(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 50.dp, vertical = 30.dp)
                        .testTag("addToContactsButton"),
                colors = ButtonDefaults.buttonColors(com.swent.suddenbump.ui.theme.Purple40),
                onClick = {
                  userViewModel.sendFriendRequest(
                      userViewModel.getCurrentUser().value,
                      friend = user,
                      onSuccess = { isFriendRequestSent = true },
                      onFailure = { println("Error sending friend request") })
                }) {
                  Text(
                      "Send Friend Request",
                      color = Color.White,
                  )
                }
          }
        }

        if (showDialog) {
          AlertDialog(
              onDismissRequest = { showDialog = false },
              title = { Text("Block User") },
              text = { Text("Are you sure you want to block this user?") },
              confirmButton = {
                Button(
                    onClick = {
                      showDialog = false
                      userViewModel.blockUser(
                          user = userViewModel.getCurrentUser().value,
                          blockedUser = user,
                          onSuccess = { navigationActions.goBack() },
                          onFailure = { println("Error blocking user") })
                    }) {
                      Text("Yes")
                    }
              },
              dismissButton = { Button(onClick = { showDialog = false }) { Text("No") } })
        }
      })
}
