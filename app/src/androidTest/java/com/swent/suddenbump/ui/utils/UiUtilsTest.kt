package com.swent.suddenbump.ui.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.swent.suddenbump.model.meeting_location.Location
import com.swent.suddenbump.ui.navigation.NavigationActions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class UiUtilsTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun labeledButtonSectionDisplaysCorrectly() {
    val label = "Test Label"
    val buttonText = "Test Button"
    val labelTag = "labelTag"
    val buttonTag = "buttonTag"
    var buttonClicked = false

    composeTestRule.setContent {
      LabeledButtonSection(
          label = label,
          buttonText = buttonText,
          onClick = { buttonClicked = true },
          labelTag = labelTag,
          buttonTag = buttonTag)
    }

    // Verify that the label and button are displayed
    composeTestRule.onNodeWithTag(labelTag).assertIsDisplayed().assertTextEquals(label)
    composeTestRule.onNodeWithTag(buttonTag).assertIsDisplayed().assertTextEquals(buttonText)

    // Simulate button click
    composeTestRule.onNodeWithTag(buttonTag).performClick()
    assert(buttonClicked) { "Button was not clicked!" }
  }

  @Test
  fun customTopBarDisplaysCorrectlyAndHandlesBackAction() {
    val navigationActions = mock<NavigationActions>()

    composeTestRule.setContent {
      CustomTopBar(
          title = "Test Title",
          navigationActions = navigationActions,
          titleTag = "titleTag",
          backButtonTag = "backButtonTag")
    }

    // Verify that the title is displayed
    composeTestRule.onNodeWithTag("titleTag").assertIsDisplayed().assertTextEquals("Test Title")

    // Verify that the back button is displayed and performs the navigation action
    composeTestRule.onNodeWithTag("backButtonTag").assertIsDisplayed().performClick()
    verify(navigationActions).goBack()
  }

  @Test
  fun optionColumnDisplaysAndHandlesSelection() {
    val options = listOf("Option 1", "Option 2", "Option 3")
    composeTestRule.setContent {
      var selectedOption by remember { mutableStateOf(options[0]) }

      OptionColumn(
          options = options,
          selectedOption = selectedOption,
          onOptionSelected = { selectedOption = it })
    }

    // Verify that all options are displayed
    options.forEach { option ->
      composeTestRule.onNodeWithTag("${option.replace(" ", "")}Option").assertIsDisplayed()
    }

    // Simulate selecting an option
    composeTestRule.onNodeWithTag("Option2Option").performClick()

    // Verify that the state is updated
    composeTestRule.onNodeWithTag("Option2Option").assertExists()
  }

  @Test
  fun accountOptionDisplaysAndHandlesClick() {
    val label = "Test Option"
    val testTag = "testOptionTag"
    var optionClicked = false

    composeTestRule.setContent {
      AccountOption(
          label = label,
          backgroundColor = Color.White,
          onClick = { optionClicked = true },
          testTag = testTag)
    }

    // Verify that the label is displayed
    composeTestRule.onNodeWithTag(testTag).assertIsDisplayed().assertTextContains(label)

    // Simulate a click and verify the action
    composeTestRule.onNodeWithTag(testTag).performClick()
    assert(optionClicked) { "Account option was not clicked!" }
  }

  @Test
  fun testLocationFieldDisplaysAndHandlesSelection() {
    val locationSuggestions =
        listOf(
            Location(12.34, 56.78, "Location 1"),
            Location(34.56, 78.90, "Location 2"),
            Location(90.12, 34.56, "Location 3"))

    var locationQuery = ""
    var showDropdown = true
    var selectedLocation: Location? = null

    composeTestRule.setContent {
      LocationField(
          locationQuery = locationQuery,
          onLocationQueryChange = { locationQuery = it },
          locationSuggestions = locationSuggestions,
          onLocationSelected = { selectedLocation = it },
          showDropdown = showDropdown,
          onDropdownChange = { showDropdown = it })
    }

    // Verify the text field exists
    composeTestRule.onNodeWithTag("Location").assertExists()
    composeTestRule.onNodeWithTag("Location").assertIsDisplayed()

    // Simulate user typing to trigger the dropdown
    composeTestRule.onNodeWithTag("Location").performTextInput("Loc")
    composeTestRule.waitForIdle()

    // Verify the dropdown menu is displayed
    composeTestRule.onNodeWithTag("DropDownMenu").assertExists()
    composeTestRule.onNodeWithTag("Location").assertIsDisplayed()

    // Select the first suggestion
    composeTestRule.onAllNodesWithTag("DropDownMenuItem")[0].performClick()
    composeTestRule.waitForIdle()

    // Verify the selected location is updated
    assert(selectedLocation?.name == "Location 1")
  }
}
