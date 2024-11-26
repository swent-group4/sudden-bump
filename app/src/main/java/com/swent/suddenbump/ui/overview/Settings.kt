package com.swent.suddenbump.ui.overview

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
  val profilePictureUri = remember { mutableStateOf<Uri?>(null) }
  var notificationsEnabled by remember { mutableStateOf(true) }

  val context = LocalContext.current
  val launcher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        profilePictureUri.value = uri
      }

  SampleAppTheme(darkTheme = true) {
    Scaffold(
        modifier = Modifier.testTag("settingsScreen"),
        topBar = {
          TopAppBar(
              title = {
                Text(
                    "Settings",
                    style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    modifier = Modifier.testTag("settingsTitle"))
              },
              navigationIcon = {
                IconButton(
                    onClick = { navigationActions.goBack() }, Modifier.testTag("goBackButton")) {
                      Icon(
                          imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                          contentDescription = "Back",
                          tint = Color.White)
                    }
              },
              colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple40))
        },
        content = { paddingValues ->
          LazyColumn(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(paddingValues)
                      .padding(16.dp)
                      .background(Color.Black)
                      .testTag("settingsLazyColumn"),
              verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item { ProfileSection(profilePictureUri, launcher, userViewModel) }
                item {
                  SettingsOption(
                      label = "Account",
                      backgroundColor = Color.White,
                      onClick = { navigationActions.navigateTo("AccountScreen") },
                      modifier = Modifier.testTag("AccountOption"))
                }
                item {
                  SettingsOption(
                      label = "Confidentiality",
                      backgroundColor = Color.White,
                      onClick = { navigationActions.navigateTo("ConfidentialityScreen") },
                      modifier = Modifier.testTag("ConfidentialityOption"))
                }
                item {
                  SettingsOption(
                      label = "Discussions",
                      backgroundColor = Color.White,
                      onClick = { navigationActions.navigateTo("DiscussionsScreen") },
                      modifier = Modifier.testTag("DiscussionsOption"))
                }
                item {
                  NotificationsSwitch(
                      notificationsEnabled = notificationsEnabled,
                      onCheckedChange = {
                        notificationsEnabled = it
                        onNotificationsEnabledChange(it)
                      },
                      modifier = Modifier.testTag("NotificationsSwitch"))
                }
                item {
                  SettingsOption(
                      label = "Storage and Data",
                      backgroundColor = Color.White,
                      onClick = { navigationActions.navigateTo("StorageAndDataScreen") },
                      modifier = Modifier.testTag("StorageAndDataOption"))
                }
                item {
                  SettingsOption(
                      label = "Help",
                      backgroundColor = Color.White,
                      onClick = { navigationActions.navigateTo("HelpScreen") },
                      modifier = Modifier.testTag("HelpOption"))
                }
              }
        })
  }
}

@Composable
fun ProfileSection(
    profilePictureUri: MutableState<Uri?>,
    launcher: ActivityResultLauncher<String>,
    userViewModel: UserViewModel
) {
  val context = LocalContext.current
  val userName = userViewModel.getCurrentUser().collectAsState().value.firstName
  val profilePicture = userViewModel.getCurrentUser().collectAsState()

  Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    Spacer(modifier = Modifier.height(16.dp)) // Additional space above the photo
    Text(
        userName,
        style =
            MaterialTheme.typography.titleMedium.copy(
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White),
        modifier = Modifier.testTag("userName"))

    Spacer(modifier = Modifier.height(16.dp)) // Additional space above the photo
    Box(
        modifier =
            Modifier.size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .testTag("profilePicture")) {
          if (profilePictureUri.value != null) {
            val bitmap =
                BitmapFactory.decodeStream(
                    context.contentResolver.openInputStream(profilePictureUri.value!!))
            bitmap.let {
              userViewModel.setUser(
                  userViewModel
                      .getCurrentUser()
                      .collectAsState()
                      .value
                      .copy(profilePicture = bitmap.asImageBitmap()),
                  onSuccess = {
                    profilePictureUri.value = null
                    Log.i("Firebase", "Status : User updated")
                  },
                  onFailure = {
                    profilePictureUri.value = null
                    Log.i("Firebase", "Status : User update failed")
                  })
            }
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize())
          } else {
            if (profilePicture.value.profilePicture != null) {
              userViewModel.getCurrentUser().collectAsState().value.profilePicture?.let {
                Image(
                    bitmap = it,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize())
              }
            } else {
              Image(
                  painter = painterResource(id = R.drawable.settings_user),
                  contentDescription = "Profile Picture",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize())
            }
          }
        }
    Spacer(modifier = Modifier.height(16.dp)) // Additional space below the photo
    Button(
        onClick = { launcher.launch("image/*") },
        modifier = Modifier.testTag("addPhotoButton"),
        colors = ButtonDefaults.buttonColors(containerColor = Purple40)) {
          Text(
              "Add Photo",
              style =
                  MaterialTheme.typography.bodyLarge.copy(
                      fontWeight = FontWeight.Bold, color = Color.White))
        }
  }
}

@Composable
fun SettingsOption(
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp)
              .clickable { onClick() }
              .background(backgroundColor, RoundedCornerShape(8.dp))
              .height(48.dp),
      verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            modifier = Modifier.padding(start = 16.dp))
      }
}

@Composable
fun NotificationsSwitch(
    notificationsEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .height(56.dp)
              .padding(horizontal = 8.dp)
              .background(Color.White, RoundedCornerShape(8.dp))
              .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Enable Notifications",
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold, color = Color.Black))
        Switch(
            checked = notificationsEnabled,
            onCheckedChange = onCheckedChange,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Purple40,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.DarkGray))
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
