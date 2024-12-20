package com.swent.suddenbump.ui.messages

import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.swent.suddenbump.model.chat.convertFirstParticipantToUser
import com.swent.suddenbump.model.chat.convertLastSenderUidToDisplay
import com.swent.suddenbump.model.chat.convertParticipantsUidToDisplay
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.theme.Purple80
import com.swent.suddenbump.ui.utils.UserProfileImage
import kotlinx.coroutines.flow.MutableStateFlow

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
        CenterAlignedTopAppBar(
            title = {
              Text("Messages", color = Color.White, modifier = Modifier.testTag("Messages Title"))
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
  val unknownUser =
      User(
          uid = "unknown",
          firstName = "Unknown",
          lastName = "User",
          phoneNumber = "+33 0 00 00 00 00",
          null,
          "mail@mail.com",
          MutableStateFlow(Location("")))
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .padding(vertical = 8.dp)
              .clickable {
                viewModel.user =
                    convertFirstParticipantToUser(
                        message, viewModel.getUserFriends().value, unknownUser)
                navigationActions.navigateTo(Screen.CHAT)
              }
              .testTag("message_item_${message.sender}")) {
        convertFirstParticipantToUser(
                message, viewModel.getUserFriends().collectAsState().value, unknownUser)
            .let { UserProfileImage(user = it, size = 40) }

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
                            viewModel.getUserFriends().collectAsState().value,
                            unknownUser),
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
                    text =
                        convertLastSenderUidToDisplay(
                            message,
                            viewModel.getCurrentUser().collectAsState().value,
                            viewModel.getUserFriends().collectAsState().value,
                            unknownUser) + " : ${message.content}",
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
