package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen

@Composable
fun UserCard(user: User, navigationActions: NavigationActions, userViewModel: UserViewModel) {
  Card(
      onClick = {
        userViewModel.setSelectedContact(user)
        navigationActions.navigateTo(Screen.CONTACT)
      },
      modifier = Modifier.fillMaxWidth().height(150.dp).padding(8.dp).testTag(user.uid),
  ) {
    Row(
        modifier = Modifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      AsyncImage(
          model = user.profilePicture,
          contentDescription = null,
          modifier = Modifier.width(100.dp).height(100.dp).padding(8.dp))
      Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "${user.firstName} ${user.lastName}")
        Text(text = "Email: ${user.emailAddress}")
        Text(text = "Phone: ${user.phoneNumber}")
        Text(
            text =
                "latitude: ${user.lastKnownLocation?.latitude}, longitude: ${user.lastKnownLocation?.longitude}")
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
  val friends by userViewModel.getUserFriends().collectAsState(initial = emptyList())

  val filteredFriends =
      friends.filter { user ->
        user.firstName.contains(searchQuery.text, ignoreCase = true) ||
            user.lastName.contains(searchQuery.text, ignoreCase = true)
      }

  val friendRequests = userViewModel.getUserFriendRequests().collectAsState().value

  Scaffold(
      modifier = Modifier.testTag("friendsListScreen"),
      topBar = {
        CenterAlignedTopAppBar(
            title = {
              Text(
                  "Friends",
                  modifier = Modifier.testTag("title"),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis)
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("backButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back")
                  }
            },
            actions = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.ADD_CONTACT) },
                  modifier = Modifier.testTag("addContactButton")) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add new friends")
                  }
            },
        )
      },
      content = { pd ->
        Column(
            modifier = Modifier.padding(pd).testTag("friendsListContent"),
            horizontalAlignment = Alignment.CenterHorizontally) {
              TextField(
                  value = searchQuery,
                  onValueChange = { newValue -> searchQuery = newValue },
                  label = { Text("Search") },
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(horizontal = 10.dp, vertical = 10.dp)
                          .testTag("searchTextField"),
              )
              if (friendRequests.isNotEmpty() || searchQuery.text.isEmpty()) {
                Text(
                    text = "Friend Requests",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    style = MaterialTheme.typography.headlineSmall)
                LazyColumn(modifier = Modifier.testTag("friendRequestsList")) {
                  items(friendRequests) { user ->
                    UserCard(user = user, navigationActions, userViewModel)
                  }
                }
                Text(
                    text = "Friends",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    style = MaterialTheme.typography.headlineSmall)
              }
              if (filteredFriends.isNotEmpty()) {
                LazyColumn(modifier = Modifier.testTag("userList")) {
                  items(filteredFriends) { user ->
                    UserCard(user = user, navigationActions, userViewModel)
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

// @Preview(showBackground = true)
// @Composable
// fun PreviewAddContactScreen() {
//  val navController = rememberNavController()
//  val navigationActions = NavigationActions(navController)
//  FriendsListScreen(navigationActions,)
// }
