package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
              IconButton(onClick = { navigationActions.goBack() }) {
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
              // Chat Wallpaper Section
              Text("Chat wallpaper", color = Color.White)
              Button(
                  onClick = { /* Add logic for chat wallpaper */},
                  colors = ButtonDefaults.buttonColors(containerColor = Purple80)) {
                    Text("Change chat wallpaper", color = Color.White)
                  }

              Divider()

              // Export Chat Section
              Text("Export chat", color = Color.White)
              Button(
                  onClick = { /* Add logic for exporting chat */},
                  colors = ButtonDefaults.buttonColors(containerColor = Purple80)) {
                    Text("Export chat")
                  }

              Divider()

              // Archive All Chats Section
              Text("Archive all chats", color = Color.White)
              Button(
                  onClick = { /* Add logic for archiving all chats */},
                  colors = ButtonDefaults.buttonColors(containerColor = Purple80)) {
                    Text("Archive all chats")
                  }

              Divider()

              // Clear All Chats Section
              Text("Clear all chats", color = Color.White)
              Button(
                  onClick = { /* Add logic for clearing all chats */},
                  colors = ButtonDefaults.buttonColors(containerColor = Purple80)) {
                    Text("Clear all chats")
                  }

              Divider()

              // Delete All Chats Section
              Text("Delete all chats", color = Color.White)
              Button(
                  onClick = { /* Add logic for deleting all chats */},
                  colors = ButtonDefaults.buttonColors(containerColor = Purple80)) {
                    Text("Delete all chats")
                  }
            }
      })
}
