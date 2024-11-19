package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Purple40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionScreen(navigationActions: NavigationActions) {
  val sections =
      listOf(
          SectionData(
              "Chat wallpaper",
              "Change chat wallpaper",
              "chatWallpaperText",
              "changeChatWallpaperButton"),
          SectionData("Export chat", "Export chat", "exportChatText", "exportChatButton"),
          SectionData(
              "Archive all chats",
              "Archive all chats",
              "archiveAllChatsText",
              "archiveAllChatsButton"),
          SectionData(
              "Clear all chats", "Clear all chats", "clearAllChatsText", "clearAllChatsButton"),
          SectionData(
              "Delete all chats", "Delete all chats", "deleteAllChatsText", "deleteAllChatsButton"))

  Scaffold(
      modifier =
          Modifier.testTag("discussionScreen").background(Color.Black), // Outer black background
      topBar = {
        TopAppBar(
            title = {
              Text(
                  "Chats",
                  style =
                      MaterialTheme.typography.headlineMedium.copy(
                          fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White),
                  modifier = Modifier.testTag("discussionTitle"))
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
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black)
                    .padding(paddingValues)
                    .padding(16.dp)
                    .testTag("discussionLazyColumn"), // Add testTag for scrollable LazyColumn
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              items(sections.size) { index ->
                val section = sections[index]
                SectionWithButton(
                    label = section.label,
                    buttonText = section.buttonText,
                    onClick = { /* Add specific logic */},
                    labelTag = section.labelTag,
                    buttonTag = section.buttonTag)
                if (index != sections.lastIndex) {
                  Divider(color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
                }
              }
            }
      })
}

data class SectionData(
    val label: String,
    val buttonText: String,
    val labelTag: String,
    val buttonTag: String
)

@Composable
fun SectionWithButton(
    label: String,
    buttonText: String,
    onClick: () -> Unit,
    labelTag: String,
    buttonTag: String
) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .background(Color.White, RoundedCornerShape(8.dp))
              .padding(16.dp)) {
        Text(
            text = label,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag(labelTag))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Purple40),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag(buttonTag)) {
              Text(
                  buttonText,
                  style =
                      MaterialTheme.typography.bodyLarge.copy(
                          fontWeight = FontWeight.Bold, color = Color.White))
            }
      }
}
