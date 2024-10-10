package com.swent.suddenbump.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Place
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

object Route {
  const val OVERVIEW = "Overview"
  const val MAP = "Map"
  const val AUTH = "Auth"
  const val MESS = "Messages"
}

object Screen {
  const val OVERVIEW = "Overview Screen"
  const val ADD_CONTACT = "Add Contact Screen"
  const val AUTH = "Auth Screen"
  const val MAP = "Map Screen"
  const val CONV = "Conversation Screen"
  const val MESS = "Messages Screen"
  const val SETTINGS = "Settings Screen"
}

data class TopLevelDestination(val route: String, val icon: ImageVector, val textId: String)

object TopLevelDestinations {
  val OVERVIEW =
      TopLevelDestination(route = Route.OVERVIEW, icon = Icons.Outlined.Menu, textId = "Overview")
  val MESSAGES =
      TopLevelDestination(
          route = Route.MESS, icon = Icons.Outlined.MailOutline, textId = "Messages")
  val MAP = TopLevelDestination(route = Route.MAP, icon = Icons.Outlined.Place, textId = "Map")
}

val LIST_TOP_LEVEL_DESTINATION =
    listOf(TopLevelDestinations.OVERVIEW, TopLevelDestinations.MESSAGES, TopLevelDestinations.MAP)

open class NavigationActions(
    private val navController: NavHostController,
) {
  /**
   * Navigate to the specified [TopLevelDestination]
   *
   * @param destination The top level destination to navigate to Clear the back stack when
   *   navigating to a new destination This is useful when navigating to a new screen from the
   *   bottom navigation bar as we don't want to keep the previous screen in the back stack
   */
  open fun navigateTo(destination: TopLevelDestination) {

    navController.navigate(destination.route) {
      // Pop up to the start destination of the graph to
      // avoid building up a large stack of destinations
      popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
        // inclusive = true
      }

      // Avoid multiple copies of the same destination when reselecting same item
      launchSingleTop = true

      // Restore state when reselecting a previously selected item
      if (destination.route != Route.AUTH) {
        restoreState = true
      }
    }
  }

  /**
   * Navigate to the specified screen.
   *
   * @param screen The screen to navigate to
   */
  open fun navigateTo(screen: String) {
    navController.navigate(screen)
  }

  /** Navigate back to the previous screen. */
  open fun goBack() {
    navController.popBackStack()
  }

  /**
   * Get the current route of the navigation controller.
   *
   * @return The current route
   */
  open fun currentRoute(): String {
    return navController.currentDestination?.route ?: ""
  }
}
