import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.R
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.utils.PhoneNumberVisualTransformation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navigationActions: NavigationActions) {
  var firstName by remember { mutableStateOf("") }
  var lastName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var phoneNumber by remember { mutableStateOf("") }
  var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
  val coroutineScope = rememberCoroutineScope()

  val launcher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri?
        ->
        profilePictureUri = uri
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
              onValueChange = { email = it },
              label = { Text("Email") },
              modifier = Modifier.fillMaxWidth().testTag("emailField"),
          )
          Spacer(modifier = Modifier.height(16.dp))
          OutlinedTextField(
              value = phoneNumber,
              onValueChange = { phoneNumber = it },
              label = { Text("Phone Number") },
              modifier = Modifier.fillMaxWidth().testTag("phoneField"),
              visualTransformation = PhoneNumberVisualTransformation(),
              placeholder = { Text("Use international prefix with +") },
              keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone))
          Spacer(modifier = Modifier.height(16.dp))
          Button(
              onClick = { navigationActions.navigateTo(Screen.OVERVIEW) },
              modifier = Modifier.fillMaxWidth().testTag("createAccountButton")) {
                Text("Create Account")
              }
        }
      })
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  SignUpScreen(navigationActions)
}
