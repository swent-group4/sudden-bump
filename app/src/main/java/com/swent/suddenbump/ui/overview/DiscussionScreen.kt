@file:OptIn(ExperimentalMaterial3Api::class)

package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Pink40
import com.swent.suddenbump.ui.utils.CustomTopBar

data class SectionData(
    val label: String,
    val buttonText: String,
    val labelTag: String,
    val buttonTag: String
)

@Composable
fun DiscussionScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  // State variable to track whether the delete confirmation dialog should be shown
  var showDialog by remember { mutableStateOf(false) }

  // Display the confirmation dialog when the user clicks the "Delete all chats" button
  if (showDialog) {
    ConfirmDeleteDialog(
        onConfirm = {
          // When confirmed, delete all messages and close the dialog
          userViewModel.deleteAllMessages()
          showDialog = false
        },
        onDismiss = {
          // Close the dialog without taking any action
          showDialog = false
        })
  }

  // Scaffold to define the screen layout
  Scaffold(
      modifier = Modifier.testTag("discussionScreen").background(Color.Black),
      topBar = { OverviewScreenTopBar("Discussions", navigationActions = navigationActions) },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize() // Fill the entire screen
                    .background(Color.Black) // Black background for the screen
                    .padding(paddingValues) // Handle padding for content under the top bar
                    .padding(16.dp), // Additional padding for spacing
            verticalArrangement = Arrangement.spacedBy(16.dp), // Space between items in the column
            horizontalAlignment =
                Alignment.CenterHorizontally // Center align all content horizontally
            ) {
              // Button to delete all chats
              Button(
                  onClick = { showDialog = true }, // Show the confirmation dialog when clicked
                  colors =
                      ButtonDefaults.buttonColors(
                          containerColor = Pink40, // Background color of the button
                          contentColor = Color.White // Text color of the button
                          ),
                  shape =
                      RoundedCornerShape(8.dp), // Match the less-rounded style of "Delete Account"
                  modifier =
                      Modifier.fillMaxWidth() // Make the button span the full width of the column
                          .testTag("deleteAllChatsButton") // Tag for testing this button
                          .padding(horizontal = 16.dp) // Padding to keep space on the sides
                  ) {
                    // Text displayed on the button
                    Text(
                        text = "Delete all chats",
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold // Bold font for emphasis
                                ))
                  }
            }
      })
}

/**
 * Confirmation dialog to ask the user for confirmation before deleting all messages.
 *
 * @param onConfirm Action to perform when the user confirms the deletion.
 * @param onDismiss Action to perform when the user dismisses the dialog.
 */
@Composable
fun ConfirmDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
      onDismissRequest = onDismiss, // Triggered when the user clicks outside the dialog
      title = {
        Text("Confirm Deletion") // Title of the dialog
      },
      text = {
        Text("Are you sure you want to delete all messages? This action cannot be undone.")
      },
      confirmButton = {
        TextButton(
            onClick = onConfirm, // Trigger the confirm action
            modifier = Modifier.testTag("confirmButton") // Tag for testing the confirm button
            ) {
              Text("Confirm") // Label for the confirm button
        }
      },
      dismissButton = {
        TextButton(
            onClick = onDismiss, // Trigger the dismiss action
            modifier = Modifier.testTag("cancelButton") // Tag for testing the cancel button
            ) {
              Text("Cancel") // Label for the cancel button
        }
      })
}

/** Helper function to create the top bar for DiscussionScreen */
@Composable
fun createTopBar(navigationActions: NavigationActions) {
  CustomTopBar(
      title = "Chats",
      navigationActions = navigationActions,
      titleTag = "discussionTitle",
      backButtonTag = "backButton")
}

/** Create the list of sections for the DiscussionScreen */
fun createDiscussionSections(): List<SectionData> =
    listOf(
        createSectionData(
            "Delete all chats", "Delete all chats", "deleteAllChatsText", "deleteAllChatsButton"))

/** Helper function to create a SectionData instance */
fun createSectionData(label: String, buttonText: String, labelTag: String, buttonTag: String) =
    SectionData(label = label, buttonText = buttonText, labelTag = labelTag, buttonTag = buttonTag)

/** Extension function to add items with dividers in LazyColumn. */
inline fun <T> LazyListScope.itemsWithDividers(
    items: List<T>,
    crossinline content: @Composable (T) -> Unit
) {
  items.forEachIndexed { index, item ->
    item {
      content(item)

      if (index != items.lastIndex) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp).testTag("divider_$index"),
            color = Color.White)
      }
    }
  }
}
