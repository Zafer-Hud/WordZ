package com.zafer.wordz

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun B44V2GameScreen(
    levelIndex: Int,
    coins: Int,
    hints: Int,
    completedBefore: Boolean,
    onCoins: (Int) -> Unit,
    onHints: (Int) -> Unit,
    onRewardXp: (Int) -> Unit,
    onComplete: () -> Unit,
    back: () -> Unit,
    next: () -> Unit,
    home: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(B44V2_PREFS, Context.MODE_PRIVATE) }
    val progressStore = remember { Base44ProgressStore(prefs) }
    val startedAt = remember(levelIndex) { System.currentTimeMillis() }
    val wasCompletedBefore = remember(levelIndex) { completedBefore }

    B44V4GameScreen(
        levelIndex = levelIndex,
        coins = coins,
        hints = hints,
        completedBefore = wasCompletedBefore,
        onCoins = onCoins,
        onHints = onHints,
        onRewardXp = onRewardXp,
        onComplete = {
            val reward = Base44ProgressStore.rewardFor(
                elapsedMs = System.currentTimeMillis() - startedAt,
                alreadyCompleted = wasCompletedBefore,
            )
            if (reward.score > 0) {
                progressStore.addScore(reward.score)
                prefs.edit()
                    .putInt("b44_last_score_gain", reward.score)
                    .putInt("b44_last_speed_bonus", reward.speedBonus)
                    .apply()
            }

            // B44V4 currently grants the base 50 coins itself. Top up the
            // additional 20-coin speed reward here so the persisted economy
            // exactly matches gameContext.jsx.
            if (!wasCompletedBefore && reward.speedBonus > 0) {
                onCoins(coins + reward.coins)
            }
            onComplete()
        },
        back = back,
        next = next,
        home = home,
    )
}
