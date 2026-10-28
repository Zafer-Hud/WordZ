package com.zafer.wordz

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.ceil

@Composable
internal fun Base44ModeGameScreen(
    mode: Base44PlayMode,
    levelIndex: Int,
    coins: Int,
    hints: Int,
    completedBefore: Boolean,
    dailyStreak: Int,
    onCoins: (Int) -> Unit,
    onHints: (Int) -> Unit,
    onRewardXp: (Int) -> Unit,
    onComplete: (elapsedMs: Long) -> Unit,
    back: () -> Unit,
    next: () -> Unit,
    home: () -> Unit,
) {
    val startedAt = remember(levelIndex, mode) { SystemClock.elapsedRealtime() }
    var remainingMs by remember(levelIndex, mode) { mutableLongStateOf(Base44ModeStore.SPEED_LIMIT_MS) }
    var expired by remember(levelIndex, mode) { mutableStateOf(false) }
    var finished by remember(levelIndex, mode) { mutableStateOf(false) }
    val haptics = rememberWordZHaptics()

    LaunchedEffect(levelIndex, mode, finished) {
        if (mode != Base44PlayMode.SPEED || finished) return@LaunchedEffect
        while (!finished) {
            val elapsed = SystemClock.elapsedRealtime() - startedAt
            remainingMs = (Base44ModeStore.SPEED_LIMIT_MS - elapsed).coerceAtLeast(0L)
            if (remainingMs <= 0L) {
                expired = true
                haptics.error()
                GameToastCenter.warning("Süre doldu. Tekrar deneyebilirsin.")
                break
            }
            delay(100L)
        }
    }

    Box(Modifier.fillMaxSize()) {
        Base44CanonicalGameScreen(
            levelIndex = levelIndex,
            coins = coins,
            hints = hints,
            completedBefore = completedBefore,
            onCoins = onCoins,
            onHints = onHints,
            onRewardXp = onRewardXp,
            onComplete = {
                if (!expired) {
                    finished = true
                    onComplete(SystemClock.elapsedRealtime() - startedAt)
                }
            },
            back = back,
            next = next,
            home = home,
        )

        if (mode != Base44PlayMode.CLASSIC) {
            val tint = if (mode == Base44PlayMode.SPEED) B44V2Colors.Orange else B44V2Colors.Gold
            val text = when (mode) {
                Base44PlayMode.SPEED -> {
                    val seconds = ceil(remainingMs / 1000.0).toInt().coerceAtLeast(0)
                    "⚡ ${seconds}s"
                }
                Base44PlayMode.DAILY -> "🏆 Günlük · 🔥 $dailyStreak"
                Base44PlayMode.CLASSIC -> ""
            }
            Row(
                Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 58.dp)
                    .background(Color(0xE60A1322), CircleShape)
                    .border(1.dp, tint.copy(alpha = .45f), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text, color = tint, fontSize = 11.sp, fontWeight = FontWeight.Black)
            }
        }

        AnimatedVisibility(
            visible = expired,
            enter = fadeIn() + scaleIn(initialScale = .94f),
        ) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = .78f)),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.foundation.layout.Column(
                    Modifier.padding(24.dp).background(
                        Brush.verticalGradient(listOf(Color(0xFF17233A), Color(0xFF090F1B))),
                        RoundedCornerShape(26.dp),
                    ).border(1.dp, B44V2Colors.Orange.copy(alpha = .38f), RoundedCornerShape(26.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("⏱️", fontSize = 42.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("Süre Doldu", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(6.dp))
                    Text("90 saniyelik tur tamamlandı.", color = B44Ui.MutedText, fontSize = 11.sp)
                    Spacer(Modifier.height(18.dp))
                    Box(
                        Modifier.height(48.dp).width(220.dp).background(
                            Brush.horizontalGradient(listOf(B44V2Colors.Orange, Color(0xFFEA580C))),
                            RoundedCornerShape(15.dp),
                        ).clickable(onClick = home),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("ANA MENÜ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
