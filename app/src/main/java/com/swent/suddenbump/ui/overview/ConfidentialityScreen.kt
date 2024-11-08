package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.model.user.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfidentialityScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confidentiality") },
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Online Presence Section
                Text("Online presence")
                Text("Who can see my last time online")
                OnlinePresenceOptions()

                Text("Who can see when I am online")
                OnlineStatusOptions()

                Divider()

                // Profile Photo Section
                Text("Profile photo")
                Text("Who can see my profile photo")
                ProfilePhotoOptions()

                Divider()

                // My Info Section
                Text("My info")
                Text("Who can see my info")
                MyInfoOptions()

                Divider()

                // Groups Section
                Text("Groups")
                Text("Who can add me to groups")
                GroupsOptions()

                Divider()

                // Status Section
                Text("Status")
                Text("Who can see my status")
                StatusOptions()

                Divider()

                // Blocked Contacts Section
                Text("Blocked Contacts")
                Button(onClick = { /* Add logic to add blocked contacts */}) {
                    Text("Add to blocked contacts")
                }

                Divider()

                // Location Status Section
                Text("Location Status")
                LocationStatusOptions(userViewModel)
            }
        })
}

@Composable
fun OnlinePresenceOptions() {
    val options = listOf("Everyone", "My contacts", "My contacts except...", "No one")
    var selectedOption by remember { mutableStateOf(options[0]) }

    options.forEach { option ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(option)
            RadioButton(selected = (selectedOption == option), onClick = { selectedOption = option })
        }
    }
}

@Composable
fun OnlineStatusOptions() {
    val options = listOf("Everyone", "Same as last time online")
    var selectedOption by remember { mutableStateOf(options[0]) }

    options.forEach { option ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(option)
            RadioButton(selected = (selectedOption == option), onClick = { selectedOption = option })
        }
    }
}

@Composable
fun ProfilePhotoOptions() {
    val options = listOf("Everyone", "My contacts", "My contacts except...", "No one")
    var selectedOption by remember { mutableStateOf(options[0]) }

    options.forEach { option ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(option)
            RadioButton(selected = (selectedOption == option), onClick = { selectedOption = option })
        }
    }
}

@Composable
fun MyInfoOptions() {
    val options = listOf("Everyone", "My contacts", "My contacts except...", "No one")
    var selectedOption by remember { mutableStateOf(options[0]) }

    options.forEach { option ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(option)
            RadioButton(selected = (selectedOption == option), onClick = { selectedOption = option })
        }
    }
}

@Composable
fun GroupsOptions() {
    val options = listOf("Everyone", "My contacts", "My contacts except...")
    var selectedOption by remember { mutableStateOf(options[0]) }

    options.forEach { option ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(option)
            RadioButton(selected = (selectedOption == option), onClick = { selectedOption = option })
        }
    }
}

@Composable
fun StatusOptions() {
    val options = listOf("Everyone", "My contacts", "My contacts except...")
    var selectedOption by remember { mutableStateOf(options[0]) }

    options.forEach { option ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(option)
            RadioButton(selected = (selectedOption == option), onClick = { selectedOption = option })
        }
    }
}

@Composable
fun LocationStatusOptions(userViewModel: UserViewModel) {
    val options = listOf("Enabled", "Disabled")
    var selectedOption by remember { mutableStateOf(options[0]) }

    options.forEach { option ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(option)
            RadioButton(selected = (selectedOption == option), onClick = { selectedOption = option })
        }
    }
}