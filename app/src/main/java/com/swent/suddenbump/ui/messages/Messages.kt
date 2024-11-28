package com.swent.suddenbump.ui.messages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.suddenbump.model.chat.ChatSummary
import com.swent.suddenbump.model.chat.convertParticipantsUidToDisplay
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.theme.Purple80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(viewModel: UserViewModel, navigationActions: NavigationActions) {
  viewModel.getChatSummaries()
  var search by remember { mutableStateOf("") }

  val messages = viewModel.chatSummaries.collectAsState()
  val list =
      messages.value.filter { summary ->
        summary.date != "" && summary.sender.contains(search, true)
      }

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              BasicTextField(
                  value = search,
                  onValueChange = { search = it },
                  modifier =
                      Modifier.fillMaxWidth()
                          .background(Color.White, shape = MaterialTheme.shapes.small)
                          .padding(12.dp),
                  textStyle =
                      LocalTextStyle.current.copy(
                          color = Color.Black,
                          fontSize = 16.sp,
                      ),
                  singleLine = true)
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier =
                      Modifier.testTag("back_button") // Ajout du testTag pour le bouton de retour
                  ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                  }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White))
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.Black)) {
          LazyColumn(modifier = Modifier.background(Color.Black).testTag("messages_list")) {
            itemsIndexed(list) { index, message ->
              MessageItem(message, viewModel, navigationActions)
              if (index < messages.value.size - 1) {
                // Ajouter un Divider seulement si ce n'est pas le dernier message
                Divider(
                    color = Color.Gray, thickness = 1.dp, modifier = Modifier.testTag("divider"))
              }
            }
          }
        }
      }
}

@Composable
fun MessageItem(
    message: ChatSummary,
    viewModel: UserViewModel,
    navigationActions: NavigationActions
) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 8.dp)
              .clickable {
                viewModel.user =
                    viewModel.getUserFriends().value.first {
                      it.uid ==
                          message.participants
                              .filterNot { it2 -> it2 == viewModel.getCurrentUser().value.uid }
                              .first()
                    }
                navigationActions.navigateTo(Screen.CHAT)
              }
              .testTag("message_item_${message.sender}")) {
        viewModel
            .getUserFriends()
            .collectAsState()
            .value
            .first {
              it.uid ==
                  message.participants
                      .filterNot { it2 -> it2 == viewModel.getCurrentUser().value.uid }
                      .first()
            }
            .profilePicture
            ?.let {
              Image(
                  bitmap = it,
                  contentDescription = "Profile Avatar",
                  modifier =
                      Modifier.size(40.dp)
                          .padding(start = 8.dp)
                          .testTag("profile_picture_${message.sender}"))
            }

        Column(modifier = Modifier.padding(start = 16.dp).fillMaxWidth()) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()) {
                Text(
                    text =
                        convertParticipantsUidToDisplay(
                            message,
                            viewModel.getCurrentUser().collectAsState().value,
                            viewModel.getUserFriends().collectAsState().value),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp)
                Text(
                    text = message.date,
                    color = Purple80,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 5.dp))
              }
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "You: ${message.content}",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // Notification Badge
                /*if (message.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp) // Adjust position to align with the message content
                            .size(24.dp) // Badge size
                    ) {
                        Card(
                            shape = RoundedCornerShape(100),
                            colors = CardDefaults.cardColors(
                                containerColor = Purple40
                            ),
                            modifier = Modifier
                                .align(Alignment.Center)
                        ) {
                            Text(
                                text = "${message.unreadCount}", // Display unread count
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }context
                    }
                }*/
              }
        }
      }
}
