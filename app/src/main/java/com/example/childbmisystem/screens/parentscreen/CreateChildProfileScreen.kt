package com.example.childbmisystem.screens.parentscreen

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChildProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var ageMonths by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var genderExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var submitAttempted by remember { mutableStateOf(false) }

    var selectedChildPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedChildPhotoFileName by remember { mutableStateOf<String?>(null) }
    var selectedEvidenceUri by remember { mutableStateOf<Uri?>(null) }
    var selectedEvidenceFileName by remember { mutableStateOf<String?>(null) }

    val genderOptions = listOf("Male", "Female")
    val inputBackgroundColor = Color(0xFFE8E9EC)
    val cardBorderColor = Color(0xFFE0E0E0)
    val primaryGreen = Color(0xFF007958)

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

    val proofPhotoPickerLauncher = rememberLauncherForActivityResult(
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
    val heightValue = height.toDoubleOrNull()
    val weightValue = weight.toDoubleOrNull()
    val liveAgeMonths = monthsValue ?: 0
    val liveBmi = remember(heightValue, weightValue, liveAgeMonths) {
        if ((heightValue ?: 0.0) > 0 && (weightValue ?: 0.0) > 0) {
            AppData.calculateBmi(heightValue ?: 0.0, weightValue ?: 0.0, liveAgeMonths)
        } else {
            0.0
        }
    }
    val liveBmiStatus = remember(liveBmi, liveAgeMonths, gender) {
        if (liveBmi > 0) AppData.bmiStatus(liveBmi, liveAgeMonths, gender) else "No Data"
    }

    val nameError = shouldShowRequiredError(name, submitAttempted)
    val ageError = shouldShowPositiveWholeNumberError(ageMonths, submitAttempted)
    val addressError = shouldShowRequiredError(address, submitAttempted)
    val heightError = shouldShowPositiveDecimalError(height, submitAttempted)
    val weightError = shouldShowPositiveDecimalError(weight, submitAttempted)

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 8.dp, end = 16.dp, bottom = 8.dp)
            ) {
                androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Column {
                    Text(
                        text = "Create Child Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                    )
                    Text(
                        text = "Add new child",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, cardBorderColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FormInputField(
                        label = "Name",
                        placeholder = "Enter name",
                        value = name,
                        onValueChange = { name = it },
                        backgroundColor = inputBackgroundColor,
                        isError = nameError
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormInputField(
                            label = "Age (months)",
                            placeholder = "e.g. 24",
                            value = ageMonths,
                            onValueChange = { ageMonths = filterWholeNumberInput(it) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            backgroundColor = inputBackgroundColor,
                            isError = ageError
                        )

                        ExposedDropdownMenuBox(
                            expanded = genderExpanded,
                            onExpandedChange = { genderExpanded = it },
                            modifier = Modifier
                                .weight(1f)
                                .animateContentSize()
                        ) {
                            Column {
                                Text(
                                    text = "Gender",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    ),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                TextField(
                                    value = gender,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded)
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        focusedContainerColor = inputBackgroundColor,
                                        unfocusedContainerColor = inputBackgroundColor,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTrailingIconColor = Color.Black,
                                        unfocusedTrailingIconColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                        .fillMaxWidth()
                                )
                            }

                            ExposedDropdownMenu(
                                expanded = genderExpanded,
                                onDismissRequest = { genderExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                genderOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(text = option, color = Color.Black) },
                                        colors = MenuDefaults.itemColors(textColor = Color.Black),
                                        onClick = {
                                            gender = option
                                            genderExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    FormInputField(
                        label = "Address",
                        placeholder = "Enter address",
                        value = address,
                        onValueChange = { address = it },
                        backgroundColor = inputBackgroundColor,
                        isError = addressError
                    )

                    Text(
                        text = "Upload Child Profile",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )

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
                            if (selectedChildPhotoUri != null) {
                                AsyncImage(
                                    model = selectedChildPhotoUri,
                                    contentDescription = "Child profile photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Filled.FileUpload,
                                    contentDescription = "Child profile photo",
                                    tint = primaryGreen,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { childPhotoPickerLauncher.launch(arrayOf("image/*")) },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                    ) {
                        if (selectedChildPhotoUri != null) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Child Profile Photo Selected",
                                tint = primaryGreen
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedChildPhotoFileName ?: "Child Profile Photo Selected",
                                color = primaryGreen,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.FileUpload,
                                contentDescription = "Upload Child Profile"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload Child Profile")
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, cardBorderColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Child BMI Record",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormInputField(
                            label = "Height (cm)",
                            placeholder = "Enter height",
                            value = height,
                            onValueChange = { height = filterPositiveDecimalInput(it) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            backgroundColor = inputBackgroundColor,
                            isError = heightError
                        )
                        FormInputField(
                            label = "Weight (kg)",
                            placeholder = "Enter weight",
                            value = weight,
                            onValueChange = { weight = filterPositiveDecimalInput(it) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            backgroundColor = inputBackgroundColor,
                            isError = weightError
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF2F5FB), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (liveBmi > 0) {
                                "BMI: $liveBmi  •  $liveBmiStatus"
                            } else {
                                "BMI: --  •  No Data"
                            },
                            color = Color(0xFF4285F4),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "Add Proof / Photo",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )

                    OutlinedButton(
                        onClick = { proofPhotoPickerLauncher.launch(arrayOf("image/*")) },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                    ) {
                        if (selectedEvidenceUri != null) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Photo Selected",
                                tint = primaryGreen
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedEvidenceFileName ?: "Photo Selected",
                                color = primaryGreen,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Filled.FileUpload,
                                contentDescription = "Upload Photo"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload Photo")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

            Button(
                onClick = {
                    submitAttempted = true

                    if (nameError || ageError || addressError || heightError || weightError) {
                        Toast.makeText(context, "Please correct the incorrect input.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val months = monthsValue ?: return@Button
                    val h = heightValue ?: return@Button
                    val w = weightValue ?: return@Button

                    isLoading = true

                    scope.launch {
                        try {
                            val cal = Calendar.getInstance()
                            cal.add(Calendar.MONTH, -months)
                            val dobDay = cal.get(Calendar.DAY_OF_MONTH)
                            val dobMonth = cal.get(Calendar.MONTH) + 1
                            val dobYear = cal.get(Calendar.YEAR)
                            val dob = "%02d-%02d-%d".format(dobDay, dobMonth, dobYear)

                            val child = AppData.addChild(
                                fullName = name.trim(),
                                dob = dob,
                                gender = gender,
                                imageUri = selectedChildPhotoUri
                            )

                            AppData.addBmiRecord(
                                childId = child.id,
                                heightCm = h,
                                weightKg = w,
                                notes = address.trim(),
                                date = AppData.getCurrentDate(),
                                evidenceUri = selectedEvidenceUri,
                                evidenceMimeType = selectedEvidenceUri?.let { context.contentResolver.getType(it) }.orEmpty(),
                                evidenceFileName = selectedEvidenceFileName.orEmpty()
                            )

                            Toast.makeText(context, "Profile Created!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Create Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun FormInputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    backgroundColor: Color,
    isError: Boolean = false
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.Black
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, color = Color.Gray) },
            keyboardOptions = keyboardOptions,
            singleLine = true,
            isError = isError,
            supportingText = {
                if (isError) {
                    Text("Incorrect input", color = Color(0xFFD32F2F))
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                disabledTextColor = Color.Black,
                focusedContainerColor = backgroundColor,
                unfocusedContainerColor = backgroundColor,
                disabledContainerColor = backgroundColor,
                focusedIndicatorColor = if (isError) Color(0xFFD32F2F) else Color.Transparent,
                unfocusedIndicatorColor = if (isError) Color(0xFFD32F2F) else Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color(0xFFD32F2F),
                errorContainerColor = backgroundColor
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

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
