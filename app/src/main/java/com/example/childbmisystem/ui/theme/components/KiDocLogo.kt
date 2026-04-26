package com.example.childbmisystem.ui.theme.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun KiDocLogo(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    heartSize: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(Color(0xFF00C8FF), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Favorite,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(heartSize)
        )
    }
}
