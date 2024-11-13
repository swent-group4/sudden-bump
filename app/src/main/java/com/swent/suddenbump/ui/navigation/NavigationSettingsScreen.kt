package com.swent.suddenbump.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.swent.suddenbump.ui.overview.AccountScreen
import com.swent.suddenbump.ui.overview.ConfidentialityScreen
import com.swent.suddenbump.ui.overview.DiscussionScreen
import com.swent.suddenbump.ui.overview.HelpScreen
import com.swent.suddenbump.ui.overview.NotificationScreen
import com.swent.suddenbump.ui.overview.StorageAndDataScreen
import com.swent.suddenbump.ui.overview.UsernameAndPhotoScreen

import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.overview.SettingsScreen

@Composable
fun NavigationSettingsScreen(navController: NavHostController, userViewModel: UserViewModel) {
  NavHost(navController = navController, startDestination = "settings") {
    composable("settings") { SettingsScreen(NavigationActions(navController), userViewModel) }
    composable("AccountScreen") { AccountScreen(NavigationActions(navController)) }
    composable("ConfidentialityScreen") { ConfidentialityScreen(NavigationActions(navController), userViewModel ) }
    composable("DiscussionsScreen") { DiscussionScreen(NavigationActions(navController)) }
    composable("NotificationsScreen") { NotificationScreen(NavigationActions(navController), userViewModel) }
    composable("StorageAndDataScreen") { StorageAndDataScreen(NavigationActions(navController)) }
    composable("HelpScreen") { HelpScreen(NavigationActions(navController)) }
  }
}