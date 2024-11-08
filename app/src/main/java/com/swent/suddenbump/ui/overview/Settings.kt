package com.swent.suddenbump.ui.overview

import android.graphics.BitmapFactory
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
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.suddenbump.R
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
    var username by remember { mutableStateOf("User123") }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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
                        modifier = Modifier.size(80.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .testTag("profilePicture")) {
                        profilePictureUri?.let {
                            val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(it))
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize())
                        } ?: Image(
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
                        keyboardActions = KeyboardActions(onDone = {
                            userViewModel.updateUsername(username, {
                                // Handle success
                            }, {
                                // Handle failure
                            })
                        }),
                        modifier = Modifier.testTag("usernameField"))
                }

                HorizontalDivider()

                // Account Section
                Text("Account", Modifier.clickable { navigationActions.navigateTo("AccountScreen") })
                HorizontalDivider()

                // Confidentiality Section
                Text("Confidentiality", Modifier.clickable { navigationActions.navigateTo("ConfidentialityScreen") })
                HorizontalDivider()

                // Discussions Section
                Text("Discussions", Modifier.clickable { navigationActions.navigateTo("DiscussionsScreen") })
                HorizontalDivider()

                // Notifications Section
                Text("Notifications", Modifier.clickable { navigationActions.navigateTo("NotificationsScreen") })
                HorizontalDivider()

                // Storage and Data Section
                Text("Storage and Data", Modifier.clickable { navigationActions.navigateTo("StorageAndDataScreen") })
                HorizontalDivider()

                // Help Section
                Text("Help", Modifier.clickable { navigationActions.navigateTo("HelpScreen") })

                HorizontalDivider()

                // Location Status Section
                LocationStatusSection(userViewModel)
            }
        })
}

@Composable
fun LocationStatusSection(userViewModel: UserViewModel) {
    val isLocationSharingEnabled by userViewModel.isLocationSharingEnabled.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Location Sharing")
        Switch(
            checked = isLocationSharingEnabled,
            onCheckedChange = { userViewModel.updateLocationSharingStatus(it) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    val userRepository = UserRepositoryFirestore(FirebaseFirestore.getInstance())
    val userViewModel = UserViewModel(userRepository)
    SettingsScreen(navigationActions, userViewModel)
}