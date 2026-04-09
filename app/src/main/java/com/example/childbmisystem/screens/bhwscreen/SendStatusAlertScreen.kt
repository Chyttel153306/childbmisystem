package com.example.childbmisystem.screens.bhwscreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendStatusAlertScreen(navController: NavController) {

    var selectedAlertType by remember { mutableStateOf("Warning") }
    var alertMessage by remember { mutableStateOf("") }
    var selectedChildren by remember { mutableStateOf(setOf<String>()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val primaryBlue = Color(0xFF2196F3)
    val lightBlueBanner = Color(0xFFE3F2FD)
    val infoTextColor = Color(0xFF1565C0)
    val amber = Color(0xFFFFA000)

    val alertTypes = listOf(
        Triple("Warning", "Status needs attention", amber),
        Triple("Critical", "Immediate action required", Color(0xFFF44336)),
        Triple("Information", "General update", primaryBlue),
        Triple("Checkup Due", "Schedule appointment", Color(0xFF9C27B0))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Send Status Alert",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                        Text(
                            "Notify parents about health status",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = lightBlueBanner),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = primaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Send alerts to parents via app notification",
                        fontSize = 13.sp,
                        color = infoTextColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                "Health Status",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                alertTypes.chunked(2).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { (type, subtitle, color) ->
                            val selected = selectedAlertType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(75.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) color else Color.White)
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) color else Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        selectedAlertType = type
                                        statusMessage = null
                                    }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column {
                                    Text(
                                        type,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else Color.Black,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        subtitle,
                                        fontSize = 11.sp,
                                        color = if (selected) Color.White.copy(alpha = 0.8f) else Color.Gray,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                "Select Children (${selectedChildren.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppData.children.forEach { child ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        elevation = CardDefaults.cardElevation(0.5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedChildren =
                                        if (child.id in selectedChildren) {
                                            selectedChildren - child.id
                                        } else {
                                            selectedChildren + child.id
                                        }
                                    statusMessage = null
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = child.id in selectedChildren,
                                onCheckedChange = { checked ->
                                    selectedChildren =
                                        if (checked) selectedChildren + child.id
                                        else selectedChildren - child.id
                                    statusMessage = null
                                },
                                colors = CheckboxDefaults.colors(checkedColor = primaryBlue)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = child.fullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Parent: Contact Registered",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Text(
                "Query (Message)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black
            )

            OutlinedTextField(
                value = alertMessage,
                onValueChange = {
                    alertMessage = it
                    statusMessage = null
                },
                placeholder = {
                    Text(
                        "Enter message for parents...",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = primaryBlue,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        scope.launch {
                            statusMessage = null

                            if (selectedChildren.isEmpty()) {
                                isSuccess = false
                                statusMessage = "Please select at least one child."
                                return@launch
                            }

                            if (alertMessage.isBlank()) {
                                isSuccess = false
                                statusMessage = "Please enter a message."
                                return@launch
                            }

                            isLoading = true
                            val success = AppData.sendAlert(
                                selectedChildren.toList(),
                                selectedAlertType,
                                alertMessage
                            )
                            isLoading = false

                            if (success) {
                                isSuccess = true
                                statusMessage = "Alert sent successfully."
                                selectedChildren = emptySet()
                                alertMessage = ""
                                selectedAlertType = "Warning"
                            } else {
                                isSuccess = false
                                statusMessage = "Failed to send alert."
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryBlue,
                        contentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Send",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            statusMessage?.let { message ->
                Text(
                    text = message,
                    color = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}