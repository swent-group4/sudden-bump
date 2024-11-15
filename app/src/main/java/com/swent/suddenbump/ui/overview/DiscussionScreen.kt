package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
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
import com.swent.suddenbump.ui.theme.Purple40
import com.swent.suddenbump.ui.theme.Purple80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionScreen(navigationActions: NavigationActions) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Chats", color = Color.White) },
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
              SectionWithButton(
                  label = "Chat wallpaper",
                  buttonText = "Change chat wallpaper",
                  onClick = { /* Add logic for chat wallpaper */},
                  labelTag = "chatWallpaperText",
                  buttonTag = "changeChatWallpaperButton")

              Divider()

              SectionWithButton(
                  label = "Export chat",
                  buttonText = "Export chat",
                  onClick = { /* Add logic for exporting chat */},
                  labelTag = "exportChatText",
                  buttonTag = "exportChatButton")

              Divider()

              SectionWithButton(
                  label = "Archive all chats",
                  buttonText = "Archive all chats",
                  onClick = { /* Add logic for archiving all chats */},
                  labelTag = "archiveAllChatsText",
                  buttonTag = "archiveAllChatsButton")

              Divider()

              SectionWithButton(
                  label = "Clear all chats",
                  buttonText = "Clear all chats",
                  onClick = { /* Add logic for clearing all chats */},
                  labelTag = "clearAllChatsText",
                  buttonTag = "clearAllChatsButton")

              Divider()

              SectionWithButton(
                  label = "Delete all chats",
                  buttonText = "Delete all chats",
                  onClick = { /* Add logic for deleting all chats */},
                  labelTag = "deleteAllChatsText",
                  buttonTag = "deleteAllChatsButton")
            }
      })
}

@Composable
fun SectionWithButton(
    label: String,
    buttonText: String,
    onClick: () -> Unit,
    labelTag: String,
    buttonTag: String
) {
  Text(text = label, color = Color.White, modifier = Modifier.testTag(labelTag))
  Button(
      onClick = onClick,
      colors = ButtonDefaults.buttonColors(containerColor = Purple80),
      modifier = Modifier.testTag(buttonTag)) {
        Text(buttonText, color = Color.White)
      }
}
