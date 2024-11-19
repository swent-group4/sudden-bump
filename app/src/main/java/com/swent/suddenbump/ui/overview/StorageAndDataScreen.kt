@file:OptIn(ExperimentalMaterial3Api::class)

package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.utils.CustomTopBar
import com.swent.suddenbump.ui.utils.LabeledButtonSection

@Composable
fun StorageAndDataScreen(navigationActions: NavigationActions) {
  Scaffold(
      modifier = Modifier.testTag("storageAndDataScreen").background(Color.Black),
      topBar = {
        CustomTopBar(
            title = "Storage and Data",
            navigationActions = navigationActions,
            titleTag = "storageAndDataTitle",
            backButtonTag = "backButton")
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              LabeledButtonSection(
                  label = "Manage storage",
                  buttonText = "Manage storage",
                  onClick = { /* TODO: Add logic for managing storage */},
                  labelTag = "manageStorageText",
                  buttonTag = "manageStorageButton")

              Divider(color = Color.White)

              LabeledButtonSection(
                  label = "Network usage",
                  buttonText = "View network usage",
                  onClick = { /* TODO: Add logic for viewing network usage */},
                  labelTag = "networkUsageText",
                  buttonTag = "viewNetworkUsageButton")

              Divider(color = Color.White)

              // Media Quality Section
              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .background(Color.White)
                          .padding(16.dp)
                          .testTag("mediaQualityBox")) {
                    Text(
                        text = "Media Quality",
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = Color.Black),
                        modifier = Modifier.testTag("mediaQualityText"))
                  }

              MediaQualityOptions(modifier = Modifier.testTag("mediaQualityOptions"))
            }
      })
}

@Composable
fun MediaQualityOptions(modifier: Modifier = Modifier) {
  val options = listOf("Standard", "HD")
  var selectedOption by androidx.compose.runtime.remember { mutableStateOf(options[0]) }

  Column(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp)
              .background(Color.White)
              .padding(16.dp)) {
        options.forEach { option ->
          Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = option,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = Color.Black),
                    modifier = Modifier.testTag("${option}QualityOption"))
                androidx.compose.material3.RadioButton(
                    selected = (selectedOption == option),
                    onClick = { selectedOption = option },
                    colors =
                        androidx.compose.material3.RadioButtonDefaults.colors(
                            selectedColor = com.swent.suddenbump.ui.theme.Purple40))
              }
        }
      }
}
