package com.zafer.wordz

import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

internal fun Modifier.graphicsLayerCompat(
    alpha: Float = 1f,
    scaleX: Float = 1f,
    scaleY: Float = 1f,
    translationY: Float = 0f,
    rotationZ: Float = 0f,
): Modifier = this
    .alpha(alpha)
    .scale((scaleX + scaleY) / 2f)
    .offset(y = translationY.dp)
    .rotate(rotationZ)
