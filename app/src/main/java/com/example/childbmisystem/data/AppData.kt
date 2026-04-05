
package com.example.childbmisystem.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Models ───────────────────────────────────────────────────────────────────

data class User(
    val id: String = "",
    val fullName: String = "",
    val username: String = "",
    val password: String = "",
    val role: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val address: String = ""
) {
    fun toMap() = mapOf(
        "fullName"    to fullName,
        "username"    to username.lowercase(),
        "role"        to role,
        "email"       to email,
        "phoneNumber" to phoneNumber,
        "address"     to address
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
    val recordedBy: String = ""
) {
    fun toMap() = mapOf(
        "date"       to date,
        "heightCm"   to heightCm,
        "weightKg"   to weightKg,
        "bmi"        to bmi,
        "status"     to status,
        "notes"      to notes,
        "recordedBy" to recordedBy,
        "timestamp"  to Timestamp.now()
    )
}

data class Child(
    val id: String = "",
    val fullName: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val parentId: String? = null,
    val bmiHistory: MutableList<BmiRecord> = mutableListOf()
) {
    val latestBmi: BmiRecord? get() = bmiHistory.lastOrNull()
    val bmiStatus: String get() = latestBmi?.status ?: "No Data"

    val ageYears: Int get() = try {
        val parts = dateOfBirth.split("-")
        val birthYear = parts.last().trim().toInt()
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        currentYear - birthYear
    } catch (e: Exception) { 0 }

    fun toMap() = mapOf(
        "fullName"    to fullName,
        "dateOfBirth" to dateOfBirth,
        "gender"      to gender,
        "parentId"    to (parentId ?: "")
    )
}

data class StatusAlert(
    val id: String = "",
    val childId: String = "",
    val alertType: String = "",
    val message: String = "",
    val sentBy: String = "",
    val date: String = ""
) {
    fun toMap() = mapOf(
        "childId"   to childId,
        "alertType" to alertType,
        "message"   to message,
        "sentBy"    to sentBy,
        "date"      to date
    )
}

// ─── Firebase-backed Store ────────────────────────────────────────────────────

object AppData {

    var currentUser = mutableStateOf<User?>(null)
    val children    = mutableStateListOf<Child>()
    val alerts      = mutableStateListOf<StatusAlert>()

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // ─── Auth ─────────────────────────────────────────────────────────────────

    // ✅ FIXED LOGIN (EMAIL-BASED)
    suspend fun login(email: String, password: String, role: String): Boolean {

        // 🔍 Get user using EMAIL instead of username
        val user = FirebaseRepository.getUserByEmail(email) ?: return false

        // 🔐 Check role
        if (!user.role.equals(role, ignoreCase = true)) return false

        // 🔑 Firebase Auth login
        val result = FirebaseRepository.loginWithEmail(email, password)

        return if (result.isSuccess) {
            currentUser.value = user
            loadData()
            true
        } else false
    }

    suspend fun register(
        fullName: String, username: String, email: String,
        password: String, role: String, phoneNumber: String, address: String
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
    }

    // ─── Data Loading ─────────────────────────────────────────────────────────

    fun loadData() {
        scope.launch {
            val loadedChildren = FirebaseRepository.loadChildren()
            children.clear()
            children.addAll(loadedChildren)

            val loadedAlerts = FirebaseRepository.loadAlerts()
            alerts.clear()
            alerts.addAll(loadedAlerts)
        }
    }

    // ─── Children ─────────────────────────────────────────────────────────────

    suspend fun addChild(fullName: String, dob: String, gender: String): Child {
        val child = Child(
            fullName = fullName,
            dateOfBirth = dob,
            gender = gender,
            parentId = currentUser.value?.id
        )

        val result = FirebaseRepository.addChild(child)
        val newChild = child.copy(id = result.getOrNull() ?: "")
        children.add(newChild)
        return newChild
    }

    suspend fun deleteChild(childId: String) {
        FirebaseRepository.deleteChild(childId)
        FirebaseRepository.deleteAlertsForChild(childId)

        children.removeAll { it.id == childId }
        alerts.removeAll { it.childId == childId }
    }

    fun getChild(childId: String): Child? =
        children.find { it.id == childId }

    fun childrenNeedingAttention(): Int =
        children.count { it.bmiStatus != "Normal" && it.bmiStatus != "No Data" }

    // ─── BMI ──────────────────────────────────────────────────────────────────

    fun calculateBmi(heightCm: Double, weightKg: Double): Double {
        if (heightCm <= 0) return 0.0
        val h = heightCm / 100.0
        return Math.round((weightKg / (h * h)) * 10.0) / 10.0
    }

    fun bmiStatus(bmi: Double): String = when {
        bmi <= 0   -> "No Data"
        bmi < 16.0 -> "Underweight"
        bmi < 25.0 -> "Normal"
        bmi < 30.0 -> "Overweight"
        else       -> "Obese"
    }

    suspend fun addBmiRecord(
        childId: String,
        heightCm: Double,
        weightKg: Double,
        notes: String,
        date: String
    ) {
        val bmi = calculateBmi(heightCm, weightKg)

        val record = BmiRecord(
            date = date,
            heightCm = heightCm,
            weightKg = weightKg,
            bmi = bmi,
            status = bmiStatus(bmi),
            notes = notes,
            recordedBy = currentUser.value?.fullName ?: "BHW"
        )

        val result = FirebaseRepository.addBmiRecord(childId, record)
        val newRecord = record.copy(id = result.getOrNull() ?: "")

        val index = children.indexOfFirst { it.id == childId }

        if (index >= 0) {
            val updated = children[index].bmiHistory.toMutableList()
            updated.add(newRecord)
            children[index] = children[index].copy(bmiHistory = updated)
        }
    }

    // ─── Alerts ───────────────────────────────────────────────────────────────

    suspend fun sendAlert(childIds: List<String>, alertType: String, message: String) {
        val date = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())

        childIds.forEach { childId ->
            val alert = StatusAlert(
                childId = childId,
                alertType = alertType,
                message = message,
                sentBy = currentUser.value?.fullName ?: "BHW",
                date = date
            )

            val result = FirebaseRepository.sendAlert(alert)
            alerts.add(alert.copy(id = result.getOrNull() ?: ""))
        }
    }

    fun getCurrentDate(): String =
        SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
}

