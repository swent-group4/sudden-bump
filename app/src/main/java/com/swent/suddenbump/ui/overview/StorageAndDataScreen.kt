@file:OptIn(ExperimentalMaterial3Api::class)

package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
              StorageAndDataOptionSection(
                  label = "Manage storage",
                  buttonText = "Manage storage",
                  onClick = { /* TODO: Add logic for managing storage */},
                  labelTag = "manageStorageText",
                  buttonTag = "manageStorageButton")

              StorageAndDataOptionSection(
                  label = "Network usage",
                  buttonText = "View network usage",
                  onClick = { /* TODO: Add logic for viewing network usage */},
                  labelTag = "networkUsageText",
                  buttonTag = "viewNetworkUsageButton")

              MediaQualitySection()
            }
      })
}

@Composable
fun StorageAndDataOptionSection(
    label: String,
    buttonText: String,
    onClick: () -> Unit,
    labelTag: String,
    buttonTag: String
) {
  LabeledButtonSection(
      label = label,
      buttonText = buttonText,
      onClick = onClick,
      labelTag = labelTag,
      buttonTag = buttonTag)
  Divider(color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun MediaQualitySection() {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .background(Color.White)
              .padding(16.dp)
              .testTag("mediaQualitySection")) {
        Text(
            text = "Media Quality",
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold, color = Color.Black),
            modifier = Modifier.testTag("mediaQualityText"))
        MediaQualityOptions(modifier = Modifier.testTag("mediaQualityOptions"))
      }
}

@Composable
fun MediaQualityOptions(modifier: Modifier = Modifier) {
  val options = listOf("Standard", "HD")
  var selectedOption by remember { mutableStateOf(options[0]) }

  OptionSelector(
      options = options,
      selectedOption = selectedOption,
      onOptionSelected = { selectedOption = it },
      modifier = modifier)
}

@Composable
fun OptionSelector(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
                            fontWeight = FontWeight.Bold, color = Color.Black),
                    modifier = Modifier.testTag("${option}QualityOption"))
                RadioButton(
                    selected = (selectedOption == option),
                    onClick = { onOptionSelected(option) },
                    colors =
                        RadioButtonDefaults.colors(
                            selectedColor = com.swent.suddenbump.ui.theme.Purple40))
              }
        }
      }
}
