package com.example.childbmisystem.screens.bhwscreen

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.childbmisystem.data.AppData
import com.example.childbmisystem.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateChildProfileScreen(navController: NavController, childId: String) {

    val context = LocalContext.current
    val child = AppData.getChild(childId)

    if (child == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Child not found.", color = Color.Black)
        }
        return
    }

    val latest = child.latestBmi

    // Pre-fill age as the child's current month-based age.
    var fullName     by remember { mutableStateOf(child.fullName) }
    var ageMonths    by remember { mutableStateOf(child.ageMonths.toString()) }
    var gender       by remember { mutableStateOf(child.gender) }
    var genderExpanded by remember { mutableStateOf(false) }
    var heightCm     by remember { mutableStateOf(latest?.heightCm?.toString() ?: "") }
    var weightKg     by remember { mutableStateOf(latest?.weightKg?.toString() ?: "") }
    var notes        by remember { mutableStateOf("") }
    var saved        by remember { mutableStateOf(false) }
    var isLoading    by remember { mutableStateOf(false) }
    var selectedEvidenceUri by remember { mutableStateOf<Uri?>(null) }
    var selectedEvidenceFileName by remember { mutableStateOf<String?>(null) }

    val genderOptions = listOf("Male", "Female")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            selectedEvidenceUri = it
            selectedEvidenceFileName = it.displayName(context.contentResolver)
            Toast.makeText(context, "Photo selected", Toast.LENGTH_SHORT).show()
        }
    }

    val scope        = rememberCoroutineScope()
    val primaryBlue  = Color(0xFF2196F3)
    val primaryGreen = Color(0xFF4CAF50)
    val cardBg       = Color(0xFFF5F5F5)

    // ── BMI live calculation ────────────────────────────────────────────────
    val enteredAgeMonths = ageMonths.toIntOrNull() ?: child.ageMonths
    val newBmi = remember(heightCm, weightKg, enteredAgeMonths) {
        val h = heightCm.toDoubleOrNull() ?: 0.0
        val w = weightKg.toDoubleOrNull() ?: 0.0
        if (h > 0 && w > 0) AppData.calculateBmi(h, w, enteredAgeMonths) else 0.0
    }
    val bmiStatusLabel = if (newBmi > 0) AppData.bmiStatus(newBmi, enteredAgeMonths, gender) else "No Data"
    val bmiStatusColor = when (bmiStatusLabel) {
        "Normal"      -> primaryBlue
        "Overweight"  -> Color(0xFFFFA000)
        "Obese"       -> Color(0xFFD32F2F)
        "Underweight" -> Color(0xFF1565C0)
        else          -> primaryBlue
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Update Child Profile", fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Edit child info", fontSize = 12.sp, color = Color.Gray)
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
                .background(Color.White)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Card 1: Child Information ───────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Name
                    Text("Name", fontWeight = FontWeight.SemiBold, color = Color.Black)
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        singleLine = true,
                        placeholder = { Text("Enter name", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = cardBg,
                            unfocusedContainerColor = cardBg,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )

                    // Age (months) + Gender row
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Age in months
                        Column(Modifier.weight(1f)) {
                            Text("Age (months)", fontWeight = FontWeight.SemiBold, color = Color.Black)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = ageMonths,
                                onValueChange = { ageMonths = it },
                                singleLine = true,
                                placeholder = { Text("e.g. 24", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                )
                            )
                        }

                        // Gender dropdown
                        Column(Modifier.weight(1f)) {
                            Text("Gender", fontWeight = FontWeight.SemiBold, color = Color.Black)
                            Spacer(Modifier.height(4.dp))
                            ExposedDropdownMenuBox(
                                expanded = genderExpanded,
                                onExpandedChange = { genderExpanded = !genderExpanded }
                            ) {
                                OutlinedTextField(
                                    value = gender,
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedContainerColor = cardBg,
                                        unfocusedContainerColor = cardBg,
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = genderExpanded,
                                    onDismissRequest = { genderExpanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    genderOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option, color = Color.Black) },
                                            onClick = {
                                                gender = option
                                                genderExpanded = false
                                            },
                                            colors = MenuItemColors(
                                                textColor = Color.Black,
                                                leadingIconColor = Color.Black,
                                                trailingIconColor = Color.Black,
                                                disabledTextColor = Color.Gray,
                                                disabledLeadingIconColor = Color.Gray,
                                                disabledTrailingIconColor = Color.Gray
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Card 2: Child BMI Record ────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Child BMI Record", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)

                    // Height + Weight row
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Height (cm)", fontWeight = FontWeight.SemiBold, color = Color.Black)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = heightCm,
                                onValueChange = { heightCm = it },
                                singleLine = true,
                                placeholder = { Text("Enter height", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                )
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Weight (kg)", fontWeight = FontWeight.SemiBold, color = Color.Black)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = weightKg,
                                onValueChange = { weightKg = it },
                                singleLine = true,
                                placeholder = { Text("Enter weight", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                )
                            )
                        }
                    }

                    // ── BMI Status pill (status only, no number) ────────────
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF4FF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BMI Status",
                                color = bmiStatusColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "  •  ",
                                color = bmiStatusColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = bmiStatusLabel,
                                color = bmiStatusColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // ── Upload Photo ────────────────────────────────────────
                    Text("Add Proof / Photo", fontWeight = FontWeight.SemiBold, color = Color.Black)
                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch(arrayOf("image/*")) },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                    ) {
                        if (selectedEvidenceUri != null) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = primaryGreen)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = selectedEvidenceFileName ?: "Photo Selected",
                                color = primaryGreen,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Icon(Icons.Filled.FileUpload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Upload Photo")
                        }
                    }

                    // ── Notes ───────────────────────────────────────────────
                    Text("Notes", fontWeight = FontWeight.SemiBold, color = Color.Black)
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Add notes...", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = cardBg,
                            unfocusedContainerColor = cardBg,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                }
            }

            if (saved) {
                Text(
                    "✅ Record saved successfully!",
                    color = primaryGreen,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // ── Action Buttons ──────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                }

                Button(
                    onClick = {
                        scope.launch {
                            val h   = heightCm.toDoubleOrNull() ?: 0.0
                            val w   = weightKg.toDoubleOrNull() ?: 0.0
                            val months = ageMonths.toIntOrNull() ?: 0
                            if (fullName.isBlank()) {
                                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            if (months <= 0) {
                                Toast.makeText(context, "Enter a valid age in months", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            isLoading = true

                            AppData.updateChildInfo(
                                childId   = childId,
                                fullName  = fullName,
                                ageMonths = months,       // ← now passing months
                                gender    = gender
                            )

                            if (h > 0 && w > 0) {
                                AppData.addBmiRecord(
                                    childId  = childId,
                                    heightCm = h,
                                    weightKg = w,
                                    notes    = notes,
                                    date     = AppData.getCurrentDate(),
                                    evidenceUri = selectedEvidenceUri,
                                    evidenceMimeType = selectedEvidenceUri?.let { context.contentResolver.getType(it) }.orEmpty(),
                                    evidenceFileName = selectedEvidenceFileName.orEmpty()
                                )
                            }

                            isLoading = false
                            saved = true
                            Toast.makeText(context, "Child profile updated.", Toast.LENGTH_SHORT).show()
                            navController.navigate(Routes.BHW_DASHBOARD) {
                                popUpTo(Routes.BHW_DASHBOARD) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    enabled = !isLoading
                ) {
                    if (isLoading)
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else
                        Text("Save", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                }
            }
        }
    }
}

private fun Uri.displayName(contentResolver: android.content.ContentResolver): String {
    return runCatching {
        contentResolver.query(this, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else {
                    lastPathSegment?.substringAfterLast('/') ?: "Photo Selected"
                }
            } ?: (lastPathSegment?.substringAfterLast('/') ?: "Photo Selected")
    }.getOrDefault("Photo Selected")
}
