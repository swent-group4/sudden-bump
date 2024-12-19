package com.swent.suddenbump.ui.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.suddenbump.R
import com.swent.suddenbump.model.meeting_location.Location
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Gray
import com.swent.suddenbump.ui.theme.Pink40
import com.swent.suddenbump.ui.theme.Purple40

/**
 * A reusable composable component for a labeled section with a button.
 *
 * @param label The text label for the section.
 * @param buttonText The text for the button.
 * @param onClick The callback for when the button is clicked.
 * @param labelTag The test tag for the label.
 * @param buttonTag The test tag for the button.
 */
@Composable
fun LabeledButtonSection(
    label: String,
    buttonText: String,
    onClick: () -> Unit,
    labelTag: String,
    buttonTag: String
) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .background(Color.White, RoundedCornerShape(8.dp))
              .padding(16.dp)) {
        Text(
            text = label,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).testTag(labelTag))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Purple40),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag(buttonTag)) {
              Text(
                  buttonText,
                  style =
                      MaterialTheme.typography.bodyLarge.copy(
                          fontWeight = FontWeight.Bold, color = Color.White))
            }
      }
}

/**
 * A reusable composable component for a top bar with a title and a back button.
 *
 * @param title The title of the top bar.
 * @param navigationActions The navigation actions for the app.
 * @param titleTag The test tag for the title.
 * @param backButtonTag The test tag for the back button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopBar(
    title: String,
    navigationActions: NavigationActions,
    titleTag: String,
    backButtonTag: String
) {
  TopAppBar(
      title = {
        Text(
            title,
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White),
            modifier = Modifier.testTag(titleTag))
      },
      navigationIcon = {
        IconButton(
            onClick = { navigationActions.goBack() }, modifier = Modifier.testTag(backButtonTag)) {
              Icon(
                  imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                  contentDescription = "Back",
                  tint = Color.White)
            }
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple40))
}

/**
 * A composable function that displays a center aligned top app bar for some screen.
 *
 * @param title The title to be displayed in the top app bar.
 * @param navigationActions An instance of [NavigationActions] to handle navigation events.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCenterAlignedTopBar(title: String, navigationActions: NavigationActions) {
  CenterAlignedTopAppBar(
      title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
      navigationIcon = {
        IconButton(
            onClick = { navigationActions.goBack() }, modifier = Modifier.testTag("backButton")) {
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
            }
      },
      colors =
          TopAppBarDefaults.topAppBarColors(
              containerColor = Color.Black,
              titleContentColor = Color.White,
              navigationIconContentColor = Color.White))
}

@Composable
fun OptionColumn(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
    options.forEach { option ->
      Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = option,
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color.Black),
                modifier = Modifier.testTag("${option.replace(" ", "")}Option"))
            androidx.compose.material3.Checkbox(
                checked = selectedOption == option,
                onCheckedChange = { if (it) onOptionSelected(option) },
                colors =
                    androidx.compose.material3.CheckboxDefaults.colors(
                        checkedColor = com.swent.suddenbump.ui.theme.Purple40))
          }
    }
  }
}

@Composable
fun AccountOption(label: String, backgroundColor: Color, onClick: () -> Unit, testTag: String) {
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = 8.dp)
              .clickable { onClick() }
              .background(backgroundColor, RoundedCornerShape(8.dp))
              .height(48.dp)
              .testTag(testTag),
      contentAlignment = Alignment.CenterStart) {
        Text(
            text = label,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = if (backgroundColor == Color.White) Color.Black else Color.White),
            modifier = Modifier.padding(start = 16.dp))
      }
}
/**
 * Composable that displays a location field with dropdown suggestions.
 *
 * @param locationQuery The current query string for the location.
 * @param onLocationQueryChange The callback for when the location query changes.
 * @param locationSuggestions The list of location suggestions.
 * @param onLocationSelected The callback for when a location is selected.
 * @param showDropdown The visibility state of the dropdown.
 * @param onDropdownChange The callback for when the dropdown visibility changes.
 * @param modifier The modifier for the location field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationField(
    locationQuery: String?,
    onLocationQueryChange: (String) -> Unit,
    locationSuggestions: List<Location?>,
    onLocationSelected: (Location) -> Unit,
    showDropdown: Boolean,
    onDropdownChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
  // State to track the number of items displayed
  val maxItemsToShow = remember { mutableIntStateOf(3) }

  ExposedDropdownMenuBox(
      expanded = showDropdown && locationSuggestions.isNotEmpty(),
      onExpandedChange = { onDropdownChange(it) } // Toggle dropdown visibility
      ) {
        OutlinedTextField(
            value = locationQuery ?: "",
            onValueChange = {
              onLocationQueryChange(it)
              onDropdownChange(true) // Show dropdown when user starts typing
              maxItemsToShow.intValue = 3 // Reset to show initial 3 items when query changes
            },
            label = { Text("Location") },
            textStyle = TextStyle(color = Color.White),
            placeholder = { Text("Enter an Address or Location") },
            modifier =
                modifier
                    .menuAnchor() // Anchor the dropdown to this text field
                    .fillMaxWidth()
                    .testTag("Location"),
            singleLine = true)

        // Dropdown menu for location suggestions
        ExposedDropdownMenu(
            expanded = showDropdown && locationSuggestions.isNotEmpty(),
            onDismissRequest = { onDropdownChange(false) },
            modifier = Modifier.background(Color.Black).testTag("DropDownMenu")) {
              locationSuggestions.filterNotNull().take(maxItemsToShow.intValue).forEach { location
                ->
                DropdownMenuItem(
                    text = {
                      Text(
                          text =
                              location.name.take(30) +
                                  if (location.name.length > 30) "..." else "", // Limit name length
                          color = Color.White,
                          maxLines = 1 // Ensure name doesn't overflow
                          )
                    },
                    onClick = {
                      // Extract substring up to the first comma
                      val trimmedLocation = location.name.substringBefore(",")
                      onLocationQueryChange(
                          trimmedLocation) // Update the query with trimmed location
                      onLocationSelected(
                          location.copy(name = trimmedLocation)) // Save the trimmed name
                      onDropdownChange(false) // Close dropdown on selection
                    },
                    modifier =
                        Modifier.padding(8.dp).background(Color.Black).testTag("DropDownMenuItem"))
              }
              if (locationSuggestions.size > maxItemsToShow.intValue) {
                DropdownMenuItem(
                    text = { Text(text = "More...", color = Color.White) },
                    onClick = {
                      maxItemsToShow.value += 3 // Show 3 more items
                    },
                    modifier = Modifier.padding(8.dp).background(Color.Black).testTag("MoreButton"))
              }
            }
      }
}

/**
 * Composable that displays a user's profile image.
 *
 * @param user The user whose profile image is to be displayed.
 */
@Composable
fun UserProfileImage(user: User, size: Int) {
  Box(
      modifier =
          Modifier.size(size.dp) // Set the size of the circular container
              .clip(CircleShape) // Clip the content to a circle
              .background(Gray)
              .testTag("profileImage_${user.uid}")) {
        if (user.profilePicture != null) {
          Image(
              bitmap = user.profilePicture,
              contentDescription = "Existing profile picture",
              modifier = Modifier.fillMaxSize(), // Ensure the image fills the Box
              contentScale = ContentScale.Crop // Crop and scale the image to fit the circle
              )
        } else {
          Image(
              painter = painterResource(R.drawable.profile),
              contentDescription = "Non-Existing profile picture",
              modifier = Modifier.fillMaxSize(), // Ensure the image fills the Box
              contentScale = ContentScale.Crop // Crop and scale the image to fit the circle
              )
        }
      }
}
