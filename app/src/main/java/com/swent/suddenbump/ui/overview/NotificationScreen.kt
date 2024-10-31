package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navigationActions: NavigationActions) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = { navigationActions.goBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Message Notifications Section
                Text("Message Notifications")
                NotificationOption("Show notifications")
                SoundOption()

                Divider()

                // Notifications for Reactions Section
                Text("Notifications for reactions")
                NotificationOption("Show notifications")

                Divider()

                // Group Notifications Section
                Text("Group Notifications")
                NotificationOption("Show notifications")
                SoundOption()

                Divider()

                // Reaction Notifications Section
                Text("Reaction notifications")
                NotificationOption("Show notifications")

                Divider()

                // Reset Notification Settings Button
                Button(onClick = { /* Add logic to reset notification settings */ }) {
                    Text("Reset notification settings")
                }
            }
        }
    )
}

@Composable
fun NotificationOption(text: String) {
    var isChecked by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text)
        Checkbox(
            checked = isChecked,
            onCheckedChange = { isChecked = it }
        )
    }
}

@Composable
fun SoundOption() {
    val options = listOf("By default", "None")
    var selectedOption by remember { mutableStateOf(options[0]) }

    options.forEach { option ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(option)
            RadioButton(
                selected = (selectedOption == option),
                onClick = { selectedOption = option }
            )
        }
    }
}