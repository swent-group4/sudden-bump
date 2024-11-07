package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navigationActions: NavigationActions) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text("Help") },
            navigationIcon = {
              IconButton(onClick = { navigationActions.goBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back")
              }
            })
      },
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              // Help Center Section
              Text("Help Center")
              Button(onClick = { /* Add logic for Help Center */}) { Text("Visit Help Center") }

              Divider()

              // Contact Us Section
              Text("Contact Us")
              Button(onClick = { /* Add logic for Contact Us */}) { Text("Contact Us") }

              Divider()

              // Terms and Privacy Policy Section
              Text("Terms and Privacy Policy")
              Button(onClick = { /* Add logic for Terms and Privacy Policy */}) {
                Text("View Terms and Privacy Policy")
              }

              Divider()

              // Licenses Section
              Text("Licenses")
              Button(onClick = { /* Add logic for Licenses */}) { Text("View Licenses") }

              Spacer(modifier = Modifier.weight(1f))

              // Footer
              Text("© 2024 SuddenBump LLC", style = MaterialTheme.typography.bodySmall)
            }
      })
}
