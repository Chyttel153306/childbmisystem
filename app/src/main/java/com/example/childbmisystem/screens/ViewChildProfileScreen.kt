package com.example.childbmisystem.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.childbmisystem.data.AppData
import com.example.childbmisystem.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewChildProfileScreen(navController: NavController, childId: String) {

    val child = AppData.getChild(childId)

    if (child == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Child not found.", color = Color.Black)
        }
        return
    }

    val latest = child.latestBmi
    val statusColor = statusColor(child.bmiStatus)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Child Profile", fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("View information", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Header card
            Card(shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(56.dp).background(Color(0xFFEDE7F6), CircleShape),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccountCircle, null,
                            tint = Color(0xFF7E57C2), modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        // Color the name of the child black
                        Text(child.fullName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                        Text("${child.ageYears} years old • ${child.gender}",
                            fontSize = 13.sp, color = Color.Black)
                        Spacer(Modifier.height(4.dp))
                        Surface(shape = RoundedCornerShape(20.dp),
                            color = statusColor.copy(alpha = 0.15f)) {
                            Text(child.bmiStatus, color = statusColor, fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                }
            }

            // Born
            Card(shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, tint = Color.Gray,
                        modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Born: ${child.dateOfBirth}", fontSize = 14.sp, color = Color.Black)
                }
            }

            // Current Measurements
            if (latest != null) {
                Text("Current Measurements", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                Card(shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MeasurementItem("📏", "Height", "${latest.heightCm} cm")
                        MeasurementItem("⚖️", "Weight", "${latest.weightKg} kg")
                        MeasurementItem("📊", "BMI", "${latest.bmi}")
                    }
                }
            }

            // BMI History (last 3)
            Text("BMI History", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
            Card(shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)) {
                Column(Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (child.bmiHistory.isEmpty()) {
                        Text("No records yet.", color = Color.Black)
                    } else {
                        child.bmiHistory.takeLast(3).reversed().forEachIndexed { index, rec ->
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(rec.date, fontSize = 13.sp, color = Color.Black)
                                    Text(rec.status, fontSize = 13.sp,
                                        color = statusColor(rec.status))
                                }
                                Text("${rec.bmi}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                            }
                            if (index < child.bmiHistory.takeLast(3).size - 1) {
                                HorizontalDivider(
                                    color = Color(0xFFEEEEEE),
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }
            }

            // Action buttons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Update Button: Text is Black by default, White color is handled by contentColor in M3
                Button(
                    onClick = { navController.navigate(Routes.updateChild(childId)) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3),
                        contentColor = Color.Black // Black text normally, Ripple/State handles tap color
                    )
                ) {
                    Text("Update", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { navController.navigate(Routes.childHistory(childId)) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black // Black text when not tapped
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Text("History", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MeasurementItem(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = Color.Gray)
        // Make the numbers black
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
    }
}

fun statusColor(status: String) = when (status) {
    "Normal"      -> Color(0xFF4CAF50)
    "Overweight"  -> Color(0xFFFF9800)
    "Underweight" -> Color(0xFFF44336)
    "Obese"       -> Color(0xFF9C27B0)
    else          -> Color.Gray
}