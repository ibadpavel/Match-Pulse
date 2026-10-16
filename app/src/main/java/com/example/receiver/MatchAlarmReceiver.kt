package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.UefaApplication
import com.example.data.local.AppDatabase
import com.example.model.Tournament
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class MatchAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DAILY_MORNING_UPDATE = "com.example.ACTION_DAILY_MORNING_UPDATE"
        const val ACTION_REMINDER_ONE_HOUR = "com.example.ACTION_REMINDER_ONE_HOUR"
        const val ACTION_REMINDER_WHISTLE = "com.example.ACTION_REMINDER_WHISTLE"

        const val EXTRA_MATCH_ID = "extra_match_id"
        const val EXTRA_TOURNAMENT = "extra_tournament"
        const val EXTRA_HOME_TEAM = "extra_home_team"
        const val EXTRA_AWAY_TEAM = "extra_away_team"
        const val EXTRA_KICKOFF = "extra_kickoff"
        const val EXTRA_STADIUM = "extra_stadium"
        const val EXTRA_BROADCAST = "extra_broadcast"

        const val REQUEST_CODE_MORNING_UPDATE = 9001
    }

    override fun onReceive(context: Context, intent: Intent) {
        val helper = NotificationHelper(context)

        when (intent.action) {
            // 1. Утреннее напоминание после обновления списка
            ACTION_DAILY_MORNING_UPDATE -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val app = context.applicationContext as? UefaApplication
                        if (app != null) {
                            app.repository.syncFullSchedule()
                        }

                        val db = AppDatabase.getInstance(context)
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, 0)
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        val startOfDay = cal.timeInMillis

                        cal.set(Calendar.HOUR_OF_DAY, 23)
                        cal.set(Calendar.MINUTE, 59)
                        cal.set(Calendar.SECOND, 59)
                        cal.set(Calendar.MILLISECOND, 999)
                        val endOfDay = cal.timeInMillis

                        val todayMatches = db.matchDao().getTodayMatches(startOfDay, endOfDay)
                        if (todayMatches.isNotEmpty()) {
                            helper.showMorningDigest(todayMatches)
                        }

                        // Перепланируем на следующее утро
                        AlarmScheduler(context).scheduleDailyMorningUpdate(9, 0)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            // 2. Напоминание за 1 час до начала матча
            ACTION_REMINDER_ONE_HOUR -> {
                val matchId = intent.getStringExtra(EXTRA_MATCH_ID) ?: return
                val tournamentRaw = intent.getStringExtra(EXTRA_TOURNAMENT) ?: Tournament.CHAMPIONS_LEAGUE.name
                val tournamentName = if (tournamentRaw == Tournament.EUROPA_LEAGUE.name) {
                    Tournament.EUROPA_LEAGUE.title
                } else {
                    Tournament.CHAMPIONS_LEAGUE.title
                }
                val homeTeam = intent.getStringExtra(EXTRA_HOME_TEAM) ?: "Хозяева"
                val awayTeam = intent.getStringExtra(EXTRA_AWAY_TEAM) ?: "Гости"
                val kickoff = intent.getLongExtra(EXTRA_KICKOFF, System.currentTimeMillis())
                val stadium = intent.getStringExtra(EXTRA_STADIUM) ?: "Стадион турнира"
                val broadcast = intent.getStringExtra(EXTRA_BROADCAST) ?: "Матч ТВ / Okko"

                helper.showOneHourReminder(
                    notificationId = (matchId + "_1h").hashCode(),
                    tournamentName = tournamentName,
                    homeTeam = homeTeam,
                    awayTeam = awayTeam,
                    kickoffTimeMillis = kickoff,
                    stadium = stadium,
                    broadcastChannel = broadcast
                )
            }

            // 3. Третье напоминание: "по свистку" (в момент начала матча)
            ACTION_REMINDER_WHISTLE -> {
                val matchId = intent.getStringExtra(EXTRA_MATCH_ID) ?: return
                val tournamentRaw = intent.getStringExtra(EXTRA_TOURNAMENT) ?: Tournament.CHAMPIONS_LEAGUE.name
                val tournamentName = if (tournamentRaw == Tournament.EUROPA_LEAGUE.name) {
                    Tournament.EUROPA_LEAGUE.title
                } else {
                    Tournament.CHAMPIONS_LEAGUE.title
                }
                val homeTeam = intent.getStringExtra(EXTRA_HOME_TEAM) ?: "Хозяева"
                val awayTeam = intent.getStringExtra(EXTRA_AWAY_TEAM) ?: "Гости"
                val stadium = intent.getStringExtra(EXTRA_STADIUM) ?: "Стадион турнира"
                val broadcast = intent.getStringExtra(EXTRA_BROADCAST) ?: "Матч ТВ / Okko"

                helper.showWhistleReminder(
                    notificationId = (matchId + "_whistle").hashCode(),
                    tournamentName = tournamentName,
                    homeTeam = homeTeam,
                    awayTeam = awayTeam,
                    stadium = stadium,
                    broadcastChannel = broadcast
                )
            }
        }
    }
}
