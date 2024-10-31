
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
fun StorageAndDataScreen(navigationActions: NavigationActions) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage and Data") },
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
                // Manage Storage Section
                Text("Manage storage")
                Button(onClick = { /* Add logic for managing storage */ }) {
                    Text("Manage storage")
                }

                Divider()

                // Network Usage Section
                Text("Network usage")
                Button(onClick = { /* Add logic for network usage */ }) {
                    Text("View network usage")
                }

                Divider()

                // Media Quality Section
                Text("Media Quality")
                MediaQualityOptions()
            }
        }
    )
}

@Composable
fun MediaQualityOptions() {
    val options = listOf("Standard", "HD")
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
