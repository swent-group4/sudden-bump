package com.swent.suddenbump.worker
/*
import android.content.Context
import android.location.Location
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ApplicationProvider
import com.swent.suddenbump.R
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task
import com.swent.suddenbump.model.user.User
import com.swent.suddenbump.model.user.UserViewModel
import com.swent.suddenbump.worker.LocationUpdateWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible


@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class LocationUpdateWorkerTest {

    private lateinit var context: Context

    @Mock
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @Mock
    private lateinit var userViewModel: UserViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun testLocationUpdateWorker_success() = runTest {
        // Mock location
        val mockLocation = mock(Location::class.java).apply {
            `when`(latitude).thenReturn(37.4219983)
            `when`(longitude).thenReturn(-122.084)
        }

        // Mock Task<Location>
        val mockTask = mock(Task::class.java) as Task<Location>
        `when`(mockTask.result).thenReturn(mockLocation)
        `when`(fusedLocationClient.lastLocation).thenReturn(mockTask)

        // Load the profile picture from resources
        val context = ApplicationProvider.getApplicationContext<Context>()
        val profilePictureDrawable = context.getDrawable(R.drawable.profile)
        val profilePictureBitmap =  profilePictureDrawable?.toBitmap()?.asImageBitmap()

        // Mock UserViewModel
        val mockUser = mock(User::class.java).apply {
            `when`(uid).thenReturn("user123")
            `when`(firstName).thenReturn("Test")
            `when`(lastName).thenReturn("User")
            `when`(phoneNumber).thenReturn("123-456-7890")
            `when`(profilePicture).thenReturn(profilePictureBitmap)
            `when`(emailAddress).thenReturn("testuser@example.com")
        }
        doNothing().`when`(userViewModel).updateLocation(
            eq(mockUser),
            eq(mockLocation),
            any(),
            any()
        )
        doNothing().`when`(userViewModel).loadFriendsLocations()

        // Build the worker
        val worker = TestListenableWorkerBuilder<LocationUpdateWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker? {
                    return LocationUpdateWorker(appContext, workerParameters).apply {
                        setPrivateProperty(this, "fusedLocationClient", this@LocationUpdateWorkerTest.fusedLocationClient)
                        setPrivateProperty(this, "userViewModel", this@LocationUpdateWorkerTest.userViewModel)
                    }
                }
            })
            .build()

        // Run the worker
        val result = worker.startWork().get()

        // Verify the result
        assert(result is ListenableWorker.Result.Success)
    }

    @Test
    fun testLocationUpdateWorker_failure() = runTest {
        // Mock Task<Location> to throw an exception
        val mockTask = mock(Task::class.java) as Task<Location>
        `when`(mockTask.exception).thenReturn(RuntimeException("Location error"))
        `when`(fusedLocationClient.lastLocation).thenReturn(mockTask)

        // Build the worker
        val worker = TestListenableWorkerBuilder<LocationUpdateWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker? {
                    return LocationUpdateWorker(appContext, workerParameters).apply {
                        setPrivateProperty(this, "fusedLocationClient", this@LocationUpdateWorkerTest.fusedLocationClient)
                    }
                }
            })
            .build()

        // Run the worker
        val result = worker.startWork().get()

        // Verify the result
        assert(result is ListenableWorker.Result.Failure)
    }

    private fun setPrivateProperty(instance: Any, propertyName: String, value: Any) {
        val property = instance::class.declaredMemberProperties.first { it.name == propertyName }
        property.isAccessible = true
        (property as? KMutableProperty1<Any, Any>)?.set(instance, value)
    }
}*/