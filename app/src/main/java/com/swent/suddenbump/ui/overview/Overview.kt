package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.theme.violetColor

data class User(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val profilePictureUrl: String,
    val birthDate: String,
    val mail: String,
    val phoneNumber: String,
    val relativeDist: Int,
)

@Composable
fun UserCard(user: User, navigationActions: NavigationActions) {
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
                Text(text = "Email: ${user.mail}")
                Text(text = "Phone: ${user.phoneNumber}")
            }
        }
    }
}

fun generateMockUsers(): List<User> {
    val relativeDistances = listOf(5, 5, 5, 5, 10, 10, 10, 15, 15, 15)
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

    return (1..10).map { index ->
        val relativeDist = relativeDistances[index % relativeDistances.size]
        val firstName = firstNames[index % firstNames.size]
        val lastName = lastNames[index % lastNames.size]
        User(
            uid = index.toString(),
            firstName = firstName,
            lastName = lastName,
            profilePictureUrl = "https://api.dicebear.com/9.x/lorelei/png?seed=${firstName}${lastName}",
            birthDate = birthDates[index % birthDates.size],
            mail = "${firstName.lowercase()}.${lastName.lowercase()}@example.com",
            phoneNumber = "123-456-78${index.toString().padStart(2, '0')}",
            relativeDist = relativeDist)
    }
}

@Composable
fun OverviewScreen(navigationActions: NavigationActions) {


  val mockUsers = generateMockUsers().sortedBy { it.relativeDist }

  Scaffold(
      topBar = {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
              FloatingActionButton(
                  onClick = { navigationActions.navigateTo(Screen.SETTINGS) },
                  modifier = Modifier.testTag("settingsFab")) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                  }
              Text(
                  modifier = Modifier.testTag("appName").weight(1f),
                  text = "SuddenBump!",
                  style =
                      MaterialTheme.typography.headlineMedium.copy(
                          fontSize = 30.sp, lineHeight = 44.sp),
                  color = violetColor,
                  fontWeight = FontWeight.Bold,
                  textAlign = TextAlign.Center)

              FloatingActionButton(
                  onClick = { navigationActions.navigateTo(Screen.FRIENDS_LIST) },
                  modifier = Modifier.testTag("seeFriendsFab")) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "See Friends")
                  }
            }
      },
      modifier = Modifier.testTag("overviewScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      content = { pd ->
        Column(
            modifier = Modifier.padding(pd), horizontalAlignment = Alignment.CenterHorizontally) {
              if (mockUsers.isNotEmpty()) {
                LazyColumn(modifier = Modifier.testTag("userList")) {
                  var currentDist: Int? = null
                  mockUsers.forEach { user ->
                    if (currentDist != user.relativeDist) {
                      currentDist = user.relativeDist
                      item {
                        Text(
                            text = "Distance: ${user.relativeDist} km",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            style = MaterialTheme.typography.headlineSmall)
                      }
                    }
                    item { UserCard(user = user, navigationActions) }
                  }
                }
              } else {
                Text(
                    text = "Looks like no friends are nearby",
                    modifier =
                        Modifier.testTag("noFriends").fillMaxWidth().padding(vertical = 8.dp),
                    style = MaterialTheme.typography.titleLarge)
              }
            }
      })
}

@Preview(showBackground = true)
@Composable
fun PreviewOverviewScreen() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  OverviewScreen(navigationActions)
}
