package com.zafer.wordz

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Fallback align modifier for composables that are not declared in a BoxScope.
 * BoxScope.align still takes precedence when a BoxScope is available.
 */
fun Modifier.align(alignment: Alignment): Modifier = this
