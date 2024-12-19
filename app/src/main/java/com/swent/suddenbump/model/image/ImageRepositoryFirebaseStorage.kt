package com.swent.suddenbump.model.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.firebase.storage.FileDownloadTask.TaskSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.swent.suddenbump.ui.utils.isInternetAvailable
import com.swent.suddenbump.ui.utils.isUsingMockException
import com.swent.suddenbump.ui.utils.isUsingMockFileDownloadTask
import com.swent.suddenbump.ui.utils.testableFileDownloadTask
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class ImageRepositoryFirebaseStorage(
    private val storage: FirebaseStorage,
    private val context: Context
) : ImageRepository {

  val profilePicturesPath = "/data/data/com.swent.suddenbump/files/"

  private val imageBitMapIO = ImageBitMapIO()

  override fun init(onSuccess: () -> Unit) {
    onSuccess()
  }

  /**
   * Downloads an image from Firebase Storage and processes it into an `ImageBitmap`.
   *
   * This function retrieves an image file from Firebase Storage using the provided `path`, stores
   * it locally in the app's profile pictures directory, and decodes it into a `Bitmap`. Upon
   * successful completion, the image is passed to the `onSuccess` callback as an `ImageBitmap`. If
   * the operation fails or the task is canceled, the `onFailure` callback is invoked with the
   * corresponding error.
   *
   * This function uses `runBlocking` to perform the image download and decoding operations
   * synchronously within the current thread, blocking it until the task is completed.
   *
   * @param path The path to the image file in Firebase Storage. This should include the file name
   *   and extension (e.g., "images/profile.jpg").
   * @param onSuccess A callback function that is invoked when the image download and decoding are
   *   successful. It receives the resulting `ImageBitmap` as its parameter.
   * @param onFailure A callback function that is invoked when the image download or decoding fails.
   *   It receives an `Exception` that describes the failure.
   * @throws Exception if an error occurs during the image download or decoding process.
   *
   * Example usage:
   * ```kotlin
   * downloadImage("path/to/image.jpg", onSuccess = { image ->
   *     // Use the downloaded image
   * }, onFailure = { exception ->
   *     // Handle the error
   * })
   * ```
   */
  override fun downloadImage(
      path: String,
      onSuccess: (ImageBitmap) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val imageRef = storage.reference.child(path)
    val localFile = File(profilePicturesPath + path.substringAfterLast('/'))
    // Ensure the parent directories exist
    localFile.parentFile?.mkdirs()

    // Create the file
    if (!localFile.exists()) {
      localFile.createNewFile()
    }

    runBlocking { downloadImageFactory(path, imageRef, localFile, onSuccess, onFailure) }
  }

  /**
   * Asynchronously downloads an image from Firebase Storage and processes it into an `ImageBitmap`.
   *
   * This function retrieves an image file from Firebase Storage using the provided `path`, stores
   * it locally in the app's profile pictures directory, and decodes it into a `Bitmap`. Upon
   * successful completion, the image is passed to the `onSuccess` callback as an `ImageBitmap`. If
   * the operation fails or the task is canceled, the `onFailure` callback is invoked with the
   * corresponding error.
   *
   * This function runs the image download and decoding operations asynchronously in a background
   * thread using `CoroutineScope(Dispatchers.IO)` to avoid blocking the main thread.
   *
   * @param path The path to the image file in Firebase Storage. This should include the file name
   *   and extension (e.g., "images/profile.jpg").
   * @param onSuccess A callback function that is invoked when the image download and decoding are
   *   successful. It receives the resulting `ImageBitmap` as its parameter.
   * @param onFailure A callback function that is invoked when the image download or decoding fails.
   *   It receives an `Exception` that describes the failure.
   * @throws Exception if an error occurs during the image download or decoding process.
   *
   * Example usage:
   * ```kotlin
   * downloadImageAsync("path/to/image.jpg", onSuccess = { image ->
   *     // Use the downloaded image
   * }, onFailure = { exception ->
   *     // Handle the error
   * })
   * ```
   */
  override fun downloadImageAsync(
      path: String,
      onSuccess: (ImageBitmap) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val imageRef = storage.reference.child(path)
    val localFile = File(profilePicturesPath + path.substringAfterLast('/'))
    localFile.parentFile?.mkdirs()

    // Create the file
    if (!localFile.exists()) {
      localFile.createNewFile()
    }
    CoroutineScope(Dispatchers.IO).launch {
      downloadImageFactory(path, imageRef, localFile, onSuccess, onFailure)
    }
  }

  /**
   * Uploads an image to Firebase Storage asynchronously.
   *
   * This function takes an `ImageBitmap`, compresses it into a JPEG byte array, and uploads it to
   * Firebase Storage at the specified `path`. Upon successful upload, the `onSuccess` callback is
   * invoked. If the upload fails, the `onFailure` callback is triggered with the corresponding
   * exception.
   *
   * The image is first converted to a byte array using `ByteArrayOutputStream`, and then the
   * `putBytes()` method of Firebase Storage is used to upload the data. The function uses listeners
   * to handle success and failure scenarios, ensuring that the appropriate callback is triggered.
   *
   * This operation is asynchronous and does not block the calling thread.
   *
   * @param imageBitmap The image to be uploaded, provided as an `ImageBitmap`.
   * @param path The storage path where the image will be uploaded. This should include the file
   *   name and extension (e.g., "images/profile.jpg").
   * @param onSuccess A callback function that is invoked when the image upload is successful. It
   *   does not take any parameters.
   * @param onFailure A callback function that is invoked when the image upload fails. It receives
   *   an `Exception` describing the failure.
   * @throws Exception If an error occurs while compressing the image or uploading the data.
   *
   * Example usage:
   * ```kotlin
   * uploadImage(imageBitmap, "images/profile.jpg", onSuccess = {
   *     // Handle success (e.g., update UI)
   * }, onFailure = { exception ->
   *     // Handle failure (e.g., show error message)
   * })
   * ```
   */
  override fun uploadImage(
      imageBitmap: ImageBitmap,
      path: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val baos = ByteArrayOutputStream()
    imageBitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val data = baos.toByteArray()

    val uploadTask = storage.reference.child(path).putBytes(data)
    uploadTask.addOnFailureListener { onFailure(it) }.addOnSuccessListener { onSuccess() }
  }

  private suspend fun downloadImageFactory(
      path: String,
      imageRef: StorageReference,
      localFile: File,
      onSuccess: (ImageBitmap) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val fileInputStream = withContext(Dispatchers.IO) { FileInputStream(localFile) }
    if (!isInternetAvailable(context)) {
      Log.i("FirebaseDownload", "Offline mode : $path")
      offlineDownloadImageFactory(path, onSuccess, onFailure, fileInputStream)
    } else {
      try {
        val timeoutMillis = 1_500L
        withTimeout(timeoutMillis) {
          val fileDownloadTask: TaskSnapshot =
              if (!isUsingMockFileDownloadTask) imageRef.getFile(localFile).await()
              else testableFileDownloadTask
          if (fileDownloadTask.task.isCanceled) {
            fileInputStream.close()
            Log.e("FirebaseDownload", "Download failed : $path")
            val storageTask = fileDownloadTask.task
            onFailure(fileDownloadTask.task.exception!!)
          } else {
            val bitmap =
                BitmapFactory.decodeStream(fileInputStream).also { fileInputStream.close() }
            Log.i(
                "FirebaseDownload",
                "Finished online : ${profilePicturesPath + path.substringAfterLast('/')}")
            onSuccess(bitmap.asImageBitmap())
          }
        }
      } catch (e: CancellationException) {
        Log.e("FirebaseDownload", "Download timed out", e)
        offlineDownloadImageFactory(path, onSuccess, onFailure, fileInputStream)
      } catch (e: Exception) {
        Log.e("FirebaseDownload", "Failed to download file", e)
        withContext(Dispatchers.IO) { fileInputStream.close() }
        onFailure(e)
      }
    }
  }

  private suspend fun offlineDownloadImageFactory(
      path: String,
      onSuccess: (ImageBitmap) -> Unit,
      onFailure: (Exception) -> Unit,
      fileInputStream: FileInputStream
  ) {
    try {
      if (isUsingMockException) {
        throw Exception("TEST")
      }
      val bitmap = BitmapFactory.decodeStream(fileInputStream).also { fileInputStream.close() }
      Log.i(
          "FirebaseDownload",
          "Finished offline : ${profilePicturesPath + path.substringAfterLast('/')}")
      onSuccess(bitmap.asImageBitmap())
    } catch (e2: Exception) {
      Log.e("FirebaseDownload", "Failed to download file locally", e2)
      println(e2)
      withContext(Dispatchers.IO) { fileInputStream.close() }
      onFailure(e2)
    }
  }
}
