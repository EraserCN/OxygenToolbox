package org.eraser.oostools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.eraser.oostools.ui.theme.OxygenToolboxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OxygenToolboxTheme {
                MainScreen()
            }
        }
    }
}
