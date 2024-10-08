//package com.swent.suddenbump.ui
//
//import androidx.compose.ui.test.*
//import androidx.compose.ui.test.junit4.createComposeRule
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import com.swent.suddenbump.ui.map.MapScreen
//import android.content.Context
//import androidx.activity.ComponentActivity
//import androidx.compose.ui.platform.LocalContext
//
//@RunWith(AndroidJUnit4::class)
//class MapScreenTest {
//
//    @get:Rule
//    val composeTestRule = createComposeRule()
//
//    @Test
//    fun testMapIsDisplayed() {
//        // Start the composable under test
//        composeTestRule.setContent {
//            // Pass a mock context to MapScreen
//            MapScreen(LocalContext.current)
//        }
//
//        // Check that the Google Map is being displayed by looking for the testTag
//        composeTestRule.onNodeWithTag("overviewScreen").assertExists()
//    }
//}
