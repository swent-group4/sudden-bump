package com.swent.suddenbump.model.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.swent.suddenbump.R
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ImageBitMapIO {

    fun getInternalImageBitMap(
        context: Context,
        path: String,
        onSuccess: (ImageBitmap) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val fileInputStream = FileInputStream(File(context.filesDir, path))
            val bitmap =
                BitmapFactory.decodeStream(fileInputStream).also { fileInputStream.close() }
            onSuccess(bitmap.asImageBitmap())
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    fun setInternalImageBitMap(
        context: Context,
        path: String,
        imageBitmap: ImageBitmap,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileOutputStream: FileOutputStream
        val bitmap = imageBitmap.asAndroidBitmap()
        try {
            fileOutputStream = FileOutputStream(File(context.filesDir, path))
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    fun getInternalProfilePicture(
        context: Context,
        onSuccess: (ImageBitmap) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        getInternalImageBitMap(
            context,
            R.string.default_profile_picture_path.toString(),
            onSuccess,
            onFailure
        )
    }

    fun setInternalProfilePicture(
        context: Context,
        imageBitmap: ImageBitmap,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        setInternalImageBitMap(
            context,
            R.string.default_profile_picture_path.toString(),
            imageBitmap,
            onSuccess,
            onFailure
        )
    }
}
