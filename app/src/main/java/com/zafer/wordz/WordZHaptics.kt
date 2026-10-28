package com.zafer.wordz

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

internal class WordZHaptics(private val view: View) {
    fun select() {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun success() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    fun error() {
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }

    fun reward() {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}

@Composable
internal fun rememberWordZHaptics(): WordZHaptics {
    val view = LocalView.current
    return remember(view) { WordZHaptics(view) }
}
