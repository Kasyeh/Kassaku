package com.example.kassaku.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.kassaku.MainActivity
import com.example.kassaku.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "kassaku_notifications"
        private const val CHANNEL_NAME = "Kassaku Notifications"
    }

    /**
     * Dipanggil ketika FCM token baru di-generate
     * Token ini harus dikirim ke backend Laravel
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")
        
        // TODO: Kirim token ini ke backend Laravel
        sendTokenToServer(token)
    }

    /**
     * Dipanggil ketika menerima notifikasi FCM
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "From: ${message.from}")
        
        // Cek apakah ada notification payload
        message.notification?.let {
            Log.d(TAG, "Notification Title: ${it.title}")
            Log.d(TAG, "Notification Body: ${it.body}")
            
            showNotification(it.title, it.body)
        }
        
        // Cek apakah ada data payload
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Data Payload: ${message.data}")
            
            // Handle custom data di sini
            handleDataPayload(message.data)

            // Jika notification object kosong (data-only message), 
            // atau jika kita ingin memaksa notifikasi muncul di foreground
            if (message.notification == null) {
                val title = message.data["title"] ?: "Kassaku"
                val body = message.data["message"] ?: "Ada update baru"
                showNotification(title, body)
            }
        }
    }

    /**
     * Tampilkan notifikasi di system tray
     */
    private fun showNotification(title: String?, body: String?) {
        Log.d(TAG, "Showing notification: $title - $body")
        createNotificationChannel()
        
        // Intent ketika notifikasi di-tap
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notifikasi
        val logoBitmap = BitmapFactory.decodeResource(resources, R.drawable.logo)
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo) // Use drawable logo for better system compatibility
            .setLargeIcon(logoBitmap)
            .setContentTitle(title ?: "Kassaku")
            .setContentText(body ?: "Ada notifikasi baru")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        // Tampilkan notifikasi
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
            Log.d(TAG, "Notification triggered via NotificationManager")
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering notification: ${e.message}")
        }
    }

    /**
     * Buat notification channel (wajib untuk Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi dari Kassaku"
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Handle custom data payload
     */
    private fun handleDataPayload(data: Map<String, String>) {
        val type = data["type"]
        
        when (type) {
            "transaction" -> {
                // Handle notifikasi transaksi baru
                Log.d(TAG, "Transaction notification: ${data["message"]}")
            }
            "reminder" -> {
                // Handle notifikasi reminder
                Log.d(TAG, "Reminder notification: ${data["message"]}")
            }
            "admin" -> {
                // Handle notifikasi dari admin
                Log.d(TAG, "Admin Action: ${data["action"]}")
                val message = data["message"] ?: "Pesan dari admin"
                Log.d(TAG, "Admin message: $message")
                // Notifikasi sudah dipicu oleh onMessageReceived di atas jika data-only
            }
        }
    }

    /**
     * Kirim FCM token ke backend Laravel
     */
    private fun sendTokenToServer(token: String) {
        // Get user ID from SharedPreferences
        val sharedPref = getSharedPreferences("KassakuPrefs", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        
        if (userId == -1) {
            Log.w(TAG, "User ID not found, cannot send token to server")
            return
        }
        
        // Send to server menggunakan coroutine
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val response = com.example.kassaku.data.remote.ApiClient.api.saveFcmToken(token, userId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d(TAG, "FCM token sent to server successfully")
                } else {
                    Log.e(TAG, "Failed to send FCM token: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending FCM token to server: ${e.message}")
            }
        }
    }
}
