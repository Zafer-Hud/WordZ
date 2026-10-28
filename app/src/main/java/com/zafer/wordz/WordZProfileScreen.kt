package com.zafer.wordz

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class WordZArtifact(
    val id: String,
    val name: String,
    val icon: String,
    val rarity: String,
    val world: String,
)

private val wordzArtifacts = listOf(
    WordZArtifact("hayvan_1", "Aslan Tacı", "🦁", "legendary", "hayvanlar"),
    WordZArtifact("hayvan_2", "Kartal Tüyü", "🪶", "epic", "hayvanlar"),
    WordZArtifact("hayvan_3", "Pati Rozeti", "🐾", "rare", "hayvanlar"),
    WordZArtifact("hayvan_4", "Yunus Madalyonu", "🐬", "epic", "hayvanlar"),
    WordZArtifact("hayvan_5", "Panda Figürü", "🐼", "rare", "hayvanlar"),
    WordZArtifact("hayvan_6", "Kelebek Kanadı", "🦋", "common", "hayvanlar"),

    WordZArtifact("sehir_1", "İstanbul Silüeti", "🌉", "legendary", "sehirler"),
    WordZArtifact("sehir_2", "Ankara Anıtı", "🏛️", "epic", "sehirler"),
    WordZArtifact("sehir_3", "Saat Kulesi", "🕰️", "rare", "sehirler"),
    WordZArtifact("sehir_4", "Kapadokya Balonu", "🎈", "epic", "sehirler"),
    WordZArtifact("sehir_5", "Akdeniz Güneşi", "☀️", "rare", "sehirler"),
    WordZArtifact("sehir_6", "Şehir Kartı", "🏙️", "common", "sehirler"),

    WordZArtifact("yemek_1", "Baklava Tepsisi", "🍯", "legendary", "yemekler"),
    WordZArtifact("yemek_2", "Kebap Şişi", "🍢", "epic", "yemekler"),
    WordZArtifact("yemek_3", "Mantı Kasesi", "🥟", "rare", "yemekler"),
    WordZArtifact("yemek_4", "Simit Ustası", "🥨", "epic", "yemekler"),
    WordZArtifact("yemek_5", "Kahve Fincanı", "☕", "rare", "yemekler"),
    WordZArtifact("yemek_6", "Lokum Kutusu", "🍬", "common", "yemekler"),

    WordZArtifact("spor_1", "Altın Kupa", "🏆", "legendary", "spor"),
    WordZArtifact("spor_2", "Futbol Topu", "⚽", "epic", "spor"),
    WordZArtifact("spor_3", "Basketbol Topu", "🏀", "rare", "spor"),
    WordZArtifact("spor_4", "Tenis Raketi", "🎾", "epic", "spor"),
    WordZArtifact("spor_5", "Boks Eldiveni", "🥊", "rare", "spor"),
    WordZArtifact("spor_6", "Yüzme Madalyası", "🏅", "common", "spor"),
)

private fun worldLabel(id: String): String = when (id) {
    "hayvanlar" -> "Hayvanlar"
    "sehirler" -> "Şehirler"
    "yemekler" -> "Yemekler"
    "spor" -> "Spor"
    else -> id
}

private fun rarityColor(rarity: String): Color = when (rarity) {
    "legendary" -> B44V2Colors.Gold
    "epic" -> Color(0xFFA78BFA)
    "rare" -> Color(0xFF60A5FA)
    else -> Color.White.copy(alpha = .48f)
}

@Composable
internal fun WordZProfileScreen(
    coins: Int,
    hints: Int,
    player: PlayerProgress,
    completed: Set<Int>,
    onTab: (B44V2Screen) -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(B44V2_PREFS, Context.MODE_PRIVATE) }
    val profile = remember(coins, hints, player.totalXp, completed) {
        Base44UserProfileStore(prefs).snapshot().copy(completedLevels = completed)
    }
    var tab by remember { mutableStateOf("stats") }

    B44V2Background {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp).padding(bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(12.dp))
                Box(Modifier.size(78.dp).background(B44V2Colors.Emerald.copy(alpha = .20f), CircleShape).border(2.dp, B44V2Colors.Emerald.copy(alpha = .5f), CircleShape), contentAlignment = Alignment.Center) {
                    Text(profile.name.take(1).uppercase(), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    Box(Modifier.align(Alignment.BottomEnd).size(25.dp).background(B44V2Colors.Gold, CircleShape), contentAlignment = Alignment.Center) {
                        Text(player.level.toString(), color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.height(9.dp))
                Text(profile.name, color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                Text(profileTitle(player.level), color = B44V2Colors.Emerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Color.White.copy(alpha = .07f))) {
                    Box(Modifier.fillMaxWidth((player.currentLevelXp / 500f).coerceIn(0f, 1f)).height(6.dp).background(Brush.horizontalGradient(listOf(B44V2Colors.Emerald, B44V2Colors.Gold))))
                }
                Spacer(Modifier.height(5.dp))
                Text("${player.currentLevelXp}/500 XP", color = B44Ui.DimText, fontSize = 9.sp)

                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileTab("stats", "İstatistik", tab == "stats", Modifier.weight(1f)) { tab = "stats" }
                    ProfileTab("ach", "Başarılar", tab == "ach", Modifier.weight(1f)) { tab = "ach" }
                    ProfileTab("collection", "Koleksiyon", tab == "collection", Modifier.weight(1f)) { tab = "collection" }
                }
                Spacer(Modifier.height(14.dp))

                when (tab) {
                    "stats" -> StatsGrid(coins, hints, player, completed, profile.score)
                    "ach" -> Achievements(coins, player, completed)
                    else -> ThemedCollection(completed)
                }
            }
            B44V2BottomNav(B44V2Screen.PROFILE, Modifier.align(Alignment.BottomCenter), onTab)
        }
    }
}

private fun profileTitle(level: Int): String = when {
    level < 5 -> "Çaylak"
    level < 10 -> "Kelime Ustası"
    level < 20 -> "Kelime Uzmanı"
    else -> "Kelime Efsanesi"
}

@Composable
private fun ProfileTab(id: String, label: String, active: Boolean, modifier: Modifier, click: () -> Unit) {
    Box(
        modifier.height(39.dp).clip(RoundedCornerShape(13.dp))
            .background(if (active) B44V2Colors.Emerald.copy(alpha = .16f) else B44Ui.Glass)
            .border(1.dp, if (active) B44V2Colors.Emerald.copy(alpha = .35f) else B44Ui.Border, RoundedCornerShape(13.dp))
            .clickable(onClick = click),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = if (active) B44V2Colors.Emerald else B44Ui.MutedText, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun StatsGrid(coins: Int, hints: Int, player: PlayerProgress, completed: Set<Int>, score: Int) {
    val stats = listOf(
        Triple("◆", "Altın", coins.toString()),
        Triple("⚡", "XP", player.totalXp.toString()),
        Triple("🏆", "Skor", score.toString()),
        Triple("✓", "Tamamlanan", "${completed.size}/120"),
        Triple("💡", "İpucu", hints.toString()),
        Triple("★", "Seviye", player.level.toString()),
    )
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        stats.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                pair.forEach { (icon, label, value) ->
                    Column(
                        Modifier.weight(1f).height(86.dp).clip(RoundedCornerShape(17.dp)).background(B44Ui.Glass)
                            .border(1.dp, B44Ui.Border, RoundedCornerShape(17.dp)).padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(icon, fontSize = 16.sp)
                        Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text(label, color = B44Ui.MutedText, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun Achievements(coins: Int, player: PlayerProgress, completed: Set<Int>) {
    val items = listOf(
        Triple("İlk Adım", "İlk bölümü tamamla", completed.size >= 1),
        Triple("Kelime Avcısı", "5 bölüm tamamla", completed.size >= 5),
        Triple("Kelime Ustası", "10 bölüm tamamla", completed.size >= 10),
        Triple("Tema Gezgini", "30 bölüm tamamla", completed.size >= 30),
        Triple("Zengin", "1000 altına ulaş", coins >= 1000),
        Triple("XP Koleksiyoncusu", "500 XP kazan", player.totalXp >= 500),
        Triple("Efsane", "10. seviyeye ulaş", player.level >= 10),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (title, desc, unlocked) ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (unlocked) B44V2Colors.Emerald.copy(alpha = .08f) else B44Ui.Glass)
                    .border(1.dp, if (unlocked) B44V2Colors.Emerald.copy(alpha = .25f) else B44Ui.Border, RoundedCornerShape(16.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(if (unlocked) "🏅" else "🔒", fontSize = 20.sp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, color = if (unlocked) Color.White else Color.White.copy(alpha = .45f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(desc, color = B44Ui.DimText, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun ThemedCollection(completed: Set<Int>) {
    var filter by remember { mutableStateOf("all") }
    val unlockedCount = (completed.size / 2).coerceAtMost(wordzArtifacts.size)
    val collected = wordzArtifacts.take(unlockedCount).map { it.id }.toSet()
    val filters = listOf("all" to "Tümü", "hayvanlar" to "Hayvanlar", "sehirler" to "Şehirler", "yemekler" to "Yemekler", "spor" to "Spor")
    val visible = if (filter == "all") wordzArtifacts else wordzArtifacts.filter { it.world == filter }

    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Koleksiyonum", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
            Text("${collected.size}/24 eser", color = B44V2Colors.Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(Color.White.copy(alpha = .07f))) {
            Box(Modifier.fillMaxWidth(collected.size / 24f).height(5.dp).background(Brush.horizontalGradient(listOf(B44V2Colors.Emerald, B44V2Colors.Gold))))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            filters.forEach { (id, label) ->
                val active = filter == id
                Box(
                    Modifier.clip(CircleShape).background(if (active) B44V2Colors.Emerald else B44Ui.GlassStrong)
                        .border(1.dp, if (active) Color.Transparent else B44Ui.Border, CircleShape)
                        .clickable { filter = id }.padding(horizontal = 11.dp, vertical = 6.dp)
                ) { Text(label, color = if (active) Color.White else B44Ui.MutedText, fontSize = 8.sp) }
            }
        }
        Spacer(Modifier.height(12.dp))
        visible.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { artifact ->
                    val owned = artifact.id in collected
                    val tint = rarityColor(artifact.rarity)
                    Column(
                        Modifier.weight(1f).height(116.dp).clip(RoundedCornerShape(16.dp))
                            .background(if (owned) tint.copy(alpha = .09f) else B44Ui.Glass)
                            .border(1.dp, if (owned) tint.copy(alpha = .28f) else B44Ui.Border, RoundedCornerShape(16.dp))
                            .alpha(if (owned) 1f else .48f).padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(if (owned) artifact.icon else "🔒", fontSize = 24.sp)
                        Spacer(Modifier.height(5.dp))
                        Text(if (owned) artifact.name else "???", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 9.sp)
                        if (owned) Text(worldLabel(artifact.world), color = tint, fontSize = 7.sp)
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
