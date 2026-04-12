package com.example.childbmisystem.screens.parentscreen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.childbmisystem.data.AppData
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChildProfileScreen(navController: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Fields
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }

    // Gender
    var gender by remember { mutableStateOf("Male") }
    var genderExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Male", "Female", "Other")

    var isLoading by remember { mutableStateOf(false) }

    // Photo state
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Image picker — opens phone gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedPhotoUri = uri
        if (uri != null) {
            Toast.makeText(context, "Photo selected", Toast.LENGTH_SHORT).show()
        }
    }

    // Live BMI — AppData.calculateBmi returns Double directly
    val hVal = height.toDoubleOrNull() ?: 0.0
    val wVal = weight.toDoubleOrNull() ?: 0.0
    val liveBmi = remember(hVal, wVal) { AppData.calculateBmi(hVal, wVal) }

    // UI Colors
    val inputBackgroundColor = Color(0xFFE8E9EC)
    val cardBorderColor = Color(0xFFE0E0E0)
    val primaryGreen = Color(0xFF00C853)

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 8.dp, end = 16.dp, bottom = 8.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
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

            // --- SECTION 1: Personal Info ---
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
                        backgroundColor = inputBackgroundColor
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormInputField(
                            label = "Age",
                            placeholder = "Enter age",
                            value = age,
                            onValueChange = { age = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            backgroundColor = inputBackgroundColor
                        )

                        ExposedDropdownMenuBox(
                            expanded = genderExpanded,
                            onExpandedChange = { genderExpanded = it },
                            modifier = Modifier.weight(1f)
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
                        backgroundColor = inputBackgroundColor
                    )
                }
            }

            // --- SECTION 2: BMI Record ---
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
                        "Child BMI Record",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FormInputField(
                            label = "Height (cm)",
                            placeholder = "Enter height",
                            value = height,
                            onValueChange = { height = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            backgroundColor = inputBackgroundColor
                        )
                        FormInputField(
                            label = "Weight (kg)",
                            placeholder = "Enter weight",
                            value = weight,
                            onValueChange = { weight = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            backgroundColor = inputBackgroundColor
                        )
                    }

                    // Live BMI Display — liveBmi is already a Double
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF2F5FB), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "BMI: $liveBmi  •  ${AppData.bmiStatus(liveBmi)}",
                            color = Color(0xFF4285F4),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    // --- Upload Photo Button ---
                    Text(
                        text = "Add Proof / Photo",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )

                    OutlinedButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                    ) {
                        if (selectedPhotoUri != null) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Photo Selected",
                                tint = primaryGreen
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Photo Selected",
                                color = primaryGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Icon(
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

            // --- SAVE BUTTON ---
            Button(
                onClick = {
                    if (name.isBlank() || age.isBlank() || height.isBlank() || weight.isBlank()) {
                        Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val ageInt = age.toIntOrNull()
                    val h = height.toDoubleOrNull()
                    val w = weight.toDoubleOrNull()

                    if (ageInt == null || h == null || w == null) {
                        Toast.makeText(context, "Invalid input", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true

                    scope.launch {
                        try {
                            // Step 1: Build dob string from age
                            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                            val dob = "01-01-${currentYear - ageInt}"

                            // Step 2: AppData.addChild handles:
                            //   → FirebaseRepository.addChild (creates Firestore doc, returns childId)
                            //   → FirebaseRepository.uploadChildPhoto (uploads to Storage if uri given)
                            //   → updates "photoUrl" field on the child document
                            val child = AppData.addChild(
                                fullName = name,
                                dob = dob,
                                gender = gender,
                                imageUri = selectedPhotoUri  // null = no photo, Uri = upload
                            )

                            // Step 3: AppData.addBmiRecord handles:
                            //   → AppData.calculateBmi (Double)
                            //   → AppData.bmiStatus (String)
                            //   → FirebaseRepository.addBmiRecord (saves to children/$id/bmiRecords)
                            //   → updates local AppData.children list
                            AppData.addBmiRecord(
                                childId = child.id,
                                heightCm = h,
                                weightKg = w,
                                notes = address,         // address saved as notes
                                date = AppData.getCurrentDate()
                            )

                            Toast.makeText(context, "Profile Created!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()

                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                        isLoading = false
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
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Create Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
    backgroundColor: Color
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
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                disabledTextColor = Color.Black,
                focusedContainerColor = backgroundColor,
                unfocusedContainerColor = backgroundColor,
                disabledContainerColor = backgroundColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}