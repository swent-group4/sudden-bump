package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Pink40
import com.swent.suddenbump.ui.theme.Purple40
import com.swent.suddenbump.ui.theme.clickableTextModifier
import com.swent.suddenbump.ui.theme.screenPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(navigationActions: NavigationActions) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Account", color = Color.White) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("backButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White)
                  }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple40))
      },
      content = { paddingValues ->
        Column(
            modifier = screenPadding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              // Each section uses the reusable clickableTextModifier
              Text(
                  text = "Birthday",
                  color = Color.White,
                  modifier =
                      clickableTextModifier(
                          backgroundColor = Color.Gray, testTag = "birthdaySection") {
                            navigationActions.navigateTo("AccountScreen")
                          })

              Text(
                  text = "Language",
                  color = Color.White,
                  modifier =
                      clickableTextModifier(
                          backgroundColor = Color.Gray, testTag = "languageSection") {
                            navigationActions.navigateTo("AccountScreen")
                          })

              Text(
                  text = "Delete Account",
                  color = Color.White,
                  modifier =
                      clickableTextModifier(
                          backgroundColor = Pink40, testTag = "deleteAccountSection") {
                            navigationActions.navigateTo("Delete Account")
                          })

              Text(
                  text = "Log out",
                  color = Color.White,
                  modifier =
                      clickableTextModifier(backgroundColor = Pink40, testTag = "logoutSection") {
                        navigationActions.navigateTo("Log out")
                      })
            }
      })
}
