package com.example.childbmisystem.ui.theme.components


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Shared colors – define once
val AppBg            = Color(0xFFF7F8F6)
val AppCardBg        = Color.White
val AppCardBorder    = Color(0xFFE6E8E6)
val AppTextPrimary   = Color(0xFF111827)
val AppTextSecondary = Color(0xFF6B7280)
val AppGreen         = Color(0xFF47B881)
val AppPurpleStart   = Color(0xFF5B7CFA)
val AppPurpleEnd     = Color(0xFF9B4DFF)
val AppPurpleAvatar  = Color(0xFF7C4DFF)

@Composable
fun AppSummaryCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppCardBg),
        elevation = CardDefaults.cardElevation(0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppTextPrimary)
                Text(title, fontSize = 12.sp, color = AppTextSecondary)
            }
        }
    }
}

@Composable
fun AppEmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFFD0D5DD), modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, color = AppTextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
fun appNavColors() = NavigationBarItemDefaults.colors(
    selectedIconColor   = AppGreen,
    selectedTextColor   = AppGreen,
    unselectedIconColor = AppTextSecondary,
    unselectedTextColor = AppTextSecondary,
    indicatorColor      = Color.Transparent
)