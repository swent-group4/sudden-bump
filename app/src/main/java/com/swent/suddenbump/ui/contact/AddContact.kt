package com.swent.suddenbump.ui.contact

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions

@Composable
fun UserCard(user: User, navigationActions: NavigationActions, userViewModel: UserViewModel) {
  // generate random integer between 0 and 10
  val randomInt = (0..10).random()
  val context = LocalContext.current
  Card(
      onClick = {
        userViewModel.addUserFriend(
            user,
            onSuccess = { Toast.makeText(context, "Friend added", Toast.LENGTH_SHORT).show() },
            onFailure = { Toast.makeText(context, "Friend added", Toast.LENGTH_SHORT).show() })
      },
      modifier = Modifier.fillMaxWidth().height(150.dp).padding(8.dp),
  ) {
    Row(
        modifier = Modifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      AsyncImage(
          model = user.profilePictureUrl,
          contentDescription = null,
          placeholder =
              painterResource(com.swent.suddenbump.R.drawable.profile), // Add your drawable here
          error = painterResource(com.swent.suddenbump.R.drawable.profile), //
          modifier = Modifier.width(100.dp).height(100.dp).padding(8.dp))
      Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "${user.firstName} ${user.lastName}")
        //        Text(text = "${randomInt} friends in common")
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
) {
  var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
  val mockUsers by userViewModel.getAllUsers().collectAsState(emptyList())

  Scaffold(
      modifier = Modifier.testTag("addContactScreen"),
      topBar = {
        CenterAlignedTopAppBar(
            title = { Text("Add contact", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("backButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back")
                  }
            },
        )
      },
      content = { pd ->
        Column(
            modifier = Modifier.padding(pd).testTag("addContactContent"),
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
              Row(
                  modifier =
                      Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("recommendedRow"),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "Recommended",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodyLarge)
                    HorizontalDivider(modifier = Modifier.weight(1f))
                  }
              if (mockUsers.isNotEmpty()) {
                LazyColumn(modifier = Modifier.testTag("userList")) {
                  items(mockUsers) { user ->
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
