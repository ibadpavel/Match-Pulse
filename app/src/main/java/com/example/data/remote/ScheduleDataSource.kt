package com.example.data.remote

import com.example.data.local.MatchEntity
import com.example.model.Tournament
import java.util.Calendar

/**
 * Локальный начальный источник календаря UEFA (используется только при первом запуске без сети).
 * Все актуальные матчи загружаются онлайн напрямую по официальному календарю UEFA.
 */
class ScheduleDataSource {

    fun fetchLatestFixtures(): List<MatchEntity> {
        val now = System.currentTimeMillis()

        fun createUtcTimestamp(y: Int, m: Int, d: Int, hour: Int, minute: Int): Long {
            return Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m - 1)
                set(Calendar.DAY_OF_MONTH, d)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }

        return listOf(
            // ==============================================================
            // ЛИГА ЧЕМПИОНОВ УЕФА — КАЛЕНДАРЬ МАТЧЕЙ (ТУР 3)
            // ==============================================================
            MatchEntity(
                id = "ucl_r3_liverpool_villarreal",
                tournament = Tournament.CHAMPIONS_LEAGUE.name,
                homeTeam = "Liverpool FC",
                awayTeam = "Villarreal CF",
                homeCountry = "Англия",
                awayCountry = "Испания",
                kickoffTimeMillis = createUtcTimestamp(2026, 10, 20, 19, 0),
                stadium = "Энфилд (Ливерпуль)",
                stage = "Общий этап • Тур 3",
                broadcastChannel = "Матч ТВ",
                isReminderEnabled = true,
                lastUpdatedMillis = now
            ),
            MatchEntity(
                id = "ucl_r3_psg_barcelona",
                tournament = Tournament.CHAMPIONS_LEAGUE.name,
                homeTeam = "Paris Saint-Germain FC",
                awayTeam = "FC Barcelona",
                homeCountry = "Франция",
                awayCountry = "Испания",
                kickoffTimeMillis = createUtcTimestamp(2026, 10, 20, 19, 0),
                stadium = "Парк де Пренс (Париж)",
                stage = "Общий этап • Тур 3",
                broadcastChannel = "Okko Спорт / Матч ТВ",
                isReminderEnabled = true,
                lastUpdatedMillis = now
            ),
            MatchEntity(
                id = "ucl_r3_como_manutd",
                tournament = Tournament.CHAMPIONS_LEAGUE.name,
                homeTeam = "Como 1907",
                awayTeam = "Manchester United FC",
                homeCountry = "Италия",
                awayCountry = "Англия",
                kickoffTimeMillis = createUtcTimestamp(2026, 10, 21, 16, 45),
                stadium = "Стадио Джузеппе Синигалья (Комо)",
                stage = "Общий этап • Тур 3",
                broadcastChannel = "Okko Спорт",
                isReminderEnabled = true,
                lastUpdatedMillis = now
            ),
            MatchEntity(
                id = "ucl_r3_real_madrid_leipzig",
                tournament = Tournament.CHAMPIONS_LEAGUE.name,
                homeTeam = "Real Madrid CF",
                awayTeam = "RB Leipzig",
                homeCountry = "Испания",
                awayCountry = "Германия",
                kickoffTimeMillis = createUtcTimestamp(2026, 10, 21, 19, 0),
                stadium = "Сантьяго Бернабеу (Мадрид)",
                stage = "Общий этап • Тур 3",
                broadcastChannel = "Матч ТВ",
                isReminderEnabled = true,
                lastUpdatedMillis = now
            ),
            MatchEntity(
                id = "ucl_r3_bayern_arsenal",
                tournament = Tournament.CHAMPIONS_LEAGUE.name,
                homeTeam = "FC Bayern München",
                awayTeam = "Arsenal FC",
                homeCountry = "Германия",
                awayCountry = "Англия",
                kickoffTimeMillis = createUtcTimestamp(2026, 10, 21, 19, 0),
                stadium = "Альянц Арена (Мюнхен)",
                stage = "Общий этап • Тур 3",
                broadcastChannel = "Okko Спорт",
                isReminderEnabled = true,
                lastUpdatedMillis = now
            ),
            MatchEntity(
                id = "ucl_r3_inter_shakhtar",
                tournament = Tournament.CHAMPIONS_LEAGUE.name,
                homeTeam = "FC Internazionale Milano",
                awayTeam = "FK Shakhtar Donetsk",
                homeCountry = "Италия",
                awayCountry = "Украина",
                kickoffTimeMillis = createUtcTimestamp(2026, 10, 21, 19, 0),
                stadium = "Сан-Сиро (Милан)",
                stage = "Общий этап • Тур 3",
                broadcastChannel = "Матч! Футбол 1",
                isReminderEnabled = true,
                lastUpdatedMillis = now
            ),

            // ==============================================================
            // ЛИГА ЕВРОПЫ УЕФА — КАЛЕНДАРЬ МАТЧЕЙ (ТУР 3)
            // ==============================================================
            MatchEntity(
                id = "uel_r3_juventus_rennes",
                tournament = Tournament.EUROPA_LEAGUE.name,
                homeTeam = "Juventus",
                awayTeam = "Rennes",
                homeCountry = "Италия",
                awayCountry = "Франция",
                kickoffTimeMillis = createUtcTimestamp(2026, 10, 22, 16, 45),
                stadium = "Juventus Stadium (Турин)",
                stage = "Общий этап • Тур 3",
                broadcastChannel = "Матч ТВ",
                isReminderEnabled = true,
                lastUpdatedMillis = now
            ),
            MatchEntity(
                id = "uel_r3_celtic_celta",
                tournament = Tournament.EUROPA_LEAGUE.name,
                homeTeam = "Celtic",
                awayTeam = "Celta",
                homeCountry = "Шотландия",
                awayCountry = "Испания",
                kickoffTimeMillis = createUtcTimestamp(2026, 10, 22, 19, 0),
                stadium = "Celtic Park (Глазго)",
                stage = "Общий этап • Тур 3",
                broadcastChannel = "Okko Спорт",
                isReminderEnabled = true,
                lastUpdatedMillis = now
            ),
            MatchEntity(
                id = "uel_r3_hoffenheim_lyon",
                tournament = Tournament.EUROPA_LEAGUE.name,
                homeTeam = "Hoffenheim",
                awayTeam = "Lyon",
                homeCountry = "Германия",
                awayCountry = "Франция",
                kickoffTimeMillis = createUtcTimestamp(2026, 10, 22, 19, 0),
                stadium = "Rhein-Neckar-Arena (Зинсхайм)",
                stage = "Общий этап • Тур 3",
                broadcastChannel = "Матч! Футбол 2",
                isReminderEnabled = true,
                lastUpdatedMillis = now
            ),
            MatchEntity(
                id = "uel_r3_bournemouth_milan",
                tournament = Tournament.EUROPA_LEAGUE.name,
                homeTeam = "Bournemouth",
                awayTeam = "Milan",
                homeCountry = "Англия",
                awayCountry = "Италия",
                kickoffTimeMillis = createUtcTimestamp(2026, 10, 22, 19, 0),
                stadium = "Bournemouth Stadium",
                stage = "Общий этап • Тур 3",
                broadcastChannel = "Матч ТВ / Okko Спорт",
                isReminderEnabled = true,
                lastUpdatedMillis = now
            ),
            MatchEntity(
                id = "uel_r3_besiktas_palace",
                tournament = Tournament.EUROPA_LEAGUE.name,
                homeTeam = "Besiktas",
                awayTeam = "Crystal Palace",
                homeCountry = "Турция",
                awayCountry = "Англия",
                kickoffTimeMillis = createUtcTimestamp(2026, 10, 22, 19, 0),
                stadium = "Besiktas Stadium (Стамбул)",
                stage = "Общий этап • Тур 3",
                broadcastChannel = "Okko Спорт",
                isReminderEnabled = true,
                lastUpdatedMillis = now
            ),
            MatchEntity(
                id = "uel_r3_union_beer_sheva",
                tournament = Tournament.EUROPA_LEAGUE.name,
                homeTeam = "Union SG",
                awayTeam = "H. Beer-Sheva",
                homeCountry = "Бельгия",
                awayCountry = "Израиль",
                kickoffTimeMillis = createUtcTimestamp(2026, 10, 22, 16, 45),
                stadium = "Düsseldorf Arena",
                stage = "Общий этап • Тур 3",
                broadcastChannel = "Okko Спорт",
                isReminderEnabled = true,
                lastUpdatedMillis = now
            )
        )
    }
}
