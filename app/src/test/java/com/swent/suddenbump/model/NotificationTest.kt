package com.swent.suddenbump.model

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.swent.suddenbump.MainActivity
import com.swent.suddenbump.model.meeting.Meeting
import com.swent.suddenbump.ui.calendar.showMeetingScheduledNotification
import com.swent.suddenbump.ui.map.showFriendNearbyNotification
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
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

  @Test
  fun testShowMeetingScheduledNotification() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val parsedDate = dateFormat.parse("25/12/2024")
    val calendar =
        GregorianCalendar().apply {
          if (parsedDate != null) {
            time = parsedDate
          }
          set(Calendar.HOUR_OF_DAY, 0)
          set(Calendar.MINUTE, 0)
          set(Calendar.SECOND, 0)
          set(Calendar.MILLISECOND, 0)
        }
    val meetingDate = com.google.firebase.Timestamp(calendar.time)

    // Create a sample meeting object
    val meeting =
        Meeting(
            meetingId = "123",
            location = "Ikea",
            date = meetingDate,
            friendId = "friend",
            creatorId = "creator",
            accepted = false)

    // Call the function to show the notification
    showMeetingScheduledNotification(context, meeting)

    // Verify that the NotificationChannel was created
    val notificationChannel =
        notificationManager.getNotificationChannel("meeting_scheduled_channel")
    assertEquals("Meeting Scheduled Notifications", notificationChannel?.name)
    assertEquals(NotificationManager.IMPORTANCE_HIGH, notificationChannel?.importance)

    // Retrieve the latest notification from the NotificationManager
    val shadowNotificationManager = Shadows.shadowOf(notificationManager)
    val notification: Notification = shadowNotificationManager.allNotifications.first()

    // Verify notification content
    assertEquals("New Meeting Request", notification.extras.getString(Notification.EXTRA_TITLE))
    assertEquals(
        "You have a new meeting request at Ikea on 25/12/2024.",
        notification.extras.getString(Notification.EXTRA_TEXT))

    // Verify the PendingIntent destination
    val pendingIntent = notification.contentIntent
    val shadowPendingIntent = Shadows.shadowOf(pendingIntent)
    val actualIntent = shadowPendingIntent.savedIntent
    assertEquals(MainActivity::class.java.name, actualIntent.component?.className)
    assertEquals("Screen.PENDING_MEETINGS", actualIntent.getStringExtra("destination"))
    assertEquals(
        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK, actualIntent.flags)
  }
}
