package com.firestudio.quokkalabsalarm.alarm_module.screen

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.firestudio.quokkalabsalarm.alarm_module.adapter.AlarmAdapter
import com.firestudio.quokkalabsalarm.databinding.ActivityMainBinding
import com.firestudio.quokkalabsalarm.alarm_module.view_model.AlarmViewModel
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var alarmAdapter: AlarmAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var alarmViewModel: AlarmViewModel
    private val selectedAlarms = mutableListOf<Calendar>() // Class-level property
    private val requestNotificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            createNotificationChannel()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmViewModel = ViewModelProvider(this)[AlarmViewModel::class.java]

        alarmAdapter = AlarmAdapter()
        requestPermissions()

        binding.alarmRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = alarmAdapter
        }

        alarmViewModel.allAlarms.observe(this) { alarms ->
            alarmAdapter.setAlarms(alarms)
        }

        binding.addAlarmFabButton.setOnClickListener {
            showDateTimePickerDialog()
        }
    }

    private fun requestPermissions() {
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                createNotificationChannel()
            }
        } else {
            createNotificationChannel()
        }

        // Handle exact alarm permission for Android 12 and above
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Please enable exact alarm permission in settings", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 and 13
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Exact alarm permission required. Please enable it in settings.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            calendar.set(Calendar.SECOND, 0)
            alarmViewModel.scheduleAlarm(this, calendar, "New Alarm")
        }, hour, minute, true)

        timePickerDialog.show()
    }


    private fun showDateTimePickerDialog() {
        val calendar = Calendar.getInstance()

        // Function to show DatePicker and TimePicker
        fun showPicker() {
            val datePickerDialog = DatePickerDialog(this,
                { _, year, month, dayOfMonth ->
                    val timePickerDialog = TimePickerDialog(this, { _, hour, minute ->
                        val alarmCalendar = Calendar.getInstance()
                        alarmCalendar.set(year, month, dayOfMonth, hour, minute, 0)

                        selectedAlarms.add(alarmCalendar) // Add selected time to list
                        // Ask if the user wants to add another alarm
                        showAddAnotherAlarmDialog()
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

                    timePickerDialog.show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        showPicker() // Show the first picker
    }

    // Function to ask user if they want to add another alarm
    private fun showAddAnotherAlarmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Add another alarm?")
            .setPositiveButton("Yes") { _, _ ->
                showDateTimePickerDialog() // Show picker again
            }
            .setNegativeButton("No") { _, _ ->
                // Schedule all selected alarms
                scheduleAllAlarms()
            }
            .show()
    }

    // Function to schedule all selected alarms
    private fun scheduleAllAlarms() {
        selectedAlarms.forEach { alarmTime ->
            alarmViewModel.scheduleAlarm(this, alarmTime, "⏰ New Alarm")
        }
        // Clear the list if needed
        selectedAlarms.clear()
    }




    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alarm Channel"
            val descriptionText = "Channel for Alarm notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("alarm_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
