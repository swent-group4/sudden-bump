package com.swent.suddenbump.ui.messages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.R
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions

data class Message(
    val sender: String,
    val content: String,
    val date: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(navigationActions: NavigationActions) {
    val messages = listOf(
        Message("Gérard Barel", "Hi Marc! Thanks for your message, I'd love to go for a drink! Let's say 9pm?", "11/05/2024, 11:54"),
        Message("Clarisse Biquet", "Hi Marc! Thanks for your message, I'd love to go for a drink! Let's say 9pm?", "Yesterday"),
        Message("Valérie Blouse", "Hi Marc! Thanks for your message, I'd love to go for a drink! Let's say 9pm?", "Sunday"),
        Message("Marc, Julien", "Hi Marc! Thanks for your message, I'd love to go for a drink! Let's say 9pm?", "17/12/2022")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                navigationIcon = {
                    IconButton(
                        onClick = { /* Handle back action */ },
                        modifier = Modifier.testTag("back_button")  // Ajout du testTag pour le bouton de retour
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavigationMenu(
                onTabSelect = { route -> navigationActions.navigateTo(route) },
                tabList = LIST_TOP_LEVEL_DESTINATION,
                selectedItem = navigationActions.currentRoute()
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .background(Color.Black)
                .testTag("messages_list")
        ) {
            itemsIndexed(messages) { index, message ->
                MessageItem(message)
                if (index < messages.size - 1) {
                    // Ajouter un Divider seulement si ce n'est pas le dernier message
                    Divider(
                        color = Color.Gray,
                        thickness = 1.dp,
                        modifier = Modifier.testTag("divider")
                    )
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("message_item_${message.sender}")  // Ajout du testTag pour chaque message
    ) {
        Image(
            painter = painterResource(R.drawable.profile), // Utilise une icône de ressource pour l'avatar
            contentDescription = "Profile Avatar",
            modifier = Modifier
                .size(40.dp)
                .padding(start = 8.dp)
        )
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = message.sender,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "You: ${message.content}",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.testTag("message_content")
            )
            Text(
                text = message.date,
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMessagesScreen() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    MessagesScreen(navigationActions)
}
