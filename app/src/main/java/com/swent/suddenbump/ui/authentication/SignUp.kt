package com.swent.suddenbump.ui.authentication

import android.app.Activity
import android.location.Location
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.swent.suddenbump.R
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.TopLevelDestinations
import com.swent.suddenbump.ui.theme.Purple40
import com.swent.suddenbump.ui.theme.PurpleGrey40
import com.swent.suddenbump.ui.utils.PhoneNumberVisualTransformation
import com.yalantis.ucrop.UCrop
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Composable function for the Sign-Up screen.
 *
 * @param navigationActions Actions for navigating between screens.
 * @param userViewModel ViewModel for managing user-related data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {

  // Get the current context
  val context = LocalContext.current

  // State variables for user input fields
  var firstName by remember {
    mutableStateOf(
        FirebaseAuth.getInstance().currentUser?.displayName?.split(" ")?.firstOrNull() ?: "")
  }
  var lastName by remember {
    mutableStateOf(
        FirebaseAuth.getInstance().currentUser?.displayName?.split(" ")?.lastOrNull() ?: "")
  }
  val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
  var phoneNumber by remember {
    mutableStateOf(FirebaseAuth.getInstance().currentUser?.phoneNumber ?: "")
  }
  var verificationCode by remember { mutableStateOf("") }
  var isCodeSent by remember { mutableStateOf(false) }
  var profilePictureUri by remember { mutableStateOf<Uri?>(null) }

  // Coroutine scope for launching coroutines
  val coroutineScope = rememberCoroutineScope()

  // StateFlow for base location
  val baseLocation =
      MutableStateFlow(
          Location("providerName").apply {
            latitude = 0.0
            longitude = 0.0
          })

  // LiveData for verification status
  val verificationStatus by userViewModel.verificationStatus.observeAsState()

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

  // Scaffold for the Sign-Up screen layout
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(text = "Sign Up", color = Color.White) },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White)
              }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black, titleContentColor = Color.White))
      },
      containerColor = Color.Black,
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())) {
              // Profile picture selection
              Box(
                  modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                  contentAlignment = Alignment.Center) {
                    IconButton(
                        onClick = { coroutineScope.launch { launcher.launch("image/*") } },
                        modifier =
                            Modifier.size(64.dp)
                                .background(Color.Gray, CircleShape)
                                .testTag("profilePictureButton")) {
                          if (profilePictureUri != null) {
                            val bitmap =
                                MediaStore.Images.Media.getBitmap(
                                    LocalContext.current.contentResolver, profilePictureUri)
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(64.dp).testTag("profilePicture"))
                          } else {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_add_a_photo_24),
                                contentDescription = "Import Profile Picture",
                                tint = Color.White,
                                modifier = Modifier.testTag("noProfilePic"))
                          }
                        }
                  }

              val textFieldColors =
                  TextFieldDefaults.colors(
                      focusedTextColor = Color.White,
                      unfocusedTextColor = Color.White,
                      disabledTextColor = Color.Gray,
                      errorTextColor = Color.Red,
                      cursorColor = Color.White,
                      errorCursorColor = Color.Red,
                      focusedIndicatorColor = Color.White,
                      unfocusedIndicatorColor = Color.Gray,
                      disabledIndicatorColor = Color.LightGray,
                      errorIndicatorColor = Color.Red,
                      focusedLabelColor = Color.White,
                      unfocusedLabelColor = Color.Gray,
                      disabledLabelColor = Color.LightGray,
                      errorLabelColor = Color.Red,
                      focusedPlaceholderColor = Color.White,
                      unfocusedPlaceholderColor = Color.Gray,
                      disabledPlaceholderColor = Color.LightGray,
                      errorPlaceholderColor = Color.Red,
                      focusedContainerColor = Color.Black,
                      unfocusedContainerColor = Color.Black,
                      disabledContainerColor = Color.DarkGray,
                      errorContainerColor = Color.Black)

              // First name input field
              OutlinedTextField(
                  value = firstName,
                  onValueChange = { firstName = it },
                  label = { Text("First Name") },
                  colors = textFieldColors,
                  modifier = Modifier.fillMaxWidth().testTag("firstNameField").padding(16.dp))

              // Last name input field
              OutlinedTextField(
                  value = lastName,
                  onValueChange = { lastName = it },
                  label = { Text("Last Name") },
                  colors = textFieldColors,
                  modifier = Modifier.fillMaxWidth().testTag("lastNameField").padding(16.dp))

              // Email input field (disabled)
              OutlinedTextField(
                  value = email,
                  onValueChange = {},
                  label = { Text("Email") },
                  enabled = false,
                  colors = textFieldColors,
                  modifier = Modifier.fillMaxWidth().testTag("emailField").padding(16.dp))

              // Phone number input field
              OutlinedTextField(
                  value = phoneNumber,
                  onValueChange = { phoneNumber = it },
                  label = { Text("Phone Number") },
                  modifier = Modifier.fillMaxWidth().testTag("phoneField").padding(16.dp),
                  visualTransformation = PhoneNumberVisualTransformation(),
                  placeholder = { Text("Use international prefix with +") },
                  colors = textFieldColors,
                  keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone))

              // Button to send verification code
              Button(
                  onClick = {
                    userViewModel.verifyUnusedPhoneNumber(
                        phoneNumber,
                        onSuccess = { isUnused ->
                          if (!isUnused) {
                            Toast.makeText(
                                    context, "Phone number already in use", Toast.LENGTH_SHORT)
                                .show()
                          } else {
                            userViewModel.sendVerificationCode(phoneNumber)
                            isCodeSent = true
                          }
                        },
                        onFailure = {
                          Toast.makeText(
                                  context, "Failed to verify phone number", Toast.LENGTH_SHORT)
                              .show()
                        })
                  },
                  modifier = Modifier.fillMaxWidth().testTag("sendCodeButton").padding(16.dp),
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = Purple40,
                          contentColor = Color.White,
                          disabledContainerColor = PurpleGrey40,
                          disabledContentColor = Color.LightGray),
                  enabled = phoneNumber.isNotBlank()) {
                    Text("Send Verification Code")
                  }

              // Verification code input field (visible if code is sent)
              if (isCodeSent) {
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = { verificationCode = it },
                    label = { Text("Verification Code") },
                    modifier = Modifier.fillMaxWidth().testTag("codeField").padding(16.dp),
                    placeholder = { Text("6-digit code received by SMS") },
                    colors = textFieldColors,
                    keyboardOptions =
                        KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number))

                // Button to verify the code
                Button(
                    onClick = { userViewModel.verifyCode(verificationCode) },
                    modifier = Modifier.fillMaxWidth().testTag("verifyCodeButton").padding(16.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Purple40,
                            contentColor = Color.White,
                            disabledContainerColor = PurpleGrey40,
                            disabledContentColor = Color.LightGray),
                    enabled = verificationCode.length == 6) {
                      Text("Verify Code")
                    }

                // Display verification status
                verificationStatus?.let {
                  Text(
                      text = it,
                      modifier = Modifier.fillMaxWidth().padding(16.dp),
                      color = if (it.contains("failed", true)) Color.Red else Color.Green)
                }
              }

              // Button to create the account
              Button(
                  onClick = {
                    val profileBitmap =
                        if (profilePictureUri != null) {
                              MediaStore.Images.Media.getBitmap(
                                  context.contentResolver, profilePictureUri)
                            } else {
                              val defaultUri =
                                  Uri.parse("android.resource://${context.packageName}/raw/profile")
                              MediaStore.Images.Media.getBitmap(context.contentResolver, defaultUri)
                            }
                            .asImageBitmap()
                    val newUid = userViewModel.getNewUid()
                    userViewModel.createUserAccount(
                        User(
                            uid = newUid,
                            firstName = firstName,
                            lastName = lastName,
                            emailAddress = email,
                            phoneNumber = phoneNumber,
                            profilePicture = profileBitmap,
                            lastKnownLocation = baseLocation),
                        onSuccess = {
                          userViewModel.saveUserLoginStatus(newUid)
                          userViewModel.scheduleWorker(newUid)
                          navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                        },
                        onFailure = {
                          Toast.makeText(context, "Account creation failed", Toast.LENGTH_SHORT)
                              .show()
                        })
                  },
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = Purple40,
                          contentColor = Color.White,
                          disabledContainerColor = PurpleGrey40,
                          disabledContentColor = Color.LightGray),
                  enabled =
                      firstName.isNotBlank() &&
                          lastName.isNotBlank() &&
                          email.isNotBlank() &&
                          phoneNumber.isNotBlank() &&
                          verificationStatus == "Phone Verified",
                  modifier =
                      Modifier.fillMaxWidth().testTag("createAccountButton").padding(16.dp)) {
                    Text("Create Account")
                  }
            }
      })
}
