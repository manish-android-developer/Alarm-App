package com.firestudio.quokkalabsalarm.alarm_module.adapter

// AlarmAdapter.kt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firestudio.quokkalabsalarm.R
import com.firestudio.quokkalabsalarm.alarm_module.alarm_room_db.entity.Alarm
import java.text.SimpleDateFormat
import java.util.*

class AlarmAdapter : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    private val alarms = mutableListOf<Alarm>()

    fun setAlarms(newAlarms: List<Alarm>) {
        alarms.clear()
        alarms.addAll(newAlarms)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.bind(alarm)
    }

    override fun getItemCount(): Int = alarms.size

    inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        private val labelTextView: TextView = itemView.findViewById(R.id.labelTextView)

        fun bind(alarm: Alarm) {
            val dateFormat = SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault())
            timeTextView.text = dateFormat.format(Date(alarm.timeInMillis))
            labelTextView.text = alarm.label
        }
    }
}
