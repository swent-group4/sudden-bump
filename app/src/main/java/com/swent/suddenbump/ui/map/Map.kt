package com.swent.suddenbump.ui.map

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions

@Composable
fun MapScreen(navigationActions: NavigationActions) {
  Scaffold(
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      content = { pd -> Text("Map Screen", modifier = Modifier.padding(pd)) })
}

@Preview(showBackground = true)
@Composable
fun PreviewMapScreen() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  MapScreen(navigationActions)
}
