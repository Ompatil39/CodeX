package com.snapbudget.ocr.data.model

/**
 * Firestore-friendly user profile.
 *
 * The document ID in the `users` collection equals [uid].
 * A no-arg constructor is required by Firestore deserialization.
 */
data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val monthlyBudget: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
