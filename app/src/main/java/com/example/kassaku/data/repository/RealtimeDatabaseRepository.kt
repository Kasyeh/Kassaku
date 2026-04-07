package com.example.kassaku.data.repository

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class RealtimeDatabaseRepository {

    private val database = FirebaseDatabase.getInstance("https://kassaku-8beb0-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val TAG = "RealtimeDB"

    /**
     * Listen to user balance updates in real-time
     */
    fun getUserBalanceFlow(userId: Int): Flow<Double?> = callbackFlow {
        val balanceRef = database.getReference("users/$userId/balance/saldo")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val balance = snapshot.getValue(Double::class.java)
                Log.d(TAG, "Balance updated: $balance")
                trySend(balance)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Balance listener cancelled: ${error.message}")
                trySend(null)
            }
        }

        balanceRef.addValueEventListener(listener)

        awaitClose {
            balanceRef.removeEventListener(listener)
        }
    }

    /**
     * Listen to user active status in real-time
     */
    fun getUserStatusFlow(userId: Int): Flow<Int?> = callbackFlow {
        val statusRef = database.getReference("users/$userId/status/active")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val active = snapshot.getValue(Int::class.java)
                Log.d(TAG, "Status active updated: $active")
                trySend(active)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Status listener cancelled: ${error.message}")
                trySend(null)
            }
        }

        statusRef.addValueEventListener(listener)

        awaitClose {
            statusRef.removeEventListener(listener)
        }
    }

    /**
     * Data class for unblock response from admin
     */
    data class UnblockResponseData(
        val status: String,
        val message: String?,
        val timestamp: Long
    )

    data class AccountEventData(
        val event: String,
        val message: String?,
        val timestamp: Long,
        val eventId: String?
    )

    /**
     * Listen to unblock response from admin in real-time
     * Admin writes to this RTDB node when they approve/reject an unblock request
     */
    fun getUnblockResponseFlow(userId: Int): Flow<UnblockResponseData?> = callbackFlow {
        val responseRef = database.getReference("users/$userId/unblock_response")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(null)
                    return
                }
                val status = snapshot.child("status").getValue(String::class.java)
                val message = snapshot.child("message").getValue(String::class.java)
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0
                if (status != null) {
                    Log.d(TAG, "Unblock response received: status=$status, message=$message")
                    trySend(UnblockResponseData(status, message, timestamp))
                } else {
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Unblock response listener cancelled: ${error.message}")
                trySend(null)
            }
        }

        responseRef.addValueEventListener(listener)

        awaitClose {
            responseRef.removeEventListener(listener)
        }
    }

    fun getAccountEventFlow(userId: Int): Flow<AccountEventData?> = callbackFlow {
        val eventRef = database.getReference("users/$userId/account_event")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(null)
                    return
                }

                val event = snapshot.child("event").getValue(String::class.java)
                val message = snapshot.child("message").getValue(String::class.java)
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                val eventId = snapshot.child("event_id").getValue(String::class.java)

                if (event != null) {
                    Log.d(TAG, "Account event received: event=$event, message=$message")
                    trySend(AccountEventData(event, message, timestamp, eventId))
                } else {
                    trySend(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Account event listener cancelled: ${error.message}")
                trySend(null)
            }
        }

        eventRef.addValueEventListener(listener)

        awaitClose {
            eventRef.removeEventListener(listener)
        }
    }

    /**
     * Clear unblock response after user has acknowledged it
     */
    fun clearUnblockResponse(userId: Int) {
        database.getReference("users/$userId/unblock_response").removeValue()
            .addOnSuccessListener { Log.d(TAG, "Unblock response cleared for user $userId") }
            .addOnFailureListener { Log.e(TAG, "Failed to clear unblock response: ${it.message}") }
    }
}
