package com.snapbudget.ocr.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.snapbudget.ocr.data.model.Transaction
import com.snapbudget.ocr.data.model.User
import kotlinx.coroutines.tasks.await

/**
 * Handles reads and writes to Cloud Firestore.
 *
 * Collection layout:
 * ```
 * users/{uid}                      ← User profile document
 * users/{uid}/transactions/{id}    ← Per-user transaction sub-collection
 * ```
 */
class FirestoreRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private companion object {
        const val USERS_COLLECTION = "users"
        const val TRANSACTIONS_SUB = "transactions"
    }

    // ─── User Profile ────────────────────────────────────────────────────

    /** Save (create or overwrite) the user profile document. */
    suspend fun saveUserProfile(user: User) {
        db.collection(USERS_COLLECTION)
            .document(user.uid)
            .set(user)
            .await()
    }

    /** Fetch the user profile for [uid], or null if it doesn't exist. */
    suspend fun getUserProfile(uid: String): User? {
        val snapshot = db.collection(USERS_COLLECTION)
            .document(uid)
            .get()
            .await()
        return snapshot.toObject(User::class.java)
    }

    /** Update specific fields on the user profile without overwriting the whole doc. */
    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>) {
        db.collection(USERS_COLLECTION)
            .document(uid)
            .update(updates)
            .await()
    }

    // ─── Transaction Sync ────────────────────────────────────────────────

    /**
     * Write a single transaction to the user's Firestore sub-collection.
     *
     * The transaction's local Room `id` is used as the document ID so
     * the same transaction is not duplicated on re-sync.
     */
    suspend fun syncTransaction(uid: String, transaction: Transaction) {
        val data = hashMapOf(
            "merchantName" to transaction.merchantName,
            "amount" to transaction.amount,
            "date" to transaction.date.time,           // store as epoch millis
            "category" to transaction.category,
            "gstNumber" to (transaction.gstNumber ?: ""),
            "notes" to (transaction.notes ?: ""),
            "confidenceScore" to transaction.confidenceScore,
            "createdAt" to transaction.createdAt.time,
            "updatedAt" to transaction.updatedAt.time
        )

        db.collection(USERS_COLLECTION)
            .document(uid)
            .collection(TRANSACTIONS_SUB)
            .document(transaction.id.toString())
            .set(data)
            .await()
    }

    /**
     * Fetch all transactions stored in Firestore for this user.
     *
     * Returns a list of generic maps — the caller decides whether to
     * convert them into Room [Transaction] entities.
     */
    suspend fun getCloudTransactions(uid: String): List<Map<String, Any>> {
        val snapshot = db.collection(USERS_COLLECTION)
            .document(uid)
            .collection(TRANSACTIONS_SUB)
            .get()
            .await()

        return snapshot.documents.mapNotNull { it.data }
    }
}
