package com.swent.suddenbump.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.suddenbump.ui.navigation.NavigationActions
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
