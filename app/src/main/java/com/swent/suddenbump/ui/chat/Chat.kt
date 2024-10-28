package com.swent.suddenbump.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.model.chat.Message
import com.swent.suddenbump.model.chat.toCustomFormat
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.messages.MessageItem
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.violetColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: UserViewModel, navigationActions: NavigationActions) {
    // Observe messages from the ViewModel
    viewModel.getOrCreateChat()
    val messages by viewModel.messages.collectAsState(emptyList())
    val user by viewModel.getCurrentUser().collectAsState()
    val otherUser =  viewModel.user
    if (messages.isNotEmpty())
        viewModel.markMessagesAsRead()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "${otherUser?.firstName?.replaceFirstChar { it.uppercase() }} ${otherUser?.lastName?.replaceFirstChar { it.uppercase() }}",
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigationActions.goBack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack, // Back icon (replace with your drawable)
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(Color.Black),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            bottomBar = {
                // Input Box at the Bottom
                ChatInputBox(viewModel, otherUser)
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color.Black)
            ) {
                // Chat Section
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = true // This makes the newest messages appear at the bottom
                ) {
                    items(messages) { message ->
                        MessageBubble(message)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val bubbleMaxWidth = screenWidth * 0.8f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(8.dp),
        horizontalAlignment = if (message.isOwner) Alignment.End else Alignment.Start
    ) {
        // Show the message content
        Box(
            modifier = Modifier
                .widthIn(max = bubbleMaxWidth)
                .background(
                    color = if (message.isOwner) violetColor else Color.White,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(10.dp)
        ) {
            Text(
                text = message.content,
                color = if (message.isOwner)  Color.White else Color.Black,
                fontSize = 16.sp
            )
        }

        // Timestamp
        Text(
            text = message.timestamp.toDate().toCustomFormat(),
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ChatInputBox(viewModel: UserViewModel, otherUser: User?) {
    var inputText by remember { mutableStateOf(TextFieldValue()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .padding(8.dp)
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Input Field
        BasicTextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier
                .weight(1f)
                .background(Color.Black, shape = MaterialTheme.shapes.small)
                .padding(12.dp),
            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 16.sp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = {
            if (inputText.text.isNotEmpty()) {
                val name = otherUser?.firstName +" "+ otherUser?.lastName
                viewModel.sendMessage(inputText.text,name)  // Send message through ViewModel
                inputText = TextFieldValue()           // Clear input field after sending
            }
        }) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChatScreen() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
    ChatScreen(userViewModel, navigationActions)
}
