package com.zafer.wordz

import androidx.compose.runtime.Composable

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
    B44V4GameScreen(
        levelIndex = levelIndex,
        coins = coins,
        hints = hints,
        completedBefore = completedBefore,
        onCoins = onCoins,
        onHints = onHints,
        onRewardXp = onRewardXp,
        onComplete = onComplete,
        back = back,
        next = next,
        home = home,
    )
}
