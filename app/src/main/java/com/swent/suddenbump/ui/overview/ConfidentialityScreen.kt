package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import com.swent.suddenbump.ui.theme.Purple80
import com.swent.suddenbump.ui.theme.screenPadding
import com.swent.suddenbump.ui.theme.sectionModifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfidentialityScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  var showBlockedContacts by remember { mutableStateOf(false) }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Confidentiality Settings", color = Color.White) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("backButton")) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White)
                  }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple40))
      },
      content = { paddingValues ->
        Column(
            modifier = screenPadding(paddingValues).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              ConfidentialityQuestionWithOptions(
                  question = "Who can see my last time online",
                  options = listOf("No one", "Friends", "Everyone"),
                  modifier = Modifier.testTag("onlinePresenceOptions"))

              ConfidentialityQuestionWithOptions(
                  question = "Who can see when I am online",
                  options = listOf("No one", "Friends", "Everyone"),
                  modifier = Modifier.testTag("whenOnlineOptions"))

              Divider(color = Color.White)

              ConfidentialityQuestionWithOptions(
                  question = "Who can see my profile photo",
                  options = listOf("No one", "Friends", "Everyone"),
                  modifier = Modifier.testTag("profilePhotoOptions"))

              Divider(color = Color.White)

              ConfidentialityQuestionWithOptions(
                  question = "Who can see my info",
                  options = listOf("No one", "Friends", "Everyone"),
                  modifier = Modifier.testTag("myInfoOptions"))

              Divider(color = Color.White)

              ConfidentialityQuestionWithOptions(
                  question = "Who can add me to groups",
                  options = listOf("No one", "Friends", "Everyone"),
                  modifier = Modifier.testTag("groupsOptions"))

              Divider(color = Color.White)

              ConfidentialityQuestionWithOptions(
                  question = "Who can see my status",
                  options = listOf("No one", "Friends", "Everyone"),
                  modifier = Modifier.testTag("statusOptions"))

              Divider(color = Color.White)

              Text(
                  "Blocked Contacts",
                  color = Color.White,
                  modifier = Modifier.testTag("blockedContacts"))
              Button(
                  onClick = { showBlockedContacts = !showBlockedContacts },
                  colors = ButtonDefaults.buttonColors(containerColor = Purple80),
                  modifier = Modifier.testTag("showBlockedContactsButton")) {
                    Text("Show blocked contacts", color = Color.White)
                  }

              if (showBlockedContacts) {
                BlockedContactsList(userViewModel)
              }

              Divider(color = Color.White)

              ConfidentialityQuestionWithOptions(
                  question = "Location Status",
                  options = listOf("Enabled", "Disabled"),
                  modifier = Modifier.testTag("locationStatusOptions"))
            }
      })
}

@Composable
fun ConfidentialityQuestionWithOptions(
    question: String,
    options: List<String>,
    modifier: Modifier = Modifier
) {
  Text(question, color = Color.White)
  OptionColumn(options, modifier)
}

@Composable
fun BlockedContactsList(userViewModel: UserViewModel) {
  val blockedFriends by userViewModel.getBlockedFriends().collectAsState(initial = emptyList())

  Column { blockedFriends.forEach { user -> Text(user.firstName, color = Color.White) } }
}

@Composable
fun OptionColumn(options: List<String>, modifier: Modifier = Modifier) {
  var selectedOption by remember { mutableStateOf(options[0]) }
  Column(modifier = modifier.then(sectionModifier())) {
    options.forEach { option ->
      Row(
          modifier =
              Modifier.fillMaxWidth()
                  .padding(8.dp)
                  .background(Color.Gray, RoundedCornerShape(8.dp))
                  .padding(8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically) {
            Text(option, color = Color.White)
            RadioButton(
                selected = (selectedOption == option),
                onClick = { selectedOption = option },
                colors = RadioButtonDefaults.colors(selectedColor = Purple40))
          }
    }
  }
}
