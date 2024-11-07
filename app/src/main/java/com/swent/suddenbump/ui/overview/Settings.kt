package com.swent.suddenbump.ui.settings

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.R
import com.swent.suddenbump.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navigationActions: NavigationActions) {
  var username by remember { mutableStateOf("User123") }
  var profilePictureUri by remember { mutableStateOf<Uri?>(null) }

  val context = LocalContext.current
  val launcher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        profilePictureUri = uri
      }

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
              // Username and Photo Section
              Text("Username and Photo")
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
                          profilePictureUri?.let {
                            val bitmap =
                                BitmapFactory.decodeStream(
                                    context.contentResolver.openInputStream(it))
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize())
                          }
                              ?: Image(
                                  painter = painterResource(id = R.drawable.settings_user),
                                  contentDescription = "Profile Picture",
                                  contentScale = ContentScale.Crop,
                                  modifier = Modifier.fillMaxSize())
                        }

                    // Button to add photo
                    Button(
                        onClick = { launcher.launch("image/*") },
                        Modifier.testTag("addPhotoButton")) {
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
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { /* Save username logic */}),
                        modifier = Modifier.testTag("usernameField"))
                  }

              Divider()

              // Account Section
              Text("Account", Modifier.clickable { navigationActions.navigateTo("AccountScreen") })
              Divider()

              // Confidentiality Section
              Text(
                  "Confidentiality",
                  Modifier.clickable { navigationActions.navigateTo("ConfidentialityScreen") })
              Divider()

              // Discussions Section
              Text(
                  "Discussions",
                  Modifier.clickable { navigationActions.navigateTo("DiscussionsScreen") })
              Divider()

              // Notifications Section
              Text(
                  "Notifications",
                  Modifier.clickable { navigationActions.navigateTo("NotificationsScreen") })
              Divider()

              // Storage and Data Section
              Text(
                  "Storage and Data",
                  Modifier.clickable { navigationActions.navigateTo("StorageAndDataScreen") })
              Divider()

              // Help Section
              Text("Help", Modifier.clickable { navigationActions.navigateTo("HelpScreen") })
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
