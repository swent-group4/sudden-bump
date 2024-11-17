package com.swent.suddenbump.ui.overview

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.suddenbump.R
import com.swent.suddenbump.model.chat.ChatRepositoryFirestore
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Purple40
import com.swent.suddenbump.ui.theme.SampleAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    onNotificationsEnabledChange: (Boolean) -> Unit
) {
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            profilePictureUri = uri
        }

    SampleAppTheme(darkTheme = true) {
        Scaffold(
            modifier = Modifier.testTag("settingsScreen"),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Settings",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ),
                            modifier = Modifier.testTag("settingsTitle")
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navigationActions.goBack() },
                            Modifier.testTag("goBackButton")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple40)
                )
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .background(Color.Black),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileSection(profilePictureUri, launcher, userViewModel)
                    SettingsOption(
                        label = "Account",
                        backgroundColor = Color.White,
                        onClick = { navigationActions.navigateTo("AccountScreen") },
                        testTag = "AccountOption"
                    )
                    SettingsOption(
                        label = "Confidentiality",
                        backgroundColor = Color.White,
                        onClick = { navigationActions.navigateTo("ConfidentialityScreen") },
                        testTag = "ConfidentialityOption"
                    )
                    SettingsOption(
                        label = "Discussions",
                        backgroundColor = Color.White,
                        onClick = { navigationActions.navigateTo("DiscussionsScreen") },
                        testTag = "DiscussionsOption"
                    )
                    NotificationsSwitch(notificationsEnabled) {
                        notificationsEnabled = it
                        onNotificationsEnabledChange(it)
                    }
                    SettingsOption(
                        label = "Storage and Data",
                        backgroundColor = Color.White,
                        onClick = { navigationActions.navigateTo("StorageAndDataScreen") },
                        testTag = "StorageAndDataOption"
                    )
                    SettingsOption(
                        label = "Help",
                        backgroundColor = Color.White,
                        onClick = { navigationActions.navigateTo("HelpScreen") },
                        testTag = "HelpOption"
                    )
                }
            }
        )
    }
}

@Composable
fun ProfileSection(
    profilePictureUri: Uri?,
    launcher: ActivityResultLauncher<String>,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val userName = userViewModel.getCurrentUser().collectAsState().value.firstName

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp)) // Additional space above the photo
        Text(
            userName,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.testTag("userName")
        )

        Spacer(modifier = Modifier.height(16.dp)) // Additional space above the photo
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .testTag("profilePicture")
        ) {
            profilePictureUri?.let {
                val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(it))
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
                ?: Image(
                    painter = painterResource(id = R.drawable.settings_user),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
        }
        Spacer(modifier = Modifier.height(16.dp)) // Additional space below the photo
        Button(
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.testTag("addPhotoButton"),
            colors = ButtonDefaults.buttonColors(containerColor = Purple40) // Changed to Purple40
        ) {
            Text(
                "Add Photo",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
fun SettingsOption(
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { onClick() }
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .height(48.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically // Center content vertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            modifier = Modifier.padding(start = 16.dp) // Add padding to the left
        )
    }
}


@Composable
fun NotificationsSwitch(notificationsEnabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp), // Padding inside the Row
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically // Center vertically
    ) {
        Text(
            text = "Enable Notifications",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        )
        Switch(
            checked = notificationsEnabled,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Purple40,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}




@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)

    val firestoreInstance = FirebaseFirestore.getInstance()
    val authInstance = FirebaseAuth.getInstance()

    val userRepository =
        UserRepositoryFirestore(db = firestoreInstance, context = LocalContext.current)
    val chatRepository = ChatRepositoryFirestore(firestore = firestoreInstance)

    val userViewModel = UserViewModel(userRepository, chatRepository)
    SettingsScreen(navigationActions, userViewModel, onNotificationsEnabledChange = {})
}
