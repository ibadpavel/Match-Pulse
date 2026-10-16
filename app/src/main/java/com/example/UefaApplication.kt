package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.remote.IcsScheduleParser
import com.example.data.remote.OnlineMatchFetcher
import com.example.data.remote.ScheduleDataSource
import com.example.data.repository.MatchRepository
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UefaApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: MatchRepository
        private set

    lateinit var notificationHelper: NotificationHelper
        private set

    lateinit var alarmScheduler: AlarmScheduler
        private set

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getInstance(this)
        notificationHelper = NotificationHelper(this)
        alarmScheduler = AlarmScheduler(this)

        val onlineFetcher = OnlineMatchFetcher()
        val icsParser = IcsScheduleParser()
        val dataSource = ScheduleDataSource()

        repository = MatchRepository(
            context = this,
            matchDao = database.matchDao(),
            onlineMatchFetcher = onlineFetcher,
            icsScheduleParser = icsParser,
            scheduleDataSource = dataSource,
            alarmScheduler = alarmScheduler,
            notificationHelper = notificationHelper
        )

        CoroutineScope(Dispatchers.IO).launch {
            repository.syncFullSchedule()
        }
    }
}
