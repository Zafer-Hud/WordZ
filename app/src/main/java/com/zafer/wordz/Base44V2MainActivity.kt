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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
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

internal object B44V2Colors {
    val Bg0 = Color(0xFF040C18)
    val Bg1 = Color(0xFF071420)
    val Bg2 = Color(0xFF050F1E)
    val Deep = Color(0xFF070E1A)
    val Mid = Color(0xFF0D1829)
    val Emerald = Color(0xFF10D98A)
    val EmeraldDark = Color(0xFF0AA86A)
    val Gold = Color(0xFFFFCA44)
    val GoldDark = Color(0xFFD4A017)
    val Cyan = Color(0xFF38BDF8)
    val Orange = Color(0xFFFB923C)
    val Purple = Color(0xFF9B51E0)
    val Red = Color(0xFFEF4444)
}

internal const val B44V2_PREFS = "wordz_progress"
internal const val B44V2_COINS = "coins"
internal const val B44V2_UNLOCKED = "unlocked_level"
internal const val B44V2_COMPLETED = "completed_levels"
internal const val B44V2_HINTS = "b44_hints"

internal enum class B44V2Screen { HOME, MAP, GAME, MARKET, PROFILE }

internal data class B44Country(
    val id: String,
    val name: String,
    val emoji: String,
    val start: Int,
    val end: Int,
    val accent: Color,
    val subtitle: String,
)

internal val b44Countries = listOf(
    B44Country("misir", "MISIR", "🇪🇬", 1, 20, Color(0xFFF5C842), "Piramitlerin gizemini çöz"),
    B44Country("fransa", "FRANSA", "🇫🇷", 21, 40, Color(0xFF38BDF8), "Işıklar şehrinde kelimeleri bul"),
    B44Country("ispanya", "İSPANYA", "🇪🇸", 41, 60, Color(0xFFF43F5E), "Akdeniz ritmini yakala"),
    B44Country("turkiye", "TÜRKİYE", "🇹🇷", 61, 80, Color(0xFFFB923C), "Anadolu boyunca keşfet"),
    B44Country("sili", "ŞİLİ", "🇨🇱", 81, 100, Color(0xFF22C55E), "And Dağları'na uzan"),
    B44Country("japonya", "JAPONYA", "🇯🇵", 101, 120, Color(0xFFF472B6), "Uzak doğunun son meydan okuması"),
)

class Base44V2MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            ) { B44V2App() }
        }
    }
}

@Composable
private fun B44V2App() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences(B44V2_PREFS, Context.MODE_PRIVATE) }
    val playerStore = remember { PlayerProgressStore(prefs) }

    var showSplash by remember { mutableStateOf(true) }
    var screen by remember { mutableStateOf(B44V2Screen.HOME) }
    var selectedLevel by remember { mutableIntStateOf(prefs.getInt(B44V2_UNLOCKED, 0).coerceIn(0, levels.lastIndex)) }
    var coins by remember { mutableIntStateOf(if (prefs.contains(B44V2_COINS)) prefs.getInt(B44V2_COINS, 500) else 500) }
    var hints by remember { mutableIntStateOf(prefs.getInt(B44V2_HINTS, 3).coerceAtLeast(0)) }
    var unlocked by remember { mutableIntStateOf(prefs.getInt(B44V2_UNLOCKED, 0).coerceIn(0, levels.lastIndex)) }
    var completed by remember {
        mutableStateOf(
            prefs.getStringSet(B44V2_COMPLETED, emptySet()).orEmpty()
                .mapNotNull { it.toIntOrNull() }.filter { it in levels.indices }.toSet()
        )
    }
    var player by remember { mutableStateOf(playerStore.snapshot()) }

    fun saveCore() {
        prefs.edit()
            .putInt(B44V2_COINS, coins)
            .putInt(B44V2_HINTS, hints)
            .putInt(B44V2_UNLOCKED, unlocked)
            .putStringSet(B44V2_COMPLETED, completed.map(Int::toString).toSet())
            .apply()
    }

    fun openLevel(index: Int) {
        selectedLevel = index.coerceIn(0, levels.lastIndex)
        screen = B44V2Screen.GAME
    }

    Box(Modifier.fillMaxSize()) {
        when (screen) {
            B44V2Screen.HOME -> B44V2Home(
                coins = coins,
                currentLevel = unlocked + 1,
                player = player,
                onPlay = { openLevel(unlocked) },
                onTab = { screen = it },
            )
            B44V2Screen.MAP -> B44V2WorldMap(
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
                onCoins = { coins = it.coerceAtLeast(0); saveCore() },
                onHints = { hints = it.coerceAtLeast(0); saveCore() },
                onRewardXp = { amount -> player = playerStore.grant(amount).after },
                onComplete = {
                    completed = completed + selectedLevel
                    if (selectedLevel >= unlocked && unlocked < levels.lastIndex) unlocked = selectedLevel + 1
                    saveCore()
                },
                back = { screen = B44V2Screen.MAP },
                next = {
                    if (selectedLevel < levels.lastIndex) openLevel(selectedLevel + 1) else screen = B44V2Screen.MAP
                },
                home = { screen = B44V2Screen.HOME },
            )
            B44V2Screen.MARKET -> B44V2Placeholder("Market", "Base44 market tasarımı bir sonraki pakette bağlanacak.", B44V2Screen.MARKET) { screen = it }
            B44V2Screen.PROFILE -> B44V2Placeholder("Profil", "Base44 profil ve koleksiyon ekranı bir sonraki pakette bağlanacak.", B44V2Screen.PROFILE) { screen = it }
        }

        AnimatedVisibility(visible = showSplash, enter = fadeIn(), exit = fadeOut(tween(700))) {
            B44V2Splash { showSplash = false }
        }
    }
}

@Composable
internal fun B44V2Background(content: @Composable () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(B44V2Colors.Bg0, B44V2Colors.Bg1, B44V2Colors.Bg2))
        )
    ) {
        B44V2Ambient()
        content()
    }
}

@Composable
private fun B44V2Ambient() {
    val transition = rememberInfiniteTransition(label = "ambient")
    val a by transition.animateFloat(1f, 1.10f, infiniteRepeatable(tween(7000), RepeatMode.Reverse), label = "a")
    val b by transition.animateFloat(1f, 1.15f, infiniteRepeatable(tween(9000), RepeatMode.Reverse), label = "b")
    Box(
        Modifier.size(500.dp).offset((-110).dp, (-160).dp).scale(a)
            .background(Brush.radialGradient(listOf(B44V2Colors.Emerald.copy(alpha = .18f), Color.Transparent)), CircleShape)
    )
    Box(
        Modifier.align(Alignment.BottomEnd).size(400.dp).offset(85.dp, 55.dp).scale(b)
            .background(Brush.radialGradient(listOf(B44V2Colors.Cyan.copy(alpha = .12f), Color.Transparent)), CircleShape)
    )
    B44V2Stars(44, 25)
}

@Composable
private fun B44V2Stars(seed: Int, count: Int) {
    val stars = remember(seed, count) {
        val r = Random(seed)
        List(count) { Triple(r.nextFloat(), r.nextFloat() * .86f, .6f + r.nextFloat() * 1.4f) }
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        stars.forEachIndexed { i, star ->
            val transition = rememberInfiniteTransition(label = "s$i")
            val alpha by transition.animateFloat(.08f, .65f, infiniteRepeatable(tween(2200 + i * 83), RepeatMode.Reverse), label = "sa$i")
            Box(Modifier.offset(maxWidth * star.first, maxHeight * star.second).size(star.third.dp).alpha(alpha).background(Color.White, CircleShape))
        }
    }
}

@Composable
private fun B44V2Splash(done: () -> Unit) {
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
        B44V2Stars(144, 25)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, B44V2Colors.Emerald))))
                Spacer(Modifier.width(12.dp)); Box(Modifier.size(8.dp).background(B44V2Colors.Emerald, CircleShape)); Spacer(Modifier.width(12.dp))
                Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(B44V2Colors.Emerald, Color.Transparent))))
            }
            Spacer(Modifier.height(22.dp))
            Text("WORDS", color = Color.White, fontSize = 58.sp, lineHeight = 58.sp, fontWeight = FontWeight.Black)
            Text("Z", color = B44V2Colors.Emerald, fontSize = 102.sp, lineHeight = 88.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(12.dp))
            Text("KELİME BULMACA", color = Color.White.copy(alpha = .30f), fontSize = 10.sp, letterSpacing = 3.sp)
            Spacer(Modifier.height(58.dp))
            Box(Modifier.width(208.dp).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = .07f))) {
                Box(Modifier.fillMaxWidth((progress / 100f).coerceIn(0f, 1f)).height(4.dp).background(Brush.horizontalGradient(listOf(B44V2Colors.Emerald, B44V2Colors.Gold))))
            }
            Spacer(Modifier.height(12.dp))
            Text("YÜKLENİYOR", color = Color.White.copy(alpha = .25f), fontSize = 10.sp, letterSpacing = 3.sp)
        }
    }
}

@Composable
private fun B44V2Home(coins: Int, currentLevel: Int, player: PlayerProgress, onPlay: () -> Unit, onTab: (B44V2Screen) -> Unit) {
    B44V2Background {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 104.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    B44V2GlassChip("◆", coins.toString(), B44V2Colors.Gold)
                    B44V2GlassChip("⚡", "Lv.${player.level}   ${player.totalXp} XP", B44V2Colors.Emerald)
                }
                Spacer(Modifier.height(26.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent, B44V2Colors.Emerald.copy(alpha=.5f)))))
                    Spacer(Modifier.width(10.dp)); Text("KELİME OYUNU", color = B44V2Colors.Emerald.copy(alpha=.65f), fontSize = 10.sp, letterSpacing = 3.sp); Spacer(Modifier.width(10.dp))
                    Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(B44V2Colors.Emerald.copy(alpha=.5f), Color.Transparent))))
                }
                Spacer(Modifier.height(14.dp))
                Text("WORDS", color = Color.White, fontSize = 50.sp, lineHeight = 50.sp, fontWeight = FontWeight.Black)
                Text("Z", color = B44V2Colors.Emerald, fontSize = 84.sp, lineHeight = 72.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(26.dp))
                B44V2PlayButton(onPlay)
                Spacer(Modifier.height(24.dp))
                Row(Modifier.clip(CircleShape).background(Color.White.copy(alpha=.045f)).border(1.dp, Color.White.copy(alpha=.09f), CircleShape).padding(horizontal=18.dp, vertical=9.dp), verticalAlignment=Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(B44V2Colors.Emerald, CircleShape)); Spacer(Modifier.width(8.dp))
                    Text("Bölüm ", color=Color.White.copy(alpha=.55f), fontSize=13.sp); Text(currentLevel.toString(), color=Color.White, fontWeight=FontWeight.Bold, fontSize=13.sp); Text(" aktif", color=Color.White.copy(alpha=.55f), fontSize=13.sp)
                }
                Spacer(Modifier.height(28.dp))
                Text("OYUN MODU", color = Color.White.copy(alpha=.30f), fontSize=10.sp, fontWeight=FontWeight.Bold, letterSpacing=2.sp, modifier=Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    B44V2ModeCard("📖", "Klasik", "Kelimeleri bul, dünyayı keşfet", B44V2Colors.Emerald, false, onPlay, Modifier.weight(1f))
                    B44V2ModeCard("⚡", "Hız Modu", "Süre yarışı — ne kadar hızlısın?", B44V2Colors.Orange, true, {}, Modifier.weight(1f))
                    B44V2ModeCard("🏆", "Günlük Görev", "Her gün yeni meydan okuma", B44V2Colors.Gold, true, {}, Modifier.weight(1f))
                }
                Spacer(Modifier.height(24.dp))
                B44V2Leaderboard()
            }
            B44V2BottomNav(B44V2Screen.HOME, Modifier.align(Alignment.BottomCenter), onTab)
        }
    }
}

@Composable
private fun B44V2GlassChip(symbol: String, text: String, tint: Color) {
    Row(Modifier.clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha=.05f)).border(1.dp, Color.White.copy(alpha=.09f), RoundedCornerShape(16.dp)).padding(horizontal=12.dp, vertical=8.dp), verticalAlignment=Alignment.CenterVertically) {
        Box(Modifier.size(20.dp).background(Brush.linearGradient(listOf(tint, if (tint==B44V2Colors.Gold) B44V2Colors.GoldDark else B44V2Colors.EmeraldDark)), CircleShape), contentAlignment=Alignment.Center) { Text(symbol, color=Color.White, fontSize=9.sp, fontWeight=FontWeight.Black) }
        Spacer(Modifier.width(7.dp)); Text(text, color=Color.White, fontSize=12.sp, fontWeight=FontWeight.Bold)
    }
}

@Composable
private fun B44V2PlayButton(click: () -> Unit) {
    val t = rememberInfiniteTransition(label="play")
    val p by t.animateFloat(1f,1.22f,infiniteRepeatable(tween(2600),RepeatMode.Reverse),label="p")
    Box(Modifier.size(190.dp), contentAlignment=Alignment.Center) {
        Box(Modifier.size(160.dp).scale(p).alpha(.20f).background(Brush.radialGradient(listOf(B44V2Colors.Emerald.copy(alpha=.4f),Color.Transparent)),CircleShape))
        Column(Modifier.size(144.dp).shadow(28.dp,CircleShape,ambientColor=B44V2Colors.Emerald.copy(alpha=.4f),spotColor=B44V2Colors.Emerald.copy(alpha=.4f)).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF1AE896),B44V2Colors.Emerald,B44V2Colors.EmeraldDark))).border(1.dp,Color.White.copy(alpha=.15f),CircleShape).clickable(onClick=click), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.Center) {
            Icon(Icons.Filled.PlayArrow,null,tint=Color.White,modifier=Modifier.size(42.dp)); Text("OYNA",color=Color.White,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)
        }
    }
}

@Composable
private fun B44V2ModeCard(emoji:String,title:String,desc:String,color:Color,locked:Boolean,click:()->Unit,modifier:Modifier) {
    Box(modifier.height(126.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(color.copy(alpha=.18f),color.copy(alpha=.05f)))).border(1.dp,color.copy(alpha=.3f),RoundedCornerShape(16.dp)).alpha(if(locked).6f else 1f).clickable(enabled=!locked,onClick=click).padding(9.dp)) {
        if(locked) Icon(Icons.Filled.Lock,null,tint=Color.White.copy(alpha=.35f),modifier=Modifier.size(13.dp).align(Alignment.TopEnd))
        Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
            Text(emoji,fontSize=22.sp); Text(title,color=color,fontSize=11.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center); Spacer(Modifier.height(4.dp)); Text(desc,color=Color.White.copy(alpha=.4f),fontSize=9.sp,textAlign=TextAlign.Center,lineHeight=11.sp)
        }
    }
}

@Composable
private fun B44V2Leaderboard() {
    val names = listOf("KelimeUstası","ZekaAvcısı","MaviKedi","Gezgin","BulmacaTR")
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha=.04f)).border(1.dp,Color.White.copy(alpha=.08f),RoundedCornerShape(18.dp))) {
        Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically) { Text("🏆",fontSize=16.sp); Spacer(Modifier.width(8.dp)); Text("Liderlik Tablosu",color=Color.White,fontWeight=FontWeight.Bold,fontSize=14.sp) }
        names.forEachIndexed { i,n ->
            Row(Modifier.fillMaxWidth().background(if(i==0) B44V2Colors.Gold.copy(alpha=.05f) else Color.Transparent).padding(horizontal=14.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically) {
                Text(if(i<3) listOf("🥇","🥈","🥉")[i] else "${i+1}.",fontSize=13.sp,modifier=Modifier.width(28.dp)); Box(Modifier.size(28.dp).background(Color.hsv((i*60f)%360, .6f,.45f),CircleShape),contentAlignment=Alignment.Center){Text(n.take(1),color=Color.White,fontWeight=FontWeight.Bold,fontSize=11.sp)}; Spacer(Modifier.width(9.dp)); Column(Modifier.weight(1f)){Text(n,color=Color.White,fontSize=12.sp,fontWeight=FontWeight.SemiBold);Text("Lv.${i+2} · ${i+3} seviye",color=Color.White.copy(alpha=.35f),fontSize=9.sp)}; Text("${(5-i)*1240}",color=B44V2Colors.Gold,fontSize=11.sp,fontWeight=FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun B44V2WorldMap(unlocked:Int, completed:Set<Int>, back:()->Unit, onOpenLevel:(Int)->Unit, onTab:(B44V2Screen)->Unit) {
    var selected by remember { mutableStateOf<B44Country?>(null) }
    B44V2Background {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().statusBarsPadding().padding(bottom=92.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal=18.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha=.07f)).border(1.dp,Color.White.copy(alpha=.1f),RoundedCornerShape(12.dp)).clickable(onClick=back),contentAlignment=Alignment.Center){Icon(Icons.Filled.ArrowBack,null,tint=Color.White.copy(alpha=.65f),modifier=Modifier.size(20.dp))}; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)){Text("Dünya Haritası",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Black);Text("Kelimelerle dünyayı keşfet",color=Color.White.copy(alpha=.38f),fontSize=10.sp)}; Text("${b44Countries.count{ b44CountryUnlocked(it,completed) }}/${b44Countries.size}",color=B44V2Colors.Emerald,fontWeight=FontWeight.Bold,fontSize=12.sp)
                }
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=18.dp).padding(bottom=20.dp)) {
                    b44Countries.forEachIndexed { index,country ->
                        val open = b44CountryUnlocked(country,completed)
                        val done = (country.start-1..country.end-1).count { it in completed }
                        val total = country.end-country.start+1
                        val active = (unlocked+1) in country.start..country.end
                        B44V2CountryCard(index,country,done,total,open,active) { if(open) selected=country }
                        if(index<b44Countries.lastIndex) { Box(Modifier.padding(start=25.dp).width(2.dp).height(22.dp).background(Color.White.copy(alpha=.08f))) }
                    }
                }
            }
            B44V2BottomNav(B44V2Screen.MAP,Modifier.align(Alignment.BottomCenter),onTab)
            selected?.let { country -> B44V2CountrySheet(country,completed,unlocked,{selected=null}) {
                val first = (country.start-1).coerceAtMost(levels.lastIndex)
                val candidate = if((unlocked+1) in country.start..country.end) unlocked else first
                if(candidate in levels.indices) onOpenLevel(candidate)
                selected=null
            } }
        }
    }
}

private fun b44CountryUnlocked(country:B44Country,completed:Set<Int>):Boolean {
    val index=b44Countries.indexOfFirst{it.id==country.id}
    if(index<=0) return true
    val prev=b44Countries[index-1]
    val prevDone=(prev.start-1..prev.end-1).count{it in completed}
    return prevDone >= ((prev.end-prev.start+1)/2)
}

@Composable
private fun B44V2CountryCard(index:Int,country:B44Country,done:Int,total:Int,open:Boolean,active:Boolean,click:()->Unit) {
    val progress=(done.toFloat()/total.coerceAtLeast(1)).coerceIn(0f,1f)
    Row(Modifier.fillMaxWidth().height(116.dp).clip(RoundedCornerShape(20.dp)).background(if(active) country.accent.copy(alpha=.10f) else Color.White.copy(alpha=.035f)).border(1.dp,if(active) country.accent.copy(alpha=.35f) else Color.White.copy(alpha=.08f),RoundedCornerShape(20.dp)).alpha(if(open)1f else .48f).clickable(enabled=open,onClick=click).padding(14.dp),verticalAlignment=Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(country.accent.copy(alpha=.32f),country.accent.copy(alpha=.10f)))).border(1.dp,country.accent.copy(alpha=.35f),RoundedCornerShape(16.dp)),contentAlignment=Alignment.Center){ if(!open) Icon(Icons.Filled.Lock,null,tint=Color.White.copy(alpha=.45f)) else Text(country.emoji,fontSize=26.sp) }
        Spacer(Modifier.width(13.dp)); Column(Modifier.weight(1f)){ Row(verticalAlignment=Alignment.CenterVertically){Text(country.name,color=if(open)Color.White else Color.White.copy(alpha=.45f),fontWeight=FontWeight.Black,fontSize=15.sp); if(active){Spacer(Modifier.width(8.dp));Text("● Aktif",color=country.accent,fontSize=9.sp,fontWeight=FontWeight.Bold)} }; Text("${country.start}–${country.end} · ${country.subtitle}",color=Color.White.copy(alpha=.38f),fontSize=9.sp);Spacer(Modifier.height(10.dp));Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(Color.White.copy(alpha=.07f))){Box(Modifier.fillMaxWidth(progress).height(5.dp).background(country.accent))};Spacer(Modifier.height(5.dp));Row{Text(if(open)"$done/$total" else "Önceki bölgeyi %50 tamamla",color=Color.White.copy(alpha=.35f),fontSize=9.sp,modifier=Modifier.weight(1f));if(done==total)Text("✓ Tamam",color=B44V2Colors.Emerald,fontSize=9.sp,fontWeight=FontWeight.Bold)} }
        Spacer(Modifier.width(8.dp)); Box(Modifier.size(34.dp).background(if(done==total)B44V2Colors.Emerald.copy(alpha=.18f) else Color.White.copy(alpha=.05f),CircleShape),contentAlignment=Alignment.Center){if(done==total)Icon(Icons.Filled.Check,null,tint=B44V2Colors.Emerald,modifier=Modifier.size(17.dp))else Text("${index+1}",color=Color.White.copy(alpha=.4f),fontSize=11.sp,fontWeight=FontWeight.Bold)}
    }
}

@Composable
private fun B44V2CountrySheet(country:B44Country,completed:Set<Int>,unlocked:Int,close:()->Unit,start:()->Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=.68f)).clickable(onClick=close),contentAlignment=Alignment.BottomCenter) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart=34.dp,topEnd=34.dp)).background(Brush.verticalGradient(listOf(Color(0xFF122039),Color(0xFF070E1A)))).border(1.dp,Color.White.copy(alpha=.08f),RoundedCornerShape(topStart=34.dp,topEnd=34.dp)).clickable(enabled=false){}.padding(22.dp).navigationBarsPadding(),horizontalAlignment=Alignment.CenterHorizontally) {
            Box(Modifier.width(48.dp).height(4.dp).background(Color.White.copy(alpha=.12f),CircleShape));Spacer(Modifier.height(18.dp));Text(country.emoji,fontSize=46.sp);Spacer(Modifier.height(8.dp));Text(country.name,color=Color.White,fontSize=24.sp,fontWeight=FontWeight.Black);Text(country.subtitle,color=Color.White.copy(alpha=.42f),fontSize=11.sp);Spacer(Modifier.height(18.dp));val done=(country.start-1..country.end-1).count{it in completed};val total=country.end-country.start+1;Box(Modifier.fillMaxWidth().height(7.dp).clip(CircleShape).background(Color.White.copy(alpha=.08f))){Box(Modifier.fillMaxWidth(done.toFloat()/total).height(7.dp).background(country.accent))};Spacer(Modifier.height(8.dp));Row(Modifier.fillMaxWidth()){Text("$done/$total tamamlandı",color=Color.White.copy(alpha=.4f),fontSize=10.sp,modifier=Modifier.weight(1f));Row{repeat(5){Icon(Icons.Filled.Star,null,tint=if(it<((done*5)/total))B44V2Colors.Gold else Color.White.copy(alpha=.12f),modifier=Modifier.size(14.dp))}}};Spacer(Modifier.height(20.dp));Box(Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(17.dp)).background(Brush.horizontalGradient(listOf(country.accent,country.accent.copy(alpha=.72f)))).clickable(onClick=start),contentAlignment=Alignment.Center){Text(if(done==0)"Başla" else if((unlocked+1) in country.start..country.end)"Devam Et" else "Tekrar Oyna",color=Color.White,fontWeight=FontWeight.Black,fontSize=14.sp)}
        }
    }
}

@Composable
private fun B44V2Placeholder(title:String,subtitle:String,active:B44V2Screen,onTab:(B44V2Screen)->Unit) {
    B44V2Background { Box(Modifier.fillMaxSize()){Column(Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(title,color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Black);Spacer(Modifier.height(10.dp));Text(subtitle,color=Color.White.copy(alpha=.45f),textAlign=TextAlign.Center,fontSize=13.sp)};B44V2BottomNav(active,Modifier.align(Alignment.BottomCenter),onTab)} }
}

@Composable
internal fun B44V2BottomNav(active:B44V2Screen,modifier:Modifier=Modifier,onTab:(B44V2Screen)->Unit) {
    val items=listOf(Triple(B44V2Screen.HOME,Icons.Filled.Home,"Ana Sayfa"),Triple(B44V2Screen.MAP,Icons.Filled.Map,"Harita"),Triple(B44V2Screen.MARKET,Icons.Filled.ShoppingBag,"Market"),Triple(B44V2Screen.PROFILE,Icons.Filled.Person,"Profil"))
    Box(modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent,B44V2Colors.Deep.copy(alpha=.98f)))).padding(horizontal=14.dp,vertical=8.dp).navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(18.dp)).background(B44V2Colors.Mid.copy(alpha=.94f)).border(1.dp,Color.White.copy(alpha=.07f),RoundedCornerShape(18.dp)).padding(horizontal=4.dp),verticalAlignment=Alignment.CenterVertically) {
            items.forEach{(tab,icon,label)->val isActive=active==tab;Column(Modifier.weight(1f).height(54.dp).clip(RoundedCornerShape(14.dp)).background(if(isActive)B44V2Colors.Emerald.copy(alpha=.10f)else Color.Transparent).border(if(isActive)1.dp else 0.dp,if(isActive)B44V2Colors.Emerald.copy(alpha=.20f)else Color.Transparent,RoundedCornerShape(14.dp)).clickable{onTab(tab)},horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Icon(icon,null,tint=if(isActive)B44V2Colors.Emerald else Color.White.copy(alpha=.35f),modifier=Modifier.size(20.dp));Text(label,color=if(isActive)B44V2Colors.Emerald else Color.White.copy(alpha=.3f),fontSize=9.sp,fontWeight=FontWeight.SemiBold)}}
        }
    }
}
