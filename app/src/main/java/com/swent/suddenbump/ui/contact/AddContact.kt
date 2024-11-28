package com.swent.suddenbump.ui.contact

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen

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
        !blockedUsers.map { user: User -> user.uid }.contains(it.uid)
      }

  val filteredUsers =
      recommendedUsers.filter { user ->
        user.firstName.contains(searchQuery.text, ignoreCase = true) ||
            user.lastName.contains(searchQuery.text, ignoreCase = true)
      }

  Scaffold(
      modifier = Modifier.testTag("addContactScreen"),
      topBar = {
        CenterAlignedTopAppBar(
            title = { Text("Add friends", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("backButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back")
                  }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White))
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
                  singleLine = true,
                  maxLines = 1)
              if (friendRequests.value.isNotEmpty() && searchQuery.text.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = "Friend requests",
                          style = MaterialTheme.typography.headlineSmall,
                          color = com.swent.suddenbump.ui.theme.Purple40,
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
                        color = com.swent.suddenbump.ui.theme.Purple40,
                        modifier = Modifier.padding(start = 8.dp))
                  }
              if (filteredUsers.isNotEmpty()) {
                LazyColumn(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .padding(16.dp)
                            .testTag("userList")) {
                      itemsIndexed(filteredUsers) { index, user ->
                        UserRecommendedRow(
                            user = user,
                            navigationActions = navigationActions,
                            userViewModel = userViewModel,
                            currentUser = userViewModel.getCurrentUser().collectAsState().value,
                            sentFriendRequests = sentFriendRequests,
                            friendRequests = friendRequests.value)
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
              AsyncImage(
                  model = "https://avatar.iran.liara.run/public/42",
                  contentDescription = null,
                  modifier =
                      Modifier.width(50.dp).height(50.dp).padding(8.dp).testTag("profileImage"))
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
    currentUser: User,
    user: User,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    sentFriendRequests: List<User>,
    friendRequests: List<User>
) {
  var showButton by remember {
    mutableStateOf(
        (!sentFriendRequests.map { user: User -> user.uid }.contains(user.uid)) &&
            !friendRequests.map { user: User -> user.uid }.contains(user.uid))
  }
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
              AsyncImage(
                  model = "https://avatar.iran.liara.run/public/42",
                  contentDescription = null,
                  modifier =
                      Modifier.width(50.dp).height(50.dp).padding(8.dp).testTag("profileImage"))
              Text(
                  text = "${user.firstName} ${user.lastName.first()}.",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  modifier = Modifier.testTag("userName"))
            }
        if (showButton) {
          IconButton(
              onClick = {
                userViewModel.sendFriendRequest(currentUser, user, { showButton = false }, {})
              },
              modifier = Modifier.testTag("denyButton")) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Send friend request",
                    tint = com.swent.suddenbump.ui.theme.Purple40)
              }
        }
      }
}
