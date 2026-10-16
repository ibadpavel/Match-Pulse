package com.example

import com.example.ui.components.TeamBadgeProvider
import com.example.ui.components.TeamNameNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TeamBadgeProviderTest {

    @Test
    fun testAll2026TournamentClubsHaveBadgeUrls() {
        val testClubs = listOf(
            // Гранды
            "Real Madrid CF",
            "Реал Мадрид",
            "FC Barcelona",
            "Барселона",
            "Liverpool FC",
            "Ливерпуль",
            "Manchester City FC",
            "Манчестер Сити",
            "Arsenal FC",
            "Арсенал",
            "FC Bayern München",
            "Бавария",
            "Paris Saint-Germain FC",
            "ПСЖ",
            "FC Internazionale Milano",
            "Интер",
            "Juventus",
            "Ювентус",
            "Milan",
            "Милан",
            "Bayer 04 Leverkusen",
            "Байер",
            "Atalanta BC",
            "Аталанта",
            "Sporting CP",
            "Спортинг",
            "SL Benfica",
            "Бенфика",
            "Celtic FC",
            "Селтик",
            "FK Shakhtar Donetsk",
            "Шахтер",
            "Villarreal CF",
            "Вильярреал",
            "AS Monaco",
            "Монако",
            "Aston Villa FC",
            "Астон Вилла",

            // 22 клуба с сокращениями и спецификой календаря 2026
            "Atleti",
            "B. Dortmund",
            "S. Bratislava",
            "GNK Dinamo",
            "Bod&#248;/Glimt",
            "Bodø/Glimt",
            "Fenerbah&#231;e",
            "Fenerbahçe",
            "Ferencv&#225;ros",
            "Ferencváros",
            "Club Brugge",
            "LASK",
            "AEK Athens",
            "Viking",
            "Sunderland",
            "Lens",
            "AZ Alkmaar",
            "Celje",
            "Jagiellonia",
            "Lech Poznan",
            "Levski Sofia",
            "N.E.C.",
            "OFI Crete",
            "Omonia",
            "Torreense",
            "Sabah",
            "Ararat-Armenia"
        )

        for (club in testClubs) {
            val url = TeamBadgeProvider.getFastBadgeUrl(null, club)
            assertNotNull("Клуб '$club' должен иметь валидный URL эмблемы", url)
            assertTrue("URL эмблемы должен вести на HTTPS CDN: $url", url!!.startsWith("https://"))
            assertTrue("URL эмблемы должен оканчиваться на .png: $url", url.endsWith(".png"))
        }
    }

    @Test
    fun testHtmlEntityUnescaping() {
        assertEquals("Fenerbahçe", TeamNameNormalizer.unescapeHtml("Fenerbah&#231;e"))
        assertEquals("Bodø/Glimt", TeamNameNormalizer.unescapeHtml("Bod&#248;/Glimt"))
        assertEquals("Ferencváros", TeamNameNormalizer.unescapeHtml("Ferencv&#225;ros"))
    }

    @Test
    fun testInitialsGeneration() {
        assertEquals("RM", TeamBadgeProvider.getTeamInitials("Real Madrid"))
        assertEquals("BAR", TeamBadgeProvider.getTeamInitials("FC Barcelona"))
        assertEquals("MC", TeamBadgeProvider.getTeamInitials("Manchester City"))
        assertEquals("LIV", TeamBadgeProvider.getTeamInitials("Liverpool"))
        assertEquals("BAY", TeamBadgeProvider.getTeamInitials("Bayern"))
        assertEquals("BD", TeamBadgeProvider.getTeamInitials("B. Dortmund"))
        assertEquals("ATL", TeamBadgeProvider.getTeamInitials("Atleti"))
    }
}
