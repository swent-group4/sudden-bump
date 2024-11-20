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
import androidx.compose.runtime.remember
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
  val sections = remember { createDiscussionSections() }

  Scaffold(
      modifier = Modifier.testTag("discussionScreen").background(Color.Black),
      topBar = { createTopBar(navigationActions) },
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

/** Helper function to create the top bar for DiscussionScreen */
@Composable
fun createTopBar(navigationActions: NavigationActions) {
  CustomTopBar(
      title = "Chats",
      navigationActions = navigationActions,
      titleTag = "discussionTitle",
      backButtonTag = "backButton")
}

/** Create the list of sections for the DiscussionScreen */
fun createDiscussionSections(): List<SectionData> =
    listOf(
        createSectionData(
            "Chat wallpaper",
            "Change chat wallpaper",
            "chatWallpaperText",
            "changeChatWallpaperButton"),
        createSectionData("Export chat", "Export chat", "exportChatText", "exportChatButton"),
        createSectionData(
            "Archive all chats",
            "Archive all chats",
            "archiveAllChatsText",
            "archiveAllChatsButton"),
        createSectionData(
            "Clear all chats", "Clear all chats", "clearAllChatsText", "clearAllChatsButton"),
        createSectionData(
            "Delete all chats", "Delete all chats", "deleteAllChatsText", "deleteAllChatsButton"))

/** Helper function to create a SectionData instance */
fun createSectionData(label: String, buttonText: String, labelTag: String, buttonTag: String) =
    SectionData(label = label, buttonText = buttonText, labelTag = labelTag, buttonTag = buttonTag)

/** Extension function to add items with dividers in LazyColumn. */
inline fun <T> LazyListScope.itemsWithDividers(
    items: List<T>,
    crossinline content: @Composable (T) -> Unit
) {
  items.forEachIndexed { index, item ->
    item {
      content(item)

      if (index != items.lastIndex) {
        Divider(
            color = Color.White,
            modifier = Modifier.padding(vertical = 8.dp).testTag("divider_$index"))
      }
    }
  }
}
