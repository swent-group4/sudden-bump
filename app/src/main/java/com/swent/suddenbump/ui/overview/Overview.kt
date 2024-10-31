package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.theme.violetColor

@Composable
fun OverviewScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
) {
  val users by userViewModel.getUserFriends().collectAsState(emptyList())

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
              if (users.isNotEmpty()) {
                LazyColumn(modifier = Modifier.testTag("userList")) {
                  var currentDist: Int? = null
                  users.forEach { user ->
                    if (currentDist != user.relativeDist) {
                      currentDist = user.relativeDist
                      item {
                        Text(
                            text = "Distance: ${user.relativeDist} km",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            style = MaterialTheme.typography.headlineSmall)
                      }
                    }
                    item { UserCard(user = user, navigationActions, userViewModel) }
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
  val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
  OverviewScreen(navigationActions, userViewModel)
}
