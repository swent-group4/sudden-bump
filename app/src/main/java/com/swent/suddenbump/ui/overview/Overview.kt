package com.swent.suddenbump.ui.overview

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.suddenbump.R
import com.swent.suddenbump.model.user.DistanceCategory
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.theme.violetColor
import kotlinx.coroutines.launch

@Composable
fun OverviewScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val currentUser by userViewModel.getCurrentUser().collectAsState()
  val groupedFriends by userViewModel.groupedFriends.collectAsState()

  // Charge les emplacements des amis lorsque l'écran est composé
  LaunchedEffect(Unit) {
    Log.d("OverviewScreen", "currentUser: $currentUser")
    Log.d("OverviewScreen", "groupedFriends: $groupedFriends")
    userViewModel.loadFriends()
  }

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
              if (groupedFriends?.isNotEmpty() == true) {
                Log.d("OverviewScreen", "groupedFriends 2: $groupedFriends")
                groupedFriends!!
                    .entries
                    .sortedBy { it.key.ordinal }
                    .forEach { (category, friendsList) ->
                      item { CategoryHeader(category) }
                      items(friendsList) { (friend, _) ->
                        UserRow(
                            user = friend,
                            navigationActions = navigationActions,
                            userViewModel = userViewModel)
                      }
                    }
              } else if (groupedFriends == null) {
                item {
                  Box(
                      modifier = Modifier.fillMaxSize().padding(vertical = 8.dp),
                      contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = Color.White, modifier = Modifier.testTag("loadingFriends"))
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
// Modify UserRow to fetch and display city and country
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun UserRow(user: User, navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val coroutineScope = rememberCoroutineScope()
  var locationText by remember { mutableStateOf("Loading...") }
  val locationSharedWith by userViewModel.locationSharedWith.collectAsState()
  var isLocationShared = false
  locationSharedWith.forEach { friend ->
    isLocationShared = isLocationShared || friend.uid == user.uid
  }
  Log.d("UserRow", "isLocationShared: $isLocationShared")
  Log.d("UserRow", "locationSharedWith: $locationSharedWith")

  LaunchedEffect(user.uid) {
    coroutineScope.launch {
      val location = user.lastKnownLocation
      locationText = userViewModel.getCityAndCountry(location)
    }
  }

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
                Image(
                    painter = painterResource(R.drawable.profile),
                    contentDescription = "Non-Existing profile pictures",
                    modifier =
                        Modifier.width(50.dp)
                            .height(50.dp)
                            .padding(8.dp)
                            .testTag("profileImage_${user.uid}"))
              }
              Column {
                Text(
                    text = "${user.firstName} ${user.lastName.first()}.",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White)
                Text(
                    text = locationText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White)
              }
            }
        IconToggleButton(
            checked = isLocationShared,
            onCheckedChange = { isChecked ->
              if (isChecked) {
                userViewModel.shareLocationWithFriend(
                    uid = userViewModel.getCurrentUser().value.uid,
                    friend = user,
                    onSuccess = { isLocationShared = true },
                    onFailure = { exception ->
                      println("Failed to share location: ${exception.message}")
                    })
              } else {
                userViewModel.stopSharingLocationWithFriend(
                    uid = userViewModel.getCurrentUser().value.uid,
                    friend = user,
                    onSuccess = { isLocationShared = false },
                    onFailure = { exception ->
                      println("Failed to stop sharing location: ${exception.message}")
                    })
              }
            }) {
              Icon(
                  painter =
                      painterResource(
                          id =
                              if (isLocationShared) R.drawable.baseline_location_on_24
                              else R.drawable.baseline_location_off_24),
                  contentDescription =
                      if (isLocationShared) "Stop sharing location" else "Share location",
                  tint =
                      if (isLocationShared) com.swent.suddenbump.ui.theme.Purple40
                      else com.swent.suddenbump.ui.theme.PurpleGrey40,
                  modifier = Modifier.size(24.dp).testTag("locationIcon_${user.uid}"))
            }
      }
}
