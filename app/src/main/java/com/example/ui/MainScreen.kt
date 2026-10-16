package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.Stadium
import com.example.ui.components.TeamBadge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.MatchEntity
import com.example.model.Tournament
import com.example.ui.theme.ChampionsLeagueCyan
import com.example.ui.theme.EuropaLeagueAmber
import com.example.ui.theme.StarGold
import com.example.ui.theme.StatusScheduled
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UefaCardBorder
import com.example.ui.theme.UefaCardNavy
import com.example.ui.theme.UefaDeepNavy
import com.example.ui.theme.UefaSurfaceNavy
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showSettingsDialog by remember { mutableStateOf(false) }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            viewModel.sendTestNotification()
        }
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            viewModel.clearToast()
        }
    }

    Scaffold(
        containerColor = UefaDeepNavy,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = UefaSurfaceNavy
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0C142B))
                                .border(1.5.dp, ChampionsLeagueCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = "UEFA Logo",
                                tint = ChampionsLeagueCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "UEFA Match Pulse",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 17.sp
                                )
                            )
                            Text(
                                text = "Календарь UEFA • Напоминания 1ч и свисток",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.syncNow() },
                        enabled = !uiState.isSyncing,
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = ChampionsLeagueCyan
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Обновить календарь UEFA",
                                tint = ChampionsLeagueCyan
                            )
                        }
                    }

                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки",
                            tint = TextSecondary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Permission Banner (if not granted)
            if (!hasNotificationPermission) {
                Surface(
                    color = Color(0xFF2A1C08),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = EuropaLeagueAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Включите уведомления: утренний дайджест, за 1 час и по свистку",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFFE0B2), fontSize = 11.sp),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            modifier = Modifier.testTag("enable_notifications_button")
                        ) {
                            Text("Включить", color = StarGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Tournament Switcher: Все турниры | Лига Чемпионов | Лига Европы
            TournamentSelector(
                selectedTournament = uiState.selectedTournament,
                onTournamentSelected = { viewModel.selectTournament(it) }
            )

            // Filter Chips: Все | Сегодня | Предстоящие
            FilterChipsSection(
                selectedFilter = uiState.selectedFilter,
                todayCount = uiState.todayMatches.size,
                onFilterSelected = { viewModel.selectFilter(it) }
            )

            // Matches List
            if (uiState.filteredMatches.isEmpty()) {
                EmptyStateView(
                    filter = uiState.selectedFilter,
                    isSyncing = uiState.isSyncing
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.filteredMatches, key = { it.id }) { match ->
                        MatchCard(
                            match = match,
                            onToggleReminder = { viewModel.toggleReminder(match.id, match.isReminderEnabled) }
                        )
                    }

                    item {
                        SyncFooter(syncText = uiState.lastSyncText)
                    }
                }
            }
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            hasPermission = hasNotificationPermission,
            onRequestPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onSendTestNotification = {
                viewModel.sendTestNotification()
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}

@Composable
fun TournamentSelector(
    selectedTournament: Tournament?,
    onTournamentSelected: (Tournament?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TournamentTabPill(
            title = "Все турниры",
            isSelected = selectedTournament == null,
            badgeColor = StarGold,
            modifier = Modifier.weight(1f),
            onClick = { onTournamentSelected(null) }
        )

        TournamentTabPill(
            title = "Лига Чемпионов",
            isSelected = selectedTournament == Tournament.CHAMPIONS_LEAGUE,
            badgeColor = ChampionsLeagueCyan,
            modifier = Modifier.weight(1.2f),
            onClick = { onTournamentSelected(Tournament.CHAMPIONS_LEAGUE) }
        )

        TournamentTabPill(
            title = "Лига Европы",
            isSelected = selectedTournament == Tournament.EUROPA_LEAGUE,
            badgeColor = EuropaLeagueAmber,
            modifier = Modifier.weight(1.1f),
            onClick = { onTournamentSelected(Tournament.EUROPA_LEAGUE) }
        )
    }
}

@Composable
fun TournamentTabPill(
    title: String,
    isSelected: Boolean,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) UefaCardNavy else Color(0xFF0F172E),
        animationSpec = tween(200),
        label = "bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) badgeColor else UefaCardBorder,
        animationSpec = tween(200),
        label = "border"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) badgeColor else TextMuted)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FilterChipsSection(
    selectedFilter: MatchFilter,
    todayCount: Int,
    onFilterSelected: (MatchFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf(MatchFilter.ALL, MatchFilter.TODAY, MatchFilter.UPCOMING)
        filters.forEach { filter ->
            val isSelected = selectedFilter == filter
            val label = when (filter) {
                MatchFilter.ALL -> "Все"
                MatchFilter.TODAY -> if (todayCount > 0) "Сегодня ($todayCount)" else "Сегодня"
                MatchFilter.UPCOMING -> "Предстоящие"
            }

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color(0xFF0E162C),
                    selectedContainerColor = ChampionsLeagueCyan.copy(alpha = 0.2f),
                    labelColor = TextSecondary,
                    selectedLabelColor = ChampionsLeagueCyan
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = UefaCardBorder,
                    selectedBorderColor = ChampionsLeagueCyan,
                    enabled = true,
                    selected = isSelected
                ),
                modifier = Modifier.testTag("filter_${filter.name.lowercase()}")
            )
        }
    }
}

@Composable
fun MatchCard(
    match: MatchEntity,
    onToggleReminder: () -> Unit
) {
    val isChampionsLeague = match.tournament == Tournament.CHAMPIONS_LEAGUE.name
    val tournamentColor = if (isChampionsLeague) ChampionsLeagueCyan else EuropaLeagueAmber
    val tournamentTitle = if (isChampionsLeague) "Лига Чемпионов" else "Лига Европы"

    val timeFormat = SimpleDateFormat("HH:mm", Locale("ru"))
    val dateFormat = SimpleDateFormat("d MMM, EEE", Locale("ru"))
    val matchDate = Date(match.kickoffTimeMillis)
    val isToday = isMatchToday(match.kickoffTimeMillis)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("match_card_${match.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = UefaCardNavy),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    tournamentColor.copy(alpha = 0.4f),
                    UefaCardBorder,
                    UefaCardBorder
                )
            ),
            width = 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Tournament + Stage + Date badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(tournamentColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$tournamentTitle • ${match.stage}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = tournamentColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Date badge
                val statusText = if (isToday) "Сегодня" else dateFormat.format(matchDate)
                val statusBg = if (isToday) StatusScheduled.copy(alpha = 0.2f) else Color(0xFF1B2648)
                val statusTextColor = if (isToday) StatusScheduled else TextSecondary

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Teams Row: Home vs Away with Kickoff Time in Center and Club Crests
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home Club
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    TeamBadge(teamName = match.homeTeam, size = 36.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        modifier = Modifier.weight(1f, fill = false),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = match.homeTeam,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = match.homeCountry,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    }
                }

                // Center: Kickoff Time
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0C142B))
                        .border(
                            1.dp,
                            UefaCardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = timeFormat.format(matchDate),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = ChampionsLeagueCyan,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = "Начало",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        )
                    }
                }

                // Away Club
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Column(
                        modifier = Modifier.weight(1f, fill = false),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = match.awayTeam,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.5.sp
                            ),
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = match.awayCountry,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontSize = 11.sp
                            ),
                            textAlign = TextAlign.End,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TeamBadge(teamName = match.awayTeam, size = 36.dp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Stadium + Broadcast Channel + Reminder Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Stadium,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = match.stadium,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Reminder Button (1 hour before + whistle)
                val isBellActive = match.isReminderEnabled
                val bellColor = if (isBellActive) ChampionsLeagueCyan else TextMuted
                val bellBg = if (isBellActive) ChampionsLeagueCyan.copy(alpha = 0.15f) else Color(0xFF131D38)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(bellBg)
                        .border(
                            1.dp,
                            if (isBellActive) ChampionsLeagueCyan else UefaCardBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable(onClick = onToggleReminder)
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                        .testTag("reminder_button_${match.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isBellActive) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                            contentDescription = "Напоминание о матче",
                            tint = bellColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBellActive) "1ч и свисток (Вкл)" else "Напомнить",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = bellColor,
                                fontWeight = if (isBellActive) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

fun isMatchToday(kickoffMillis: Long): Boolean {
    val calNow = Calendar.getInstance()
    val calMatch = Calendar.getInstance().apply { timeInMillis = kickoffMillis }
    return calNow.get(Calendar.YEAR) == calMatch.get(Calendar.YEAR) &&
            calNow.get(Calendar.DAY_OF_YEAR) == calMatch.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun SyncFooter(syncText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = StatusScheduled,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = syncText,
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
            )
        }
    }
}

@Composable
fun EmptyStateView(
    filter: MatchFilter,
    isSyncing: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            val title = when {
                isSyncing -> "Синхронизация календаря UEFA..."
                filter == MatchFilter.TODAY -> "Сегодня матчей еврокубков нет"
                else -> "Матчей не найдено"
            }
            val description = when {
                filter == MatchFilter.TODAY -> "Матчи Лиги Чемпионов проходят во вторник и среду, а Лиги Европы — в четверг.\n\nПереключитесь на вкладку «Предстоящие», чтобы увидеть расписание следующих туров."
                else -> "Попробуйте выбрать другую вкладку турнира или обновить календарь."
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SettingsDialog(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onSendTestNotification: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UefaSurfaceNavy,
        shape = RoundedCornerShape(18.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = StarGold,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Настройки приложения",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Calendar Source Info Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = UefaCardNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = ChampionsLeagueCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Источник расписания",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Расписание матчей формируется напрямую по официальному календарю UEFA (ЛЧ и ЛЕ) без использования API-токенов.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                        )
                    }
                }

                // Notification Schedule Scheme Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = UefaCardNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = StarGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Схема напоминаний",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        NotificationScheduleItem(
                            stepNumber = "1",
                            title = "Утром после обновления списка",
                            time = "09:00",
                            description = "«Сегодня N матчей ЛЧ/ЛЕ в <время>, <время>» (повторяющееся время не выводится)"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        NotificationScheduleItem(
                            stepNumber = "2",
                            title = "За 1 час до начала",
                            time = "-60 мин",
                            description = "Предупреждение о скором старте матча и стадионе"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        NotificationScheduleItem(
                            stepNumber = "3",
                            title = "По свистку",
                            time = "Старт",
                            description = "Оповещение в момент стартового свистка о начале игры"
                        )
                    }
                }

                // Push Permission Status
                Card(
                    colors = CardDefaults.cardColors(containerColor = UefaCardNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (hasPermission) Icons.Default.Check else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (hasPermission) StatusScheduled else EuropaLeagueAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasPermission) "Уведомления разрешены" else "Уведомления отключены",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        if (!hasPermission) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = onRequestPermission,
                                colors = ButtonDefaults.buttonColors(containerColor = ChampionsLeagueCyan),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Предоставить разрешение", color = UefaDeepNavy, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Test Notification Button
                Button(
                    onClick = onSendTestNotification,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2B50)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = ChampionsLeagueCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Тестовое уведомление", color = ChampionsLeagueCyan, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = ChampionsLeagueCyan, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun NotificationScheduleItem(stepNumber: String, title: String, time: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(ChampionsLeagueCyan.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = ChampionsLeagueCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 11.5.sp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• $time",
                    style = MaterialTheme.typography.labelSmall.copy(color = StarGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 10.5.sp, lineHeight = 14.sp)
            )
        }
    }
}
