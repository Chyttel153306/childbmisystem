package com.example.childbmisystem.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateChildProfileScreen(navController: NavController, childId: String) {

    val child = AppData.getChild(childId)

    if (child == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Child not found.", color = Color.Black)
        }
        return
    }

    val latest = child.latestBmi
    var heightCm by remember { mutableStateOf(latest?.heightCm?.toString() ?: "") }
    var weightKg by remember { mutableStateOf(latest?.weightKg?.toString() ?: "") }
    var notes by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val green = Color(0xFF00C853)
    val primaryBlue = Color(0xFF2196F3)

    val newBmi = remember(heightCm, weightKg) {
        val h = heightCm.toDoubleOrNull() ?: 0.0
        val w = weightKg.toDoubleOrNull() ?: 0.0
        if (h > 0 && w > 0) AppData.calculateBmi(h, w) else 0.0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Update Child Profile", fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Modify measurements", fontSize = 12.sp, color = Color.Black)
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Child Info Header
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(child.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Text("${child.ageYears} years old • ${child.gender}",
                        fontSize = 13.sp, color = Color.Black)
                }
            }

            Text("Update Measurements", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.Black)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Height (cm)", fontWeight = FontWeight.Medium, color = Color.Black,
                        modifier = Modifier.padding(bottom = 4.dp))
                    OutlinedTextField(
                        value = heightCm,
                        onValueChange = { heightCm = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = Color.LightGray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("Weight (kg)", fontWeight = FontWeight.Medium, color = Color.Black,
                        modifier = Modifier.padding(bottom = 4.dp))
                    OutlinedTextField(
                        value = weightKg,
                        onValueChange = { weightKg = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = Color.LightGray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                }
            }

            if (newBmi > 0) {
                Text("New BMI: $newBmi  (${AppData.bmiStatus(newBmi)})",
                    color = green, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Text("Update BMI Proof", fontWeight = FontWeight.SemiBold, color = Color.Black)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) { Text("⬆ Upload") }
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) { Text("📷 Photo") }
            }

            Text("Notes", fontWeight = FontWeight.SemiBold, color = Color.Black)
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Add notes...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryBlue,
                    unfocusedBorderColor = Color.LightGray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            if (saved) {
                Text("✅ Record saved successfully!", color = green, fontWeight = FontWeight.SemiBold)
            }

            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                ) { Text("Cancel", fontWeight = FontWeight.Bold) }

                Button(
                    onClick = {
                        scope.launch {
                            val h = heightCm.toDoubleOrNull() ?: 0.0
                            val w = weightKg.toDoubleOrNull() ?: 0.0
                            if (h > 0 && w > 0) {
                                isLoading = true
                                AppData.addBmiRecord(childId, h, w, notes, AppData.getCurrentDate())
                                isLoading = false
                                saved = true
                            }
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Save", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}