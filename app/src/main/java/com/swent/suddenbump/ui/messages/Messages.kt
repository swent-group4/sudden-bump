package com.swent.suddenbump.ui.messages

import android.util.Log
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.swent.suddenbump.R
import com.swent.suddenbump.model.chat.ChatSummary
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.BottomNavigationMenu
import com.swent.suddenbump.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.navigation.Screen
import com.swent.suddenbump.ui.theme.violetColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    viewModel: UserViewModel = viewModel(factory = UserViewModel.Factory),
    navigationActions: NavigationActions
) {
    val messages by viewModel.getChatSummaries().collectAsState(emptyList())
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    messages.forEach { chatSummary ->
        chatSummary.participants
            .firstOrNull { it != currentUser }
            ?.let {
                viewModel.getUserAccount(it, onSuccess = { user ->
                    chatSummary.otherUserName =
                        "${user?.firstName?.replaceFirstChar { it.uppercase() }} ${user?.lastName?.replaceFirstChar { it.uppercase() }}"

                }, onFailure = {
                    Log.e("MessageItem", "Error getting user account: ${it.message}")
                })
            }
    }
    var search by remember {
        mutableStateOf("")
    }
    var list by remember {
        mutableStateOf<List<ChatSummary>>(messages)
    }
    list = messages.filter { it.date != "" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    BasicTextField(
                        value = search,
                        onValueChange = {
                            search = it
                            if (search.isEmpty())
                                list = messages.filter { it.date != "" }
                            else {
                                list = messages.filter {
                                    it.otherUserName.contains(search) || it.otherUserName.startsWith(
                                        search
                                    ) || it.lastMessage.contains(search) || it.lastMessage.startsWith(
                                        search
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, shape = MaterialTheme.shapes.small)
                            .padding(12.dp),
                        textStyle = LocalTextStyle.current.copy(
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    )
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
        }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
        ) {
            LazyColumn(
                modifier = Modifier
                    .background(Color.Black)
                    .testTag("messages_list")
            ) {
                itemsIndexed(list) { index, message ->
                    MessageItem(message, viewModel, navigationActions)
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
}

@Composable
fun MessageItem(
    message: ChatSummary,
    viewModel: UserViewModel,
    navigationActions: NavigationActions
) {
    val currentUser = viewModel.getCurrentUser()
    var name by remember {
        mutableStateOf(message.sender)
    }
    message.participants
        .firstOrNull { it != currentUser.value.uid }
        ?.let {
            viewModel.getUserAccount(it, onSuccess = { user ->
                name =
                    "${user?.firstName?.replaceFirstChar { it.uppercase() }} ${user?.lastName?.replaceFirstChar { it.uppercase() }}"
            }, onFailure = {
                Log.e("MessageItem", "Error getting user account: ${it.message}")
            })
        }
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("message_item_${message.sender}")
            .clickable {
                message.participants
                    .firstOrNull { it != currentUser.value.uid }
                    ?.let {
                        viewModel.getUserAccount(it, onSuccess = {
                            viewModel.user = it
                            navigationActions.navigateTo(Screen.CHAT)
                        }, onFailure = {
                            Log.e("MessageItem", "Error getting user account: ${it.message}")
                        })
                    }
            }
    ) {
        Image(
            painter = painterResource(R.drawable.profile),
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = message.date,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 5.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "You: ${message.content}",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.testTag("message_content")
                )
                if (message.unreadCount > 0) {

                    Card(
                        shape = RoundedCornerShape(100),
                        colors = CardDefaults.cardColors(
                            containerColor = violetColor
                        ),
                        modifier = Modifier.padding(5.dp)
                    ) {
                        Text(
                            text = "${message.unreadCount}", // Display unread count
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                        )

                    }
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMessagesScreen() {
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)
    val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
    MessagesScreen(userViewModel, navigationActions)
}
