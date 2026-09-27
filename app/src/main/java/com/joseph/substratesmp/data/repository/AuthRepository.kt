package com.joseph.substratesmp.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class AuthUserState(
  val uid: String = "",
  val gamertag: String = "",
  val role: String = "MEMBER", // "ADMIN" or "MEMBER"
  val isAdmin: Boolean = false,
  val isAnonymous: Boolean = true,
  val isFirebaseReady: Boolean = false,
  val isInitialized: Boolean = false,
  val needsGamertagSetup: Boolean = false
)

class AuthRepository(private val context: Context) {
  private val TAG = "SubstrateAuth"
  private val prefs: SharedPreferences =
    context.getSharedPreferences("substrate_auth_prefs", Context.MODE_PRIVATE)

  private val firestore: FirebaseFirestore by lazy {
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
    FirebaseFirestore.getInstance()
  }

  private val _userState = MutableStateFlow(
    AuthUserState(
      gamertag = prefs.getString("gamertag", "") ?: "",
      isAdmin = prefs.getBoolean("isAdmin", false),
      role = if (prefs.getBoolean("isAdmin", false)) "ADMIN" else "MEMBER"
    )
  )
  val userState: StateFlow<AuthUserState> = _userState.asStateFlow()

  suspend fun initializeAuth() {
    var isReady = false
    var currentUid = "local_${System.currentTimeMillis() % 10000}"
    var savedGamertag = prefs.getString("gamertag", "") ?: ""
    var isAdmin = false

    try {
      if (FirebaseApp.getApps(context).isEmpty()) {
        FirebaseApp.initializeApp(context)
      }

      val auth = FirebaseAuth.getInstance()
      val currentUser = auth.currentUser
      if (currentUser != null) {
        currentUid = currentUser.uid
      } else {
        val authResult = auth.signInAnonymously().await()
        currentUid = authResult.user?.uid ?: currentUid
      }

      // Check Firestore users/{userId} for existing registered profile
      try {
        val userDoc = firestore.collection("users").document(currentUid).get().await()
        if (userDoc.exists()) {
          val remoteGamertag = userDoc.getString("gamertag") ?: ""
          if (remoteGamertag.isNotBlank()) {
            savedGamertag = remoteGamertag
            // Exclusive rule: isAdmin is true exclusively for Siang5680
            isAdmin = savedGamertag.equals("Siang5680", ignoreCase = true)
            prefs.edit()
              .putString("gamertag", savedGamertag)
              .putBoolean("isAdmin", isAdmin)
              .apply()
          }
        } else if (savedGamertag.isNotBlank()) {
          isAdmin = savedGamertag.equals("Siang5680", ignoreCase = true)
          firestore.collection("users").document(currentUid).set(
            mapOf("gamertag" to savedGamertag, "isAdmin" to isAdmin)
          ).await()
        }
      } catch (fe: Exception) {
        Log.w(TAG, "Firestore user profile read warning: ${fe.message}")
        if (savedGamertag.isNotBlank()) {
          isAdmin = savedGamertag.equals("Siang5680", ignoreCase = true)
        }
      }

      if (savedGamertag.isNotBlank()) {
        auth.currentUser?.updateProfile(
          userProfileChangeRequest {
            displayName = savedGamertag
          }
        )?.await()
      }

      isReady = true
      Log.i(TAG, "Auth initialized for user: $savedGamertag (uid: $currentUid, admin: $isAdmin)")
    } catch (e: Exception) {
      Log.w(TAG, "Firebase Auth offline fallback: ${e.message}")
      isReady = false
    }

    val needsSetup = savedGamertag.isBlank()
    val finalRole = if (isAdmin) "ADMIN" else "MEMBER"

    _userState.value = AuthUserState(
      uid = currentUid,
      gamertag = savedGamertag,
      role = finalRole,
      isAdmin = isAdmin,
      isAnonymous = true,
      isFirebaseReady = isReady,
      isInitialized = true,
      needsGamertagSetup = needsSetup
    )
  }

  suspend fun registerGamertag(newGamertag: String): Result<String> {
    val trimmed = newGamertag.trim()
    if (trimmed.length < 3) {
      return Result.failure(IllegalArgumentException("Gamertag must be at least 3 characters long."))
    }
    if (trimmed.length > 16) {
      return Result.failure(IllegalArgumentException("Gamertag cannot exceed 16 characters."))
    }

    val currentUid = _userState.value.uid

    // Check for exact duplicate gamertags in Firestore `users` collection
    try {
      val querySnapshot = firestore.collection("users")
        .whereEqualTo("gamertag", trimmed)
        .get()
        .await()

      val isDuplicate = querySnapshot.documents.any { it.id != currentUid }
      if (isDuplicate) {
        return Result.failure(IllegalStateException("Gamertag '$trimmed' is already registered by another player. Please pick a unique name."))
      }
    } catch (e: Exception) {
      Log.w(TAG, "Duplicate check exception: ${e.message}")
    }

    // Set isAdmin exclusively if gamertag is "Siang5680" (case-insensitive)
    val isAdmin = trimmed.equals("Siang5680", ignoreCase = true)
    val role = if (isAdmin) "ADMIN" else "MEMBER"

    try {
      firestore.collection("users").document(currentUid).set(
        mapOf(
          "gamertag" to trimmed,
          "isAdmin" to isAdmin
        )
      ).await()
    } catch (e: Exception) {
      Log.e(TAG, "Error saving user profile to Firestore: ${e.message}", e)
    }

    prefs.edit()
      .putString("gamertag", trimmed)
      .putBoolean("isAdmin", isAdmin)
      .apply()

    _userState.value = _userState.value.copy(
      gamertag = trimmed,
      role = role,
      isAdmin = isAdmin,
      needsGamertagSetup = false
    )

    try {
      FirebaseAuth.getInstance().currentUser?.updateProfile(
        userProfileChangeRequest {
          displayName = trimmed
        }
      )?.await()
    } catch (_: Exception) {}

    return Result.success(trimmed)
  }
}
