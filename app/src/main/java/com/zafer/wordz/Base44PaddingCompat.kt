package com.zafer.wordz

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Suppress("unused")
fun Modifier.padding(top: Dp, horizontal: Dp): Modifier =
    androidx.compose.foundation.layout.padding(
        this,
        start = horizontal,
        top = top,
        end = horizontal,
        bottom = 0.dp,
    )
