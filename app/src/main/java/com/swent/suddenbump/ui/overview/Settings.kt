package com.swent.suddenbump.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navigationActions: NavigationActions) {
  var notificationsEnabled by remember { mutableStateOf(true) }
  var darkModeEnabled by remember { mutableStateOf(false) }
  var username by remember { mutableStateOf("User123") }
  var expanded by remember { mutableStateOf(false) }
  var selectedVisibility by remember { mutableStateOf("Visible for all") }

  Scaffold(
      modifier = Modifier.testTag("settingsScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Settings", Modifier.testTag("settingsTitle")) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() }, Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      },
      content = { pd ->
        Column(
            modifier = Modifier.padding(pd).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              // Profile Picture Section
              Text("Profile Picture")
              Row(
                  modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween) {
                    // Placeholder for profile picture
                    Box(
                        modifier =
                            Modifier.size(80.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                                .testTag("profilePicture")) {
                          Image(
                              painter = painterResource(id = android.R.drawable.ic_menu_camera),
                              contentDescription = "Profile Picture",
                              contentScale = ContentScale.Crop,
                              modifier = Modifier.fillMaxSize())
                        }

                    // Button to add photo
                    Button(onClick = { /* Add photo logic */}, Modifier.testTag("addPhotoButton")) {
                      Text("Add Photo")
                    }
                  }

              // Username setting
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    Text("Username")
                    BasicTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.testTag("usernameField"))
                  }

              // Notifications toggle
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable Notifications")
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        modifier = Modifier.testTag("notificationsSwitch"))
                  }

              // Dark mode toggle
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    Text("Dark Mode")
                    Switch(
                        checked = darkModeEnabled,
                        onCheckedChange = { darkModeEnabled = it },
                        modifier = Modifier.testTag("darkModeSwitch"))
                  }

              // Visibility Drop-down menu
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    Text("Visibility")
                    Box {
                      Button(
                          onClick = { expanded = !expanded },
                          Modifier.testTag("visibilityButton")) {
                            Text(selectedVisibility)
                          }
                      DropdownMenu(
                          expanded = expanded,
                          onDismissRequest = { expanded = false },
                      ) {
                        DropdownMenuItem(
                            onClick = {
                              selectedVisibility = "Visible for all"
                              expanded = false
                            },
                            text = { Text("Visible for all") })
                        DropdownMenuItem(
                            onClick = {
                              selectedVisibility = "Visible for my contacts"
                              expanded = false
                            },
                            text = { Text("Visible for my contacts") })
                        DropdownMenuItem(
                            onClick = {
                              selectedVisibility = "Visible for my friends"
                              expanded = false
                            },
                            text = { Text("Visible for my friends") })
                      }
                    }
                  }
            }
      })
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  SettingsScreen(navigationActions)
}
