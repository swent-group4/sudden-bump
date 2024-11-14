package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navigationActions: NavigationActions, viewModel: UserViewModel) {
  var notificationsEnabled by remember { mutableStateOf(true) }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Notifications") },
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
              // Message Notifications Section
              Text("Message Notifications")
              SoundOption()

              Divider()

              // Notifications for Reactions Section
              Text("Notifications for reactions")

              Divider()

              // Group Notifications Section
              Text("Group Notifications")
              SoundOption()

              Divider()

              // Reaction Notifications Section
              Text("Reaction notifications")

              Divider()

              // Enable/Disable Notifications Switch
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Notifications")
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it })
                  }

              Divider()

              // Reset Notification Settings Button
              Button(onClick = { /* Add logic to reset notification settings */}) {
                Text("Reset notification settings")
              }
            }
      })
}

@Composable
fun SoundOption() {
  val options = listOf("By default", "None")
  var selectedOption by remember { mutableStateOf(options[0]) }

  options.forEach { option ->
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
          Text(option)
          RadioButton(selected = (selectedOption == option), onClick = { selectedOption = option })
        }
  }
}
