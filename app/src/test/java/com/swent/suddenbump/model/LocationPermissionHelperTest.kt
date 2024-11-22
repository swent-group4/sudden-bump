package com.swent.suddenbump.model

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.swent.suddenbump.model.location.LocationPermissionHelper
import junit.framework.Assert.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class LocationPermissionTest {

  private lateinit var activity: Activity
  private lateinit var locationPermissionHelper: LocationPermissionHelper

  @Before
  fun setUp() {
    activity = Robolectric.buildActivity(Activity::class.java).create().get()
    locationPermissionHelper = LocationPermissionHelper(activity)
  }

  @Test
  fun locationPermissionGranted_returnsTrue() {
    // Mock the permission check to return PERMISSION_GRANTED
    Mockito.mockStatic(ContextCompat::class.java).use { mockedContextCompat ->
      `when`(ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION))
          .thenReturn(PackageManager.PERMISSION_GRANTED)

      // Check if the permission is granted
      val result = locationPermissionHelper.isLocationPermissionGranted()

      // Assert that the permission is granted
      assertTrue(result)
    }
  }

  @Test
  fun locationPermissionNotGranted_returnsFalse() {
    // Mock the permission check to return PERMISSION_DENIED
    Mockito.mockStatic(ContextCompat::class.java).use { mockedContextCompat ->
      `when`(ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION))
          .thenReturn(PackageManager.PERMISSION_DENIED)

      // Check if the permission is denied
      val result = locationPermissionHelper.isLocationPermissionGranted()

      // Assert that the permission is denied
      assertFalse(result)
    }
  }

  @Test
  fun requestLocationPermission_permissionNotGranted_requestsPermission() {
    // Assume permission is not granted
    locationPermissionHelper.requestLocationPermission()

    // Verify the request is made (can be improved depending on the app logic)
    assertTrue(
        ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_DENIED)
  }

  @Test
  fun requestLocationPermission_permissionGranted_showsToast() {
    // Mock the permission check to return PERMISSION_GRANTED
    Mockito.mockStatic(ContextCompat::class.java).use { mockedContextCompat ->
      `when`(ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION))
          .thenReturn(PackageManager.PERMISSION_GRANTED)

      // Request permission (since it's already granted, it should show a toast)
      locationPermissionHelper.requestLocationPermission()

      // Check that a toast was shown
      assertTrue(ShadowToast.shownToastCount() == 1)
      assertTrue(ShadowToast.getTextOfLatestToast() == "Location Permission Already Granted")
    }
  }

  @Test
  fun handlePermissionResult_permissionGranted_returnsTrue() {
    val grantResults = intArrayOf(PackageManager.PERMISSION_GRANTED)

    val result =
        locationPermissionHelper.handlePermissionResult(
            LocationPermissionHelper.LOCATION_PERMISSION_REQUEST_CODE, grantResults)

    assertTrue(result)
    assertTrue(ShadowToast.shownToastCount() == 1)
    assertTrue(ShadowToast.getTextOfLatestToast() == "Location Permission Granted")
  }

  @Test
  fun handlePermissionResult_permissionDenied_returnsFalse() {
    val grantResults = intArrayOf(PackageManager.PERMISSION_DENIED)

    val result =
        locationPermissionHelper.handlePermissionResult(
            LocationPermissionHelper.LOCATION_PERMISSION_REQUEST_CODE, grantResults)

    assertFalse(result)
    assertTrue(ShadowToast.shownToastCount() == 1)
    assertTrue(ShadowToast.getTextOfLatestToast() == "Location Permission Denied")
  }

  @Test
  fun handlePermissionResult_invalidRequestCode_returnsFalse() {
    val grantResults = intArrayOf(PackageManager.PERMISSION_GRANTED)

    val result = locationPermissionHelper.handlePermissionResult(999, grantResults)

    assertFalse(result)
  }
}
