package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.UefaApplication
import com.example.data.local.MatchEntity
import com.example.model.Tournament
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class MatchFilter(val label: String) {
    ALL("Все"),
    TODAY("Сегодня"),
    UPCOMING("Предстоящие")
}

data class MainUiState(
    val matches: List<MatchEntity> = emptyList(),
    val filteredMatches: List<MatchEntity> = emptyList(),
    val todayMatches: List<MatchEntity> = emptyList(),
    val selectedTournament: Tournament? = null, // "Все турниры"
    val selectedFilter: MatchFilter = MatchFilter.TODAY, // "Сегодня"
    val isSyncing: Boolean = false,
    val lastSyncText: String = "Календарь UEFA актуален",
    val toastMessage: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val uefaApp = application as UefaApplication
    private val repository = uefaApp.repository
    private val notificationHelper = uefaApp.notificationHelper

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var initialFilterAdjusted = false

    init {
        observeMatches()
        syncNow()
    }

    private fun observeMatches() {
        repository.observeMatches()
            .onEach { allMatches ->
                _uiState.update { current ->
                    val todayMatches = calculateTodayMatches(allMatches)
                    val targetFilter = if (!initialFilterAdjusted && allMatches.isNotEmpty()) {
                        initialFilterAdjusted = true
                        if (todayMatches.isEmpty()) MatchFilter.UPCOMING else MatchFilter.TODAY
                    } else {
                        current.selectedFilter
                    }
                    val filtered = applyFilters(allMatches, current.selectedTournament, targetFilter)
                    current.copy(
                        matches = allMatches,
                        filteredMatches = filtered,
                        todayMatches = todayMatches,
                        selectedFilter = targetFilter
                    )
                }
            }
            .catch { e ->
                _uiState.update { it.copy(toastMessage = "Ошибка загрузки: ${e.message}") }
            }
            .launchIn(viewModelScope)
    }

    fun selectTournament(tournament: Tournament?) {
        _uiState.update { current ->
            val filtered = applyFilters(current.matches, tournament, current.selectedFilter)
            current.copy(selectedTournament = tournament, filteredMatches = filtered)
        }
    }

    fun selectFilter(filter: MatchFilter) {
        _uiState.update { current ->
            val filtered = applyFilters(current.matches, current.selectedTournament, filter)
            current.copy(selectedFilter = filter, filteredMatches = filtered)
        }
    }

    fun toggleReminder(matchId: String, currentEnabled: Boolean) {
        viewModelScope.launch {
            val newStatus = !currentEnabled
            repository.setReminderEnabled(matchId, newStatus)
            val msg = if (newStatus) {
                "Напоминания включены (за 1 час и по свистку)"
            } else {
                "Напоминания отключены"
            }
            _uiState.update { it.copy(toastMessage = msg) }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val scheduleResult = repository.syncFullSchedule()

            val timeFormat = SimpleDateFormat("HH:mm", Locale("ru"))
            val currentTime = timeFormat.format(Date())

            _uiState.update { current ->
                if (scheduleResult.isSuccess) {
                    current.copy(
                        isSyncing = false,
                        lastSyncText = "Календарь UEFA обновлен в $currentTime",
                        toastMessage = "Расписание актуально (${scheduleResult.getOrNull()} матчей)"
                    )
                } else {
                    current.copy(
                        isSyncing = false,
                        lastSyncText = "Календарь загружен из кэша",
                        toastMessage = "Используется сохраненное расписание"
                    )
                }
            }
        }
    }

    fun sendTestNotification() {
        notificationHelper.showTestNotificationSuite()
        _uiState.update { it.copy(toastMessage = "Тестовые напоминания отправлены!") }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    private fun calculateTodayMatches(matches: List<MatchEntity>): List<MatchEntity> {
        val now = Calendar.getInstance()
        val startOfDay = now.clone() as Calendar
        startOfDay.set(Calendar.HOUR_OF_DAY, 0)
        startOfDay.set(Calendar.MINUTE, 0)
        startOfDay.set(Calendar.SECOND, 0)
        startOfDay.set(Calendar.MILLISECOND, 0)

        val endOfDay = now.clone() as Calendar
        endOfDay.set(Calendar.HOUR_OF_DAY, 23)
        endOfDay.set(Calendar.MINUTE, 59)
        endOfDay.set(Calendar.SECOND, 59)
        endOfDay.set(Calendar.MILLISECOND, 999)

        return matches.filter {
            it.kickoffTimeMillis in startOfDay.timeInMillis..endOfDay.timeInMillis
        }
    }

    private fun applyFilters(
        all: List<MatchEntity>,
        tournament: Tournament?,
        filter: MatchFilter
    ): List<MatchEntity> {
        var list = all
        if (tournament != null) {
            list = list.filter { it.tournament == tournament.name }
        }

        val now = Calendar.getInstance()
        val startOfDay = now.clone() as Calendar
        startOfDay.set(Calendar.HOUR_OF_DAY, 0)
        startOfDay.set(Calendar.MINUTE, 0)
        startOfDay.set(Calendar.SECOND, 0)
        startOfDay.set(Calendar.MILLISECOND, 0)

        val endOfDay = now.clone() as Calendar
        endOfDay.set(Calendar.HOUR_OF_DAY, 23)
        endOfDay.set(Calendar.MINUTE, 59)
        endOfDay.set(Calendar.SECOND, 59)
        endOfDay.set(Calendar.MILLISECOND, 999)

        return when (filter) {
            MatchFilter.ALL -> list
            MatchFilter.TODAY -> list.filter { it.kickoffTimeMillis in startOfDay.timeInMillis..endOfDay.timeInMillis }
            MatchFilter.UPCOMING -> list.filter { it.kickoffTimeMillis > endOfDay.timeInMillis }
        }
    }
}
