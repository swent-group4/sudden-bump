package com.swent.suddenbump.model

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.swent.suddenbump.MainActivity
import com.swent.suddenbump.ui.map.showFriendNearbyNotification
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class NotificationTest {

  @Test
  fun testShowFriendNearbyNotification() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Call the function to show the notification
    showFriendNearbyNotification(context)

    // Verify that the NotificationChannel was created
    val notificationChannel = notificationManager.getNotificationChannel("friend_nearby_channel")
    assertEquals("Friend Nearby Notifications", notificationChannel?.name)
    assertEquals(NotificationManager.IMPORTANCE_HIGH, notificationChannel?.importance)

    // Retrieve the latest notification from the NotificationManager
    val shadowNotificationManager = Shadows.shadowOf(notificationManager)
    val notification: Notification = shadowNotificationManager.allNotifications.first()

    // Verify notification content
    assertEquals("Friend Nearby", notification.extras.getString(Notification.EXTRA_TITLE))
    assertEquals(
        "A friend is within your radius!", notification.extras.getString(Notification.EXTRA_TEXT))

    // Verify the PendingIntent destination
    val pendingIntent = notification.contentIntent
    val shadowPendingIntent = Shadows.shadowOf(pendingIntent)
    val actualIntent = shadowPendingIntent.savedIntent
    assertEquals(MainActivity::class.java.name, actualIntent.component?.className)
    assertEquals("Screen.OVERVIEW", actualIntent.getStringExtra("destination"))
    assertEquals(
        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK, actualIntent.flags)
  }
}
