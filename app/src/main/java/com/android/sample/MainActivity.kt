package com.android.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import com.android.sample.resources.C
import com.android.sample.ui.map.MapScreen
import com.android.sample.ui.theme.SampleAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().semantics { testTag = C.Tag.main_screen_container },
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapScreen(this) // Pass context for permission and location handling
                }
            }
        }
    }
}
