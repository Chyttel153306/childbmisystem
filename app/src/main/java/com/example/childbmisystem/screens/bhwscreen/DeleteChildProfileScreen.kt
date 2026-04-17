package com.example.childbmisystem.screens.bhwscreen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.childbmisystem.data.AppData
import com.example.childbmisystem.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteChildProfileScreen(
    navController: NavController,
    childId: String
) {
    val child = AppData.getChild(childId)

    if (child == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Child not found.")
        }
        return
    }

    var isLoading by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val documentCount = child.bmiHistory.count { it.evidenceUrl.isNotBlank() || it.photoUrl.isNotBlank() }
    val backgroundColor = Color(0xFFF8F8F8)
    val red = Color(0xFFFF3B30)
    val lightRed = Color(0xFFFFF1F1)
    val borderRed = Color(0xFFFFD6D6)
    val noteBg = Color(0xFFFFFBEA)
    val noteBorder = Color(0xFFFFE082)
    val grayText = Color(0xFF7A7A7A)
    val cardBorder = Color(0xFFEAEAEA)

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Child Profile", color = Color.Black, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this child profile?", color = Color.Black) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            try {
                                isLoading = true
                                AppData.deleteChild(childId)
                                Toast.makeText(context, "Successfully deleted.", Toast.LENGTH_SHORT).show()
                                navController.navigate(Routes.BHW_DASHBOARD) {
                                    popUpTo(Routes.BHW_DASHBOARD) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    e.message ?: "Failed to delete child profile.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = red)
                ) {
                    Text("Yes", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("No", color = Color.Black)
                }
            },
            containerColor = Color.White
        )
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Delete Child Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Remove from monitoring",
                            fontSize = 14.sp,
                            color = grayText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = lightRed),
                border = BorderStroke(1.dp, borderRed)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(red, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Warning",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color(0xFFB71C1C)
                            )
                            Text(
                                text = "Cannot be undone",
                                fontSize = 15.sp,
                                color = Color(0xFFC62828)
                            )
                        }
                    }

                    Text(
                        text = "All health records and BMI history will be permanently removed.",
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Delete Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF5B8CFF),
                                            Color(0xFF7B4DFF)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonOutline,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = child.fullName,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "Child Name",
                                fontSize = 16.sp,
                                color = grayText
                            )
                        }
                    }
                }
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Data to be Deleted",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    DeleteDataRow("Health Records", "${child.bmiHistory.size}")
                    DividerLine()
                    DeleteDataRow("BMI History", "${child.bmiHistory.size}")
                    DividerLine()
                    DeleteDataRow("Status Alerts", "${AppData.alerts.count { it.childId == childId }}")
                    DividerLine()
                    DeleteDataRow("Documents", "$documentCount")
                }
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = noteBg),
                border = BorderStroke(1.dp, noteBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Note:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF7B6000)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Consider updating status instead of deleting.",
                        fontSize = 16.sp,
                        color = Color(0xFF7B6000)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(1.dp, cardBorder)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = red,
                        contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DeleteDataRow(label: String, count: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color(0xFF333333)
        )
        Text(
            text = count,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
private fun DividerLine() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = Color(0xFFEEEEEE),
        thickness = 1.dp
    )
}
