package com.swent.suddenbump.model.image

import androidx.compose.ui.graphics.ImageBitmap

interface ImageRepository {

    fun init(onSuccess: () -> Unit)

    fun downloadImage(path: String, onSuccess: (ImageBitmap) -> Unit, onFailure: (Exception) -> Unit)

    suspend fun downloadImage(path: String): ImageBitmap?

    fun uploadImage(
        imageBitmap: ImageBitmap,
        path: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )
}
