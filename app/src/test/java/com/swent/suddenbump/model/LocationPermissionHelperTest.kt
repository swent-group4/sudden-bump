package com.swent.suddenbump.model

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.swent.suddenbump.model.location.LocationPermissionHelper
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
class LocationPermissionHelperTest {

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
      org.junit.Assert.assertTrue(result)
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
      org.junit.Assert.assertFalse(result)
    }
  }

  @Test
  fun requestLocationPermission_permissionNotGranted_requestsPermission() {
    // Assume permission is not granted
    Mockito.mockStatic(ContextCompat::class.java).use { mockedContextCompat ->
      `when`(ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION))
          .thenReturn(PackageManager.PERMISSION_DENIED)

      // Request location permission
      locationPermissionHelper.requestLocationPermission()

      // Verify that the permission is still denied
      val permissionStatus =
          ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
      org.junit.Assert.assertTrue(permissionStatus == PackageManager.PERMISSION_DENIED)
    }
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
      org.junit.Assert.assertTrue(ShadowToast.shownToastCount() == 1)
      org.junit.Assert.assertTrue(
          ShadowToast.getTextOfLatestToast() == "Location Permission Already Granted")
    }

    @Test
    fun handlePermissionResult_permissionGranted_returnsTrue() {
      val grantResults = intArrayOf(PackageManager.PERMISSION_GRANTED)

      val result =
          locationPermissionHelper.handlePermissionResult(
              LocationPermissionHelper.LOCATION_PERMISSION_REQUEST_CODE, grantResults)

      org.junit.Assert.assertTrue(result)
      org.junit.Assert.assertTrue(ShadowToast.shownToastCount() == 1)
      org.junit.Assert.assertTrue(
          ShadowToast.getTextOfLatestToast() == "Location Permission Granted")
    }

    @Test
    fun handlePermissionResult_permissionDenied_returnsFalse() {
      val grantResults = intArrayOf(PackageManager.PERMISSION_DENIED)

      val result =
          locationPermissionHelper.handlePermissionResult(
              LocationPermissionHelper.LOCATION_PERMISSION_REQUEST_CODE, grantResults)

      org.junit.Assert.assertFalse(result)
      org.junit.Assert.assertTrue(ShadowToast.shownToastCount() == 1)
      org.junit.Assert.assertTrue(
          ShadowToast.getTextOfLatestToast() == "Location Permission Denied")
    }

    @Test
    fun handlePermissionResult_invalidRequestCode_returnsFalse() {
      val grantResults = intArrayOf(PackageManager.PERMISSION_GRANTED)

      val result = locationPermissionHelper.handlePermissionResult(999, grantResults)

      org.junit.Assert.assertFalse(result)
    }
  }
}
