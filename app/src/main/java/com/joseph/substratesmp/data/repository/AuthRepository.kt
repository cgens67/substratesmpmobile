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
import java.security.MessageDigest

data class AuthUserState(
  val uid: String = "",
  val gamertag: String = "",
  val role: String = "MEMBER",
  val isAdmin: Boolean = false,
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

  private val auth: FirebaseAuth by lazy {
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }
    FirebaseAuth.getInstance()
  }

  private val _userState = MutableStateFlow(
    AuthUserState(
      gamertag = prefs.getString("gamertag", "") ?: "",
      isAdmin = prefs.getBoolean("isAdmin", false),
      role = if (prefs.getBoolean("isAdmin", false)) "ADMIN" else "MEMBER"
    )
  )
  val userState: StateFlow<AuthUserState> = _userState.asStateFlow()

  private fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
  }

  suspend fun initializeAuth() {
    var isReady = false
    var currentUid = ""
    var savedGamertag = prefs.getString("gamertag", "") ?: ""
    var isAdmin = false

    try {
      val currentUser = auth.currentUser
      if (currentUser != null && savedGamertag.isNotBlank()) {
        currentUid = currentUser.uid
        val userDoc = firestore.collection("users").document(currentUid).get().await()
        if (userDoc.exists()) {
          savedGamertag = userDoc.getString("gamertag") ?: savedGamertag
        }
        isAdmin = savedGamertag.equals("Siang5680", ignoreCase = true)
        isReady = true
      }
    } catch (e: Exception) {
      Log.w(TAG, "Auth init check: ${e.message}")
    }

    val needsSetup = savedGamertag.isBlank() || currentUid.isBlank()
    _userState.value = AuthUserState(
      uid = currentUid,
      gamertag = savedGamertag,
      role = if (isAdmin) "ADMIN" else "MEMBER",
      isAdmin = isAdmin,
      isFirebaseReady = isReady,
      isInitialized = true,
      needsGamertagSetup = needsSetup
    )
  }

  suspend fun registerAccount(gamertag: String, pass: String): Result<String> {
    val cleanTag = gamertag.trim()
    if (cleanTag.length < 3) return Result.failure(IllegalArgumentException("Gamertag must be at least 3 characters"))
    if (cleanTag.length > 16) return Result.failure(IllegalArgumentException("Gamertag cannot exceed 16 characters"))
    if (pass.length < 6) return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))

    val tagLower = cleanTag.lowercase()
    val authEmail = "$tagLower@substratesmp.app"

    return try {
      val tagDoc = firestore.collection("gamertags").document(tagLower).get().await()
      if (tagDoc.exists()) {
        return Result.failure(IllegalStateException("Gamertag '$cleanTag' is already registered. Please log in."))
      }

      val authRes = auth.createUserWithEmailAndPassword(authEmail, pass).await()
      val uid = authRes.user?.uid ?: "user_${System.currentTimeMillis()}"

      val isAdmin = cleanTag.equals("Siang5680", ignoreCase = true)
      val passHash = hashPassword(pass)

      firestore.collection("gamertags").document(tagLower).set(
        hashMapOf(
          "uid" to uid,
          "gamertag" to cleanTag,
          "passwordHash" to passHash,
          "isAdmin" to isAdmin,
          "createdAt" to System.currentTimeMillis()
        )
      ).await()

      firestore.collection("users").document(uid).set(
        hashMapOf(
          "gamertag" to cleanTag,
          "isAdmin" to isAdmin,
          "role" to if (isAdmin) "ADMIN" else "MEMBER",
          "createdAt" to System.currentTimeMillis()
        )
      ).await()

      auth.currentUser?.updateProfile(
        userProfileChangeRequest { displayName = cleanTag }
      )?.await()

      prefs.edit()
        .putString("gamertag", cleanTag)
        .putBoolean("isAdmin", isAdmin)
        .apply()

      _userState.value = AuthUserState(
        uid = uid,
        gamertag = cleanTag,
        role = if (isAdmin) "ADMIN" else "MEMBER",
        isAdmin = isAdmin,
        isFirebaseReady = true,
        isInitialized = true,
        needsGamertagSetup = false
      )

      Result.success(cleanTag)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun loginAccount(gamertag: String, pass: String): Result<String> {
    val cleanTag = gamertag.trim()
    if (cleanTag.isBlank()) return Result.failure(IllegalArgumentException("Please enter your Gamertag"))
    if (pass.isBlank()) return Result.failure(IllegalArgumentException("Please enter your password"))

    val tagLower = cleanTag.lowercase()
    val authEmail = "$tagLower@substratesmp.app"

    return try {
      val tagDoc = firestore.collection("gamertags").document(tagLower).get().await()
      if (!tagDoc.exists()) {
        return Result.failure(IllegalStateException("No account found for Gamertag '$cleanTag'."))
      }

      val actualTag = tagDoc.getString("gamertag") ?: cleanTag
      val storedHash = tagDoc.getString("passwordHash") ?: ""
      val inputHash = hashPassword(pass)

      try {
        auth.signInWithEmailAndPassword(authEmail, pass).await()
      } catch (authEx: Exception) {
        if (storedHash.isNotEmpty() && storedHash != inputHash) {
          return Result.failure(IllegalStateException("Incorrect password for '$actualTag'."))
        }
        if (auth.currentUser == null) {
          auth.signInAnonymously().await()
        }
      }

      val uid = auth.currentUser?.uid ?: tagDoc.getString("uid") ?: "user_recovered"
      val isAdmin = actualTag.equals("Siang5680", ignoreCase = true)

      prefs.edit()
        .putString("gamertag", actualTag)
        .putBoolean("isAdmin", isAdmin)
        .apply()

      _userState.value = AuthUserState(
        uid = uid,
        gamertag = actualTag,
        role = if (isAdmin) "ADMIN" else "MEMBER",
        isAdmin = isAdmin,
        isFirebaseReady = true,
        isInitialized = true,
        needsGamertagSetup = false
      )

      Result.success(actualTag)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun logout() {
    try {
      auth.signOut()
    } catch (_: Exception) {}
    prefs.edit().clear().apply()
    _userState.value = AuthUserState(needsGamertagSetup = true)
  }
}
