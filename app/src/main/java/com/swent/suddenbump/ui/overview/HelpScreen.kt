@file:OptIn(ExperimentalMaterial3Api::class)

package com.swent.suddenbump.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swent.suddenbump.ui.navigation.NavigationActions
import com.swent.suddenbump.ui.utils.CustomTopBar
import com.swent.suddenbump.ui.utils.LabeledButtonSection

data class HelpSectionData(
    val label: String,
    val buttonText: String,
    val labelTag: String,
    val buttonTag: String
)

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
      modifier = Modifier.testTag("helpScreen").background(Color.Black),
      topBar = {
        CustomTopBar(
            title = "Help",
            navigationActions = navigationActions,
            titleTag = "helpTitle",
            backButtonTag = "backButton")
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .testTag("helpScreenScrollable"),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
              sections.forEach { section ->
                LabeledButtonSection(
                    label = section.label,
                    buttonText = section.buttonText,
                    onClick = { /* TODO: Add specific logic */},
                    labelTag = section.labelTag,
                    buttonTag = section.buttonTag)
                Divider(color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Footer
              Text(
                  text = "© 2024 SuddenBump LLC",
                  style =
                      MaterialTheme.typography.bodySmall.copy(
                          fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White),
                  modifier = Modifier.fillMaxWidth().padding(top = 32.dp).testTag("footerText"))
            }
      })
}
