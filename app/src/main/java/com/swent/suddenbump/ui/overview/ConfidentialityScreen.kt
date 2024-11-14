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
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Purple40
import com.swent.suddenbump.ui.theme.Purple80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfidentialityScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Confidentiality", color = Color.White) },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              // Online Presence Section
              Text("Online presence", color = Color.White)
              Text("Who can see my last time online", color = Color.White)
              OnlinePresenceOptions()

              Text("Who can see when I am online", color = Color.White)
              OnlineStatusOptions()

              Divider(color = Color.White)

              // Profile Photo Section
              Text("Profile photo", color = Color.White)
              Text("Who can see my profile photo", color = Color.White)
              ProfilePhotoOptions()

              Divider(color = Color.White)

              // My Info Section
              Text("My info", color = Color.White)
              Text("Who can see my info", color = Color.White)
              MyInfoOptions()

              Divider(color = Color.White)

              // Groups Section
              Text("Groups", color = Color.White)
              Text("Who can add me to groups", color = Color.White)
              GroupsOptions()

              Divider(color = Color.White)

              // Status Section
              Text("Status", color = Color.White)
              Text("Who can see my status", color = Color.White)
              StatusOptions()

              Divider(color = Color.White)

              // Blocked Contacts Section
              Text("Blocked Contacts", color = Color.White)
              Button(
                  onClick = { /* Add logic to add blocked contacts */},
                  colors = ButtonDefaults.buttonColors(containerColor = Purple80)) {
                    Text("Add to blocked contacts", color = Color.White)
                  }

              Divider(color = Color.White)

              // Location Status Section
              Text("Location Status", color = Color.White)
              LocationStatusOptions(userViewModel)
            }
      })
}

@Composable
fun OnlinePresenceOptions() {
  val options = listOf("Everyone", "My contacts", "My contacts except...", "No one")
  var selectedOption by remember { mutableStateOf(options[0]) }

  options.forEach { option ->
    Row(
        modifier =
            Modifier.fillMaxWidth().background(Color.Gray, RoundedCornerShape(8.dp)).padding(1.dp),
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

@Composable
fun OnlineStatusOptions() {
  val options = listOf("Everyone", "Same as last time online")
  var selectedOption by remember { mutableStateOf(options[0]) }

  options.forEach { option ->
    Row(
        modifier =
            Modifier.fillMaxWidth().background(Color.Gray, RoundedCornerShape(8.dp)).padding(1.dp),
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

@Composable
fun ProfilePhotoOptions() {
  val options = listOf("Everyone", "My contacts", "My contacts except...", "No one")
  var selectedOption by remember { mutableStateOf(options[0]) }

  options.forEach { option ->
    Row(
        modifier =
            Modifier.fillMaxWidth().background(Color.Gray, RoundedCornerShape(8.dp)).padding(1.dp),
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

@Composable
fun MyInfoOptions() {
  val options = listOf("Everyone", "My contacts", "My contacts except...", "No one")
  var selectedOption by remember { mutableStateOf(options[0]) }

  options.forEach { option ->
    Row(
        modifier =
            Modifier.fillMaxWidth().background(Color.Gray, RoundedCornerShape(8.dp)).padding(1.dp),
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

@Composable
fun GroupsOptions() {
  val options = listOf("Everyone", "My contacts", "My contacts except...")
  var selectedOption by remember { mutableStateOf(options[0]) }

  options.forEach { option ->
    Row(
        modifier =
            Modifier.fillMaxWidth().background(Color.Gray, RoundedCornerShape(8.dp)).padding(1.dp),
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

@Composable
fun StatusOptions() {
  val options = listOf("Everyone", "My contacts", "My contacts except...")
  var selectedOption by remember { mutableStateOf(options[0]) }

  options.forEach { option ->
    Row(
        modifier =
            Modifier.fillMaxWidth().background(Color.Gray, RoundedCornerShape(8.dp)).padding(1.dp),
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

@Composable
fun LocationStatusOptions(userViewModel: UserViewModel) {
  val options = listOf("Enabled", "Disabled")
  var selectedOption by remember { mutableStateOf(options[0]) }

  options.forEach { option ->
    Row(
        modifier =
            Modifier.fillMaxWidth().background(Color.Gray, RoundedCornerShape(8.dp)).padding(1.dp),
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
