package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Purple40
import com.swent.suddenbump.ui.theme.Purple80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageAndDataScreen(navigationActions: NavigationActions) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Storage and Data", color = Color.White) },
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
              // Manage Storage Section
              Text(
                  "Manage storage",
                  color = Color.White,
                  modifier = Modifier.testTag("manageStorageText"))
              Button(
                  onClick = { /* Add logic for managing storage */},
                  colors = ButtonDefaults.buttonColors(containerColor = Purple80),
                  modifier = Modifier.testTag("manageStorageButton")) {
                    Text("Manage storage", color = Color.White)
                  }

              Divider()

              // Network Usage Section
              Text(
                  "Network usage",
                  color = Color.White,
                  modifier = Modifier.testTag("networkUsageText"))
              Button(
                  onClick = { /* Add logic for network usage */},
                  colors = ButtonDefaults.buttonColors(containerColor = Purple80),
                  modifier = Modifier.testTag("viewNetworkUsageButton")) {
                    Text("View network usage", color = Color.White)
                  }

              Divider()

              // Media Quality Section
              Text(
                  "Media Quality",
                  color = Color.White,
                  modifier = Modifier.testTag("mediaQualityText"))
              MediaQualityOptions(modifier = Modifier.testTag("mediaQualityOptions"))
            }
      })
}

@Composable
fun MediaQualityOptions(modifier: Modifier = Modifier) {
  val options = listOf("Standard", "HD")
  var selectedOption by remember { mutableStateOf(options[0]) }

  Column(modifier = modifier) {
    options.forEach { option ->
      Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically) {
            Text(option, color = Color.White, modifier = Modifier.testTag("${option}QualityOption"))
            RadioButton(
                selected = (selectedOption == option),
                onClick = { selectedOption = option },
                colors = RadioButtonDefaults.colors(selectedColor = Purple80))
          }
    }
  }
}
