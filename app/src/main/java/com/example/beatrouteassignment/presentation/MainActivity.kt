package com.example.beatrouteassignment.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.beatrouteassignment.presentation.screen.ProductScreen
import com.example.beatrouteassignment.presentation.theme.BeatRouteAssignmentTheme
import com.example.beatrouteassignment.presentation.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: ProductViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { BeatRouteAssignmentTheme { ProductScreen(viewModel) } }
    }
}