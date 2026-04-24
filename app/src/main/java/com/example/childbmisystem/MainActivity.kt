package com.example.childbmisystem

import android.os.Bundle
import android.os.Build
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.childbmisystem.navigation.AppNavGraph
import com.example.childbmisystem.ui.theme.ChildbmisystemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ChildbmisystemTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    AppNavGraph()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            findViewById<View>(android.R.id.content)?.scrollCaptureHint =
                View.SCROLL_CAPTURE_HINT_INCLUDE
        }
    }


}
