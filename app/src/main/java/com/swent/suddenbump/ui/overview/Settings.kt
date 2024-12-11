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
import androidx.compose.material.icons.filled.Delete
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
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.DividerColor
import com.swent.suddenbump.ui.theme.Purple40
import com.swent.suddenbump.ui.theme.VibrantPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    meetingViewModel: MeetingViewModel,
    uri: Uri? = null
) {
  var profilePictureUri = remember { mutableStateOf<Uri?>(uri) }
  var notificationsEnabled by remember {
    mutableStateOf(userViewModel.getSavedNotificationStatus())
  }
  var radius by remember { mutableStateOf(userViewModel.getSavedRadius()) }

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
                    .background(Color.Black)
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
                RadiusSlider(
                    radius = radius,
                    onRadiusChange = {
                      radius = it
                      userViewModel.saveRadius(it)
                    },
                    modifier = Modifier.testTag("RadiusSlider"))
              }
              item {
                SettingsOption(
                    label = "Discussions",
                    backgroundColor = Color.White,
                    onClick = { navigationActions.navigateTo("DiscussionsScreen") },
                    modifier = Modifier.testTag("DiscussionsOption"))
              }
              item {
                SettingsOption(
                    label = "Blocked Users",
                    backgroundColor = Color.White,
                    onClick = { navigationActions.navigateTo("BlockedUsersScreen") },
                    modifier = Modifier.testTag("BlockedUsersOption"))
              }
              item {
                NotificationsSwitch(
                    notificationsEnabled = notificationsEnabled,
                    onCheckedChange = {
                      notificationsEnabled = it
                      userViewModel.saveNotificationStatus(it)
                    },
                    modifier = Modifier.testTag("NotificationsSwitch"))
              }
              item {
                DeleteAllMeetingsItem(
                    meetingViewModel,
                    userViewModel,
                    modifier = Modifier.testTag("DeleteAllMeetings"))
              }
            }
      })
}

@Composable
fun RadiusSlider(radius: Float, onRadiusChange: (Float) -> Unit, modifier: Modifier = Modifier) {
  Column(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp)
              .background(DividerColor, RoundedCornerShape(8.dp))
              .padding(16.dp)) {
        Text(
            text = "Adjust Radius",
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold, color = VibrantPurple),
            modifier = Modifier.padding(bottom = 8.dp))
        Slider(
            value = radius,
            onValueChange = onRadiusChange,
            valueRange = 3f..12f,
            steps = 3,
            colors =
                SliderDefaults.colors(
                    thumbColor = Purple40,
                    activeTrackColor = VibrantPurple,
                    inactiveTrackColor = Purple40.copy(alpha = 0.5f),
                    activeTickColor = VibrantPurple,
                    inactiveTickColor = Purple40.copy(alpha = 0.5f)),
            modifier = Modifier.testTag("RadiusSliderControl"))
        Text(
            text = "Radius: ${radius.toInt()} km",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
            modifier = Modifier.padding(top = 8.dp))
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

@Composable
fun DeleteAllMeetingsItem(
    meetingViewModel: MeetingViewModel,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier
) {

  var showDialog by remember { mutableStateOf(false) }

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
            text = "Delete All Meetings",
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold, color = Color.Black),
            modifier = Modifier.testTag("deleteAllMeetingsText"))
        Button(
            onClick = { showDialog = true },
            colors =
                ButtonDefaults.buttonColors(containerColor = Purple40, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            modifier =
                Modifier.defaultMinSize(minWidth = 80.dp, minHeight = 40.dp)
                    .padding(4.dp)
                    .testTag("DeleteButton")) {
              Row(
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                  verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Icon",
                        tint = Color.Black)
                    Text(
                        text = "Delete",
                        style =
                            MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                  }
            }
      }

  // Confirmation Dialog
  if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = {
          Text(text = "Delete All Meetings", modifier = Modifier.testTag("deleteAllMeetingsTitle"))
        },
        text = {
          Text(
              text = "Are you sure you want to delete all your scheduled meetings?",
              modifier = Modifier.testTag("AreYouSureDeleteText"))
        },
        modifier = Modifier.testTag("deleteAllMeetingsDialog"),
        confirmButton = {
          Button(
              onClick = {
                showDialog = false
                meetingViewModel.deleteMeetingsForUser(
                    userViewModel.getCurrentUser().value?.uid ?: "")
              },
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = Color.Red, contentColor = Color.White)) {
                Text("Yes")
              }
        },
        dismissButton = {
          Button(
              onClick = { showDialog = false },
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = Color.Gray, contentColor = Color.White)) {
                Text("No")
              }
        })
  }
}
