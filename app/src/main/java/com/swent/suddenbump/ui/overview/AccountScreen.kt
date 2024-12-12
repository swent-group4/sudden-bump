package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Pink40
import com.swent.suddenbump.ui.utils.AccountOption
import com.swent.suddenbump.ui.utils.CustomCenterAlignedTopBar

@Composable
fun AccountScreen(navigationActions: NavigationActions) {
  var selectedLanguage by remember { mutableStateOf("English") }
  var isLanguageMenuExpanded by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.testTag("accountScreen"),
      topBar = {
        CustomCenterAlignedTopBar(title = "Account", navigationActions = navigationActions)
      },
      content = { paddingValues ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black)
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              item {
                // Language Section with Pop-Up Menu
                Box(modifier = Modifier.testTag("languageSection")) { // Only one testTag here
                  AccountOption(
                      label = "Language",
                      backgroundColor = Color.White,
                      onClick = { isLanguageMenuExpanded = true },
                      testTag = "" // Remove redundant tag from the child
                      )
                  DropdownMenu(
                      expanded = isLanguageMenuExpanded,
                      onDismissRequest = { isLanguageMenuExpanded = false }) {
                        listOf("English", "French", "German").forEach { language ->
                          DropdownMenuItem(
                              text = { Text(language) },
                              onClick = {
                                selectedLanguage = language
                                isLanguageMenuExpanded = false
                              },
                              modifier = Modifier.testTag("languageMenuItem_$language"))
                        }
                      }
                }
              }

              item {
                AccountOption(
                    label = "Delete Account",
                    backgroundColor = Pink40,
                    onClick = { navigationActions.navigateTo("AccountScreen") },
                    testTag = "deleteAccountSection")
              }

              item {
                AccountOption(
                    label = "Log out",
                    backgroundColor = Pink40,
                    onClick = { navigationActions.navigateTo("AccountScreen") },
                    testTag = "logoutSection")
              }
            }
      })
}

@Composable
fun ChangeSection(
    title: String,
    placeholder: String,
    sectionTag: String,
    placeholderTag: String,
    buttonTag: String
) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .background(Color.White, RoundedCornerShape(8.dp))
              .padding(16.dp)
              .testTag(sectionTag)) {
        Text(
            text = title,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black))
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(48.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                    .testTag(placeholderTag)) {
              Text(
                  text = placeholder,
                  style =
                      MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, color = Color.Gray),
                  modifier = Modifier.align(Alignment.CenterStart).padding(8.dp))
            }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { /* Do nothing for now */},
            modifier = Modifier.fillMaxWidth().testTag(buttonTag),
            colors = ButtonDefaults.buttonColors(containerColor = Pink40)) {
              Text(
                  text = "Update ${title.split(" ")[1]}",
                  style =
                      MaterialTheme.typography.bodyLarge.copy(
                          fontWeight = FontWeight.Bold, color = Color.White))
            }
      }
}
