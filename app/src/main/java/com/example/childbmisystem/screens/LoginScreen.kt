package com.example.childbmisystem.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.childbmisystem.data.AppData
import com.example.childbmisystem.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {

    var email           by remember { mutableStateOf("") }   // ✅ changed
    var password        by remember { mutableStateOf("") }
    var selectedRole    by remember { mutableStateOf("BHW") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf("") }
    var isLoading       by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val green = Color(0xFF48CF4F)
    val blue  = Color(0xFF2F80ED)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Top Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF00C8FF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Title
        Text(
            "Child BMI Monitoring",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            "Child BMI Record",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 28.dp)
        )

        // LOGIN AS
        Text(
            "Login As",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("BHW", "Parent").forEach { role ->
                val isSelected = selectedRole == role

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) green else Color.Transparent,
                    label = ""
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(bgColor, RoundedCornerShape(10.dp))
                        .clickable { selectedRole = role }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = role,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else Color.Black
                    )
                }
            }
        }

        // ✅ EMAIL (replaced Username)
        Text("Email", modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Enter email", color = Color.LightGray) },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = green,
                unfocusedBorderColor = Color.LightGray
            )
        )

        // Password
        Text("Password", modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Enter password", color = Color.LightGray) },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = green,
                unfocusedBorderColor = Color.LightGray
            )
        )

        // Error Message
        if (errorMessage.isNotEmpty()) {
            Text(
                errorMessage,
                color = Color.Red,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        // LOGIN BUTTON
        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = ""

                    val success = AppData.login(
                        email.trim(),   // ✅ changed
                        password,
                        selectedRole
                    )

                    isLoading = false

                    if (success) {

                        val destination = if (selectedRole == "BHW") {
                            Routes.BHW_DASHBOARD
                        } else {
                            Routes.PARENT_DASHBOARD
                        }

                        navController.navigate(destination) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }

                    } else {
                        errorMessage = "Invalid email, password, or role."
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = blue),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Login",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // REGISTER
        Row {
            Text("Don't have an account? ", color = Color.DarkGray)
            Text(
                "Create Account",
                color = green,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }
    }
}

