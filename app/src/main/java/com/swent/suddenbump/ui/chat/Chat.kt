package com.swent.suddenbump.ui.chat

import android.annotation.SuppressLint
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
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.suddenbump.model.chat.ListItem
import com.swent.suddenbump.model.chat.generateListItems
import com.swent.suddenbump.model.chat.toOnlyTimeFormat
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.theme.purple
import com.swent.suddenbump.ui.utils.UserProfileImage
import com.swent.suddenbump.ui.utils.defaultUserOnlineValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: UserViewModel, navigationActions: NavigationActions) {
  // Observe messages from the ViewModel
  viewModel.user?.let { viewModel.getOrCreateChat(it.uid) }

  val messages by viewModel.messages.collectAsState(emptyList())
  val user by viewModel.getCurrentUser().collectAsState()
  val otherUser = viewModel.user
  val friendIsOnline = remember { mutableStateOf(defaultUserOnlineValue) }
  LaunchedEffect(Unit) {
    CoroutineScope(Dispatchers.IO).launch {
      if (otherUser != null) {
        // Fetch the status of the other user and update the friendIsOnline state
        viewModel.getUserStatus(
            otherUser.uid, onSuccess = { friendIsOnline.value = it }, onFailure = {})
      }
    }
  }
  // if (messages.isNotEmpty())
  //    viewModel.markMessagesAsRead()
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
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    if (otherUser != null) {
                      UserProfileImage(otherUser, 40)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${otherUser?.firstName?.replaceFirstChar { it.uppercase() }} ${otherUser?.lastName?.replaceFirstChar { it.uppercase() }}",
                        color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (friendIsOnline.value) "Online" else "Offline",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp))
                  }
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
          ChatInputBox(viewModel, otherUser, navigationActions)
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
                              shape = RoundedCornerShape(30),
                              border = BorderStroke(1.dp, Color.White)) {
                                Text(
                                    text = date,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = purple,
                                    modifier =
                                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    textAlign = TextAlign.Center)
                              }
                        }
                  }
                  is ListItem.Messages -> {
                    val messageItem = list[index] as ListItem.Messages
                    MessageBubble(messageItem, user)
                  }
                }
              }
            }
      }
    }
  }
}

@Composable
fun MessageBubble(data: ListItem.Messages, user: User) {
  val message = data.message
  val screenWidth = LocalConfiguration.current.screenWidthDp.dp
  val bubbleMaxWidth = screenWidth * 0.8f

  Column(
      modifier = Modifier.fillMaxWidth().padding(8.dp),
      horizontalAlignment = if (message.senderId == user.uid) Alignment.End else Alignment.Start) {
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
                    containerColor = if (message.senderId == user.uid) purple else Color.White,
                ),
            shape =
                RoundedCornerShape(
                    topStart = 48f,
                    topEnd = 48f,
                    bottomStart = if (message.senderId == user.uid) 48f else 0f,
                    bottomEnd = if (message.senderId == user.uid) 0f else 48f),
            border = BorderStroke(1.dp, Color.White)) {
              Text(
                  text = message.content,
                  color = if (message.senderId == user.uid) Color.White else Color.Black,
                  fontSize = 16.sp,
                  modifier = Modifier.padding(10.dp))
            }
      }
}

@Composable
fun ChatInputBox(viewModel: UserViewModel, otherUser: User?, navigationActions: NavigationActions) {
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
                    .padding(12.dp)
                    .testTag("ChatInputTextBox"),
            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 16.sp))

        Spacer(modifier = Modifier.width(8.dp))

        // Add Meeting Icon
        IconButton(onClick = { navigationActions.navigateTo(Screen.ADD_MEETING) }) {
          Icon(
              imageVector = Icons.Default.DateRange,
              contentDescription = "Add Meeting",
              tint = Color.White,
              modifier = Modifier.size(28.dp))
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Send Message Icon
        IconButton(
            onClick = {
              if (inputText.text.isNotEmpty()) {
                val name = otherUser?.firstName + " " + otherUser?.lastName
                if (otherUser != null) {
                  viewModel.sendMessage(inputText.text, user = otherUser)
                } // Send message through ViewModel
                inputText = TextFieldValue() // Clear input field after sending
              }
            },
            modifier = Modifier.testTag("SendButton")) {
              Icon(
                  imageVector = Icons.Default.Send,
                  contentDescription = "Send",
                  tint = Color.White,
                  modifier = Modifier.size(28.dp))
            }
      }
}
