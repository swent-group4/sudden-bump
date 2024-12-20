package com.swent.suddenbump.ui.overview

import android.app.Activity
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.suddenbump.R
import com.swent.suddenbump.model.meeting.MeetingViewModel
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.theme.DividerColor
import com.swent.suddenbump.ui.theme.Purple40
import com.swent.suddenbump.ui.theme.VibrantPurple
import com.swent.suddenbump.ui.utils.CustomCenterAlignedTopBar
import com.yalantis.ucrop.UCrop
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    meetingViewModel: MeetingViewModel,
    uri: Uri? = null
) {
  var notificationsEnabled by remember {
    mutableStateOf(userViewModel.getSavedNotificationStatus())
  }
  var radius by remember { mutableFloatStateOf(userViewModel.getSavedRadius()) }

  Scaffold(
      modifier = Modifier.testTag("settingsScreen"),
      topBar = { CustomCenterAlignedTopBar("Settings", navigationActions) },
      content = { paddingValues ->
        LazyColumn(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black)
                    .padding(paddingValues)
                    .padding(16.dp)
                    .testTag("settingsLazyColumn"),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              item { ProfileSection(userViewModel) }
              item {
                SettingsOption(
                    label = "Account",
                    backgroundColor = Color.White,
                    onClick = { navigationActions.navigateTo(Screen.ACCOUNT) },
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
                    label = "Blocked Users",
                    backgroundColor = Color.White,
                    onClick = { navigationActions.navigateTo(Screen.BLOCKED_USERS) },
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
                // Delete all meetings buttons
                DeleteAllButton(
                    onClick = {
                      meetingViewModel.deleteMeetingsForUser(
                          userViewModel.getCurrentUser().value.uid)
                    },
                    toDelete = "meetings",
                    confirmationText =
                        "Are you sure you want to delete all meetings in which you are participating? This action can't be undone.")
              }
              item {
                // Delete all chats buttons
                DeleteAllButton(
                    onClick = { userViewModel.deleteAllMessages() },
                    toDelete = "chats",
                    confirmationText =
                        "Are you sure you want to delete all conversations in which you participated? This action can't be undone.")
              }
            }
      })
}

/**
 * A composable function that displays a slider for adjusting the radius.
 *
 * @param radius The current radius value.
 * @param onRadiusChange A callback function to handle changes in the radius value.
 * @param modifier A [Modifier] for this composable.
 */
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
            text = "Notification Radius",
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold, color = Color.Black),
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
fun ProfileSection(userViewModel: UserViewModel) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // Observe user profile from UserViewModel
  val currentUser by userViewModel.getCurrentUser().collectAsState()

  // Local state for profile picture URI
  var profilePictureUri by remember { mutableStateOf<Uri?>(null) }

  // Launcher for cropping images
  val cropLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
              result.data?.let { data ->
                val resultUri = UCrop.getOutput(data)
                profilePictureUri = resultUri
              }
            }
          }

  // Launcher for selecting images
  val launcher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri?
        ->
        uri?.let {
          val destinationUri = Uri.fromFile(File(context.cacheDir, "cropped_image.jpg"))
          val uCropIntent =
              UCrop.of(it, destinationUri)
                  .withAspectRatio(1f, 1f)
                  .withMaxResultSize(512, 512)
                  .getIntent(context)
          cropLauncher.launch(uCropIntent)
        }
      }

  // Handle changes in `profilePictureUri`
  LaunchedEffect(profilePictureUri) {
    profilePictureUri?.let { uri ->
      try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
        inputStream?.close()

        // Update the user profile in the ViewModel
        if (bitmap != null) {
          userViewModel.setUser(
              currentUser.copy(profilePicture = bitmap),
              onSuccess = { Log.i("Profile", "Profile updated successfully") },
              onFailure = { Log.e("Profile", "Failed to update profile") })
        }
      } catch (e: Exception) {
        Log.e("Profile", "Error loading profile picture", e)
      }
    }
  }

  Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = currentUser.firstName,
        style =
            MaterialTheme.typography.titleMedium.copy(
                fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White),
        modifier = Modifier.testTag("userName"))

    Spacer(modifier = Modifier.height(16.dp))
    Box(
        modifier =
            Modifier.size(120.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .testTag("profilePicture")) {
          currentUser.profilePicture?.let {
            Image(
                bitmap = it,
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().testTag("nonNullProfilePicture"))
          }
              ?: Image(
                  painter = painterResource(id = R.drawable.settings_user),
                  contentDescription = "Default Profile Picture",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize().testTag("nullProfilePicture"))
        }

    Spacer(modifier = Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      Button(
          onClick = { coroutineScope.launch { launcher.launch("image/*") } },
          modifier = Modifier.testTag("uploadPhotoButton"),
          colors = ButtonDefaults.buttonColors(containerColor = Purple40)) {
            Text(
                text = if (currentUser.profilePicture != null) "Edit Photo" else "Add Photo",
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold, color = Color.White))
          }

      if (currentUser.profilePicture != null) {
        Button(
            onClick = {
              // Clear the profile picture
              userViewModel.setUser(
                  currentUser.copy(profilePicture = null),
                  onSuccess = { Log.i("Profile", "Profile picture removed") },
                  onFailure = { Log.e("Profile", "Failed to remove profile picture") })
            },
            modifier = Modifier.testTag("removePhotoButton"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
              Text(
                  text = "Remove Photo",
                  style =
                      MaterialTheme.typography.bodyLarge.copy(
                          fontWeight = FontWeight.Bold, color = Color.White))
            }
      }
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
fun DeleteAllButton(onClick: () -> Unit, toDelete: String, confirmationText: String) {
  var showDialog by remember { mutableStateOf(false) }

  Button(
      onClick = { showDialog = true }, // Show the confirmation dialog when clicked
      colors =
          ButtonDefaults.buttonColors(
              containerColor = Purple40, // Background color of the button
              contentColor = Color.White // Text color of the button
              ),
      shape = RoundedCornerShape(8.dp), // Match the less-rounded style of "Delete Account"
      modifier =
          Modifier.fillMaxWidth() // Make the button span the full width of the column
              .testTag("deleteAllButton_$toDelete") // Tag for testing this button
              .padding(horizontal = 8.dp) // Padding to keep space on the sides
      ) {
        // Text displayed on the button
        Row(
            verticalAlignment = Alignment.CenterVertically, // Center align icon and text
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Add spacing between icon and text
            ) {
              Icon(
                  imageVector = Icons.Default.Delete, // Use a default trash icon
                  contentDescription = "Delete Icon", // Accessibility description
                  tint = Color.White // Match the icon color to the text
                  )
              Text(
                  text = "Delete all $toDelete",
                  style =
                      MaterialTheme.typography.bodyLarge.copy(
                          fontWeight = FontWeight.Bold // Bold font for emphasis
                          ))
            }
      }

  // Confirmation Dialog
  if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = {
          Text(
              text = "Delete all $toDelete", modifier = Modifier.testTag("delete_${toDelete}_text"))
        },
        text = {
          Text(text = confirmationText, modifier = Modifier.testTag("areYouSureText_$toDelete"))
        },
        modifier = Modifier.testTag("deleteAllDialog_$toDelete"),
        confirmButton = {
          TextButton(modifier = Modifier.testTag("confirmButton"), onClick = onClick) {
            Text("Confirm")
          }
        },
        dismissButton = {
          TextButton(
              modifier = Modifier.testTag("cancelButton"), onClick = { showDialog = false }) {
                Text("Cancel")
              }
        })
  }
}
