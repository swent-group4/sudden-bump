package com.swent.suddenbump

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

// import coil3.compose.AsyncImage

data class MockUser(
    val uid: String,
    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val profilePictureUrl: String,
    val phoneNumber: String,
    val email: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileScreen() {
  val user =
      MockUser(
          uid = "123",
          firstName = "John",
          lastName = "Doe",
          profilePictureUrl = "https://i.pravatar.cc/300",
          phoneNumber = "+3345676543",
          email = "john.doe@gmail.com",
          birthDate = "28 February 1998")

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
              IconButton(
                  onClick = { println("Go Back !") },
              ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }

              Text(text = user.firstName + " " + user.lastName)

              Spacer(modifier = Modifier.width(50.dp))
            }
      },
      content = { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Top,
          ) {
            Image(
                painter = painterResource(id = R.drawable.profile), // Ensure this drawable exists
                contentDescription = "App Logo",
                modifier = Modifier.size(150.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 50.dp, vertical = 10.dp)) {
              Text(modifier = Modifier.padding(10.dp), text = "Birthday: " + user.birthDate)
            }

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 50.dp, vertical = 10.dp)) {
              Text(modifier = Modifier.padding(10.dp), text = "Phone: " + user.phoneNumber)
            }

            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 50.dp, vertical = 10.dp)) {
              Text(modifier = Modifier.padding(10.dp), text = "Email: " + user.email)
            }
          }

          Button(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 50.dp, vertical = 30.dp),
              onClick = { println("Button click !") }) {
                Text("Send a message")
              }
        }
      })
}
