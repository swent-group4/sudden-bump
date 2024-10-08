package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navigationActions: NavigationActions) {
  Scaffold(
      modifier = Modifier.testTag("settingsScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Settings", Modifier.testTag("settingsTitle")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() }, Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      },
      content = { pd -> Text("Settings Screen", modifier = Modifier.padding(pd)) })
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  SettingsScreen(navigationActions)
}
