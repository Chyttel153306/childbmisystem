package com.example.childbmisystem.screens.parentscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.childbmisystem.data.AppData
import com.example.childbmisystem.data.BmiRecord
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildHistoryScreen(navController: NavController, childId: String) {

    val child = AppData.getChild(childId)

    if (child == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Child not found.", color = Color.Black)
        }
        return
    }

    val history = child.bmiHistory.reversed()
    val greenStatus = Color(0xFF00C853)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Child History", fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(
                            "${child.fullName} • ${child.ageYears} yrs old",
                            fontSize = 12.sp,
                            color = Color.Black
                        )
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📈", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Total Records", fontSize = 12.sp, color = Color.Black)
                            Text(
                                "${history.size}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = greenStatus
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "BMI Tracking History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (history.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No history records yet.", color = Color.Black)
                    }
                }
            } else {
                items(history) { record ->
                    HistoryCard(record)
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(record: BmiRecord) {
    val sc = statusColor(record.status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.DateRange,
                        null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(record.date, fontWeight = FontWeight.SemiBold, color = Color.Black)
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = sc.copy(alpha = 0.15f)
                ) {
                    Text(
                        record.status,
                        color = sc,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFEEEEEE))
            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RecordStat("Height", "${record.heightCm} cm")
                RecordStat("Weight", "${record.weightKg} kg")
                RecordStat("BMI", "${record.bmi}")
            }

            if (record.notes.isNotBlank() || record.recordedBy.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                if (record.notes.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Notes:", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        Text(record.notes, fontSize = 12.sp, color = Color.Black)
                    }
                }
                if (record.recordedBy.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("By:", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        Text(record.recordedBy, fontSize = 12.sp, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 11.sp, color = Color.Black)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
    }
}

private fun statusColor(status: String) = when (status) {
    "Normal" -> Color(0xFF4CAF50)
    "Overweight" -> Color(0xFFFF9800)
    "Underweight" -> Color(0xFFF44336)
    "Obese" -> Color(0xFF9C27B0)
    else -> Color.Gray
}