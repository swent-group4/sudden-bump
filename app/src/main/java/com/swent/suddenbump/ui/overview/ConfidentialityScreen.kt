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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Purple40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfidentialityScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  var showBlockedContacts by remember { mutableStateOf(false) }

  Scaffold(
      modifier =
          Modifier.testTag("confidentialityScreen")
              .background(Color.Black), // Outer black background
      topBar = {
        TopAppBar(
            title = {
              Text(
                  "Confidentiality Settings",
                  style =
                      MaterialTheme.typography.headlineMedium.copy(
                          fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White),
                  modifier = Modifier.testTag("confidentialityTitle"))
            },
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
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black) // Inner black background
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
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

              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .background(Color.White, RoundedCornerShape(8.dp))
                          .padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "Blocked Contacts",
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black),
                        modifier = Modifier.testTag("blockedContacts"))
                  }

              Button(
                  onClick = { showBlockedContacts = !showBlockedContacts },
                  colors = ButtonDefaults.buttonColors(containerColor = Purple40),
                  modifier =
                      Modifier.fillMaxWidth().height(48.dp).testTag("showBlockedContactsButton")) {
                    Text(
                        "Show blocked contacts",
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold, color = Color.White))
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
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 8.dp)
              .background(Color.White, RoundedCornerShape(8.dp))
              .padding(8.dp)) {
        Text(
            question,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black))
        OptionColumn(options, modifier)
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
                      fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black))
        }
      }
}

@Composable
fun OptionColumn(options: List<String>, modifier: Modifier = Modifier) {
  var selectedOption by remember { mutableStateOf(options[0]) }
  Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) { // Reduced vertical padding
    options.forEach { option ->
      Row(
          modifier =
              Modifier.fillMaxWidth()
                  .height(40.dp) // Reduced height for each option
                  .padding(horizontal = 4.dp, vertical = 2.dp) // Adjusted padding
                  .background(Color.White, RoundedCornerShape(8.dp))
                  .padding(horizontal = 8.dp), // Inner padding for alignment
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically) {
            Text(
                option,
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp, // Slightly smaller font
                        color = Color.Black))
            RadioButton(
                selected = (selectedOption == option),
                onClick = { selectedOption = option },
                colors = RadioButtonDefaults.colors(selectedColor = Purple40))
          }
    }
  }
}
