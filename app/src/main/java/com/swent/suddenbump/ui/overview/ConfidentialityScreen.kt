@file:OptIn(ExperimentalMaterial3Api::class)

package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Purple40
import com.swent.suddenbump.ui.utils.LabeledButtonSection

data class ConfidentialityOption(
    val question: String,
    val options: List<String>,
    val testTag: String
)

@Composable
fun ConfidentialityScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  var showBlockedContacts by remember { mutableStateOf(false) }

  val confidentialityOptions =
      listOf(
          ConfidentialityOption(
              "Location Status", listOf("Enabled", "Disabled"), "locationStatusOptions"))

  Scaffold(
      modifier = Modifier.testTag("confidentialityScreen"),
      topBar = {
        OverviewScreenTopBar(title = "Confidentiality", navigationActions = navigationActions)
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
              confidentialityOptions.forEach { option ->
                ConfidentialityQuestionWithOptions(
                    question = option.question,
                    options = option.options,
                    modifier = Modifier.testTag(option.testTag))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White)
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Blocked Contacts Section
              LabeledButtonSection(
                  label = "Blocked Contacts",
                  buttonText =
                      if (showBlockedContacts) "Hide Blocked Contacts" else "Show Blocked Contacts",
                  onClick = { showBlockedContacts = !showBlockedContacts },
                  labelTag = "blockedContacts",
                  buttonTag = "showBlockedContactsButton")

              if (showBlockedContacts) {
                BlockedContactsList(userViewModel)
              }
            }
      })
}

@Composable
fun ConfidentialityQuestionWithOptions(
    question: String,
    options: List<String>,
    modifier: Modifier = Modifier
) {
  var selectedOption by remember { mutableStateOf(options[0]) }

  Column(
      modifier =
          modifier
              .fillMaxWidth()
              .background(Color.White, RoundedCornerShape(8.dp))
              .padding(16.dp)) {
        Text(
            question,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = Color.Black))
        Spacer(modifier = Modifier.height(8.dp))
        options.forEach { option ->
          Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = option,
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = Color.Black))
                RadioButton(
                    selected = (selectedOption == option),
                    onClick = { selectedOption = option },
                    colors = RadioButtonDefaults.colors(selectedColor = Purple40))
              }
        }
      }
}

@Composable
fun BlockedContactsList(userViewModel: UserViewModel) {
  val blockedFriends by userViewModel.getBlockedFriends().collectAsState(initial = emptyList())

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .background(Color.White, RoundedCornerShape(8.dp))
              .padding(16.dp)) {
        blockedFriends.forEach { user ->
          Text(
              user.firstName,
              style =
                  MaterialTheme.typography.bodyLarge.copy(
                      fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                      color = Color.Black))
        }
      }
}
