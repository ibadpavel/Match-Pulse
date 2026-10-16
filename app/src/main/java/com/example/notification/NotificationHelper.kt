package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.MatchEntity
import com.example.model.Tournament
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_MATCH_REMINDERS = "channel_uefa_match_reminders"
        const val CHANNEL_DAILY_DIGEST = "channel_uefa_daily_digest"

        const val NOTIFICATION_ID_DIGEST = 2001
        const val NOTIFICATION_ID_TEST_BASE = 3000
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_MATCH_REMINDERS,
                "Напоминания о матчах (за 1 час и по свистку)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления за 1 час до матча и по свистку к началу встречи"
                enableVibration(true)
            }

            val digestChannel = NotificationChannel(
                CHANNEL_DAILY_DIGEST,
                "Утренний дайджест игрового дня",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Утреннее уведомление о сегодняшних матчах с перечнем времени начала"
                enableVibration(true)
            }

            manager.createNotificationChannel(reminderChannel)
            manager.createNotificationChannel(digestChannel)
        }
    }

    /**
     * Утреннее уведомление после обновления списка:
     * "Сегодня N матчей ЛЧ/ЛЕ в <время>, <время>. Повторяющееся время не выводить."
     */
    fun showMorningDigest(todayMatches: List<MatchEntity>) {
        if (todayMatches.isEmpty()) return

        val timeFormat = SimpleDateFormat("HH:mm", Locale("ru"))
        val distinctTimes = todayMatches
            .map { timeFormat.format(Date(it.kickoffTimeMillis)) }
            .distinct()
            .sorted()
        val timesJoined = distinctTimes.joinToString(", ")

        val count = todayMatches.size
        val matchesWord = getMatchesWord(count)

        val tournCount = todayMatches.map { it.tournament }.distinct()
        val tourLabel = when {
            tournCount.contains(Tournament.CHAMPIONS_LEAGUE.name) && tournCount.contains(Tournament.EUROPA_LEAGUE.name) -> "ЛЧ/ЛЕ"
            tournCount.contains(Tournament.CHAMPIONS_LEAGUE.name) -> "ЛЧ"
            tournCount.contains(Tournament.EUROPA_LEAGUE.name) -> "ЛЕ"
            else -> "ЛЧ/ЛЕ"
        }

        val messageText = "Сегодня $count $matchesWord $tourLabel в $timesJoined"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_DIGEST,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val summaryList = todayMatches.joinToString("\n") { m ->
            "• ${timeFormat.format(Date(m.kickoffTimeMillis))} | ${m.homeTeam} — ${m.awayTeam} (${m.stage})"
        }

        val bigText = "$messageText\n\nРасписание игрового дня:\n$summaryList"

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_DIGEST)
            .setSmallIcon(R.drawable.ic_uefa_logo)
            .setContentTitle("⚽ UEFA Match Pulse")
            .setContentText(messageText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DIGEST, notification)
        } catch (_: SecurityException) {
        }
    }

    /**
     * Напоминание за 1 час до начала матча.
     */
    fun showOneHourReminder(
        notificationId: Int,
        tournamentName: String,
        homeTeam: String,
        awayTeam: String,
        kickoffTimeMillis: Long,
        stadium: String,
        broadcastChannel: String
    ) {
        val timeFormat = SimpleDateFormat("HH:mm", Locale("ru"))
        val kickoffFormatted = timeFormat.format(Date(kickoffTimeMillis))

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⏰ Через 1 час: $homeTeam — $awayTeam"
        val shortText = "Начало в $kickoffFormatted! $stadium"
        val bigText = "Через 1 час стартует матч $tournamentName:\n" +
                "$homeTeam — $awayTeam\n" +
                "⏰ Начало: $kickoffFormatted\n" +
                "📍 Стадион: $stadium\n" +
                "📺 Трансляция: $broadcastChannel"

        val notification = NotificationCompat.Builder(context, CHANNEL_MATCH_REMINDERS)
            .setSmallIcon(R.drawable.ic_uefa_logo)
            .setContentTitle(title)
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
        }
    }

    /**
     * Третье напоминание: "по свистку" (в момент начала матча).
     */
    fun showWhistleReminder(
        notificationId: Int,
        tournamentName: String,
        homeTeam: String,
        awayTeam: String,
        stadium: String,
        broadcastChannel: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "⚡ Стартовый свисток: $homeTeam — $awayTeam!"
        val shortText = "Матч начался! Включайте $broadcastChannel"
        val bigText = "Прозвучал стартовый свисток к началу матча $tournamentName!\n" +
                "$homeTeam — $awayTeam\n" +
                "🏟 Стадион: $stadium\n" +
                "📺 Трансляция: $broadcastChannel"

        val notification = NotificationCompat.Builder(context, CHANNEL_MATCH_REMINDERS)
            .setSmallIcon(R.drawable.ic_uefa_logo)
            .setContentTitle(title)
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
        }
    }

    /**
     * Тестовое уведомление, демонстрирующее всю схему (утреннее, за 1 час и по свистку).
     */
    fun showTestNotificationSuite() {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_TEST_BASE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MATCH_REMINDERS)
            .setSmallIcon(R.drawable.ic_uefa_logo)
            .setContentTitle("⚽ Схема напоминаний настроена")
            .setContentText("1. Утром: Сегодня N матчей в... 2. За 1 час до матча 3. По свистку")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Схема уведомлений активна:\n" +
                "🌅 Утром после обновления: «Сегодня N матчей ЛЧ/ЛЕ в 19:45, 22:00» (без повторов времени)\n" +
                "⏰ За 1 час до игры: напоминание о начале встречи\n" +
                "⚡ По свистку: мгновенное оповещение о старте матча"
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TEST_BASE, notification)
        } catch (_: SecurityException) {
        }
    }

    private fun getMatchesWord(count: Int): String {
        val mod10 = count % 10
        val mod100 = count % 100
        return when {
            mod100 in 11..19 -> "матчей"
            mod10 == 1 -> "матч"
            mod10 in 2..4 -> "матча"
            else -> "матчей"
        }
    }
}
