package com.joseph.substratesmp.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
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

  private var userDocListener: ListenerRegistration? = null

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
      role = prefs.getString("role", "MEMBER") ?: "MEMBER"
    )
  )
  val userState: StateFlow<AuthUserState> = _userState.asStateFlow()

  private fun hashPassword(password: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
  }

  private fun startLiveUserListener(uid: String) {
    userDocListener?.remove()
    userDocListener = firestore.collection("users").document(uid).addSnapshotListener { snapshot, _ ->
      if (snapshot != null && snapshot.exists()) {
        val liveRole = snapshot.getString("role") ?: "MEMBER"
        val liveAdmin = snapshot.getBoolean("isAdmin") ?: (liveRole == "ADMIN")
        val liveTag = snapshot.getString("gamertag") ?: _userState.value.gamertag

        prefs.edit()
          .putString("role", liveRole)
          .putBoolean("isAdmin", liveAdmin)
          .putString("gamertag", liveTag)
          .apply()

        _userState.value = _userState.value.copy(
          gamertag = liveTag,
          role = liveRole,
          isAdmin = liveAdmin
        )
      }
    }
  }

  suspend fun initializeAuth() {
    var isReady = false
    var currentUid = ""
    var savedGamertag = prefs.getString("gamertag", "") ?: ""
    var savedRole = prefs.getString("role", "MEMBER") ?: "MEMBER"
    var isAdmin = false

    try {
      val currentUser = auth.currentUser
      if (currentUser != null && savedGamertag.isNotBlank()) {
        currentUid = currentUser.uid
        val userDoc = firestore.collection("users").document(currentUid).get().await()
        if (userDoc.exists()) {
          savedGamertag = userDoc.getString("gamertag") ?: savedGamertag
          savedRole = userDoc.getString("role") ?: savedRole
        }
        isAdmin = savedGamertag.equals("Siang5680", ignoreCase = true) || savedRole == "ADMIN"
        isReady = true
        startLiveUserListener(currentUid)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Auth init check: ${e.message}")
    }

    val needsSetup = savedGamertag.isBlank() || currentUid.isBlank()
    _userState.value = AuthUserState(
      uid = currentUid,
      gamertag = savedGamertag,
      role = savedRole,
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
      val initialRole = if (isAdmin) "ADMIN" else "MEMBER"
      val passHash = hashPassword(pass)

      firestore.collection("gamertags").document(tagLower).set(
        hashMapOf(
          "uid" to uid,
          "gamertag" to cleanTag,
          "passwordHash" to passHash,
          "isAdmin" to isAdmin,
          "role" to initialRole,
          "createdAt" to System.currentTimeMillis()
        )
      ).await()

      firestore.collection("users").document(uid).set(
        hashMapOf(
          "gamertag" to cleanTag,
          "isAdmin" to isAdmin,
          "role" to initialRole,
          "createdAt" to System.currentTimeMillis()
        )
      ).await()

      auth.currentUser?.updateProfile(
        userProfileChangeRequest { displayName = cleanTag }
      )?.await()

      prefs.edit()
        .putString("gamertag", cleanTag)
        .putString("role", initialRole)
        .putBoolean("isAdmin", isAdmin)
        .apply()

      _userState.value = AuthUserState(
        uid = uid,
        gamertag = cleanTag,
        role = initialRole,
        isAdmin = isAdmin,
        isFirebaseReady = true,
        isInitialized = true,
        needsGamertagSetup = false
      )

      startLiveUserListener(uid)
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
      val userDoc = firestore.collection("users").document(uid).get().await()
      val actualRole = userDoc.getString("role") ?: tagDoc.getString("role") ?: "MEMBER"
      val isAdmin = actualTag.equals("Siang5680", ignoreCase = true) || actualRole == "ADMIN"

      prefs.edit()
        .putString("gamertag", actualTag)
        .putString("role", actualRole)
        .putBoolean("isAdmin", isAdmin)
        .apply()

      _userState.value = AuthUserState(
        uid = uid,
        gamertag = actualTag,
        role = actualRole,
        isAdmin = isAdmin,
        isFirebaseReady = true,
        isInitialized = true,
        needsGamertagSetup = false
      )

      startLiveUserListener(uid)
      Result.success(actualTag)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun logout() {
    userDocListener?.remove()
    try {
      auth.signOut()
    } catch (_: Exception) {}
    prefs.edit().clear().apply()
    _userState.value = AuthUserState(needsGamertagSetup = true)
  }
}
