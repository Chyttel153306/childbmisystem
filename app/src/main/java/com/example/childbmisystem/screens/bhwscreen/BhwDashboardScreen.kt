package com.example.childbmisystem.screens.bhwscreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.childbmisystem.data.AppData
import com.example.childbmisystem.data.Child
import com.example.childbmisystem.data.FirebaseRepository
import com.example.childbmisystem.data.StatusAlert
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
    val currentUser = AppData.currentUser.value
    val children = AppData.children
    val alerts by remember { derivedStateOf { AppData.alerts } }
    val parentNames = remember { mutableStateMapOf<String, String>() }

    var showNotifications by remember { mutableStateOf(false) }
    var selectedAlert by remember { mutableStateOf<StatusAlert?>(null) }

    LaunchedEffect(children.mapNotNull { it.parentId }.distinct()) {
        children.mapNotNull { it.parentId }
            .distinct()
            .filterNot { parentNames.containsKey(it) }
            .forEach { parentId ->
                parentNames[parentId] = FirebaseRepository.getUser(parentId)?.fullName ?: "Unknown Parent"
            }
    }

    if (showNotifications) {
        AlertDialog(
            onDismissRequest = { showNotifications = false },
            confirmButton = {
                TextButton(onClick = { showNotifications = false }) {
                    Text("Close", color = Color.Black)
                }
            },
            title = {
                Text(
                    text = "Notifications",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                if (alerts.isEmpty()) {
                    Text("No notifications yet.", color = Color.Black)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        alerts.forEach { alert ->
                            BhwAlertCard(
                                alert = alert,
                                parentName = AppData.getChild(alert.childId)
                                    ?.parentId
                                    ?.let { parentNames[it] }
                                    ?: "Unknown Parent",
                                onClick = { selectedAlert = alert }
                            )
                        }
                    }
                }
            },
            containerColor = Color.White
        )
    }

    selectedAlert?.let { alert ->
        val child = AppData.getChild(alert.childId)
        val parentName = child?.parentId?.let { parentNames[it] } ?: "Unknown Parent"

        AlertDialog(
            onDismissRequest = { selectedAlert = null },
            confirmButton = {
                TextButton(onClick = { selectedAlert = null }) {
                    Text("Close", color = Color.Black)
                }
            },
            title = {
                Text(
                    text = "${alert.alertType} Alert Details",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Child: ${child?.fullName ?: "Unknown Child"}", color = Color.Black)
                    Text("Sent to: $parentName", color = Color.Black)
                    Text("Message:", color = Color.Black, fontWeight = FontWeight.SemiBold)
                    Text(alert.message.ifBlank { "No message provided." }, color = Color.DarkGray)
                    Text("Date: ${alert.date}", color = Color.DarkGray)
                    Text("Sent by: ${alert.sentBy}", color = Color.DarkGray)
                }
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        containerColor = AppBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBg),
                title = {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            "Hello ${currentUser?.fullName ?: "BHW"}.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = AppTextPrimary
                        )
                        Text("Manage child health records and alerts.", color = AppTextSecondary, fontSize = 13.sp)
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F1FF))
                            .border(1.dp, AppCardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentUser?.photoUrl?.isNotBlank() == true) {
                            AsyncImage(
                                model = currentUser.photoUrl,
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Photo",
                                tint = AppTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    IconButton(onClick = { showNotifications = true }) {
                        BadgedBox(
                            badge = {
                                if (alerts.isNotEmpty()) {
                                    Badge { Text(alerts.size.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = AppTextPrimary
                            )
                        }
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
                    modifier = Modifier.weight(1f),
                    title = "Total Children",
                    value = children.size.toString(),
                    icon = Icons.Default.Group,
                    iconBg = Color(0xFFE8F1FF),
                    iconColor = Color(0xFF3B82F6)
                )
                AppSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Needs Attention",
                    value = AppData.childrenNeedingAttention().toString(),
                    icon = Icons.Default.Warning,
                    iconBg = Color(0xFFFFECE8),
                    iconColor = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Children List", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            if (children.isEmpty()) {
                AppEmptyState("No children added yet")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(children) { child ->
                        BhwChildItemCard(
                            child = child,
                            onViewClick = { navController.navigate(Routes.viewChild(child.id)) },
                            onUpdateClick = { navController.navigate(Routes.updateChild(child.id)) },
                            onSendAlertClick = { navController.navigate(Routes.sendAlert(child.id)) },
                            onDeleteClick = { navController.navigate(Routes.deleteChild(child.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BhwAlertCard(
    alert: StatusAlert,
    parentName: String,
    onClick: () -> Unit
) {
    val child = AppData.getChild(alert.childId)

    val alertColor = when (alert.alertType) {
        "Critical" -> Color(0xFFF44336)
        "Warning" -> Color(0xFFFF9800)
        "Information" -> Color(0xFF2196F3)
        "Checkup Due" -> Color(0xFF9C27B0)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = alertColor,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${alert.alertType} Alert",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "Child: ${child?.fullName ?: "Unknown Child"}",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = "Sent to: $parentName",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.message.ifBlank { "No message provided." },
                    fontSize = 13.sp,
                    color = Color.Black,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${alert.sentBy} - ${alert.date}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun BhwCommonBottomBar(navController: NavController, currentRoute: String) {
    NavigationBar(containerColor = AppCardBg, tonalElevation = 0.dp) {
        NavigationBarItem(
            selected = currentRoute == Routes.BHW_DASHBOARD,
            onClick = { navController.navigate(Routes.BHW_DASHBOARD) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            colors = appNavColors()
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Routes.sendAlert()) },
            icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) },
            label = { Text("Alerts") },
            colors = appNavColors()
        )
        NavigationBarItem(
            selected = currentRoute == Routes.BHW_PROFILE,
            onClick = { navController.navigate(Routes.BHW_PROFILE) { launchSingleTop = true } },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Profile") },
            colors = appNavColors()
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
        "Normal" -> Color(0xFF007958)
        "Overweight", "Obese" -> Color(0xFFFFA000)
        "Underweight" -> Color(0xFFF44336)
        else -> AppTextSecondary
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppCardBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(AppPurpleAvatar),
                    contentAlignment = Alignment.Center
                ) {
                    if (child.photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = child.photoUrl,
                            contentDescription = child.fullName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(child.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTextPrimary)
                    Text("${child.ageMonthLabel} - ${child.gender}", fontSize = 13.sp, color = AppTextSecondary)
                    child.latestBmi?.let { bmi ->
                        Text(
                            "H: ${bmi.heightCm}cm  W: ${bmi.weightKg}kg",
                            fontSize = 13.sp,
                            color = AppTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Surface(color = statusColor.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        child.bmiStatus,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.OutlinedButton(
                    onClick = onViewClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp),
                    border = BorderStroke(1.dp, AppCardBorder)
                ) {
                    Text("View", color = AppTextPrimary, fontSize = 13.sp)
                }
                Button(
                    onClick = onUpdateClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("Update", color = Color.White, fontSize = 13.sp)
                }
                IconButton(
                    onClick = onSendAlertClick,
                    modifier = Modifier
                        .size(42.dp)
                        .border(1.dp, AppCardBorder, CircleShape)
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .size(42.dp)
                        .border(1.dp, Color(0xFFFFE4E4), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFD92D20),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
