package com.zafer.wordz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

private data class B44GameCell(val row:Int,val col:Int,val letter:Char)
private data class B44WordCells(val word:String,val cells:List<Pair<Int,Int>>)

private fun b44BuildGrid(level: Level): Pair<List<B44GameCell>,List<B44WordCells>> {
    val map=linkedMapOf<Pair<Int,Int>,Char>()
    val words=mutableListOf<B44WordCells>()
    level.placements.forEach { p ->
        val coords=p.word.indices.map { i -> if(p.horizontal) p.row to (p.col+i) else (p.row+i) to p.col }
        p.word.forEachIndexed { i,ch ->
            val key=coords[i]
            if(map[key]==null || map[key]==ch) map[key]=ch
        }
        words += B44WordCells(p.word,coords)
    }
    return map.map { (k,v)->B44GameCell(k.first,k.second,v) } to words
}

private fun b44CountryForLevel(levelNumber:Int):B44Country = b44Countries.firstOrNull { levelNumber in it.start..it.end } ?: b44Countries.last()

@Composable
internal fun B44V2GameScreen(
    levelIndex:Int,
    coins:Int,
    hints:Int,
    completedBefore:Boolean,
    onCoins:(Int)->Unit,
    onHints:(Int)->Unit,
    onRewardXp:(Int)->Unit,
    onComplete:()->Unit,
    back:()->Unit,
    next:()->Unit,
    home:()->Unit,
) {
    val level=levels[levelIndex]
    val gridData=remember(levelIndex){b44BuildGrid(level)}
    val cells=gridData.first
    val words=gridData.second
    val country=b44CountryForLevel(levelIndex+1)
    var found by remember(levelIndex){mutableStateOf<Set<String>>(emptySet())}
    var revealed by remember(levelIndex){mutableStateOf<Set<Pair<Int,Int>>>(emptySet())}
    var correctBanner by remember { mutableStateOf<String?>(null) }
    var settings by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf(false) }
    var rewardXp by remember { mutableIntStateOf(0) }
    var rewardCoins by remember { mutableIntStateOf(0) }
    var speedBonus by remember { mutableIntStateOf(0) }
    var finished by remember(levelIndex){mutableStateOf(false)}
    val startedAt=remember(levelIndex){System.currentTimeMillis()}
    val shake=remember{Animatable(0f)}

    fun finishIfNeeded(newFound:Set<String>) {
        if(finished || newFound.size < level.targets.size) return
        finished=true
        val elapsed=System.currentTimeMillis()-startedAt
        val bonus=if(elapsed<30_000)150 else if(elapsed<60_000)50 else 0
        val xp=if(completedBefore)0 else 100+bonus
        val coinReward=if(completedBefore)0 else 50+if(bonus>0)20 else 0
        speedBonus=if(completedBefore)0 else bonus
        rewardXp=xp
        rewardCoins=coinReward
        if(coinReward>0) onCoins(coins+coinReward)
        if(xp>0) onRewardXp(xp)
        onComplete()
        result=true
    }

    fun submit(raw:String) {
        if(raw.length<2 || finished) return
        val word=raw.uppercase(java.util.Locale("tr","TR"))
        if(word in level.targets && word !in found) {
            val target=words.firstOrNull{it.word==word}
            if(target!=null) revealed=revealed+target.cells
            val newFound=found+word
            found=newFound
            correctBanner=word
            finishIfNeeded(newFound)
        } else if(word in level.bonusWords) {
            correctBanner="BONUS · $word"
        } else {
            correctBanner=null
            kotlinx.coroutines.GlobalScope
            // shake is launched by state below; invalid word simply gives visual feedback through wheel reset
        }
    }

    LaunchedEffect(correctBanner) {
        if(correctBanner!=null){ delay(1500); correctBanner=null }
    }

    B44V2Background {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(bottom=26.dp),
                horizontalAlignment=Alignment.CenterHorizontally,
            ) {
                B44GameHeader(coins,hints){settings=true}
                Row(Modifier.padding(top=2.dp),verticalAlignment=Alignment.CenterVertically) {
                    Text(country.emoji,fontSize=18.sp);Spacer(Modifier.width(7.dp));Text(country.name,color=country.accent,fontSize=10.sp,fontWeight=FontWeight.Black,letterSpacing=1.sp);Spacer(Modifier.width(8.dp));Box(Modifier.clip(CircleShape).background(Color.White.copy(alpha=.06f)).border(1.dp,Color.White.copy(alpha=.08f),CircleShape).padding(horizontal=10.dp,vertical=4.dp)){Text("Bölüm ${levelIndex+1}",color=Color.White.copy(alpha=.55f),fontSize=9.sp)}
                }
                Spacer(Modifier.height(12.dp))
                Box(Modifier.offset(x=shake.value.dp)) { B44CrosswordGrid(cells,revealed,country.accent) }
                Spacer(Modifier.height(10.dp))
                AnimatedVisibility(correctBanner!=null,enter=fadeIn()+scaleIn(),exit=fadeOut()+scaleOut()) {
                    Box(Modifier.clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(country.accent.copy(alpha=.42f),country.accent.copy(alpha=.16f)))).border(1.dp,country.accent.copy(alpha=.55f),RoundedCornerShape(16.dp)).padding(horizontal=20.dp,vertical=8.dp)){Text(correctBanner.orEmpty(),color=Color.White,fontWeight=FontWeight.Black,fontSize=13.sp,letterSpacing=1.sp)}
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal=18.dp),horizontalArrangement=Arrangement.Center) {
                    level.targets.forEach { w ->
                        Box(Modifier.padding(horizontal=3.dp).clip(CircleShape).background(if(w in found) country.accent.copy(alpha=.18f) else Color.White.copy(alpha=.04f)).border(1.dp,if(w in found)country.accent.copy(alpha=.32f)else Color.White.copy(alpha=.07f),CircleShape).padding(horizontal=10.dp,vertical=5.dp)){Text(if(w in found)w else "•".repeat(w.length),color=if(w in found)Color.White else Color.White.copy(alpha=.25f),fontSize=9.sp,fontWeight=FontWeight.Bold)}
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal=18.dp),verticalAlignment=Alignment.Bottom,horizontalArrangement=Arrangement.SpaceBetween) {
                    B44ThemeButton(country.accent)
                    B44LetterWheel(level.letters,country.accent,::submit)
                    B44HintButton(hints,coins) {
                        val canUse = when { hints>0 -> {onHints(hints-1);true}; coins>=100 -> {onCoins(coins-100);true}; else -> false }
                        if(canUse){
                            val target=words.firstOrNull{it.word !in found}
                            val pos=target?.cells?.firstOrNull{it !in revealed}
                            if(pos!=null){
                                revealed=revealed+pos
                                val all=target.cells.all{it in revealed || it==pos}
                                if(all){val nf=found+target.word;found=nf;correctBanner=target.word;finishIfNeeded(nf)}
                            }
                        }
                    }
                }
            }
            if(settings) B44SettingsSheet(levelIndex+1,{settings=false},home)
            if(result) B44ResultOverlay(levelIndex+1,rewardXp,rewardCoins,speedBonus,completedBefore,next,home)
        }
    }
}

@Composable
private fun B44GameHeader(coins:Int,hints:Int,settings:()->Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically) {
        B44ResourceChip("◆",coins.toString(),B44V2Colors.Gold);Spacer(Modifier.width(8.dp));B44ResourceChip("💡",hints.toString(),B44V2Colors.Emerald);Spacer(Modifier.weight(1f));Box(Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha=.06f)).border(1.dp,Color.White.copy(alpha=.10f),RoundedCornerShape(14.dp)).clickable(onClick=settings),contentAlignment=Alignment.Center){Icon(Icons.Filled.Settings,null,tint=Color.White.copy(alpha=.60f),modifier=Modifier.size(19.dp))}
    }
}

@Composable
private fun B44ResourceChip(symbol:String,value:String,color:Color) {
    Row(Modifier.clip(RoundedCornerShape(16.dp)).background(color.copy(alpha=.10f)).border(1.dp,color.copy(alpha=.22f),RoundedCornerShape(16.dp)).padding(horizontal=7.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically) {Box(Modifier.size(25.dp).background(Brush.linearGradient(listOf(color,color.copy(alpha=.65f))),RoundedCornerShape(9.dp)),contentAlignment=Alignment.Center){Text(symbol,fontSize=10.sp,color=Color.White)};Spacer(Modifier.width(6.dp));Text(value,color=Color.White,fontWeight=FontWeight.Bold,fontSize=12.sp);Spacer(Modifier.width(4.dp))}
}

@Composable
private fun B44CrosswordGrid(cells:List<B44GameCell>,revealed:Set<Pair<Int,Int>>,accent:Color) {
    if(cells.isEmpty()) return
    val minRow=cells.minOf{it.row};val maxRow=cells.maxOf{it.row};val minCol=cells.minOf{it.col};val maxCol=cells.maxOf{it.col}
    val rows=maxRow-minRow+1;val cols=maxCol-minCol+1
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal=24.dp),contentAlignment=Alignment.Center) {
        val gap=6.dp
        val cellSize=minOf(56.dp,(maxWidth-gap*(cols-1))/cols)
        Column(verticalArrangement=Arrangement.spacedBy(gap),horizontalAlignment=Alignment.CenterHorizontally) {
            repeat(rows){rr->Row(horizontalArrangement=Arrangement.spacedBy(gap)){repeat(cols){cc->val row=rr+minRow;val col=cc+minCol;val cell=cells.firstOrNull{it.row==row&&it.col==col};if(cell==null)Spacer(Modifier.size(cellSize))else B44GridCell(cell,cell.row to cell.col in revealed,accent,cellSize)}}}
        }
    }
}

@Composable
private fun B44GridCell(cell:B44GameCell,revealed:Boolean,accent:Color,size:Dp) {
    val rotation by animateFloatAsState(if(revealed)0f else -90f,spring(stiffness=280f,dampingRatio=.78f),label="flip")
    Box(Modifier.size(size).graphicsLayer{rotationY=rotation;cameraDistance=12*density}.clip(RoundedCornerShape(15.dp)).background(if(revealed)Brush.linearGradient(listOf(accent.copy(alpha=.95f),accent.copy(alpha=.62f)))else Brush.linearGradient(listOf(Color.White.copy(alpha=.07f),Color.White.copy(alpha=.025f)))).border(1.dp,if(revealed)accent.copy(alpha=.75f)else Color.White.copy(alpha=.10f),RoundedCornerShape(15.dp)).shadow(if(revealed)5.dp else 0.dp,RoundedCornerShape(15.dp)),contentAlignment=Alignment.Center){if(revealed)Text(cell.letter.toString(),color=Color.White,fontWeight=FontWeight.Black,fontSize=(size.value*.38f).sp)}
}

@Composable
private fun B44ThemeButton(accent:Color) {
    Column(horizontalAlignment=Alignment.CenterHorizontally) {Box(Modifier.size(52.dp).clip(RoundedCornerShape(17.dp)).background(Color.White.copy(alpha=.055f)).border(1.dp,Color.White.copy(alpha=.09f),RoundedCornerShape(17.dp)),contentAlignment=Alignment.Center){Icon(Icons.Filled.Star,null,tint=accent,modifier=Modifier.size(21.dp))};Text("TEMA",color=Color.White.copy(alpha=.25f),fontSize=8.sp,modifier=Modifier.padding(top=4.dp))}
}

@Composable
private fun B44HintButton(hints:Int,coins:Int,click:()->Unit) {
    val free=hints>0
    Box(Modifier.size(62.dp)) {Box(Modifier.size(56.dp).align(Alignment.BottomStart).clip(RoundedCornerShape(18.dp)).background(if(free)Brush.linearGradient(listOf(Color(0xFF9B51E0),Color(0xFF6B21A8)))else Brush.linearGradient(listOf(Color(0xFF2D2D4E),Color(0xFF1E1E36)))).border(1.dp,if(free)Color(0xFF9B51E0).copy(alpha=.45f)else Color.White.copy(alpha=.07f),RoundedCornerShape(18.dp)).clickable(onClick=click),contentAlignment=Alignment.Center){Icon(Icons.Filled.Lightbulb,null,tint=if(free)Color(0xFFFFF176)else Color.White.copy(alpha=.3f),modifier=Modifier.size(22.dp))};Box(Modifier.align(Alignment.TopEnd).height(20.dp).clip(CircleShape).background(if(free)B44V2Colors.Gold else B44V2Colors.Red).border(1.dp,B44V2Colors.Deep,CircleShape).padding(horizontal=6.dp),contentAlignment=Alignment.Center){Text(if(free)hints.toString() else if(coins>=100)"◆" else "✗",color=Color.White,fontSize=8.sp,fontWeight=FontWeight.Black)}}
}

@Composable
private fun B44LetterWheel(letters:List<Char>,accent:Color,onSubmit:(String)->Unit) {
    var shuffled by remember(letters){mutableStateOf(letters)}
    var selected by remember(letters){mutableStateOf<List<Int>>(emptyList())}
    var dragging by remember{mutableStateOf(false)}
    var pointer by remember{mutableStateOf<Offset?>(null)}
    var wheelPx by remember{mutableStateOf(0 to 0)}
    val density=LocalDensity.current
    val size=260.dp;val radius=90.dp
    val current=selected.joinToString(""){shuffled[it].toString()}
    fun centersPx():List<Offset>{
        val centerX=wheelPx.first/2f;val centerY=wheelPx.second/2f;val r=with(density){radius.toPx()}
        return shuffled.indices.map{i->val angle=(i.toDouble()/shuffled.size)*2.0*PI-PI/2;Offset(centerX+(r*cos(angle)).toFloat(),centerY+(r*sin(angle)).toFloat())}
    }
    fun findIndex(p:Offset):Int{val hit=with(density){30.dp.toPx()};return centersPx().indexOfFirst{hypot((p.x-it.x).toDouble(),(p.y-it.y).toDouble())<hit}}

    Column(horizontalAlignment=Alignment.CenterHorizontally) {
        Box(Modifier.height(42.dp),contentAlignment=Alignment.Center){if(current.isBlank())Text("harfleri sürükle...",color=Color.White.copy(alpha=.25f),fontSize=11.sp,letterSpacing=1.sp)else Box(Modifier.clip(RoundedCornerShape(15.dp)).background(accent.copy(alpha=.28f)).border(1.dp,accent.copy(alpha=.4f),RoundedCornerShape(15.dp)).padding(horizontal=18.dp,vertical=7.dp)){Text(current,color=Color.White,fontWeight=FontWeight.Black,fontSize=16.sp,letterSpacing=2.sp)}}
        Box(Modifier.size(size).onSizeChanged{wheelPx=it.width to it.height}.pointerInput(shuffled){detectDragGestures(onDragStart={p->val idx=findIndex(p);if(idx>=0){dragging=true;selected=listOf(idx);pointer=p}},onDrag={change,_->if(dragging){pointer=change.position;val idx=findIndex(change.position);if(idx>=0&&idx !in selected)selected=selected+idx;change.consume()}},onDragEnd={if(selected.size>=2)onSubmit(selected.joinToString(""){shuffled[it].toString()});dragging=false;selected=emptyList();pointer=null},onDragCancel={dragging=false;selected=emptyList();pointer=null})}) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(color=accent.copy(alpha=.06f),radius=size.minDimension/2f);drawCircle(color=Color.White.copy(alpha=.07f),radius=size.minDimension/2f,style=androidx.compose.ui.graphics.drawscope.Stroke(width=1.dp.toPx()))
                val centers=centersPx()
                if(selected.isNotEmpty()){
                    val pts=selected.map{centers[it]} + listOfNotNull(pointer.takeIf{dragging})
                    pts.zipWithNext().forEach{(a,b)->drawLine(color=accent.copy(alpha=.9f),start=a,end=b,strokeWidth=4.dp.toPx(),cap=StrokeCap.Round)}
                    selected.forEach{drawCircle(color=accent,radius=6.dp.toPx(),center=centers[it])}
                }
            }
            Box(Modifier.size(46.dp).align(Alignment.Center).clip(RoundedCornerShape(15.dp)).background(Brush.linearGradient(listOf(accent,accent.copy(alpha=.72f)))).border(1.dp,accent.copy(alpha=.75f),RoundedCornerShape(15.dp)).clickable{val a=shuffled.toMutableList();for(i in a.lastIndex downTo 1){val j=Random.nextInt(i+1);val t=a[i];a[i]=a[j];a[j]=t};shuffled=a;selected=emptyList()},contentAlignment=Alignment.Center){Icon(Icons.Filled.Shuffle,null,tint=Color.White,modifier=Modifier.size(21.dp))}
            shuffled.forEachIndexed{i,ch->val angle=(i.toDouble()/shuffled.size)*2.0*PI-PI/2;val x=(130+90*cos(angle)-26).dp;val y=(130+90*sin(angle)-26).dp;val sel=i in selected;val sc by animateFloatAsState(if(sel)1.22f else 1f,spring(stiffness=500f,dampingRatio=.72f),label="letter$i");Box(Modifier.offset(x,y).size(52.dp).scale(sc).clip(RoundedCornerShape(16.dp)).background(if(sel)Brush.linearGradient(listOf(accent,accent.copy(alpha=.78f)))else Brush.linearGradient(listOf(Color.Transparent,Color.Transparent))),contentAlignment=Alignment.Center){Text(ch.toString(),color=if(sel)Color.White else Color(0xFF1E0B3C),fontWeight=FontWeight.Black,fontSize=19.sp);if(sel){Box(Modifier.size(16.dp).align(Alignment.TopEnd).background(B44V2Colors.Gold,CircleShape),contentAlignment=Alignment.Center){Text((selected.indexOf(i)+1).toString(),color=Color(0xFF1A0800),fontSize=7.sp,fontWeight=FontWeight.Black)}}}}
        }
    }
}

@Composable
private fun B44SettingsSheet(level:Int,close:()->Unit,home:()->Unit) {
    var sound by remember{mutableStateOf(true)};var music by remember{mutableStateOf(true)}
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=.68f)).clickable(onClick=close),contentAlignment=Alignment.BottomCenter){Column(Modifier.fillMaxWidth().padding(14.dp).clip(RoundedCornerShape(30.dp)).background(Brush.verticalGradient(listOf(Color(0xFF160832),Color(0xFF0A051A)))).border(1.dp,Color.White.copy(alpha=.08f),RoundedCornerShape(30.dp)).clickable(enabled=false){}.padding(20.dp)){Box(Modifier.width(48.dp).height(4.dp).background(Color.White.copy(alpha=.12f),CircleShape).align(Alignment.CenterHorizontally));Spacer(Modifier.height(16.dp));Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Ayarlar",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Black);Text("Bölüm $level",color=Color(0xFF9B51E0).copy(alpha=.75f),fontSize=10.sp)};Text("✕",color=Color.White.copy(alpha=.5f),fontSize=18.sp,modifier=Modifier.clickable(onClick=close).padding(8.dp))};Spacer(Modifier.height(14.dp));B44ToggleRow(Icons.Filled.VolumeUp,"Ses Efektleri",sound){sound=it};Spacer(Modifier.height(9.dp));B44ToggleRow(Icons.Filled.MusicNote,"Müzik",music){music=it};Spacer(Modifier.height(14.dp));Row(Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(16.dp)).background(B44V2Colors.Red.copy(alpha=.10f)).border(1.dp,B44V2Colors.Red.copy(alpha=.20f),RoundedCornerShape(16.dp)).clickable(onClick=home),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center){Icon(Icons.Filled.Home,null,tint=Color(0xFFFCA5A5),modifier=Modifier.size(18.dp));Spacer(Modifier.width(8.dp));Text("Ana Menüye Dön",color=Color(0xFFFCA5A5),fontWeight=FontWeight.SemiBold,fontSize=12.sp)};Spacer(Modifier.height(12.dp));Text("v0.19.0 · Words Z",color=Color.White.copy(alpha=.15f),fontSize=9.sp,modifier=Modifier.align(Alignment.CenterHorizontally))}}
}

@Composable
private fun B44ToggleRow(icon:androidx.compose.ui.graphics.vector.ImageVector,label:String,value:Boolean,onChange:(Boolean)->Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha=.04f)).border(1.dp,Color.White.copy(alpha=.07f),RoundedCornerShape(16.dp)).padding(12.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(34.dp).background(Color(0xFF7B3FC4).copy(alpha=.15f),RoundedCornerShape(11.dp)),contentAlignment=Alignment.Center){Icon(icon,null,tint=Color(0xFF9B51E0),modifier=Modifier.size(17.dp))};Spacer(Modifier.width(10.dp));Text(label,color=Color.White.copy(alpha=.65f),fontSize=12.sp,modifier=Modifier.weight(1f));Box(Modifier.width(48.dp).height(26.dp).clip(CircleShape).background(if(value)Brush.horizontalGradient(listOf(Color(0xFF9B51E0),Color(0xFF6B21A8)))else Brush.horizontalGradient(listOf(Color.White.copy(alpha=.08f),Color.White.copy(alpha=.08f)))).clickable{onChange(!value)}){Box(Modifier.offset(x=if(value)23.dp else 3.dp,y=3.dp).size(20.dp).background(Color.White,CircleShape))}}
}

@Composable
private fun B44ResultOverlay(level:Int,xp:Int,coins:Int,speed:Int,replay:Boolean,next:()->Unit,home:()->Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xFF080313).copy(alpha=.96f)),contentAlignment=Alignment.Center){Column(Modifier.fillMaxWidth().padding(horizontal=28.dp),horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.size(94.dp).shadow(24.dp,CircleShape,ambientColor=B44V2Colors.Gold.copy(alpha=.5f),spotColor=B44V2Colors.Gold.copy(alpha=.5f)).background(Brush.linearGradient(listOf(B44V2Colors.Gold,Color(0xFFC8911F))),CircleShape),contentAlignment=Alignment.Center){Text("🏆",fontSize=48.sp)};Spacer(Modifier.height(18.dp));Text("Harika!",color=Color.White,fontSize=32.sp,fontWeight=FontWeight.Black);Text("Bölüm $level tamamlandı",color=Color.White.copy(alpha=.42f),fontSize=12.sp);Spacer(Modifier.height(12.dp));Row{repeat(3){Text("★",color=B44V2Colors.Gold,fontSize=27.sp,modifier=Modifier.padding(horizontal=3.dp))}};Spacer(Modifier.height(20.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){B44RewardCard("⚡","+$xp XP",Modifier.weight(1f));B44RewardCard("◆","+$coins Altın",Modifier.weight(1f))};if(speed>0){Spacer(Modifier.height(10.dp));B44RewardCard("🔥","Hız Bonusu +$speed XP",Modifier.fillMaxWidth())};if(replay){Spacer(Modifier.height(10.dp));Text("Tekrar oynama · yeni ödül yok",color=Color.White.copy(alpha=.3f),fontSize=10.sp)};Spacer(Modifier.height(24.dp));Box(Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(18.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF9B51E0),Color(0xFF6B21A8)))).clickable(onClick=next),contentAlignment=Alignment.Center){Text("Bölüm ${level+1}  →",color=Color.White,fontWeight=FontWeight.Black,fontSize=14.sp)};Spacer(Modifier.height(12.dp));Text("Ana Menü",color=Color.White.copy(alpha=.35f),fontSize=11.sp,modifier=Modifier.clickable(onClick=home).padding(8.dp))}}
}

@Composable
private fun B44RewardCard(icon:String,text:String,modifier:Modifier) {Row(modifier.height(58.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha=.05f)).border(1.dp,Color.White.copy(alpha=.08f),RoundedCornerShape(16.dp)),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center){Text(icon,fontSize=17.sp);Spacer(Modifier.width(7.dp));Text(text,color=B44V2Colors.Gold,fontWeight=FontWeight.Black,fontSize=12.sp)}}
