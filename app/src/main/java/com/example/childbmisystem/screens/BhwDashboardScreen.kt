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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.childbmisystem.data.AppData
import com.example.childbmisystem.data.Child
import com.example.childbmisystem.navigation.Routes

// ─── Shared Design Tokens (same across ALL screens) ───────────────────────────
val AppBg           = Color(0xFFF7F8F6)
val AppCardBg       = Color.White
val AppCardBorder   = Color(0xFFE6E8E6)
val AppTextPrimary  = Color(0xFF111827)
val AppTextSecondary = Color(0xFF6B7280)
val AppGreen        = Color(0xFF47B881)
val AppPurpleStart  = Color(0xFF5B7CFA)
val AppPurpleEnd    = Color(0xFF9B4DFF)
val AppPurpleAvatar = Color(0xFF7C4DFF)

// ─── BHW Dashboard ────────────────────────────────────────────────────────────

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
                        Text(
                            text       = "Hello BHW.",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 22.sp,
                            color      = AppTextPrimary
                        )
                        Text(
                            text     = "Manage child health records and alerts.",
                            color    = AppTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            )
        },
        bottomBar = {
            BhwCommonBottomBar(
                navController = navController,
                currentRoute  = Routes.BHW_DASHBOARD
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            // ── Summary Cards ────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
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

            // ── List Header ──────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = "Children List",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                    color      = AppTextPrimary
                )
                Button(
                    onClick        = { navController.navigate(Routes.CREATE_CHILD) },
                    colors         = ButtonDefaults.buttonColors(containerColor = AppGreen),
                    shape          = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontSize = 14.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── List ─────────────────────────────────────────────────────────
            if (children.isEmpty()) {
                AppEmptyState(message = "No children added yet")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding      = PaddingValues(bottom = 24.dp)
                ) {
                    items(children) { child ->
                        BhwChildItemCard(
                            child            = child,
                            onViewClick      = { navController.navigate(Routes.viewChild(child.id)) },
                            onUpdateClick    = { navController.navigate(Routes.updateChild(child.id)) },
                            onSendAlertClick = { navController.navigate(Routes.SEND_ALERT) },
                            onDeleteClick    = { navController.navigate(Routes.deleteChild(child.id)) }
                        )
                    }
                }
            }
        }
    }
}

// ─── BHW Bottom Bar ───────────────────────────────────────────────────────────

@Composable
fun BhwCommonBottomBar(navController: NavController, currentRoute: String) {
    NavigationBar(containerColor = AppCardBg, tonalElevation = 0.dp) {
        NavigationBarItem(
            selected = currentRoute == Routes.BHW_DASHBOARD,
            onClick  = { navController.navigate(Routes.BHW_DASHBOARD) { launchSingleTop = true } },
            icon     = { Icon(Icons.Default.Home, contentDescription = null) },
            label    = { Text("Home") },
            colors   = AppNavColors()
        )
        NavigationBarItem(
            selected = false,
            onClick  = { navController.navigate(Routes.CREATE_CHILD) },
            icon     = { Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(26.dp)) },
            label    = { Text("Add") },
            colors   = AppNavColors()
        )
        NavigationBarItem(
            selected = currentRoute == Routes.BHW_PROFILE,
            onClick  = { navController.navigate(Routes.BHW_PROFILE) { launchSingleTop = true } },
            icon     = { Icon(Icons.Default.Person, contentDescription = null) },
            label    = { Text("Profile") },
            colors   = AppNavColors()
        )
    }
}

// ─── BHW Child Card ───────────────────────────────────────────────────────────

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
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(child.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTextPrimary)
                    Text("${child.ageYears} yrs · ${child.gender}", fontSize = 13.sp, color = AppTextSecondary)
                    child.latestBmi?.let {
                        Text("H: ${it.heightCm}cm  W: ${it.weightKg}kg", fontSize = 13.sp, color = AppTextPrimary, fontWeight = FontWeight.Medium)
                    }
                }
                Surface(color = statusColor.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text       = child.bmiStatus,
                        color      = statusColor,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick  = onViewClick,
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(42.dp),
                    border   = BorderStroke(1.dp, AppCardBorder)
                ) { Text("View", color = AppTextPrimary, fontSize = 13.sp) }

                Button(
                    onClick  = onUpdateClick,
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.2f).height(42.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) { Text("Update", color = Color.White, fontSize = 13.sp) }

                IconButton(
                    onClick  = onSendAlertClick,
                    modifier = Modifier.size(42.dp).border(1.dp, AppCardBorder, CircleShape)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFFFA000), modifier = Modifier.size(20.dp))
                }

                IconButton(
                    onClick  = onDeleteClick,
                    modifier = Modifier.size(42.dp).border(1.dp, Color(0xFFFFE4E4), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD92D20), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ─── Shared Components (used by both dashboards + profile) ────────────────────

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
        modifier  = modifier.height(110.dp),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = AppCardBg),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(iconBg),
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
            Text(text = message, color = AppTextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
fun AppNavColors() = NavigationBarItemDefaults.colors(
    selectedIconColor   = AppGreen,
    selectedTextColor   = AppGreen,
    unselectedIconColor = AppTextSecondary,
    unselectedTextColor = AppTextSecondary,
    indicatorColor      = Color.Transparent
)