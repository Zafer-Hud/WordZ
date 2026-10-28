package com.zafer.wordz

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

internal object B44V2Colors {
    val Bg0 = Color(0xFF040C18)
    val Bg1 = Color(0xFF071420)
    val Bg2 = Color(0xFF050F1E)
    val Deep = Color(0xFF070E1A)
    val Mid = Color(0xFF0D1829)
    val Bright = Color(0xFF112240)
    val Emerald = Color(0xFF10D98A)
    val EmeraldLight = Color(0xFF4DFFB4)
    val EmeraldDark = Color(0xFF0AA86A)
    val Gold = Color(0xFFFFCA44)
    val GoldLight = Color(0xFFFFE07A)
    val GoldDark = Color(0xFFD4A017)
    val Cyan = Color(0xFF38BDF8)
    val Orange = Color(0xFFFB923C)
    val Purple = Color(0xFF9B51E0)
    val Red = Color(0xFFEF4444)
}

/** Central visual constants used by active WordZ Compose surfaces. */
internal object B44Ui {
    val ScreenPadding = 20.dp
    val CardRadius = 20.dp
    val ControlRadius = 14.dp
    val SmallRadius = 12.dp
    val Glass = Color.White.copy(alpha = .045f)
    val GlassStrong = Color.White.copy(alpha = .070f)
    val Border = Color.White.copy(alpha = .085f)
    val BorderStrong = Color.White.copy(alpha = .11f)
    val MutedText = Color.White.copy(alpha = .40f)
    val DimText = Color.White.copy(alpha = .28f)
    const val EnterMs = 550
    const val SplashExitMs = 700
    const val ToastMs = 2800
}

internal const val B44V2_PREFS = "wordz_progress"
internal const val B44V2_COINS = "coins"
internal const val B44V2_UNLOCKED = "unlocked_level"
internal const val B44V2_COMPLETED = "completed_levels"
internal const val B44V2_HINTS = "b44_hints"

internal enum class B44V2Screen { HOME, MAP, GAME, MARKET, PROFILE }

/** Compatibility shape: these records now represent theme worlds, not countries. */
internal data class B44Country(
    val id: String,
    val name: String,
    val emoji: String,
    val start: Int,
    val end: Int,
    val accent: Color,
    val subtitle: String,
)

@Composable
internal fun B44V2Background(content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(B44V2Colors.Bg0, B44V2Colors.Bg1, B44V2Colors.Bg2))
        )
    ) {
        val transition = rememberInfiniteTransition(label = "wordzAmbient")
        val emeraldScale by transition.animateFloat(
            1f, 1.12f,
            infiniteRepeatable(tween(7000), RepeatMode.Reverse),
            label = "emeraldAmbient",
        )
        val cyanScale by transition.animateFloat(
            1f, 1.16f,
            infiniteRepeatable(tween(9000, delayMillis = 1600), RepeatMode.Reverse),
            label = "cyanAmbient",
        )
        val goldAlpha by transition.animateFloat(
            .05f, .12f,
            infiniteRepeatable(tween(6100), RepeatMode.Reverse),
            label = "goldAmbient",
        )

        Box(
            Modifier.size(500.dp).offset((-110).dp, (-160).dp).scale(emeraldScale)
                .background(
                    Brush.radialGradient(listOf(B44V2Colors.Emerald.copy(alpha = .18f), Color.Transparent)),
                    CircleShape,
                )
        )
        Box(
            Modifier.align(Alignment.BottomEnd).size(400.dp).offset(85.dp, 55.dp).scale(cyanScale)
                .background(
                    Brush.radialGradient(listOf(B44V2Colors.Cyan.copy(alpha = .12f), Color.Transparent)),
                    CircleShape,
                )
        )
        Box(
            Modifier.align(Alignment.CenterEnd).size(220.dp).offset(100.dp, (-30).dp).alpha(goldAlpha)
                .background(
                    Brush.radialGradient(listOf(B44V2Colors.Gold.copy(alpha = .25f), Color.Transparent)),
                    CircleShape,
                )
        )
        B44AmbientStars(seed = 44, count = 25)
        content()
    }
}

@Composable
internal fun B44AmbientStars(seed: Int, count: Int) {
    val stars = remember(seed, count) {
        val random = Random(seed)
        List(count) { Triple(random.nextFloat(), random.nextFloat() * .90f, .6f + random.nextFloat() * 1.5f) }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        stars.forEachIndexed { index, star ->
            val transition = rememberInfiniteTransition(label = "star$index")
            val alpha by transition.animateFloat(
                .07f, .72f,
                infiniteRepeatable(tween(1700 + index * 91, delayMillis = (index * 73) % 1100), RepeatMode.Reverse),
                label = "starAlpha$index",
            )
            Box(
                Modifier.offset(maxWidth * star.first, maxHeight * star.second)
                    .size(star.third.dp).alpha(alpha).background(Color.White, CircleShape)
            )
        }
    }
}

private data class B44NavItem(val screen: B44V2Screen, val icon: ImageVector, val label: String)

@Composable
internal fun B44V2BottomNav(
    active: B44V2Screen,
    modifier: Modifier = Modifier,
    onTab: (B44V2Screen) -> Unit,
) {
    val items = remember {
        listOf(
            B44NavItem(B44V2Screen.HOME, Icons.Filled.Home, "Ana Sayfa"),
            B44NavItem(B44V2Screen.MAP, Icons.Filled.Map, "Bölümler"),
            B44NavItem(B44V2Screen.MARKET, Icons.Filled.ShoppingBag, "Market"),
            B44NavItem(B44V2Screen.PROFILE, Icons.Filled.Person, "Profil"),
        )
    }
    val activeIndex = items.indexOfFirst { it.screen == active }.coerceAtLeast(0)

    Box(
        modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, B44V2Colors.Deep.copy(alpha = .99f))))
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .navigationBarsPadding()
    ) {
        BoxWithConstraints(
            Modifier.fillMaxWidth().height(66.dp).clip(RoundedCornerShape(B44Ui.CardRadius))
                .background(Brush.verticalGradient(listOf(Color(0xF20E1B2E), Color(0xFA081321))))
                .border(1.dp, B44Ui.Border, RoundedCornerShape(B44Ui.CardRadius))
        ) {
            val tabWidth = maxWidth / items.size
            val indicatorX by animateDpAsState(
                targetValue = tabWidth * activeIndex + (tabWidth - 30.dp) / 2,
                animationSpec = spring(stiffness = 420f, dampingRatio = .78f),
                label = "bottomIndicator",
            )
            Box(
                Modifier.offset(x = indicatorX).size(width = 30.dp, height = 3.dp)
                    .background(B44V2Colors.Emerald, CircleShape)
            )
            Row(Modifier.fillMaxSize().padding(horizontal = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                items.forEach { item ->
                    val selected = item.screen == active
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.06f else 1f,
                        animationSpec = spring(stiffness = 500f, dampingRatio = .72f),
                        label = "navScale${item.label}",
                    )
                    Column(
                        Modifier.weight(1f).height(58.dp).scale(scale).clip(RoundedCornerShape(15.dp))
                            .background(if (selected) B44V2Colors.Emerald.copy(alpha = .09f) else Color.Transparent)
                            .clickable { onTab(item.screen) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            item.icon,
                            null,
                            tint = if (selected) B44V2Colors.Emerald else Color.White.copy(alpha = .35f),
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            item.label,
                            color = if (selected) B44V2Colors.Emerald else Color.White.copy(alpha = .30f),
                            fontSize = 9.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}
