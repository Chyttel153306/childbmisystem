package com.example.childbmisystem.screens.commonscreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.childbmisystem.data.AppData
import com.example.childbmisystem.navigation.Routes
import com.example.childbmisystem.ui.theme.components.AppBg
import com.example.childbmisystem.ui.theme.components.AppCardBg
import com.example.childbmisystem.ui.theme.components.AppCardBorder
import com.example.childbmisystem.ui.theme.components.AppTextPrimary
import com.example.childbmisystem.ui.theme.components.AppTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController) {
    val user = AppData.currentUser.value
    val isBhw = user?.role.equals("BHW", ignoreCase = true)

    val displayName = user?.fullName?.ifBlank { if (isBhw) "Maria C. Reyes" else "Ana D. Santos" }
        ?: if (isBhw) "Maria C. Reyes" else "Ana D. Santos"
    val displayAddress = user?.address?.ifBlank { if (isBhw) "Brgy. San Jose, Manila" else "Brgy. Santa Cruz, QC" }
        ?: if (isBhw) "Brgy. San Jose, Manila" else "Brgy. Santa Cruz, QC"

    val pillBg = if (isBhw) Color(0xFFE0EAFF) else Color(0xFFF5F3FF)
    val pillText = if (isBhw) Color(0xFF444CE7) else Color(0xFF7C3AED)
    val roleLabel = if (isBhw) "Barangay Health Worker" else "Parent / Guardian"
    val roleIcon = if (isBhw) Icons.Default.HomeWork else Icons.Default.Person

    val scrollState = rememberScrollState()
    var isLoggingOut by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            containerColor = AppBg,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .systemBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(44.dp)
                            .border(1.dp, AppCardBorder, RoundedCornerShape(12.dp))
                            .background(AppCardBg, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        "My Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTextPrimary
                    )

                    Spacer(modifier = Modifier.size(44.dp))
                }
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isLoggingOut = true
                                delay(500) // simulate network delay
                                AppData.logout()
                                isLoggingOut = false
                                navController.navigate(Routes.LOGIN) { popUpTo(0) }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFFFE4E4)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = AppCardBg,
                            contentColor = Color(0xFFD92D20)
                        ),
                        enabled = !isLoggingOut
                    ) {
                        if (isLoggingOut) {
                            CircularProgressIndicator(
                                color = Color.Red,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Log Out",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = AppCardBg),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppCardBorder, RoundedCornerShape(24.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF444CE7),
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                displayName,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(shape = RoundedCornerShape(50.dp), color = pillBg) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(roleIcon, contentDescription = null, tint = pillText, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        roleLabel,
                                        color = pillText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFF44336),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(displayAddress, color = AppTextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}