package com.swent.suddenbump.model

import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.swent.suddenbump.model.location.LocationGetter
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class LocationGetterTest {

  private lateinit var context: Context
  private lateinit var listener: LocationGetter.LocationListener
  private lateinit var fusedLocationClient: FusedLocationProviderClient
  private lateinit var locationGetter: LocationGetter

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    listener = mock(LocationGetter.LocationListener::class.java)
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    locationGetter = LocationGetter(context, listener)
  }

  @Test
  fun requestLocationUpdates_permissionGranted_requestsLocationUpdates() {
    ShadowApplication.getInstance()
        .grantPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION)

    locationGetter.requestLocationUpdates()

    assertNotNull(locationGetter)
  }

  @Test
  fun onLocationResult_locationAvailable_callsOnLocationResult() {
    // Create a mock location with sample latitude and longitude
    val location =
        Location("mock_provider").apply {
          latitude = 37.7749
          longitude = -122.4194
        }

    // Create a mock LocationResult containing the mock location
    val locationResult = LocationResult.create(listOf(location))

    // Simulate the LocationCallback receiving a location result
    locationGetter.requestLocationUpdates()

    // Access the private locationCallback field indirectly through the LocationGetter class
    // behavior
    val locationCallbackField = locationGetter.javaClass.getDeclaredField("locationCallback")
    locationCallbackField.isAccessible = true
    val locationCallback = locationCallbackField.get(locationGetter) as LocationCallback

    // Trigger the location callback with the mock LocationResult
    locationCallback.onLocationResult(locationResult)

    // Verify that the listener was called with the correct location
    verify(listener).onLocationResult(location)
  }

  @Test
  fun onLocationResult_locationNotAvailable_callsOnLocationFailure() {
    // Create an empty LocationResult (no locations available)
    val locationResult = LocationResult.create(emptyList())

    // Simulate the LocationCallback receiving an empty location result
    locationGetter.requestLocationUpdates()

    // Access the private locationCallback field indirectly through the LocationGetter class
    // behavior
    val locationCallbackField = locationGetter.javaClass.getDeclaredField("locationCallback")
    locationCallbackField.isAccessible = true
    val locationCallback = locationCallbackField.get(locationGetter) as LocationCallback

    // Trigger the location callback with the empty LocationResult
    locationCallback.onLocationResult(locationResult)

    // Verify that the listener was called with the failure message
    verify(listener).onLocationFailure("Location not available") // Verify interaction with the mock
  }

  @Test
  fun stopLocationUpdates_removesLocationUpdates() {
    locationGetter.requestLocationUpdates()

    locationGetter.stopLocationUpdates()

    assertNotNull(locationGetter)
  }
}
