package com.swent.suddenbump.ui.authentication

import android.app.Activity
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.swent.suddenbump.ui.utils.PhoneNumberVisualTransformation
import com.yalantis.ucrop.UCrop
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  var firstName by remember { mutableStateOf("") }
  var lastName by remember { mutableStateOf("") }
  val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
  var phoneNumber by remember { mutableStateOf("") }
  var verificationCode by remember { mutableStateOf("") }
  var isCodeSent by remember { mutableStateOf(false) }
  var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current
  val verificationStatus by userViewModel.verificationStatus.observeAsState()

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

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(text = "Sign Up") },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            })
      },
      content = { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
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

          OutlinedTextField(
              value = firstName,
              onValueChange = { firstName = it },
              label = { Text("First Name") },
              modifier = Modifier.fillMaxWidth().testTag("firstNameField"))
          Spacer(modifier = Modifier.height(16.dp))
          OutlinedTextField(
              value = lastName,
              onValueChange = { lastName = it },
              label = { Text("Last Name") },
              modifier = Modifier.fillMaxWidth().testTag("lastNameField"))
          Spacer(modifier = Modifier.height(16.dp))
          OutlinedTextField(
              value = email,
              onValueChange = {},
              label = { Text("Email") },
              enabled = false,
              modifier = Modifier.fillMaxWidth().testTag("emailField"),
          )
          Spacer(modifier = Modifier.height(16.dp))

          // Phone number input
          OutlinedTextField(
              value = phoneNumber,
              onValueChange = { phoneNumber = it },
              label = { Text("Phone Number") },
              modifier = Modifier.fillMaxWidth().testTag("phoneField"),
              visualTransformation = PhoneNumberVisualTransformation(),
              placeholder = { Text("Use international prefix with +") },
              keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone))

          // Button to send verification code
          Button(
              onClick = {
                userViewModel.sendVerificationCode(phoneNumber)
                isCodeSent = true
              },
              modifier = Modifier.fillMaxWidth().testTag("sendCodeButton"),
              enabled = phoneNumber.isNotBlank()) {
                Text("Send Verification Code")
              }

          // Verification code input if code has been sent
          if (isCodeSent) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it },
                label = { Text("Verification Code") },
                modifier = Modifier.fillMaxWidth().testTag("codeField"),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number))

            // Button to verify code
            Button(
                onClick = { userViewModel.verifyCode(verificationCode) },
                modifier = Modifier.fillMaxWidth().testTag("verifyCodeButton"),
                enabled = verificationCode.isNotBlank()) {
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

          Spacer(modifier = Modifier.height(16.dp))

          // Create account button
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

                userViewModel.createUserAccount(
                    User(
                        uid = userViewModel.getNewUid(),
                        firstName = firstName,
                        lastName = lastName,
                        emailAddress = email,
                        phoneNumber = phoneNumber,
                        profilePicture = profileBitmap),
                    onSuccess = { navigationActions.navigateTo(TopLevelDestinations.OVERVIEW) },
                    onFailure = {
                      Toast.makeText(context, "Account creation failed", Toast.LENGTH_SHORT).show()
                    })
              },
              enabled =
                  firstName.isNotBlank() &&
                      lastName.isNotBlank() &&
                      email.isNotBlank() &&
                      phoneNumber.isNotBlank() &&
                      verificationStatus == "Phone Verified",
              modifier = Modifier.fillMaxWidth().testTag("createAccountButton")) {
                Text("Create Account")
              }
        }
      })
}

/*@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
  SignUpScreen(navigationActions, userViewModel)
}*/
