package com.phantomcode.v2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.phantomcode.v2.ui.PhantomV2App
import com.phantomcode.v2.vm.LinuxRuntimeController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val runtime = remember { LinuxRuntimeController(applicationContext) }
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PhantomV2App(runtime)
                }
            }
        }
    }
}
