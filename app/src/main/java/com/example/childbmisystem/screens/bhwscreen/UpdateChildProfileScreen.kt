package com.example.childbmisystem.screens.bhwscreen

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.childbmisystem.data.AppData
import com.example.childbmisystem.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateChildProfileScreen(navController: NavController, childId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val child = AppData.getChild(childId)

    if (child == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Child not found.", color = Color.Black)
        }
        return
    }

    val latest = child.latestBmi
    val cardBg = Color(0xFFF5F5F5)
    val primaryGreen = Color(0xFF007958)

    var fullName by remember { mutableStateOf(child.fullName) }
    var ageMonths by remember { mutableStateOf(child.ageMonths.toString()) }
    var gender by remember { mutableStateOf(child.gender) }
    var genderExpanded by remember { mutableStateOf(false) }
    var heightCm by remember { mutableStateOf(latest?.heightCm?.toString() ?: "") }
    var weightKg by remember { mutableStateOf(latest?.weightKg?.toString() ?: "") }
    var notes by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var submitAttempted by remember { mutableStateOf(false) }
    var selectedChildPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedChildPhotoFileName by remember { mutableStateOf<String?>(null) }
    var selectedEvidenceUri by remember { mutableStateOf<Uri?>(null) }
    var selectedEvidenceFileName by remember { mutableStateOf<String?>(null) }

    val genderOptions = listOf("Male", "Female")

    val childPhotoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            selectedChildPhotoUri = it
            selectedChildPhotoFileName = it.displayName(context.contentResolver)
            Toast.makeText(context, "Child profile photo selected", Toast.LENGTH_SHORT).show()
        }
    }

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

    val monthsValue = ageMonths.toIntOrNull()
    val heightValue = heightCm.toDoubleOrNull()
    val weightValue = weightKg.toDoubleOrNull()
    val enteredAgeMonths = monthsValue ?: child.ageMonths
    val newBmi = remember(heightValue, weightValue, enteredAgeMonths) {
        if ((heightValue ?: 0.0) > 0 && (weightValue ?: 0.0) > 0) {
            AppData.calculateBmi(heightValue ?: 0.0, weightValue ?: 0.0, enteredAgeMonths)
        } else {
            0.0
        }
    }
    val bmiStatusLabel = if (newBmi > 0) {
        AppData.bmiStatus(newBmi, enteredAgeMonths, gender)
    } else {
        "No Data"
    }
    val bmiStatusColor = when (bmiStatusLabel) {
        "Normal" -> Color(0xFF2196F3)
        "Overweight" -> Color(0xFFFFA000)
        "Obese" -> Color(0xFFD32F2F)
        "Underweight" -> Color(0xFF1565C0)
        else -> Color(0xFF2196F3)
    }

    val nameError = shouldShowRequiredError(fullName, submitAttempted)
    val ageError = shouldShowPositiveWholeNumberError(ageMonths, submitAttempted)
    val heightError = shouldShowPositiveDecimalError(heightCm, submitAttempted)
    val weightError = shouldShowPositiveDecimalError(weightKg, submitAttempted)

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
                    androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                        androidx.compose.material3.Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            null,
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                    ValidatedOutlinedField(
                        label = "Name",
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = "Enter name",
                        isError = nameError,
                        backgroundColor = cardBg
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Age (months)", fontWeight = FontWeight.SemiBold, color = Color.Black)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = ageMonths,
                                onValueChange = { ageMonths = filterWholeNumberInput(it) },
                                singleLine = true,
                                isError = ageError,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                placeholder = { Text("e.g. 24", color = Color.Gray) },
                                supportingText = {
                                    if (ageError) {
                                        Text("Incorrect input", color = Color(0xFFD32F2F))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = outlinedFieldColors(cardBg)
                            )
                        }

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
                                    colors = outlinedFieldColors(cardBg)
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
                                            colors = MenuDefaults.itemColors(textColor = Color.Black)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text("Child Profile Photo", fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(108.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDDF0E7))
                                .border(2.dp, primaryGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                selectedChildPhotoUri != null -> {
                                    AsyncImage(
                                        model = selectedChildPhotoUri,
                                        contentDescription = "Child profile photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                child.photoUrl.isNotBlank() -> {
                                    AsyncImage(
                                        model = child.photoUrl,
                                        contentDescription = "Child profile photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                else -> {
                                    androidx.compose.material3.Icon(
                                        Icons.Default.FileUpload,
                                        contentDescription = null,
                                        tint = primaryGreen,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { childPhotoPickerLauncher.launch(arrayOf("image/*")) },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                    ) {
                        if (selectedChildPhotoUri != null) {
                            androidx.compose.material3.Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = primaryGreen)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = selectedChildPhotoFileName ?: "Child Profile Photo Selected",
                                color = primaryGreen,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            androidx.compose.material3.Icon(Icons.Filled.FileUpload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Upload Child Profile")
                        }
                    }
                }
            }

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

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ValidatedOutlinedField(
                            label = "Height (cm)",
                            value = heightCm,
                            onValueChange = { heightCm = filterPositiveDecimalInput(it) },
                            placeholder = "Enter height",
                            isError = heightError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            backgroundColor = cardBg,
                            modifier = Modifier.weight(1f)
                        )
                        ValidatedOutlinedField(
                            label = "Weight (kg)",
                            value = weightKg,
                            onValueChange = { weightKg = filterPositiveDecimalInput(it) },
                            placeholder = "Enter weight",
                            isError = weightError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            backgroundColor = cardBg,
                            modifier = Modifier.weight(1f)
                        )
                    }

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

                    Text("Add Proof / Photo", fontWeight = FontWeight.SemiBold, color = Color.Black)
                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch(arrayOf("image/*")) },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                    ) {
                        if (selectedEvidenceUri != null) {
                            androidx.compose.material3.Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = primaryGreen)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = selectedEvidenceFileName ?: "Photo Selected",
                                color = primaryGreen,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            androidx.compose.material3.Icon(Icons.Filled.FileUpload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Upload Photo")
                        }
                    }

                    Text("Notes", fontWeight = FontWeight.SemiBold, color = Color.Black)
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Add notes...", color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = outlinedFieldColors(cardBg)
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                }

                Button(
                    onClick = {
                        submitAttempted = true

                        val missingFields = buildList {
                            if (fullName.trim().isBlank()) add("name")
                            if (ageMonths.trim().isBlank()) add("age")
                            if (heightCm.trim().isBlank()) add("height")
                            if (weightKg.trim().isBlank()) add("weight")
                        }

                        val invalidFields = buildList {
                            if (fullName.trim().isNotBlank() && nameError) add("name")
                            if (ageMonths.trim().isNotBlank() && ageError) add("age")
                            if (heightCm.trim().isNotBlank() && heightError) add("height")
                            if (weightKg.trim().isNotBlank() && weightError) add("weight")
                        }

                        if (missingFields.isNotEmpty()) {
                            Toast.makeText(
                                context,
                                "Please fill all the missing input: ${formatFieldList(missingFields)}.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        if (invalidFields.isNotEmpty()) {
                            Toast.makeText(
                                context,
                                "Please correct the following input: ${formatFieldList(invalidFields)}.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        val months = monthsValue ?: return@Button
                        val h = heightValue ?: return@Button
                        val w = weightValue ?: return@Button

                        scope.launch {
                            isLoading = true
                            try {
                                AppData.updateChildInfo(
                                    childId = childId,
                                    fullName = fullName.trim(),
                                    ageMonths = months,
                                    gender = gender,
                                    imageUri = selectedChildPhotoUri
                                )

                                AppData.addBmiRecord(
                                    childId = childId,
                                    heightCm = h,
                                    weightKg = w,
                                    notes = notes.trim(),
                                    date = AppData.getCurrentDate(),
                                    evidenceUri = selectedEvidenceUri,
                                    evidenceMimeType = selectedEvidenceUri?.let { context.contentResolver.getType(it) }.orEmpty(),
                                    evidenceFileName = selectedEvidenceFileName.orEmpty()
                                )

                                Toast.makeText(context, "Child profile updated.", Toast.LENGTH_SHORT).show()
                                navController.navigate(Routes.BHW_DASHBOARD) {
                                    popUpTo(Routes.BHW_DASHBOARD) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    e.message ?: "Failed to update child profile.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ValidatedOutlinedField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = modifier) {
        Text(label, fontWeight = FontWeight.SemiBold, color = Color.Black)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            isError = isError,
            keyboardOptions = keyboardOptions,
            placeholder = { Text(placeholder, color = Color.Gray) },
            supportingText = {
                if (isError) {
                    Text("Incorrect input", color = Color(0xFFD32F2F))
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = outlinedFieldColors(backgroundColor)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun outlinedFieldColors(backgroundColor: Color) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        errorBorderColor = Color(0xFFD32F2F),
        focusedContainerColor = backgroundColor,
        unfocusedContainerColor = backgroundColor,
        errorContainerColor = backgroundColor,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        errorTextColor = Color.Black
    )

private fun shouldShowRequiredError(value: String, submitAttempted: Boolean): Boolean {
    return submitAttempted && value.trim().isBlank()
}

private fun shouldShowPositiveWholeNumberError(value: String, submitAttempted: Boolean): Boolean {
    if (value.isNotBlank()) {
        return value.toIntOrNull()?.let { it <= 0 } != false
    }
    return submitAttempted
}

private fun shouldShowPositiveDecimalError(value: String, submitAttempted: Boolean): Boolean {
    if (value.isNotBlank()) {
        return value.toDoubleOrNull()?.let { it <= 0.0 } != false
    }
    return submitAttempted
}

private fun filterWholeNumberInput(input: String): String = input.filter { it.isDigit() }

private fun filterPositiveDecimalInput(input: String): String {
    val result = StringBuilder()
    var hasDecimal = false

    input.forEach { char ->
        when {
            char.isDigit() -> result.append(char)
            char == '.' && !hasDecimal -> {
                if (result.isEmpty()) {
                    result.append("0")
                }
                result.append(char)
                hasDecimal = true
            }
        }
    }

    return result.toString()
}

private fun formatFieldList(fields: List<String>): String {
    return fields.joinToString(", ") { field ->
        field.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
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
