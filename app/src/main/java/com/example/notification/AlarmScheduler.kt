package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.MatchEntity
import com.example.receiver.MatchAlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    /**
     * Планирует напоминания для матча:
     * 1) За 1 час до начала игры
     * 2) В момент стартового свистка ("по свистку")
     */
    fun scheduleMatchReminders(match: MatchEntity) {
        val now = System.currentTimeMillis()

        // 1. Напоминание за 1 час (60 минут)
        val triggerAt1h = match.kickoffTimeMillis - (60 * 60 * 1000L)
        if (triggerAt1h > now) {
            val intent1h = Intent(context, MatchAlarmReceiver::class.java).apply {
                action = MatchAlarmReceiver.ACTION_REMINDER_ONE_HOUR
                putExtra(MatchAlarmReceiver.EXTRA_MATCH_ID, match.id)
                putExtra(MatchAlarmReceiver.EXTRA_TOURNAMENT, match.tournament)
                putExtra(MatchAlarmReceiver.EXTRA_HOME_TEAM, match.homeTeam)
                putExtra(MatchAlarmReceiver.EXTRA_AWAY_TEAM, match.awayTeam)
                putExtra(MatchAlarmReceiver.EXTRA_KICKOFF, match.kickoffTimeMillis)
                putExtra(MatchAlarmReceiver.EXTRA_STADIUM, match.stadium)
                putExtra(MatchAlarmReceiver.EXTRA_BROADCAST, match.broadcastChannel)
            }
            val pending1h = PendingIntent.getBroadcast(
                context,
                (match.id + "_1h").hashCode(),
                intent1h,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            scheduleAlarm(triggerAt1h, pending1h)
        }

        // 2. Напоминание "по свистку" (ровно в момент начала матча)
        val triggerAtWhistle = match.kickoffTimeMillis
        if (triggerAtWhistle > now) {
            val intentWhistle = Intent(context, MatchAlarmReceiver::class.java).apply {
                action = MatchAlarmReceiver.ACTION_REMINDER_WHISTLE
                putExtra(MatchAlarmReceiver.EXTRA_MATCH_ID, match.id)
                putExtra(MatchAlarmReceiver.EXTRA_TOURNAMENT, match.tournament)
                putExtra(MatchAlarmReceiver.EXTRA_HOME_TEAM, match.homeTeam)
                putExtra(MatchAlarmReceiver.EXTRA_AWAY_TEAM, match.awayTeam)
                putExtra(MatchAlarmReceiver.EXTRA_KICKOFF, match.kickoffTimeMillis)
                putExtra(MatchAlarmReceiver.EXTRA_STADIUM, match.stadium)
                putExtra(MatchAlarmReceiver.EXTRA_BROADCAST, match.broadcastChannel)
            }
            val pendingWhistle = PendingIntent.getBroadcast(
                context,
                (match.id + "_whistle").hashCode(),
                intentWhistle,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            scheduleAlarm(triggerAtWhistle, pendingWhistle)
        }
    }

    /**
     * Отменяет напоминания матча (и за 1 час, и по свистку).
     */
    fun cancelMatchReminders(matchId: String) {
        if (alarmManager == null) return

        // Отмена 1 часа
        val intent1h = Intent(context, MatchAlarmReceiver::class.java).apply {
            action = MatchAlarmReceiver.ACTION_REMINDER_ONE_HOUR
        }
        val pending1h = PendingIntent.getBroadcast(
            context,
            (matchId + "_1h").hashCode(),
            intent1h,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pending1h != null) {
            alarmManager.cancel(pending1h)
            pending1h.cancel()
        }

        // Отмена свистка
        val intentWhistle = Intent(context, MatchAlarmReceiver::class.java).apply {
            action = MatchAlarmReceiver.ACTION_REMINDER_WHISTLE
        }
        val pendingWhistle = PendingIntent.getBroadcast(
            context,
            (matchId + "_whistle").hashCode(),
            intentWhistle,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingWhistle != null) {
            alarmManager.cancel(pendingWhistle)
            pendingWhistle.cancel()
        }
    }

    /**
     * Утренний запуск в 09:00 для обновления расписания и отправки утреннего дайджеста.
     */
    fun scheduleDailyMorningUpdate(hourOfDay: Int = 9, minute: Int = 0) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, MatchAlarmReceiver::class.java).apply {
            action = MatchAlarmReceiver.ACTION_DAILY_MORNING_UPDATE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            MatchAlarmReceiver.REQUEST_CODE_MORNING_UPDATE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleAlarm(calendar.timeInMillis, pendingIntent)
    }

    private fun scheduleAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (alarmManager == null) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                10 * 60 * 1000L,
                pendingIntent
            )
        }
    }
}
