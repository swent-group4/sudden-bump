package com.swent.suddenbump.model.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class ImageRepositoryFirebaseStorage(private val storage: FirebaseStorage) : ImageRepository {

  private val imageBitMapIO = ImageBitMapIO()

  override fun init(onSuccess: () -> Unit) {
    onSuccess()
  }

  override fun downloadImage(
      path: String,
      onSuccess: (ImageBitmap) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val imageRef = storage.reference.child(path)
    val localFile = File.createTempFile("sudden-bump-", path.substringAfterLast('/'))

    runBlocking {
      try {
        val fileDownloadTask = imageRef.getFile(localFile).await()
        if (fileDownloadTask.task.isCanceled) {
          onFailure(fileDownloadTask.task.exception!!)
        } else {
          val fileInputStream = FileInputStream(localFile)
          val bitmap = BitmapFactory.decodeStream(fileInputStream).also { fileInputStream.close() }
          onSuccess(bitmap.asImageBitmap())
        }
      } catch (e: Exception) {
        Log.e("SuddenBump", e.toString())
      }
    }
  }

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
}
