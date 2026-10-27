package com.zafer.wordz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

private object B44Colors {
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
    val GoldDark = Color(0xFFD4A017)
    val Cyan = Color(0xFF38BDF8)
    val Orange = Color(0xFFFB923C)
    val White40 = Color.White.copy(alpha = .40f)
    val White30 = Color.White.copy(alpha = .30f)
    val White08 = Color.White.copy(alpha = .08f)
    val White06 = Color.White.copy(alpha = .06f)
}

private const val B44_PREFS = "wordz_progress"
private const val B44_COINS = "coins"
private const val B44_UNLOCKED = "unlocked_level"

private enum class B44Tab { HOME, MAP, MARKET, PROFILE }

private data class B44Star(
    val x: Float,
    val y: Float,
    val size: Float,
    val duration: Int,
    val delay: Int,
    val peak: Float,
)

class Base44MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = B44Colors.Emerald,
                    secondary = B44Colors.Gold,
                    background = B44Colors.Deep,
                    surface = B44Colors.Mid,
                    onPrimary = Color.White,
                    onBackground = Color.White,
                    onSurface = Color.White,
                )
            ) {
                B44App(
                    openLegacyGame = {
                        startActivity(Intent(this@Base44MainActivity, V17MainActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
private fun B44App(openLegacyGame: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences(B44_PREFS, Context.MODE_PRIVATE) }
    val playerStore = remember { PlayerProgressStore(prefs) }

    var showSplash by remember { mutableStateOf(true) }
    var activeTab by remember { mutableStateOf(B44Tab.HOME) }

    val coins = remember {
        if (prefs.contains(B44_COINS)) prefs.getInt(B44_COINS, 500) else 500
    }
    val unlocked = remember { prefs.getInt(B44_UNLOCKED, 0).coerceAtLeast(0) }
    val player = remember { playerStore.snapshot() }

    Box(Modifier.fillMaxSize()) {
        when (activeTab) {
            B44Tab.HOME -> B44Home(
                coins = coins,
                currentLevel = unlocked + 1,
                player = player,
                onPlay = openLegacyGame,
                onTab = { activeTab = it },
            )
            B44Tab.MAP -> B44Placeholder("Dünya Haritası", "Base44 harita ekranı sonraki pakette bu kabuğa taşınacak.", activeTab) { activeTab = it }
            B44Tab.MARKET -> B44Placeholder("Market", "Base44 market ekranı sonraki pakette bu kabuğa taşınacak.", activeTab) { activeTab = it }
            B44Tab.PROFILE -> B44Placeholder("Profil", "Base44 profil ekranı sonraki pakette bu kabuğa taşınacak.", activeTab) { activeTab = it }
        }

        AnimatedVisibility(
            visible = showSplash,
            exit = fadeOut(tween(700)) + scaleOut(targetScale = 1.05f, animationSpec = tween(700)),
        ) {
            B44Splash { showSplash = false }
        }
    }
}

@Composable
private fun B44Background(content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(B44Colors.Bg0, B44Colors.Bg1, B44Colors.Bg2))
        )
    ) {
        B44Ambient()
        content()
    }
}

@Composable
private fun B44Ambient() {
    val transition = rememberInfiniteTransition(label = "ambient")
    val pulseA by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Reverse),
        label = "emeraldOrb"
    )
    val pulseB by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "cyanOrb"
    )

    Box(
        Modifier.size(500.dp).offset(x = (-105).dp, y = (-155).dp).scale(pulseA)
            .background(
                Brush.radialGradient(listOf(B44Colors.Emerald.copy(alpha = .18f), Color.Transparent)),
                CircleShape,
            )
    )
    Box(
        Modifier.align(Alignment.BottomEnd).size(400.dp).offset(x = 85.dp, y = 55.dp).scale(pulseB)
            .background(
                Brush.radialGradient(listOf(B44Colors.Cyan.copy(alpha = .12f), Color.Transparent)),
                CircleShape,
            )
    )
    B44Stars(seed = 44, count = 25, maxY = .82f)
}

@Composable
private fun B44Stars(seed: Int, count: Int, maxY: Float = 1f) {
    val stars = remember(seed, count, maxY) {
        val random = Random(seed)
        List(count) {
            B44Star(
                x = random.nextFloat(),
                y = random.nextFloat() * maxY,
                size = .5f + random.nextFloat() * 1.5f,
                duration = 2000 + random.nextInt(4000),
                delay = random.nextInt(4000),
                peak = .35f + random.nextFloat() * .35f,
            )
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        stars.forEachIndexed { index, star ->
            val transition = rememberInfiniteTransition(label = "star$index")
            val opacity by transition.animateFloat(
                initialValue = .08f,
                targetValue = star.peak,
                animationSpec = infiniteRepeatable(
                    animation = tween(star.duration, delayMillis = star.delay),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "twinkle$index",
            )
            Box(
                Modifier.offset(x = maxWidth * star.x, y = maxHeight * star.y)
                    .size(star.size.dp).alpha(opacity).background(Color.White, CircleShape)
            )
        }
    }
}

@Composable
private fun B44Splash(onComplete: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(progress, tween(80), label = "splashProgress")

    LaunchedEffect(Unit) {
        repeat(50) {
            delay(38)
            progress = ((it + 1) * 2) / 100f
        }
        delay(400)
        onComplete()
    }

    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF040C18), Color(0xFF071420), Color(0xFF030A14)))
        ),
        contentAlignment = Alignment.Center,
    ) {
        val transition = rememberInfiniteTransition(label = "splashAmbient")
        val emeraldScale by transition.animateFloat(
            1f, 1.20f,
            infiniteRepeatable(tween(5000), RepeatMode.Reverse), label = "splashEmerald"
        )
        val goldScale by transition.animateFloat(
            1f, 1.15f,
            infiniteRepeatable(tween(7000), RepeatMode.Reverse), label = "splashGold"
        )
        val cyanScale by transition.animateFloat(
            1f, 1.30f,
            infiniteRepeatable(tween(6000), RepeatMode.Reverse), label = "splashCyan"
        )
        val sparkleRotation by transition.animateFloat(
            0f, 360f,
            infiniteRepeatable(tween(8000, easing = LinearEasing)), label = "sparkleRotation"
        )

        Box(
            Modifier.align(Alignment.TopStart).size(500.dp).offset((-120).dp, (-180).dp).scale(emeraldScale)
                .background(Brush.radialGradient(listOf(B44Colors.Emerald.copy(alpha = .20f), Color.Transparent)), CircleShape)
        )
        Box(
            Modifier.align(Alignment.BottomEnd).size(350.dp).offset(60.dp, 80.dp).scale(goldScale)
                .background(Brush.radialGradient(listOf(B44Colors.Gold.copy(alpha = .10f), Color.Transparent)), CircleShape)
        )
        Box(
            Modifier.align(Alignment.CenterEnd).size(280.dp).offset(60.dp, 0.dp).scale(cyanScale)
                .background(Brush.radialGradient(listOf(B44Colors.Cyan.copy(alpha = .12f), Color.Transparent)), CircleShape)
        )
        B44Stars(seed = 144, count = 25)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, B44Colors.Emerald.copy(alpha = .6f)))))
                Spacer(Modifier.width(12.dp))
                Box(Modifier.size(8.dp).background(B44Colors.Emerald, CircleShape).shadow(8.dp, CircleShape))
                Spacer(Modifier.width(12.dp))
                Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(B44Colors.Emerald.copy(alpha = .6f), Color.Transparent))))
            }
            Spacer(Modifier.height(22.dp))
            Text("WORDS", color = Color.White, fontSize = 58.sp, lineHeight = 58.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
            Box(contentAlignment = Alignment.TopEnd) {
                Text(
                    "Z",
                    color = B44Colors.Emerald,
                    fontSize = 102.sp,
                    lineHeight = 92.sp,
                    fontWeight = FontWeight.Black,
                )
                Box(Modifier.size(24.dp).scale(1f).offset(x = 8.dp, y = (-4).dp)) {
                    Box(Modifier.size(6.dp).background(B44Colors.Gold, CircleShape).align(Alignment.TopCenter))
                    Box(Modifier.size(4.dp).background(B44Colors.Emerald, CircleShape).align(Alignment.BottomEnd))
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(32.dp).height(1.dp).background(Color.White.copy(alpha = .12f)))
                Spacer(Modifier.width(10.dp))
                Text("KELİME BULMACA", color = Color.White.copy(alpha = .30f), fontSize = 10.sp, letterSpacing = 3.sp)
                Spacer(Modifier.width(10.dp))
                Box(Modifier.width(32.dp).height(1.dp).background(Color.White.copy(alpha = .12f)))
            }
            Spacer(Modifier.height(58.dp))
            Box(Modifier.width(208.dp).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = .07f))) {
                Box(
                    Modifier.fillMaxWidth(animatedProgress.coerceIn(0f, 1f)).height(4.dp)
                        .background(Brush.horizontalGradient(listOf(B44Colors.Emerald, B44Colors.Gold)))
                )
            }
            Spacer(Modifier.height(12.dp))
            Text("YÜKLENİYOR", color = Color.White.copy(alpha = .25f), fontSize = 10.sp, letterSpacing = 3.sp)
        }
    }
}

@Composable
private fun B44Home(
    coins: Int,
    currentLevel: Int,
    player: PlayerProgress,
    onPlay: () -> Unit,
    onTab: (B44Tab) -> Unit,
) {
    B44Background {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp).padding(bottom = 104.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    B44GlassChip("◆", coins.toString(), B44Colors.Gold)
                    B44GlassChip("⚡", "Lv.${player.level}   ${player.totalXp} XP", B44Colors.Emerald)
                }

                Spacer(Modifier.height(20.dp))
                B44HeroTitle()
                Spacer(Modifier.height(24.dp))
                B44PlayButton(onPlay)
                Spacer(Modifier.height(28.dp))
                B44LevelBadge(currentLevel)
                Spacer(Modifier.height(28.dp))
                B44GameModes(onPlay)
                Spacer(Modifier.height(24.dp))
                B44Leaderboard()
                Spacer(Modifier.height(18.dp))
            }

            B44BottomNav(
                active = B44Tab.HOME,
                modifier = Modifier.align(Alignment.BottomCenter),
                onTab = onTab,
            )
        }
    }
}

@Composable
private fun B44HeroTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, B44Colors.Emerald.copy(alpha = .5f)))))
            Spacer(Modifier.width(12.dp))
            Text("KELİME OYUNU", color = B44Colors.Emerald.copy(alpha = .60f), fontSize = 10.sp, letterSpacing = 3.sp)
            Spacer(Modifier.width(12.dp))
            Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(B44Colors.Emerald.copy(alpha = .5f), Color.Transparent))))
        }
        Spacer(Modifier.height(16.dp))
        Text("WORDS", color = Color.White, fontSize = 50.sp, lineHeight = 50.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
        Text("Z", color = B44Colors.Emerald, fontSize = 84.sp, lineHeight = 72.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun B44GlassChip(symbol: String, text: String, tint: Color) {
    Row(
        Modifier.clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Color.White.copy(alpha = .07f), Color.White.copy(alpha = .025f))))
            .border(1.dp, Color.White.copy(alpha = .09f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(20.dp).background(Brush.linearGradient(listOf(tint, if (tint == B44Colors.Gold) B44Colors.GoldDark else B44Colors.EmeraldDark)), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(symbol, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(7.dp))
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun B44PlayButton(onPlay: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "playPulse")
    val pulse1 by transition.animateFloat(1f, 1.18f, infiniteRepeatable(tween(3000), RepeatMode.Restart), label = "pulse1")
    val pulse2 by transition.animateFloat(1f, 1.35f, infiniteRepeatable(tween(3000, delayMillis = 500), RepeatMode.Restart), label = "pulse2")

    Box(Modifier.size(210.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(178.dp).scale(pulse2).alpha((1.35f - pulse2).coerceIn(0f, .18f))
                .background(Brush.radialGradient(listOf(B44Colors.Emerald.copy(alpha = .20f), Color.Transparent)), CircleShape)
        )
        Box(
            Modifier.size(168.dp).scale(pulse1).alpha((1.18f - pulse1).coerceIn(0f, .28f))
                .background(Brush.radialGradient(listOf(B44Colors.Emerald.copy(alpha = .40f), Color.Transparent)), CircleShape)
        )
        Column(
            Modifier.size(144.dp).shadow(28.dp, CircleShape, ambientColor = B44Colors.Emerald.copy(alpha = .45f), spotColor = B44Colors.Emerald.copy(alpha = .45f))
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF1AE896), B44Colors.Emerald, B44Colors.EmeraldDark)))
                .border(1.dp, Color.White.copy(alpha = .15f), CircleShape)
                .clickable(onClick = onPlay),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(42.dp))
            Text("OYNA", color = Color.White.copy(alpha = .92f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun B44LevelBadge(currentLevel: Int) {
    Row(
        Modifier.clip(CircleShape).background(Color.White.copy(alpha = .045f))
            .border(1.dp, Color.White.copy(alpha = .09f), CircleShape)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val transition = rememberInfiniteTransition(label = "activeDot")
        val alpha by transition.animateFloat(.45f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "dotAlpha")
        Box(Modifier.size(8.dp).alpha(alpha).background(B44Colors.Emerald, CircleShape))
        Spacer(Modifier.width(9.dp))
        Text("Bölüm ", color = Color.White.copy(alpha = .55f), fontSize = 13.sp)
        Text(currentLevel.toString(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(" aktif", color = Color.White.copy(alpha = .55f), fontSize = 13.sp)
    }
}

private data class B44Mode(
    val emoji: String,
    val title: String,
    val description: String,
    val color: Color,
    val locked: Boolean,
)

@Composable
private fun B44GameModes(onPlay: () -> Unit) {
    val modes = remember {
        listOf(
            B44Mode("📖", "Klasik", "Kelimeleri bul, dünyayı keşfet", B44Colors.Emerald, false),
            B44Mode("⚡", "Hız Modu", "Süre yarışı — ne kadar hızlısın?", B44Colors.Orange, true),
            B44Mode("🏆", "Günlük Görev", "Her gün yeni meydan okuma", B44Colors.Gold, true),
        )
    }
    Column(Modifier.fillMaxWidth()) {
        Text("OYUN MODU", color = Color.White.copy(alpha = .30f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            modes.forEach { mode ->
                Box(
                    Modifier.weight(1f).height(126.dp).clip(RoundedCornerShape(16.dp))
                        .background(Brush.linearGradient(listOf(mode.color.copy(alpha = .18f), mode.color.copy(alpha = .05f))))
                        .border(1.dp, mode.color.copy(alpha = .30f), RoundedCornerShape(16.dp))
                        .alpha(if (mode.locked) .60f else 1f)
                        .clickable(enabled = !mode.locked, onClick = onPlay)
                        .padding(10.dp)
                ) {
                    if (mode.locked) {
                        Icon(Icons.Filled.Lock, null, tint = Color.White.copy(alpha = .35f), modifier = Modifier.size(13.dp).align(Alignment.TopEnd))
                    }
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(mode.emoji, fontSize = 22.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(mode.title, color = mode.color, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(4.dp))
                        Text(mode.description, color = Color.White.copy(alpha = .40f), fontSize = 9.sp, lineHeight = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun B44Leaderboard() {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = .04f))
            .border(1.dp, Color.White.copy(alpha = .08f), RoundedCornerShape(16.dp))
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.EmojiEvents, null, tint = B44Colors.Gold, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(8.dp))
            Text("Liderlik Tablosu", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Box(Modifier.fillMaxWidth().height(64.dp).border(1.dp, Color.White.copy(alpha = .05f)), contentAlignment = Alignment.Center) {
            Text("Henüz kayıt yok", color = Color.White.copy(alpha = .30f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun B44BottomNav(active: B44Tab, modifier: Modifier = Modifier, onTab: (B44Tab) -> Unit) {
    val items = listOf(
        Triple(B44Tab.HOME, "Ana Sayfa", Icons.Filled.Home),
        Triple(B44Tab.MAP, "Harita", Icons.Filled.Map),
        Triple(B44Tab.MARKET, "Market", Icons.Filled.ShoppingBag),
        Triple(B44Tab.PROFILE, "Profil", Icons.Filled.Person),
    )
    Box(
        modifier.fillMaxWidth().background(
            Brush.verticalGradient(listOf(Color.Transparent, B44Colors.Deep.copy(alpha = .97f)))
        ).navigationBarsPadding().padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().height(66.dp).clip(RoundedCornerShape(18.dp)).background(B44Colors.Mid.copy(alpha = .94f))
                .border(1.dp, Color.White.copy(alpha = .07f), RoundedCornerShape(18.dp))
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { (tab, label, icon) ->
                val selected = tab == active
                B44NavItem(
                    tab = tab,
                    label = label,
                    icon = icon,
                    selected = selected,
                    modifier = Modifier.weight(1f),
                    onTab = onTab,
                )
            }
        }
    }
}

@Composable
private fun B44NavItem(
    tab: B44Tab,
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier,
    onTab: (B44Tab) -> Unit,
) {
    Column(
        modifier.height(56.dp).clip(RoundedCornerShape(14.dp))
            .background(if (selected) B44Colors.Emerald.copy(alpha = .10f) else Color.Transparent)
            .border(if (selected) 1.dp else 0.dp, if (selected) B44Colors.Emerald.copy(alpha = .20f) else Color.Transparent, RoundedCornerShape(14.dp))
            .clickable { onTab(tab) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = if (selected) B44Colors.Emerald else Color.White.copy(alpha = .35f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, color = if (selected) B44Colors.Emerald else Color.White.copy(alpha = .30f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun B44Placeholder(title: String, subtitle: String, active: B44Tab, onTab: (B44Tab) -> Unit) {
    B44Background {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Filled.AutoAwesome, null, tint = B44Colors.Emerald, modifier = Modifier.size(38.dp))
                Spacer(Modifier.height(16.dp))
                Text(title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(8.dp))
                Text(subtitle, color = Color.White.copy(alpha = .45f), fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Center)
            }
            B44BottomNav(active, Modifier.align(Alignment.BottomCenter), onTab)
        }
    }
}
