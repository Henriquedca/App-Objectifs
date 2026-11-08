package com.henrique.objectifs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.henrique.objectifs.ui.App
import com.henrique.objectifs.ui.ThemeObjectifs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThemeObjectifs {
                App()
            }
        }
    }
}
