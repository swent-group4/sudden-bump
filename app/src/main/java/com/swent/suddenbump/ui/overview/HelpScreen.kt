package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(navigationActions: NavigationActions) {
  val sections =
      listOf(
          HelpSectionData(
              "Help Center", "Visit Help Center", "helpCenterText", "visitHelpCenterButton"),
          HelpSectionData("Contact Us", "Get in Touch", "contactUsText", "contactUsButton"),
          HelpSectionData(
              "Terms and Privacy Policy",
              "View Terms and Privacy Policy",
              "termsPrivacyPolicyText",
              "viewTermsPrivacyPolicyButton"),
          HelpSectionData("Licenses", "View Licenses", "licensesText", "viewLicensesButton"))

  Scaffold(
      modifier = Modifier.testTag("helpScreen").background(Color.Black), // Outer black background
      topBar = {
        TopAppBar(
            title = {
              Text(
                  "Help",
                  style =
                      MaterialTheme.typography.headlineMedium.copy(
                          fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White),
                  modifier = Modifier.testTag("helpTitle"))
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() },
                  modifier = Modifier.testTag("backButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White)
                  }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple40))
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black) // Inner black background
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()) // Add scrolling behavior
                    .padding(16.dp)
                    .testTag("helpScreenScrollable"), // Tag for scrolling
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              sections.forEach { section ->
                HelpSectionWithButton(
                    label = section.label,
                    buttonText = section.buttonText,
                    onClick = { /* Add specific logic */},
                    labelTag = section.labelTag,
                    buttonTag = section.buttonTag)
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Footer Text
              Text(
                  "© 2024 SuddenBump LLC",
                  style =
                      MaterialTheme.typography.bodySmall.copy(
                          fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(top = 32.dp)
                          .testTag("footerText") // Tag for the footer
                  )
            }
      })
}

data class HelpSectionData(
    val label: String,
    val buttonText: String,
    val labelTag: String,
    val buttonTag: String
)

@Composable
fun HelpSectionWithButton(
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
