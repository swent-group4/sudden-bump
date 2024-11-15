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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Purple40
import com.swent.suddenbump.ui.theme.Purple80

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
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              // Online Presence Section
              Text(
                  "Online presence",
                  color = Color.White,
                  modifier = Modifier.testTag("onlinePresence"))
              Text(
                  "Who can see my last time online",
                  color = Color.White,
                  modifier = Modifier.testTag("lastTimeOnline"))
              OnlinePresenceOptions(modifier = Modifier.testTag("onlinePresenceOptions"))

              Text(
                  "Who can see when I am online",
                  color = Color.White,
                  modifier = Modifier.testTag("whenOnline"))
              OnlineStatusOptions(modifier = Modifier.testTag("onlineStatusOptions"))

              Divider(color = Color.White)

              // Profile Photo Section
              Text(
                  "Profile photo", color = Color.White, modifier = Modifier.testTag("profilePhoto"))
              Text(
                  "Who can see my profile photo",
                  color = Color.White,
                  modifier = Modifier.testTag("whoCanSeeProfilePhoto"))
              ProfilePhotoOptions(modifier = Modifier.testTag("profilePhotoOptions"))

              Divider(color = Color.White)

              // My Info Section
              Text("My info", color = Color.White, modifier = Modifier.testTag("myInfo"))
              Text(
                  "Who can see my info",
                  color = Color.White,
                  modifier = Modifier.testTag("whoCanSeeMyInfo"))
              MyInfoOptions(modifier = Modifier.testTag("myInfoOptions"))

              Divider(color = Color.White)

              // Groups Section
              Text("Groups", color = Color.White, modifier = Modifier.testTag("groups"))
              Text(
                  "Who can add me to groups",
                  color = Color.White,
                  modifier = Modifier.testTag("whoCanAddToGroups"))
              GroupsOptions(modifier = Modifier.testTag("groupsOptions"))

              Divider(color = Color.White)

              // Status Section
              Text("Status", color = Color.White, modifier = Modifier.testTag("status"))
              Text(
                  "Who can see my status",
                  color = Color.White,
                  modifier = Modifier.testTag("whoCanSeeStatus"))
              StatusOptions(modifier = Modifier.testTag("statusOptions"))

              Divider(color = Color.White)

              // Blocked Contacts Section
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

              // Location Status Section
              Text(
                  "Location Status",
                  color = Color.White,
                  modifier = Modifier.testTag("locationStatus"))
              LocationStatusOptions(
                  userViewModel, modifier = Modifier.testTag("locationStatusOptions"))
            }
      })
}

@Composable
fun BlockedContactsList(userViewModel: UserViewModel) {
  val blockedFriends by userViewModel.getBlockedFriends().collectAsState(initial = emptyList())

  Column {
    blockedFriends.forEach { user ->
      Text(user.firstName, color = Color.White) // Assuming the correct property is `firstName`
    }
  }
}

@Composable
fun OnlinePresenceOptions(modifier: Modifier = Modifier) {
  val options = listOf("No one", "Friends", "Everyone")
  var selectedOption by remember { mutableStateOf(options[0]) }

  Column(modifier = modifier) {
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

@Composable
fun OnlineStatusOptions(modifier: Modifier = Modifier) {
  val options = listOf("No one", "Friends", "Everyone")
  var selectedOption by remember { mutableStateOf(options[0]) }

  Column(modifier = modifier) {
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

@Composable
fun ProfilePhotoOptions(modifier: Modifier = Modifier) {
  val options = listOf("No one", "Friends", "Everyone")
  var selectedOption by remember { mutableStateOf(options[0]) }

  Column(modifier = modifier) {
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

@Composable
fun MyInfoOptions(modifier: Modifier = Modifier) {
  val options = listOf("No one", "Friends", "Everyone")
  var selectedOption by remember { mutableStateOf(options[0]) }

  Column(modifier = modifier) {
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

@Composable
fun GroupsOptions(modifier: Modifier = Modifier) {
  val options = listOf("No one", "Friends", "Everyone")
  var selectedOption by remember { mutableStateOf(options[0]) }

  Column(modifier = modifier) {
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

@Composable
fun StatusOptions(modifier: Modifier = Modifier) {
  val options = listOf("No one", "Friends", "Everyone")
  var selectedOption by remember { mutableStateOf(options[0]) }

  Column(modifier = modifier) {
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

@Composable
fun LocationStatusOptions(userViewModel: UserViewModel, modifier: Modifier = Modifier) {
  val options = listOf("Enabled", "Disabled")
  var selectedOption by remember { mutableStateOf(options[0]) }

  Column(modifier = modifier) {
    options.forEach { option ->
      Row(
          modifier =
              Modifier.fillMaxWidth()
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
