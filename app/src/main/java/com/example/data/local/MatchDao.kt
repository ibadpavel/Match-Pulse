package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY kickoffTimeMillis ASC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE tournament = :tournament ORDER BY kickoffTimeMillis ASC")
    fun getMatchesByTournament(tournament: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE id = :id LIMIT 1")
    suspend fun getMatchById(id: String): MatchEntity?

    @Query("SELECT * FROM matches WHERE kickoffTimeMillis BETWEEN :startOfDay AND :endOfDay ORDER BY kickoffTimeMillis ASC")
    suspend fun getTodayMatches(startOfDay: Long, endOfDay: Long): List<MatchEntity>

    @Query("SELECT * FROM matches WHERE kickoffTimeMillis > :nowTime AND isReminderEnabled = 1 ORDER BY kickoffTimeMillis ASC")
    suspend fun getUpcomingMatchesWithReminders(nowTime: Long): List<MatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Query("UPDATE matches SET isReminderEnabled = :enabled WHERE id = :id")
    suspend fun updateReminder(id: String, enabled: Boolean)

    @Query("SELECT COUNT(*) FROM matches")
    suspend fun getMatchCount(): Int

    @Query("DELETE FROM matches")
    suspend fun clearAll()
}
