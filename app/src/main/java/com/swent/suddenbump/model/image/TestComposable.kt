package com.swent.suddenbump.model.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.swent.suddenbump.model.user.UserViewModel

@Composable
fun TestComposableScreen(userViewModel: UserViewModel) {
    val context = LocalContext.current

    var mutableImageBitmap = remember { mutableStateOf(ImageBitmap(100, 100)) }

    LaunchedEffect(userViewModel.hasProfilePictureChanged()) {
        userViewModel.getProfilePicture(context, onSuccess = {
            mutableImageBitmap.value = it
        }, onFailure = {
            Log.i("Main", "FAILED UPDATE IN LAUNCHED EFFECT")
        })
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            val imageBitmap = generateBitmap()
            userViewModel.setProfilePicture(context = context, imageBitmap = imageBitmap, onSuccess = {
                Log.i("Main", imageBitmap.toString())
            }, onFailure = {
                Log.i("Main", "FAILED!!")
                Log.i("Main", it.toString())
            })
        }) { Text("Generate new ImageBitmap") }

        Image(bitmap = mutableImageBitmap.value, contentDescription = null)
    }
}

fun generateBitmap(): ImageBitmap {
    val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        setARGB(
            (color.alpha * (0..255).random()).toInt(),
            (color.red * (0..255).random()).toInt(),
            (color.green * (0..255).random()).toInt(),
            (color.blue * (0..255).random()).toInt()
        )
    }
    canvas.drawRect(0f, 0f, 100f, 100f, paint)

    return bitmap.asImageBitmap()
}