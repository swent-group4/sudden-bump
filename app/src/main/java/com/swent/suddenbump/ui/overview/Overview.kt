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
import coil3.compose.AsyncImage
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
  val friendsGroupedByDistance by userViewModel.friendsGroupedByDistance.collectAsState()
  
  // Start updating friends' locations when the screen is composed
  LaunchedEffect(Unit) { userViewModel.startUpdatingFriendsLocations() }

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
        if (friendsGroupedByDistance.isEmpty()) {
          // Display a loading indicator or a message
          Box(
              modifier = Modifier.fillMaxSize().background(Color.Black),
              contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
              }
        } else {
          LazyColumn(
              modifier =
                  Modifier.fillMaxHeight()
                      .background(Color.Black)
                      .padding(pd)
                      .padding(horizontal = 16.dp),
              horizontalAlignment = Alignment.CenterHorizontally) {
                UserViewModel.DistanceRange.values().forEach { distanceRange ->
                  val friendsInRange = friendsGroupedByDistance[distanceRange] ?: emptyList()
                  if (friendsInRange.isNotEmpty()) {
                    item { CategoryHeader(distanceRange.label) }
                    items(friendsInRange) { friend ->
                      UserRow(
                          user = friend,
                          navigationActions = navigationActions,
                          userViewModel = userViewModel)
                    }
                  }
                }
              }
        }
      })
}

@Composable
fun CategoryHeader(title: String) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).testTag(title),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(16.dp)) {
          drawCircle(color = com.swent.suddenbump.ui.theme.Purple40, radius = size.minDimension / 2)
        }
        Text(
            text = title,
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
              Text(
                  text = "${user.firstName} ${user.lastName.first()}.",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = Color.White)
              Text(
                  text = "Lausanne, Switzerland",
                  style = MaterialTheme.typography.bodyLarge,
                  color = Color.White)
            }
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = "Message",
            tint = com.swent.suddenbump.ui.theme.Purple40)
      }
}
