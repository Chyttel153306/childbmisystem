package com.example.childbmisystem.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.childbmisystem.data.AppData
import com.example.childbmisystem.data.Child
import com.example.childbmisystem.navigation.Routes

// ─── Parent Dashboard ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(navController: NavController) {
    val currentUser = AppData.currentUser.value
    val children    = AppData.children.filter { it.parentId == currentUser?.id }

    Scaffold(
        containerColor = AppBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBg),
                title = {
                    Column {
                        Text(
                            text       = "Hello Parent.",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 22.sp,
                            color      = AppTextPrimary
                        )
                        Text(
                            text     = "View your child's health status.",
                            color    = AppTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            )
        },
        bottomBar = {
            ParentCommonBottomBar(
                navController = navController,
                currentRoute  = Routes.PARENT_DASHBOARD
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
                    value     = children.count {
                        it.bmiStatus != "Normal" && it.bmiStatus != "No Data"
                    }.toString(),
                    icon      = Icons.Default.Warning,
                    iconBg    = Color(0xFFFFECE8),
                    iconColor = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ── Create Child Button ──────────────────────────────────────────
            Button(
                onClick        = { navController.navigate(Routes.CREATE_CHILD) },
                shape          = RoundedCornerShape(18.dp),
                colors         = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                modifier       = Modifier.fillMaxWidth().height(62.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF58D39B), Color(0xFF7FD58C))),
                            RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Child Profile", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("My Children", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppTextPrimary)

            Spacer(modifier = Modifier.height(12.dp))

            // ── Children List ────────────────────────────────────────────────
            if (children.isEmpty()) {
                AppEmptyState(message = "No children added yet")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding      = PaddingValues(bottom = 24.dp)
                ) {
                    items(children) { child ->
                        ParentChildItemCard(
                            child          = child,
                            onViewClick    = { navController.navigate(Routes.childHistory(child.id)) },
                            onHistoryClick = { navController.navigate(Routes.childHistory(child.id)) }
                        )
                    }
                }
            }
        }
    }
}

// ─── Parent Bottom Bar ────────────────────────────────────────────────────────

@Composable
fun ParentCommonBottomBar(navController: NavController, currentRoute: String) {
    Box(modifier = Modifier.fillMaxWidth().background(AppCardBg)) {
        NavigationBar(containerColor = AppCardBg, tonalElevation = 0.dp) {
            NavigationBarItem(
                selected = currentRoute == Routes.PARENT_DASHBOARD,
                onClick  = { navController.navigate(Routes.PARENT_DASHBOARD) { launchSingleTop = true } },
                icon     = { Icon(Icons.Default.Home, contentDescription = null) },
                label    = { Text("Home") },
                colors   = AppNavColors()
            )
            Spacer(Modifier.weight(1f))
            NavigationBarItem(
                selected = currentRoute == Routes.PARENT_PROFILE,
                onClick  = { navController.navigate(Routes.PARENT_PROFILE) { launchSingleTop = true } },
                icon     = { Icon(Icons.Default.Person, contentDescription = null) },
                label    = { Text("Profile") },
                colors   = AppNavColors()
            )
        }

        // FAB centered in the bottom bar
        FloatingActionButton(
            onClick        = { navController.navigate(Routes.CREATE_CHILD) },
            shape          = CircleShape,
            containerColor = AppGreen,
            contentColor   = Color.White,
            modifier       = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
                .size(60.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(30.dp))
        }
    }
}

// ─── Parent Child Card ────────────────────────────────────────────────────────

@Composable
fun ParentChildItemCard(
    child: Child,
    onViewClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val bmi     = child.latestBmi
    val pronoun = if (child.gender.equals("Male", ignoreCase = true)) "He" else "She"

    val (msg, color, bg) = when (child.bmiStatus) {
        "Normal" -> Triple("$pronoun is in a healthy range.", Color(0xFF2196F3), Color(0xFFE3F2FD))
        "Obese"  -> Triple("$pronoun needs monitoring.",      Color(0xFFF44336), Color(0xFFFFEBEE))
        else     -> Triple("$pronoun may need attention.",    Color(0xFFFFA000), Color(0xFFFFF3E0))
    }

    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, AppCardBorder, RoundedCornerShape(20.dp)),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = AppCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(AppPurpleStart, AppPurpleEnd))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(child.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTextPrimary)
                    Text(
                        "${child.ageYears} yrs | H: ${bmi?.heightCm ?: "--"}cm W: ${bmi?.weightKg ?: "--"}kg",
                        color    = AppTextSecondary,
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
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(msg, color = color, fontSize = 12.sp)
                    }
                }
            }

            Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick  = onViewClick,
                    modifier = Modifier.weight(1f),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = AppGreen)
                ) { Text("View", color = Color.White) }

                IconButton(
                    onClick  = onHistoryClick,
                    modifier = Modifier.border(1.dp, AppCardBorder, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.History, contentDescription = null, tint = AppTextSecondary)
                }
            }
        }
    }
}