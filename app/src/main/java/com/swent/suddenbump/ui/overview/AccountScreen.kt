package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Pink40
import com.swent.suddenbump.ui.theme.Purple40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(navigationActions: NavigationActions) {
  Scaffold(
      modifier =
          Modifier.background(Color.Black).testTag("AccountScreen"), // Set outer contour to black
      topBar = {
        TopAppBar(
            title = {
              Text(
                  "Account",
                  style =
                      MaterialTheme.typography.headlineMedium.copy(
                          fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White),
                  modifier = Modifier.testTag("accountTitle"))
            },
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
                    .background(Color.Black)
                    .padding(paddingValues)
                    .padding(16.dp), // Inner background also set to black
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              AccountOption(
                  label = "Birthday",
                  backgroundColor = Color.White,
                  onClick = { navigationActions.navigateTo("AccountScreen") },
                  testTag = "birthdaySection")
              AccountOption(
                  label = "Language",
                  backgroundColor = Color.White,
                  onClick = { navigationActions.navigateTo("AccountScreen") },
                  testTag = "languageSection")
              AccountOption(
                  label = "Delete Account",
                  backgroundColor = Pink40,
                  onClick = { navigationActions.navigateTo("Delete Account") },
                  testTag = "deleteAccountSection")
              AccountOption(
                  label = "Log out",
                  backgroundColor = Pink40,
                  onClick = { navigationActions.navigateTo("Log out") },
                  testTag = "logoutSection")
            }
      })
}

@Composable
fun AccountOption(label: String, backgroundColor: Color, onClick: () -> Unit, testTag: String) {
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 8.dp)
              .clickable { onClick() }
              .background(backgroundColor, RoundedCornerShape(8.dp))
              .height(48.dp)
              .testTag(testTag),
      contentAlignment = Alignment.CenterStart // Center vertically and align to start (left)
      ) {
        Text(
            text = label,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (backgroundColor == Pink40) Color.White else Color.Black),
            modifier = Modifier.padding(start = 16.dp) // Add padding for alignment
            )
      }
}
