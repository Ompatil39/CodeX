package com.snapbudget.ocr.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.snapbudget.ocr.data.model.User
import kotlinx.coroutines.tasks.await

/**
 * Wraps [FirebaseAuth] calls into clean suspend functions.
 *
 * After a successful registration the user's Firestore profile is
 * created via [FirestoreRepository] so there's a single source of truth
 * for extended user data (budget, display name, etc.).
 */
class FirebaseAuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /** Currently signed-in Firebase user, or null. */
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /** Quick check — is a user signed in right now? */
    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    /**
     * Create a new account with email + password.
     *
     * On success the Firebase display-name is set and a Firestore
     * user-profile document is created.
     *
     * @return [Result.success] with the new [FirebaseUser], or
     *         [Result.failure] with the exception.
     */
    suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Registration succeeded but user is null"))

            // Set display name on the Firebase Auth profile
            val profileUpdate = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdate).await()

            // Create matching Firestore document
            val user = User(
                uid = firebaseUser.uid,
                displayName = displayName,
                email = email,
                createdAt = System.currentTimeMillis()
            )
            FirestoreRepository().saveUserProfile(user)

            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign in with email + password.
     *
     * @return [Result.success] with the signed-in [FirebaseUser], or
     *         [Result.failure] with the exception.
     */
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
                ?: return Result.failure(Exception("Login succeeded but user is null"))
            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Sign the current user out. */
    fun logout() {
        auth.signOut()
    }
}
