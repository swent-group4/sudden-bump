package com.swent.suddenbump.ui.destination

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.swent.suddenbump.ui.map.openGoogleMapsDirections
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.argumentCaptor

class OpenGoogleMapsDirectionsTest {

  @Test
  fun openGoogleMapsDirections_createsCorrectIntent() {
    // Create mock of context
    val context = mock<Context>()

    val origin = LatLng(0.0, 0.0)
    val destination = LatLng(1.0, 1.0)
    val mode = "driving"

    // Call function to test
    openGoogleMapsDirections(context, origin, destination, mode)

    // Captures past intent to startActivity
    val intentCaptor = argumentCaptor<Intent>()
    verify(context).startActivity(intentCaptor.capture())

    val capturedIntent = intentCaptor.firstValue

    // Construct URI for expected intent
    val expectedUri =
        Uri.parse(
            "https://www.google.com/maps/dir/?api=1&origin=0.0,0.0&destination=1.0,1.0&travelmode=driving")

    // checks if captured intent corresponds to intent expected
    assertEquals(Intent.ACTION_VIEW, capturedIntent.action)
    assertEquals(expectedUri, capturedIntent.data)
    assertEquals("com.google.android.apps.maps", capturedIntent.`package`)
  }
}
