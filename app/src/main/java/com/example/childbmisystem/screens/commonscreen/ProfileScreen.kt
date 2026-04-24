package com.example.childbmisystem.screens.commonscreen

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.childbmisystem.data.AppData
import com.example.childbmisystem.navigation.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val AppBg = Color(0xFFF7F8FA)
private val WhiteCard = Color(0xFFFFFFFF)
private val Green = Color(0xFF007958)
private val DarkGreen = Color(0xFF007958)
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
    var showLogoutConfirm by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedProfileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedProfileFileName by remember { mutableStateOf<String?>(null) }
    var cachedProfilePhotoUrl by remember { mutableStateOf("") }

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
            selectedProfileUri = it
            selectedProfileFileName = it.displayName(context.contentResolver)
            isEditing = true
            showPersonalInfo = true
            Toast.makeText(context, "Profile photo selected.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(user?.id, user?.fullName, user?.phoneNumber, user?.address, user?.photoUrl) {
        fullName = user?.fullName.orEmpty()
        phoneNumber = user?.phoneNumber.orEmpty()
        address = user?.address.orEmpty()
        cachedProfilePhotoUrl = loadCachedProfilePhoto(context, user?.id.orEmpty())
        if (!user?.photoUrl.isNullOrBlank()) {
            saveCachedProfilePhoto(context, user?.id.orEmpty(), user?.photoUrl.orEmpty())
            cachedProfilePhotoUrl = user?.photoUrl.orEmpty()
        }
        selectedProfileUri = null
        selectedProfileFileName = null
    }

    fun resetFields() {
        fullName = user?.fullName.orEmpty()
        phoneNumber = user?.phoneNumber.orEmpty()
        address = user?.address.orEmpty()
        selectedProfileUri = null
        selectedProfileFileName = null
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

    val displayPhotoModel = when {
        selectedProfileUri != null -> selectedProfileUri
        cachedProfilePhotoUrl.isNotBlank() -> cachedProfilePhotoUrl
        user.photoUrl.isNotBlank() -> user.photoUrl
        else -> null
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = {
                Text("Log Out", color = Color.Black, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to log out?", color = Color.Black)
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        showLogoutConfirm = false
                        scope.launch {
                            isLoggingOut = true
                            delay(300)
                            AppData.logout()
                            isLoggingOut = false
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0)
                                launchSingleTop = true
                            }
                        }
                    },
                    border = androidx.compose.foundation.BorderStroke(1.dp, Danger),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger)
                ) {
                    Text("Yes", color = Danger)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLogoutConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text("No", color = Color.White)
                }
            },
            containerColor = Color.White
        )
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

                Spacer(modifier = Modifier.weight(1f))
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
                        when {
                            displayPhotoModel != null -> {
                                AsyncImage(
                                    model = displayPhotoModel,
                                    contentDescription = "Profile photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Green,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            isEditing = true
                            showPersonalInfo = true
                            photoPickerLauncher.launch(arrayOf("image/*"))
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Upload photo",
                            tint = DarkGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedProfileFileName.isNullOrBlank()) "Upload Profile Photo" else "Photo Ready",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = fullName.ifBlank { "User" },
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
                            text = address.ifBlank { "Address not set" },
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
                                                address = cleanAddress,
                                                profileImageUri = selectedProfileUri
                                            )
                                            isSaving = false

                                            if (saved) {
                                                if (selectedProfileUri != null) {
                                                    saveCachedProfilePhoto(
                                                        context = context,
                                                        userId = user.id,
                                                        photoUrl = selectedProfileUri.toString()
                                                    )
                                                    cachedProfilePhotoUrl = selectedProfileUri.toString()
                                                }
                                                isEditing = false
                                                selectedProfileUri = null
                                                selectedProfileFileName = null
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
                        showLogoutConfirm = true
                    }
                    .padding(vertical = 22.dp),
                contentAlignment = Alignment.Center
            ) {
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

private fun Uri.displayName(contentResolver: android.content.ContentResolver): String {
    return runCatching {
        contentResolver.query(this, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else {
                    lastPathSegment?.substringAfterLast('/') ?: "Profile Photo"
                }
            } ?: (lastPathSegment?.substringAfterLast('/') ?: "Profile Photo")
    }.getOrDefault("Profile Photo")
}

private fun loadCachedProfilePhoto(context: android.content.Context, userId: String): String {
    if (userId.isBlank()) return ""
    val prefs = context.getSharedPreferences("profile_photo_cache", android.content.Context.MODE_PRIVATE)
    return prefs.getString("photo_$userId", "") ?: ""
}

private fun saveCachedProfilePhoto(
    context: android.content.Context,
    userId: String,
    photoUrl: String
) {
    if (userId.isBlank() || photoUrl.isBlank()) return
    val prefs = context.getSharedPreferences("profile_photo_cache", android.content.Context.MODE_PRIVATE)
    prefs.edit().putString("photo_$userId", photoUrl).apply()
}

@Composable
private fun ProfileScreenPreviewContent() {
    MaterialTheme {
        ProfileScreen(navController = NavController(LocalContext.current))
    }
}
