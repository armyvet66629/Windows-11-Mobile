package com.example.windows11mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import com.example.windows11mobile.ui.home.HomeViewModel

@Composable
fun GlobalBlurLayer(viewModel: HomeViewModel) {
    val blur by viewModel.blurAmount.collectAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .blur(blur)
            .background(
                MaterialTheme.colorScheme.background.copy(alpha = 0.35f)
            )
    )
}
