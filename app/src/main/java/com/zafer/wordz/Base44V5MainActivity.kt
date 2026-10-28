package com.zafer.wordz

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

class Base44V5MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ensureBase44CountryCatalog()
        validateAllLevels()
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = B44V2Colors.Emerald,
                    secondary = B44V2Colors.Gold,
                    background = B44V2Colors.Deep,
                    surface = B44V2Colors.Mid,
                    onPrimary = Color.White,
                    onBackground = Color.White,
                    onSurface = Color.White,
                )
            ) { B44V5App() }
        }
    }
}

@Composable
private fun B44V5App() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences(B44V2_PREFS, Context.MODE_PRIVATE) }
    val playerStore = remember { PlayerProgressStore(prefs) }
    val profileStore = remember { Base44UserProfileStore(prefs) }

    var showSplash by remember { mutableStateOf(true) }
    var screen by remember { mutableStateOf(B44V2Screen.HOME) }
    var selectedLevel by remember { mutableIntStateOf(prefs.getInt(B44V2_UNLOCKED, 0).coerceIn(0, levels.lastIndex)) }
    var coins by remember { mutableIntStateOf(if (prefs.contains(B44V2_COINS)) prefs.getInt(B44V2_COINS, 500) else 500) }
    var hints by remember { mutableIntStateOf(prefs.getInt(B44V2_HINTS, 3).coerceAtLeast(0)) }
    var unlocked by remember { mutableIntStateOf(prefs.getInt(B44V2_UNLOCKED, 0).coerceIn(0, levels.lastIndex)) }
    var completed by remember {
        mutableStateOf(
            prefs.getStringSet(B44V2_COMPLETED, emptySet()).orEmpty()
                .mapNotNull { it.toIntOrNull() }
                .filter { it in levels.indices }
                .toSet()
        )
    }
    var player by remember { mutableStateOf(playerStore.snapshot()) }

    fun setCoinsPersist(value: Int) {
        val safe = value.coerceAtLeast(0)
        coins = safe
        prefs.edit().putInt(B44V2_COINS, safe).apply()
    }

    fun setHintsPersist(value: Int) {
        val safe = value.coerceAtLeast(0)
        hints = safe
        prefs.edit().putInt(B44V2_HINTS, safe).apply()
    }

    fun openLevel(index: Int) {
        if (index !in levels.indices) {
            GameToastCenter.warning("Bu bölümün içeriği henüz yüklenmemiş.")
            return
        }
        selectedLevel = index
        screen = B44V2Screen.GAME
    }

    Box(Modifier.fillMaxSize()) {
        when (screen) {
            B44V2Screen.HOME -> B44V5Home(
                coins = coins,
                currentLevel = unlocked + 1,
                player = player,
                completed = completed,
                profileStore = profileStore,
                onPlay = { openLevel(unlocked) },
                onTab = { screen = it },
            )
            B44V2Screen.MAP -> B44V5Map(
                unlocked = unlocked,
                completed = completed,
                back = { screen = B44V2Screen.HOME },
                onOpenLevel = ::openLevel,
                onTab = { screen = it },
            )
            B44V2Screen.GAME -> B44V2GameScreen(
                levelIndex = selectedLevel,
                coins = coins,
                hints = hints,
                completedBefore = selectedLevel in completed,
                onCoins = ::setCoinsPersist,
                onHints = ::setHintsPersist,
                onRewardXp = { amount -> player = playerStore.grant(amount).after },
                onComplete = {
                    val nextCompleted = completed + selectedLevel
                    val nextUnlocked = if (selectedLevel >= unlocked && unlocked < levels.lastIndex) selectedLevel + 1 else unlocked
                    completed = nextCompleted
                    unlocked = nextUnlocked
                    prefs.edit()
                        .putInt(B44V2_UNLOCKED, nextUnlocked)
                        .putStringSet(B44V2_COMPLETED, nextCompleted.map(Int::toString).toSet())
                        .apply()
                    GameToastCenter.success("Bölüm ${selectedLevel + 1} tamamlandı!")
                },
                back = { screen = B44V2Screen.MAP },
                next = { if (selectedLevel < levels.lastIndex) openLevel(selectedLevel + 1) else screen = B44V2Screen.MAP },
                home = { screen = B44V2Screen.HOME },
            )
            B44V2Screen.MARKET -> B44V5Market(coins, hints, ::setCoinsPersist, ::setHintsPersist) { screen = it }
            B44V2Screen.PROFILE -> B44V5Profile(coins, hints, player, completed) { screen = it }
        }

        GameToastHost(Modifier.align(Alignment.TopCenter))
        AnimatedVisibility(showSplash, enter = fadeIn(), exit = fadeOut(tween(700))) {
            B44V5Splash { showSplash = false }
        }
    }
}

@Composable
private fun B44V5Splash(done: () -> Unit) {
    var progress by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        repeat(50) { delay(38); progress = (it + 1) * 2 }
        delay(400)
        done()
    }
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF040C18), Color(0xFF071420), Color(0xFF030A14)))),
        contentAlignment = Alignment.Center,
    ) {
        B44V5Stars(144, 25)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, B44V2Colors.Emerald))))
                Spacer(Modifier.width(12.dp))
                Box(Modifier.size(8.dp).background(B44V2Colors.Emerald, CircleShape))
                Spacer(Modifier.width(12.dp))
                Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(B44V2Colors.Emerald, Color.Transparent))))
            }
            Spacer(Modifier.height(22.dp))
            Text("WORDS", color = Color.White, fontSize = 58.sp, lineHeight = 58.sp, fontWeight = FontWeight.Black)
            Text("Z", color = B44V2Colors.Emerald, fontSize = 102.sp, lineHeight = 88.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(12.dp))
            Text("KELİME BULMACA", color = Color.White.copy(alpha = .30f), fontSize = 10.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(58.dp))
            Box(Modifier.width(208.dp).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = .07f))) {
                Box(
                    Modifier.fillMaxWidth((progress / 100f).coerceIn(0f, 1f)).height(4.dp)
                        .background(Brush.horizontalGradient(listOf(B44V2Colors.Emerald, B44V2Colors.Gold)))
                )
            }
            Spacer(Modifier.height(12.dp))
            Text("YÜKLENİYOR", color = Color.White.copy(alpha = .25f), fontSize = 10.sp, letterSpacing = 3.sp)
        }
    }
}

@Composable
private fun B44V5Stars(seed: Int, count: Int) {
    val stars = remember(seed, count) {
        val random = Random(seed)
        List(count) { Triple(random.nextFloat(), random.nextFloat() * .86f, .6f + random.nextFloat() * 1.4f) }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        stars.forEachIndexed { index, star ->
            val transition = rememberInfiniteTransition(label = "v5star$index")
            val alpha by transition.animateFloat(
                .08f, .65f,
                infiniteRepeatable(tween(2200 + index * 83), RepeatMode.Reverse),
                label = "v5starAlpha$index",
            )
            Box(
                Modifier.offset(maxWidth * star.first, maxHeight * star.second)
                    .size(star.third.dp).alpha(alpha).background(Color.White, CircleShape)
            )
        }
    }
}

@Composable
private fun B44V5Home(
    coins: Int,
    currentLevel: Int,
    player: PlayerProgress,
    completed: Set<Int>,
    profileStore: Base44UserProfileStore,
    onPlay: () -> Unit,
    onTab: (B44V2Screen) -> Unit,
) {
    val profile = profileStore.snapshot()
    val level = player.totalXp / PlayerProgressStore.XP_PER_LEVEL + 1
    B44V2Background {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp).padding(bottom = 104.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    B44V5Chip("◆", coins.toString(), B44V2Colors.Gold)
                    B44V5Chip("⚡", "Lv.$level   ${player.totalXp} XP", B44V2Colors.Emerald)
                }
                Spacer(Modifier.height(26.dp))
                Text("KELİME OYUNU", color = B44V2Colors.Emerald.copy(alpha = .65f), fontSize = 10.sp, letterSpacing = 3.sp)
                Spacer(Modifier.height(12.dp))
                Text("WORDS", color = Color.White, fontSize = 50.sp, lineHeight = 50.sp, fontWeight = FontWeight.Black)
                Text("Z", color = B44V2Colors.Emerald, fontSize = 84.sp, lineHeight = 72.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(22.dp))
                B44V5Play(onPlay)
                Spacer(Modifier.height(20.dp))
                Row(
                    Modifier.clip(CircleShape).background(Color.White.copy(alpha = .045f))
                        .border(1.dp, Color.White.copy(alpha = .09f), CircleShape)
                        .padding(horizontal = 18.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(8.dp).background(B44V2Colors.Emerald, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text("Bölüm $currentLevel aktif", color = Color.White.copy(alpha = .62f), fontSize = 13.sp)
                }
                if (profile.banned) {
                    Spacer(Modifier.height(12.dp))
                    Text("⚠️ Profil erişimi kısıtlı", color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(28.dp))
                Text("OYUN MODU", color = Color.White.copy(alpha = .30f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    B44V5Mode("📖", "Klasik", "Kelimeleri bul, dünyayı keşfet", B44V2Colors.Emerald, false, onPlay, Modifier.weight(1f))
                    B44V5Mode("⚡", "Hız Modu", "Süre yarışı — ne kadar hızlısın?", B44V2Colors.Orange, true, { GameToastCenter.info("Hız Modu yakında.") }, Modifier.weight(1f))
                    B44V5Mode("🏆", "Günlük Görev", "Her gün yeni meydan okuma", B44V2Colors.Gold, true, { GameToastCenter.info("Günlük Görev yakında.") }, Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp))
                B44V5Leaderboard(profile.copy(completedLevels = completed))
            }
            B44V2BottomNav(B44V2Screen.HOME, Modifier.align(Alignment.BottomCenter), onTab)
        }
    }
}

@Composable
private fun B44V5Chip(symbol: String, text: String, tint: Color) {
    Row(
        Modifier.clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = .05f))
            .border(1.dp, Color.White.copy(alpha = .09f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(20.dp).background(tint, CircleShape), contentAlignment = Alignment.Center) {
            Text(symbol, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(7.dp))
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun B44V5Play(click: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "v5play")
    val pulse by transition.animateFloat(1f, 1.22f, infiniteRepeatable(tween(2600), RepeatMode.Reverse), label = "v5playPulse")
    Box(Modifier.size(190.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(160.dp).scale(pulse).alpha(.20f)
                .background(Brush.radialGradient(listOf(B44V2Colors.Emerald.copy(alpha = .4f), Color.Transparent)), CircleShape)
        )
        Column(
            Modifier.size(144.dp).shadow(28.dp, CircleShape, ambientColor = B44V2Colors.Emerald.copy(alpha = .4f), spotColor = B44V2Colors.Emerald.copy(alpha = .4f))
                .clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF1AE896), B44V2Colors.Emerald, B44V2Colors.EmeraldDark)))
                .border(1.dp, Color.White.copy(alpha = .15f), CircleShape).clickable(onClick = click),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(42.dp))
            Text("OYNA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun B44V5Mode(emoji: String, title: String, desc: String, color: Color, locked: Boolean, click: () -> Unit, modifier: Modifier) {
    Box(
        modifier.height(126.dp).clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(color.copy(alpha = .18f), color.copy(alpha = .05f))))
            .border(1.dp, color.copy(alpha = .3f), RoundedCornerShape(16.dp))
            .alpha(if (locked) .60f else 1f).clickable(onClick = click).padding(9.dp)
    ) {
        if (locked) Icon(Icons.Filled.Lock, null, tint = Color.White.copy(alpha = .35f), modifier = Modifier.size(13.dp).align(Alignment.TopEnd))
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(emoji, fontSize = 22.sp)
            Text(title, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(desc, color = Color.White.copy(alpha = .4f), fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 11.sp)
        }
    }
}

@Composable
private fun B44V5Leaderboard(profile: Base44LocalProfile) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = .04f))
            .border(1.dp, Color.White.copy(alpha = .08f), RoundedCornerShape(18.dp))
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🏆", fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Liderlik Tablosu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Yerel profil", color = Color.White.copy(alpha = .30f), fontSize = 8.sp)
            }
        }
        Row(
            Modifier.fillMaxWidth().background(B44V2Colors.Gold.copy(alpha = .05f)).padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🥇", fontSize = 14.sp, modifier = Modifier.width(30.dp))
            Box(Modifier.size(30.dp).background(B44V2Colors.Emerald.copy(alpha = .35f), CircleShape), contentAlignment = Alignment.Center) {
                Text(profile.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(profile.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text("Lv.${profile.playerLevel} · ${profile.completedLevels.size} bölüm", color = Color.White.copy(alpha = .35f), fontSize = 9.sp)
            }
            Text(profile.score.toString(), color = B44V2Colors.Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun b44V5CountryUnlocked(country: B44Country, completed: Set<Int>): Boolean {
    val index = base44CanonicalCountries.indexOfFirst { it.id == country.id }
    if (index <= 0) return true
    val previous = base44CanonicalCountries[index - 1]
    val done = (previous.start - 1..previous.end - 1).count { it in completed }
    return done >= 10
}

@Composable
private fun B44V5Map(
    unlocked: Int,
    completed: Set<Int>,
    back: () -> Unit,
    onOpenLevel: (Int) -> Unit,
    onTab: (B44V2Screen) -> Unit,
) {
    var selected by remember { mutableStateOf<B44Country?>(null) }
    B44V2Background {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().statusBarsPadding().padding(bottom = 92.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = .07f))
                            .border(1.dp, Color.White.copy(alpha = .1f), RoundedCornerShape(12.dp)).clickable(onClick = back),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White.copy(alpha = .65f), modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Dünya Haritası", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("${levels.size}/120 bölüm verisi yüklü", color = Color.White.copy(alpha = .38f), fontSize = 10.sp)
                    }
                    Text("${base44CanonicalCountries.count { b44V5CountryUnlocked(it, completed) }}/6", color = B44V2Colors.Emerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(bottom = 20.dp)) {
                    base44CanonicalCountries.forEachIndexed { index, country ->
                        val open = b44V5CountryUnlocked(country, completed)
                        val done = (country.start - 1..country.end - 1).count { it in completed }
                        val active = (unlocked + 1) in country.start..country.end
                        val hasContent = country.start <= levels.size
                        B44V5CountryCard(country, done, open, active, hasContent) { if (open) selected = country }
                        if (index < base44CanonicalCountries.lastIndex) {
                            Box(Modifier.padding(start = 25.dp).width(2.dp).height(22.dp).background(Color.White.copy(alpha = .08f)))
                        }
                    }
                }
            }
            B44V2BottomNav(B44V2Screen.MAP, Modifier.align(Alignment.BottomCenter), onTab)
            selected?.let { country ->
                B44V5CountrySheet(country, completed, unlocked, close = { selected = null }) {
                    val first = country.start - 1
                    val candidate = if ((unlocked + 1) in country.start..country.end) unlocked else first
                    if (candidate in levels.indices) onOpenLevel(candidate)
                    else GameToastCenter.warning("${country.name} bölüm verileri henüz yüklenmedi.")
                    selected = null
                }
            }
        }
    }
}

@Composable
private fun B44V5CountryCard(country: B44Country, done: Int, open: Boolean, active: Boolean, hasContent: Boolean, click: () -> Unit) {
    val progress = (done / 20f).coerceIn(0f, 1f)
    Row(
        Modifier.fillMaxWidth().height(116.dp).clip(RoundedCornerShape(20.dp))
            .background(if (active) country.accent.copy(alpha = .10f) else Color.White.copy(alpha = .035f))
            .border(1.dp, if (active) country.accent.copy(alpha = .35f) else Color.White.copy(alpha = .08f), RoundedCornerShape(20.dp))
            .alpha(if (open) 1f else .48f).clickable(enabled = open, onClick = click).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(country.accent.copy(alpha = .16f))
                .border(1.dp, country.accent.copy(alpha = .35f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) { if (!open) Icon(Icons.Filled.Lock, null, tint = Color.White.copy(alpha = .45f)) else Text(country.emoji, fontSize = 26.sp) }
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(country.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                if (active) {
                    Spacer(Modifier.width(8.dp))
                    Text("● Aktif", color = country.accent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text("${country.start}–${country.end} · ${country.subtitle}", color = Color.White.copy(alpha = .38f), fontSize = 9.sp)
            Spacer(Modifier.height(9.dp))
            Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(Color.White.copy(alpha = .07f))) {
                Box(Modifier.fillMaxWidth(progress).height(5.dp).background(country.accent))
            }
            Spacer(Modifier.height(5.dp))
            Text(
                when {
                    !open -> "Önceki bölgeyi %50 tamamla"
                    !hasContent -> "Bölüm verisi bekleniyor"
                    else -> "$done/20"
                },
                color = Color.White.copy(alpha = .35f),
                fontSize = 9.sp,
            )
        }
    }
}

@Composable
private fun B44V5CountrySheet(country: B44Country, completed: Set<Int>, unlocked: Int, close: () -> Unit, play: () -> Unit) {
    val done = (country.start - 1..country.end - 1).count { it in completed }
    val hasContent = country.start <= levels.size
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .65f)).clickable(onClick = close), contentAlignment = Alignment.BottomCenter) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF13223B), Color(0xFF07101E))))
                .border(1.dp, country.accent.copy(alpha = .28f), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .clickable(enabled = false) {}.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(country.emoji, fontSize = 42.sp)
            Text(country.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("20 BÖLÜM", color = country.accent, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(8.dp))
            Text(country.subtitle, color = Color.White.copy(alpha = .45f), fontSize = 11.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(14.dp))
            Text("$done/20 tamamlandı", color = Color.White.copy(alpha = .52f), fontSize = 10.sp)
            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(15.dp))
                    .background(if (hasContent) country.accent else Color.White.copy(alpha = .08f))
                    .clickable(onClick = play),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (!hasContent) "İÇERİK BEKLENİYOR" else if ((unlocked + 1) in country.start..country.end) "DEVAM ET" else "BAŞLA",
                    color = if (hasContent) Color.White else Color.White.copy(alpha = .35f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}