package com.arifaizin.myfirebaseapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private val TAG = MyFirebaseMessagingService::class.java.simpleName
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.from)

        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            sendNotification(remoteMessage)
        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification?.body)

            sendNotification(remoteMessage.notification?.body)

        }
    }

    private fun sendNotification(messageBody: RemoteMessage) {
        val channelId = "21"
        val channelName = "ChatNotification"
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Pesan dari "+messageBody.data.get("sender"))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText(messageBody.data.get("message"))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationBuilder.setChannelId(channelId)
            mNotificationManager.createNotificationChannel(channel)
        }
        val notification = notificationBuilder.build()
        mNotificationManager.notify(0, notification)
    }


    private fun sendNotification(messageBody: String?) {
        val channelId = "21"
        val channelName = "ChatNotification"
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationBuilder.setChannelId(channelId)
            mNotificationManager.createNotificationChannel(channel)
        }
        val notification = notificationBuilder.build()
        mNotificationManager.notify(0, notification)
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        val client = OkHttpClient()
        val body = FormBody.Builder()
            .add("Token", token)
            .build()

        val request = Request.Builder()
            .url("https://firebasefcm.000webhostapp.com/firebasefcm/register.php")
            .post(body)
            .build()

        try {
            client.newCall(request).execute()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}
