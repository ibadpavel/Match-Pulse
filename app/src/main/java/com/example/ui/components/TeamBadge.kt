package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

/**
 * Compose-компонент эмблемы клуба.
 * URL резолвится через [TeamCrestRepository] (static catalog → versioned cache → scored dynamic).
 */
@Composable
fun TeamBadge(
    teamName: String,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp
) {
    val context = LocalContext.current
    var badgeUrl by remember(teamName) {
        mutableStateOf(TeamCrestRepository.getFastBadgeUrl(context, teamName))
    }

    LaunchedEffect(teamName) {
        if (badgeUrl == null) {
            val dynamicUrl = TeamCrestRepository.resolveDynamicBadge(context, teamName)
            if (dynamicUrl != null) {
                badgeUrl = dynamicUrl
            }
        }
    }

    val initials = TeamCrestRepository.getTeamInitials(teamName)
    val (primaryColor, secondaryColor) = TeamCrestRepository.getTeamColors(teamName)

    val containerModifier = modifier
        .size(size)
        .clip(CircleShape)
        .background(Color(0xFF0E162C))
        .border(1.dp, Color(0xFF243660), CircleShape)

    val currentUrl = badgeUrl
    if (currentUrl != null) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(currentUrl)
                .memoryCacheKey("crest_$teamName")
                .diskCacheKey("crest_$teamName")
                .crossfade(200)
                .build(),
            contentDescription = "Эмблема $teamName",
            modifier = containerModifier.padding(3.dp),
            contentScale = ContentScale.Fit,
            loading = {
                TeamBadgeShimmer(
                    initials = initials,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
            },
            error = {
                TeamBadgeFallback(
                    initials = initials,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
            }
        )
    } else {
        TeamBadgeFallback(
            initials = initials,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            modifier = containerModifier
        )
    }
}

@Composable
private fun TeamBadgeShimmer(
    initials: String,
    primaryColor: Color,
    secondaryColor: Color
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = alpha),
                        secondaryColor.copy(alpha = alpha)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White
            )
        )
    }
}

@Composable
private fun TeamBadgeFallback(
    initials: String,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(primaryColor, secondaryColor)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = Color.White
            )
        )
    }
}
