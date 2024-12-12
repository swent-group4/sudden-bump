package com.swent.suddenbump.ui.overview

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.suddenbump.R
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Purple40
import com.swent.suddenbump.ui.utils.CustomCenterAlignedTopBar

@Composable
fun BlockedUsersScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val blockedUsers = userViewModel.getBlockedFriends().collectAsState()

  Scaffold(
      modifier = Modifier.testTag("blockedUsersScreen"),
      topBar = {
        CustomCenterAlignedTopBar(title = "Blocked Users", navigationActions = navigationActions)
      }) { paddingValues ->
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black)
                    .padding(paddingValues)
                    .padding(16.dp)) {
              if (blockedUsers.value.isEmpty()) {
                EmptyBlockedUsersMessage()
              } else {
                BlockedUsersList(
                    blockedUsers = blockedUsers.value,
                    onUnblock = { user ->
                      userViewModel.unblockUser(
                          blockedUser = user,
                          onSuccess = {
                            // The list will be automatically updated through the StateFlow
                          },
                          onFailure = { exception ->
                            Log.e(
                                "BlockedUsersScreen",
                                "Failed to unblock user: ${exception.message}")
                            // You might want to show an error message to the user here
                          })
                    })
              }
            }
      }
}

@Composable
private fun EmptyBlockedUsersMessage() {
  Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
    Text(
        text = "You haven't blocked any users",
        style =
            MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White),
        modifier = Modifier.testTag("emptyBlockedUsersMessage"))
  }
}

@Composable
private fun BlockedUsersList(blockedUsers: List<User>, onUnblock: (User) -> Unit) {
  LazyColumn(
      modifier = Modifier.fillMaxSize().padding(16.dp).testTag("blockedUsersList"),
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(blockedUsers) { user -> BlockedUserItem(user = user, onUnblock = onUnblock) }
      }
}

@Composable
private fun BlockedUserItem(user: User, onUnblock: (User) -> Unit) {
  var showConfirmDialog by remember { mutableStateOf(false) }

  Card(
      modifier = Modifier.fillMaxWidth().testTag("blockedUserItem"),
      shape = RoundedCornerShape(8.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Profile Picture
                    Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(Color.Gray)) {
                      if (user.profilePicture != null) {
                        Image(
                            bitmap = user.profilePicture,
                            contentDescription = "Profile picture of ${user.firstName}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize())
                      } else {
                        Image(
                            painter = painterResource(id = R.drawable.settings_user),
                            contentDescription = "Default profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize())
                      }
                    }

                    // User Name
                    Column {
                      Text(
                          text = "${user.firstName} ${user.lastName}",
                          style =
                              MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    }
                  }

              // Unblock Button
              Button(
                  onClick = { showConfirmDialog = true },
                  colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                  modifier = Modifier.testTag("unblockButton")) {
                    Text("Unblock")
                  }
            }
      }

  if (showConfirmDialog) {
    AlertDialog(
        onDismissRequest = { showConfirmDialog = false },
        title = {
          Text(
              text = "Unblock User",
              style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        },
        text = {
          Text(
              text = "Are you sure you want to unblock ${user.firstName} ${user.lastName}?",
              style = MaterialTheme.typography.bodyLarge)
        },
        confirmButton = {
          Button(
              onClick = {
                onUnblock(user)
                showConfirmDialog = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = Purple40)) {
                Text("Yes")
              }
        },
        dismissButton = {
          Button(
              onClick = { showConfirmDialog = false },
              colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                Text("No")
              }
        })
  }
}
