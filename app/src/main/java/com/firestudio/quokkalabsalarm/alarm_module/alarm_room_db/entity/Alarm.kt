package com.firestudio.quokkalabsalarm.alarm_module.alarm_room_db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timeInMillis: Long,
    val label: String = "Alarm"

)
