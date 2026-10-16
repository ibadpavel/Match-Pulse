package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AppDatabase
import com.example.notification.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val scheduler = AlarmScheduler(context)

                    scheduler.scheduleDailyMorningUpdate(9, 0)

                    val upcoming = db.matchDao().getUpcomingMatchesWithReminders(System.currentTimeMillis())
                    for (match in upcoming) {
                        scheduler.scheduleMatchReminders(match)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
