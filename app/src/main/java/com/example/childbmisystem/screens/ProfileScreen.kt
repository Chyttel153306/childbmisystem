package com.example.childbmisystem.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.childbmisystem.navigation.Routes

// ─── Shared Profile Screen ────────────────────────────────────────────────────

@Composable
fun ProfileScreen(navController: NavController) {
    val user  = AppData.currentUser.value
    val isBhw = user?.role?.equals("BHW", ignoreCase = true) == true

    val children = AppData.children.let { list ->
        if (isBhw) list else list.filter { it.parentId == user?.id }
    }

    var notificationsEnabled by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    val displayName    = user?.fullName?.ifBlank { if (isBhw) "Maria C. Reyes" else "Ana D. Santos" }
        ?: if (isBhw) "Maria C. Reyes" else "Ana D. Santos"
    val displayAddress = user?.address?.ifBlank { if (isBhw) "Brgy. San Jose, Manila" else "Brgy. Santa Cruz, QC" }
        ?: if (isBhw) "Brgy. San Jose, Manila" else "Brgy. Santa Cruz, QC"

    // Role pill
    val roleLabel = if (isBhw) "Barangay Health Worker" else "Parent / Guardian"
    val roleIcon  = if (isBhw) Icons.Default.HomeWork   else Icons.Default.People
    val pillBg    = if (isBhw) Color(0xFFE0EAFF)        else Color(0xFFF5F3FF)
    val pillText  = if (isBhw) Color(0xFF444CE7)         else Color(0xFF7C3AED)

    // Stats row
    val stats = if (isBhw) {
        listOf(
            Triple("CHILDREN", children.size.toString(), "👶"),
            Triple("BHW ID",   "BHW-0091",               "📋"),
            Triple("SINCE",    "2021",                    "📅")
        )
    } else {
        listOf(
            Triple("CHILDREN", children.size.toString(), "👶"),
            Triple("ROLE",     "Parent",                  "🏥"),
            Triple("SINCE",    "2024",                    "📅")
        )
    }

    Scaffold(
        containerColor = AppBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick  = { navController.popBackStack() },
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, AppCardBorder, RoundedCornerShape(12.dp))
                        .background(AppCardBg, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(20.dp))
                }

                Text("My Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppTextPrimary)

                IconButton(
                    onClick  = { },
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, AppCardBorder, RoundedCornerShape(12.dp))
                        .background(AppCardBg, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = AppTextSecondary)
                }
            }
        },
        bottomBar = {
            ProfileBottomBar(
                isBhw       = isBhw,
                currentRoute = if (isBhw) Routes.BHW_PROFILE else Routes.PARENT_PROFILE,
                onHomeClick = {
                    val home = if (isBhw) Routes.BHW_DASHBOARD else Routes.PARENT_DASHBOARD
                    navController.navigate(home) { launchSingleTop = true }
                },
                onAddClick  = { navController.navigate(Routes.CREATE_CHILD) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {

            // ── Avatar Card ──────────────────────────────────────────────────
            Card(
                shape    = RoundedCornerShape(24.dp),
                colors   = CardDefaults.cardColors(containerColor = AppCardBg),
                modifier = Modifier.fillMaxWidth().border(1.dp, AppCardBorder, RoundedCornerShape(24.dp))
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF444CE7), modifier = Modifier.size(40.dp))
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF444CE7))
                                .border(2.dp, AppCardBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isBhw) Icons.Default.Add else Icons.Default.People,
                                contentDescription = null,
                                tint     = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(displayName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppTextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(shape = RoundedCornerShape(50.dp), color = pillBg) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(roleIcon, contentDescription = null, tint = pillText, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(roleLabel, color = pillText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(displayAddress, color = AppTextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Stats Row ────────────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                stats.forEach { (title, value, emoji) ->
                    Card(
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(containerColor = AppCardBg),
                        modifier = Modifier.weight(1f).height(100.dp).border(1.dp, AppCardBorder, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(emoji, fontSize = 20.sp)
                            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppTextPrimary)
                            Text(title, fontSize = 10.sp, color = AppTextSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Menu Card ────────────────────────────────────────────────────
            Card(
                shape    = RoundedCornerShape(24.dp),
                colors   = CardDefaults.cardColors(containerColor = AppCardBg),
                modifier = Modifier.fillMaxWidth().border(1.dp, AppCardBorder, RoundedCornerShape(24.dp))
            ) {
                Column {
                    ProfileMenuItem(Icons.Default.Edit, Color(0xFFEEF2FF), "Edit Profile", "Update your info") { }
                    ProfileDivider()
                    ProfileMenuSwitchItem(
                        icon            = Icons.Default.Notifications,
                        iconBg          = Color(0xFFECFDF3),
                        title           = "Notifications",
                        subtitle        = if (isBhw) "Alerts & reminders" else "Health alerts",
                        checked         = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                    ProfileDivider()
                    ProfileMenuItem(Icons.Default.Lock, Color(0xFFFFF7ED), "Change Password", "Security settings") { }
                    ProfileDivider()
                    if (isBhw) {
                        ProfileMenuItem(Icons.Default.BarChart, Color(0xFFF5F3FF), "My Reports", "Submitted records") { }
                    } else {
                        ProfileMenuItem(Icons.Default.Face, Color(0xFFF5F3FF), "My Children", "${children.size} registered") {
                            navController.navigate(Routes.PARENT_DASHBOARD) { launchSingleTop = true }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Log Out ──────────────────────────────────────────────────────
            OutlinedButton(
                onClick  = {
                    AppData.logout()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape    = RoundedCornerShape(18.dp),
                border   = BorderStroke(1.5.dp, Color(0xFFFFE4E4)),
                colors   = ButtonDefaults.outlinedButtonColors(
                    containerColor = AppCardBg,
                    contentColor   = Color(0xFFD92D20)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// ─── Profile Bottom Bar ───────────────────────────────────────────────────────

@Composable
private fun ProfileBottomBar(
    isBhw: Boolean,
    currentRoute: String,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit
) {
    NavigationBar(containerColor = AppCardBg, tonalElevation = 0.dp) {
        NavigationBarItem(
            selected = false,
            onClick  = onHomeClick,
            icon     = { Icon(Icons.Default.Home, contentDescription = null) },
            label    = { Text("Home") },
            colors   = AppNavColors()
        )
        NavigationBarItem(
            selected = false,
            onClick  = onAddClick,
            icon     = { Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(26.dp)) },
            label    = { Text("Add") },
            colors   = AppNavColors()
        )
        NavigationBarItem(
            selected = true,
            onClick  = { },
            icon     = { Icon(Icons.Default.Person, contentDescription = null) },
            label    = { Text("Profile") },
            colors   = AppNavColors()
        )
    }
}

// ─── Shared Menu Components ───────────────────────────────────────────────────

@Composable
fun ProfileMenuItem(icon: ImageVector, iconBg: Color, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AppTextPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title,    fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTextPrimary)
            Text(subtitle, fontSize = 12.sp, color = AppTextSecondary)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFD0D5DD))
    }
}

@Composable
fun ProfileMenuSwitchItem(icon: ImageVector, iconBg: Color, title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AppTextPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title,    fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTextPrimary)
            Text(subtitle, fontSize = 12.sp, color = AppTextSecondary)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedTrackColor = AppGreen))
    }
}

@Composable
fun ProfileDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = AppCardBorder)
}