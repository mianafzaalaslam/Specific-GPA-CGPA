package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GpaViewModel
import com.example.ui.MainScreen
import com.example.ui.theme.SpecificGpaTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      SpecificGpaTheme {
        val gpaViewModel: GpaViewModel = viewModel()
        MainScreen(viewModel = gpaViewModel)
      }
    }
  }
}
