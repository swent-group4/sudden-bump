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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.ui.navigation.NavigationActions

@Composable
fun SettingsScreen(navigationActions: NavigationActions) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("User123") }
    var expandedVisibility by remember { mutableStateOf(false) }
    var selectedVisibility by remember { mutableStateOf("Visible for all") }
    var expandedLanguage by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var accountPrivate by remember { mutableStateOf(false) }
    var dataUsageLimit by remember { mutableStateOf(50f) }

    Scaffold(
        modifier = Modifier.testTag("settingsScreen"),
        topBar = {
            CustomTopBar(
                title = "Settings",
                onBackClick = { navigationActions.goBack() }
            )
        },
        content = { pd ->
            Column(
                modifier = Modifier
                    .padding(pd)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Picture Section
                Text("Profile Picture")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Placeholder for profile picture
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .testTag("profilePicture")
                    ) {
                        Image(
                            painter = painterResource(id = android.R.drawable.ic_menu_camera),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Button to add photo
                    Button(onClick = { /* Add photo logic */ }, Modifier.testTag("addPhotoButton")) {
                        Text("Add Photo")
                    }
                }

                // Username setting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Username")
                    BasicTextField(
                        value = username,
                        onValueChange = { username = it },
                        modifier = Modifier.testTag("usernameField")
                    )
                }

                // Notifications toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Notifications")
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        modifier = Modifier.testTag("notificationsSwitch")
                    )
                }

                // Dark mode toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Dark Mode")
                    Switch(
                        checked = darkModeEnabled,
                        onCheckedChange = { darkModeEnabled = it },
                        modifier = Modifier.testTag("darkModeSwitch")
                    )
                }

                // Visibility Drop-down menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Visibility")
                    Box {
                        Button(onClick = { expandedVisibility = !expandedVisibility }, Modifier.testTag("visibilityButton")) {
                            Text(selectedVisibility)
                        }
                        DropdownMenu(
                            expanded = expandedVisibility,
                            onDismissRequest = { expandedVisibility = false },
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    selectedVisibility = "Visible for all"
                                    expandedVisibility = false
                                },
                                text = { Text("Visible for all") }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    selectedVisibility = "Visible for my contacts"
                                    expandedVisibility = false
                                },
                                text = { Text("Visible for my contacts") }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    selectedVisibility = "Visible for my friends"
                                    expandedVisibility = false
                                },
                                text = { Text("Visible for my friends") }
                            )
                        }
                    }
                }

                // Language Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Language")
                    Box {
                        Button(onClick = { expandedLanguage = !expandedLanguage }, Modifier.testTag("languageButton")) {
                            Text(selectedLanguage)
                        }
                        DropdownMenu(
                            expanded = expandedLanguage,
                            onDismissRequest = { expandedLanguage = false },
                        ) {
                            DropdownMenuItem(
                                onClick = {
                                    selectedLanguage = "English"
                                    expandedLanguage = false
                                },
                                text = { Text("English") }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    selectedLanguage = "Spanish"
                                    expandedLanguage = false
                                },
                                text = { Text("Spanish") }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    selectedLanguage = "French"
                                    expandedLanguage = false
                                },
                                text = { Text("French") }
                            )
                        }
                    }
                }

                // Account Privacy toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Account Private")
                    Switch(
                        checked = accountPrivate,
                        onCheckedChange = { accountPrivate = it },
                        modifier = Modifier.testTag("accountPrivacySwitch")
                    )
                }

                // Data Usage Limit Slider
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Data Usage Limit: ${dataUsageLimit.toInt()}MB")
                    Slider(
                        value = dataUsageLimit,
                        onValueChange = { dataUsageLimit = it },
                        valueRange = 0f..100f,
                        modifier = Modifier.testTag("dataUsageSlider")
                    )
                }

                // Change Password Button
                Button(
                    onClick = { /* Navigate to change password screen */ },
                    modifier = Modifier.fillMaxWidth().testTag("changePasswordButton")
                ) {
                    Text("Change Password")
                }

                // Logout Button
                Button(
                    onClick = { /* Add logout logic */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth().testTag("logoutButton")
                ) {
                    Text("Logout", color = Color.White)
                }
            }
        }
    )
}

@Composable
fun CustomTopBar(title: String, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("customGoBackButton")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    SettingsScreen(navigationActions)
}