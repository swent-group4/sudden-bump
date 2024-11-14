package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.theme.Purple40
import com.swent.suddenbump.ui.theme.Purple80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navigationActions: NavigationActions) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Help", color = Color.White) },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back")
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple40))
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Black)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              // Help Center Section
              Text("Help Center", color = Color.White)
              Button(
                  onClick = { /* Add logic for Help Center */},
                  colors = ButtonDefaults.buttonColors(containerColor = Purple80)) {
                    Text("Visit Help Center", color = Color.White)
                  }

              Divider()

              // Contact Us Section
              Text("Contact Us", color = Color.White)
              Button(
                  onClick = { /* Add logic for Contact Us */},
                  colors = ButtonDefaults.buttonColors(containerColor = Purple80)) {
                    Text("Contact Us")
                  }

              Divider()

              // Terms and Privacy Policy Section
              Text("Terms and Privacy Policy", color = Color.White)
              Button(
                  onClick = { /* Add logic for Terms and Privacy Policy */},
                  colors = ButtonDefaults.buttonColors(containerColor = Purple80)) {
                    Text("View Terms and Privacy Policy", color = Color.White)
                  }

              Divider()

              // Licenses Section
              Text("Licenses", color = Color.White)
              Button(
                  onClick = { /* Add logic for Licenses */},
                  colors = ButtonDefaults.buttonColors(containerColor = Purple80)) {
                    Text("View Licenses", color = Color.White)
                  }

              Spacer(modifier = Modifier.weight(1f))

              // Footer
              Text(
                  "© 2024 SuddenBump LLC",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White)
            }
      })
}
