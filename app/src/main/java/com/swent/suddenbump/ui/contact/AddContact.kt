package com.swent.suddenbump.ui.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.theme.Pinkish
import com.swent.suddenbump.ui.utils.CustomCenterAlignedTopBar
import com.swent.suddenbump.ui.utils.UserProfileImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
  val req = userViewModel.getUserFriendRequests().collectAsState().value

  val friendRequests = remember { mutableStateOf(req) }
  val sentFriendRequests = userViewModel.getSentFriendRequests().collectAsState().value
  val blockedUsers = userViewModel.getBlockedFriends().collectAsState().value

  val recommendedUsers =
      userViewModel.getUserRecommendedFriends().collectAsState().value.filter {
        !blockedUsers.map { user: User -> user.uid }.contains(it.user.uid) &&
            !sentFriendRequests.map { user: User -> user.uid }.contains(it.user.uid) &&
            !friendRequests.value.map { user: User -> user.uid }.contains(it.user.uid)
      }

  val allNonBlockedUsers = userViewModel.getAllNonBlockedUsers().collectAsState().value

  val sortedRecommendedUsers =
      recommendedUsers
          .filter { data ->
            data.user.firstName.contains(searchQuery.text, ignoreCase = true) ||
                data.user.lastName.contains(searchQuery.text, ignoreCase = true)
          }
          .sortedByDescending { it.friendsInCommon }
          .take(5)

    //
  val filteredUsers =
      allNonBlockedUsers.filter { user ->
        user.firstName.contains(searchQuery.text, ignoreCase = true) ||
            user.lastName.contains(searchQuery.text, ignoreCase = true)
      }

  println("Non blocked users: $allNonBlockedUsers")
  println("Filtered users: $filteredUsers")

  Scaffold(
      modifier = Modifier.testTag("addContactScreen"),
      topBar = {
        CustomCenterAlignedTopBar(title = "Add friends", navigationActions = navigationActions)
      },
      content = { pd ->
        Column(
            modifier =
                Modifier.fillMaxHeight()
                    .padding(pd)
                    .background(Color.Black)
                    .padding(horizontal = 16.dp)
                    .testTag("addContactContent"),
            horizontalAlignment = Alignment.CenterHorizontally) {
              TextField(
                  value = searchQuery,
                  onValueChange = { newValue -> searchQuery = newValue },
                  label = { Text("Search") },
                  leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = com.swent.suddenbump.ui.theme.Purple40)
                  },
                  colors =
                      TextFieldDefaults.colors(
                          unfocusedIndicatorColor =
                              Color.Transparent, // Mask mask indicator under search bar
                          focusedIndicatorColor = Color.Transparent, // Mask also for the focus
                          unfocusedContainerColor = Color.White,
                          unfocusedLabelColor = com.swent.suddenbump.ui.theme.Purple40,
                          unfocusedTextColor = com.swent.suddenbump.ui.theme.Purple40,
                          focusedContainerColor = Color.White,
                          focusedLabelColor = com.swent.suddenbump.ui.theme.Purple40,
                          focusedTextColor = com.swent.suddenbump.ui.theme.Purple40,
                      ),
                  shape = RoundedCornerShape(16.dp), // Adjust the corner radius as needed
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(horizontal = 10.dp, vertical = 10.dp)
                          .testTag("searchTextField"),
              )
              if (friendRequests.value.isNotEmpty() && searchQuery.text.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = "Friend requests",
                          style = MaterialTheme.typography.headlineSmall,
                          color = Pinkish,
                          modifier = Modifier.padding(start = 8.dp))
                    }
                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(16.dp)
                            .testTag("requestRow")) {
                      friendRequests.value.forEachIndexed { index, user ->
                        UserRequestRow(
                            user = user,
                            navigationActions = navigationActions,
                            userViewModel = userViewModel,
                            currentUser = userViewModel.getCurrentUser().collectAsState().value,
                            requestList = friendRequests)
                        if (index < friendRequests.value.size - 1) {
                          HorizontalDivider(
                              color = Color.Gray,
                              thickness = 0.5.dp,
                              modifier = Modifier.padding(vertical = 4.dp).testTag("divider"))
                        }
                      }
                    }
              }
              Row(
                  modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Recommended friends",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Pinkish,
                        modifier = Modifier.padding(start = 8.dp))
                  }
              if (searchQuery.text.isEmpty() && recommendedUsers.isNotEmpty()) {
                LazyColumn(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(16.dp)
                            .testTag("userList")) {
                      itemsIndexed(sortedRecommendedUsers) { index, data ->
                        UserRecommendedRow(
                            user = data.user,
                            friendsInCommon = data.friendsInCommon,
                            navigationActions = navigationActions,
                            userViewModel = userViewModel,
                        )
                        if (index < filteredUsers.size - 1) {
                          HorizontalDivider(
                              color = Color.Gray,
                              thickness = 0.5.dp,
                              modifier = Modifier.padding(vertical = 4.dp).testTag("divider"))
                        }
                      }
                    }
              } else if (recommendedUsers.isEmpty() &&
                  filteredUsers.isNotEmpty() &&
                  searchQuery.text.isEmpty()) {
                LazyColumn(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(16.dp)
                            .testTag("userList")) {
                      itemsIndexed(filteredUsers.take(5)) { index, data ->
                        UserRecommendedRow(
                            user = data,
                            friendsInCommon = 0,
                            navigationActions = navigationActions,
                            userViewModel = userViewModel,
                        )
                        if (index < filteredUsers.size - 1) {
                          HorizontalDivider(
                              color = Color.Gray,
                              thickness = 0.5.dp,
                              modifier = Modifier.padding(vertical = 4.dp).testTag("divider"))
                        }
                      }
                    }
              } else if (filteredUsers.isNotEmpty() && searchQuery.text.isNotEmpty()) {
                LazyColumn(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(16.dp)
                            .testTag("userList")) {
                      itemsIndexed(filteredUsers.take(5)) { index, data ->
                        UserRecommendedRow(
                            user = data,
                            friendsInCommon = 0,
                            navigationActions = navigationActions,
                            userViewModel = userViewModel,
                        )
                        if (index < filteredUsers.size - 1) {
                          HorizontalDivider(
                              color = Color.Gray,
                              thickness = 0.5.dp,
                              modifier = Modifier.padding(vertical = 4.dp).testTag("divider"))
                        }
                      }
                    }
              } else {
                Text(
                    text = "Looks like no user corresponds to your query",
                    modifier = Modifier.testTag("noUsersText"))
              }
            }
      })
}

@Composable
fun UserRequestRow(
    currentUser: User,
    user: User,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    requestList: MutableState<List<User>>
) {
  Row(
      modifier =
          Modifier.fillMaxWidth().clickable {
            userViewModel.setSelectedContact(user)
            navigationActions.navigateTo(Screen.CONTACT)
          },
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
              UserProfileImage(user, 40)
              Text(
                  text = "${user.firstName} ${user.lastName.first()}.",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  modifier = Modifier.testTag("userName"))
            }
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
              onClick = {
                userViewModel.declineFriendRequest(
                    currentUser,
                    user,
                    {
                      val updatedList = requestList.value.toMutableList()
                      updatedList.remove(user)
                      requestList.value = updatedList
                    },
                    {})
              },
              modifier = Modifier.testTag("denyButton")) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Message",
                    tint = Color.Red)
              }
          IconButton(
              onClick = {
                userViewModel.acceptFriendRequest(
                    currentUser,
                    user,
                    {
                      val updatedList = requestList.value.toMutableList()
                      updatedList.remove(user)
                      requestList.value = updatedList
                    },
                    {})
              },
              modifier = Modifier.testTag("acceptButton")) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Message",
                    tint = Color.Green)
              }
        }
      }
}

@Composable
fun UserRecommendedRow(
    user: User,
    friendsInCommon: Int,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .clickable {
                userViewModel.setSelectedContact(user)
                navigationActions.navigateTo(Screen.CONTACT)
              }
              .testTag("recommendedUserRow"),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
              UserProfileImage(user, 40)
              Text(
                  text = "${user.firstName} ${user.lastName.first()}.",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  modifier = Modifier.testTag("userName"))

              if (friendsInCommon > 0) {
                Text(
                    text = "${friendsInCommon} friends in common",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.testTag("friendsInCommon"))
              }
            }
      }
}
