package com.example.childbmisystem.data

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class User(
    val id: String = "",
    val fullName: String = "",
    val username: String = "",
    val password: String = "",
    val role: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val photoUrl: String = ""
) {
    fun toMap() = mapOf(
        "fullName" to fullName,
        "username" to username.lowercase(),
        "role" to role,
        "email" to email,
        "phoneNumber" to phoneNumber,
        "address" to address,
        "photoUrl" to photoUrl
    )
}

data class BmiRecord(
    val id: String = "",
    val date: String = "",
    val heightCm: Double = 0.0,
    val weightKg: Double = 0.0,
    val bmi: Double = 0.0,
    val status: String = "",
    val notes: String = "",
    val recordedBy: String = "",
    val evidenceUrl: String = "",
    val photoUrl: String = "",
    val evidenceMimeType: String = "",
    val evidenceFileName: String = ""
) {
    fun toMap() = mapOf(
        "date" to date,
        "heightCm" to heightCm,
        "weightKg" to weightKg,
        "bmi" to bmi,
        "status" to status,
        "notes" to notes,
        "recordedBy" to recordedBy,
        "timestamp" to Timestamp.now(),
        "evidenceUrl" to evidenceUrl.ifBlank { photoUrl },
        "photoUrl" to photoUrl.ifBlank { evidenceUrl },
        "evidenceMimeType" to evidenceMimeType,
        "evidenceFileName" to evidenceFileName
    )
}

data class Child(
    val id: String = "",
    val fullName: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val parentId: String? = null,
    val photoUrl: String = "",
    val bmiHistory: MutableList<BmiRecord> = mutableListOf()
) {
    val latestBmi: BmiRecord?
        get() = bmiHistory.firstOrNull()

    val bmiStatus: String
        get() = latestBmi?.let { AppData.bmiStatus(it.bmi, ageMonths, gender) } ?: "No Data"

    val ageMonths: Int
        get() = monthsFromDob(dateOfBirth)

    val ageYears: Int
        get() = ageMonths / 12

    fun toMap() = mapOf(
        "fullName" to fullName,
        "dateOfBirth" to dateOfBirth,
        "gender" to gender,
        "parentId" to (parentId ?: ""),
        "photoUrl" to photoUrl
    )
}

data class StatusAlert(
    val id: String = "",
    val childId: String = "",
    val alertType: String = "",
    val message: String = "",
    val sentBy: String = "",
    val date: String = "",
    val timestamp: Timestamp = Timestamp.now()
) {
    fun toMap() = mapOf(
        "childId" to childId,
        "alertType" to alertType,
        "message" to message,
        "sentBy" to sentBy,
        "date" to date,
        "timestamp" to timestamp
    )
}

object AppData {

    var currentUser = mutableStateOf<User?>(null)
    val children = mutableStateListOf<Child>()
    val alerts = mutableStateListOf<StatusAlert>()

    private var isListening = false

    suspend fun login(email: String, password: String, role: String): Boolean {
        val user = FirebaseRepository.getUserByEmail(email) ?: return false
        if (!user.role.equals(role, ignoreCase = true)) return false

        val result = FirebaseRepository.loginWithEmail(email, password)
        return if (result.isSuccess) {
            currentUser.value = user
            loadData()
            true
        } else {
            false
        }
    }

    suspend fun register(
        fullName: String,
        username: String,
        email: String,
        password: String,
        role: String,
        phoneNumber: String,
        address: String
    ): Boolean {
        if (FirebaseRepository.getUserByUsername(username) != null) return false

        val result = FirebaseRepository.registerWithEmail(email, password)
        if (result.isFailure) return false

        val uid = result.getOrNull() ?: return false

        val user = User(
            id = uid,
            fullName = fullName,
            username = username.lowercase(),
            password = "",
            role = role.lowercase(),
            email = email,
            phoneNumber = phoneNumber,
            address = address
        )

        FirebaseRepository.saveUser(user)
        return true
    }

    fun logout() {
        FirebaseRepository.logout()
        currentUser.value = null
        children.clear()
        alerts.clear()
        isListening = false
    }

    fun loadData() {
        val user = currentUser.value ?: return
        val role = user.role

        if (!isListening) {
            children.clear()
            alerts.clear()

            FirebaseRepository.startListeningToCurrentUser(user.id) { updatedUser ->
                currentUser.value = updatedUser
            }

            FirebaseRepository.startListeningToChildren(role) { updatedChildren ->
                children.clear()
                children.addAll(updatedChildren)
            }

            FirebaseRepository.startListeningToAlerts(role) { updatedAlerts ->
                alerts.clear()
                alerts.addAll(updatedAlerts)
            }

            isListening = true
        }
    }

    suspend fun updateCurrentUserProfile(
        fullName: String,
        phoneNumber: String,
        address: String,
        profileImageUri: Uri? = null
    ): Boolean {
        val existingUser = currentUser.value ?: return false

        val uploadedPhotoUrl = if (profileImageUri != null) {
            val uploadResult = FirebaseRepository.uploadUserProfilePhoto(existingUser.id, profileImageUri)
            uploadResult.getOrNull() ?: profileImageUri.toString()
        } else {
            existingUser.photoUrl
        }

        val updatedUser = existingUser.copy(
            fullName = fullName.trim(),
            phoneNumber = phoneNumber.trim(),
            address = address.trim(),
            photoUrl = uploadedPhotoUrl
        )

        val result = FirebaseRepository.saveUser(updatedUser)
        if (result.isSuccess) {
            currentUser.value = updatedUser
            return true
        }

        return false
    }

    private fun upsertChildLocal(child: Child) {
        val index = children.indexOfFirst { it.id == child.id }
        if (index >= 0) {
            children[index] = child
        } else {
            children.add(child)
        }
    }

    suspend fun addChild(
        fullName: String,
        dob: String,
        gender: String,
        imageUri: Uri? = null
    ): Child {
        val tempChild = Child(
            fullName = fullName,
            dateOfBirth = dob,
            gender = gender,
            parentId = currentUser.value?.id
        )

        val addResult = FirebaseRepository.addChild(tempChild)
        val childId = addResult.getOrNull() ?: return tempChild

        var photoUrl = ""
        if (imageUri != null) {
            val uploadResult = FirebaseRepository.uploadChildPhoto(childId, imageUri)
            photoUrl = uploadResult.getOrNull() ?: imageUri.toString()
        }

        val finalChild = tempChild.copy(id = childId, photoUrl = photoUrl)

        FirebaseRepository.db.collection("children")
            .document(childId)
            .update("photoUrl", photoUrl)
            .await()

        upsertChildLocal(finalChild)

        return finalChild
    }

    suspend fun deleteChild(childId: String) {
        val result = FirebaseRepository.deleteChild(childId)
        result.getOrThrow()

        children.removeAll { it.id == childId }
        alerts.removeAll { it.childId == childId }
    }

    fun getChild(childId: String): Child? =
        children.find { it.id == childId }

    fun childrenNeedingAttention(): Int =
        children.count { it.bmiStatus != "Normal" && it.bmiStatus != "No Data" }

    fun calculateBmi(heightCm: Double, weightKg: Double, ageMonths: Int? = null): Double {
        if (heightCm <= 0 || weightKg <= 0) return 0.0

        val adjustedHeightCm = if (ageMonths != null && ageMonths in 0..23) {
            heightCm + 0.7
        } else {
            heightCm
        }

        val heightMeters = adjustedHeightCm / 100.0
        return ((weightKg / (heightMeters * heightMeters)) * 10.0).roundToInt() / 10.0
    }

    fun bmiStatus(bmi: Double, ageMonths: Int? = null, gender: String? = null): String {
        if (bmi <= 0) return "No Data"

        if (ageMonths != null && gender != null) {
            BmiForAgeStandards.statusFor(ageMonths, gender, bmi)?.let { return it }
        }

        return fallbackBmiStatus(bmi)
    }

    suspend fun addBmiRecord(
        childId: String,
        heightCm: Double,
        weightKg: Double,
        notes: String,
        date: String,
        evidenceUri: Uri? = null,
        evidenceMimeType: String = "",
        evidenceFileName: String = ""
    ) {
        val child = getChild(childId)
        val childAgeMonths = child?.ageMonths


        val childGender = child?.gender
        val bmi = calculateBmi(heightCm, weightKg, childAgeMonths)
        val initialEvidenceUrl = evidenceUri?.toString().orEmpty()

        val record = BmiRecord(
            date = date,
            heightCm = heightCm,
            weightKg = weightKg,
            bmi = bmi,
            status = bmiStatus(bmi, childAgeMonths, childGender),
            notes = notes,
            recordedBy = currentUser.value?.fullName ?: "BHW",
            evidenceUrl = initialEvidenceUrl,
            photoUrl = initialEvidenceUrl,
            evidenceMimeType = evidenceMimeType,
            evidenceFileName = evidenceFileName
        )

        val result = FirebaseRepository.addBmiRecord(childId, record)
        val recordId = result.getOrNull() ?: ""

        var evidenceUrl = initialEvidenceUrl
        if (evidenceUri != null && recordId.isNotBlank()) {
            val uploadResult = FirebaseRepository.uploadBmiEvidence(
                childId = childId,
                recordId = recordId,
                evidenceUri = evidenceUri,
                evidenceFileName = evidenceFileName
            )
            if (uploadResult.isSuccess) {
                evidenceUrl = uploadResult.getOrNull() ?: ""
            }
        }

        FirebaseRepository.db
            .collection("children")
            .document(childId)
            .collection("bmiRecords")
            .document(recordId)
            .update(
                mapOf(
                    "evidenceUrl" to evidenceUrl,
                    "photoUrl" to evidenceUrl,
                    "evidenceMimeType" to evidenceMimeType,
                    "evidenceFileName" to evidenceFileName
                )
            )
            .await()

        val newRecord = record.copy(
            id = recordId,
            evidenceUrl = evidenceUrl,
            photoUrl = evidenceUrl
        )

        FirebaseRepository.db
            .collection("children")
            .document(childId)
            .update("lastBmiUpdatedAt", Timestamp.now())
            .await()

        val index = children.indexOfFirst { it.id == childId }
        if (index >= 0) {
            val updatedHistory = children[index].bmiHistory.toMutableList()
            updatedHistory.add(0, newRecord)
            children[index] = children[index].copy(bmiHistory = updatedHistory)
        }
    }

    suspend fun updateChildInfo(
        childId: String,
        fullName: String,
        ageMonths: Int,
        gender: String,
        imageUri: Uri? = null
    ) {
        val existingChild = getChild(childId) ?: return

        val newDob = dobFromAgeMonths(ageMonths)
        var photoUrl = existingChild.photoUrl
        if (imageUri != null) {
            val uploadResult = FirebaseRepository.uploadChildPhoto(childId, imageUri)
            photoUrl = uploadResult.getOrNull() ?: imageUri.toString()
        }

        val updates = mapOf(
            "fullName" to fullName,
            "dateOfBirth" to newDob,
            "gender" to gender,
            "photoUrl" to photoUrl
        )

        FirebaseRepository.db
            .collection("children")
            .document(childId)
            .update(updates)
            .await()

        val index = children.indexOfFirst { it.id == childId }
        if (index >= 0) {
            val updatedHistory = children[index].bmiHistory.toMutableList()
            if (updatedHistory.isNotEmpty()) {
                val latestRecord = updatedHistory.first()
                updatedHistory[0] = latestRecord.copy(
                    status = bmiStatus(latestRecord.bmi, ageMonths, gender)
                )
            }

            children[index] = children[index].copy(
                fullName = fullName,
                dateOfBirth = newDob,
                gender = gender,
                photoUrl = photoUrl,
                bmiHistory = updatedHistory
            )
        }
    }

    suspend fun sendAlert(
        childIds: List<String>,
        alertType: String,
        message: String
    ): Boolean {
        val date = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
        var allSuccess = true

        childIds.forEach { childId ->
            val alert = StatusAlert(
                childId = childId,
                alertType = alertType,
                message = message,
                sentBy = currentUser.value?.fullName ?: "BHW",
                date = date,
                timestamp = Timestamp.now()
            )

            val result = FirebaseRepository.sendAlert(alert)

            if (result.isSuccess) {
                val savedAlert = alert.copy(id = result.getOrNull() ?: "")
                if (alerts.none { it.id == savedAlert.id && savedAlert.id.isNotBlank() }) {
                    alerts.add(0, savedAlert)
                }
            } else {
                allSuccess = false
            }
        }

        return allSuccess
    }

    fun getAlertsForCurrentParent(): List<StatusAlert> {
        val parentId = currentUser.value?.id ?: return emptyList()

        return alerts.filter { alert ->
            val child = getChild(alert.childId)
            child?.parentId == parentId
        }
    }

    fun getCurrentDate(): String =
        SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
}

private fun fallbackBmiStatus(bmi: Double): String = when {
    bmi < 16.0 -> "Underweight"
    bmi < 25.0 -> "Normal"
    bmi < 30.0 -> "Overweight"
    else -> "Obese"
}

private val dobFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

private fun monthsFromDob(dateOfBirth: String): Int {
    return try {
        val birthDate = LocalDate.parse(dateOfBirth, dobFormatter)
        val today = LocalDate.now()
        if (birthDate.isAfter(today)) {
            0
        } else {
            val totalMonths = (today.year - birthDate.year) * 12 + (today.monthValue - birthDate.monthValue)
            if (today.dayOfMonth < birthDate.dayOfMonth) {
                (totalMonths - 1).coerceAtLeast(0)
            } else {
                totalMonths.coerceAtLeast(0)
            }
        }
    } catch (_: DateTimeParseException) {
        0
    }
}

private fun dobFromAgeMonths(ageMonths: Int): String {
    return LocalDate.now()
        .minusMonths(ageMonths.toLong())
        .format(dobFormatter)
}
