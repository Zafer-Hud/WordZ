package com.zafer.wordz

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier

/** Recreates game composition when a new play session starts on the same level. */
@Composable
internal fun Box(
    modifier: Modifier,
    key: Int,
    content: @Composable BoxScope.() -> Unit,
) {
    key(key) {
        androidx.compose.foundation.layout.Box(modifier = modifier, content = content)
    }
}
