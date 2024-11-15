package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Purple40
import com.swent.suddenbump.ui.theme.Purple80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscussionScreen(navigationActions: NavigationActions) {
    val sections = listOf(
        SectionData("Chat wallpaper", "Change chat wallpaper", "chatWallpaperText", "changeChatWallpaperButton"),
        SectionData("Export chat", "Export chat", "exportChatText", "exportChatButton"),
        SectionData("Archive all chats", "Archive all chats", "archiveAllChatsText", "archiveAllChatsButton"),
        SectionData("Clear all chats", "Clear all chats", "clearAllChatsText", "clearAllChatsButton"),
        SectionData("Delete all chats", "Delete all chats", "deleteAllChatsText", "deleteAllChatsButton")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats", color = Color.White) },
                navigationIcon = {
                    IconButton(
                        onClick = { navigationActions.goBack() },
                        modifier = Modifier.testTag("backButton")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple40)
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                sections.forEach { section ->
                    SectionWithButton(
                        label = section.label,
                        buttonText = section.buttonText,
                        onClick = { /* Add specific logic */ },
                        labelTag = section.labelTag,
                        buttonTag = section.buttonTag
                    )
                    Divider()
                }
            }
        }
    )
}

data class SectionData(
    val label: String,
    val buttonText: String,
    val labelTag: String,
    val buttonTag: String
)

@Composable
fun SectionWithButton(
    label: String,
    buttonText: String,
    onClick: () -> Unit,
    labelTag: String,
    buttonTag: String
) {
    Text(text = label, color = Color.White, modifier = Modifier.testTag(labelTag))
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Purple80),
        modifier = Modifier.testTag(buttonTag)
    ) {
        Text(buttonText, color = Color.White)
    }
}

