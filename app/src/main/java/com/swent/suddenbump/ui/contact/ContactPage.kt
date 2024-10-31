package com.swent.suddenbump.ui.contact

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ContactScreen(
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory),
    navigationActions: NavigationActions
) {
  val user = userViewModel.user ?: return

  Scaffold(
      modifier = Modifier.fillMaxSize().testTag("contactScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Contact") },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("backButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      },
      content = { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("contactContent"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Top,
          ) {
            AsyncImage(
                model = user.profilePictureUrl,
                contentDescription = null,
                placeholder =
                    painterResource(
                        com.swent.suddenbump.R.drawable.profile), // Add your drawable here
                error = painterResource(com.swent.suddenbump.R.drawable.profile), //
                modifier =
                    Modifier.width(150.dp).height(150.dp).padding(8.dp).testTag("profileImage"))
            Column(
                Modifier.fillMaxWidth().padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Text(
                  user.firstName + " " + user.lastName,
                  style =
                      androidx.compose.ui.text.TextStyle(
                          fontSize = 20.sp, // Adjust the size as needed
                          fontWeight = FontWeight.Bold),
                  modifier = Modifier.testTag("userName"))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 50.dp, vertical = 10.dp)
                        .testTag("birthdayCard")) {
                  Text(
                      modifier = Modifier.padding(10.dp).testTag("birthdayText"),
                      text = "Birthday: " + user.birthDate)
                }

            Card(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 50.dp, vertical = 10.dp)
                        .testTag("phoneCard")) {
                  Text(modifier = Modifier.padding(10.dp), text = "Phone: " + user.phoneNumber)
                }

            Card(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 50.dp, vertical = 10.dp)
                        .testTag("emailCard")) {
                  Text(modifier = Modifier.padding(10.dp), text = "Email: " + user.emailAddress)
                }
          }

          if (user.isFriend) {
            Button(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 50.dp, vertical = 30.dp)
                        .testTag("sendMessageButton"),
                onClick = {
                  userViewModel.user = user
                  navigationActions.navigateTo(Screen.CHAT)
                }) {
                  Text("Send a message")
                }
          } else {
            Button(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 50.dp, vertical = 30.dp)
                        .testTag("addToContactsButton"),
                onClick = { println("Button click !") }) {
                  Text("Add to contacts")
                }
          }
        }
      })
}
