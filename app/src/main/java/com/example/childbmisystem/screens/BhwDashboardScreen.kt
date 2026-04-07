package com.example.childbmisystem.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.childbmisystem.data.AppData
import com.example.childbmisystem.data.Child
import com.example.childbmisystem.navigation.Routes
import com.example.childbmisystem.ui.theme.components.AppBg
import com.example.childbmisystem.ui.theme.components.AppCardBg
import com.example.childbmisystem.ui.theme.components.AppCardBorder
import com.example.childbmisystem.ui.theme.components.AppEmptyState
import com.example.childbmisystem.ui.theme.components.AppPurpleAvatar
import com.example.childbmisystem.ui.theme.components.AppSummaryCard
import com.example.childbmisystem.ui.theme.components.AppTextPrimary
import com.example.childbmisystem.ui.theme.components.AppTextSecondary
import com.example.childbmisystem.ui.theme.components.appNavColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BhwDashboardScreen(navController: NavController) {
    val children = AppData.children

    Scaffold(
        containerColor = AppBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBg),
                title = {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Text("Hello BHW.", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppTextPrimary)
                        Text("Manage child health records and alerts.", color = AppTextSecondary, fontSize = 13.sp)
                    }
                }
            )
        },
        bottomBar = {
            BhwCommonBottomBar(navController, Routes.BHW_DASHBOARD)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                AppSummaryCard(
                    modifier  = Modifier.weight(1f),
                    title     = "Total Children",
                    value     = children.size.toString(),
                    icon      = Icons.Default.Group,
                    iconBg    = Color(0xFFE8F1FF),
                    iconColor = Color(0xFF3B82F6)
                )
                AppSummaryCard(
                    modifier  = Modifier.weight(1f),
                    title     = "Needs Attention",
                    value     = AppData.childrenNeedingAttention().toString(),
                    icon      = Icons.Default.Warning,
                    iconBg    = Color(0xFFFFECE8),
                    iconColor = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Children List", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            if (children.isEmpty()) {
                AppEmptyState("No children added yet")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(children) { child ->
                        BhwChildItemCard(
                            child = child,
                            onViewClick = { navController.navigate(Routes.viewChild(child.id)) },
                            onUpdateClick = { navController.navigate(Routes.updateChild(child.id)) },
                            onSendAlertClick = { navController.navigate(Routes.SEND_ALERT) },
                            onDeleteClick = { navController.navigate(Routes.deleteChild(child.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BhwCommonBottomBar(navController: NavController, currentRoute: String) {
    NavigationBar(containerColor = AppCardBg, tonalElevation = 0.dp) {
        NavigationBarItem(
            selected = currentRoute == Routes.BHW_DASHBOARD,
            onClick  = { navController.navigate(Routes.BHW_DASHBOARD) { launchSingleTop = true } },
            icon     = { Icon(Icons.Default.Home, contentDescription = null) },
            label    = { Text("Home") },
            colors   = appNavColors()
        )
        NavigationBarItem(
            selected = false,
            onClick  = { navController.navigate(Routes.SEND_ALERT) },
            icon     = { Icon(Icons.Default.NotificationsActive, contentDescription = null) },
            label    = { Text("Alerts") },
            colors   = appNavColors()
        )
        NavigationBarItem(
            selected = currentRoute == Routes.BHW_PROFILE,
            onClick  = { navController.navigate(Routes.BHW_PROFILE) { launchSingleTop = true } },
            icon     = { Icon(Icons.Default.Person, contentDescription = null) },
            label    = { Text("Profile") },
            colors   = appNavColors()
        )
    }
}

@Composable
fun BhwChildItemCard(
    child: Child,
    onViewClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onSendAlertClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val statusColor = when (child.bmiStatus) {
        "Normal"              -> Color(0xFF47B881)
        "Overweight", "Obese" -> Color(0xFFFFA000)
        "Underweight"         -> Color(0xFFF44336)
        else                  -> AppTextSecondary
    }

    Card(
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = AppCardBg),
        modifier = Modifier.fillMaxWidth().border(1.dp, AppCardBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(AppPurpleAvatar),
                    contentAlignment = Alignment.Center
                ) {
                    if (child.photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = child.photoUrl,
                            contentDescription = child.fullName,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(child.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTextPrimary)
                    Text("${child.ageYears} yrs · ${child.gender}", fontSize = 13.sp, color = AppTextSecondary)
                    child.latestBmi?.let { bmi ->
                        Text("H: ${bmi.heightCm}cm  W: ${bmi.weightKg}kg", fontSize = 13.sp, color = AppTextPrimary, fontWeight = FontWeight.Medium)
                    }
                }

                Surface(color = statusColor.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                    Text(child.bmiStatus, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onViewClick, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1f).height(42.dp), border = BorderStroke(1.dp, AppCardBorder)) {
                    Text("View", color = AppTextPrimary, fontSize = 13.sp)
                }
                Button(onClick = onUpdateClick, shape = RoundedCornerShape(10.dp), modifier = Modifier.weight(1.2f).height(42.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))) {
                    Text("Update", color = Color.White, fontSize = 13.sp)
                }
                IconButton(onClick = onSendAlertClick, modifier = Modifier.size(42.dp).border(1.dp, AppCardBorder, CircleShape)) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(42.dp).border(1.dp, Color(0xFFFFE4E4), CircleShape)) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD92D20), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}