package com.zafer.wordz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicLong

enum class GameToastType { SUCCESS, ERROR, WARNING, INFO }

data class GameToastMessage(
    val id: Long,
    val message: String,
    val type: GameToastType,
    val durationMs: Long,
)

object GameToastCenter {
    private val nextId = AtomicLong(0)
    val toasts = mutableStateListOf<GameToastMessage>()

    fun show(message: String, type: GameToastType = GameToastType.INFO, durationMs: Long = 2800L) {
        val item = GameToastMessage(nextId.incrementAndGet(), message, type, durationMs.coerceAtLeast(300L))
        while (toasts.size >= 5) toasts.removeAt(0)
        toasts += item
    }

    fun success(message: String, durationMs: Long = 2800L) = show(message, GameToastType.SUCCESS, durationMs)
    fun error(message: String, durationMs: Long = 2800L) = show(message, GameToastType.ERROR, durationMs)
    fun warning(message: String, durationMs: Long = 2800L) = show(message, GameToastType.WARNING, durationMs)
    fun info(message: String, durationMs: Long = 2800L) = show(message, GameToastType.INFO, durationMs)

    fun dismiss(id: Long) {
        toasts.removeAll { it.id == id }
    }
}

private data class ToastStyle(val bg: Color, val border: Color, val accent: Color, val emoji: String)

private fun styleFor(type: GameToastType): ToastStyle = when (type) {
    GameToastType.SUCCESS -> ToastStyle(Color(0x2610D98A), Color(0x5910D98A), Color(0xFF10D98A), "✅")
    GameToastType.ERROR -> ToastStyle(Color(0x26EF4444), Color(0x59EF4444), Color(0xFFF87171), "❌")
    GameToastType.WARNING -> ToastStyle(Color(0x1FFFCA44), Color(0x4DFFCA44), Color(0xFFFFCA44), "⚠️")
    GameToastType.INFO -> ToastStyle(Color(0x1F38BDF8), Color(0x4D38BDF8), Color(0xFF38BDF8), "ℹ️")
}

@Composable
fun GameToastHost(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().statusBarsPadding().padding(top = 12.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GameToastCenter.toasts.toList().forEach { toast ->
            LaunchedEffect(toast.id) {
                delay(toast.durationMs)
                GameToastCenter.dismiss(toast.id)
            }
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { -20 },
                    animationSpec = spring(stiffness = 380f, dampingRatio = .78f),
                ) + scaleIn(initialScale = .90f, animationSpec = spring(stiffness = 380f, dampingRatio = .78f)),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -12 }) + scaleOut(targetScale = .92f),
            ) {
                val style = styleFor(toast.type)
                Row(
                    Modifier.fillMaxWidth().then(Modifier)
                        .background(style.bg, RoundedCornerShape(16.dp))
                        .border(1.dp, style.border, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(style.emoji, fontSize = 16.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        toast.message,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        Modifier.size(24.dp).background(Color.White.copy(alpha = .04f), CircleShape)
                            .clickable { GameToastCenter.dismiss(toast.id) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Close, null, tint = Color.White.copy(alpha = .55f), modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
    }
}
