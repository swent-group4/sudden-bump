package com.swent.suddenbump.ui.profile

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
import coil3.compose.AsyncImage
import com.swent.suddenbump.R
import com.swent.suddenbump.ui.navigation.NavigationActions

//import coil3.compose.AsyncImage

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
fun ContactScreen(navigationActions: NavigationActions) {
    val user = MockUser(
        uid = "123",
        firstName= "Jane",
        lastName= "Smith",
        profilePictureUrl = "https://api.dicebear.com/9.x/lorelei/png?seed=JaneSmith",
        phoneNumber = "+3345676543",
        email = "jane.smith@gmail.com",
        birthDate = "28 February 1998"
    )

    Scaffold (
        modifier=Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Contact") },
                navigationIcon = {
                    IconButton(
                        onClick = { navigationActions.goBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back")
                    }
                })
        },
        content={
                padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            )
            {
                Column (horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,) {
                    AsyncImage(
                        model = user.profilePictureUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .width(150.dp)
                            .height(150.dp)
                            .padding(8.dp)
                    )
                    Column(
                        Modifier.fillMaxWidth().padding(top = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            user.firstName + " " + user.lastName,
                            style = androidx.compose.ui.text.TextStyle(
                                fontSize = 20.sp, // Adjust the size as needed
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 50.dp, vertical = 10.dp)
                    ) {
                        Text(modifier = Modifier.padding(10.dp),text = "Birthday: " + user.birthDate)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 50.dp, vertical = 10.dp)
                    ) {
                        Text(modifier = Modifier.padding(10.dp),text = "Phone: " + user.phoneNumber)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 50.dp, vertical = 10.dp)
                    ) {
                        Text(modifier = Modifier.padding(10.dp),text = "Email: " + user.email)
                    }
                }

                Button(modifier = Modifier.fillMaxWidth().padding(horizontal = 50.dp, vertical = 30.dp), onClick = { println("Button click !") }) {
                    Text("Send a message")
                }

            }
        }
    )
}