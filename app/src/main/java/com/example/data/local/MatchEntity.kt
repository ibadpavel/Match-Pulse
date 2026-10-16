package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey
    val id: String,
    val tournament: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeCountry: String,
    val awayCountry: String,
    val kickoffTimeMillis: Long,
    val stadium: String,
    val stage: String,
    val broadcastChannel: String = "Матч ТВ / Okko Спорт",
    val isReminderEnabled: Boolean = true,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)
