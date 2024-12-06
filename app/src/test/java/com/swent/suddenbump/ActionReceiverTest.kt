package com.swent.suddenbump

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.swent.suddenbump.model.user.UserRepositoryFirestore
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ActionReceiverTest {

  private lateinit var context: Context
  private lateinit var intent: Intent
  private lateinit var mockNotificationManager: NotificationManagerCompat
  private lateinit var mockRepository: UserRepositoryFirestore
  private lateinit var actionReceiver: ActionReceiver

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    // Initialize context using Robolectric
    context = RuntimeEnvironment.getApplication()

    intent = Intent()

    // Mock NotificationManagerCompat
    mockNotificationManager = mock(NotificationManagerCompat::class.java)

    // Mock UserRepositoryFirestore
    mockRepository = mock(UserRepositoryFirestore::class.java)

    // Inject mocked dependencies into ActionReceiver
    actionReceiver =
        ActionReceiver(
            notificationManagerFactory = { mockNotificationManager },
            userRepositoryFactory = { mockRepository })
  }

  @Test
  fun onReceiveShouldHandleActionAccept() {
    // Arrange
    intent.action = "ACTION_ACCEPT"
    intent.putExtra("userUID", "user123")
    intent.putExtra("FriendUID", "friend456")
    intent.putExtra("notificationId", 1)

    // Act
    actionReceiver.onReceive(context, intent)

    // Assert
    verify(mockNotificationManager).cancel(eq(1))
    verify(mockRepository).shareLocationWithFriend(eq("user123"), eq("friend456"), any(), any())
  }

  @Test
  fun onReceiveShouldHandleActionRefuse() {
    // Arrange
    intent.action = "ACTION_REFUSE"
    intent.putExtra("userUID", "user123")
    intent.putExtra("FriendUID", "friend456")
    intent.putExtra("notificationId", 2)

    // Act
    actionReceiver.onReceive(context, intent)

    // Assert
    verify(mockNotificationManager).cancel(eq(2))
    verify(mockRepository)
        .stopSharingLocationWithFriend(eq("user123"), eq("friend456"), any(), any())
  }

  @Test
  fun onReceiveShouldHandleUnknownAction() {
    // Arrange
    intent.action = "UNKNOWN_ACTION"
    intent.putExtra("userUID", "user123")
    intent.putExtra("FriendUID", "friend456")
    intent.putExtra("notificationId", 3)

    // Act
    actionReceiver.onReceive(context, intent)

    // Assert
    verifyNoInteractions(mockNotificationManager)
    verifyNoInteractions(mockRepository)
  }
}
