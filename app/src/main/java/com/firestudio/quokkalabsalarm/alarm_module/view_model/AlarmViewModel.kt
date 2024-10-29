package com.firestudio.quokkalabsalarm.alarm_module.view_model

// AlarmViewModel.kt
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.firestudio.quokkalabsalarm.alarm_module.broadcast.AlarmReceiver
import com.firestudio.quokkalabsalarm.alarm_module.alarm_room_db.database.AlarmDatabase
import com.firestudio.quokkalabsalarm.alarm_module.alarm_room_db.entity.Alarm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmDao = AlarmDatabase.getDatabase(application).alarmDao()
    val allAlarms: LiveData<List<Alarm>> = alarmDao.getAllAlarms()

    fun scheduleAlarm(context: Context, calendar: Calendar, label: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val alarm = Alarm(timeInMillis = calendar.timeInMillis, label = label)
            alarmDao.insert(alarm)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("ALARM_ID", alarm.id)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    } else {

                        showDialogToRequestPermission(context)
                    }
                } else {

                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            } else {
                println("AlarmManager is not available.")
            }
        }
    }


    private fun showDialogToRequestPermission(context: Context) {
    }

    fun cancelAlarm(context: Context, alarm: Alarm) {
        viewModelScope.launch(Dispatchers.IO) {
            alarmDao.delete(alarm)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager?.cancel(pendingIntent)
        }
    }
}
