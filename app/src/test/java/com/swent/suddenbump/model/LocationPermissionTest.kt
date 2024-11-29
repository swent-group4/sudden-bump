package com.swent.suddenbump.model

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.swent.suddenbump.model.location.LocationPermission
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

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class LocationPermissionTest {

  private lateinit var activity: Activity
  private lateinit var locationPermission: LocationPermission

  @Before
  fun setUp() {
    activity = Robolectric.buildActivity(Activity::class.java).create().get()
    locationPermission = LocationPermission(activity)
  }

  @Test
  fun locationPermissionGranted_returnsTrue() {
    // Mock the permission check to return PERMISSION_GRANTED
    Mockito.mockStatic(ContextCompat::class.java).use {
      `when`(ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION))
          .thenReturn(PackageManager.PERMISSION_GRANTED)

      // Check if the permission is granted
      val result = locationPermission.isLocationPermissionGranted()

      // Assert that the permission is granted
      assertTrue(result)
    }
  }

  @Test
  fun locationPermissionNotGranted_returnsFalse() {
    // Mock the permission check to return PERMISSION_DENIED
    Mockito.mockStatic(ContextCompat::class.java).use {
      `when`(ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION))
          .thenReturn(PackageManager.PERMISSION_DENIED)

      // Check if the permission is denied
      val result = locationPermission.isLocationPermissionGranted()

      // Assert that the permission is denied
      assertFalse(result)
    }
  }
}
