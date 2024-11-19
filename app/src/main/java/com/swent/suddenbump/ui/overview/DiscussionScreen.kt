@file:OptIn(ExperimentalMaterial3Api::class)

package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.utils.CustomTopBar
import com.swent.suddenbump.ui.utils.LabeledButtonSection

data class SectionData(
    val label: String,
    val buttonText: String,
    val labelTag: String,
    val buttonTag: String
)

@Composable
fun DiscussionScreen(navigationActions: NavigationActions) {
  val sections =
      listOf(
          SectionData(
              label = "Chat wallpaper",
              buttonText = "Change chat wallpaper",
              labelTag = "chatWallpaperText",
              buttonTag = "changeChatWallpaperButton"),
          SectionData(
              label = "Export chat",
              buttonText = "Export chat",
              labelTag = "exportChatText",
              buttonTag = "exportChatButton"),
          SectionData(
              label = "Archive all chats",
              buttonText = "Archive all chats",
              labelTag = "archiveAllChatsText",
              buttonTag = "archiveAllChatsButton"),
          SectionData(
              label = "Clear all chats",
              buttonText = "Clear all chats",
              labelTag = "clearAllChatsText",
              buttonTag = "clearAllChatsButton"),
          SectionData(
              label = "Delete all chats",
              buttonText = "Delete all chats",
              labelTag = "deleteAllChatsText",
              buttonTag = "deleteAllChatsButton"))

  Scaffold(
      modifier = Modifier.testTag("discussionScreen").background(Color.Black),
      topBar = {
        CustomTopBar(
            title = "Chats",
            navigationActions = navigationActions,
            titleTag = "discussionTitle",
            backButtonTag = "backButton")
      },
      content = { paddingValues ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black)
                    .padding(paddingValues)
                    .padding(16.dp)
                    .testTag("discussionLazyColumn"),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              itemsWithDividers(sections) { section ->
                LabeledButtonSection(
                    label = section.label,
                    buttonText = section.buttonText,
                    onClick = { /* TODO: Add logic for each section */},
                    labelTag = section.labelTag,
                    buttonTag = section.buttonTag)
              }
            }
      })
}

/** Extension function to add items with dividers in LazyColumn. */
inline fun <T> LazyListScope.itemsWithDividers(
    items: List<T>,
    crossinline content: @Composable (T) -> Unit
) {
  items.forEachIndexed { index, item ->
    item {
      content(item)
      if (index != items.lastIndex) {
        Divider(color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
      }
    }
  }
}
