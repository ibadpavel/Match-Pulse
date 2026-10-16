package com.example.data.repository

import android.content.Context
import com.example.data.local.MatchDao
import com.example.data.local.MatchEntity
import com.example.data.remote.IcsScheduleParser
import com.example.data.remote.OnlineMatchFetcher
import com.example.data.remote.ScheduleDataSource
import com.example.model.Tournament
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MatchRepository(
    private val context: Context,
    private val matchDao: MatchDao,
    private val onlineMatchFetcher: OnlineMatchFetcher,
    private val icsScheduleParser: IcsScheduleParser,
    private val scheduleDataSource: ScheduleDataSource,
    private val alarmScheduler: AlarmScheduler,
    private val notificationHelper: NotificationHelper
) {

    private val prefs = context.getSharedPreferences("uefa_sync_prefs", Context.MODE_PRIVATE)

    fun observeMatches(tournament: Tournament? = null): Flow<List<MatchEntity>> {
        return if (tournament == null) {
            matchDao.getAllMatches()
        } else {
            matchDao.getMatchesByTournament(tournament.name)
        }
    }

    suspend fun getTodayMatches(): List<MatchEntity> = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        matchDao.getTodayMatches(start, end)
    }

    /**
     * Загружает полное расписание матчей Лиги Чемпионов и Лиги Европы по календарю UEFA.
     * После обновления списка утром отправляет 1 напоминание о сегодняшних матчах с перечнем времен начала.
     */
    suspend fun syncFullSchedule(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val combinedMatches = mutableListOf<MatchEntity>()

            // 1. Загрузка календаря Лиги Чемпионов УЕФА онлайн
            try {
                val uclMatches = onlineMatchFetcher.fetchChampionsLeagueOnline()
                combinedMatches.addAll(uclMatches)
            } catch (_: Exception) {}

            // 2. Загрузка календаря Лиги Европы УЕФА онлайн
            try {
                val uelMatches = onlineMatchFetcher.fetchEuropaLeagueOnline()
                combinedMatches.addAll(uelMatches)
            } catch (_: Exception) {}

            // 3. Если устройство оффлайн и загрузить не удалось, используем локальный начальный календарь
            if (combinedMatches.isEmpty()) {
                val existingCount = matchDao.getMatchCount()
                if (existingCount == 0) {
                    val seed = scheduleDataSource.fetchLatestFixtures()
                    combinedMatches.addAll(seed)
                } else {
                    checkAndSendMorningDigestIfNeeded()
                    return@withContext Result.success(existingCount)
                }
            }

            // Сохраняем пользовательские настройки напоминаний для матчей
            val mergedList = combinedMatches.map { fresh ->
                val existing = matchDao.getMatchById(fresh.id)
                if (existing != null) {
                    fresh.copy(isReminderEnabled = existing.isReminderEnabled)
                } else {
                    fresh
                }
            }

            matchDao.clearAll()
            matchDao.insertMatches(mergedList)

            // Перепланируем будильники для будущих матчей (за 1 час и по свистку)
            val now = System.currentTimeMillis()
            for (match in mergedList) {
                if (match.isReminderEnabled && match.kickoffTimeMillis > now) {
                    alarmScheduler.scheduleMatchReminders(match)
                }
            }
            alarmScheduler.scheduleDailyMorningUpdate(9, 0)

            // Проверяем и отправляем утреннее уведомление после обновления
            checkAndSendMorningDigestIfNeeded()

            Result.success(mergedList.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setReminderEnabled(matchId: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        matchDao.updateReminder(matchId, enabled)
        val match = matchDao.getMatchById(matchId)
        if (match != null) {
            if (enabled) {
                alarmScheduler.scheduleMatchReminders(match)
            } else {
                alarmScheduler.cancelMatchReminders(matchId)
            }
        }
    }

    /**
     * Отправляет утреннее напоминание после обновления списка:
     * "Сегодня N матчей ЛЧ/ЛЕ в <время>, <время>. Повторяющееся время не выводить."
     * Вызывается утром (между 06:00 и 12:00) не более 1 раза в день.
     */
    private suspend fun checkAndSendMorningDigestIfNeeded() {
        val nowCal = Calendar.getInstance()
        val hour = nowCal.get(Calendar.HOUR_OF_DAY)
        // Если утро (с 6 до 12 часов)
        if (hour in 6..12) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val todayStr = dateFormat.format(Date())
            val lastNotifiedDate = prefs.getString("last_morning_digest_date", null)

            if (lastNotifiedDate != todayStr) {
                val todayMatches = getTodayMatches()
                if (todayMatches.isNotEmpty()) {
                    notificationHelper.showMorningDigest(todayMatches)
                    prefs.edit().putString("last_morning_digest_date", todayStr).apply()
                }
            }
        }
    }
}
