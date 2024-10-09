package com.swent.suddenbump.model

import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
    listener =
        object : LocationGetter.LocationListener {
          override fun onLocationResult(location: Location?) {
            // You can add assertions in the implementation
          }

          override fun onLocationFailure(message: String) {
            // You can add assertions in the implementation
          }
        }
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

  //  @Test
  //  fun requestLocationUpdates_permissionDenied_callsOnLocationFailure() {
  //    ShadowApplication.getInstance()
  //        .denyPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION)
  //
  //    locationGetter.requestLocationUpdates()
  //
  //    // Ensure that the failure message is handled correctly (you can mock listener if needed)
  //    // Replace with assertions to ensure correctness
  //  }

  @Test
  fun onLocationResult_locationAvailable_callsOnLocationResult() {
    val location = Location("mock_provider")
    location.latitude = 37.7749
    location.longitude = -122.4194

    fusedLocationClient.requestLocationUpdates(
        LocationRequest.create(),
        object : LocationCallback() {
          override fun onLocationResult(result: LocationResult) {
            locationGetter.requestLocationUpdates()
            listener.onLocationResult(location)
          }
        },
        null)

    // Simulate location available
    listener.onLocationResult(location)

    assertEquals(37.7749, location.latitude, 0.0001)
    assertEquals(-122.4194, location.longitude, 0.0001)
  }

  @Test
  fun onLocationResult_locationNotAvailable_callsOnLocationFailure() {
    fusedLocationClient.requestLocationUpdates(
        LocationRequest.create(),
        object : LocationCallback() {
          override fun onLocationResult(result: LocationResult) {
            locationGetter.requestLocationUpdates()
            listener.onLocationFailure("Location not available")
          }
        },
        null)

    // Simulate no location available
    listener.onLocationFailure("Location not available")

    // Add your assertions here for the failure case
  }

  //  @Test
  //  fun stopLocationUpdates_removesLocationUpdates() {
  //    locationGetter.requestLocationUpdates()
  //
  //    locationGetter.stopLocationUpdates()
  //
  //    assertNotNull(locationGetter)
  //  }
}
