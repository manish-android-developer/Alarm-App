package com.firestudio.quokkalabsalarm.alarm_module.alarm_room_db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.firestudio.quokkalabsalarm.alarm_module.alarm_room_db.entity.Alarm

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: Alarm)

    @Delete
    suspend fun delete(alarm: Alarm)

    @Query("SELECT * FROM alarms ORDER BY timeInMillis ASC")
    fun getAllAlarms(): LiveData<List<Alarm>>
}