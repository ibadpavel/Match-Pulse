package com.example

import com.example.data.remote.OnlineMatchFetcher
import org.junit.Assert.assertEquals
import org.junit.Test

class OnlineMatchFetcherTest {

    @Test
    fun onlineMatchFetcher_extractCountryWorksCorrectly() {
        val fetcher = OnlineMatchFetcher()

        assertEquals("Испания", fetcher.extractCountry("Real Madrid"))
        assertEquals("Испания", fetcher.extractCountry("Celta"))
        assertEquals("Англия", fetcher.extractCountry("Manchester United"))
        assertEquals("Англия", fetcher.extractCountry("Crystal Palace"))
        assertEquals("Италия", fetcher.extractCountry("Juventus"))
        assertEquals("Франция", fetcher.extractCountry("Lyon"))
        assertEquals("Германия", fetcher.extractCountry("FC Bayern München"))
        assertEquals("Португалия", fetcher.extractCountry("Benfica"))
    }
}
