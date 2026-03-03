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
                close(error.toException())
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
                close(error.toException())
            }
        }

        statusRef.addValueEventListener(listener)

        awaitClose {
            statusRef.removeEventListener(listener)
        }
    }
}
