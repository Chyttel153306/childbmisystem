package com.example.childbmisystem.screens.parentscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.childbmisystem.data.AppData
import com.example.childbmisystem.data.BmiRecord
import com.example.childbmisystem.navigation.Routes
import java.util.Locale

private val ScreenBg = Color(0xFFF2F2F2)
private val CardBg = Color.White
private val GreenCard = Color(0xFF35B558)
private val PurpleChipBg = Color(0xFFF2DDF7)
private val PurpleChipText = Color(0xFFA23AB7)
private val BlackText = Color(0xFF111111)
private val GrayText = Color(0xFF555555)
private val DividerColor = Color(0xFFE3E3E3)
private val DisabledButton = Color(0xFFB9D9C0)

@Composable
fun ChildHistoryScreen(navController: NavController, childId: String) {
    val child = AppData.getChild(childId)

    if (child == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Child not found.", color = BlackText)
        }
        return
    }

    val records = child.bmiHistory

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ScreenBg
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeaderSection(
                    childName = child.fullName,
                    childAge = "${child.ageMonths} Months Old",
                    onBackClick = { navController.popBackStack() }
                )
            }

            item {
                TotalRecordsCard(total = records.size)
            }

            item {
                Text(
                    text = "BMI Tracking History",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlackText
                )
            }

            if (records.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No history records yet.",
                                color = GrayText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                items(records, key = { it.id.ifBlank { "${it.date}-${it.bmi}" } }) { record ->
                    ChildHistoryCard(
                        record = record,
                        fallbackPhotoUrl = child.photoUrl,
                        onViewEvidenceClick = { imageUrl ->
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("evidence_url", imageUrl)
                            navController.navigate(Routes.EVIDENCE_PREVIEW)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun HeaderSection(
    childName: String,
    childAge: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F7F7))
            .padding(horizontal = 10.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier.size(34.dp)
            )
        }

        Column(
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = "Child History",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BlackText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$childName • $childAge",
                fontSize = 15.sp,
                color = BlackText
            )
        }
    }
}

@Composable
private fun TotalRecordsCard(total: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GreenCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.BarChart,
                    contentDescription = "Total records",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column {
                Text(
                    text = "Total Records",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = total.toString(),
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ChildHistoryCard(
    record: BmiRecord,
    fallbackPhotoUrl: String,
    onViewEvidenceClick: (String) -> Unit
) {
    val evidenceUrl = record.evidenceUrl
        .ifBlank { record.photoUrl }
        .ifBlank { fallbackPhotoUrl }
        .trim()
    val hasEvidence = evidenceUrl.isNotBlank()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Date",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = record.date,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BlackText
                )

                Spacer(modifier = Modifier.weight(1f))

                StatusChip(record.status)
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = DividerColor, thickness = 1.dp)

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RecordValue(label = "Height", value = formatMeasurement(record.heightCm, "cm"))
                RecordValue(label = "Weight", value = formatMeasurement(record.weightKg, "kg"))
                RecordValue(label = "BMI", value = formatNumber(record.bmi))
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (record.notes.isNotBlank()) {
                Text(
                    text = "Notes: ${record.notes}",
                    color = BlackText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text(
                text = "By: ${record.recordedBy.ifBlank { "Unknown" }}",
                color = BlackText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { if (hasEvidence) onViewEvidenceClick(evidenceUrl) },
                enabled = hasEvidence,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenCard,
                    contentColor = Color.White,
                    disabledContainerColor = DisabledButton,
                    disabledContentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.RemoveRedEye,
                    contentDescription = "View Evidence",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasEvidence) "View Evidence" else "No Evidence Uploaded",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(PurpleChipBg)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = status,
            color = statusTextColor(status),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RecordValue(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = GrayText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BlackText
        )
    }
}

private fun formatMeasurement(value: Double, unit: String): String {
    return "${formatNumber(value)} $unit"
}

private fun formatNumber(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

private fun statusTextColor(status: String): Color = when (status) {
    "Normal" -> Color(0xFF2E9B50)
    "Overweight" -> Color(0xFFCF7A0A)
    "Underweight" -> Color(0xFFD34141)
    "Obese" -> PurpleChipText
    else -> PurpleChipText
}
