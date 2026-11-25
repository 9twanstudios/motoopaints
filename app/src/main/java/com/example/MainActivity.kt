package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MotoopaintApp
import com.example.ui.theme.MotoopaintTheme
import com.example.ui.viewmodel.MotoopaintViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as MotoopaintApplication).container

        setContent {
            val vm: MotoopaintViewModel = viewModel(
                factory = MotoopaintViewModel.Factory(appContainer)
            )
            val themeMode by vm.themeMode.collectAsStateWithLifecycle()

            MotoopaintTheme(themeMode = themeMode) {
                MotoopaintApp(viewModel = vm)
            }
        }
    }
}
