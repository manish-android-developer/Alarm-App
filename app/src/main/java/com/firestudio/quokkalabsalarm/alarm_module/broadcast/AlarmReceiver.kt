package com.firestudio.quokkalabsalarm.alarm_module.broadcast

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver : BroadcastReceiver() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("ALARM_ID", 0)
        sendNotification(context, "Alarm $alarmId", "Your alarm is ringing!")

        // Play alarm sound
        playAlarmSound(context)
    }

    private fun sendNotification(context: Context, title: String, message: String) {
        val notificationId = (System.currentTimeMillis() % 10000).toInt()
        val builder = NotificationCompat.Builder(context, "alarm_channel")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            notify(notificationId, builder.build())
        }
    }

    private fun playAlarmSound(context: Context) {
       //Default Alarm
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

// custom ringtone
        val customSoundUri = android.net.Uri.parse("android.resource://${context.packageName}/raw/alarm")
        mediaPlayer = MediaPlayer.create(context, ringtoneUri).apply {
            isLooping = true
            setOnCompletionListener {
                start()
            }
            start()
        }


    }
}
