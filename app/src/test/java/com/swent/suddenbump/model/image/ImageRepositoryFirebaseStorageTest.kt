package com.swent.suddenbump.model.image

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FileDownloadTask.TaskSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.kaspersky.kaspresso.internal.extensions.other.createFileIfNeeded
import java.io.File
import java.io.FileOutputStream
import kotlin.test.fail
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ImageRepositoryFirebaseStorageTest {

  @Mock private lateinit var mockImageRepository: ImageRepositoryFirebaseStorage

  @Mock private lateinit var mockFirebaseStorage: FirebaseStorage

  @Mock private lateinit var mockFirebaseReference: StorageReference

  @Mock private lateinit var mockFileDownloadTask: FileDownloadTask

  @Mock private lateinit var mockTaskSnapshot: TaskSnapshot

  @Mock private lateinit var mockStorageTask: StorageTask<TaskSnapshot>

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)

    System.setProperty("mockito.verbose", "true")

    // Initialize Firebase if necessary
    if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
      FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
    }

    mockImageRepository = ImageRepositoryFirebaseStorage(mockFirebaseStorage)
  }

  @Test
  fun downloadImageSyncMakeRequests() {
    runBlocking {
      val testContext: Context = ApplicationProvider.getApplicationContext()
      val pathOverride = testContext.getExternalFilesDir(null)?.path

      val profilePicturesPath =
          ImageRepositoryFirebaseStorage::class.java.getDeclaredField("profilePicturesPath")
      profilePicturesPath.isAccessible = true
      profilePicturesPath.set(mockImageRepository, pathOverride)

      val uriExternal = testContext.getExternalFilesDir(null)?.toURI()

      val uriImage = Uri.parse("file://${uriExternal!!.path}imagetest.jpeg")

      val fileOutputStream: FileOutputStream
      val drawable = ContextCompat.getDrawable(testContext, android.R.drawable.arrow_down_float)!!
      val bitmap =
          Bitmap.createBitmap(
              drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)

      try {
        val file = File(uriImage.path!!).createFileIfNeeded()
        fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream).also {
          fileOutputStream.close()
        }
      } catch (e: Exception) {
        Log.d("Debug", e.toString())
        fail("Couldn't write the file for the test")
      }

      `when`(mockFirebaseStorage.reference).thenReturn(mockFirebaseReference)
      `when`(mockFirebaseReference.child(anyString())).thenReturn(mockFirebaseReference)
      `when`(mockFirebaseReference.getFile(any<File>())).thenReturn(mockFileDownloadTask)
      `when`(mockFileDownloadTask.isCanceled).thenReturn(false)
      `when`(mockFileDownloadTask.isComplete).thenReturn(true)
      `when`(mockFileDownloadTask.isSuccessful).thenReturn(true)
      `when`(mockFileDownloadTask.await()).thenReturn(mockTaskSnapshot)
      `when`(mockTaskSnapshot.task).thenReturn(mockStorageTask)

      val results =
          mockImageRepository.downloadImage(
              "/imagetest.jpeg", { println() }, { fail("Fetching should not fail") })
    }

    verify(mockFirebaseStorage).reference
    verify(mockTaskSnapshot).task
  }

  @Test
  fun downloadImageAsyncMakeRequests() {
    runBlocking {
      val testContext: Context = ApplicationProvider.getApplicationContext()
      val pathOverride = testContext.getExternalFilesDir(null)?.path

      val profilePicturesPath =
          ImageRepositoryFirebaseStorage::class.java.getDeclaredField("profilePicturesPath")
      profilePicturesPath.isAccessible = true
      profilePicturesPath.set(mockImageRepository, pathOverride)

      val uriExternal = testContext.getExternalFilesDir(null)?.toURI()

      val uriImage = Uri.parse("file://${uriExternal!!.path}imagetest.jpeg")

      val fileOutputStream: FileOutputStream
      val drawable = ContextCompat.getDrawable(testContext, android.R.drawable.arrow_down_float)!!
      val bitmap =
          Bitmap.createBitmap(
              drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)

      try {
        val file = File(uriImage.path!!).createFileIfNeeded()
        fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream).also {
          fileOutputStream.close()
        }
      } catch (e: Exception) {
        Log.d("Debug", e.toString())
        fail("Couldn't write the file for the test")
      }

      `when`(mockFirebaseStorage.reference).thenReturn(mockFirebaseReference)
      `when`(mockFirebaseReference.child(anyString())).thenReturn(mockFirebaseReference)
      `when`(mockFirebaseReference.getFile(any<File>())).thenReturn(mockFileDownloadTask)
      `when`(mockFileDownloadTask.isCanceled).thenReturn(false)
      `when`(mockFileDownloadTask.isComplete).thenReturn(true)
      `when`(mockFileDownloadTask.isSuccessful).thenReturn(true)
      `when`(mockFileDownloadTask.await()).thenReturn(mockTaskSnapshot)
      `when`(mockTaskSnapshot.task).thenReturn(mockStorageTask)
      `when`(mockStorageTask.isCanceled).thenReturn(false)

      val results =
          mockImageRepository.downloadImageAsync(
              "/imagetest.jpeg", { println() }, { fail("Fetching should not fail") })
    }

    verify(mockFirebaseStorage).reference
  }
}