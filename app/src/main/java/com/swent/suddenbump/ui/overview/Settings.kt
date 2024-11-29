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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.suddenbump.R
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Purple40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    uri: Uri? = null
) {
  var profilePictureUri = remember { mutableStateOf<Uri?>(uri) }
  var notificationsEnabled by remember { mutableStateOf(true) }

  val launcher =
      rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        profilePictureUri.value = uri
      }

  Scaffold(
      modifier = Modifier.testTag("settingsScreen"),
      topBar = {
        CenterAlignedTopAppBar(
            title = { Text("Settings", maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("backButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go back")
                  }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White))
      },
      content = { paddingValues ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
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
            }
      })
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
                modifier = Modifier.fillMaxSize().testTag("uriProfilePicture"))
          } else {
            if (profilePicture.value.profilePicture != null) {
              userViewModel.getCurrentUser().collectAsState().value.profilePicture?.let {
                Image(
                    bitmap = it,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().testTag("nonNullProfilePicture"))
              }
            } else {
              Image(
                  painter = painterResource(id = R.drawable.settings_user),
                  contentDescription = "Profile Picture",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize().testTag("nullProfilePicture"))
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
