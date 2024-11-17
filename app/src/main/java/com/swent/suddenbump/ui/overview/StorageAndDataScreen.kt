package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Purple40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageAndDataScreen(navigationActions: NavigationActions) {
  Scaffold(
      modifier =
          Modifier.testTag("storageAndDataScreen")
              .background(Color.Black), // Outer black background
      topBar = {
        TopAppBar(
            title = {
              Text(
                  "Storage and Data",
                  style =
                      MaterialTheme.typography.headlineMedium.copy(
                          fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White),
                  modifier = Modifier.testTag("storageAndDataTitle"))
            },
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
                    .background(Color.Black) // Inner black background
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              LabeledButtonSection(
                  label = "Manage storage",
                  buttonText = "Manage storage",
                  onClick = { /* Add logic for managing storage */},
                  labelTag = "manageStorageText",
                  buttonTag = "manageStorageButton")

              Divider(color = Color.White)

              LabeledButtonSection(
                  label = "Network usage",
                  buttonText = "View network usage",
                  onClick = { /* Add logic for network usage */},
                  labelTag = "networkUsageText",
                  buttonTag = "viewNetworkUsageButton")

              Divider(color = Color.White)

              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .background(Color.White, RoundedCornerShape(8.dp))
                          .padding(16.dp)) {
                    Text(
                        "Media Quality",
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black),
                        modifier = Modifier.testTag("mediaQualityText"))
                  }

              MediaQualityOptions(modifier = Modifier.testTag("mediaQualityOptions"))
            }
      })
}

@Composable
fun LabeledButtonSection(
    label: String,
    buttonText: String,
    onClick: () -> Unit,
    labelTag: String,
    buttonTag: String
) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .background(Color.White, RoundedCornerShape(8.dp))
              .padding(16.dp)) {
        Text(
            text = label,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag(labelTag))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Purple40),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag(buttonTag)) {
              Text(
                  buttonText,
                  style =
                      MaterialTheme.typography.bodyLarge.copy(
                          fontWeight = FontWeight.Bold, color = Color.White))
            }
      }
}

@Composable
fun MediaQualityOptions(modifier: Modifier = Modifier) {
  val options = listOf("Standard", "HD")
  var selectedOption by remember { mutableStateOf(options[0]) }

  Column(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp)
              .background(Color.White, RoundedCornerShape(8.dp))
              .padding(16.dp)) {
        options.forEach { option ->
          Row(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(vertical = 8.dp)
                      .background(Color.White, RoundedCornerShape(8.dp))
                      .padding(8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = option,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold, color = Color.Black),
                    modifier = Modifier.testTag("${option}QualityOption"))
                RadioButton(
                    selected = (selectedOption == option),
                    onClick = { selectedOption = option },
                    colors = RadioButtonDefaults.colors(selectedColor = Purple40))
              }
        }
      }
}
