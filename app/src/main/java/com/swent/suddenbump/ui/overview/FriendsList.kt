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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen

@Composable
fun UserCard(user: User, navigationActions: NavigationActions, userViewModel: UserViewModel) {
    Card(
        onClick = {
            userViewModel.user = user
            navigationActions.navigateTo(Screen.CONTACT) },
        modifier = Modifier.fillMaxWidth().height(150.dp).padding(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = user.profilePictureUrl,
                contentDescription = null,
                placeholder = painterResource(com.swent.suddenbump.R.drawable.profile), // Add your drawable here
                error = painterResource(com.swent.suddenbump.R.drawable.profile), //
                modifier = Modifier.width(100.dp).height(100.dp).padding(8.dp))
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text =  "${user?.firstName?.replaceFirstChar { it.uppercase() }} ${user?.lastName?.replaceFirstChar { it.uppercase() }}")
                Text(text = "Email: ${user.emailAddress}")
                Text(text = "Phone: ${user.phoneNumber}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreen(navigationActions: NavigationActions, userViewModel: UserViewModel =  viewModel(factory = UserViewModel.Factory)) {
    var searchQuery by remember { mutableStateOf("") }

    // Fetch the full list of users and store it
    val allUsers by userViewModel.getUserFriends().collectAsState(initial = emptyList())

    // Filtered list based on the search query
    val filteredUsers = remember(searchQuery, allUsers) {
        allUsers.filter { user ->
            user.firstName.contains(searchQuery, ignoreCase = true) ||
                    user.lastName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = Modifier.testTag("friendsListScreen"),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Friends", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(
                        onClick = { navigationActions.goBack() },
                        modifier = Modifier.testTag("backButton")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navigationActions.navigateTo(Screen.ADD_CONTACT) },
                        modifier = Modifier.testTag("addContactButton")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add new friends")
                    }
                },
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier.padding(padding).testTag("friendsListContent"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { newValue -> searchQuery = newValue },
                    label = { Text("Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .testTag("searchTextField"),
                )

                if (filteredUsers.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.testTag("userList")) {
                        items(filteredUsers) { user ->
                            UserCard(user = user, navigationActions = navigationActions, userViewModel = userViewModel)
                        }
                    }
                } else {
                    Text(
                        text = "Looks like no user corresponds to your query",
                        modifier = Modifier.testTag("noUsersText")
                    )
                }
            }
        }
    )
}



@Preview(showBackground = true)
@Composable
fun PreviewAddContactScreen() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
    FriendsListScreen(navigationActions, userViewModel)
}
