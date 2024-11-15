package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {

              // Account Section
              Text(
                  "Birthday",
                  Modifier.fillMaxWidth()
                      .padding(horizontal = 8.dp)
                      .clickable { navigationActions.navigateTo("AccountScreen") }
                      .background(Color.Gray, RoundedCornerShape(8.dp))
                      .padding(8.dp),
                  color = Color.White)

              Text(
                  "Language",
                  Modifier.fillMaxWidth()
                      .padding(horizontal = 8.dp)
                      .clickable { navigationActions.navigateTo("AccountScreen") }
                      .background(Color.Gray, RoundedCornerShape(8.dp))
                      .padding(8.dp),
                  color = Color.White)

              Text(
                  "Delete Account",
                  Modifier.fillMaxWidth()
                      .padding(horizontal = 8.dp)
                      .clickable { navigationActions.navigateTo("Delete Account") }
                      .background(Pink40, RoundedCornerShape(8.dp))
                      .padding(8.dp),
                  color = Color.White)

              Text(
                  "Log out",
                  Modifier.fillMaxWidth()
                      .padding(horizontal = 8.dp)
                      .clickable { navigationActions.navigateTo("Log out") }
                      .background(Pink40, RoundedCornerShape(8.dp))
                      .padding(8.dp),
                  color = Color.White)
            }
      })
}
