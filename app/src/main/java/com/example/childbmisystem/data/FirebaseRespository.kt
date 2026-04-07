package com.example.childbmisystem.data

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FirebaseRepository {

    private const val TAG = "FirebaseRepository"

    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    val db: FirebaseFirestore get() = FirebaseFirestore.getInstance()
    val storage: FirebaseStorage get() = FirebaseStorage.getInstance()

    val currentUid: String?
        get() = auth.currentUser?.uid

    private var childrenListener: ListenerRegistration? = null

    // ───────────────── AUTH ─────────────────

    suspend fun loginWithEmail(email: String, password: String): Result<Unit> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Login failed", e)
        Result.failure(e)
    }

    suspend fun registerWithEmail(email: String, password: String): Result<String> = try {
        val res = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = res.user?.uid ?: ""
        Result.success(uid)
    } catch (e: Exception) {
        Log.e(TAG, "Registration failed", e)
        Result.failure(e)
    }

    fun logout() {
        stopListeningToChildren()
        auth.signOut()
    }

    // ───────────────── USERS ─────────────────

    suspend fun saveUser(user: User): Result<Unit> = try {
        val normalizedUser = user.copy(username = user.username.lowercase())
        db.collection("users")
            .document(normalizedUser.id)
            .set(normalizedUser.toMap())
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Save user failed", e)
        Result.failure(e)
    }

    suspend fun getUser(userId: String): User? = try {
        val doc = db.collection("users").document(userId).get().await()
        if (doc.exists()) doc.toUser() else null
    } catch (e: Exception) {
        Log.e(TAG, "Get user failed", e)
        null
    }

    suspend fun getUserByEmail(email: String): User? = try {
        val snap = db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .await()
        snap.documents.firstOrNull()?.toUser()
    } catch (e: Exception) {
        Log.e(TAG, "Get user by email failed", e)
        null
    }

    suspend fun getUserByUsername(username: String): User? = try {
        val snap = db.collection("users")
            .whereEqualTo("username", username.lowercase())
            .get()
            .await()
        snap.documents.firstOrNull()?.toUser()
    } catch (e: Exception) {
        Log.e(TAG, "Get user by username failed", e)
        null
    }

    private fun DocumentSnapshot.toUser() = User(
        id = id,
        fullName = getString("fullName") ?: "",
        username = getString("username") ?: "",
        role = getString("role") ?: "",
        email = getString("email") ?: "",
        phoneNumber = getString("phoneNumber") ?: "",
        address = getString("address") ?: ""
    )

    // ───────────────── CHILDREN (REAL‑TIME) ─────────────────

    fun startListeningToChildren(role: String, onChildrenChanged: (List<Child>) -> Unit) {
        stopListeningToChildren()

        val query: Query = when {
            currentUid == null -> db.collection("children")
            role.equals("bhw", ignoreCase = true) -> db.collection("children")
            else -> db.collection("children").whereEqualTo("parentId", currentUid)
        }

        childrenListener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Listen to children failed", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            val childrenWithoutHistory = snapshot.documents.mapNotNull { doc ->
                Child(
                    id = doc.id,
                    fullName = doc.getString("fullName") ?: "",
                    dateOfBirth = doc.getString("dateOfBirth") ?: "",
                    gender = doc.getString("gender") ?: "",
                    parentId = doc.getString("parentId"),
                    photoUrl = doc.getString("photoUrl") ?: ""
                )
            }

            CoroutineScope(Dispatchers.IO).launch {
                val childrenWithHistory = childrenWithoutHistory.map { child ->
                    val records = loadBmiRecords(child.id)
                    child.copy(bmiHistory = records.toMutableList())
                }
                withContext(Dispatchers.Main) {
                    onChildrenChanged(childrenWithHistory)
                }
            }
        }
    }

    fun stopListeningToChildren() {
        childrenListener?.remove()
        childrenListener = null
    }

    suspend fun addChild(child: Child): Result<String> = try {
        val ref = db.collection("children").add(child.toMap()).await()
        Result.success(ref.id)
    } catch (e: Exception) {
        Log.e(TAG, "Add child failed", e)
        Result.failure(e)
    }

    suspend fun deleteChild(childId: String): Result<Unit> = try {
        db.collection("children").document(childId).delete().await()
        deleteChildPhoto(childId)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Delete child failed", e)
        Result.failure(e)
    }

    // ───────────────── CHILD PHOTO UPLOAD ─────────────────

    suspend fun uploadChildPhoto(childId: String, imageUri: Uri): Result<String> = try {
        val storageRef = storage.reference.child("child_photos/$childId.jpg")
        storageRef.putFile(imageUri).await()
        val downloadUrl = storageRef.downloadUrl.await()
        Result.success(downloadUrl.toString())
    } catch (e: Exception) {
        Log.e(TAG, "Upload child photo failed", e)
        Result.failure(e)
    }

    suspend fun deleteChildPhoto(childId: String): Result<Unit> = try {
        val storageRef = storage.reference.child("child_photos/$childId.jpg")
        storageRef.delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Log.d(TAG, "No photo to delete for child $childId")
        Result.success(Unit)
    }

    // ───────────────── BMI RECORDS ─────────────────

    suspend fun loadBmiRecords(childId: String): List<BmiRecord> = try {
        db.collection("children")
            .document(childId)
            .collection("bmiRecords")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents.map { doc ->
                BmiRecord(
                    id = doc.id,
                    date = doc.getString("date") ?: "",
                    heightCm = doc.getDouble("heightCm") ?: 0.0,
                    weightKg = doc.getDouble("weightKg") ?: 0.0,
                    bmi = doc.getDouble("bmi") ?: 0.0,
                    status = doc.getString("status") ?: "",
                    notes = doc.getString("notes") ?: "",
                    recordedBy = doc.getString("recordedBy") ?: ""
                )
            }
    } catch (e: Exception) {
        Log.e(TAG, "Load BMI records failed", e)
        emptyList()
    }

    suspend fun addBmiRecord(childId: String, record: BmiRecord): Result<String> = try {
        val ref = db.collection("children")
            .document(childId)
            .collection("bmiRecords")
            .add(record.toMap())
            .await()
        Result.success(ref.id)
    } catch (e: Exception) {
        Log.e(TAG, "Add BMI record failed", e)
        Result.failure(e)
    }

    // ───────────────── ALERTS ─────────────────

    suspend fun sendAlert(alert: StatusAlert): Result<String> = try {
        val ref = db.collection("alerts").add(alert.toMap()).await()
        Result.success(ref.id)
    } catch (e: Exception) {
        Log.e(TAG, "Send alert failed", e)
        Result.failure(e)
    }

    suspend fun loadAlerts(): List<StatusAlert> = try {
        db.collection("alerts")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()
            .documents.map { doc ->
                StatusAlert(
                    id = doc.id,
                    childId = doc.getString("childId") ?: "",
                    alertType = doc.getString("alertType") ?: "",
                    message = doc.getString("message") ?: "",
                    sentBy = doc.getString("sentBy") ?: "",
                    date = doc.getString("date") ?: ""
                )
            }
    } catch (e: Exception) {
        Log.e(TAG, "Load alerts failed", e)
        emptyList()
    }

    suspend fun deleteAlertsForChild(childId: String): Result<Unit> = try {
        val snap = db.collection("alerts")
            .whereEqualTo("childId", childId)
            .get()
            .await()
        snap.documents.forEach { it.reference.delete().await() }
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Delete alerts failed", e)
        Result.failure(e)
    }
}