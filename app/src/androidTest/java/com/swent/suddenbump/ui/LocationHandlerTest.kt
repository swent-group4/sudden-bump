//package com.swent.suddenbump.ui
//
//import androidx.compose.ui.test.junit4.createComposeRule
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.mockito.Mockito.*
//import android.location.Location
//import androidx.activity.ComponentActivity
//import com.swent.suddenbump.ui.map.LocationHandler
//import com.swent.suddenbump.model.LocationGetter
//import org.mockito.Mockito
//
//@RunWith(AndroidJUnit4::class)
//class LocationHandlerTest {
//
//    @get:Rule
//    val composeTestRule = createComposeRule()
//
//    @Test
//    fun testLocationUpdatesWithPermissionGranted() {
//        val mockContext = mock(ComponentActivity::class.java)
//        val mockLocationGetter = mock(LocationGetter::class.java)
//
//        // Use the composable under test
//        composeTestRule.setContent {
//            LocationHandler(context = mockContext) {
//                // Validate location updates
//            }
//        }
//
//        // Simulate permission granted and location updates
//        `when`(mockLocationGetter.requestLocationUpdates()).then {
//            // Simulate location update
//            Location("mockLocation").apply {
//                latitude = 40.0
//                longitude = -74.0
//            }
//        }
//
//        // Verify that location updates were requested
//        verify(mockLocationGetter).requestLocationUpdates()
//    }
//}
