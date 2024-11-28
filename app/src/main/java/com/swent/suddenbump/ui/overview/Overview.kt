package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
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
  val users by userViewModel.getUserFriends().collectAsState()

  // Load friends' locations when the screen is composed
  LaunchedEffect(Unit) { userViewModel.loadFriends() }

  // Compute distances and group friends
  val friendsWithDistances =
      users.mapNotNull { friend ->
        val distance = userViewModel.getRelativeDistance(friend)
        if (distance != Float.MAX_VALUE) {
          friend to distance
        } else {
          null
        }
      }

  // Group friends into categories based on distance
  val friendsWithin5km = friendsWithDistances.filter { it.second <= 5000f }
  val friendsWithin10km = friendsWithDistances.filter { it.second > 5000f && it.second <= 10000f }
  val friendsWithin20km = friendsWithDistances.filter { it.second > 10000f && it.second <= 20000f }
  val friendsFurther = friendsWithDistances.filter { it.second > 20000f }

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
              if (friendsWithin5km.isNotEmpty()) {
                item { CategoryHeader("Within 5km") }
                items(friendsWithin5km) { (friend, _) ->
                  UserRow(
                      user = friend,
                      navigationActions = navigationActions,
                      userViewModel = userViewModel)
                }
              }
              if (friendsWithin10km.isNotEmpty()) {
                item { CategoryHeader("Within 10km") }
                items(friendsWithin10km) { (friend, _) ->
                  UserRow(
                      user = friend,
                      navigationActions = navigationActions,
                      userViewModel = userViewModel)
                }
              }
              if (friendsWithin20km.isNotEmpty()) {
                item { CategoryHeader("Within 20km") }
                items(friendsWithin20km) { (friend, _) ->
                  UserRow(
                      user = friend,
                      navigationActions = navigationActions,
                      userViewModel = userViewModel)
                }
              }
              if (friendsFurther.isNotEmpty()) {
                item { CategoryHeader("Further") }
                items(friendsFurther) { (friend, _) ->
                  UserRow(
                      user = friend,
                      navigationActions = navigationActions,
                      userViewModel = userViewModel)
                }
              }
              /*if (friendsWithDistances.isEmpty()) {
                  item {
                      Text(
                          text = "Looks like no friends are nearby",
                          color = Color.White,
                          modifier = Modifier
                              .testTag("noFriends")
                              .fillMaxWidth()
                              .padding(vertical = 8.dp),
                          style = MaterialTheme.typography.titleLarge
                      )
                  }
              }*/
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
              .testTag(user.uid)
              .semantics(mergeDescendants = false) {},
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
              if (user.profilePicture != null) {
                Image(
                    bitmap = user.profilePicture,
                    contentDescription = "Existing profile pictures",
                    modifier =
                        Modifier.width(50.dp)
                            .height(50.dp)
                            .padding(8.dp)
                            .testTag("profileImageNotNull_${user.uid}"))
              } else {
                AsyncImage(
                    model = "https://avatar.iran.liara.run/public/42",
                    contentDescription = "Non-Existing profile pictures",
                    modifier =
                        Modifier.width(50.dp)
                            .height(50.dp)
                            .padding(8.dp)
                            .testTag("profileImage_${user.uid}"))
              }
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
