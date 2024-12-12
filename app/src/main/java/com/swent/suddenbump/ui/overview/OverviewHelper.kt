package com.swent.suddenbump.ui.overview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.swent.suddenbump.ui.navigation.NavigationActions

/**
 * A composable function that displays a top app bar for the some screen in Overview.
 *
 * @param title The title to be displayed in the top app bar.
 * @param navigationActions An instance of [NavigationActions] to handle navigation events.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreenTopBar(title: String, navigationActions: NavigationActions) {
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
