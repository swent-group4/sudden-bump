package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionScreen(navigationActions: NavigationActions) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Chats") },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back")
              }
            })
      },
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              // Chat Wallpaper Section
              Text("Chat wallpaper")
              Button(onClick = { /* Add logic for chat wallpaper */}) {
                Text("Change chat wallpaper")
              }

              Divider()

              // Export Chat Section
              Text("Export chat")
              Button(onClick = { /* Add logic for exporting chat */}) { Text("Export chat") }

              Divider()

              // Archive All Chats Section
              Text("Archive all chats")
              Button(onClick = { /* Add logic for archiving all chats */}) {
                Text("Archive all chats")
              }

              Divider()

              // Clear All Chats Section
              Text("Clear all chats")
              Button(onClick = { /* Add logic for clearing all chats */}) {
                Text("Clear all chats")
              }

              Divider()

              // Delete All Chats Section
              Text("Delete all chats")
              Button(onClick = { /* Add logic for deleting all chats */}) {
                Text("Delete all chats")
              }
            }
      })
}
