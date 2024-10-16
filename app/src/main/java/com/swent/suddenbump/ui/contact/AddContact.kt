package com.swent.suddenbump.ui.contact

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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen

data class User(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val profilePictureUrl: String,
    val birthDate: String,
    val mail: String,
    val phoneNumber: String,
)


fun generateMockUsers(): List<User> {
    val firstNames =
        listOf("John", "Jane", "Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Hank")
    val lastNames =
        listOf(
            "Doe",
            "Smith",
            "Johnson",
            "Williams",
            "Brown",
            "Jones",
            "Garcia",
            "Miller",
            "Davis",
            "Rodriguez")
    val birthDates =
        listOf(
            "01 Janvier 2002",
            "28 Juin 1998",
            "15 Mars 1995",
            "22 Avril 1990",
            "30 Mai 1985",
            "10 Juillet 1980",
            "05 Août 1975",
            "12 Septembre 1970",
            "18 Octobre 1965",
            "25 Novembre 1960")

    return (10..15).map { index ->
        val firstName = firstNames[index % firstNames.size]
        val lastName = lastNames[index % lastNames.size]
        User(
            uid = index.toString(),
            firstName = firstName,
            lastName = lastName,
            profilePictureUrl = "https://api.dicebear.com/9.x/lorelei/png?seed=${firstName}${lastName}",
            birthDate = birthDates[index % birthDates.size],
            mail = "${firstName.lowercase()}.${lastName.lowercase()}@example.com",
            phoneNumber = "123-456-78${index.toString().padStart(2, '0')}")
    }
}


@Composable
fun UserCard(user: User, navigationActions: NavigationActions) {
    //generate random integer between 0 and 10
    val randomInt = (0..10).random()
    Card(
        onClick = { navigationActions.navigateTo(Screen.CONTACT) },
        modifier = Modifier.fillMaxWidth().height(150.dp).padding(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = user.profilePictureUrl,
                contentDescription = null,
                modifier = Modifier.width(100.dp).height(100.dp).padding(8.dp))
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "${user.firstName} ${user.lastName}")
                Text(text = "${randomInt} friends in common")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(navigationActions: NavigationActions) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var mockUsers = generateMockUsers()

    mockUsers =
        mockUsers.filter { user ->
            user.firstName.contains(searchQuery.text, ignoreCase = true) ||
                    user.lastName.contains(searchQuery.text, ignoreCase = true)
        }

    Scaffold(
        modifier = Modifier.testTag("friendsListScreen"),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Add contact",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navigationActions.goBack()
                    }) {
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
                modifier = Modifier.padding(pd), horizontalAlignment = Alignment.CenterHorizontally) {
                TextField(
                    value = searchQuery,
                    onValueChange = { newValue -> searchQuery = newValue },
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                if (mockUsers.isNotEmpty()) {
                    LazyColumn { items(mockUsers) { user -> UserCard(user = user, navigationActions) } }
                } else {
                    Text(text = "Looks like no user corresponds to your query")
                }
            }
        })
}

@Preview(showBackground = true)
@Composable
fun PreviewAddContactScreen() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    AddContactScreen(navigationActions)
}
