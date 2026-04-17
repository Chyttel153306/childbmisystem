package com.example.childbmisystem.screens.parentscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.childbmisystem.data.AppData
import com.example.childbmisystem.data.Child
import com.example.childbmisystem.data.StatusAlert
import com.example.childbmisystem.navigation.Routes
import com.example.childbmisystem.ui.theme.components.AppBg
import com.example.childbmisystem.ui.theme.components.AppCardBg
import com.example.childbmisystem.ui.theme.components.AppCardBorder
import com.example.childbmisystem.ui.theme.components.AppEmptyState
import com.example.childbmisystem.ui.theme.components.AppGreen
import com.example.childbmisystem.ui.theme.components.AppPurpleEnd
import com.example.childbmisystem.ui.theme.components.AppPurpleStart
import com.example.childbmisystem.ui.theme.components.AppSummaryCard
import com.example.childbmisystem.ui.theme.components.AppTextPrimary
import com.example.childbmisystem.ui.theme.components.AppTextSecondary
import com.example.childbmisystem.ui.theme.components.appNavColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(navController: NavController) {
    val currentUser = AppData.currentUser.value
    val children = AppData.children.filter { it.parentId == currentUser?.id }

    val parentAlerts by remember(currentUser?.id) {
        derivedStateOf { AppData.getAlertsForCurrentParent() }
    }

    var showNotifications by remember { mutableStateOf(false) }

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
                if (parentAlerts.isEmpty()) {
                    Text("No notifications yet.", color = Color.Black)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        parentAlerts.forEach { alert ->
                            ParentAlertCard(alert = alert)
                        }
                    }
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
                    Column {
                        Text(
                            text = "Hello ${currentUser?.fullName ?: "Parent"}.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = AppTextPrimary
                        )
                        Text(
                            text = "View your child's health status.",
                            color = AppTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showNotifications = true }) {
                        BadgedBox(
                            badge = {
                                if (parentAlerts.isNotEmpty()) {
                                    Badge {
                                        Text(parentAlerts.size.toString())
                                    }
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
            ParentCommonBottomBar(navController, Routes.PARENT_DASHBOARD)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AppSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Children",
                    value = children.size.toString(),
                    icon = Icons.Default.ChildCare,
                    iconBg = Color(0xFFE8F1FF),
                    iconColor = Color(0xFF3B82F6)
                )

                AppSummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Needs Attention",
                    value = children.count {
                        it.bmiStatus != "Normal" && it.bmiStatus != "No Data"
                    }.toString(),
                    icon = Icons.Default.Warning,
                    iconBg = Color(0xFFFFECE8),
                    iconColor = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { navController.navigate(Routes.CREATE_CHILD) },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Child Profile",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "My Children",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AppTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (children.isEmpty()) {
                AppEmptyState("No children added yet")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(children) { child ->
                        ParentChildItemCard(
                            child = child,
                            onViewClick = { navController.navigate(Routes.childHistory(child.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParentAlertCard(alert: StatusAlert) {
    val child = AppData.getChild(alert.childId)

    val alertColor = when (alert.alertType) {
        "Critical" -> Color(0xFFF44336)
        "Warning" -> Color(0xFFFF9800)
        "Information" -> Color(0xFF2196F3)
        "Checkup Due" -> Color(0xFF9C27B0)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Message:",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = alert.message.ifBlank { "No message provided." },
                    fontSize = 13.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "From: ${alert.sentBy} - ${alert.date}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun ParentCommonBottomBar(navController: NavController, currentRoute: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppCardBg)
    ) {
        NavigationBar(
            containerColor = AppCardBg,
            tonalElevation = 0.dp
        ) {
            NavigationBarItem(
                selected = currentRoute == Routes.PARENT_DASHBOARD,
                onClick = { navController.navigate(Routes.PARENT_DASHBOARD) },
                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                label = { Text("Home") },
                colors = appNavColors()
            )

            Spacer(modifier = Modifier.weight(1f))

            NavigationBarItem(
                selected = currentRoute == Routes.PARENT_PROFILE,
                onClick = { navController.navigate(Routes.PARENT_PROFILE) },
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                label = { Text("Profile") },
                colors = appNavColors()
            )
        }

        FloatingActionButton(
            onClick = { navController.navigate(Routes.CREATE_CHILD) },
            shape = CircleShape,
            containerColor = AppGreen,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
                .size(60.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun ParentChildItemCard(
    child: Child,
    onViewClick: () -> Unit
) {
    val pronoun = if (child.gender.equals("Male", ignoreCase = true)) "He" else "She"

    val (msg, color, bg) = when (child.bmiStatus) {
        "Normal" -> Triple(
            "$pronoun is in a healthy range.",
            Color(0xFF2196F3),
            Color(0xFFE3F2FD)
        )
        "Obese" -> Triple(
            "$pronoun needs monitoring.",
            Color(0xFFF44336),
            Color(0xFFFFEBEE)
        )
        else -> Triple(
            "$pronoun may need attention.",
            Color(0xFFFFA000),
            Color(0xFFFFF3E0)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppCardBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(AppPurpleStart, AppPurpleEnd))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (child.photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = child.photoUrl,
                            contentDescription = child.fullName,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = child.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${child.ageYears} yrs",
                        fontSize = 13.sp
                    )

                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(bg)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = color
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            color = color,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row {
                Button(
                    onClick = onViewClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppGreen)
                ) {
                    Text("View", color = Color.White)
                }
            }
        }
    }
}
