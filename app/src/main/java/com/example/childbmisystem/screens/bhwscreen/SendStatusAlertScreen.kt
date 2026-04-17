package com.example.childbmisystem.screens.bhwscreen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.childbmisystem.data.AppData
import com.example.childbmisystem.data.FirebaseRepository
import com.example.childbmisystem.data.User
import com.example.childbmisystem.navigation.Routes
import com.example.childbmisystem.ui.theme.components.AppBg
import com.example.childbmisystem.ui.theme.components.AppCardBg
import com.example.childbmisystem.ui.theme.components.AppGreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendStatusAlertScreen(
    navController: NavController,
    preselectedChildId: String? = null
) {
    var selectedAlertType by remember { mutableStateOf("Warning") }
    var alertMessage by remember { mutableStateOf("") }
    var selectedChildren by remember { mutableStateOf(setOf<String>()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var scheduledCheckupAtMillis by remember { mutableStateOf<Long?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val parentNames = remember { mutableStateMapOf<String, String>() }
    val scheduleFormatter = remember {
        SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    }

    val visibleChildren = if (preselectedChildId.isNullOrBlank()) {
        AppData.children.toList()
    } else {
        AppData.children.filter { it.id == preselectedChildId }
    }
    val isSpecificChildAlert = !preselectedChildId.isNullOrBlank()

    LaunchedEffect(preselectedChildId, visibleChildren.size) {
        if (!preselectedChildId.isNullOrBlank() && visibleChildren.any { it.id == preselectedChildId }) {
            selectedChildren = setOf(preselectedChildId)
        }
    }

    LaunchedEffect(visibleChildren.map { it.parentId }) {
        visibleChildren.mapNotNull { it.parentId }
            .distinct()
            .forEach { parentId ->
                if (!parentNames.containsKey(parentId)) {
                    val parent = FirebaseRepository.getUser(parentId)
                    parentNames[parentId] = parent.displayName()
                }
            }
    }

    val lightBlueBanner = Color(0xFFE3F2FD)
    val infoTextColor = Color(0xFF1565C0)
    val amber = Color(0xFFFFA000)

    val alertTypes = listOf(
        Triple("Warning", "Status needs attention", amber),
        Triple("Critical", "Immediate action required", Color(0xFFF44336)),
        Triple("Information", "General update", AppGreen),
        Triple("Checkup Due", "Schedule appointment", Color(0xFF9C27B0))
    )

    Scaffold(
        containerColor = AppBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Send Status Alert",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                        Text(
                            text = if (isSpecificChildAlert) {
                                "Send an alert for this child only"
                            } else {
                                "Notify parents about health status"
                            },
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBg)
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
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AppGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = if (isSpecificChildAlert) {
                            "This alert will only be sent to the selected child's parent."
                        } else {
                            "Select the children whose parents should receive this alert."
                        },
                        fontSize = 13.sp,
                        color = infoTextColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = "Health Status",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                alertTypes.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                        text = type,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Color.White else Color.Black,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = subtitle,
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
                text = "Select Children (${selectedChildren.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (visibleChildren.isEmpty()) {
                    Text(
                        text = "No child available for alert.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                visibleChildren.forEach { child ->
                    val parentName = child.parentId?.let(parentNames::get) ?: "No parent assigned"

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AppCardBg),
                        border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                        elevation = CardDefaults.cardElevation(0.5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isSpecificChildAlert) {
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
                                    if (!isSpecificChildAlert) {
                                        selectedChildren =
                                            if (checked) selectedChildren + child.id
                                            else selectedChildren - child.id
                                        statusMessage = null
                                    }
                                },
                                enabled = !isSpecificChildAlert,
                                colors = CheckboxDefaults.colors(checkedColor = AppGreen)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Column {
                                Text(
                                    text = child.fullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Parent: $parentName",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = child.bmiStatus,
                                    fontSize = 12.sp,
                                    color = statusColor(child.bmiStatus),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            if (selectedAlertType == "Checkup Due") {
                Text(
                    text = "Checkup Schedule",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )

                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        scheduledCheckupAtMillis?.let { calendar.timeInMillis = it }

                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val pickedCalendar = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                }

                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        pickedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                        pickedCalendar.set(Calendar.MINUTE, minute)
                                        pickedCalendar.set(Calendar.SECOND, 0)
                                        pickedCalendar.set(Calendar.MILLISECOND, 0)
                                        scheduledCheckupAtMillis = pickedCalendar.timeInMillis
                                        statusMessage = null
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    false
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AppGreen),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppGreen)
                ) {
                    Text(
                        text = scheduledCheckupAtMillis?.let {
                            scheduleFormatter.format(Date(it))
                        } ?: "Set date and time for checkup",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = "Message",
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
                        text = "Enter message for parents...",
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
                    focusedBorderColor = AppGreen,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black
                )
            )

            Row(
                modifier = Modifier
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
                        containerColor = AppGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Cancel", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        scope.launch {
                            statusMessage = null

                            if (selectedChildren.isEmpty() || alertMessage.isBlank()) {
                                isSuccess = false
                                statusMessage = "Please select child and fill in message."
                                return@launch
                            }

                            if (selectedAlertType == "Checkup Due" && scheduledCheckupAtMillis == null) {
                                isSuccess = false
                                statusMessage = "Please set the checkup date and time."
                                return@launch
                            }

                            val finalMessage = buildString {
                                append(alertMessage.trim())
                                if (selectedAlertType == "Checkup Due" && scheduledCheckupAtMillis != null) {
                                    append("\nCheckup schedule: ")
                                    append(scheduleFormatter.format(Date(scheduledCheckupAtMillis!!)))
                                }
                            }

                            isLoading = true
                            val success = AppData.sendAlert(
                                childIds = selectedChildren.toList(),
                                alertType = selectedAlertType,
                                message = finalMessage
                            )
                            isLoading = false

                            if (success) {
                                isSuccess = true
                                statusMessage = "Alert sent successfully."
                                Toast.makeText(context, "Alert sent successfully.", Toast.LENGTH_SHORT).show()
                                selectedChildren = if (isSpecificChildAlert) {
                                    setOfNotNull(preselectedChildId)
                                } else {
                                    emptySet()
                                }
                                alertMessage = ""
                                selectedAlertType = "Warning"
                                scheduledCheckupAtMillis = null
                                navController.navigate(Routes.BHW_DASHBOARD) {
                                    popUpTo(Routes.BHW_DASHBOARD) { inclusive = false }
                                    launchSingleTop = true
                                }
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
                        containerColor = AppGreen,
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
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = "Send",
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

private fun User?.displayName(): String {
    return when {
        this == null -> "No parent assigned"
        fullName.isNotBlank() -> fullName
        email.isNotBlank() -> email
        else -> "Parent"
    }
}
