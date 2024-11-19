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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.utils.CustomTopBar
import com.swent.suddenbump.ui.utils.LabeledButtonSection

data class AccountSectionData(
    val label: String,
    val buttonText: String,
    val labelTag: String,
    val buttonTag: String
)

@Composable
fun AccountScreen(navigationActions: NavigationActions) {
  val sections =
      listOf(
          AccountSectionData(
              "Change Email", "Update Email", "changeEmailText", "updateEmailButton"),
          AccountSectionData(
              "Change Password", "Update Password", "changePasswordText", "updatePasswordButton"),
          AccountSectionData(
              "Delete Account", "Remove Account", "deleteAccountText", "removeAccountButton"))

  Scaffold(
      modifier = Modifier.testTag("accountScreen").background(Color.Black),
      topBar = {
        CustomTopBar(
            title = "Account",
            navigationActions = navigationActions,
            titleTag = "accountTitle",
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
              sections.forEach { section ->
                LabeledButtonSection(
                    label = section.label,
                    buttonText = section.buttonText,
                    onClick = { /* TODO: Add logic for each section */},
                    labelTag = section.labelTag,
                    buttonTag = section.buttonTag)
                Divider(color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Additional Account Settings
              AccountPreferences(modifier = Modifier.testTag("accountPreferences"))
            }
      })
}

@Composable
fun AccountPreferences(modifier: Modifier = Modifier) {
  val options = listOf("Enable Notifications", "Enable Two-Factor Authentication")
  var selectedOption by remember { mutableStateOf(options[0]) } // Proper delegation for state

  Column(modifier = modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
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
                modifier = Modifier.testTag("${option.replace(" ", "")}Option"))
            androidx.compose.material3.Checkbox(
                checked = selectedOption == option,
                onCheckedChange = { if (it) selectedOption = option },
                colors =
                    androidx.compose.material3.CheckboxDefaults.colors(
                        checkedColor = com.swent.suddenbump.ui.theme.Purple40))
          }
    }
  }
}
