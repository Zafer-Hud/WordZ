package com.zafer.wordz

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private data class B44MarketItem(
    val id:String,
    val name:String,
    val description:String,
    val type:String,
    val price:Int,
    val value:Int,
    val featured:Boolean=false,
)

private val b44MarketItems = listOf(
    B44MarketItem("d1","5 İpucu Paketi","Zor seviyelerde yardım alın","hint",200,5),
    B44MarketItem("d2","10 İpucu Paketi","Büyük ipucu avantajı","hint",350,10,true),
    B44MarketItem("d3","500 Altın","Market alışverişi için altın","coins",0,500),
    B44MarketItem("d4","Süper İpucu","Tüm kelimeyi açar","power_up",300,1),
)

private data class B44Artifact(val id:String,val name:String,val icon:String,val rarity:String,val country:String)

private val b44Artifacts = listOf(
    B44Artifact("misir_1","Firavun Maskesi","👑","legendary","misir"),
    B44Artifact("misir_2","Papirüs Rulosu","📜","rare","misir"),
    B44Artifact("misir_3","Skarabeus","🪲","common","misir"),
    B44Artifact("misir_4","Altın Mühür","🔱","epic","misir"),
    B44Artifact("fransa_1","Fransız Tacı","👸","legendary","fransa"),
    B44Artifact("fransa_2","Kılıç","⚔️","rare","fransa"),
    B44Artifact("fransa_3","Kadeh","🏺","common","fransa"),
    B44Artifact("fransa_4","Rölyef Plaka","🖼️","epic","fransa"),
    B44Artifact("sili_1","Inka Güneşi","☀️","legendary","sili"),
    B44Artifact("sili_2","Taş Heykel","🗿","epic","sili"),
    B44Artifact("sili_3","Renkli Taş","💎","rare","sili"),
    B44Artifact("sili_4","Kaya Resmi","🪨","common","sili"),
    B44Artifact("ispanya_1","Flamenco Gülü","🌹","legendary","ispanya"),
    B44Artifact("ispanya_2","Eski Harita","🗺️","epic","ispanya"),
    B44Artifact("ispanya_3","Seramik Vazo","🏺","rare","ispanya"),
    B44Artifact("ispanya_4","Demir Mühür","🔐","common","ispanya"),
    B44Artifact("turkiye_1","Osmanlı Fermanı","📋","legendary","turkiye"),
    B44Artifact("turkiye_2","Çini Kase","🫙","epic","turkiye"),
    B44Artifact("turkiye_3","Altın Sikke","🪙","rare","turkiye"),
    B44Artifact("turkiye_4","İpek Kumaş","🧣","common","turkiye"),
    B44Artifact("japonya_1","Katana","⚔️","legendary","japonya"),
    B44Artifact("japonya_2","Samurai Miğferi","⛩️","epic","japonya"),
    B44Artifact("japonya_3","Çay Kabı","🍵","rare","japonya"),
    B44Artifact("japonya_4","Bonsai Tohumu","🌿","common","japonya"),
)

private fun rarityColor(rarity:String)=when(rarity){
    "legendary"->B44V2Colors.Gold
    "epic"->Color(0xFFA78BFA)
    "rare"->Color(0xFF60A5FA)
    else->Color.White.copy(alpha=.5f)
}

private fun rarityLabel(rarity:String)=when(rarity){"legendary"->"Efsanevi";"epic"->"Epik";"rare"->"Nadir";else->"Sıradan"}

@Composable
internal fun B44V3Market(
    coins:Int,
    hints:Int,
    onCoins:(Int)->Unit,
    onHints:(Int)->Unit,
    onTab:(B44V2Screen)->Unit,
) {
    var filter by remember { mutableStateOf("all") }
    var toast by remember { mutableStateOf<Pair<String,String>?>(null) }
    val context=androidx.compose.ui.platform.LocalContext.current
    val prefs=remember{context.getSharedPreferences(B44V2_PREFS,Context.MODE_PRIVATE)}

    fun show(message:String,type:String){toast=message to type}
    fun buy(item:B44MarketItem){
        when {
            item.type=="coins" -> show("Bu özellik yakında eklenecek!","info")
            coins<item.price -> show("Yetersiz altın! Daha fazla kazan.","error")
            else -> {
                onCoins(coins-item.price)
                if(item.type=="hint") onHints(hints+item.value)
                if(item.type=="power_up") prefs.edit().putInt("b44_super_hint",prefs.getInt("b44_super_hint",0)+item.value).apply()
                show("${item.name} satın alındı!","success")
            }
        }
    }

    LaunchedEffect(toast){if(toast!=null){delay(2800);toast=null}}
    val filters=listOf("all" to "Tümü","hint" to "İpucu","coins" to "Altın","power_up" to "Güç","theme" to "Tema")
    val filtered=if(filter=="all") b44MarketItems else b44MarketItems.filter{it.type==filter}

    B44V2Background {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().statusBarsPadding().padding(bottom=92.dp)) {
                Column(Modifier.padding(horizontal=20.dp,vertical=10.dp)) {
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
                        Column { Text("Market",color=Color.White,fontSize=25.sp,fontWeight=FontWeight.Black);Text("Güçlenme zamanı",color=Color.White.copy(alpha=.4f),fontSize=10.sp) }
                        Row(Modifier.clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha=.05f)).border(1.dp,Color.White.copy(alpha=.09f),RoundedCornerShape(16.dp)).padding(horizontal=12.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(20.dp).background(Brush.linearGradient(listOf(B44V2Colors.Gold,B44V2Colors.GoldDark)),CircleShape),contentAlignment=Alignment.Center){Text("◆",color=Color.White,fontSize=9.sp)};Spacer(Modifier.width(7.dp));Text(coins.toString(),color=Color.White,fontSize=13.sp,fontWeight=FontWeight.Bold)}
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                        filters.forEach { (id,label) ->
                            val active=filter==id
                            Box(Modifier.clip(CircleShape).background(if(active)Brush.horizontalGradient(listOf(B44V2Colors.Emerald,B44V2Colors.EmeraldDark)) else Brush.horizontalGradient(listOf(Color.White.copy(alpha=.07f),Color.White.copy(alpha=.07f)))).border(if(active)0.dp else 1.dp,if(active)Color.Transparent else Color.White.copy(alpha=.1f),CircleShape).clickable{filter=id}.padding(horizontal=14.dp,vertical=7.dp)){Text(label,color=if(active)Color.White else Color.White.copy(alpha=.5f),fontSize=10.sp,fontWeight=FontWeight.SemiBold)}
                        }
                    }
                }
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=16.dp).padding(bottom=20.dp)) {
                    if(filtered.isEmpty()){
                        Column(Modifier.fillMaxWidth().padding(top=80.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("🛍️",fontSize=40.sp);Spacer(Modifier.height(10.dp));Text("Bu kategoride ürün yok",color=Color.White.copy(alpha=.35f),fontSize=12.sp)}
                    } else filtered.chunked(2).forEach { rowItems ->
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                            rowItems.forEach { item -> B44MarketCard(item,::buy,Modifier.weight(1f)) }
                            if(rowItems.size==1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
            B44V2BottomNav(B44V2Screen.MARKET,Modifier.align(Alignment.BottomCenter),onTab)
            AnimatedVisibility(toast!=null,enter=fadeIn(),exit=fadeOut(),modifier=Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top=10.dp,horizontal=16.dp)){toast?.let{B44MetaToast(it.first,it.second){toast=null}}}
        }
    }
}

@Composable
private fun B44MarketCard(item:B44MarketItem,buy:(B44MarketItem)->Unit,modifier:Modifier){
    val tint=when(item.type){"hint"->B44V2Colors.Emerald;"coins"->B44V2Colors.Gold;"power_up"->B44V2Colors.Purple;else->Color(0xFF38BDF8)}
    Column(modifier.height(202.dp).clip(RoundedCornerShape(18.dp)).background(if(item.featured)tint.copy(alpha=.12f)else Color.White.copy(alpha=.04f)).border(1.dp,if(item.featured)tint.copy(alpha=.30f)else Color.White.copy(alpha=.08f),RoundedCornerShape(18.dp)).padding(13.dp),verticalArrangement=Arrangement.SpaceBetween){
        Column {
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.Top){Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(if(item.featured)tint.copy(alpha=.22f)else Color.White.copy(alpha=.06f)).border(1.dp,Color.White.copy(alpha=.1f),RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center){Text(when(item.type){"hint"->"💡";"coins"->"◆";"power_up"->"⚡";else->"★"},fontSize=20.sp)};if(item.featured)Box(Modifier.clip(CircleShape).background(B44V2Colors.Gold).padding(horizontal=7.dp,vertical=3.dp)){Text("⭐ Popüler",color=Color.Black,fontSize=8.sp,fontWeight=FontWeight.Bold)}}
            Spacer(Modifier.height(10.dp));Text(item.name,color=Color.White,fontSize=13.sp,fontWeight=FontWeight.Black,lineHeight=15.sp);Spacer(Modifier.height(4.dp));Text(item.description,color=Color.White.copy(alpha=.4f),fontSize=9.sp,lineHeight=12.sp)
        }
        Box(Modifier.fillMaxWidth().height(38.dp).clip(RoundedCornerShape(12.dp)).background(if(item.price==0)Brush.horizontalGradient(listOf(B44V2Colors.Gold,B44V2Colors.GoldDark))else Brush.horizontalGradient(listOf(B44V2Colors.Emerald.copy(alpha=.25f),B44V2Colors.Emerald.copy(alpha=.10f)))).border(if(item.price==0)0.dp else 1.dp,if(item.price==0)Color.Transparent else B44V2Colors.Emerald.copy(alpha=.4f),RoundedCornerShape(12.dp)).clickable{buy(item)},contentAlignment=Alignment.Center){Text(if(item.price==0)"💰 Satın Al" else "◆ ${item.price}",color=if(item.price==0)Color.Black else B44V2Colors.Emerald,fontSize=11.sp,fontWeight=FontWeight.Black)}
    }
}

@Composable
internal fun B44V3Profile(
    coins:Int,
    hints:Int,
    player:PlayerProgress,
    completed:Set<Int>,
    onTab:(B44V2Screen)->Unit,
){
    var tab by remember { mutableStateOf("stats") }
    var fullCollection by remember { mutableStateOf(false) }
    if(fullCollection){B44V3Collection(completed,{fullCollection=false},onTab);return}
    val context=androidx.compose.ui.platform.LocalContext.current
    val prefs=remember{context.getSharedPreferences(B44V2_PREFS,Context.MODE_PRIVATE)}
    val score=Base44ProgressStore(prefs).score()
    val xp=player.totalXp
    val level=(xp/500)+1
    val xpProgress=((xp%500)/500f).coerceIn(0f,1f)
    val xpToNext=level*500
    val title=when{level<5->"Çaylak";level<10->"Kelime Ustası";level<20->"Kelime Uzmanı";else->"Kelime Efsanesi"}

    B44V2Background {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().statusBarsPadding().padding(bottom=92.dp)) {
                Column(Modifier.padding(horizontal=20.dp,vertical=10.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha=.07f)).border(1.dp,Color.White.copy(alpha=.1f),RoundedCornerShape(12.dp)).clickable{onTab(B44V2Screen.HOME)},contentAlignment=Alignment.Center){Icon(Icons.Filled.ArrowBack,null,tint=Color.White.copy(alpha=.6f),modifier=Modifier.size(20.dp))};Text("Profil",color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center,modifier=Modifier.weight(1f));Spacer(Modifier.width(40.dp))}
                    Spacer(Modifier.height(18.dp))
                    Box(Modifier.size(104.dp),contentAlignment=Alignment.Center){Box(Modifier.size(96.dp).clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(B44V2Colors.Emerald.copy(alpha=.20f),B44V2Colors.Emerald.copy(alpha=.05f)))).border(2.dp,B44V2Colors.Emerald.copy(alpha=.30f),RoundedCornerShape(28.dp)).shadow(18.dp,RoundedCornerShape(28.dp)),contentAlignment=Alignment.Center){Icon(Icons.Filled.Person,null,tint=B44V2Colors.Emerald.copy(alpha=.7f),modifier=Modifier.size(48.dp))};Box(Modifier.size(32.dp).align(Alignment.BottomEnd).background(Brush.linearGradient(listOf(B44V2Colors.Gold,B44V2Colors.GoldDark)),CircleShape),contentAlignment=Alignment.Center){Text(level.toString(),color=Color.Black,fontWeight=FontWeight.Black,fontSize=10.sp)}}
                    Spacer(Modifier.height(10.dp));Text("Oyuncu",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Black);Text(title,color=B44V2Colors.Emerald.copy(alpha=.75f),fontSize=10.sp)
                    Spacer(Modifier.height(16.dp));Row(Modifier.fillMaxWidth(.82f)){Text("XP: $xp",color=Color.White.copy(alpha=.4f),fontSize=9.sp,modifier=Modifier.weight(1f));Text("Lv.${level+1}: $xpToNext",color=Color.White.copy(alpha=.4f),fontSize=9.sp)};Spacer(Modifier.height(5.dp));Box(Modifier.fillMaxWidth(.82f).height(7.dp).clip(CircleShape).background(Color.White.copy(alpha=.08f))){Box(Modifier.fillMaxWidth(xpProgress).height(7.dp).background(Brush.horizontalGradient(listOf(B44V2Colors.Emerald,B44V2Colors.Gold))))}
                    Spacer(Modifier.height(20.dp));Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha=.05f)).border(1.dp,Color.White.copy(alpha=.08f),RoundedCornerShape(12.dp)).padding(4.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)){listOf("stats" to "İSTATİSTİK","ach" to "BAŞARIMLAR","collection" to "KOLEKSİYON").forEach{(id,label)->val active=tab==id;Box(Modifier.weight(1f).height(36.dp).clip(RoundedCornerShape(9.dp)).background(if(active)B44V2Colors.Emerald.copy(alpha=.18f)else Color.Transparent).border(if(active)1.dp else 0.dp,if(active)B44V2Colors.Emerald.copy(alpha=.30f)else Color.Transparent,RoundedCornerShape(9.dp)).clickable{tab=id},contentAlignment=Alignment.Center){Text(label,color=if(active)B44V2Colors.Emerald else Color.White.copy(alpha=.4f),fontSize=8.sp,fontWeight=FontWeight.Bold)}}}
                }
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=20.dp).padding(bottom=20.dp)) {
                    when(tab){
                        "stats"->B44ProfileStats(coins,hints,xp,score,level,completed.size)
                        "ach"->B44Achievements(coins,xp,level,completed.size)
                        else->B44ProfileCollectionPreview(completed.size){fullCollection=true}
                    }
                }
            }
            B44V2BottomNav(B44V2Screen.PROFILE,Modifier.align(Alignment.BottomCenter),onTab)
        }
    }
}

@Composable
private fun B44ProfileStats(coins:Int,hints:Int,xp:Int,score:Int,level:Int,completed:Int){
    val cards=listOf(Triple("◆","Altın",coins.toString()),Triple("⚡","XP",xp.toString()),Triple("★","Skor",score.toString()),Triple("🏆","Tamamlanan",completed.toString()),Triple("💡","İpucu",hints.toString()),Triple("🔥","Seviye",level.toString()))
    cards.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){row.forEach{(icon,label,value)->Column(Modifier.weight(1f).height(106.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha=.04f)).border(1.dp,Color.White.copy(alpha=.08f),RoundedCornerShape(16.dp)),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(icon,fontSize=19.sp);Spacer(Modifier.height(6.dp));Text(value,color=Color.White,fontSize=19.sp,fontWeight=FontWeight.Black);Text(label,color=Color.White.copy(alpha=.4f),fontSize=9.sp)}}};Spacer(Modifier.height(10.dp)) }
}

@Composable
private fun B44Achievements(coins:Int,xp:Int,level:Int,completed:Int){
    val items=listOf(Triple("🎯 İlk Adım","İlk seviyeyi tamamla",completed>=1),Triple("🏹 Kelime Avcısı","5 seviye tamamla",completed>=5),Triple("⚔️ Kelime Ustası","10 seviye tamamla",completed>=10),Triple("💰 Zengin","1000 altın biriktir",coins>=1000),Triple("⚡ XP Koleksiyoncusu","500 XP kazan",xp>=500),Triple("👑 Efsane","Seviye 10 ulaş",level>=10))
    items.forEach{(title,desc,done)->Row(Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(16.dp)).background(if(done)B44V2Colors.Emerald.copy(alpha=.08f)else Color.White.copy(alpha=.04f)).border(1.dp,if(done)B44V2Colors.Emerald.copy(alpha=.20f)else Color.White.copy(alpha=.08f),RoundedCornerShape(16.dp)).padding(horizontal=14.dp),verticalAlignment=Alignment.CenterVertically){Text(title.take(2),fontSize=22.sp,modifier=Modifier.alpha(if(done)1f else .3f));Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(title.drop(2).trim(),color=if(done)Color.White else Color.White.copy(alpha=.4f),fontSize=12.sp,fontWeight=FontWeight.Bold);Text(desc,color=Color.White.copy(alpha=.35f),fontSize=9.sp)};if(done)Box(Modifier.size(24.dp).background(B44V2Colors.Emerald,CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Filled.Check,null,tint=Color.White,modifier=Modifier.size(14.dp))}};Spacer(Modifier.height(9.dp))}
}

@Composable
private fun B44ProfileCollectionPreview(completed:Int,openFull:()->Unit){
    val preview=listOf(
        Triple("👑","Firavun Maskesi",completed>=1),Triple("📜","Papirüs Rulosu",completed>=2),Triple("🪲","Skarabeus",completed>=3),Triple("👸","Fransız Tacı",completed>=5),Triple("🖼️","Rölyef Plaka",completed>=7),Triple("⚔️","Katana",completed>=10)
    )
    preview.chunked(2).forEach { row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){row.forEach{(icon,name,done)->Column(Modifier.weight(1f).height(112.dp).clip(RoundedCornerShape(16.dp)).background(if(done)B44V2Colors.Gold.copy(alpha=.08f)else Color.White.copy(alpha=.03f)).border(1.dp,if(done)B44V2Colors.Gold.copy(alpha=.22f)else Color.White.copy(alpha=.06f),RoundedCornerShape(16.dp)),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(if(done)icon else "🔒",fontSize=25.sp,modifier=Modifier.alpha(if(done)1f else .25f));Spacer(Modifier.height(6.dp));Text(if(done)name else "???",color=if(done)Color.White.copy(alpha=.75f)else Color.White.copy(alpha=.18f),fontSize=9.sp,fontWeight=FontWeight.SemiBold,textAlign=TextAlign.Center)}}};Spacer(Modifier.height(10.dp))}
    Box(Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(14.dp)).background(B44V2Colors.Emerald.copy(alpha=.14f)).border(1.dp,B44V2Colors.Emerald.copy(alpha=.28f),RoundedCornerShape(14.dp)).clickable(onClick=openFull),contentAlignment=Alignment.Center){Text("Tüm Koleksiyonu Gör",color=B44V2Colors.Emerald,fontSize=11.sp,fontWeight=FontWeight.Bold)}
}

@Composable
private fun B44V3Collection(completed:Set<Int>,back:()->Unit,onTab:(B44V2Screen)->Unit){
    var filter by remember{mutableStateOf("all")}
    var selected by remember{mutableStateOf<B44Artifact?>(null)}
    val unlockedCount=(completed.size/2).coerceAtMost(b44Artifacts.size)
    val unlockedIds=b44Artifacts.take(unlockedCount).map{it.id}.toSet()
    val filters=listOf("all" to "Tümü","misir" to "Mısır","fransa" to "Fransa","sili" to "Şili","ispanya" to "İspanya","turkiye" to "Türkiye","japonya" to "Japonya")
    val filtered=if(filter=="all")b44Artifacts else b44Artifacts.filter{it.country==filter}
    B44V2Background {
        Box(Modifier.fillMaxSize()){
            Column(Modifier.fillMaxSize().statusBarsPadding().padding(bottom=92.dp)){
                Column(Modifier.padding(horizontal=18.dp,vertical=10.dp)){
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha=.07f)).border(1.dp,Color.White.copy(alpha=.1f),RoundedCornerShape(12.dp)).clickable(onClick=back),contentAlignment=Alignment.Center){Icon(Icons.Filled.ArrowBack,null,tint=Color.White.copy(alpha=.6f),modifier=Modifier.size(20.dp))};Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally){Text("Koleksiyonum",color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Black);Text("$unlockedCount/${b44Artifacts.size} eser",color=Color.White.copy(alpha=.4f),fontSize=9.sp)};Spacer(Modifier.width(40.dp))}
                    Spacer(Modifier.height(12.dp));Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Color.White.copy(alpha=.06f))){Box(Modifier.fillMaxWidth(unlockedCount.toFloat()/b44Artifacts.size).height(6.dp).background(Brush.horizontalGradient(listOf(B44V2Colors.Emerald,B44V2Colors.Gold))))};Spacer(Modifier.height(14.dp));Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(7.dp)){filters.forEach{(id,label)->val active=filter==id;Box(Modifier.clip(CircleShape).background(if(active)B44V2Colors.Emerald else Color.White.copy(alpha=.07f)).border(if(active)0.dp else 1.dp,if(active)Color.Transparent else Color.White.copy(alpha=.1f),CircleShape).clickable{filter=id}.padding(horizontal=11.dp,vertical=6.dp)){Text(label,color=if(active)Color.White else Color.White.copy(alpha=.5f),fontSize=9.sp,fontWeight=FontWeight.SemiBold)}}}
                }
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=14.dp).padding(bottom=20.dp)){
                    filtered.chunked(3).forEach{row->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){row.forEach{artifact->val collected=artifact.id in unlockedIds;B44ArtifactCard(artifact,collected,{if(collected)selected=artifact},Modifier.weight(1f))};repeat(3-row.size){Spacer(Modifier.weight(1f))}};Spacer(Modifier.height(8.dp))}
                    if(unlockedCount==0)Column(Modifier.fillMaxWidth().padding(top=48.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("🏺",fontSize=42.sp);Text("Henüz eser toplanmadı",color=Color.White.copy(alpha=.4f),fontSize=11.sp);Text("Seviyeleri tamamlayarak antik eserler kazan!",color=Color.White.copy(alpha=.25f),fontSize=9.sp)}
                }
            }
            B44V2BottomNav(B44V2Screen.PROFILE,Modifier.align(Alignment.BottomCenter),onTab)
            selected?.let{B44ArtifactDialog(it){selected=null}}
        }
    }
}

@Composable
private fun B44ArtifactCard(artifact:B44Artifact,collected:Boolean,click:()->Unit,modifier:Modifier){
    val color=rarityColor(artifact.rarity)
    Column(modifier.height(116.dp).clip(RoundedCornerShape(16.dp)).background(if(collected)color.copy(alpha=.09f)else Color.White.copy(alpha=.03f)).border(1.dp,if(collected)color.copy(alpha=.30f)else Color.White.copy(alpha=.06f),RoundedCornerShape(16.dp)).clickable(enabled=collected,onClick=click),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(if(collected)artifact.icon else "🔒",fontSize=25.sp,modifier=Modifier.alpha(if(collected)1f else .18f));Spacer(Modifier.height(5.dp));Text(if(collected)artifact.name else "???",color=if(collected)Color.White.copy(alpha=.75f)else Color.White.copy(alpha=.15f),fontSize=8.sp,fontWeight=FontWeight.SemiBold,textAlign=TextAlign.Center,lineHeight=10.sp,modifier=Modifier.padding(horizontal=3.dp));if(collected){Spacer(Modifier.height(4.dp));Text(rarityLabel(artifact.rarity),color=color,fontSize=7.sp,fontWeight=FontWeight.Bold)}}
}

@Composable
private fun B44ArtifactDialog(artifact:B44Artifact,close:()->Unit){
    val color=rarityColor(artifact.rarity)
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=.72f)).clickable(onClick=close),contentAlignment=Alignment.Center){Column(Modifier.fillMaxWidth(.82f).clip(RoundedCornerShape(26.dp)).background(Brush.verticalGradient(listOf(Color(0xFF122039),Color(0xFF070E1A)))).border(1.dp,color.copy(alpha=.45f),RoundedCornerShape(26.dp)).clickable(enabled=false){}.padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(artifact.icon,fontSize=54.sp);Spacer(Modifier.height(12.dp));Text(artifact.name,color=Color.White,fontSize=18.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center);Text(rarityLabel(artifact.rarity),color=color,fontSize=10.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(4.dp));Text(artifact.country.replaceFirstChar{it.uppercase()},color=Color.White.copy(alpha=.35f),fontSize=9.sp);Spacer(Modifier.height(20.dp));Box(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha=.07f)).border(1.dp,Color.White.copy(alpha=.1f),RoundedCornerShape(14.dp)).clickable(onClick=close),contentAlignment=Alignment.Center){Text("Kapat",color=Color.White,fontSize=11.sp,fontWeight=FontWeight.Bold)}}}
}

@Composable
private fun B44MetaToast(message:String,type:String,close:()->Unit){
    val (emoji,color)=when(type){"success"->"✅" to B44V2Colors.Emerald;"error"->"❌" to Color(0xFFF87171);"warning"->"⚠️" to B44V2Colors.Gold;else->"ℹ️" to B44V2Colors.Cyan}
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(color.copy(alpha=.15f)).border(1.dp,color.copy(alpha=.35f),RoundedCornerShape(16.dp)).padding(horizontal=14.dp,vertical=11.dp),verticalAlignment=Alignment.CenterVertically){Text(emoji,fontSize=15.sp);Spacer(Modifier.width(9.dp));Text(message,color=Color.White,fontSize=11.sp,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f));Box(Modifier.size(24.dp).clip(CircleShape).clickable(onClick=close),contentAlignment=Alignment.Center){Icon(Icons.Filled.Close,null,tint=Color.White.copy(alpha=.55f),modifier=Modifier.size(14.dp))}}
}
