package com.swent.suddenbump.ui.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.swent.suddenbump.model.ListItem
import com.swent.suddenbump.model.chat.generateListItems
import com.swent.suddenbump.model.chat.toOnlyTimeFormat
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.purple
import com.swent.suddenbump.ui.theme.violetColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: UserViewModel, navigationActions: NavigationActions) {
  // Observe messages from the ViewModel
  viewModel.getOrCreateChat()
  val messages by viewModel.messages.collectAsState(emptyList())
  val user by viewModel.getCurrentUser().collectAsState()
  val otherUser = viewModel.user
  if (messages.isNotEmpty()) viewModel.markMessagesAsRead()
  var list by remember { mutableStateOf<List<ListItem>>(emptyList()) }
  list = generateListItems(messages.reversed()).reversed()
  Column(
      modifier = Modifier.fillMaxSize().background(Color.Black),
  ) {
    Scaffold(
        topBar = {
          Column(modifier = Modifier.fillMaxWidth()) {
            TopAppBar(
                title = {
                  Text(
                      "${otherUser?.firstName?.replaceFirstChar { it.uppercase() }} ${otherUser?.lastName?.replaceFirstChar { it.uppercase() }}",
                      color = Color.White)
                },
                navigationIcon = {
                  IconButton(onClick = { navigationActions.goBack() }) {
                    Icon(
                        imageVector =
                            Icons.Default.ArrowBack, // Back icon (replace with your drawable)
                        contentDescription = "Back",
                        tint = Color.White)
                  }
                },
                colors = TopAppBarDefaults.topAppBarColors(Color.Black),
                modifier = Modifier.fillMaxWidth())
            Divider()
          }
        },
        bottomBar = {
          // Input Box at the Bottom
          ChatInputBox(viewModel, otherUser)
        },
    ) { padding ->
      Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black)) {
        // Chat Section
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = true // This makes the newest messages appear at the bottom
            ) {
              items(list.size) { index ->
                when (list[index]) {
                  is ListItem.DateView -> {
                    val dateItem = list[index] as ListItem.DateView
                    val date = dateItem.date
                    // Now you can use the 'date' variable as needed
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                          Card(
                              colors =
                                  CardDefaults.cardColors(
                                      containerColor = Color.White,
                                  ),
                              shape = RoundedCornerShape(50),
                              border = BorderStroke(1.dp, Color.White)) {
                                Text(
                                    text = date,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = purple,
                                    modifier =
                                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    textAlign = TextAlign.Center)
                              }
                        }
                  }
                  is ListItem.Messages -> {
                    val messageItem = list[index] as ListItem.Messages
                    MessageBubble(messageItem)
                  }
                }
              }
            }
      }
    }
  }
}

@Composable
fun MessageBubble(data: ListItem.Messages) {
  val message = data.message
  val screenWidth = LocalConfiguration.current.screenWidthDp.dp
  val bubbleMaxWidth = screenWidth * 0.8f

  Column(
      modifier = Modifier.fillMaxWidth().padding(8.dp),
      horizontalAlignment = if (message.isOwner) Alignment.End else Alignment.Start) {
        // Timestamp
        Text(
            text = message.timestamp.toDate().toOnlyTimeFormat(),
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 4.dp))

        // Show the message content
        Card(
            modifier = Modifier.widthIn(max = bubbleMaxWidth),
            colors =
                CardDefaults.cardColors(
                    containerColor = if (message.isOwner) violetColor else Color.White,
                ),
            shape = RoundedCornerShape(15),
            border = BorderStroke(1.dp, Color.White)) {
              Text(
                  text = message.content,
                  color = if (message.isOwner) Color.White else Color.Black,
                  fontSize = 16.sp,
                  modifier = Modifier.padding(10.dp))
            }
      }
}

@Composable
fun ChatInputBox(viewModel: UserViewModel, otherUser: User?) {
  var inputText by remember { mutableStateOf(TextFieldValue()) }

  Row(
      modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(8.dp),
      verticalAlignment = Alignment.CenterVertically) {
        // Input Field
        BasicTextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier =
                Modifier.weight(1f)
                    .background(Color.Black, shape = MaterialTheme.shapes.small)
                    .padding(12.dp),
            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 16.sp))

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = {
              if (inputText.text.isNotEmpty()) {
                val name = otherUser?.firstName + " " + otherUser?.lastName
                viewModel.sendMessage(inputText.text, name) // Send message through ViewModel
                inputText = TextFieldValue() // Clear input field after sending
              }
            }) {
              Icon(
                  imageVector = Icons.Default.Send,
                  contentDescription = "Send",
                  tint = Color.White,
                  modifier = Modifier.size(28.dp))
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
