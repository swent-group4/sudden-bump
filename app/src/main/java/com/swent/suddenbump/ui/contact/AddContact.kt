package com.swent.suddenbump.ui.contact

import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UserCard(user: User, navigationActions: NavigationActions) {
    Card(
        onClick = { navigationActions.navigateTo(Screen.CONTACT) },
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                bitmap = user.profilePicture!!,
                contentDescription = null,
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
                    .padding(8.dp)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "${user.firstName} ${user.lastName}")
//                Text(text = "${randomInt} friends in common")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    isTesting: Boolean = false
) {

    val timeJob: Long = 400

    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var usersQueryResponse by remember { mutableStateOf(emptyList<User>()) }

    var expanded by remember { mutableStateOf(false) }
    var job: Job? by remember { mutableStateOf(null) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.testTag("addContactScreen"),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add contact", maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
            )
        },
        content = { pd ->
            Column(
                modifier = Modifier
                    .padding(pd)
                    .testTag("addContactContent"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { newValue ->
                        searchQuery = newValue

                        Log.i("AddContact", usersQueryResponse.toString())

                        job?.cancel()
                        if (searchQuery.text.isNotBlank()) {
                            if (isTesting) {
                                userViewModel.searchQueryAddContact(
                                    searchQuery.text,
                                    onSuccess = {
                                        usersQueryResponse = it
                                        Log.i("AddContact", it.toString())
                                    },
                                    onFailure = {})
                            } else {
                                job = CoroutineScope(Dispatchers.IO).launch {
                                    delay(timeJob)
                                    userViewModel.searchQueryAddContact(
                                        searchQuery.text,
                                        onSuccess = {
                                            usersQueryResponse = it
                                        },
                                        onFailure = {})
                                }
                            }
                        } else {
                            usersQueryResponse = emptyList()
                        }
                    },
                    label = { Text("Search") },
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .testTag("searchTextField"),
                )
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("recommendedRow"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "Recommended",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
                if (usersQueryResponse.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.testTag("userList")) {
                        items(usersQueryResponse) { user ->
                            UserCard(
                                user = user,
                                navigationActions
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Looks like no user corresponds to your query",
                        modifier = Modifier.testTag("noUsersText")
                    )
                }
            }
        })
}
