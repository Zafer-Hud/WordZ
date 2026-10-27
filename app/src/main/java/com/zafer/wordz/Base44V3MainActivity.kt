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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
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

class Base44V3MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        validateAllLevels()
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary=B44V2Colors.Emerald,
                    secondary=B44V2Colors.Gold,
                    background=B44V2Colors.Deep,
                    surface=B44V2Colors.Mid,
                    onPrimary=Color.White,
                    onBackground=Color.White,
                    onSurface=Color.White,
                )
            ){ B44V3App() }
        }
    }
}

@Composable
private fun B44V3App(){
    val context=androidx.compose.ui.platform.LocalContext.current
    val prefs=remember{context.getSharedPreferences(B44V2_PREFS,Context.MODE_PRIVATE)}
    val playerStore=remember{PlayerProgressStore(prefs)}
    var showSplash by remember{mutableStateOf(true)}
    var screen by remember{mutableStateOf(B44V2Screen.HOME)}
    var selectedLevel by remember{mutableIntStateOf(prefs.getInt(B44V2_UNLOCKED,0).coerceIn(0,levels.lastIndex))}
    var coins by remember{mutableIntStateOf(if(prefs.contains(B44V2_COINS))prefs.getInt(B44V2_COINS,500)else 500)}
    var hints by remember{mutableIntStateOf(prefs.getInt(B44V2_HINTS,3).coerceAtLeast(0))}
    var unlocked by remember{mutableIntStateOf(prefs.getInt(B44V2_UNLOCKED,0).coerceIn(0,levels.lastIndex))}
    var completed by remember{mutableStateOf(prefs.getStringSet(B44V2_COMPLETED,emptySet()).orEmpty().mapNotNull{it.toIntOrNull()}.filter{it in levels.indices}.toSet())}
    var player by remember{mutableStateOf(playerStore.snapshot())}

    fun openLevel(index:Int){selectedLevel=index.coerceIn(0,levels.lastIndex);screen=B44V2Screen.GAME}
    fun setCoinsPersist(value:Int){val v=value.coerceAtLeast(0);coins=v;prefs.edit().putInt(B44V2_COINS,v).apply()}
    fun setHintsPersist(value:Int){val v=value.coerceAtLeast(0);hints=v;prefs.edit().putInt(B44V2_HINTS,v).apply()}

    Box(Modifier.fillMaxSize()){
        when(screen){
            B44V2Screen.HOME->B44V3Home(coins,unlocked+1,player,{openLevel(unlocked)}){screen=it}
            B44V2Screen.MAP->B44V3Map(unlocked,completed,{screen=B44V2Screen.HOME},::openLevel){screen=it}
            B44V2Screen.GAME->B44V2GameScreen(
                levelIndex=selectedLevel,
                coins=coins,
                hints=hints,
                completedBefore=selectedLevel in completed,
                onCoins=::setCoinsPersist,
                onHints=::setHintsPersist,
                onRewardXp={amount->player=playerStore.grant(amount).after},
                onComplete={
                    val nextCompleted=completed+selectedLevel
                    val nextUnlocked=if(selectedLevel>=unlocked&&unlocked<levels.lastIndex)selectedLevel+1 else unlocked
                    completed=nextCompleted;unlocked=nextUnlocked
                    prefs.edit().putInt(B44V2_UNLOCKED,nextUnlocked).putStringSet(B44V2_COMPLETED,nextCompleted.map(Int::toString).toSet()).apply()
                },
                back={screen=B44V2Screen.MAP},
                next={if(selectedLevel<levels.lastIndex)openLevel(selectedLevel+1)else screen=B44V2Screen.MAP},
                home={screen=B44V2Screen.HOME},
            )
            B44V2Screen.MARKET->B44V3Market(coins,hints,::setCoinsPersist,::setHintsPersist){screen=it}
            B44V2Screen.PROFILE->B44V3Profile(coins,hints,player,completed){screen=it}
        }
        AnimatedVisibility(showSplash,enter=fadeIn(),exit=fadeOut(tween(700))){B44V3Splash{showSplash=false}}
    }
}

@Composable
private fun B44V3Splash(done:()->Unit){
    var progress by remember{mutableIntStateOf(0)}
    LaunchedEffect(Unit){repeat(50){delay(38);progress=(it+1)*2};delay(400);done()}
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF040C18),Color(0xFF071420),Color(0xFF030A14)))),contentAlignment=Alignment.Center){
        B44V3Stars(144,25)
        Column(horizontalAlignment=Alignment.CenterHorizontally){
            Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent,B44V2Colors.Emerald))));Spacer(Modifier.width(12.dp));Box(Modifier.size(8.dp).background(B44V2Colors.Emerald,CircleShape));Spacer(Modifier.width(12.dp));Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(B44V2Colors.Emerald,Color.Transparent))))}
            Spacer(Modifier.height(22.dp));Text("WORDS",color=Color.White,fontSize=58.sp,lineHeight=58.sp,fontWeight=FontWeight.Black);Text("Z",color=B44V2Colors.Emerald,fontSize=102.sp,lineHeight=88.sp,fontWeight=FontWeight.Black);Spacer(Modifier.height(12.dp));Text("KELİME BULMACA",color=Color.White.copy(alpha=.3f),fontSize=10.sp,letterSpacing=3.sp);Spacer(Modifier.height(58.dp));Box(Modifier.width(208.dp).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha=.07f))){Box(Modifier.fillMaxWidth((progress/100f).coerceIn(0f,1f)).height(4.dp).background(Brush.horizontalGradient(listOf(B44V2Colors.Emerald,B44V2Colors.Gold))))};Spacer(Modifier.height(12.dp));Text("YÜKLENİYOR",color=Color.White.copy(alpha=.25f),fontSize=10.sp,letterSpacing=3.sp)
        }
    }
}

@Composable
private fun B44V3Stars(seed:Int,count:Int){
    val stars=remember(seed,count){val r=Random(seed);List(count){Triple(r.nextFloat(),r.nextFloat()*.86f,.6f+r.nextFloat()*1.4f)}}
    BoxWithConstraints(Modifier.fillMaxSize()){stars.forEachIndexed{i,s->val t=rememberInfiniteTransition(label="v3s$i");val a by t.animateFloat(.08f,.65f,infiniteRepeatable(tween(2200+i*83),RepeatMode.Reverse),label="v3a$i");Box(Modifier.offset(maxWidth*s.first,maxHeight*s.second).size(s.third.dp).alpha(a).background(Color.White,CircleShape))}}
}

@Composable
private fun B44V3Home(coins:Int,currentLevel:Int,player:PlayerProgress,onPlay:()->Unit,onTab:(B44V2Screen)->Unit){
    val level=player.totalXp/500+1
    B44V2Background{
        Box(Modifier.fillMaxSize()){
            Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal=20.dp).padding(bottom=104.dp),horizontalAlignment=Alignment.CenterHorizontally){
                Row(Modifier.fillMaxWidth().padding(top=10.dp),horizontalArrangement=Arrangement.SpaceBetween){B44V3Chip("◆",coins.toString(),B44V2Colors.Gold);B44V3Chip("⚡","Lv.$level   ${player.totalXp} XP",B44V2Colors.Emerald)}
                Spacer(Modifier.height(26.dp));Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(Color.Transparent,B44V2Colors.Emerald.copy(alpha=.5f)))));Spacer(Modifier.width(10.dp));Text("KELİME OYUNU",color=B44V2Colors.Emerald.copy(alpha=.65f),fontSize=10.sp,letterSpacing=3.sp);Spacer(Modifier.width(10.dp));Box(Modifier.width(48.dp).height(1.dp).background(Brush.horizontalGradient(listOf(B44V2Colors.Emerald.copy(alpha=.5f),Color.Transparent))))}
                Spacer(Modifier.height(14.dp));Text("WORDS",color=Color.White,fontSize=50.sp,lineHeight=50.sp,fontWeight=FontWeight.Black);Text("Z",color=B44V2Colors.Emerald,fontSize=84.sp,lineHeight=72.sp,fontWeight=FontWeight.Black);Spacer(Modifier.height(22.dp));B44V3Play(onPlay);Spacer(Modifier.height(22.dp));Row(Modifier.clip(CircleShape).background(Color.White.copy(alpha=.045f)).border(1.dp,Color.White.copy(alpha=.09f),CircleShape).padding(horizontal=18.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(8.dp).background(B44V2Colors.Emerald,CircleShape));Spacer(Modifier.width(8.dp));Text("Bölüm ",color=Color.White.copy(alpha=.55f),fontSize=13.sp);Text(currentLevel.toString(),color=Color.White,fontWeight=FontWeight.Bold,fontSize=13.sp);Text(" aktif",color=Color.White.copy(alpha=.55f),fontSize=13.sp)}
                Spacer(Modifier.height(28.dp));Text("OYUN MODU",color=Color.White.copy(alpha=.3f),fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp,modifier=Modifier.fillMaxWidth());Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){B44V3Mode("📖","Klasik","Kelimeleri bul, dünyayı keşfet",B44V2Colors.Emerald,false,onPlay,Modifier.weight(1f));B44V3Mode("⚡","Hız Modu","Süre yarışı — ne kadar hızlısın?",B44V2Colors.Orange,true,{},Modifier.weight(1f));B44V3Mode("🏆","Günlük Görev","Her gün yeni meydan okuma",B44V2Colors.Gold,true,{},Modifier.weight(1f))};Spacer(Modifier.height(24.dp));B44V3Leaderboard()
            }
            B44V2BottomNav(B44V2Screen.HOME,Modifier.align(Alignment.BottomCenter),onTab)
        }
    }
}

@Composable private fun B44V3Chip(symbol:String,text:String,tint:Color){Row(Modifier.clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha=.05f)).border(1.dp,Color.White.copy(alpha=.09f),RoundedCornerShape(16.dp)).padding(horizontal=12.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(20.dp).background(Brush.linearGradient(listOf(tint,if(tint==B44V2Colors.Gold)B44V2Colors.GoldDark else B44V2Colors.EmeraldDark)),CircleShape),contentAlignment=Alignment.Center){Text(symbol,color=Color.White,fontSize=9.sp,fontWeight=FontWeight.Black)};Spacer(Modifier.width(7.dp));Text(text,color=Color.White,fontSize=12.sp,fontWeight=FontWeight.Bold)}}

@Composable private fun B44V3Play(click:()->Unit){val t=rememberInfiniteTransition(label="v3play");val p by t.animateFloat(1f,1.22f,infiniteRepeatable(tween(2600),RepeatMode.Reverse),label="p");Box(Modifier.size(190.dp),contentAlignment=Alignment.Center){Box(Modifier.size(160.dp).scale(p).alpha(.2f).background(Brush.radialGradient(listOf(B44V2Colors.Emerald.copy(alpha=.4f),Color.Transparent)),CircleShape));Column(Modifier.size(144.dp).shadow(28.dp,CircleShape,ambientColor=B44V2Colors.Emerald.copy(alpha=.4f),spotColor=B44V2Colors.Emerald.copy(alpha=.4f)).clip(CircleShape).background(Brush.linearGradient(listOf(Color(0xFF1AE896),B44V2Colors.Emerald,B44V2Colors.EmeraldDark))).border(1.dp,Color.White.copy(alpha=.15f),CircleShape).clickable(onClick=click),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Icon(Icons.Filled.PlayArrow,null,tint=Color.White,modifier=Modifier.size(42.dp));Text("OYNA",color=Color.White,fontSize=10.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)}}}

@Composable private fun B44V3Mode(emoji:String,title:String,desc:String,color:Color,locked:Boolean,click:()->Unit,modifier:Modifier){Box(modifier.height(126.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(color.copy(alpha=.18f),color.copy(alpha=.05f)))).border(1.dp,color.copy(alpha=.3f),RoundedCornerShape(16.dp)).alpha(if(locked).6f else 1f).clickable(enabled=!locked,onClick=click).padding(9.dp)){if(locked)Icon(Icons.Filled.Lock,null,tint=Color.White.copy(alpha=.35f),modifier=Modifier.size(13.dp).align(Alignment.TopEnd));Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(emoji,fontSize=22.sp);Text(title,color=color,fontSize=11.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center);Spacer(Modifier.height(4.dp));Text(desc,color=Color.White.copy(alpha=.4f),fontSize=9.sp,textAlign=TextAlign.Center,lineHeight=11.sp)}}}

@Composable private fun B44V3Leaderboard(){val names=listOf("KelimeUstası","ZekaAvcısı","MaviKedi","Gezgin","BulmacaTR");Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha=.04f)).border(1.dp,Color.White.copy(alpha=.08f),RoundedCornerShape(18.dp))){Row(Modifier.fillMaxWidth().padding(14.dp)){Text("🏆",fontSize=16.sp);Spacer(Modifier.width(8.dp));Text("Liderlik Tablosu",color=Color.White,fontWeight=FontWeight.Bold,fontSize=14.sp)};names.forEachIndexed{i,n->Row(Modifier.fillMaxWidth().background(if(i==0)B44V2Colors.Gold.copy(alpha=.05f)else Color.Transparent).padding(horizontal=14.dp,vertical=9.dp),verticalAlignment=Alignment.CenterVertically){Text(if(i<3)listOf("🥇","🥈","🥉")[i]else"${i+1}.",fontSize=13.sp,modifier=Modifier.width(28.dp));Box(Modifier.size(28.dp).background(Color.hsv((i*60f)%360,.6f,.45f),CircleShape),contentAlignment=Alignment.Center){Text(n.take(1),color=Color.White,fontWeight=FontWeight.Bold,fontSize=11.sp)};Spacer(Modifier.width(9.dp));Column(Modifier.weight(1f)){Text(n,color=Color.White,fontSize=12.sp,fontWeight=FontWeight.SemiBold);Text("Lv.${i+2} · ${i+3} seviye",color=Color.White.copy(alpha=.35f),fontSize=9.sp)};Text("${(5-i)*1240}",color=B44V2Colors.Gold,fontSize=11.sp,fontWeight=FontWeight.Bold)}}}}

private fun b44V3CountryOpen(country:B44Country,completed:Set<Int>):Boolean{val i=b44Countries.indexOfFirst{it.id==country.id};if(i<=0)return true;val p=b44Countries[i-1];val done=(p.start-1..p.end-1).count{it in completed};return done>=((p.end-p.start+1)/2)}

@Composable
private fun B44V3Map(unlocked:Int,completed:Set<Int>,back:()->Unit,onOpenLevel:(Int)->Unit,onTab:(B44V2Screen)->Unit){
    var selected by remember{mutableStateOf<B44Country?>(null)}
    B44V2Background{
        Box(Modifier.fillMaxSize()){
            Column(Modifier.fillMaxSize().statusBarsPadding().padding(bottom=92.dp)){
                Row(Modifier.fillMaxWidth().padding(horizontal=18.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha=.07f)).border(1.dp,Color.White.copy(alpha=.1f),RoundedCornerShape(12.dp)).clickable(onClick=back),contentAlignment=Alignment.Center){Icon(Icons.Filled.ArrowBack,null,tint=Color.White.copy(alpha=.65f),modifier=Modifier.size(20.dp))};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text("Dünya Haritası",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Black);Text("Kelimelerle dünyayı keşfet",color=Color.White.copy(alpha=.38f),fontSize=10.sp)};Text("${b44Countries.count{b44V3CountryOpen(it,completed)}}/${b44Countries.size}",color=B44V2Colors.Emerald,fontWeight=FontWeight.Bold,fontSize=12.sp)}
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=18.dp).padding(bottom=20.dp)){b44Countries.forEachIndexed{i,c->val open=b44V3CountryOpen(c,completed);val done=(c.start-1..c.end-1).count{it in completed};val total=c.end-c.start+1;val active=(unlocked+1) in c.start..c.end;B44V3CountryCard(i,c,done,total,open,active){if(open)selected=c};if(i<b44Countries.lastIndex)Box(Modifier.padding(start=25.dp).width(2.dp).height(22.dp).background(Color.White.copy(alpha=.08f)))}}
            }
            B44V2BottomNav(B44V2Screen.MAP,Modifier.align(Alignment.BottomCenter),onTab)
            selected?.let{c->B44V3CountrySheet(c,completed,unlocked,{selected=null}){val first=(c.start-1).coerceAtMost(levels.lastIndex);val candidate=if((unlocked+1) in c.start..c.end)unlocked else first;if(candidate in levels.indices)onOpenLevel(candidate);selected=null}}
        }
    }
}

@Composable private fun B44V3CountryCard(index:Int,c:B44Country,done:Int,total:Int,open:Boolean,active:Boolean,click:()->Unit){val progress=(done.toFloat()/total).coerceIn(0f,1f);Row(Modifier.fillMaxWidth().height(116.dp).clip(RoundedCornerShape(20.dp)).background(if(active)c.accent.copy(alpha=.1f)else Color.White.copy(alpha=.035f)).border(1.dp,if(active)c.accent.copy(alpha=.35f)else Color.White.copy(alpha=.08f),RoundedCornerShape(20.dp)).alpha(if(open)1f else .48f).clickable(enabled=open,onClick=click).padding(14.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(c.accent.copy(alpha=.18f)).border(1.dp,c.accent.copy(alpha=.35f),RoundedCornerShape(16.dp)),contentAlignment=Alignment.Center){if(open)Text(c.emoji,fontSize=26.sp)else Icon(Icons.Filled.Lock,null,tint=Color.White.copy(alpha=.45f))};Spacer(Modifier.width(13.dp));Column(Modifier.weight(1f)){Row(verticalAlignment=Alignment.CenterVertically){Text(c.name,color=if(open)Color.White else Color.White.copy(alpha=.45f),fontSize=15.sp,fontWeight=FontWeight.Black);if(active){Spacer(Modifier.width(8.dp));Text("● Aktif",color=c.accent,fontSize=9.sp,fontWeight=FontWeight.Bold)}};Text("${c.start}–${c.end} · ${c.subtitle}",color=Color.White.copy(alpha=.38f),fontSize=9.sp);Spacer(Modifier.height(10.dp));Box(Modifier.fillMaxWidth().height(5.dp).clip(CircleShape).background(Color.White.copy(alpha=.07f))){Box(Modifier.fillMaxWidth(progress).height(5.dp).background(c.accent))};Spacer(Modifier.height(5.dp));Row{Text(if(open)"$done/$total"else"Önceki bölgeyi %50 tamamla",color=Color.White.copy(alpha=.35f),fontSize=9.sp,modifier=Modifier.weight(1f));if(done==total)Text("✓ Tamam",color=B44V2Colors.Emerald,fontSize=9.sp)}};Box(Modifier.size(34.dp).background(if(done==total)B44V2Colors.Emerald.copy(alpha=.18f)else Color.White.copy(alpha=.05f),CircleShape),contentAlignment=Alignment.Center){if(done==total)Icon(Icons.Filled.Check,null,tint=B44V2Colors.Emerald,modifier=Modifier.size(17.dp))else Text("${index+1}",color=Color.White.copy(alpha=.4f),fontSize=11.sp)}}}

@Composable private fun B44V3CountrySheet(c:B44Country,completed:Set<Int>,unlocked:Int,close:()->Unit,start:()->Unit){Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=.68f)).clickable(onClick=close),contentAlignment=Alignment.BottomCenter){Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart=34.dp,topEnd=34.dp)).background(Brush.verticalGradient(listOf(Color(0xFF122039),Color(0xFF070E1A)))).border(1.dp,Color.White.copy(alpha=.08f),RoundedCornerShape(topStart=34.dp,topEnd=34.dp)).clickable(enabled=false){}.padding(22.dp).navigationBarsPadding(),horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.width(48.dp).height(4.dp).background(Color.White.copy(alpha=.12f),CircleShape));Spacer(Modifier.height(18.dp));Text(c.emoji,fontSize=46.sp);Text(c.name,color=Color.White,fontSize=24.sp,fontWeight=FontWeight.Black);Text(c.subtitle,color=Color.White.copy(alpha=.42f),fontSize=11.sp);Spacer(Modifier.height(18.dp));val done=(c.start-1..c.end-1).count{it in completed};val total=c.end-c.start+1;Box(Modifier.fillMaxWidth().height(7.dp).clip(CircleShape).background(Color.White.copy(alpha=.08f))){Box(Modifier.fillMaxWidth(done.toFloat()/total).height(7.dp).background(c.accent))};Spacer(Modifier.height(8.dp));Row(Modifier.fillMaxWidth()){Text("$done/$total tamamlandı",color=Color.White.copy(alpha=.4f),fontSize=10.sp,modifier=Modifier.weight(1f));Row{repeat(5){Icon(Icons.Filled.Star,null,tint=if(it<((done*5)/total))B44V2Colors.Gold else Color.White.copy(alpha=.12f),modifier=Modifier.size(14.dp))}}};Spacer(Modifier.height(20.dp));Box(Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(17.dp)).background(Brush.horizontalGradient(listOf(c.accent,c.accent.copy(alpha=.72f)))).clickable(onClick=start),contentAlignment=Alignment.Center){Text(if(done==0)"Başla"else if((unlocked+1) in c.start..c.end)"Devam Et"else"Tekrar Oyna",color=Color.White,fontSize=14.sp,fontWeight=FontWeight.Black)}}}}
