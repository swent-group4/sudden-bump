package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swent.suddenbump.model.user.DistanceCategory
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.theme.violetColor

@Composable
fun OverviewScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val currentUser by userViewModel.getCurrentUser().collectAsState()
  val groupedFriends by userViewModel.groupedFriends.collectAsState()

  // Charge les emplacements des amis lorsque l'écran est composé
  LaunchedEffect(Unit) { userViewModel.loadFriends() }

  Scaffold(
      topBar = {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .background(Color.Black)
                    .padding(horizontal = 16.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
              FloatingActionButton(
                  onClick = { navigationActions.navigateTo(Screen.SETTINGS) },
                  modifier = Modifier.testTag("settingsFab"),
                  containerColor = com.swent.suddenbump.ui.theme.Purple40) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White)
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
                  onClick = { navigationActions.navigateTo(Screen.ADD_CONTACT) },
                  modifier = Modifier.testTag("seeFriendsFab"),
                  containerColor = com.swent.suddenbump.ui.theme.Purple40) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "See Friends",
                        tint = Color.White)
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
        LazyColumn(
            modifier =
                Modifier.fillMaxHeight()
                    .background(Color.Black)
                    .padding(pd)
                    .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              if (groupedFriends.isNotEmpty()) {
                groupedFriends.forEach { (category, friendsList) ->
                  item { CategoryHeader(category) }
                  items(friendsList) { (friend, _) ->
                    UserRow(
                        user = friend,
                        navigationActions = navigationActions,
                        userViewModel = userViewModel)
                  }
                }
              } else {
                item {
                  Text(
                      text = "No friends nearby, add friends to see their location",
                      color = Color.White,
                      modifier =
                          Modifier.testTag("noFriends").fillMaxWidth().padding(vertical = 8.dp),
                      style = MaterialTheme.typography.titleLarge,
                      textAlign = TextAlign.Center)
                }
              }
            }
      })
}

@Composable
fun CategoryHeader(category: DistanceCategory) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).testTag(category.title),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(16.dp)) {
          drawCircle(color = com.swent.suddenbump.ui.theme.Purple40, radius = size.minDimension / 2)
        }
        Text(
            text = category.title,
            style = MaterialTheme.typography.headlineSmall,
            color = com.swent.suddenbump.ui.theme.Purple40,
            modifier = Modifier.padding(start = 8.dp))
      }
}

@Composable
fun UserRow(user: User, navigationActions: NavigationActions, userViewModel: UserViewModel) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .clickable {
                userViewModel.setSelectedContact(user)
                navigationActions.navigateTo(Screen.CONTACT)
              }
              .testTag(user.uid),
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
              Column {
                Text(
                    text = "${user.firstName} ${user.lastName.first()}.",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White)
                Text(
                    text = "Lausanne, Switzerland",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White)
              }
            }
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = "Message",
            tint = com.swent.suddenbump.ui.theme.Purple40)
      }
}
