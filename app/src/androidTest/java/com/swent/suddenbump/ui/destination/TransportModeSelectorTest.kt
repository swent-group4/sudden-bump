package com.swent.suddenbump.ui.destination

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.suddenbump.ui.map.TransportModeSelector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransportModeSelectorTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun transportModeSelector_displaysModes() {
    composeTestRule.setContent { TransportModeSelector(onModeSelected = {}) }

    // Open Menu
    composeTestRule.onNode(hasText("Driving", ignoreCase = true)).performClick()

    // Check if test tags are displayed
    composeTestRule.onNodeWithTag("ModeOption_driving").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ModeOption_walking").assertIsDisplayed()
    composeTestRule.onNodeWithTag("ModeOption_transit").assertIsDisplayed()
  }

  @Test
  fun transportModeSelector_selectsMode() {
    val selectedMode = mutableStateOf("driving")
    composeTestRule.setContent {
      TransportModeSelector(onModeSelected = { mode -> selectedMode.value = mode })
    }

    // Ouvrir le menu
    composeTestRule.onNode(hasText("Driving", ignoreCase = true)).performClick()

    // Sélectionner "Walking"
    composeTestRule.onNode(hasText("Walking", ignoreCase = true)).performClick()

    // Vérifier que le mode sélectionné est "walking"
    assert(selectedMode.value == "walking")
  }
}
