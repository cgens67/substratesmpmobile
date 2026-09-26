package com.joseph.substratesmp.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class AuthUserState(
  val uid: String = "local_bedrock_miner",
  val gamertag: String = "DiamondMiner42",
  val role: String = "MEMBER", // "ADMIN", "MOD", "BUILDER", "MEMBER"
  val isAnonymous: Boolean = true,
  val isFirebaseReady: Boolean = false,
  val isInitialized: Boolean = false
)

class AuthRepository(private val context: Context) {
  private val TAG = "SubstrateAuth"
  private val prefs: SharedPreferences =
    context.getSharedPreferences("substrate_auth_prefs", Context.MODE_PRIVATE)

  private val _userState = MutableStateFlow(
    AuthUserState(
      gamertag = prefs.getString("gamertag", "DiamondMiner42") ?: "DiamondMiner42",
      role = prefs.getString("role", "BUILDER") ?: "BUILDER"
    )
  )
  val userState: StateFlow<AuthUserState> = _userState.asStateFlow()

  suspend fun initializeAuth() {
    val savedGamertag = prefs.getString("gamertag", "DiamondMiner42") ?: "DiamondMiner42"
    val savedRole = prefs.getString("role", "BUILDER") ?: "BUILDER"

    var isReady = false
    var currentUid = "local_${System.currentTimeMillis() % 10000}"

    try {
      if (FirebaseApp.getApps(context).isEmpty()) {
        FirebaseApp.initializeApp(context)
      }

      val app = FirebaseApp.getInstance()
      Log.i(TAG, "Connected to Firebase project: ${app.options.projectId}")

      val auth = FirebaseAuth.getInstance()
      val currentUser = auth.currentUser
      if (currentUser != null) {
        currentUid = currentUser.uid
      } else {
        val authResult = auth.signInAnonymously().await()
        currentUid = authResult.user?.uid ?: currentUid
      }

      // Update display name with gamertag
      auth.currentUser?.updateProfile(
        userProfileChangeRequest {
          displayName = savedGamertag
        }
      )?.await()

      isReady = true
      Log.i(TAG, "Firebase Anonymous Auth established for gamertag: $savedGamertag (uid: $currentUid)")
    } catch (e: Exception) {
      Log.w(TAG, "Firebase Auth offline fallback: ${e.message}")
      isReady = false
    }

    _userState.value = AuthUserState(
      uid = currentUid,
      gamertag = savedGamertag,
      role = savedRole,
      isAnonymous = true,
      isFirebaseReady = isReady,
      isInitialized = true
    )
  }

  fun updateGamertag(newGamertag: String, newRole: String) {
    prefs.edit()
      .putString("gamertag", newGamertag)
      .putString("role", newRole)
      .apply()

    _userState.value = _userState.value.copy(
      gamertag = newGamertag,
      role = newRole
    )

    try {
      if (FirebaseApp.getApps(context).isNotEmpty()) {
        FirebaseAuth.getInstance().currentUser?.updateProfile(
          userProfileChangeRequest {
            displayName = newGamertag
          }
        )
      }
    } catch (_: Exception) {
      // Ignored if offline
    }
  }
}
