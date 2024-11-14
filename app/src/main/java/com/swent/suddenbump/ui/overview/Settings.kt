package com.swent.suddenbump.ui.overview

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.swent.suddenbump.R
import com.swent.suddenbump.model.chat.ChatRepositoryFirestore
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Purple40
import com.swent.suddenbump.ui.theme.Purple80
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
              title = { Text("Settings", Modifier.testTag("settingsTitle"), color = Color.White) },
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
        content = { pd ->
          Column(
              modifier = Modifier.padding(pd).fillMaxSize().padding(16.dp).background(Color.Black),
              verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Username and Photo Section
                Text(
                    userViewModel.getCurrentUser().collectAsState().value.firstName,
                    color = Color.White)
                Box(
                    modifier =
                        Modifier.size(120.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .align(Alignment.CenterHorizontally)
                            .testTag("profilePicture")) {
                      profilePictureUri?.let {
                        val bitmap =
                            BitmapFactory.decodeStream(context.contentResolver.openInputStream(it))
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
                    Modifier.align(Alignment.CenterHorizontally).testTag("addPhotoButton"),
                    colors = ButtonDefaults.buttonColors(containerColor = Purple80)) {
                      Text("Add Photo", color = Color.White)
                    }

                // Account Section
                Text(
                    "Account",
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable { navigationActions.navigateTo("AccountScreen") }
                        .background(Color.Gray, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    color = Color.White)

                // Confidentiality Section
                Text(
                    "Confidentiality",
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable { navigationActions.navigateTo("ConfidentialityScreen") }
                        .background(Color.Gray, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    color = Color.White)

                // Discussions Section
                Text(
                    "Discussions",
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable { navigationActions.navigateTo("DiscussionsScreen") }
                        .background(Color.Gray, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    color = Color.White)

                // Notifications Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          "Enable Notifications",
                          Modifier.weight(1f)
                              .padding(horizontal = 8.dp)
                              .background(Color.Gray, RoundedCornerShape(8.dp))
                              .padding(8.dp),
                          color = Color.White)
                      Switch(
                          checked = notificationsEnabled,
                          onCheckedChange = {
                            notificationsEnabled = it
                            onNotificationsEnabledChange(it)
                          },
                          colors =
                              SwitchDefaults.colors(
                                  checkedThumbColor = Color.White,
                                  checkedTrackColor = Purple80,
                                  uncheckedThumbColor = Color.Gray,
                                  uncheckedTrackColor = Color.DarkGray))
                    }

                // Storage and Data Section
                Text(
                    "Storage and Data",
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable { navigationActions.navigateTo("StorageAndDataScreen") }
                        .background(Color.Gray, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    color = Color.White)

                // Help Section
                Text(
                    "Help",
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable { navigationActions.navigateTo("HelpScreen") }
                        .background(Color.Gray, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    color = Color.White)
              }
        })
  }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)

  val firestoreInstance = FirebaseFirestore.getInstance()
  val authInstance = FirebaseAuth.getInstance()

  val userRepository = UserRepositoryFirestore(firestoreInstance)
  val chatRepository = ChatRepositoryFirestore(firestore = firestoreInstance, auth = authInstance)

  val userViewModel = UserViewModel(userRepository, chatRepository)
  SettingsScreen(navigationActions, userViewModel, onNotificationsEnabledChange = {})
}
