package com.example.childbmisystem.screens.commonscreen

import android.widget.Toast
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.childbmisystem.navigation.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val AppBg = Color(0xFFF7F8FA)
private val WhiteCard = Color(0xFFFFFFFF)
private val Green = Color(0xFF4FD19A)
private val DarkGreen = Color(0xFF1F7A56)
private val SoftGreen = Color(0xFFE3F7EE)
private val SoftGray = Color(0xFFF1F3F5)
private val BorderGray = Color(0xFFE5E7EB)
private val TextPrimary = Color(0xFF0F172A)
private val TextSecondary = Color(0xFF6B7280)
private val Danger = Color(0xFFEF4444)

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val user = AppData.currentUser.value
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val isBhw = user?.role.equals("bhw", ignoreCase = true)
    val roleLabel = if (isBhw) "Barangay Health Worker" else "Parent / Guardian"

    var isEditing by remember { mutableStateOf(false) }
    var showPersonalInfo by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    LaunchedEffect(user?.id, user?.fullName, user?.phoneNumber, user?.address) {
        fullName = user?.fullName.orEmpty()
        phoneNumber = user?.phoneNumber.orEmpty()
        address = user?.address.orEmpty()
    }

    fun resetFields() {
        fullName = user?.fullName.orEmpty()
        phoneNumber = user?.phoneNumber.orEmpty()
        address = user?.address.orEmpty()
    }

    if (user == null) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppBg
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("No profile data available.", color = TextPrimary)
            }
        }
        return
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Card(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Profile",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "View and manage your account details.",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                isEditing = true
                                showPersonalInfo = true
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Green,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit Profile",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDDF0E7))
                            .border(2.dp, Green, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Green,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = user.fullName.ifBlank { "User" },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(SoftGreen)
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Role",
                            tint = Green,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = roleLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(SoftGray)
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Green,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = user.address.ifBlank { "Address not set" },
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Account",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                onClick = {
                    showPersonalInfo = !showPersonalInfo
                    if (showPersonalInfo) {
                        isEditing = true
                    }
                }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SoftGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Personal Information",
                                tint = Green,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Personal Information",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "View and update your personal details",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Go",
                            tint = TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    if (showPersonalInfo) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 18.dp, end = 18.dp, bottom = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ProfileField(
                                label = "Full Name",
                                value = fullName,
                                enabled = isEditing,
                                onValueChange = { fullName = it }
                            )
                            ProfileField(
                                label = "Phone Number",
                                value = phoneNumber,
                                enabled = isEditing,
                                onValueChange = { phoneNumber = it }
                            )
                            ProfileField(
                                label = "Address",
                                value = address,
                                enabled = isEditing,
                                minLines = 2,
                                onValueChange = { address = it }
                            )
                            ProfileField(
                                label = "Email",
                                value = user.email,
                                enabled = false,
                                onValueChange = {}
                            )

                            Text(
                                text = "Email is your login account and stays fixed here.",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        resetFields()
                                        isEditing = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkGreen,
                                        contentColor = Color.White
                                    ),
                                    enabled = !isSaving
                                ) {
                                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = {
                                        val cleanName = fullName.trim()
                                        val cleanAddress = address.trim()

                                        if (cleanName.isBlank() || cleanAddress.isBlank()) {
                                            Toast.makeText(
                                                context,
                                                "Please fill in full name and address.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            return@Button
                                        }

                                        scope.launch {
                                            isSaving = true
                                            val saved = AppData.updateCurrentUserProfile(
                                                fullName = cleanName,
                                                phoneNumber = phoneNumber,
                                                address = cleanAddress
                                            )
                                            isSaving = false

                                            if (saved) {
                                                isEditing = false
                                                Toast.makeText(
                                                    context,
                                                    "Profile updated successfully.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Unable to update profile.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkGreen,
                                        contentColor = Color.White
                                    ),
                                    enabled = !isSaving
                                ) {
                                    if (isSaving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("Save", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(WhiteCard)
                    .border(2.dp, Danger, RoundedCornerShape(24.dp))
                    .clickable(enabled = !isLoggingOut) {
                        scope.launch {
                            isLoggingOut = true
                            delay(300)
                            AppData.logout()
                            isLoggingOut = false
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0)
                            }
                        }
                    }
                    .padding(vertical = 22.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isLoggingOut) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Danger,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Log Out",
                                tint = Danger,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Log Out",
                            color = Danger,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Sign out of your account",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    minLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            minLines = minLines,
            singleLine = minLines == 1,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Green,
                unfocusedBorderColor = BorderGray,
                disabledBorderColor = BorderGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = SoftGray,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                disabledTextColor = TextSecondary,
                focusedLabelColor = DarkGreen,
                unfocusedLabelColor = TextSecondary
            )
        )
    }
}

@Composable
private fun ProfileScreenPreviewContent() {
    MaterialTheme {
        ProfileScreen(navController = NavController(LocalContext.current))
    }
}
